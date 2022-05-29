package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExDocumento;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import com.crivano.jlogic.CompositeExpressionSupport;
import com.crivano.jlogic.Expression;

public class ExPodeAnexarArquivoAuxiliar extends CompositeExpressionSupport {

    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    /**
     * Retorna se é possível anexar um arquivo auxiliar no mob. Regras:
     * <ul>
     * <li>Não pode haver configuração impeditiva.</li>
     * </ul>
     *
     */
    public ExPodeAnexarArquivoAuxiliar(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    @Override
    protected Expression create() {
        return

                new ExPodePorConfiguracao(titular, lotaTitular).withIdTpConf(ExTipoDeConfiguracao.MOVIMENTAR)
                        .withExTpMov(ExTipoDeMovimentacao.ANEXACAO_DE_ARQUIVO_AUXILIAR);
    }
}
