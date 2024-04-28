package br.gov.jfrj.siga.storage.blob;

public interface BlobData {
    String getId();
    void setId(String id);
    byte[] getData();
    void setData(byte[] data);
}
