package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExDocumento;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExECossignatario implements Expression {

    private final ExDocumento doc;
    private final DpPessoa titular;

    public ExECossignatario(ExDocumento doc, DpPessoa titular) {
        this.doc = doc;
        this.titular = titular;
    }

    @Override
    public boolean eval() {
        for (DpPessoa autorizado : doc.getCosignatarios()) {
            if (titular.equivale(autorizado))
                return true;
        }
        return false;
    }

    @Override
    public String explain(boolean result) {
        return titular.getSiglaCompleta() + (result ? "" : JLogic.NOT) + " é cossignatário de " + doc.getCodigo();
    }
}
