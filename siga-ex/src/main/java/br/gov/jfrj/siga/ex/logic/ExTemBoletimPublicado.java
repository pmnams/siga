package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExDocumento;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExTemBoletimPublicado implements Expression {
    ExDocumento doc;

    public ExTemBoletimPublicado(ExDocumento doc) {
        this.doc = doc;
    }

    @Override
    public boolean eval() {
        return doc.isBoletimPublicado();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("está publicado no boletim", result);
    }

}
