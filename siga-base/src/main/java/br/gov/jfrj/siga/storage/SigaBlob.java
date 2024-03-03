package br.gov.jfrj.siga.storage;

import br.gov.jfrj.siga.base.Prop;
import br.gov.jfrj.siga.storage.blob.BlobData;
import br.gov.jfrj.siga.storage.manager.BlobManager;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.persistence.*;
import java.util.Date;

@Entity()
@Table(name = "siga_blob")
public class SigaBlob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    private StorageType type;

    @Column(name = "DATA_IDENTIFIER")
    private String dataIdentifier;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Transient
    private BlobData data;

    @Transient
    @Inject
    private BlobManager manager;

    public SigaBlob(byte[] data) {
        this();
        setData(data);
    }

    public SigaBlob() {
        try {
            type = StorageType.valueOf(Prop.get("/storage.type"));
        } catch (Exception e) {
            type = StorageType.DATABASE;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getData() {
        initSource();

        if (data != null)
            return data.getData();
        else
            return null;
    }

    public void setData(byte[] data) {
        initSource();

        if (this.data == null)
            this.data = manager.fromData(this, data);
        else
            this.data.setData(data);

        updatedAt = new Date();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    private void initSource() {
        if (this.data != null)
            return;

        if (manager == null)
            setManager(this.type);

        data = manager.fromId(this.dataIdentifier);
    }

    private void setManager(StorageType type) {
        if (type == null) {
            manager = CDI.current().select(BlobManager.class).get();

            this.type = manager.getClass().getAnnotation(Manager.class).type();
        } else
            manager = CDI.current().select(BlobManager.class, new LiteralManager(type)).get();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    @PostUpdate
    @PostPersist
    private void postPersist() {
        if (this.data != null) {
            try {
                this.dataIdentifier = manager.persist(this, this.data);
            } catch (Exception e) {
                setManager(StorageType.DATABASE);
                this.dataIdentifier = manager.persist(this, this.data);
            }
        }
    }

}
