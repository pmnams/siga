package br.gov.jfrj.siga.vraptor;

import br.com.caelum.vraptor.*;
import br.com.caelum.vraptor.observer.download.Download;
import br.com.caelum.vraptor.observer.download.InputStreamDownload;
import br.gov.jfrj.itextpdf.Documento;
import br.gov.jfrj.siga.Service;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.base.Prop;
import br.gov.jfrj.siga.base.client.Hcaptcha;
import br.gov.jfrj.siga.base.util.Utils;
import br.gov.jfrj.siga.bluc.service.BlucService;
import br.gov.jfrj.siga.bluc.service.HashRequest;
import br.gov.jfrj.siga.bluc.service.HashResponse;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.*;
import br.gov.jfrj.siga.ex.bl.Ex;
import br.gov.jfrj.siga.ex.logic.ExPodeVisualizarExternamente;
import br.gov.jfrj.siga.ex.vo.ExDocumentoVO;
import br.gov.jfrj.siga.hibernate.ExDao;
import br.gov.jfrj.siga.persistencia.ExMobilDaoFiltro;
import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.*;

@Controller
public class ExProcessoAutenticacaoController extends ExController {
    private static final String URL_EXIBIR = "/public/app/processoautenticar";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    /**
     * @deprecated CDI eyes only
     */
    public ExProcessoAutenticacaoController() {
        super();
    }

    @Inject
    public ExProcessoAutenticacaoController(HttpServletRequest request, HttpServletResponse response,
                                            ServletContext context, Result result, SigaObjects so, EntityManager em) {
        super(request, response, context, result, ExDao.getInstance(), so, em);
    }

    private void setDefaultResults() {
        result.include("request", getRequest());
    }

    @Get
    @Path("/processoautenticar.action")
    public void redirecionar() throws Exception {
        result.redirectTo(this).processoautenticar(null, null, null, null, null);
    }

    @Get
    @Post
    @Path("/public/app/processoautenticar")
    public void processoautenticar(final String n, final String answer, final String assinaturaB64,
                                   final String certificadoB64, final String atributoAssinavelDataHora) throws Exception {
        String hCaptchaSiteKey = getHcaptchaSiteKey();
        String hCaptchaSitePassword = getHcaptchaSitePassword();
        result.include("hcaptchaSiteKey", hCaptchaSiteKey);
        result.include("n", n);

        if (StringUtils.isBlank(n)) {
            setDefaultResults();
            return;
        }
        boolean success = false;

        String gHcaptchaResponse = request.getParameter("h-captcha-response");
        if (gHcaptchaResponse != null) {
            JSONObject body = Hcaptcha.validar(
                    hCaptchaSitePassword,
                    gHcaptchaResponse,
                    request.getRemoteAddr()
            );

            success = body.getBoolean("success");
        }

        if (!success) {
            setDefaultResults();
            return;
        }

        setDefaultResults();
        result.include("assinaturaB64", assinaturaB64);
        result.include("certificadoB64", certificadoB64);
        result.include("atributoAssinavelDataHora", atributoAssinavelDataHora);
        result.forwardTo(this).processoArquivoAutenticado(buildJwtToken(n), null);
    }

    @Get("/public/app/processoArquivoAutenticado_stream")
    public Download processoArquivoAutenticado_stream(final boolean assinado, final Long idMov,
                                                      final String certificadoB64, final String sigla) throws Exception {
        String jwt = Utils.getCookieValue(request, "jwt-prot");
        if (jwt == null) {
            setDefaultResults();
            result.redirectTo(URL_EXIBIR);
            return null;
        }

        String n;
        try {
            n = verifyJwtToken(jwt).get("n").toString();
        } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException
                 | SignatureException | IOException | JWTVerifyException e) {
            throw new AplicacaoException("Token inválido ou expirado. Por favor, entre novamente no link do protocolo.");
        }

        ExArquivo arq = Ex.getInstance().getBL().buscarPorProtocolo(n);

        byte[] bytes;

        String fileName;
        String contentType;
        if (sigla != null) {
            Long idDocPai = arq.getIdDoc();
            ExMobilDaoFiltro flt = new ExMobilDaoFiltro();
            flt.setSigla(sigla);
            ExMobil mob = ExDao.getInstance().consultarPorSigla(flt);
            if (mob == null) {
                throw new AplicacaoException("Documento não encontrado: " + sigla);
            }
            if (!((Objects.equals(idDocPai, mob.getExMobilPai().getDoc().getIdDoc())
                    || mob.getDoc().isDescricaoEspecieDespacho())
                    && mob.isExibirNoAcompanhamento())) {
                throw new AplicacaoException("Documento não permitido para visualização: " + sigla);
            }
            arq = mob.doc();
            fileName = arq.getReferenciaPDF();
            contentType = "application/pdf";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Documento.getDocumento(baos, null, mob, null, false, true, false, null, null);
            bytes = baos.toByteArray();
        } else {
            if (idMov != null && idMov != 0) {
                ExMovimentacao mov = dao().consultar(idMov, ExMovimentacao.class, false);

                fileName = arq.getReferencia() + "_" + mov.getIdMov() + ".p7s";
                contentType = mov.getConteudoTpMov();

                bytes = mov.getConteudoBlobMov2();

            } else {
                fileName = arq.getReferenciaPDF();
                contentType = "application/pdf";

                if (assinado)
                    bytes = Ex.getInstance().getBL().obterPdfPorProtocolo(n);
                else
                    bytes = arq.getPdf();
            }
        }
        if (bytes == null) {
            throw new AplicacaoException("Arquivo não encontrado para Download.");
        }
        final boolean fB64 = getRequest().getHeader("Accept") != null
                && getRequest().getHeader("Accept").startsWith("text/vnd.siga.b64encoded");

        if (certificadoB64 != null) {
            final Date date = dao().consultarDataEHoraDoServidor();

            BlucService bluc = Service.getBlucService();
            HashRequest hashReq = new HashRequest();
            hashReq.setCertificate(certificadoB64);
            hashReq.setCrl("true");
            hashReq.setPolicy("AD-RB");
            hashReq.setTime(date);
            hashReq.setSha1(BlucService.bytearray2b64(BlucService.calcSha1(bytes)));
            hashReq.setSha256(BlucService.bytearray2b64(BlucService.calcSha256(bytes)));

            HashResponse hashresp = bluc.hash(hashReq);
            if (hashresp.getErrormsg() != null)
                throw new Exception("BluC não conseguiu produzir o pacote assinável. " + hashresp.getErrormsg());
            byte[] sa = Base64.getDecoder().decode(hashresp.getHash());

            //noinspection UastIncorrectHttpHeaderInspection
            getResponse().setHeader("Atributo-Assinavel-Data-Hora", Long.toString(date.getTime()));
            return new InputStreamDownload(makeByteArrayInputStream(sa, fB64), APPLICATION_OCTET_STREAM, null);
        }
        return new InputStreamDownload(makeByteArrayInputStream(bytes, fB64), contentType, fileName);
    }

    private ByteArrayInputStream makeByteArrayInputStream(final byte[] content, final boolean fB64) {
        final byte[] conteudo = (fB64 ? Base64.getEncoder().encode(content) : content);
        return (new ByteArrayInputStream(conteudo));
    }

    // antigo metodo arquivo();
    @Get("/public/app/processoArquivoAutenticado")
    public void processoArquivoAutenticado(final String tokenJwt, final Long idMovJuntada) throws Exception {
        String jwt = tokenJwt;
        if (jwt == null) {
            jwt = Utils.getCookieValue(request, "jwt-prot");
            if (jwt == null) {
                setDefaultResults();
                result.redirectTo(URL_EXIBIR);
                return;
            }
        }
        String n = verifyJwtToken(jwt).get("n").toString();

        ExArquivo arq = Ex.getInstance().getBL().buscarPorProtocolo(n);
        if (idMovJuntada != null) {
            final ExMovimentacao movJuntada = dao().consultar(idMovJuntada, ExMovimentacao.class, false);
            if (movJuntada != null && movJuntada.getExMobilRef() != null
                    && !movJuntada.isCancelada()
                    && (((ExDocumento) arq).contemMobil(movJuntada.getExMobil()))) {
                arq = movJuntada.getExMobilRef().getDoc();

            } else {
                throw new AplicacaoException("Documento não autorizado para visualização "
                        + "no acompanhamento do protocolo.");
            }
        }
        Set<ExMovimentacao> assinaturas = arq.getAssinaturasDigitais();

        setDefaultResults();
        result.include("assinaturas", assinaturas);
        result.include("n", n);
        result.include("jwt", jwt);

        if (!(arq instanceof ExDocumento))
            return;

        ExDocumento doc = (ExDocumento) arq;
        final ExDocumentoDTO exDocumentoDTO = new ExDocumentoDTO();
        exDocumentoDTO.setSigla(doc.getSigla());
        buscarDocumento(exDocumentoDTO);

        ExProtocolo prot = Ex.getInstance().getBL().obterProtocolo(exDocumentoDTO.getDoc());
        if (prot == null) {
            throw new AplicacaoException(
                    "Ocorreu um erro ao obter protocolo do Documento: " + exDocumentoDTO.getDoc().getSigla()
            );
        }

        List<ExMobil> lstMobil = dao().consultarMobilPorDocumento(doc);
        List<ExMovimentacao> lista = dao().consultarMovimentoIncluindoJuntadaPorMobils(lstMobil);

        DpPessoa p = doc.getSubscritor();
        DpLotacao l = doc.getLotaSubscritor();

        if (p == null && !lista.isEmpty()) {
            p = lista.get(0).getSubscritor();
        }

        if (l == null && !lista.isEmpty()) {
            l = lista.get(0).getLotaSubscritor();
        }

        ExMobil mob = null;
        if (doc.isFinalizado()) {
            if (doc.isProcesso()) {
                mob = doc.getUltimoVolume();
            } else {
                mob = doc.getPrimeiraVia();
            }
        }

        final ExDocumentoVO docVO = new ExDocumentoVO(doc, mob, getCadastrante(), p, l, true, true, false, true);
        final boolean podeVisualizarExternamente = Objects.nonNull(mob) && new ExPodeVisualizarExternamente(mob, p, l).eval();

        result.include("movs", lista);
        result.include("sigla", exDocumentoDTO.getDoc().getSigla());
        result.include("msg", exDocumentoDTO.getMsg());
        result.include("docVO", docVO);
        result.include("podeVisualizarExternamente", podeVisualizarExternamente);
        result.include("autenticacao", exDocumentoDTO.getDoc().getAssinantesCompleto()
                + " Documento Nº:  " + exDocumentoDTO.getDoc().getSiglaAssinatura()
        );
        result.include("protocolo", prot.getCodigo());
        result.include("mob", exDocumentoDTO.getMob());
        result.include("isProtocoloFilho", idMovJuntada != null);

        Cookie cookie = new Cookie("jwt-prot", buildJwtToken(n));
        cookie.setMaxAge(60 * 60);
        this.response.addCookie(cookie);
    }

    private static String getHcaptchaSiteKey() {
        return Prop.get("/siga.hcaptcha.key");
    }

    private static String getHcaptchaSitePassword() {
        return Prop.get("/siga.hcaptcha.pwd");
    }

    private static String getJwtPassword() {
        return Prop.get("/siga.autenticacao.senha");
    }

    private static String buildJwtToken(String n) {
        String token;

        final JWTSigner signer = new JWTSigner(getJwtPassword());
        final HashMap<String, Object> claims = new HashMap<>();

        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + 60 * 60L; // token expires in 1 hours
        claims.put("exp", exp);
        claims.put("iat", iat);

        claims.put("n", n);
        token = signer.sign(claims);

        return token;
    }

    private static Map<String, Object> verifyJwtToken(String token) throws Exception {
        final JWTVerifier verifier = new JWTVerifier(getJwtPassword());
        return verifier.verify(token);
    }

    @SuppressWarnings("DuplicatedCode")
    private void buscarDocumento(final ExDocumentoDTO exDocumentoDto) {
        if (exDocumentoDto.getMob() == null && StringUtils.isNotBlank(exDocumentoDto.getSigla())) {
            final ExMobilDaoFiltro filter = new ExMobilDaoFiltro();
            filter.setSigla(exDocumentoDto.getSigla());
            exDocumentoDto.setMob(dao().consultarPorSigla(filter));
            if (exDocumentoDto.getMob() != null) {
                exDocumentoDto.setDoc(exDocumentoDto.getMob().getExDocumento());
            }
        } else if (exDocumentoDto.getMob() == null && exDocumentoDto.getDocumentoViaSel().getId() != null) {
            exDocumentoDto.setIdMob(exDocumentoDto.getDocumentoViaSel().getId());
            exDocumentoDto.setMob(dao().consultar(exDocumentoDto.getIdMob(), ExMobil.class, false));
        } else if (exDocumentoDto.getMob() == null && exDocumentoDto.getIdMob() != null
                && exDocumentoDto.getIdMob() != 0) {
            exDocumentoDto.setMob(dao().consultar(exDocumentoDto.getIdMob(), ExMobil.class, false));
        }

        if (exDocumentoDto.getMob() != null) {
            exDocumentoDto.setDoc(exDocumentoDto.getMob().doc());
        }

        if (exDocumentoDto.getDoc() == null) {
            final String id = param("exDocumentoDto.id");
            if (StringUtils.isNotBlank(id)) {
                exDocumentoDto.setDoc(daoDoc(Long.parseLong(id)));
            }
        }

        if (exDocumentoDto.getDoc() != null && exDocumentoDto.getMob() == null) {
            exDocumentoDto.setMob(exDocumentoDto.getDoc().getMobilGeral());
        }
    }

}
