package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.cp.logic.CpNaoENulo;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.*;

public class ExPodeCriarVolume extends CompositeExpressionSupport {

    private final ExMobil mob;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;
    private final ExMobil ultVolume;

    public ExPodeCriarVolume(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        this.mob = mob;
        this.titular = titular;
        this.lotaTitular = lotaTitular;

        ultVolume = mob.doc().getUltimoVolume();
    }

    /**
     * Retorna se é possível criar volume para o documento que contém o móbil
     * passado por parâmetro, de acordo com as seguintes condições:
     * <ul>
     * <li>Documento tem de ser processo</li>
     * <li>Processo tem de estar finalizado</li>
     * <li>Último volume tem de estar encerrado</li>
     * </ul>
     *
     */
    @Override
    protected Expression create() {
        return And.of(

                Not.of(new ExEstaSemEfeito(mob.doc())),

                new ExEProcesso(mob.doc()),

                NAnd.of(

                        new CpNaoENulo(ultVolume, "último volume"),

                        Or.of(

                                new ExEstaEmTransito(ultVolume, titular, lotaTitular),

                                new ExEstaSobrestado(ultVolume))),

                Not.of(new ExEstaArquivado(mob)),

                new ExPodeMovimentar(mob, titular, lotaTitular),

                Not.of(new ExEstaPendenteDeAssinatura(mob.doc())),

                new ExEstaFinalizado(mob.doc()),

                new ExEstaEncerrado(ultVolume),

                NAnd.of(

                        new ExEEletronico(mob.doc()),

                        Or.of(

                                new ExTemAnexosNaoAssinados(mob),

                                new ExTemDespachosNaoAssinados(mob))));
    }
}
