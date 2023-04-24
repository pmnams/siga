-- -------------------------------------------------------------------------
--  Remove colunas com sigla do documento, id da última movimentação e data da última mov na ex_mobil
-- -------------------------------------------------------------------------
alter table siga.ex_mobil
    drop column DNM_SIGLA,
    drop column ID_ULT_MOV,
    drop column DNM_DT_ULT_MOV;
