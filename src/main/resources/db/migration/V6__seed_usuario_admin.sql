-- Usuario admin inicial 
INSERT INTO usuarios (nombre, correo, contrasena, rol)
VALUES ('Admin', 'admin@plageopa.local', 'admin123', 'ADMIN')
ON CONFLICT (correo) DO NOTHING;
