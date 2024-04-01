package br.gov.jfrj.siga.storage.manager;

import br.gov.jfrj.siga.model.ContextoPersistencia;
import br.gov.jfrj.siga.storage.Manager;
import br.gov.jfrj.siga.storage.SigaBlob;
import br.gov.jfrj.siga.storage.StorageType;
import br.gov.jfrj.siga.storage.blob.BlobData;
import br.gov.jfrj.siga.storage.blob.impl.JpaBlobData;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@ApplicationScoped
@Manager(StorageType.DATABASE)
@Default
public class JpaBlobManager implements BlobManager {

    @Inject
    private EntityManager em;

    @Override
    public BlobData fromBlob(SigaBlob blob) {
        if (blob.getDataIdentifier() == null)
            return null;

        Long JpaBlobId = Long.parseLong(blob.getDataIdentifier());

        CriteriaBuilder cb = ContextoPersistencia.em().getCriteriaBuilder();
        CriteriaQuery<JpaBlobData> q = cb.createQuery(JpaBlobData.class);
        Root<JpaBlobData> root = q.from(JpaBlobData.class);
        q.select(root).where(cb.equal(root.get("id"), JpaBlobId));

        TypedQuery<JpaBlobData> query = ContextoPersistencia.em().createQuery(q);
        return query.getSingleResult();
    }

    @Override
    public BlobData fromData(SigaBlob blob, byte[] data) {
        JpaBlobData blobData = new JpaBlobData();
        blobData.setBlob(blob);
        blobData.setData(data);
        return blobData;
    }

    @Override
    public String persist(SigaBlob blob, BlobData data) {
        em.persist(data);

        return data.getId();
    }
}
