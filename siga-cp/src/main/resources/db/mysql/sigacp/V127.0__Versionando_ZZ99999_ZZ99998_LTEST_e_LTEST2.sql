-- Substitui ZZ99999, ZZ99998, LTEST e LTEST2 por versões mais novas com a mesma id inicial. Isso serve para facilitar os testes.

DELIMITER $$

-- Versiona Lotacoes TESTES
CREATE PROCEDURE versionaLotacaoLTEST(
)
BEGIN
	DECLARE done INT DEFAULT FALSE;
    DECLARE idLotacao INT;
    DECLARE lotacao_ltest CURSOR FOR SELECT ID_LOTACAO FROM corporativo.dp_lotacao WHERE ID_ORGAO_USU = 999999999 and DATA_FIM_LOT is null;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

OPEN lotacao_ltest;
versionaLotacao: LOOP
        FETCH lotacao_ltest INTO idLotacao;
		IF done THEN
		  LEAVE versionaLotacao;
END IF;

UPDATE corporativo.dp_lotacao
SET DATA_FIM_LOT = current_timestamp()
WHERE ID_LOTACAO = idLotacao;

INSERT INTO corporativo.dp_lotacao
(`DATA_INI_LOT`,`DATA_FIM_LOT`,`NOME_LOTACAO`,`ID_LOTACAO_PAI`,`SIGLA_LOTACAO`,`ID_ORGAO_USU`,`IDE_LOTACAO`,`ID_LOTACAO_INI`,`ID_TP_LOTACAO`,`ID_LOCALIDADE`,`IS_EXTERNA_LOTACAO`,`HIS_IDC_INI`,`HIS_IDC_FIM`,`IS_SUSPENSA`)
SELECT current_timestamp(),NULL,`NOME_LOTACAO`,`ID_LOTACAO_PAI`,`SIGLA_LOTACAO`,`ID_ORGAO_USU`,`IDE_LOTACAO`,`ID_LOTACAO_INI`,`ID_TP_LOTACAO`,`ID_LOCALIDADE`,`IS_EXTERNA_LOTACAO`,`HIS_IDC_INI`,`HIS_IDC_FIM`,`IS_SUSPENSA`
FROM corporativo.dp_lotacao
WHERE ID_LOTACAO = idLotacao;
END LOOP versionaLotacao;
CLOSE lotacao_ltest;
END$$

-- Versiona Lotacoes Usuários Testes
CREATE PROCEDURE versionaUsuarioZZ(
)
BEGIN
	DECLARE done INT DEFAULT FALSE;
    DECLARE idPessoa INT;
	DECLARE pessoa_test CURSOR FOR SELECT ID_PESSOA FROM corporativo.dp_pessoa WHERE ID_ORGAO_USU = 999999999 and DATA_FIM_PESSOA is null;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;


OPEN pessoa_test;
versionaPessoa: LOOP
        FETCH pessoa_test INTO idPessoa;
		IF done THEN
		  LEAVE versionaPessoa;
END IF;

UPDATE corporativo.dp_pessoa
SET DATA_FIM_PESSOA = current_timestamp()
WHERE ID_PESSOA = idPessoa;

INSERT INTO corporativo.dp_pessoa
(`DATA_INI_PESSOA`,`DATA_FIM_PESSOA`,`CPF_PESSOA`,`NOME_PESSOA`,`DATA_NASC_PESSOA`,`MATRICULA`,`ID_LOTACAO`,`ID_CARGO`,`ID_FUNCAO_CONFIANCA`,`SESB_PESSOA`,`EMAIL_PESSOA`,`TP_SERVIDOR_PESSOA`,`SIGLA_PESSOA`,`SEXO_PESSOA`,`GRAU_INSTRUCAO_PESSOA`,`TP_SANGUINEO_PESSOA`,`NACIONALIDADE_PESSOA`,`DATA_POSSE_PESSOA`,`DATA_NOMEACAO_PESSOA`,`DATA_PUBLICACAO_PESSOA`,`DATA_INICIO_EXERCICIO_PESSOA`,`ATO_NOMEACAO_PESSOA`,`SITUACAO_FUNCIONAL_PESSOA`,`ID_PROVIMENTO`,`NATURALIDADE_PESSOA`,`FG_IMPRIME_END`,`DSC_PADRAO_REFERENCIA_PESSOA`,`ID_ORGAO_USU`,`IDE_PESSOA`,`ID_PESSOA_INICIAL`,`ENDERECO_PESSOA`,`BAIRRO_PESSOA`,`CIDADE_PESSOA`,`CEP_PESSOA`,`TELEFONE_PESSOA`,`RG_PESSOA`,`RG_ORGAO_PESSOA`,`RG_DATA_EXPEDICAO_PESSOA`,`RG_UF_PESSOA`,`ID_ESTADO_CIVIL`,`ID_TP_PESSOA`,`NOME_EXIBICAO`,`HIS_IDC_INI`,`HIS_IDC_FIM`,`NOME_PESSOA_AI`)
SELECT current_timestamp(),NULL,`CPF_PESSOA`,`NOME_PESSOA`,`DATA_NASC_PESSOA`,`MATRICULA`,`ID_LOTACAO`,`ID_CARGO`,`ID_FUNCAO_CONFIANCA`,`SESB_PESSOA`,`EMAIL_PESSOA`,`TP_SERVIDOR_PESSOA`,`SIGLA_PESSOA`,`SEXO_PESSOA`,`GRAU_INSTRUCAO_PESSOA`,`TP_SANGUINEO_PESSOA`,`NACIONALIDADE_PESSOA`,`DATA_POSSE_PESSOA`,`DATA_NOMEACAO_PESSOA`,`DATA_PUBLICACAO_PESSOA`,`DATA_INICIO_EXERCICIO_PESSOA`,`ATO_NOMEACAO_PESSOA`,`SITUACAO_FUNCIONAL_PESSOA`,`ID_PROVIMENTO`,`NATURALIDADE_PESSOA`,`FG_IMPRIME_END`,`DSC_PADRAO_REFERENCIA_PESSOA`,`ID_ORGAO_USU`,`IDE_PESSOA`,`ID_PESSOA_INICIAL`,`ENDERECO_PESSOA`,`BAIRRO_PESSOA`,`CIDADE_PESSOA`,`CEP_PESSOA`,`TELEFONE_PESSOA`,`RG_PESSOA`,`RG_ORGAO_PESSOA`,`RG_DATA_EXPEDICAO_PESSOA`,`RG_UF_PESSOA`,`ID_ESTADO_CIVIL`,`ID_TP_PESSOA`,`NOME_EXIBICAO`,`HIS_IDC_INI`,`HIS_IDC_FIM`,`NOME_PESSOA_AI`
FROM corporativo.dp_pessoa
WHERE ID_PESSOA = idPessoa;

END LOOP versionaPessoa;
CLOSE pessoa_test;

END$$
DELIMITER ;

-- Chama e Dropa proc para versionar LTEST
CALL versionaLotacaoLTEST();
DROP PROCEDURE versionaLotacaoLTEST;

-- Chama e Dropa proc para versionar Usuarios ZZ
CALL versionaUsuarioZZ();
DROP PROCEDURE versionaUsuarioZZ;