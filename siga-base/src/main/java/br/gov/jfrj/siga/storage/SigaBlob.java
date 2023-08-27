package br.gov.jfrj.siga.storage;

import br.gov.jfrj.siga.model.ContextoPersistencia;

import javax.inject.Inject;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Entity()
@Table(name = "siga_blob")
public class SigaBlob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    private StorageType type = StorageType.DATABASE;

    @Column(name = "DATA_IDENTIFIER")
    private String dataIdentifier;

    @Transient
    private BlobData data;

    @Transient
    @Inject
    private EntityManager em;

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

        if (data == null) {
            CriteriaBuilder cb = ContextoPersistencia.em().getCriteriaBuilder();

            CriteriaQuery<JpaBlob> q = cb.createQuery(JpaBlob.class);
            Root<JpaBlob> root = q.from(JpaBlob.class);
            q.select(root).where(cb.equal(root.get("id"), this.id));

            TypedQuery<JpaBlob> query = ContextoPersistencia.em().createQuery(q);
            data = query.getSingleResult();
        }

        if (data != null)
            return data.getData();
        else
            return null;
    }

    public void setData(byte[] data) {

        if (this.data == null)
            this.data = new JpaBlob(this);

        this.data.setData(data);
    }

    @PostPersist
    private void postPersist() {
        // switch (this.type) {}
        // Atualmente apenas por DATABASE

        if (this.data != null) {
            ContextoPersistencia.em().persist(this.data);

            this.dataIdentifier = this.data.getId().toString();
        }
    }

}
