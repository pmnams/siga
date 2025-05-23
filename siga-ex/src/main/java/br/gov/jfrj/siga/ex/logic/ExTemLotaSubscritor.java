package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExDocumento;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExTemLotaSubscritor implements Expression {
    ExDocumento doc;

    public ExTemLotaSubscritor(ExDocumento doc) {
        this.doc = doc;
    }

    @Override
    public boolean eval() {
        return doc.getLotaSubscritor() != null;
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("tem lotação do subscritor", result);
    }

}
