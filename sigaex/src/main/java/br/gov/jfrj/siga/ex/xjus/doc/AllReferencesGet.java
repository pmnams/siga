package br.gov.jfrj.siga.ex.xjus.doc;

import br.gov.jfrj.siga.ex.xjus.Utils;
import br.gov.jfrj.siga.hibernate.ExDao;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.Reference;
import br.jus.trf2.xjus.record.api.XjusRecordAPIContext;

import javax.persistence.TypedQuery;
import java.util.ArrayList;

public class AllReferencesGet implements IXjusRecordAPI.IAllReferencesGet {

    private static final String HQL = "select doc.idDoc from ExDocumento doc where (doc.dtFinalizacao != null) and (doc.idDoc > :id) order by doc.idDoc";

    @Override
    public void run(Request req, Response resp, XjusRecordAPIContext ctx) throws Exception {
        resp.list = new ArrayList<>();
        if (req.lastid == null)
            req.lastid = Utils.formatId(0L);
        try {
            ExDao dao = ExDao.getInstance();
            TypedQuery<Long> q = dao.em().createQuery(HQL, Long.class);
            q.setMaxResults(Integer.parseInt(req.max));
            Long first = Long.valueOf(req.lastid);
            q.setParameter("id", first);

            for (Long id : q.getResultList()) {
                Reference ref = new Reference();
                ref.id = Utils.formatId(id);
                resp.list.add(ref);
            }
        } finally {
            ExDao.freeInstance();
        }

    }

    public String getContext() {
        return "obter a lista de índices";
    }
}
