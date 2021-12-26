package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEMobilVolume implements Expression {
    ExMobil mob;

    public ExEMobilVolume(ExMobil mob) {
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return mob.isVolume();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("é volume", result);
    }

}
