package br.gov.jfrj.siga.vraptor;

import br.com.caelum.vraptor.*;
import br.com.caelum.vraptor.observer.download.Download;
import br.com.caelum.vraptor.observer.download.InputStreamDownload;
import br.com.caelum.vraptor.view.Results;
import br.gov.jfrj.siga.Service;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.base.Prop;
import br.gov.jfrj.siga.base.client.Hcaptcha;
import br.gov.jfrj.siga.bluc.service.BlucService;
import br.gov.jfrj.siga.bluc.service.HashRequest;
import br.gov.jfrj.siga.bluc.service.HashResponse;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.*;
import br.gov.jfrj.siga.ex.bl.Ex;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import br.gov.jfrj.siga.ex.vo.ExDocumentoVO;
import br.gov.jfrj.siga.hibernate.ExDao;
import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
public class ExAutenticacaoController extends ExController {
    private static final String URL_EXIBIR = "/public/app/autenticar";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final String APPLICATION_PDF = "application/pdf";


    /**
     * @deprecated CDI eyes only
     */
    public ExAutenticacaoController() {
        super();
    }

    @Inject
    public ExAutenticacaoController(HttpServletRequest request,
                                    HttpServletResponse response, ServletContext context,
                                    Result result, SigaObjects so, EntityManager em) {
        super(request, response, context, result, ExDao.getInstance(), so, em);
    }

    private void setDefaultResults() {
        result.include("request", getRequest());
    }

    @Get
    @Path("/autenticar.action")
    public void redirecionar() throws Exception {
        result.redirectTo(this).autenticar(null, null, null, null, null, null);
    }

    @Get
    @Post
    @Path("/public/app/autenticar")
    public void autenticar(final String n, final String answer,
                           final String ass, final String assinaturaB64,
                           final String certificadoB64, final String atributoAssinavelDataHora)
            throws Exception {
        String captchaSiteKey = Prop.get("/siga.hcaptcha.key");
        String captchaSitePassword = Prop.get("/siga.hcaptcha.pwd");

        result.include("hcaptchaSiteKey", captchaSiteKey);
        result.include("n", n);

        if (n == null || n.trim().length() == 0) {
            setDefaultResults();
            return;
        }

        String gRecaptchaResponse = request
                .getParameter("g-recaptcha-response");

        boolean success = false;
        if (gRecaptchaResponse != null) {
            JSONObject body = Hcaptcha.validar(captchaSitePassword, gRecaptchaResponse, request.getRemoteAddr());

            success = body.getBoolean("success");
        }
        if (!success) {
            setDefaultResults();
            return;
        }

        ExArquivo arq = Ex.getInstance().getBL().buscarPorNumeroAssinatura(n);
        Set<ExMovimentacao> assinaturas = arq.getAssinaturasDigitais();
        boolean mostrarBotaoAssinarExterno = arq
                .isCodigoParaAssinaturaExterna(n);

        ExMovimentacao mov = null;
        if (mostrarBotaoAssinarExterno) {
            mov = (ExMovimentacao) arq;
        }

        if (ass != null && ass.trim().length() != 0) {
            byte[] assinatura = Base64.getDecoder().decode(assinaturaB64 == null ? ""
                    : assinaturaB64);
            byte[] certificado = Base64.getDecoder().decode(certificadoB64 == null ? ""
                    : certificadoB64);
            Date dt = mov.getDtMov();
            if (certificado != null && certificado.length != 0)
                dt = new Date(Long.valueOf(atributoAssinavelDataHora));
            else
                certificado = null;

            try {
                SigaTransacionalInterceptor.upgradeParaTransacional();
                Ex.getInstance()
                        .getBL()
                        .assinarMovimentacao(
                                null,
                                null,
                                mov,
                                dt,
                                assinatura,
                                certificado,
                                ExTipoDeMovimentacao.ASSINATURA_DIGITAL_MOVIMENTACAO);
            } catch (final Exception e) {
                throw new AplicacaoException(e.getMessage());
            }
        }

        setDefaultResults();

        result.include("assinaturas", assinaturas);
        result.include("mov", mov);
        result.include("mostrarBotaoAssinarExterno", mostrarBotaoAssinarExterno);
        result.include("ass", ass);
        result.include("assinaturaB64", assinaturaB64);
        result.include("certificadoB64", certificadoB64);
        result.include("atributoAssinavelDataHora", atributoAssinavelDataHora);
        result.forwardTo(this).arquivoAutenticado(buildJwtToken(n));
    }

    @Get("/public/app/arquivoAutenticado_stream")
    public Download arquivoAutenticado_stream(final String jwt,
                                              final boolean assinado, final Long idMov,
                                              final String certificadoB64) throws Exception {

        if (jwt == null) {
            setDefaultResults();
            result.redirectTo(URL_EXIBIR);
            return null;
        }
        String n = verifyJwtToken(jwt).get("n").toString();

        ExArquivo arq = Ex.getInstance().getBL().buscarPorNumeroAssinatura(n);

        byte[] bytes = null;
        String fileName = null;
        String contentType = null;
        if (idMov != null && idMov != 0) {
            ExMovimentacao mov = dao().consultar(idMov, ExMovimentacao.class,
                    false);

            switch ((ExTipoDeMovimentacao) mov.getExTipoMovimentacao()) {
                case CONFERENCIA_COPIA_COM_SENHA:
                case ASSINATURA_COM_SENHA:
                case ASSINATURA_MOVIMENTACAO_COM_SENHA:
                    fileName = arq.getReferencia() + "_" + mov.getIdMov() + ".jwt";
                    contentType = "application/jwt";

                    if (mov.getAuditHash() == null) {
                        if (Prop.getBool("doc.comp.assinatura.senha", false)) {
                            result.use(Results.page()).forwardTo("/WEB-INF/jsp/compAssiSenha.jsp");
                            return null;
                        }

                        throw new AplicacaoException("Esta é uma assinatura digital com login e senha e não há nenhum artefato comprobatório disponível para download.");
                    }
                    bytes = mov.getAuditHash().getBytes(StandardCharsets.UTF_8);
                    break;

                case CONFERENCIA_COPIA_DOCUMENTO:
                case ASSINATURA_DIGITAL_DOCUMENTO:
                case ASSINATURA_DIGITAL_MOVIMENTACAO:
                    fileName = arq.getReferencia() + "_" + mov.getIdMov() + ".p7s";
                    contentType = mov.getConteudoTpMov();
                    bytes = mov.getConteudoBlobMov2();
                    break;
            }
        } else {
            fileName = arq.getReferenciaPDF();
            contentType = "application/pdf";

            if (assinado)
                bytes = Ex.getInstance().getBL().obterPdfPorNumeroAssinatura(n);
            else
                bytes = arq.getPdf();
        }
        if (bytes == null) {
            throw new AplicacaoException(
                    "Arquivo não encontrado para Download.");
        }
        final boolean fB64 = getRequest().getHeader("Accept") != null
                && getRequest().getHeader("Accept").startsWith(
                "text/vnd.siga.b64encoded");
        if (certificadoB64 != null) {
            final Date dt = dao().consultarDataEHoraDoServidor();
            getResponse().setHeader("Atributo-Assinavel-Data-Hora",
                    Long.toString(dt.getTime()));

            // Chamar o BluC para criar o pacote assinavel
            //
            BlucService bluc = Service.getBlucService();
            HashRequest hashreq = new HashRequest();
            hashreq.setCertificate(certificadoB64);
            hashreq.setCrl("true");
            hashreq.setPolicy("AD-RB");
            hashreq.setSha1(bluc.bytearray2b64(bluc.calcSha1(bytes)));
            hashreq.setSha256(bluc.bytearray2b64(bluc.calcSha256(bytes)));
            hashreq.setTime(dt);
            HashResponse hashresp = bluc.hash(hashreq);
            if (hashresp.getErrormsg() != null)
                throw new Exception(
                        "BluC não conseguiu produzir o pacote assinável. "
                                + hashresp.getErrormsg());
            byte[] sa = Base64.getDecoder().decode(hashresp.getHash());

            return new InputStreamDownload(makeByteArrayInputStream(sa, fB64),
                    APPLICATION_OCTET_STREAM, null);
        }
        return new InputStreamDownload(makeByteArrayInputStream(bytes, fB64),
                contentType, fileName);
    }

    private ByteArrayInputStream makeByteArrayInputStream(final byte[] content,
                                                          final boolean fB64) {
        final byte[] conteudo = (fB64 ? Base64.getEncoder().encode(content)
                : content);
        return (new ByteArrayInputStream(conteudo));
    }

    // antigo metodo arquivo();
    @Get("/public/app/arquivoAutenticado")
    public void arquivoAutenticado(final String jwt) throws Exception {
        if (jwt == null) {
            setDefaultResults();
            result.redirectTo(URL_EXIBIR);
            return;
        }
        String n = verifyJwtToken(jwt).get("n").toString();

        ExArquivo arq = Ex.getInstance().getBL().buscarPorNumeroAssinatura(n);
        Set<ExMovimentacao> assinaturas = arq.getAssinaturasDigitais();

        ExMovimentacao mov = null;
        if (arq.isCodigoParaAssinaturaExterna(n)) {
            mov = (ExMovimentacao) arq;
        }

        setDefaultResults();
        result.include("assinaturas", assinaturas);
        result.include("mov", mov);
        result.include("n", n);
        result.include("jwt", jwt);

        if (arq instanceof ExDocumento) {
            ExMobil mob = null;
            ExDocumento doc = (ExDocumento) arq;

            if (doc.isFinalizado()) {
                if (doc.isProcesso()) {
                    mob = doc.getUltimoVolume();
                } else {
                    mob = doc.getPrimeiraVia();
                }
            }
            List<ExMovimentacao> lista = new ArrayList<ExMovimentacao>();
            lista.addAll(doc.getAutenticacoesComTokenOuSenha());

            DpPessoa p = new DpPessoa();
            DpLotacao l = new DpLotacao();

            p = doc.getSubscritor();
            l = doc.getLotaSubscritor();

            if (p == null && !lista.isEmpty()) {
                p = lista.get(0).getSubscritor();
            }

            if (l == null && !lista.isEmpty()) {
                l = lista.get(0).getLotaSubscritor();
            }

            final ExDocumentoVO docVO = new ExDocumentoVO(doc, mob, getCadastrante(), p, l, true, false, false, true);

            result.include("docVO", docVO);
        }
    }

    private static String getJwtPassword() {
        return Prop.get("/siga.autenticacao.senha");
    }

    private static String buildJwtToken(String n) {
        String token;

        final JWTSigner signer = new JWTSigner(getJwtPassword());
        final HashMap<String, Object> claims = new HashMap<String, Object>();

        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + 1 * 60 * 60L; // token expires in 1 hours
        claims.put("exp", exp);
        claims.put("iat", iat);

        claims.put("n", n);
        token = signer.sign(claims);

        return token;
    }

    private static Map<String, Object> verifyJwtToken(String token) {
        final JWTVerifier verifier = new JWTVerifier(getJwtPassword());
        try {
            Map<String, Object> map = verifier.verify(token);
            return map;
        } catch (Exception e) {
            throw new AplicacaoException("Erro ao verificar token JWT", 0, e);
        }
    }

}
