-- Correções necessárias referentes a versão V64 e a permissão relacionada
-- a edição de locação com documentos.

INSERT INTO cp_servico (SIGLA_SERVICO, DESC_SERVICO, ID_SERVICO_PAI, ID_TP_SERVICO, LABEL_SERVICO)
VALUES ('SIGA-GI-CAD_ORGAO_USUARIO', 'Cadastrar Orgãos Usuário', 15, 2, NULL),
       ('SIGA-GI-CAD_CARGO', 'Cadastrar Cargo', 15, 2, NULL),
       ('SIGA-GI-CAD_LOTACAO', 'Cadastrar Lotação', 15, 2, NULL),
       ('SIGA-GI-CAD_FUNCAO', 'Cadastrar Função de Confiança', 15, 2, NULL),
       ('SIGA-GI-CAD_PESSOA', 'Cadastrar Pessoa', 15, 2, NULL);

-- Cadastro de Pessoa
UPDATE cp_servico as dest, cp_servico as src
SET dest.ID_SERVICO_PAI = src.ID_SERVICO
WHERE dest.SIGLA_SERVICO = 'SIGA-GI-CAD_PESSOA-EXP_DADOS'
  and src.SIGLA_SERVICO = 'SIGA-GI-CAD_PESSOA';

-- Cadastro de Cargo
UPDATE cp_servico as dest, cp_servico as src
SET dest.ID_SERVICO_PAI = src.ID_SERVICO
WHERE dest.SIGLA_SERVICO = 'SIGA-GI-CAD_CARGO-EXP_DADOS'
  and src.SIGLA_SERVICO = 'SIGA-GI-CAD_CARGO';

-- Cadastro de Função de Confiança
UPDATE cp_servico as dest, cp_servico as src
SET dest.ID_SERVICO_PAI = src.ID_SERVICO
WHERE dest.SIGLA_SERVICO = 'SIGA-GI-CAD_FUNCAO-EXP_DADOS'
  and src.SIGLA_SERVICO = 'SIGA-GI-CAD_FUNCAO';

-- Cadastro de Lotação
UPDATE cp_servico as dest, cp_servico as src
SET dest.ID_SERVICO_PAI = src.ID_SERVICO
WHERE dest.SIGLA_SERVICO = 'SIGA-GI-CAD_LOTACAO-EXP_DADOS'
  and src.SIGLA_SERVICO = 'SIGA-GI-CAD_LOTACAO';

-- Complementação V85
INSERT INTO corporativo.cp_servico (SIGLA_SERVICO, DESC_SERVICO, ID_SERVICO_PAI, ID_TP_SERVICO)
SELECT 'SIGA-GI-CAD_LOTACAO-ALT', 'Alterar Lotação com Documentos', max(id_servico), '2'
FROM corporativo.cp_servico
WHERE SIGLA_SERVICO = 'SIGA-GI-CAD_LOTACAO';
