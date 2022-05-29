package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEMobilGeralDeProcesso implements Expression {
    ExMobil mob;

    public ExEMobilGeralDeProcesso(ExMobil mob) {
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return mob.isGeralDeProcesso();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("é móbil geral de processo", result);
    }

}
