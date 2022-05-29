package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExDocumento;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEstaPublicadoNoBoletim implements Expression {
    ExDocumento doc;

    public ExEstaPublicadoNoBoletim(ExDocumento doc) {
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
