package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExDocumento;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEExternoCapturado implements Expression {
    ExDocumento doc;

    public ExEExternoCapturado(ExDocumento doc) {
        this.doc = doc;
    }

    @Override
    public boolean eval() {
        return doc.isExternoCapturado();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("é externo capturado", result);
    }

}
