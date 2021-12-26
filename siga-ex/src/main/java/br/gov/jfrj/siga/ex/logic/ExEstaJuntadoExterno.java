package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEstaJuntadoExterno implements Expression {
    ExMobil mob;

    public ExEstaJuntadoExterno(ExMobil mob) {
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return mob.isJuntadoExterno();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("está juntado a documento externo", result);
    }

}
