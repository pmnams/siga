--
-- Adiciona a coluna referente ao requerente do documento
--

alter table ex_documento
    add column ID_REQUERENTE int unsigned;


alter table ex_documento
    add constraint IXCF_DOC_REQUERENTE_FK
        foreign key (ID_REQUERENTE) references ex_requerente(ID_REQUERENTE);