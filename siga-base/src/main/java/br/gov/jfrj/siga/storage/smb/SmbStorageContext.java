package br.gov.jfrj.siga.storage.smb;

import br.gov.jfrj.siga.base.Prop;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@ApplicationScoped
public class SmbStorageContext {
    public static final Logger LOGGER = Logger.getLogger(SmbStorageContext.class.getName());

    private final SMBClient client = new SMBClient();
    private DiskShare share;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public String getContextName() {
        return "siga";
    }

    public String getFileExt() {
        return "bin";
    }

    private void setupConnection() {
        String host = Prop.get("/storage.smb.host");
        String user = Prop.get("/storage.smb.user");
        String pass = Prop.get("/storage.smb.password");
        String domain = Prop.get("/storage.smb.domain");
        String shareName = Prop.get("/storage.smb.share");

        try {
            Connection connection = client.connect(host);
            AuthenticationContext ac = new AuthenticationContext(user, pass.toCharArray(), domain);
            Session session = connection.authenticate(ac);
            share = (DiskShare) session.connectShare(shareName);
        } catch (Exception e) {
            throw new RuntimeException("Error connecting to SMB", e);
        }
    }

    public DiskShare getShare() {
        if (share != null && share.isConnected())
            return this.share;

        client.close();
        setupConnection();

        return this.share;
    }

    @PostConstruct
    public void setup() {
        setupConnection();

        Runnable periodicTask = () -> {
            try {
                this.share.fileExists("keepalive.siga");
            } catch (Exception e) {
                LOGGER.warning("Error sending SMB ECHO: " + e.getMessage());
            }
        };

        scheduler.scheduleAtFixedRate(periodicTask, 14, 14, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void destroy() {
        client.close();
        scheduler.shutdownNow();
    }
}
