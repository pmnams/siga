package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExDocumento;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import com.crivano.jlogic.And;
import com.crivano.jlogic.CompositeExpressionSupport;
import com.crivano.jlogic.Expression;

public class ExPodeVisualizarExternamente extends CompositeExpressionSupport {

    private final ExMobil mob;
    private final ExDocumento doc;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    public ExPodeVisualizarExternamente(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        this.mob = mob;
        this.doc = mob.doc();
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    @Override
    protected Expression create() {
        return And.of(
                new ExEstaFinalizado(doc),
                new ExEstaAssinadoOuAutenticadoComTokenOuSenhaERegistros(doc),
                new ExPodePorConfiguracao(titular, lotaTitular)
                        .withIdTpConf(ExTipoDeConfiguracao.MOVIMENTAR)
                        .withExTpMov(ExTipoDeMovimentacao.VISUALIZACAO_EXTERNA)
                        .withExMod(mob.doc().getExModelo())
        );
    }
}