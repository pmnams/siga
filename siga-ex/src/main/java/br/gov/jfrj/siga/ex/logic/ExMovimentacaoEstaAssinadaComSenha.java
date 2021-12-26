package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExMovimentacao;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExMovimentacaoEstaAssinadaComSenha implements Expression {
    ExMovimentacao mov;

    public ExMovimentacaoEstaAssinadaComSenha(ExMovimentacao mov) {
        this.mov = mov;
    }

    @Override
    public boolean eval() {
        return mov.isAutenticada();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("está autenticada a movimentação", result);
    }

}
