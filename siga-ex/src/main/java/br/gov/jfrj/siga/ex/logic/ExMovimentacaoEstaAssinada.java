package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMovimentacao;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExMovimentacaoEstaAssinada implements Expression {
    ExMovimentacao mov;

    public ExMovimentacaoEstaAssinada(ExMovimentacao mov) {
        this.mov = mov;
    }

    @Override
    public boolean eval() {
        return mov.isAssinada();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("está assinada a movimentação", result);
    }

}
