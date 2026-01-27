-- Senha: admin123 (BCrypt hash)
INSERT INTO usuario (username, senha_hash, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/l5.P5PQnQbBxCrG5.vLke', 'ADMIN'),
('user', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/l5.P5PQnQbBxCrG5.vLke', 'USER');
