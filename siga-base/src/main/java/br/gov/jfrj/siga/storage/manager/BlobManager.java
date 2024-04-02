package br.gov.jfrj.siga.storage.manager;

import br.gov.jfrj.siga.storage.SigaBlob;
import br.gov.jfrj.siga.storage.blob.BlobData;

public interface BlobManager {

    BlobData fromBlob(SigaBlob blob);
    BlobData fromData(SigaBlob blob, byte[] Data);
    String persist(SigaBlob blob, BlobData data);
    default void clearData(SigaBlob blob) {}
}
