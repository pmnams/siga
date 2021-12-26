package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEMobilVolumeEncerrado implements Expression {
    ExMobil mob;

    public ExEMobilVolumeEncerrado(ExMobil mob) {
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return mob.isVolumeEncerrado();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("é volume encerrado", result);
    }

}
