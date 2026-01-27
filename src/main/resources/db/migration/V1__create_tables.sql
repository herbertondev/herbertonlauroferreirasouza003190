-- Tabela de artistas (cantores solo e bandas)
CREATE TABLE artista (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('SOLO', 'BANDA')),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de álbuns
CREATE TABLE album (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    ano_lancamento INTEGER,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela associativa N:N entre artista e álbum
CREATE TABLE artista_album (
    artista_id BIGINT NOT NULL REFERENCES artista(id) ON DELETE CASCADE,
    album_id BIGINT NOT NULL REFERENCES album(id) ON DELETE CASCADE,
    PRIMARY KEY (artista_id, album_id)
);

-- Tabela de imagens de capa do álbum (MinIO)
CREATE TABLE album_imagem (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT NOT NULL REFERENCES album(id) ON DELETE CASCADE,
    bucket VARCHAR(100) NOT NULL,
    object_key VARCHAR(500) NOT NULL,
    content_type VARCHAR(100),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de regionais (sincronização externa)
CREATE TABLE regional (
    id BIGINT PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de usuários para autenticação JWT
CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'USER'
);

-- Índices para otimização de consultas
CREATE INDEX idx_artista_nome ON artista(nome);
CREATE INDEX idx_artista_tipo ON artista(tipo);
CREATE INDEX idx_album_titulo ON album(titulo);
CREATE INDEX idx_album_ano ON album(ano_lancamento);
CREATE INDEX idx_album_imagem_album_id ON album_imagem(album_id);
CREATE INDEX idx_regional_ativo ON regional(ativo);
