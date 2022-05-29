package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExDocumento;
import br.gov.jfrj.siga.ex.ExMovimentacao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExPessoaEstaVinculadaPorPerfil implements Expression {

    private final ExDocumento doc;
    private final DpPessoa titular;

    public ExPessoaEstaVinculadaPorPerfil(ExDocumento doc, DpPessoa titular) {
        this.doc = doc;
        this.titular = titular;
    }

    @Override
    public boolean eval() {
        for (ExMovimentacao mov : doc.getMobilGeral().getExMovimentacaoSet()) {
            if (!mov.isCancelada() && mov.getExTipoMovimentacao() == ExTipoDeMovimentacao.VINCULACAO_PAPEL) {
                if (mov.getSubscritor() != null)
                    if (mov.getSubscritor().equivale(titular))
                        return true;
            }
        }
        return false;
    }

    @Override
    public String explain(boolean result) {
        return "pessoa " + titular.getSiglaCompleta() + (result ? "" : JLogic.NOT)
                + " está vinculada por perfil ao documento " + doc.getCodigo();
    }
}
