--
-- Adiciona a coluna referente ao requerente do documento
--

ALTER TABLE siga.`ex_documento`
    ADD COLUMN ID_REQUERENTE int UNSIGNED;


ALTER TABLE siga.`ex_documento`
    ADD CONSTRAINT IXCF_DOC_REQUERENTE_FK
        FOREIGN KEY (ID_REQUERENTE) REFERENCES ex_requerente(ID_REQUERENTE);
