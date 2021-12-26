package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExTemDocumentosFilhos implements Expression {
    ExMobil mob;

    public ExTemDocumentosFilhos(ExMobil mob) {
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return !mob.getExDocumentoFilhoSet().isEmpty();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("tem documentos filhos", result);
    }

}
