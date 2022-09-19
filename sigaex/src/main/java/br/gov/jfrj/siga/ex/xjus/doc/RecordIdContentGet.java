package br.gov.jfrj.siga.ex.xjus.doc;

import br.gov.jfrj.siga.base.HtmlToPlainText;
import br.gov.jfrj.siga.ex.ExDocumento;
import br.gov.jfrj.siga.ex.util.PdfToPlainText;
import br.gov.jfrj.siga.hibernate.ExDao;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI;
import br.jus.trf2.xjus.record.api.XjusRecordAPIContext;

public class RecordIdContentGet implements IXjusRecordAPI.IRecordIdContentGet {

    @Override
    public void run(Request req, Response resp, XjusRecordAPIContext ctx) throws Exception {

        try {
            long primaryKey;
            try {
                primaryKey = Long.parseLong(req.id);
            } catch (NumberFormatException nfe) {
                throw new RuntimeException("REMOVED");
            }

            ExDocumento doc = ExDao.getInstance().consultar(primaryKey, ExDocumento.class, false);
            if (doc == null) {
                throw new RuntimeException("REMOVED");
            }

            resp.id = req.id;

            String html = doc.getHtml();
            if (html != null) {
                resp.content = HtmlToPlainText.getText(html).trim();
                return;
            }

            byte[] pdf = doc.getPdf();
            if (pdf != null) {
                resp.content = PdfToPlainText.getText(pdf);
            }
        } finally {
            ExDao.freeInstance();
        }
    }


    public String getContext() {
        return "obter a lista de índices";
    }

}