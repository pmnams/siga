-- Correção dos campos DATA da tabela CP_TOKEN para DATETIME

ALTER TABLE corporativo.cp_token
    MODIFY COLUMN DT_IAT DATETIME,
    MODIFY COLUMN DT_EXP DATETIME;
