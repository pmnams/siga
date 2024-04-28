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

@ApplicationScoped
public class SmbStorageContext {
    private DiskShare share;
    private final SMBClient client = new SMBClient();

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
            e.printStackTrace();
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
    }

    @PreDestroy
    public void destroy() {
        client.close();
    }
}
