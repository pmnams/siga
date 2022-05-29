/*-*****************************************************************************
 * Copyright (c) 2006 - 2011 SJRJ.
 *
 *     This file is part of SIGA.
 *
 *     SIGA is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     SIGA is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with SIGA.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package br.gov.jfrj.siga.ex.util;

import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.base.Correio;
import br.gov.jfrj.siga.base.Prop;
import br.gov.jfrj.siga.cp.model.enm.ITipoDeMovimentacao;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.*;
import br.gov.jfrj.siga.ex.bl.Ex;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import br.gov.jfrj.siga.hibernate.ExDao;
import br.gov.jfrj.siga.parser.PessoaLotacaoParser;
import org.jboss.logging.Logger;

import java.util.*;

public class Notificador {

    public static int TIPO_NOTIFICACAO_GRAVACAO = 1;
    public static int TIPO_NOTIFICACAO_CANCELAMENTO = 2;
    public static int TIPO_NOTIFICACAO_EXCLUSAO = 3;

    private static final Logger log = Logger.getLogger(Notificador.class);

    private static ExDao exDao() {
        return ExDao.getInstance();
    }


    /**
     * Método que notifica pessoas por email.
     *
     * @param destinatarios - String com uma lista de emails separados por ponto-e-vírgula. Também podem ser utilizadas siglas de pessoas ou lotações.
     * @param assunto       - Assunto do email
     * @param html          - HTML do email
     * @param txt           - Texto do email
     */
    public static void notificar(String destinatarios, String assunto, String html, String txt) throws AplicacaoException {
        HashSet<String> emails = new HashSet<>();
        List<Notificacao> notificacoes = new ArrayList<>();

        if (destinatarios == null)
            return;

        String[] addrs = destinatarios.split(";");
        for (String addr : addrs) {
            addr = addr.trim();
            DpPessoa p = exDao().getPessoaFromSigla(addr);
            if (p != null) {
                List<ExEmailNotificacao> emns = exDao().consultarEmailNotificacao(p, null);
                for (ExEmailNotificacao emn : emns) {
                    emails.add(emn.getEmail());
                }
                continue;
            }
            DpLotacao l = exDao().getLotacaoFromSigla(addr);
            if (l != null) {
                List<ExEmailNotificacao> emns = exDao().consultarEmailNotificacao(null, l);
                for (ExEmailNotificacao emn : emns) {
                    emails.add(emn.getEmail());
                }
                continue;
            }
            emails.add(addr);
        }

        Notificacao not = new Notificacao(new Destinatario(null, null, null, null, null, null, emails), html, txt, assunto);
        notificacoes.add(not);
        notificarPorEmail(notificacoes);
    }


    /**
     * Método que notifica as pessoas com perfis vinculados ao documento.
     *
     * @param tipoNotificacao - Se é uma gravação, cancelamento ou exclusão de movimentação.
     */
    public static void notificarDestinariosEmail(ExMovimentacao mov,
                                                 int tipoNotificacao) throws AplicacaoException {

        if (mov.getExMobil().isApensadoAVolumeDoMesmoProcesso())
            return;

        StringBuilder conteudo = new StringBuilder(); // armazena o corpo do
        // e-mail
        StringBuilder conteudoHTML = new StringBuilder(); // armazena o corpo
        // do e-mail no formato HTML

        // lista de destinatários com informações adicionais de perfil e código
        // da movimentação que adicionou o perfil para permitir o cancelamento.
        HashSet<Destinatario> destinatariosEmail = new HashSet<>();

        List<Notificacao> notificacoes = new ArrayList<>();

        for (PessoaLotacaoParser atendente : mov.mob().getAtendente()) {
            DpLotacao lotaAtendente = atendente.getLotacaoOuLotacaoPrincipalDaPessoa();
            if (mov.getExTipoMovimentacao() == (ExTipoDeMovimentacao.ANEXACAO_DE_ARQUIVO_AUXILIAR)
                    && lotaAtendente != null
                    && (mov.getLotaTitular() == null || !mov.getLotaTitular().equivale(lotaAtendente))) {
                try {
                    adicionarDestinatariosEmail(mov, destinatariosEmail, mov, null, lotaAtendente);
                } catch (Exception e) {
                    throw new RuntimeException("Erro ao enviar email de notificação de movimentação.", e);
                }
            }
        }

        if (mov.getExTipoMovimentacao() == (ExTipoDeMovimentacao.TRANSFERENCIA)
                || ((mov.getExTipoMovimentacao() == (ExTipoDeMovimentacao.ASSINATURA_DIGITAL_MOVIMENTACAO)
                || mov.getExTipoMovimentacao() == (ExTipoDeMovimentacao.ASSINATURA_MOVIMENTACAO_COM_SENHA))
                && (mov.getExMovimentacaoRef().getExTipoMovimentacao() == (ExTipoDeMovimentacao.DESPACHO_INTERNO_TRANSFERENCIA)
                || mov.getExMovimentacaoRef().getExTipoMovimentacao() == (ExTipoDeMovimentacao.DESPACHO_TRANSFERENCIA)))
        ) {
            DpLotacao lotacao;
            try {
                if (mov.getResp() != null)
                    lotacao = null;
                else lotacao = mov.getLotaResp();
                adicionarDestinatariosEmail(mov, destinatariosEmail, null, mov.getResp(), lotacao); /* verificar ExEmailNotificação também*/
            } catch (Exception e) {
                throw new RuntimeException("Erro ao enviar email de notificação de movimentação.", e);
            }
        }

        /*
         * Para cada movimentação do mobil geral (onde fica as movimentações
         * vinculações de perfis) verifica se: 1) A movimentação não está
         * cancelada; 2) Se a movimentação é uma vinculação de perfil; 3) Se há
         * uma configuração permitindo a notificação por e-mail.
         *
         * Caso TODAS as condições acima sejam verdadeiras, adiciona o e-mail à
         * lista de destinatários.
         * Cada notificação representa um perfil lido no mobil geral. Uma notificação pode  ter um ou
         * vários emails, dependendo da classe ExEmailNotificacao.
         */
        for (ExMovimentacao m : mov.getExDocumento().getMobilGeral()
                .getExMovimentacaoSet()) {
            if (!m.isCancelada()
                    && m.getExTipoMovimentacao() == ExTipoDeMovimentacao.VINCULACAO_PAPEL
                    && !m.getExPapel().getIdPapel().equals(ExPapel.PAPEL_REVISOR)) {

                try {
                    if (m.getSubscritor() != null) {
                        if (!m.getSubscritor().isFechada()) {

                            /*
                             * Se a movimentação é um cancelamento de uma
                             * movimentação que pode ser notificada, adiciona o
                             * e-mail.
                             */
                            if (mov.getExMovimentacaoRef() != null)
                                adicionarDestinatariosEmail(mov.getExMovimentacaoRef(), destinatariosEmail, m, m.getSubscritor().getPessoaAtual(), null); /* verificar ExEmailNotificação */
                            else
                                adicionarDestinatariosEmail(mov, destinatariosEmail, m, m.getSubscritor().getPessoaAtual(), null); /* verificar ExEmailNotificação também */
                        }
                    } else {
                        if (m.getLotaSubscritor() != null) {
                            /*
                             * Se a movimentação é um cancelamento de uma
                             * movimentação que pode ser notificada, adiciona o
                             * e-mail.
                             */
                            if (mov.getExMovimentacaoRef() != null)
                                adicionarDestinatariosEmail(mov.getExMovimentacaoRef(), destinatariosEmail, m, null, m.getLotaSubscritor().getLotacaoAtual()); /* verificar ExEmailNotificação tmb*/
                            else
                                adicionarDestinatariosEmail(mov, destinatariosEmail, m, null, m.getLotaSubscritor().getLotacaoAtual()); /* verificar ExEmailNotificação tmb*/
                        }
                    }
                } catch (Exception e) {
                    throw new AplicacaoException(
                            "Erro ao enviar email de notificação de movimentação.", 0,
                            e);
                }

            }
        }

        for (Destinatario dest : destinatariosEmail) {

            prepararTexto(dest, conteudo, conteudoHTML, mov,
                    tipoNotificacao);

            String html = conteudoHTML.toString();
            String txt = conteudo.toString();
            String assunto;

            if (Objects.equals(dest.tipo, "P"))
                assunto = "Notificação Automática -" + mov.getExMobil().getSigla() + "- Movimentação de Documento";
            else /* transferencia */
                assunto = "Documento transferido para " + dest.sigla;

            conteudoHTML.delete(0, conteudoHTML.length());
            conteudo.delete(0, conteudo.length());

            Notificacao not = new Notificacao(dest, html, txt, assunto);
            notificacoes.add(not);
        }

        notificarPorEmail(notificacoes);

    }

    /**
     * Inclui destinatários na lista baseando-se nas configurações existentes na
     * tabela EX_CONFIGURACAO
     *
     */

    private static void adicionarDestinatariosEmail(ExMovimentacao mov,
                                                    HashSet<Destinatario> destinatariosEmail, ExMovimentacao m, DpPessoa pess, DpLotacao lot)
            throws Exception {

        //	Destinatario dest = new Destinatario();
        HashSet<String> emailsTemp = new HashSet<>();
        String sigla;
        ExPapel papel = null;
        Long idMovPapel = null;

        List<ExEmailNotificacao> listaDeEmails = exDao().consultarEmailNotificacao(pess, lot);

        if (m != null) {
            papel = m.getExPapel();
            idMovPapel = m.getIdMov();
        }

        if (listaDeEmails.size() > 0) {

            for (ExEmailNotificacao emailNot : listaDeEmails) {
                // Caso exista alguma configuração com pessoaEmail, lotacaoEmail e email
                // nulos, significa que deve ser enviado para a pessoa ou para
                // todos da lotação

                if (emailNot.getPessoaEmail() == null  // configuração default
                        && emailNot.getLotacaoEmail() == null
                        && emailNot.getEmail() == null) {
                    if (emailNot.getDpPessoa() != null) {
                        if (!emailNot.getDpPessoa().isFechada()) {
                            if (m != null) { /* perfil */
                                if (temPermissao(mov.getExDocumento().getExFormaDocumento().getExTipoFormaDoc(),
                                        papel, emailNot.getDpPessoa(), mov.getExTipoMovimentacao()))
                                    emailsTemp.add(emailNot.getDpPessoa().getEmailPessoaAtual());
                            } else {  /* transferência */
                                if (temPermissao(emailNot.getDpPessoa(), emailNot.getDpPessoa().getLotacao(), mov.getExDocumento().getExModelo(), mov.getExTipoMovimentacao()))
                                    emailsTemp.add(emailNot.getDpPessoa().getEmailPessoaAtual());
                            }
                        }
                    } else {
                        for (DpPessoa pes : emailNot.getDpLotacao().getLotacaoAtual().getDpPessoaLotadosSet()) {
                            if (!pes.isFechada()) {
                                if (m != null) { /* perfil */
                                    if (temPermissao(mov.getExDocumento().getExFormaDocumento().getExTipoFormaDoc(),
                                            papel, pes, mov.getExTipoMovimentacao()))
                                        emailsTemp.add(pes.getEmailPessoaAtual());
                                } else { /* transferência */
                                    if (temPermissao(pes, pes.getLotacao(), mov.getExDocumento().getExModelo(), mov.getExTipoMovimentacao()))
                                        emailsTemp.add(pes.getEmailPessoaAtual());
                                }
                            }
                        }
                    }
                } else {
                    if (emailNot.getPessoaEmail() != null) { /* Mandar para pessoa */
                        if (!emailNot.getPessoaEmail().isFechada()) {
                            if (m != null) {/* perfil */
                                if (temPermissao(mov.getExDocumento().getExFormaDocumento().getExTipoFormaDoc(),
                                        papel, emailNot.getPessoaEmail(), mov.getExTipoMovimentacao()))
                                    emailsTemp.add(emailNot.getPessoaEmail().getEmailPessoaAtual());
                            } else { /* transferência */
                                if (temPermissao(emailNot.getPessoaEmail(), emailNot.getPessoaEmail().getLotacao(), mov.getExDocumento().getExModelo(), mov.getExTipoMovimentacao()))
                                    emailsTemp.add(emailNot.getPessoaEmail().getEmailPessoaAtual());
                            }
                        }
                    } else {
                        if (emailNot.getLotacaoEmail() != null) {
                            for (DpPessoa pes : emailNot.getLotacaoEmail().getLotacaoAtual().getDpPessoaLotadosSet()) {
                                if (!pes.isFechada()) {
                                    if (m != null) {/* perfil */
                                        if (temPermissao(mov.getExDocumento().getExFormaDocumento().getExTipoFormaDoc(),
                                                papel, pes, mov.getExTipoMovimentacao()))
                                            emailsTemp.add(pes.getEmailPessoaAtual());
                                    } else /* transferência */
                                        if (temPermissao(pes, pes.getLotacao(), mov.getExDocumento().getExModelo(), mov.getExTipoMovimentacao()))
                                            emailsTemp.add(pes.getEmailPessoaAtual());
                                }
                            }
                        } else {
                            emailsTemp.add(emailNot.getEmail());
                        }
                    }
                }
            }
        } else { /* não há ocorrencias em Ex_email_notificacao */
            if (pess != null) {
                if (!pess.isFechada()) {
                    if (m != null) { /* perfil */
                        if (temPermissao(mov.getExDocumento().getExFormaDocumento().getExTipoFormaDoc(),
                                papel, pess, mov.getExTipoMovimentacao()))
                            emailsTemp.add(pess.getEmailPessoaAtual());
                    } else {/* transferência */
                        if (temPermissao(pess, pess.getLotacao(), mov.getExDocumento().getExModelo(), mov.getExTipoMovimentacao()))
                            emailsTemp.add(pess.getEmailPessoaAtual());
                    }
                }
            } else {
                for (DpPessoa pes : lot.getLotacaoAtual().getDpPessoaLotadosSet()) {
                    if (!pes.isFechada()) {
                        if (m != null) { /* perfil */
                            if (temPermissao(mov.getExDocumento().getExFormaDocumento().getExTipoFormaDoc(),
                                    papel, pes, mov.getExTipoMovimentacao()))
                                emailsTemp.add(pes.getEmailPessoaAtual());
                        } else {/* transferência */
                            if (temPermissao(pes, pes.getLotacao(), mov.getExDocumento().getExModelo(), mov.getExTipoMovimentacao()))
                                emailsTemp.add(pes.getEmailPessoaAtual());
                        }
                    }
                }
            }
        }

        if (pess != null)
            sigla = pess.getPessoaAtual().getNomePessoa() + " (" + pess.getPessoaAtual().getSigla() + ")";
        else
            sigla = lot.getLotacaoAtual().getNomeLotacao() + " (" + lot.getLotacaoAtual().getSigla() + ")";

        if (m != null) /* perfil */ {
            if (!emailsTemp.isEmpty())
                destinatariosEmail.add(new Destinatario("P", sigla, papel != null ? papel.getDescPapel() : null,
                        papel != null ? idMovPapel : null, null, null, emailsTemp));
        } else { /* transferência */
            if (!emailsTemp.isEmpty())
                destinatariosEmail.add(new Destinatario("T", sigla, null, null, mov.getExMobil().getSigla(),
                        mov.getExDocumento().getDescrDocumento(), emailsTemp));
        }
    }

    private static boolean temPermissao(ExTipoFormaDoc tipoFormaDoc,
                                        ExPapel papel, DpPessoa pessoa, ITipoDeMovimentacao tipoMovimentacao) throws Exception {

        return (Ex.getInstance()
                .getConf()
                .podePorConfiguracao(
                        tipoFormaDoc,
                        papel,
                        pessoa,
                        tipoMovimentacao,
                        ExTipoDeConfiguracao.NOTIFICAR_POR_EMAIL));

    }

    private static boolean temPermissao(DpPessoa pessoa, DpLotacao lotacao, ExModelo modelo, ITipoDeMovimentacao idTpMov) {

        return (Ex.getInstance().getConf().podePorConfiguracao(pessoa,
                lotacao, modelo, idTpMov,
                ExTipoDeConfiguracao.NOTIFICAR_POR_EMAIL));

    }

    /*
     * Notifica os destinatários assincronamente.
     *
     * @param conteudo
     *            - texto do e-mail
     * @param conteudoHTML
     *            - texto do e-mail no formato HTML
     * @param destinatariosEmail
     *            - Conjunto de destinatários.
     */
    private static void notificarPorEmail(List<Notificacao> notificacoes) {
        // Se existirem destinatários, envia o e-mail. O e-mail é enviado
        // assincronamente.
        if (notificacoes.size() > 0) {
            CorreioThread t = new CorreioThread(notificacoes);
            t.start();
        }
    }

    /**
     * Monta o corpo do e-mail que será recebido pelas pessoas com perfis
     * vinculados.
     *
     * @param tipoNotificacao - Se é uma gravação, cancelamento ou exclusão de movimentação.
     */
    private static void prepararTexto(Destinatario dest,
                                      StringBuilder conteudo, StringBuilder conteudoHTML,
                                      ExMovimentacao mov, int tipoNotificacao) {

        if (Objects.equals(dest.tipo, "P")) {
            // conteúdo texto
            conteudo.append("Documento: ");
            conteudo.append(mov.getExMobil().getSigla());
            conteudo.append("\nDescrição:");
            conteudo.append(mov.getExDocumento().getDescrDocumento());
            conteudo.append("\nMovimentação:");
            if (tipoNotificacao == TIPO_NOTIFICACAO_GRAVACAO
                    || tipoNotificacao == TIPO_NOTIFICACAO_EXCLUSAO) {
                conteudo.append(mov.getExTipoMovimentacao().getDescr());
            }
            if (tipoNotificacao == TIPO_NOTIFICACAO_CANCELAMENTO) {
                conteudo.append(mov.getExMovimentacaoRef().getDescrTipoMovimentacao())
                        .append("(Cancelamento)");
            }
            if (mov.getCadastrante() != null) {
                conteudo.append("\nRealizada por '")
                        .append(mov.getCadastrante().getNomePessoa())
                        .append(" (Matrícula: ")
                        .append(mov.getCadastrante().getMatricula())
                        .append(")'.\n\n");

            }

            if (mov.getExTipoMovimentacao() == ExTipoDeMovimentacao.TRANSFERENCIA
                    || mov.getExTipoMovimentacao() == ExTipoDeMovimentacao.DESPACHO_TRANSFERENCIA) {
                if (mov.getResp() != null) {
                    conteudo.append("Destinatário:  <b>")
                            .append(mov.getResp().getNomePessoa())
                            .append(" (Matrícula: ")
                            .append(mov.getResp().getMatricula())
                            .append(") do (a) ")
                            .append(mov.getLotaResp().getNomeLotacao())
                            .append(" (sigla: ")
                            .append(mov.getLotaResp().getSigla())
                            .append(")</b>");
                } else {
                    conteudo.append("Lotação Destinatária: <b>")
                            .append(mov.getLotaResp().getNomeLotacao())
                            .append(" (sigla: ")
                            .append(mov.getLotaResp().getSigla())
                            .append(")</b>");
                }
            }
            conteudo.append("<p>Para visualizar o documento, clique <a href=\"")
                    .append(Prop.get("/sigaex.url"))
                    .append("/app/expediente/doc/exibir?sigla=")
                    .append(mov.getExDocumento().getSigla())
                    .append("\">aqui</a>.</p>");

            if (dest.papel != null) {
                conteudo.append("\n\nEste email foi enviado porque ")
                        .append(dest.sigla)
                        .append(" tem o perfil de '")
                        .append(dest.papel)
                        .append("' no documento ")
                        .append(mov.getExDocumento().getSigla())
                        .append(". Caso não deseje mais receber notificações desse documento, clique no link abaixo para se descadastrar:\n\n")
                        .append(Prop.get("/sigaex.url"))
                        .append("/app/expediente/mov/cancelar?id=")
                        .append(dest.idMovPapel);
            }


            // conteúdo html
            conteudoHTML.append("<html><body>")
                        .append("<p>Documento: <b>")
                        .append(mov.getExMobil().getSigla())
                        .append("</b></p>")
                        .append("<p>Descrição: <b>")
                        .append(mov.getExDocumento().getDescrDocumento()).append("</b></p>")
                        .append("<p>Movimentação: <b>");

            if (tipoNotificacao == TIPO_NOTIFICACAO_GRAVACAO) {
                conteudoHTML.append(mov.getExTipoMovimentacao().getDescr())
                        .append("</b></p>");
            }
            if (tipoNotificacao == TIPO_NOTIFICACAO_EXCLUSAO) {
                conteudoHTML.append(mov.getExTipoMovimentacao().getDescr())
                        .append(" (Excluída)</b></p>");
            }
            if (tipoNotificacao == TIPO_NOTIFICACAO_CANCELAMENTO) {
                conteudoHTML.append(mov.getExMovimentacaoRef().getDescrTipoMovimentacao())
                        .append("</b></p>.");
            }
            if (mov.getCadastrante() != null) {
                conteudoHTML.append("Realizada por <b>")
                        .append(mov.getCadastrante().getNomePessoa())
                        .append(" (Matrícula: ")
                        .append(mov.getCadastrante().getMatricula())
                        .append(")</b></p>");
            }

            if (mov.getExTipoMovimentacao() == ExTipoDeMovimentacao.TRANSFERENCIA
                    || mov.getExTipoMovimentacao() == ExTipoDeMovimentacao.DESPACHO_TRANSFERENCIA) {
                if (mov.getResp() != null) {
                    conteudoHTML.append("<p>Destinatário:  <b>")
                            .append(mov.getResp().getNomePessoa())
                            .append(" (Matrícula: ")
                            .append(mov.getResp().getMatricula())
                            .append(") do (a) ")
                            .append(mov.getLotaResp().getNomeLotacao())
                            .append(" (sigla: ")
                            .append(mov.getLotaResp().getSigla())
                            .append(")</b></p>");
                } else {
                    conteudoHTML.append("<p>Lotação Destinatária: <b>")
                            .append(mov.getLotaResp().getNomeLotacao())
                            .append(" (sigla: ")
                            .append(mov.getLotaResp().getSigla())
                            .append(")</b></p>");
                }
            }

            conteudoHTML.append("<p>Para visualizar o documento, ")
                    .append("clique <a href=\"")
                    .append(Prop.get("/sigaex.url")).append("/app/expediente/doc/exibir?sigla=")
                    .append(mov.getExDocumento().getSigla())
                    .append("\">aqui</a>.</p>");

            if (dest.papel != null) {
                conteudoHTML.append("<p>Este email foi enviado porque <b>")
                        .append(dest.sigla)
                        .append(" </b> tem o perfil de '")
                        .append(dest.papel)
                        .append("' no documento ")
                        .append(mov.getExDocumento().getSigla())
                        .append(". <br> Caso não deseje mais receber notificações desse documento, clique <a href=\"")
                        .append(Prop.get("/sigaex.url")).append("/app/expediente/mov/cancelar?id=")
                        .append(dest.idMovPapel)
                        .append("\">aqui</a> para descadastrar.</p>");
            }
            conteudoHTML.append("</body></html>");
        } else {
            String mensagemTeste = Ex.getInstance().getBL().mensagemDeTeste();

            conteudo.append("O documento ")
                    .append(dest.siglaMobil)
                    .append(", com descrição '")
                    .append(dest.descrDocumento)
                    .append("', foi transferido para ")
                    .append(dest.sigla)
                    .append(" e aguarda recebimento.\n\n")
                    .append("Para visualizar o documento, ")
                    .append("clique no link abaixo:\n\n")
                    .append(Prop.get("/sigaex.url"))
                    .append("/app/expediente/doc/exibir?sigla=")
                    .append(dest.siglaMobil);

            if (mensagemTeste != null)
                conteudo.append("\n ")
                        .append(mensagemTeste)
                        .append("\n");

            conteudoHTML.append("<html><body>")
                    .append("<p>O documento <b>")
                    .append(dest.siglaMobil)
                    .append("</b>, com descrição '<b>")
                    .append(dest.descrDocumento)
                    .append("</b>', foi transferido para <b>")
                    .append(dest.sigla)
                    .append("</b> e aguarda recebimento.</p>")
                    .append("<p>Para visualizar o documento, ")
                    .append("clique <a href=\"")
                    .append(Prop.get("/sigaex.url")).append("/app/expediente/doc/exibir?sigla=")
                    .append(dest.siglaMobil)
                    .append("\">aqui</a>.</p>");

            if (mensagemTeste != null)
                conteudoHTML.append("<p><b>")
                        .append(mensagemTeste)
                        .append("</b></p>");

            conteudoHTML.append("</body></html>");
        }
    }


    /**
     * Classe que representa um thread de envio de e-mail. Há a necessidade do
     * envio de e-mail ser assíncrono, caso contrário, o usuário sentirá uma
     * degradação de performance.
     *
     * @author kpf
     */
    static class CorreioThread extends Thread {

        private final List<Notificacao> notificacoes;

        public CorreioThread(List<Notificacao> notificacoes) {
            super();
            this.notificacoes = notificacoes;
        }

        @Override
        public void run() {
            String txt;
            String html;

            try {
                for (Notificacao not : notificacoes) {
                    txt = not.txt;
                    html = not.html;
                    Correio.enviar(null,
                            not.dest.emails.toArray(new String[0]),
                            not.assunto, txt, html);
                }

            } catch (Exception e) {
                log.error(e);
            }
        }
    }


    static class Notificacao {
        Destinatario dest;
        String html;
        String txt;
        String assunto;

        public Notificacao(Destinatario dest, String html, String txt, String assunto) {
            super();
            this.dest = dest;
            this.html = html;
            this.txt = txt;
            this.assunto = assunto;
        }
    }

    static class Destinatario implements Comparable<Destinatario> {

        public String tipo;
        public String sigla;
        public String papel;
        public Long idMovPapel;
        public String siglaMobil;
        public String descrDocumento;
        public HashSet<String> emails;

        public Destinatario(String tipo, String sigla, String papel,
                            Long idMovPapel, String siglaMobil, String descrDocumento,
                            HashSet<String> emails) {
            super();
            this.tipo = tipo;
            this.sigla = sigla;
            this.papel = papel;
            this.idMovPapel = idMovPapel;
            this.siglaMobil = siglaMobil;
            this.descrDocumento = descrDocumento;
            this.emails = emails;
        }

        public int compareTo(Destinatario o) {
            // TODO Auto-generated method stub
//			return email.compareTo(o.email);
            return 0; /* RETIRAR */
        }

    }

}
