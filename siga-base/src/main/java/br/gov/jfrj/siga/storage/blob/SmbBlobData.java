package br.gov.jfrj.siga.storage.blob;

public class SmbBlobData implements BlobData {
    private String id;

    private byte[] data;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public void setData(byte[] data) {
        this.data = data;
    }
}
