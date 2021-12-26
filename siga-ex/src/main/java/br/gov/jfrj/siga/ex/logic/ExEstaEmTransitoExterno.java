package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEstaEmTransitoExterno implements Expression {

    ExMobil mob;

    public ExEstaEmTransitoExterno(ExMobil mob) {
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return mob.isEmTransitoExterno();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("está em transito externo", result);
    }

}
