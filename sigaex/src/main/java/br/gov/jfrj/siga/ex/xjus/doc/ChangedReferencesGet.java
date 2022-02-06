package br.gov.jfrj.siga.ex.xjus.doc;

import br.gov.jfrj.siga.ex.xjus.Utils;
import br.gov.jfrj.siga.hibernate.ExDao;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.Reference;
import br.jus.trf2.xjus.record.api.XjusRecordAPIContext;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChangedReferencesGet implements IXjusRecordAPI.IChangedReferencesGet {

    private static final String HQL = "select doc.idDoc, doc.dtAltDoc from ExDocumento doc where (doc.dtFinalizacao != null) and ((doc.dtAltDoc = :dt and doc.idDoc > :id) or (doc.dtAltDoc > :dt)) order by doc.dtAltDoc";

    @Override
    public void run(Request req, Response resp, XjusRecordAPIContext ctx) throws Exception {
        resp.list = new ArrayList<>();
        if (req.lastdate == null)
            req.lastdate = new Date(0L);
        if (req.lastid == null)
            req.lastid = Utils.formatId(0L);
        try {
            ExDao dao = ExDao.getInstance();
            TypedQuery<Object[]> q = dao.em().createQuery(HQL, Object[].class);
            q.setMaxResults(Integer.parseInt(req.max));
            Date first = req.lastdate;
            Long lastid = Long.valueOf(req.lastid);
            q.setParameter("dt", first);
            q.setParameter("id", lastid);

            List<Object[]> list = q.getResultList();
            for (Object[] rs : list) {
                Reference ref = new Reference();
                Long id = (Long) rs[0];
                Date dt = (Date) rs[1];
                // System.out.println(SwaggerUtils.format(dt));
                ref.id = Utils.formatId(id);
                ref.date = dt;
                resp.list.add(ref);
            }
        } finally {
            ExDao.freeInstance();
        }

    }

    public String getContext() {
        return "obter a lista de referências";
    }
}
