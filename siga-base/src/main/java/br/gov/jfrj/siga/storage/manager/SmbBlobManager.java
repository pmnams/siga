package br.gov.jfrj.siga.storage.manager;

import br.gov.jfrj.siga.storage.Manager;
import br.gov.jfrj.siga.storage.SigaBlob;
import br.gov.jfrj.siga.storage.StorageType;
import br.gov.jfrj.siga.storage.blob.BlobData;
import br.gov.jfrj.siga.storage.blob.impl.SmbBlobData;
import br.gov.jfrj.siga.storage.smb.SmbStorageContext;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.share.File;
import com.hierynomus.smbj.utils.SmbFiles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
@Manager(StorageType.SAMBA)
public class SmbBlobManager implements BlobManager {

    private final SmbStorageContext context;

    WeakReference<Set<String>> folderCheckCache = new WeakReference<>(new HashSet<>());

    @Inject
    public SmbBlobManager(SmbStorageContext context) {
        this.context = context;
    }

    @Deprecated
    public SmbBlobManager() {
        this(null);
    }

    @Override
    public BlobData fromBlob(SigaBlob blob) {
        if (blob.getDataIdentifier() == null)
            return null;

        String filePath = getFilePath(blob, blob.getDataIdentifier());

        BlobData blobData = null;
        try (File f = this.context.getShare().openFile(
                filePath,
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
                blobData.setId(blob.getDataIdentifier());
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
        String id = String.valueOf(blob.getId());
        data.setId(id);

        String filePath = getFilePath(blob, id);
        String folder = Paths.get(filePath).getParent().toString();

        Set<String> folderCheck = folderCheckCache.get();
        if (folderCheck == null || !folderCheck.contains(folder)) {
            if (folderCheck == null) {
                folderCheck = new HashSet<>();
                folderCheckCache = new WeakReference<>(folderCheck);
            }

            if (!this.context.getShare().folderExists(folder)) {
                SmbFiles files = new SmbFiles();
                files.mkdirs(this.context.getShare(), folder);
            }
        }

        try (File f = this.context.getShare().openFile(
                filePath,
                EnumSet.of(AccessMask.GENERIC_WRITE),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_COMPRESSED),
                EnumSet.of(SMB2ShareAccess.FILE_SHARE_WRITE),
                SMB2CreateDisposition.FILE_OVERWRITE_IF,
                EnumSet.of(SMB2CreateOptions.FILE_RANDOM_ACCESS)
        )) {
            f.write(data.getData(), 0);
        }

        return id;
    }

    private String getFilePath(SigaBlob blob, String id) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(blob.getCreatedAt());

        Path path = Paths.get(
                context.getContextName(),
                blob.getCategory().toString().toLowerCase(),
                String.valueOf(calendar.get(Calendar.YEAR)),
                String.valueOf(calendar.get(Calendar.MONTH) + 1),
                String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))
        );

        return path.resolve(id + "." + context.getFileExt()).toString();
    }

    public void clearData(SigaBlob blob) {
        if (blob.getDataIdentifier() == null)
            return;

        String filePath = getFilePath(blob, blob.getDataIdentifier());
        this.context.getShare().rm(filePath);
    }
}
