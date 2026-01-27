-- Adiciona id_externo para referência da API e permite id auto-gerado
-- 1. Adicionar coluna id_externo
ALTER TABLE regional ADD COLUMN id_externo BIGINT;

-- 2. Popular id_externo com valores atuais de id (migração de dados)
UPDATE regional SET id_externo = id;

-- 3. Tornar id_externo NOT NULL
ALTER TABLE regional ALTER COLUMN id_externo SET NOT NULL;

-- 4. Criar sequência para id auto-gerado (começando após IDs existentes)
CREATE SEQUENCE IF NOT EXISTS regional_seq START WITH 10000;

-- 6. Índice para busca eficiente por id_externo e ativo
CREATE INDEX idx_regional_id_externo ON regional(id_externo);
CREATE INDEX idx_regional_id_externo_ativo ON regional(id_externo, ativo);
