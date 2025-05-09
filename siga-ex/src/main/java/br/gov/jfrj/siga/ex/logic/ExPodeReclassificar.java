package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import com.crivano.jlogic.And;
import com.crivano.jlogic.CompositeExpressionSupport;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.Not;

public class ExPodeReclassificar extends CompositeExpressionSupport {

    private ExMobil mob;
    private DpPessoa titular;
    private DpLotacao lotaTitular;

    public ExPodeReclassificar(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        this.mob = mob;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    /**
     * Retorna se é possível reclassificar um documento. Têm de ser satisfeitas as
     * seguintes condições:
     * <ul>
     * <li>Documento tem de estar assinado</li>
     * <li>Móbil tem de ser geral</li>
     * <li>Móbil não pode ter sido eliminado</li>
     * <li>Móbil não pode estar cancelado</li>
     * <li>Não pode haver configuração impeditiva</li>
     * *
     *
     * @param titular
     * @param lotaTitular
     * @param mob
     * @return
     * @throws Exception
     */
    @Override
    protected Expression create() {
        return And.of(

                Not.of(new ExEstaPendenteDeAssinatura(mob.doc())),

                new ExEMobilGeral(mob),

                Not.of(new ExEMobilCancelado(mob)),

                Not.of(new ExEstaEliminado(mob)),

                new ExPodeMovimentarPorConfiguracao(ExTipoDeMovimentacao.RECLASSIFICACAO, titular,
                        lotaTitular));
    }
}
