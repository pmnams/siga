/*-****************************************************************************
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
package br.gov.jfrj.siga.base;

import org.bouncycastle.util.encoders.Base64;

import javax.mail.*;
import javax.mail.internet.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Correio {

    private static final Logger logger = Logger.getLogger("br.gov.jfrj.siga.base.email");

    public static void enviar(final String destinatario, final String assunto,
                              final String conteudo) throws Exception {
        final String[] to = {destinatario};

        Correio.enviar(
                Prop.get("/siga.smtp.usuario.remetente"),
                to, assunto, conteudo, null);
    }

    public static void enviar(final String[] destinatarios, final String assunto,
                              final String conteudo) throws Exception {

        Correio.enviar(
                Prop.get("/siga.smtp.usuario.remetente"),
                destinatarios, assunto, conteudo, null);
    }

    public static void enviar(String remetente,
                              final String[] destinatarios, final String assunto,
                              final String conteudo, final String conteudoHTML) throws Exception {

        if (remetente == null)
            remetente = Prop.get("/siga.smtp.usuario.remetente");

        // lista indisponivel. Tenta ler apenas 1 servidor definido.
        String servidor = Prop.get("/siga.smtp");

        // Se não for definido um servidor, simplesmente não enviar nenhum email
        if (servidor == null)
            return;

        List<String> listaServidoresEmail = new ArrayList<>();
        listaServidoresEmail.add(servidor);

        boolean servidorDisponivel = false;
        String causa = "Indefinida";
        for (String servidorEmail : listaServidoresEmail) {
            try {
                enviarParaServidor(servidorEmail, remetente, destinatarios,
                        assunto, conteudo, conteudoHTML);
                servidorDisponivel = true;
                break;
            } catch (Exception e) {
                causa = e.getMessage();
                if (e.getCause() != null) {
                    causa = ", causa: " + e.getCause().getMessage();
                    logger.warning("Servidor de e-mail '" + servidorEmail
                            + "' indisponível: " + e.getMessage() + causa);
                }
            }
        }

        if (!servidorDisponivel) {
            throw new AplicacaoException(
                    "Não foi possível se conectar ao servidor de e-mail!. Causa:  " + causa);
        }

    }

    private static void enviarParaServidor(final String servidorEmail,
                                           String remetente, final String[] destinatarios,
                                           final String assunto, final String conteudo,
                                           final String conteudoHTML) throws Exception {
        // Cria propriedades a serem usadas na sessão.
        final Properties props = new Properties();

        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", Prop.get("/siga.smtp.starttls.enable"));
        props.put("mail.smtp.host", servidorEmail);
        props.put("mail.smtp.port", Prop.get("/siga.smtp.porta"));
        props.put("mail.smtp.ssl.trust", servidorEmail);

        // Cria sessão. setDebug(true) é interessante pois
        // mostra os passos do envio da mensagem e o
        // recebimento da mensagem do servidor no console.
        Session session;
        if (Boolean.TRUE.equals(Prop.getBool("/siga.smtp.auth"))) {
            props.put("mail.smtp.auth", true);
            final String usuario = Prop.get("/siga.smtp.auth.usuario");
            final String senha = Prop.get("/siga.smtp.auth.senha");
            session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(usuario, senha);
                }
            });
        } else {
            session = Session.getInstance(props);
        }

        session.setDebug(false);
        final MimeMessage msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(remetente));

        final Address[] recipients = Stream.of(destinatarios)
                .filter(email -> !email.equals("null"))
                .map(email -> {
                    try {
                        return new InternetAddress(email);
                    } catch (AddressException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .toArray(Address[]::new);

        msg.setRecipients(Message.RecipientType.TO, recipients);


        //    Se baseTeste inserir no assunto - AMBIENTE DE TESTE FAVOR DESCONSIDERAR

        Boolean isVersionTest = Boolean.TRUE.equals(Prop.getBool("/siga.base.teste"));

        if (Boolean.TRUE.equals(isVersionTest)) {
            msg.setSubject(assunto + " - AMBIENTE DE TESTE FAVOR DESCONSIDERAR", "utf-8");
        } else {
            msg.setSubject(assunto, "utf-8");
        }

        if (conteudoHTML == null) {
            msg.setContent(conteudo, "text/plain;charset=UTF-8");
        } else {
            Multipart mp = new MimeMultipart("alternative");

            // Add text version
            InternetHeaders ihs = new InternetHeaders();
            ihs.addHeader("Content-Type", "text/plain; charset=UTF-8");
            ihs.addHeader("Content-Transfer-Encoding", "base64");
            MimeBodyPart mb1 = new MimeBodyPart(ihs, Base64.encode(conteudo
                    .getBytes(StandardCharsets.UTF_8)));
            mp.addBodyPart(mb1);

            // Do the same with the HTML part
            InternetHeaders ihs2 = new InternetHeaders();
            ihs2.addHeader("Content-Type", "text/html; charset=UTF-8");
            ihs2.addHeader("Content-Transfer-Encoding", "base64");
            MimeBodyPart mb2 = new MimeBodyPart(ihs2,
                    Base64.encode(conteudoHTML.getBytes(StandardCharsets.UTF_8)));
            mp.addBodyPart(mb2);

            msg.setContent(mp);
        }

        // Envia mensagem.
        Transport.send(msg);

        logger.log(Level.INFO, "Email enviado para " + Collections.singletonList(recipients) + "[" + assunto + "]");
        logger.log(Level.FINE, "Detalhes do e-mail enviado:"
                + "\nAssunto: " + assunto
                + "\nDe: " + remetente
                + "\nPara: " + Arrays.asList(destinatarios)
                + "\nTexto: " + (conteudoHTML == null ? conteudo : conteudoHTML));
    }

    public static void enviar(String remetente, String[] destinatarios,
                              String assunto, String conteudo) throws Exception {
        Correio.enviar(remetente, destinatarios, assunto, conteudo, null);
    }

    public static String obterHTMLEmailParaUsuarioExternoAssinarDocumento(String uri, String siglaDocumento, String siglaUsuario) {

        return "<html>" +
                "<body>" +
                "	<table>" +
                "		<tbody>" +
                "			<tr>" +
                "				<td style='height: 80px; background-color: #f6f5f6; padding: 10px 20px;'>" +
                "					<img style='padding: 10px 0px; text-align: center;' src='https://www.documentos.spsempapel.sp.gov.br/siga/imagens/logo-sem-papel-cor.png' alt='SP Sem Papel' width='108' height='50' />" +
                "				</td>" +
                "			</tr>" +
                "			<tr>" +
                "				<td style='background-color: #bbb; padding: 0 20px;'>" +
                "					<h3 style='height: 20px;'>Governo do Estado de S&atilde;o Paulo</h3>" +
                "				</td>" +
                "			</tr>" +
                "			<tr>" +
                "				<td style='height: 310px; padding: 10px 20px;'>" +
                "					<div>" +
                "						<p style='color: #808080;'>Esse <a style='color: #808080;' href='" + uri + "' target='_blank'><b>link</b></a> fornece acesso ao documento nº <b>" + siglaDocumento + "</b>, do Programa SP Sem Papel, cujo usuário <b>" + siglaUsuario + "</b> é interessado.</p>" +
                "						<p style='color: #808080;'>Para visualizar e assinar o documento, acesse o link: <a style='color: #808080;' href='" + uri + "' target='_blank'><b>" + uri + "</b></a>" +
                "						<p style='color: #808080;'>Atenção: Esse e-mail é de uso restrito ao usuário e entidade para a qual foi endereçado. Se você não é destinatário desta mensagem, você está, por meio desta, notificado que não deverá retransmitir, imprimir, copiar, examinar, distribuir ou utilizar informação contida nesta mensagem.</p>" +
                "					</div>" +
                "				</td>" +
                "			</tr>" +
                "			<tr>" +
                "				<td style='height: 18px; padding: 0 20px; background-color: #eaecee;'>" +
                "					<p>" +
                "						<span style='color: #aaa;'><b>Aten&ccedil;&atilde;o:</b> esta &eacute; uma mensagem autom&aacute;tica. Por favor n&atilde;o responda&nbsp;</span>" +
                "					</p>" +
                "				</td>" +
                "			</tr>" +
                "	 	</tbody>" +
                "	</table>" +
                "</body>" +
                "</html>";
    }

}
