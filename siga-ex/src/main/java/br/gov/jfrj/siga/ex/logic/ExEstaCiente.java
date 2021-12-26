package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEstaCiente implements Expression {

    private final ExMobil mob;
    private final DpPessoa titular;

    public ExEstaCiente(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
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
