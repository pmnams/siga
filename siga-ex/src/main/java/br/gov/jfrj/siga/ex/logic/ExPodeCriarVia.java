package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.ExMovimentacao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import com.crivano.jlogic.*;

public class ExPodeCriarVia extends CompositeExpressionSupport {

    private final ExMobil mob;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    public ExPodeCriarVia(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        this.mob = mob;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    /**
     * Retorna se é possível criar via para o documento que contém o móbil passado
     * por parâmetro, conforme as seguintes condições:
     * <ul>
     * <li>Documento tem de ser expediente</li>
     * <li>Documento não pode ter pai, pois não é permitido criar vias em documento
     * filho</li>
     * <li>Número da última via não pode ser maior ou igual a 21</li>
     * <li>Documento tem de estar finalizado</li>
     * <li>Documento não pode ter sido eliminado</li>
     * <li>Documento tem de possuir alguma via não cancelada</li>
     * <li>Lotação do titular igual a do cadastrante ou a do subscritor ou
     * <p>
     * o titular ser o próprio subscritor</li>
     *
     */
    @Override
    protected Expression create() {
        return And.of(
                Not.of(new ExEstaSemEfeito(mob.doc())),
                new ExEExpediente(mob.doc()),
                Not.of(new ExEstaEliminado(mob)),
                Not.of(new ExEstaPendenteDeColaboracao(mob)),
                NAnd.of(
                        new ExTemMobilPai(mob.doc()),
                        new ExEstaPendenteDeAssinatura(mob.doc())
                ),
                Not.of(new ExTemNumeroMaximoDeVias(mob.doc())),
                new ExEstaFinalizado(mob.doc()),
                Or.of(
                        new ExPodeMovimentar(mob, titular, lotaTitular),
                        new ExECadastrante(mob.doc(), lotaTitular),
                        new ExESubscritor(mob.doc(), titular, lotaTitular)
                ),
                new ExPodePorConfiguracao(titular, lotaTitular)
                        .withExFormaDoc(mob.doc().getExFormaDocumento())
                        .withExMod(mob.doc().getExModelo())
                        .withIdTpConf(ExTipoDeConfiguracao.CRIAR_VIA)
        );

    }

}
