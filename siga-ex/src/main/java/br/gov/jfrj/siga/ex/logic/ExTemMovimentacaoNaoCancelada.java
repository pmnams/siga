package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.ExMovimentacao;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExTemMovimentacaoNaoCancelada implements Expression {
    ExMobil mob;
    ExMovimentacao exUltMovNaoCanc;
    ExMovimentacao exUltMov;

    public ExTemMovimentacaoNaoCancelada(ExMobil mob) {
        this.mob = mob;
        exUltMovNaoCanc = mob.getUltimaMovimentacaoNaoCancelada();
        exUltMov = mob.getUltimaMovimentacao();
    }

    @Override
    public boolean eval() {
        return exUltMov != null && exUltMovNaoCanc != null;
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("tem movimentação não cancelada", result);
    }

}
