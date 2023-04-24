-- Amplia o campo para 10 posiçoes.
--
ALTER TABLE corporativo.dp_pessoa
    CHANGE COLUMN `SESB_PESSOA` `SESB_PESSOA` VARCHAR(10);