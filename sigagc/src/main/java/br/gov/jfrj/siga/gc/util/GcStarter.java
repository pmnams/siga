package br.gov.jfrj.siga.gc.util;

import br.gov.jfrj.siga.cp.model.enm.CpTipoDeConfiguracao;
import br.gov.jfrj.siga.cp.util.SigaFlyway;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@Startup
@Singleton
@ApplicationScoped
@TransactionManagement(value = TransactionManagementType.BEAN)
public class GcStarter {

    private final static org.jboss.logging.Logger log = Logger.getLogger(GcStarter.class);
    public static EntityManagerFactory emf;

    @PostConstruct
    public void init() {
        log.info("INICIANDO SIGAGC.WAR");
        CpTipoDeConfiguracao.mapear(CpTipoDeConfiguracao.values());

        emf = Persistence.createEntityManagerFactory("default");
        new MigrationThread().start();
    }

    @PreDestroy
    private void shutdown() {
        emf.close();
    }

    public static class MigrationThread extends Thread {
        public void run() {
            try {
                SigaFlyway.migrate("java:/jboss/datasources/SigaGcDS", "classpath:db/mysql/sigagc", true);
            } catch (NamingException e) {
                log.error("Erro na migração do banco", e);
                SigaFlyway.stopJBoss();
            }
        }
    }


}
