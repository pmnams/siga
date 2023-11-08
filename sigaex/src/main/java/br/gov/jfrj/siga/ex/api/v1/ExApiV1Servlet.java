package br.gov.jfrj.siga.ex.api.v1;

import br.gov.jfrj.siga.base.Prop;
import br.gov.jfrj.siga.base.Prop.IPropertyProvider;
import br.gov.jfrj.siga.hibernate.ExDao;
import br.gov.jfrj.siga.model.ContextoPersistencia;
import com.crivano.swaggerservlet.SwaggerServlet;
import com.crivano.swaggerservlet.SwaggerUtils;
import com.crivano.swaggerservlet.dependency.TestableDependency;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExApiV1Servlet extends SwaggerServlet implements IPropertyProvider {
    private static final long serialVersionUID = 1756711359239182178L;

    public static ExecutorService executor = null;

    @Override
    public void initialize(ServletConfig config) throws ServletException {
        setAPI(IExApiV1.class);

        setActionPackage("br.gov.jfrj.siga.ex.api.v1");

        Prop.setProvider(this);
        Prop.defineGlobalProperties();
        defineProperties();

        // Threadpool
        if (Prop.get("redis.password") != null)
            SwaggerUtils.setCache(new MemCacheRedis());
        executor = Executors.newFixedThreadPool(Prop.getInt("threadpool.size"));

        class HttpGetDependency extends TestableDependency {
            String testsite;

            HttpGetDependency(String category, String service, String testsite, boolean partial, long msMin,
                              long msMax) {
                super(category, service, partial, msMin, msMax);
                this.testsite = testsite;
            }

            @Override
            public String getUrl() {
                return testsite;
            }

            @Override
            public boolean test() throws Exception {
                final URL url = new URL(testsite);
                final URLConnection conn = url.openConnection();
                conn.connect();
                return true;
            }
        }

        class FileSystemWriteDependency extends TestableDependency {
            private static final String TESTING = "testing...";
            String path;

            FileSystemWriteDependency(String service, String path, boolean partial, long msMin, long msMax) {
                super("filesystem", service, partial, msMin, msMax);
                this.path = path;
            }

            @Override
            public String getUrl() {
                return path;
            }

            @Override
            public boolean test() throws Exception {
                Path file = Paths.get(path + "/test.temp");
                Files.write(file, TESTING.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
                String s = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                return s != null;
            }
        }

        addDependency(new FileSystemWriteDependency("upload.dir.temp", Prop.get("upload.dir.temp"), false, 0, 10000));

        addDependency(new HttpGetDependency("rest", "www.google.com/recaptcha",
                "https://www.google.com/recaptcha/api/siteverify", false, 0, 10000));

        addDependency(new TestableDependency("database", "sigaexds", false, 0, 10000) {

            @Override
            public String getUrl() {
                return Prop.get("datasource.name");
            }

            @Override
            public boolean test() throws Exception {
                try (ExApiV1Context ctx = new ExApiV1Context()) {
                    ctx.init(null);
                    return ExDao.getInstance().dt() != null;
                }
            }

            @Override
            public boolean isPartial() {
                return false;
            }
        });

        if (Prop.get("redis.password") != null)
            addDependency(new TestableDependency("cache", "redis", false, 0, 10000) {

                @Override
                public String getUrl() {
                    return "redis://" + MemCacheRedis.getMasterHost() + ":" + MemCacheRedis.getMasterPort() + "/"
                            + MemCacheRedis.getDatabase() + " (" + "redis://" + MemCacheRedis.getSlaveHost() + ":"
                            + MemCacheRedis.getSlavePort() + "/" + MemCacheRedis.getDatabase() + ")";
                }

                @Override
                public boolean test() throws Exception {
                    String uuid = UUID.randomUUID().toString();
                    MemCacheRedis mc = new MemCacheRedis();
                    mc.store("test", uuid.getBytes());
                    String uuid2 = new String(mc.retrieve("test"));
                    return uuid.equals(uuid2);
                }
            });

    }

    private void defineProperties() {
        addPublicProperty("limita.acesso.documentos.por.configuracao", "true");
        addPublicProperty("timestamp.sistema", "siga");
        addPublicProperty("timestamp.url", "http://assijus:8080/assijus/api/v1");
        addPublicProperty("timestamp.public.key", null);

        addPublicProperty("data.validar.assinatura.digital", "01/10/2020");
        addPublicProperty("data.validar.assinatura.com.senha", "01/10/2020");

        addRestrictedProperty("upload.dir.temp");

        addPrivateProperty("api.secret", null);

//		addPrivateProperty("jwt.secret", System.getProperty("idp.jwt.modulo.pwd.sigaidp"));
//		addPublicProperty("jwt.issuer", "siga");
//		addPublicProperty("cookie.name", "siga-jwt");
//		addPublicProperty("cookie.domain", null);
//		addPublicProperty("cookie.expire.seconds", Long.toString(20 * 60L)); // Expira em 20min
//		addPublicProperty("cookie.renew.seconds", Long.toString(15 * 60L)); // Renova 15min antes de expirar

        addRestrictedProperty("datasource.url", null);
        if (Prop.get("datasource.url") != null) {
            addRestrictedProperty("datasource.username");
            addPrivateProperty("datasource.password");
            addRestrictedProperty("datasource.name", null);
        } else {
            addRestrictedProperty("datasource.username", null);
            addPrivateProperty("datasource.password", null);
            addRestrictedProperty("datasource.name", "java:/jboss/datasources/SigaExDS");
        }

        // Redis
        //
        addRestrictedProperty("redis.database", "10");
        addPrivateProperty("redis.password", null);
        addRestrictedProperty("redis.slave.port", "0");
        addRestrictedProperty("redis.slave.host", null);
        addRestrictedProperty("redis.master.host", "localhost");
        addRestrictedProperty("redis.master.port", "6379");

        addPublicProperty("threadpool.size", "10");

        addPrivateProperty("assinador.externo.password", null);
        addPrivateProperty("assinador.externo.popup.url", Prop.get("/siga.external.base.url") + "/assijus");

        addPublicProperty("assinatura.code.base.path", null);
        addPublicProperty("assinatura.messages.url.path", null);
        addPublicProperty("assinatura.policy.url.path", null);
        addPublicProperty("assinatura.estampar", "false");

        addPublicProperty("carimbo.pagina.variante", "0");
        addPublicProperty("carimbo.pagina.fundo.transparente", "false");
        addPublicProperty("carimbo.pagina.x", "522");
        addPublicProperty("carimbo.pagina.y", "18");

        addRestrictedProperty("bie.lista.destinatario.publicacao", null);
        addPublicProperty("carimbo.texto.superior", "SIGA-DOC");
        addPublicProperty("classificacao.mascara.entrada",
                "([0-9]{0,2})\\.?([0-9]{0,2})?\\.?([0-9]{0,2})?\\.?([0-9]{0,2})?([A-Z])?");
        addPublicProperty("classificacao.mascara.exibicao", null);
        addPublicProperty("classificacao.mascara.javascript", "99.99.99.99");
        addPublicProperty("classificacao.mascara.nome.nivel", "NULL,Assunto,Classe,Subclasse,Atividade");
        addPublicProperty("classificacao.mascara.saida", "%1$02d.%2$02d.%3$02d.%4$02d");
        addPublicProperty("classificacao.nivel.minimo.de.enquadramento", null);
        addPublicProperty("codigo.acronimo.ano.inicial", "9999");
        addPublicProperty("conversor.html.ext", "br.gov.jfrj.itextpdf.FlyingSaucer");
        addPublicProperty("pdf.visualizador", "pdf.js");
        addPublicProperty("conversor.html.factory", "br.gov.jfrj.siga.ex.ext.ConversorHTMLFactory");
        addPublicProperty("data.obrigacao.assinar.anexo.despacho", "31/12/2099");
        addPublicProperty("debug.modelo.padrao.arquivo", null);
        addPublicProperty("dje.lista.destinatario.publicacao", null);
        addPublicProperty("dje.servidor.data.disponivel", null);
        addPublicProperty("dje.servidor.url", null);
        addPublicProperty("email.mensagem.teste", null);
        addPublicProperty("folha.de.rosto", "inativa");
        addPublicProperty("modelo.interno.importado", null);
        addPublicProperty("modelo.processo.administrativo", null);
        addPublicProperty("montador.query", "br.gov.jfrj.siga.hibernate.ext.MontadorQuery");
        addPublicProperty("pdf.tamanho.maximo", "5000000");
        addPublicProperty("pdf.tamanho.maximo.completo", null);
        addPublicProperty("relarmaz.qtd.bytes.pagina", "51200");
        addPublicProperty("reordenacao.ativo", null);
        addPublicProperty("rodape.data.assinatura.ativa", "31/12/2099");
        addPrivateProperty("util.webservice.password", null);
        addPublicProperty("volume.max.paginas", "200");
        addPrivateProperty("webdav.senha", null);
        addPublicProperty("controlar.numeracao.expediente", "false");
        addPublicProperty("recebimento.automatico", "true");
        addPublicProperty("descricao.documento.ai.length", "4000");

        addPublicProperty("exibe.nome.acesso", "false");
        addPublicProperty("atualiza.anotacao.pesquisa", "false");

        addPublicProperty("max.linhas.pesquisa.doc", "200000");
        addPublicProperty("mostrar.botao.exportar.pesquisa.doc", "true");

        addPublicProperty("modelos.cabecalho.brasao", "contextpath/imagens/brasaoColoridoTRF2.png");
        addPublicProperty("modelos.cabecalho.brasao.width", "auto");
        addPublicProperty("modelos.cabecalho.brasao.height", "65");

        addPublicProperty("modelos.cabecalho.titulo", "PODER JUDICIÁRIO");
        addPublicProperty("modelos.cabecalho.subtitulo", null);

        addPublicProperty("arquivosAuxiliares.extensoes.excecao", ".bat,.exe,.sh,.dll,.pdf");

        // Siga-Le
        addPublicProperty("smtp.sugestao.destinatario", getProp("/siga.smtp.usuario.remetente"));
        addPublicProperty("smtp.sugestao.assunto", "Siga-Le: Sugestão");

        // anexação de pdf: quantidade de arq. a serem anexados por upload
        addPublicProperty("qtd.max.arquivo.anexado.upload", "1");

        addPublicProperty("consultapublica.exibe.tramitacao.ate.nivelacesso", "-1");

        addPrivateProperty("debug.default.template.pathname", null);

        //Informações Siafem
        addPublicProperty("ws.siafem.nome.modelo", null);
        addPublicProperty("ws.siafem.url.wsdl", null);
        addPublicProperty("ws.siafem.url.namespace", null);
        addPublicProperty("ws.siafem.service.localpart", null);
        addPublicProperty("ws.siafem.service.localpartsoap", null);
        addPublicProperty("documento.novo.modelo.padrao", "Memorando");

        //GC Control - Concatenação de PDF
        addPublicProperty("arquivo.tamanho.gc", "26214400"); //PDF - 25MB
        addPublicProperty("arquivo.contagem.gc", "25"); //HTML - 25 documentos
    }

    @Override
    public String getService() {
        return "sigaex";
    }

    @Override
    public String getUser() {
        return ContextoPersistencia.getUserPrincipal();
    }

    public static <T> Future<T> submitToExecutor(Callable<T> task) {
        return executor.submit(task);
    }

    @Override
    public String getProp(String nome) {
        return getProperty(nome);
    }

}
