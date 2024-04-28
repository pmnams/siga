--
-- Adiciona os campos de data na tabela de armazenamento blob do sistema

ALTER TABLE siga_blob ADD CREATED_AT TIMESTAMP, ADD UPDATED_AT TIMESTAMP;
