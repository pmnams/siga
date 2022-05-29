package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import com.crivano.jlogic.*;

public class ExPodeSobrestar extends CompositeExpressionSupport {

    private final ExMobil mob;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    public ExPodeSobrestar(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        this.mob = mob;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    /**
     * Retorna se é possível fazer sobrestar um móbil, segundo as regras a seguir:
     *
     *
     * <ul>
     * <li>Documento tem de estar assinado</li>
     * <li>Móbil tem de ser via ou volume (não pode ser geral)</li>
     * <li><i>podeMovimentar()</i> tem de ser verdadeiro para o usuário / móbil</li>
     * <li>Móbil não pode estar em algum arquivo</li>
     * <li>Móbil não pode estar juntado</li>
     * <li>Móbil não pode estar em trânsito</li>
     * <li>Não pode haver configuração impeditiva</li>
     * </ul>
     */
    @Override
    protected Expression create() {
        return And.of(new ExEstaFinalizado(mob.doc()),

                Or.of(new ExEMobilVia(mob), new ExEMobilUltimoVolume(mob)),

                new ExPodeMovimentar(mob, titular, lotaTitular),

                Not.of(new ExEstaArquivado(mob)), Not.of(new ExEstaApensadoAVolumeDoMesmoProcesso(mob)),
                Not.of(new ExEstaSemEfeito(mob.doc())), Not.of(new ExEstaEmTramiteParalelo(mob)),
                Not.of(new ExEstaArquivado(mob)), Not.of(new ExEstaSobrestado(mob)), Not.of(new ExEstaJuntado(mob)),
                Not.of(new ExEstaEmTransito(mob, titular, lotaTitular)),

                new ExPodeMovimentarPorConfiguracao(ExTipoDeMovimentacao.SOBRESTAR, titular,
                        lotaTitular));
    }
}
