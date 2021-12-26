package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExTemTemporalidadePermanente implements Expression {
    ExMobil mob;

    public ExTemTemporalidadePermanente(ExMobil mob) {
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return mob.isDestinacaoGuardaPermanente();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("temporalidade com destinação ao arquivo permanente", result);
    }

}
