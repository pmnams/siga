-- Corrige marcador Documento Analisado com Grupo Null

UPDATE cp_marcador
SET GRUPO_MARCADOR = 7
WHERE DESCR_MARCADOR = 'Documento Analisado'
