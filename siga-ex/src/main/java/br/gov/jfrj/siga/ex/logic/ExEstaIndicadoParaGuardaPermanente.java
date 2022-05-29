package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEstaIndicadoParaGuardaPermanente implements Expression {
    ExMobil mob;

    public ExEstaIndicadoParaGuardaPermanente(ExMobil mob) {
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return mob.isindicadoGuardaPermanente();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("está indicado para guarda permanente", result);
    }

}
