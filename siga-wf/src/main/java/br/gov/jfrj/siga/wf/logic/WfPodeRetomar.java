package br.gov.jfrj.siga.wf.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.wf.model.WfProcedimento;
import com.crivano.jlogic.CompositeExpressionSuport;
import com.crivano.jlogic.Expression;

public class WfPodeRetomar extends CompositeExpressionSuport {

    private final WfProcedimento pi;

    public WfPodeRetomar(WfProcedimento pi, DpPessoa titular, DpLotacao lotaTitular) {
        this.pi = pi;
    }

    @Override
    protected Expression create() {
        return new WfEstaRetomando(pi);
    }
}

