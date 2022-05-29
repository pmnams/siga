package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExTemAnexosNaoAssinados implements Expression {
    ExMobil mob;

    public ExTemAnexosNaoAssinados(ExMobil mob) {
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return mob.temAnexosNaoAssinados();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("tem anexos não assinados", result);
    }

}
