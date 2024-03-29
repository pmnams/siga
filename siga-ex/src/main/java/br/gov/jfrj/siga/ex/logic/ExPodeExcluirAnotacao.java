package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.ExMovimentacao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import com.crivano.jlogic.*;

public class ExPodeExcluirAnotacao extends CompositeExpressionSupport {

    private final ExMobil mob;
    private final ExMovimentacao mov;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    public ExPodeExcluirAnotacao(ExMobil mob, ExMovimentacao mov, DpPessoa titular, DpLotacao lotaTitular) {
        if (mob.isGeralDeProcesso() && mob.doc().isFinalizado())
            mob = mob.doc().getUltimoVolume();
        this.mob = mob;
        this.mov = mov;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    /**
     * Retorna se é possível excluir anotação realizada no móbil, passada pelo
     * parâmetro mov. As seguintes regras incidem sobre a movimentação a ser
     * excluída:
     * <ul>
     * <li>Não pode estar cancelada</li>
     * <li>Lotação do usuário tem de ser a do cadastrante ou do subscritor
     * (responsável) da movimentação</li>
     * <li>Não pode haver configuração impeditiva. Tipo de configuração: Excluir
     * Anotação</li>
     * </ul>
     */
    @Override
    protected Expression create() {
        return And.of(

                Not.of(new ExMovimentacaoEstaCancelada(mov)),

                Or.of(

                        new ExMovimentacaoECadastrante(mov, titular),

                        new ExMovimentacaoESubscritor(mov, titular),

                        new ExMovimentacaoELotaCadastrante(mov, titular.getLotacao()),

                        new ExMovimentacaoELotaSubscritor(mov, titular.getLotacao())),

                new ExPodePorConfiguracao(titular, lotaTitular).withIdTpConf(ExTipoDeConfiguracao.EXCLUIR_ANOTACAO));
    }
}
