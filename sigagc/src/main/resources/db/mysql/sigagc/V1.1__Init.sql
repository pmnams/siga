SET autocommit = 0;

INSERT INTO sigagc.gc_tipo_movimentacao (NOME_TIPO_MOVIMENTACAO)
VALUES ('Criação');
INSERT INTO sigagc.gc_tipo_movimentacao (NOME_TIPO_MOVIMENTACAO)
VALUES ('Concluído');
INSERT INTO sigagc.gc_tipo_movimentacao (NOME_TIPO_MOVIMENTACAO)
VALUES ('Cancelado');
INSERT INTO sigagc.gc_tipo_movimentacao (NOME_TIPO_MOVIMENTACAO)
VALUES ('Solicitação de revisão');
INSERT INTO sigagc.gc_tipo_movimentacao (NOME_TIPO_MOVIMENTACAO)
VALUES ('Revisado');
INSERT INTO sigagc.gc_tipo_movimentacao (NOME_TIPO_MOVIMENTACAO)
VALUES ('Notificação');
INSERT INTO sigagc.gc_tipo_movimentacao (NOME_TIPO_MOVIMENTACAO)
VALUES ('Ciente');
INSERT INTO sigagc.gc_tipo_movimentacao (NOME_TIPO_MOVIMENTACAO)
VALUES ('Classificação');
INSERT INTO sigagc.gc_tipo_movimentacao (NOME_TIPO_MOVIMENTACAO)
VALUES ('Interesse');
INSERT INTO sigagc.gc_tipo_movimentacao (NOME_TIPO_MOVIMENTACAO)
VALUES ('Edição');
INSERT INTO sigagc.gc_tipo_movimentacao (NOME_TIPO_MOVIMENTACAO)
VALUES ('Visita');
INSERT INTO sigagc.gc_tipo_movimentacao (NOME_TIPO_MOVIMENTACAO)
VALUES ('Cancelamento de movimentação');
INSERT INTO sigagc.gc_tipo_movimentacao (NOME_TIPO_MOVIMENTACAO)
VALUES ('Anexação de arquivo');
INSERT INTO sigagc.gc_tipo_movimentacao (NOME_TIPO_MOVIMENTACAO)
VALUES ('Duplicado');
INSERT INTO sigagc.gc_tipo_informacao (ARQUIVO, NOME_TIPO_INFORMACAO)
VALUES (NULL, 'Registro de Conhecimento');
INSERT INTO sigagc.gc_tipo_tag (NOME_TIPO_TAG)
VALUES ('Classificação');
INSERT INTO sigagc.gc_tipo_tag (NOME_TIPO_TAG)
VALUES ('Marcador');
INSERT INTO sigagc.gc_tipo_tag (NOME_TIPO_TAG)
VALUES ('Âncora');
INSERT INTO sigagc.gc_acesso (NOME_ACESSO)
VALUES ('Público');
INSERT INTO sigagc.gc_acesso (NOME_ACESSO)
VALUES ('Restrito ao órgão');
INSERT INTO sigagc.gc_acesso (NOME_ACESSO)
VALUES ('Lotação e superiores');
INSERT INTO sigagc.gc_acesso (NOME_ACESSO)
VALUES ('Lotação e inferiores');
INSERT INTO sigagc.gc_acesso (NOME_ACESSO)
VALUES ('Lotação');
INSERT INTO sigagc.gc_acesso (NOME_ACESSO)
VALUES ('Pessoal');
INSERT INTO sigagc.gc_acesso (NOME_ACESSO)
VALUES ('Lotação e Grupo');

INSERT INTO sigagc.gc_arquivo (conteudo_tipo, titulo, conteudo)
VALUES (
           'text/html',
           'Template para Erro Conhecido',
           NULL
       );
INSERT INTO sigagc.gc_tipo_informacao (NOME_TIPO_INFORMACAO, arquivo)
VALUES (
           'Erro Conhecido',
           (
               SELECT max(id_conteudo)
               FROM sigagc.gc_arquivo
           )
       );

INSERT INTO sigagc.gc_arquivo (conteudo_tipo, titulo, conteudo)
VALUES ('text/html', 'Template para Procedimento', NULL);
INSERT INTO sigagc.gc_tipo_informacao (NOME_TIPO_INFORMACAO, arquivo)
VALUES (
           'Procedimento',
           (
               SELECT max(id_conteudo)
               FROM sigagc.gc_arquivo
           )
       );

INSERT INTO sigagc.gc_tipo_informacao (NOME_TIPO_INFORMACAO, arquivo)
VALUES ('Ponto de Entrada', NULL);

INSERT INTO sigagc.gc_arquivo (conteudo_tipo, titulo, conteudo)
VALUES (
           'text/html',
           'Template para Processo de Trabalho',
           NULL
       );
INSERT INTO sigagc.gc_tipo_informacao (nome_tipo_informacao, arquivo)
VALUES (
           'Processo de Trabalho',
           (
               SELECT max(id_conteudo)
               FROM sigagc.gc_arquivo
           )
       );

INSERT INTO sigagc.gc_papel(desc_papel)
VALUES ('Executor');
INSERT INTO sigagc.gc_papel(desc_papel)
VALUES ('Interessado');
INSERT INTO sigagc.gc_tipo_movimentacao (NOME_TIPO_MOVIMENTACAO)
VALUES ('Definição de Perfil');
INSERT INTO sigagc.gc_acesso (NOME_ACESSO)
VALUES ('Ostensivo');
COMMIT;
