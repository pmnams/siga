package br.gov.jfrj.siga.wf.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.wf.model.WfProcedimento;
import com.crivano.jlogic.CompositeExpressionSupport;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.Or;

public class WfPodeRedirecionar extends CompositeExpressionSupport {

    private final WfProcedimento pi;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    public WfPodeRedirecionar(WfProcedimento pi, DpPessoa titular, DpLotacao lotaTitular) {
        this.pi = pi;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    @Override
    protected Expression create() {
        return Or.of(
                new WfEstaResponsavel(pi, titular, lotaTitular),
                new WfPodeEditarDiagrama(pi.getDefinicaoDeProcedimento(), titular, lotaTitular)
        );
    }
}
