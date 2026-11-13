import copy
import importlib.util
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.dont_write_bytecode = True
spec = importlib.util.spec_from_file_location('release_runtime', Path(__file__).with_name('release_runtime.py'))
runtime = importlib.util.module_from_spec(spec)
spec.loader.exec_module(runtime)


class ReleaseRuntimeTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.directory = Path(self.temp.name)
        self.manifest = {'release': 'test-release'}
        self.service = {'directory': 'servicio', 'container': 'servicio-container', 'image': 'servicio:test-release'}
        self.original = {'Image': 'sha256:anterior', 'Id': 'abcdefghijklmno',
            'Config': {'Image': 'servicio:anterior', 'Env': ['ORIGEN=servidor']},
            'HostConfig': {'Binds': ['/datos:/datos'], 'PortBindings': {'8081/tcp': [{'HostPort': '8087'}]}},
            'NetworkSettings': {'Networks': {'laflorida': {'Aliases': ['servicio-container']}}}}
        (self.directory / 'before').mkdir()
        (self.directory / 'before/servicio-container.json').write_text(json.dumps(self.original))

    def test_repetir_deploy_saludable_no_reemplaza_ni_revierte_contenedores(self):
        current = {'Config': {'Image': self.service['image']}}
        with patch.object(runtime, 'HERE', self.directory), patch.object(runtime, 'exists', return_value=True), patch.object(runtime, 'inspect', return_value=current), \
                patch.object(runtime, 'wait_health') as health, patch.object(runtime, 'run') as run:
            runtime.deploy_one(self.manifest, self.service)
            health.assert_called_once_with(self.service)
            run.assert_not_called()

    def test_rechaza_contenedor_modificado_despues_del_respaldo(self):
        with patch.object(runtime, 'HERE', self.directory), patch.object(runtime, 'exists', return_value=False), \
                patch.object(runtime, 'inspect', return_value={'Image': 'sha256:otra'}), patch.object(runtime, 'run') as run:
            with self.assertRaises(RuntimeError): runtime.deploy_one(self.manifest, self.service)
            run.assert_not_called()

    def test_fallo_de_salud_invoca_rollback_y_conserva_puertos_volumenes_y_entorno(self):
        def fake_run(*args, **kwargs):
            if args[:3] == ('docker', 'image', 'inspect'):
                return b'[{"Config":{"Labels":{"version":"test-release"}}}]'
            return b''
        with patch.object(runtime, 'HERE', self.directory), patch.object(runtime, 'BASE', self.directory), \
                patch.object(runtime, 'exists', return_value=False), patch.object(runtime, 'inspect', return_value=copy.deepcopy(self.original)), \
                patch.object(runtime, 'run', side_effect=fake_run), patch.object(runtime, 'engine') as engine, \
                patch.object(runtime, 'wait_health', side_effect=RuntimeError('salud fallida')), \
                patch.object(runtime, 'rollback_one') as rollback:
            with self.assertRaisesRegex(RuntimeError, 'salud fallida'):
                runtime.deploy_one(self.manifest, self.service)
            config = engine.call_args.args[2]
            self.assertEqual(self.original['HostConfig'], config['HostConfig'])
            self.assertEqual(['ORIGEN=servidor'], config['Env'])
            self.assertEqual('servicio:test-release', config['Image'])
            rollback.assert_called_once_with(self.manifest, self.service)


if __name__ == '__main__':
    unittest.main()
