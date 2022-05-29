package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExDocumento;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEInternoFolhaDeRosto implements Expression {
    ExDocumento doc;

    public ExEInternoFolhaDeRosto(ExDocumento doc) {
        this.doc = doc;
    }

    @Override
    public boolean eval() {
        return doc.isInternoFolhaDeRosto();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("é interno folha de rosto", result);
    }

}
