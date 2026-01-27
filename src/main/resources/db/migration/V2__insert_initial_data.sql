-- Inserir artistas
INSERT INTO artista (nome, tipo) VALUES
('Serj Tankian', 'SOLO'),
('Mike Shinoda', 'SOLO'),
('Michel Teló', 'SOLO'),
('Guns N'' Roses', 'BANDA');

-- Inserir álbuns do Serj Tankian
INSERT INTO album (titulo, ano_lancamento) VALUES
('Harakiri', 2012),
('Black Blooms', 2019),
('The Rough Dog', 2018);

-- Inserir álbuns do Mike Shinoda
INSERT INTO album (titulo, ano_lancamento) VALUES
('The Rising Tied', 2005),
('Post Traumatic', 2018),
('Post Traumatic EP', 2018),
('Where''d You Go', 2006);

-- Inserir álbuns do Michel Teló
INSERT INTO album (titulo, ano_lancamento) VALUES
('Bem Sertanejo', 2014),
('Bem Sertanejo - O Show (Ao Vivo)', 2015),
('Bem Sertanejo - (1a Temporada) - EP', 2014);

-- Inserir álbuns do Guns N' Roses
INSERT INTO album (titulo, ano_lancamento) VALUES
('Use Your Illusion I', 1991),
('Use Your Illusion II', 1991),
('Greatest Hits', 2004);

-- Associar artistas aos álbuns
-- Serj Tankian (id=1) com seus álbuns (ids 1-3)
INSERT INTO artista_album (artista_id, album_id)
SELECT 1, id FROM album WHERE titulo IN ('Harakiri', 'Black Blooms', 'The Rough Dog');

-- Mike Shinoda (id=2) com seus álbuns (ids 4-7)
INSERT INTO artista_album (artista_id, album_id)
SELECT 2, id FROM album WHERE titulo IN ('The Rising Tied', 'Post Traumatic', 'Post Traumatic EP', 'Where''d You Go');

-- Michel Teló (id=3) com seus álbuns (ids 8-10)
INSERT INTO artista_album (artista_id, album_id)
SELECT 3, id FROM album WHERE titulo IN ('Bem Sertanejo', 'Bem Sertanejo - O Show (Ao Vivo)', 'Bem Sertanejo - (1a Temporada) - EP');

-- Guns N' Roses (id=4) com seus álbuns (ids 11-13)
INSERT INTO artista_album (artista_id, album_id)
SELECT 4, id FROM album WHERE titulo IN ('Use Your Illusion I', 'Use Your Illusion II', 'Greatest Hits');
