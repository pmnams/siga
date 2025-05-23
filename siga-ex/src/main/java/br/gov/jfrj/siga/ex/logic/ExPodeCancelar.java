package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.ExMovimentacao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import com.crivano.jlogic.*;

public class ExPodeCancelar extends CompositeExpressionSupport {

    private final ExMobil mob;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;
    private final ExMovimentacao mov;

    public ExPodeCancelar(ExMobil mob, ExMovimentacao mov, DpPessoa titular, DpLotacao lotaTitular) {
        this.mob = mob;
        this.mov = mov;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    /**
     * Retorna se é possível cancelar uma movimentação mov, segundo as regras
     * abaixo. <b>Método em desuso?</b>
     * <ul>
     * <li>Movimentação não pode estar cancelada</li>
     * <li>Usuário tem de ser da lotação cadastrante da movimentação</li>
     * <li>Não pode haver configuração impeditiva. Tipo de configuração: Cancelar
     * Movimentação</li>
     * </ul>
     */
    @Override
    protected Expression create() {
        return And.of(

                Not.of(new ExEMobilCancelado(mob)),

                Or.of(

                        new ExMovimentacaoEDoTipo(mov,
                                ExTipoDeMovimentacao.ANEXACAO_DE_ARQUIVO_AUXILIAR),

                        new ExMovimentacaoELotaCadastrante(mov, lotaTitular)),

                new ExPodePorConfiguracao(titular, lotaTitular).withIdTpConf(ExTipoDeConfiguracao.CANCELAR_MOVIMENTACAO).withExTpMov(mov.getExTipoMovimentacao()));
    }
}
