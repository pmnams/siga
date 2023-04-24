--
-- Remove a coluna referente ao requerente do documento
--

alter table ex_documento
    drop column ID_REQUERENTE,
    drop foreign key IXCF_DOC_REQUERENTE_FK;
