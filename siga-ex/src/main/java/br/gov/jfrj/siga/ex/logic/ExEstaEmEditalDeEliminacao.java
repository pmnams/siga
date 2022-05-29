package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEstaEmEditalDeEliminacao implements Expression {
    ExMobil mob;

    public ExEstaEmEditalDeEliminacao(ExMobil mob) {
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return mob.isEmEditalEliminacao();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("está em edital de eliminação", result);
    }

}
