package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEstaArquivadoCorrente implements Expression {
    ExMobil mob;

    public ExEstaArquivadoCorrente(ExMobil mob) {
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return mob.isArquivadoCorrente();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("está no arquivo corrente", result);
    }

}
