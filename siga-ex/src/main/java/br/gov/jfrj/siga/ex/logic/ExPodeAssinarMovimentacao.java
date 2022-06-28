package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.ExMovimentacao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import com.crivano.jlogic.CompositeExpressionSupport;
import com.crivano.jlogic.Expression;

public class ExPodeAssinarMovimentacao extends CompositeExpressionSupport {

    private final ExMobil mob;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    /*
     * Retorna se pode autenticar uma movimentação que só foi assinada com senha.
     *
     */
    public ExPodeAssinarMovimentacao(ExMovimentacao mov, DpPessoa titular, DpLotacao lotaTitular) {
        this.mob = mov.mob();
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    public ExPodeAssinarMovimentacao(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        this.mob = mob;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    @Override
    protected Expression create() {
        return new ExPodePorConfiguracao(titular, lotaTitular).withExMod(mob.doc().getExModelo())
                .withExFormaDoc(mob.doc().getExFormaDocumento()).withIdTpConf(ExTipoDeConfiguracao.MOVIMENTAR)
                .withExTpMov(ExTipoDeMovimentacao.ASSINATURA_DIGITAL_MOVIMENTACAO);
    }
}
