package br.gov.jfrj.siga.wf.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.wf.model.WfDefinicaoDeProcedimento;
import br.gov.jfrj.siga.wf.model.enm.WfAcessoDeEdicao;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class WfPodeEditarDiagramaPorAcesso implements Expression {

    private final WfDefinicaoDeProcedimento pd;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    public WfPodeEditarDiagramaPorAcesso(WfDefinicaoDeProcedimento pd, DpPessoa titular, DpLotacao lotaTitular) {
        this.pd = pd;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    @Override
    public boolean eval() {
        return WfAcessoDeEdicao.acessoPermitido(pd, titular, lotaTitular);
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("tem acesso para editar o diagrama " + pd.getSigla(), result);
    }
}
