-- -------------------------------------------------------
-- Aumenta o tamanho máximo do  conteúdo de um documento
-- -------------------------------------------------------

ALTER TABLE siga.ex_documento
    MODIFY CONTEUDO_BLOB_DOC MEDIUMBLOB;
