package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import com.crivano.jlogic.CompositeExpressionSupport;
import com.crivano.jlogic.Expression;

public class ExPodeReceberDocumentoSemAssinatura extends CompositeExpressionSupport {

    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    public ExPodeReceberDocumentoSemAssinatura(DpPessoa titular, DpLotacao lotaTitular) {
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    /**
     * Retorna se é possível a uma lotação, com base em configuração, receber móbil
     * de documento não assinado. Não é aqui verificado se o móbil está realmente
     * pendente de assinatura
     */
    @Override
    protected Expression create() {

        return new ExPodePorConfiguracao(titular, lotaTitular)
                .withIdTpConf(ExTipoDeConfiguracao.RECEBER_DOC_NAO_ASSINADO);

    }
}
