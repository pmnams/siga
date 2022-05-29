package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExDocumento;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEstaPendenteDeAssinatura implements Expression {
    ExDocumento doc;

    public ExEstaPendenteDeAssinatura(ExDocumento doc) {
        this.doc = doc;
    }

    @Override
    public boolean eval() {
        return doc.isPendenteDeAssinatura();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("está pendente de assinatura", result);
    }

}
