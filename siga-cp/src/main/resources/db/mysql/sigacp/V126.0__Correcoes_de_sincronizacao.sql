-- -----------------------------------------------
-- Correções de sincronização
-- -----------------------------------------------

-- Correção dos campos DATA da tabela CP_TOKEN para DATETIME
ALTER TABLE corporativo.cp_token
    MODIFY DT_IAT DATETIME;

ALTER TABLE corporativo.cp_token
    MODIFY DT_EXP DATETIME;

-- Marcador Documento Analisado com Grupo Null
UPDATE corporativo.cp_marcador
SET GRUPO_MARCADOR = 7
WHERE ID_MARCADOR = 1005;
