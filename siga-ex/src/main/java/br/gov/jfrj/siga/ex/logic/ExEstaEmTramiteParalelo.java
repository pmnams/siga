package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEstaEmTramiteParalelo implements Expression {

    private ExMobil mob;

    public ExEstaEmTramiteParalelo(ExMobil mob) {
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return mob.isEmTramiteParalelo();
    }

    @Override
    public String explain(boolean result) {
        return mob.getCodigo() + (result ? "" : JLogic.NOT) + " está em trâmite paralelo";
    }
}
