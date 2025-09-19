-- Usuario admin inicial 
INSERT INTO usuarios (nombre, correo, contraseña, rol)
VALUES ('Admin', 'admin@plageopa.local', '{PLAIN}admin123', 'ADMIN')
ON CONFLICT (correo) DO NOTHING;
