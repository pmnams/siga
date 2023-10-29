package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import com.crivano.jlogic.CompositeExpressionSupport;
import com.crivano.jlogic.Expression;

public class ExPodeAtenderPedidoPublicacaoNoDiario extends CompositeExpressionSupport {

    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    /**
     * Retorna se é possível, com base em configuração, utilizar a rotina de
     * atendimento de pedidos indiretos de publicação no DJE. Não é utilizado o
     * parãmetro mob.
     */
    public ExPodeAtenderPedidoPublicacaoNoDiario(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    @Override
    protected Expression create() {
        return new ExPodePorConfiguracao(titular, lotaTitular)
                .withIdTpConf(ExTipoDeConfiguracao.ATENDER_PEDIDO_PUBLICACAO);
    }
}
