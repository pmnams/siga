package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExMobEstaCiente implements Expression {
    ExMobil mob;
    DpPessoa titular;

    public ExMobEstaCiente(ExMobil mob, DpPessoa titular) {
        this.mob = mob;
        this.titular = titular;
    }

    @Override
    public boolean eval() {
        return mob.isCiente(titular);
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("está ciente", result);
    }

}
