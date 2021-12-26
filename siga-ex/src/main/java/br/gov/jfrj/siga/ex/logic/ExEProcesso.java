package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExDocumento;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEProcesso implements Expression {
    ExDocumento doc;

    public ExEProcesso(ExDocumento doc) {
        this.doc = doc;
    }

    @Override
    public boolean eval() {
        return doc.isProcesso();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("é processo", result);
    }

}
