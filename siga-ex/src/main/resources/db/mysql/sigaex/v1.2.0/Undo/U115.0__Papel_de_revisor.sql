-- ------------------------------------------------------------------------------------------------
--  Remove novo papel para que Subscritores e Cossignatários possam ter acesso
--	temporario a Arvore Completa de Documentos.
-- ------------------------------------------------------------------------------------------------
delete from ex_papel where ID_PAPEL in (8, 9);
