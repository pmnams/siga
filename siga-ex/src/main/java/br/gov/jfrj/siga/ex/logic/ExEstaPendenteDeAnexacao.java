package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEstaPendenteDeAnexacao implements Expression {

    private final ExMobil mob;

    public ExEstaPendenteDeAnexacao(ExMobil mob) {
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return mob.isPendenteDeAnexacao();
    }

    @Override
    public String explain(boolean result) {
        return mob.getCodigo() + (result ? "" : JLogic.NOT) + " está pendente de anexação";
    }
}
