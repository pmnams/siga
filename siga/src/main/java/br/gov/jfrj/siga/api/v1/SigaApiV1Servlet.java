package br.gov.jfrj.siga.api.v1;

import br.gov.jfrj.siga.base.Prop;
import br.gov.jfrj.siga.base.Prop.IPropertyProvider;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.model.ContextoPersistencia;
import com.crivano.swaggerservlet.SwaggerServlet;
import com.crivano.swaggerservlet.dependency.TestableDependency;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.net.URL;
import java.net.URLConnection;

public class SigaApiV1Servlet extends SwaggerServlet implements IPropertyProvider {
    private static final long serialVersionUID = 1756711359239182178L;
    public static boolean migrationComplete = false;

//	public static ExecutorService executor = null;

    @Override
    public void initialize(ServletConfig config) throws ServletException {
        setAPI(ISigaApiV1.class);

        setActionPackage("br.gov.jfrj.siga.api.v1");

        Prop.setProvider(this);
        Prop.defineGlobalProperties();
        defineProperties();

        class HttpGetDependency extends TestableDependency {
            final String testsite;

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

        addDependency(new HttpGetDependency("rest", "www.google.com/recaptcha",
                "https://www.google.com/recaptcha/api/siteverify", false, 0, 10000));

        addDependency(new TestableDependency("database", "sigaexds", false, 0, 10000) {

            @Override
            public String getUrl() {
                return getProperty("datasource.name");
            }

            @Override
            public boolean test() throws Exception {
                try (SigaApiV1Context ctx = new SigaApiV1Context()) {
                    ctx.init(null);
                    return CpDao.getInstance().dt() != null;
                }
            }

            @Override
            public boolean isPartial() {
                return false;
            }
        });

        addDependency(new TestableDependency("database", "sigaexds-migration", false, 0, 10000) {

            @Override
            public String getUrl() {
                return getProperty("datasource.name") + "-migration";
            }

            @Override
            public boolean test() {
                return migrationComplete;
            }

            @Override
            public boolean isPartial() {
                return true;
            }
        });
    }

    private void defineProperties() {
        addPublicProperty("datasource.name", "java:/jboss/datasources/SigaCpDS");
        addPublicProperty("senha.usuario.expiracao.dias", null);
        addPrivateProperty("sinc.password", null);

    }

    @Override
    public String getService() {
        return "siga";
    }

    @Override
    public String getUser() {
        return ContextoPersistencia.getUserPrincipal();
    }

    @Override
    public String getProp(String nome) {
        return getProperty(nome);
    }

}
