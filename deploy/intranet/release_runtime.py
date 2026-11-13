#!/usr/bin/env python3
"""Activa una entrega de intranet conservando configuración y contenedores para rollback."""
import copy
import fcntl
import datetime
import hashlib
import http.client
import json
import os
from pathlib import Path
import re
import secrets
import shutil
import socket
import subprocess
import sys
import time
import urllib.request
import urllib.parse
import base64

BASE = Path('/home/desarrollo/intranet')
HERE = Path(__file__).resolve().parent
MANIFEST = HERE / 'manifest.json'


def run(*args, **kwargs):
    return subprocess.check_output(args, stderr=subprocess.STDOUT, **kwargs)


def inspect(name):
    return json.loads(run('docker', 'inspect', name))[0]


def exists(name):
    return subprocess.run(['docker', 'container', 'inspect', name], stdout=subprocess.DEVNULL,
                          stderr=subprocess.DEVNULL).returncode == 0


def save_json(path, data):
    path.write_text(json.dumps(data, indent=2))
    path.chmod(0o600)


def stamp():
    return datetime.datetime.now(datetime.timezone.utc).isoformat()


def digest(path):
    result = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            result.update(block)
    return result.hexdigest()


def engine(method, path, body=None):
    connection = http.client.HTTPConnection('localhost', timeout=60)
    sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
    sock.connect('/var/run/docker.sock')
    connection.sock = sock
    connection.request(method, path, body=json.dumps(body) if body is not None else None,
                       headers={'Content-Type': 'application/json'})
    response = connection.getresponse()
    raw = response.read()
    connection.close()
    if response.status >= 400:
        raise RuntimeError('Docker rechazó la operación ' + method + ' ' + path + ': ' + str(response.status))
    return json.loads(raw) if raw else None


def environment(config):
    return dict(item.split('=', 1) for item in config['Config']['Env'] if '=' in item)


def prepare(manifest):
    snapshots = HERE / 'before'
    if snapshots.exists():
        raise RuntimeError('El respaldo ya existe; no se sobrescribe')
    snapshots.mkdir(mode=0o700)
    aliases = []
    for service in manifest['services']:
        name = service['container']
        data = inspect(name)
        save_json(snapshots / (name + '.json'), data)
        config_dir = snapshots / service['directory']
        config_dir.mkdir(mode=0o700)
        for filename in ['deploy.sh', '.env']:
            source = BASE / service['directory'] / filename
            if source.exists():
                shutil.copy2(source, config_dir / filename)
                (config_dir / filename).chmod(0o600)
        alias = service['repository'] + ':rollback-' + manifest['release']
        run('docker', 'tag', data['Image'], alias)
        aliases.append(alias)
    nginx = Path('/etc/nginx/sites-enabled/test')
    if nginx.exists():
        (snapshots / 'nginx.sha256').write_text(digest(nginx))
    run('docker', 'save', '-o', str(HERE / 'rollback-images.tar'), *aliases)
    (HERE / 'rollback-images.sha256').write_text(digest(HERE / 'rollback-images.tar') + '  rollback-images.tar\n')
    for service in manifest['services']:
        if service['directory'] not in ['funcionarios', 'departamentos', 'new-solicitudes']:
            continue
        original = json.loads((snapshots / (service['container'] + '.json')).read_text())
        env = environment(original)
        database = env.get('DB', 'departamentos')
        if not re.fullmatch(r'[a-zA-Z0-9_]+', database):
            raise RuntimeError('Nombre de base inválido para respaldo')
        target = HERE / (database + '-before.sql')
        process_env = os.environ.copy()
        process_env['MYSQL_PWD'] = env['DB_PASSWORD']
        with target.open('wb') as output:
            result = subprocess.run(['mysqldump', '--host=' + env['HOST'], '--user=' + env['DB_USER'],
                '--single-transaction', '--no-tablespaces', '--set-gtid-purged=OFF', database],
                env=process_env, stdout=output, stderr=subprocess.PIPE)
        if result.returncode:
            raise RuntimeError('No se pudo respaldar la base ' + database + '; no se despliega')
        target.chmod(0o600)
        (HERE / (database + '-before.sha256')).write_text(digest(target) + '  ' + target.name + '\n')
    save_json(HERE / 'prepared.json', {'preparedAt': stamp(), 'release': manifest['release']})
    print('Respaldos completos: imágenes, configuración y tres bases de datos.', flush=True)


def configure_newsolicitudes():
    path = BASE / 'new-solicitudes' / '.env'
    text = path.read_text()
    values = dict(line.split('=', 1) for line in text.splitlines() if '=' in line and not line.startswith('#'))
    key = values.get('CEROPAPEL_ENCRYPTION_KEY', '')
    if key:
        if len(base64.b64decode(key, validate=True)) != 32:
            raise RuntimeError('La clave maestra existente no es válida; no se cambia automáticamente')
        return
    # No reemplazar una clave existente ni copiar claves del entorno local.
    with path.open('a') as output:
        output.write('\nCEROPAPEL_ENCRYPTION_KEY=' + base64.b64encode(secrets.token_bytes(32)).decode() + '\n')
    path.chmod(0o600)


def install_wrappers(manifest):
    for service in manifest['services']:
        target = BASE / service['directory'] / 'deploy.sh'
        target.write_text('#!/usr/bin/env bash\nset -Eeuo pipefail\nexec python3 ' + str(HERE / 'release_runtime.py')
                          + ' "${1:-deploy}" ' + service['directory'] + '\n')
        target.chmod(0o750)


def wait_health(service):
    url = 'http://127.0.0.1:' + str(service['port']) + service['health']
    for attempt in range(75):
        try:
            with urllib.request.urlopen(url, timeout=5) as response:
                body = response.read()
                if service['health'].endswith('/health') and json.loads(body).get('status') != 'UP':
                    raise RuntimeError('Salud distinta de UP')
                if response.status == 200:
                    return
        except Exception:
            pass
        time.sleep(2)
    raise RuntimeError('No superó salud: ' + service['directory'])


def deploy_one(manifest, service):
    original = json.loads((HERE / 'before' / (service['container'] + '.json')).read_text())
    name = service['container']
    backup = name + '-backup-' + manifest['release']
    if exists(backup):
        raise RuntimeError('Ya existe el contenedor de respaldo: ' + backup)
    if inspect(name)['Image'] != original['Image']:
        raise RuntimeError('El contenedor cambió desde el respaldo: ' + name)
    run('docker', 'image', 'inspect', service['image'])
    config = copy.deepcopy(original['Config'])
    config['Image'] = service['image']
    config['Hostname'] = ''
    config['HostConfig'] = copy.deepcopy(original['HostConfig'])
    labels = json.loads(run('docker', 'image', 'inspect', service['image']))[0]['Config'].get('Labels') or {}
    config['Labels'] = {**(config.get('Labels') or {}), **labels}
    env_path = BASE / service['directory'] / '.env'
    env = environment(original)
    if env_path.exists():
        for line in env_path.read_text().splitlines():
            if line and not line.startswith('#') and '=' in line:
                key, value = line.split('=', 1)
                env[key] = value
    config['Env'] = [key + '=' + value for key, value in env.items()]
    endpoints = {}
    for network, settings in original['NetworkSettings']['Networks'].items():
        aliases = [a for a in settings.get('Aliases') or [] if a != original['Id'][:12]]
        endpoints[network] = {'Aliases': aliases} if network not in ['bridge', 'host', 'none'] else {}
    config['NetworkingConfig'] = {'EndpointsConfig': endpoints}
    run('docker', 'rename', name, backup)
    try:
        run('docker', 'stop', backup)
        engine('POST', '/containers/create?name=' + urllib.parse.quote(name), config)
        run('docker', 'start', name)
        wait_health(service)
        save_json(HERE / (name + '-after.json'), inspect(name))
        print(service['directory'] + ': UP ' + service['image'], flush=True)
    except BaseException:
        rollback_one(manifest, service)
        raise


def rollback_one(manifest, service):
    name = service['container']
    backup = name + '-backup-' + manifest['release']
    if exists(backup):
        if exists(name):
            with (HERE / (name + '-failed.log')).open('wb') as log:
                subprocess.run(['docker', 'logs', '--tail', '150', name], stdout=log, stderr=log)
            run('docker', 'rm', '-f', name)
        run('docker', 'rename', backup, name)
        run('docker', 'start', name)
    config_dir = HERE / 'before' / service['directory']
    for filename in ['deploy.sh', '.env']:
        source = config_dir / filename
        if source.exists():
            shutil.copy2(source, BASE / service['directory'] / filename)
    print(service['directory'] + ': versión anterior restaurada', flush=True)


def main():
    os.umask(0o077)
    lock = (BASE / '.deploy.lock').open('a')
    fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
    manifest = json.loads(MANIFEST.read_text())
    mode = sys.argv[1]
    services = manifest['services']
    if len(sys.argv) > 2:
        services = [s for s in services if s['directory'] == sys.argv[2]]
        if not services:
            raise RuntimeError('Servicio desconocido')
    if mode == 'prepare':
        prepare(manifest)
    elif mode == 'deploy':
        if not (HERE / 'prepared.json').exists():
            raise RuntimeError('Faltan respaldos completos')
        try:
            if len(services) == len(manifest['services']):
                configure_newsolicitudes()
                install_wrappers(manifest)
            for service in services:
                deploy_one(manifest, service)
            save_json(HERE / 'activated.json', {'activatedAt': stamp(), 'release': manifest['release']})
        except BaseException:
            for service in reversed(manifest['services']):
                rollback_one(manifest, service)
            raise
    elif mode == 'rollback':
        for service in reversed(services):
            rollback_one(manifest, service)
    else:
        raise RuntimeError('Modo esperado: prepare, deploy o rollback')


if __name__ == '__main__':
    main()
