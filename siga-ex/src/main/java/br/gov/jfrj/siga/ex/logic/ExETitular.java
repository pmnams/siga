package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.base.util.Utils;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExDocumento;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExETitular implements Expression {

    private final ExDocumento doc;
    private final DpPessoa titular;

    public ExETitular(ExDocumento doc, DpPessoa titular) {
        this.doc = doc;
        this.titular = titular;
    }

    @Override
    public boolean eval() {
        return Utils.equivale(doc.getTitular(), titular);
    }

    @Override
    public String explain(boolean result) {
        return titular.getSiglaCompleta() + (result ? "" : JLogic.NOT) + " é titular de " + doc.getCodigo();
    }
}
