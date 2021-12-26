package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEstaJuntado implements Expression {
    ExMobil mob;

    public ExEstaJuntado(ExMobil mob) {
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return mob.isJuntado();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("está juntado", result);
    }

}