package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.base.util.Utils;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMovimentacao;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExMovimentacaoECadastrante implements Expression {
    ExMovimentacao mov;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    public ExMovimentacaoECadastrante(ExMovimentacao mov, DpPessoa titular) {
        this(mov, titular, null);
    }

    public ExMovimentacaoECadastrante(ExMovimentacao mov, DpLotacao lotaTitular) {
        this(mov, null, lotaTitular);
    }

    public ExMovimentacaoECadastrante(ExMovimentacao mov, DpPessoa titular, DpLotacao lotaTitular) {
        this.mov = mov;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    @Override
    public boolean eval() {
        return Utils.equivale(mov.getCadastrante(), titular) || Utils.equivale(mov.getLotaCadastrante(), lotaTitular);
    }

    @Override
    public String explain(boolean result) {
        return (titular != null && lotaTitular != null
                ? titular.getSiglaCompleta() + "/" + lotaTitular.getSiglaCompleta()
                : titular != null ? titular.getSiglaCompleta()
                : lotaTitular != null ? lotaTitular.getSiglaCompleta() : "")

                + (result ? "" : JLogic.NOT) + " é subscritor de " + mov.getReferencia();
    }

}
