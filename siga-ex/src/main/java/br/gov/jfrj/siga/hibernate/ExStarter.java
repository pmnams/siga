package br.gov.jfrj.siga.hibernate;

import br.gov.jfrj.siga.Service;
import br.gov.jfrj.siga.base.UsuarioDeSistemaEnum;
import br.gov.jfrj.siga.cp.model.enm.CpTipoDeConfiguracao;
import br.gov.jfrj.siga.cp.model.enm.CpTipoDeMovimentacao;
import br.gov.jfrj.siga.cp.util.SigaFlyway;
import br.gov.jfrj.siga.ex.ExDocumento;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import br.gov.jfrj.siga.ex.storage.ExBlobCategory;
import br.gov.jfrj.siga.storage.StorageNormalizer;
import br.gov.jfrj.siga.storage.manager.BlobManager;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.security.Security;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

@Startup
@Singleton
@TransactionManagement(value = TransactionManagementType.BEAN)
public class ExStarter {

    private final static org.jboss.logging.Logger log = Logger.getLogger(ExStarter.class);
    public static EntityManagerFactory emf;
    @Resource
    private TimerService timerService;

    @Schedule(dayOfWeek = "Sat")
    private void runFileWatcher() {
        Map<Class<?>, String[]> entities = new HashMap<>();

        entities.put(ExDocumento.class, new String[]{"conteudoBlobDoc", "exBlob", "dtDoc", "" + ExBlobCategory.DOCUMENTS.value});

        StorageNormalizer normalizer = CDI.current().select(StorageNormalizer.class).get();
        normalizer.migrateFromEntitiesField(entities,14400);
    }

    @PostConstruct
    public void init() {
        log.info("INICIANDO SIGAEX.WAR");
        CpTipoDeConfiguracao.mapear(CpTipoDeConfiguracao.values());
        CpTipoDeConfiguracao.mapear(ExTipoDeConfiguracao.values());
        CpTipoDeMovimentacao.mapear(ExTipoDeMovimentacao.values());

        Security.addProvider(new BouncyCastleProvider());

        emf = Persistence.createEntityManagerFactory("default");
        Service.setUsuarioDeSistema(UsuarioDeSistemaEnum.SIGA_EX);
        new MigrationThread().start();
    }

    @PreDestroy
    private void shutdown() {
        for (Timer timer : timerService.getTimers()) {
            timer.cancel();
        }
    }

    public static class MigrationThread extends Thread {
        public void run() {
            try {
                SigaFlyway.migrate("java:/jboss/datasources/SigaExDS", "classpath:db/mysql/sigaex", true);
            } catch (NamingException e) {
                log.error("Erro na migração do banco", e);
                SigaFlyway.stopJBoss();
            }
        }
    }

}
