package br.gov.jfrj.siga.storage.manager;

import br.gov.jfrj.siga.storage.SigaBlob;
import br.gov.jfrj.siga.storage.blob.BlobData;

public interface BlobManager {

    BlobData fromId(String id);
    BlobData fromData(SigaBlob blob, byte[] Data);
    String persist(SigaBlob blob, BlobData data);

}
