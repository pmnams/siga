package br.gov.jfrj.siga.storage.manager;

import br.gov.jfrj.siga.base.Prop;
import br.gov.jfrj.siga.storage.Manager;
import br.gov.jfrj.siga.storage.SigaBlob;
import br.gov.jfrj.siga.storage.StorageType;
import br.gov.jfrj.siga.storage.blob.BlobData;
import br.gov.jfrj.siga.storage.blob.SmbBlobData;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
@Manager(type = StorageType.SAMBA)
public class SmbBlobManager implements BlobManager {
    private DiskShare share;
    private final SMBClient client;

    WeakReference<Set<String>> folderCheckCache = new WeakReference<>(new HashSet<>());

    public SmbBlobManager() {
        client = new SMBClient();
        setupConnection();
    }

    private void setupConnection() {
        Prop.getList("/siga.substituto.tipos");
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

    @Override
    public BlobData fromId(String id) {
        if (id == null)
            return null;

        BlobData blobData = null;
        checkAndPrepareShare();
        try (File f = share.openFile(
                id,
                EnumSet.of(AccessMask.GENERIC_READ),
                null,
                EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ),
                null,
                null
        )) {
            byte[] buffer = new byte[4096];
            int length;

            try {
                InputStream in = f.getInputStream();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                while ((length = in.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();

                blobData = new SmbBlobData();
                blobData.setData(os.toByteArray());
                blobData.setId(id);
                return blobData;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            return null;
        }
        return blobData;
    }

    @Override
    public BlobData fromData(SigaBlob blob, byte[] data) {
        BlobData blobData = new SmbBlobData();
        blobData.setData(data);

        return blobData;
    }

    @Override
    public String persist(SigaBlob blob, BlobData data) {
        checkAndPrepareShare();
        String filePath = prepareAndGetFilePath(blob);
        data.setId(filePath);

        try (File f = share.openFile(
                filePath,
                EnumSet.of(AccessMask.GENERIC_WRITE),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_COMPRESSED),
                EnumSet.of(SMB2ShareAccess.FILE_SHARE_WRITE),
                SMB2CreateDisposition.FILE_OVERWRITE_IF,
                EnumSet.of(SMB2CreateOptions.FILE_RANDOM_ACCESS)
        )) {
            f.write(data.getData(), 0);
        }

        return data.getId();
    }

    private String prepareAndGetFilePath(SigaBlob blob) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(blob.getCreatedAt());
        String folder = String.format("%d-%d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

        Set<String> folderCheck = folderCheckCache.get();
        if (folderCheck == null || !folderCheck.contains(folder)) {
            if (folderCheck == null) {
                folderCheck = new HashSet<>();
                folderCheckCache = new WeakReference<>(folderCheck);
            }

            if (!share.folderExists(folder))
                share.mkdir(folder);
        }

        return folder + "/" + blob.getId().toString() + ".bin";
    }

    private void checkAndPrepareShare() {
        if (share.isConnected())
            return;

        client.close();
        setupConnection();
    }

    @PreDestroy
    void destroy() {
        client.close();
    }
}
