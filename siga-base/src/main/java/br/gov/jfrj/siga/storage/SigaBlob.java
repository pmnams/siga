package br.gov.jfrj.siga.storage;

import br.gov.jfrj.siga.base.Prop;
import br.gov.jfrj.siga.storage.blob.BlobCategory;
import br.gov.jfrj.siga.storage.blob.BlobData;
import br.gov.jfrj.siga.storage.manager.BlobManager;

import javax.enterprise.inject.spi.CDI;
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

    @Column(name = "CREATED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "UPDATED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "CATEGORY")
    private Integer category;

    @Transient
    private BlobData data;

    @Transient
    private BlobManager manager;

    public SigaBlob(byte[] data, BlobCategory category) {
        this(data);
        this.category = category.ordinal();
    }

    public SigaBlob(byte[] data) {
        setData(data);
    }

    public SigaBlob() {
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

    public BlobCategory getCategory() {
        if (category == null)
            category = BlobCategory.DefaultBlobCategory.DEFAULT.value;

        return BlobCategory.enumOf(category);
    }

    public void setCategory(BlobCategory category) {
        this.category = category.getValue();
    }

    private void initSource() {
        if (this.data != null)
            return;

        if (manager == null)
            setManager(this.type);

        data = manager.fromBlob(this);
    }

    private void setManager(StorageType type) {
        if (type == null) {
            try {
                type = StorageType.valueOf(Prop.get("/storage.type"));
                manager = CDI.current().select(BlobManager.class, new Manager.Literal(type)).get();
            } catch (Exception ignored) {
                manager = CDI.current().select(BlobManager.class).get();
                type = manager.getClass().getAnnotation(Manager.class).value();
            }
        } else
            manager = CDI.current().select(BlobManager.class, new Manager.Literal(type)).get();

        this.type = type;
    }

    public String getDataIdentifier() {
        return dataIdentifier;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
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
                e.printStackTrace();
                setManager(StorageType.DATABASE);
                this.data = manager.fromData(this, this.data.getData());
                this.dataIdentifier = manager.persist(this, this.data);
            }
        }
    }

    @PostRemove
    private void postRemove() {
        if (manager == null)
            setManager(this.type);

        manager.clearData(this);
    }

}
