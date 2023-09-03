package br.gov.jfrj.siga.storage;

public interface BlobData {
    public Long getId();
    public void setId(Long id);
    byte[] getData();
    public void setData(byte[] data);
}
