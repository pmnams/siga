package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEstaArquivado implements Expression {
    ExMobil mob;

    public ExEstaArquivado(ExMobil mob) {
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return mob != null && mob.isArquivado();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("está arquivado", result);
    }

}
