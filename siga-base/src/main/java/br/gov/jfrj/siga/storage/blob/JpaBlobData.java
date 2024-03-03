package br.gov.jfrj.siga.storage.blob;

import br.gov.jfrj.siga.storage.SigaBlob;

import javax.persistence.*;

@Entity
@Table(name = "siga_blob_data")
public class JpaBlobData implements BlobData {

    @Id
    @Column(name = "BLOB_ID")
    private Long id;

    @Lob
    private byte[] data;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "BLOB_ID")
    @MapsId
    private SigaBlob blob;

    public JpaBlobData(SigaBlob blob) {
        this.blob = blob;
    }

    public JpaBlobData() {

    }

    public String getId() {
        return id.toString();
    }

    public void setId(String id) {
        this.id = Long.valueOf(id);
    }

    @Override
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public SigaBlob getBlob() {
        return blob;
    }

    public void setBlob(SigaBlob blob) {
        this.blob = blob;
    }
}
