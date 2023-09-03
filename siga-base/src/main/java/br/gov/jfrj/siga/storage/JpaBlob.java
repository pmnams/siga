package br.gov.jfrj.siga.storage;

import javax.persistence.*;

@Entity
@Table(name = "siga_blob_data")
public class JpaBlob implements BlobData {

    @Id
    @Column(name = "BLOB_ID")
    private Long id;

    @Lob
    private byte[] data;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "BLOB_ID")
    @MapsId
    private SigaBlob blob;

    public JpaBlob(SigaBlob blob) {
        this.blob = blob;
    }

    public JpaBlob() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
