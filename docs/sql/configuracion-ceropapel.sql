-- Base de solicitudes. No modifica subrogancias ni llama al proveedor.
CREATE TABLE IF NOT EXISTS configuracion_ceropapel (
    id BIGINT NOT NULL,
    habilitada BOOLEAN NOT NULL DEFAULT FALSE,
    base_url VARCHAR(2048) NOT NULL,
    usuario VARCHAR(100) NOT NULL,
    clave_cifrada VARBINARY(4096) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    actualizado_en DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_configuracion_ceropapel_unica CHECK (id = 1)
);

INSERT INTO modulos (nombre)
SELECT 'CONFIGURACION CERO PAPEL'
WHERE NOT EXISTS (SELECT 1 FROM modulos WHERE nombre = 'CONFIGURACION CERO PAPEL');

-- El primer arranque inicializa la fila 1 usando el .env y cifrado AES-GCM.
-- Asignar el módulo desde el mantenedor de usuarios al administrador correspondiente.
