package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import com.crivano.jlogic.*;

public class ExPodeConcluir extends CompositeExpressionSupport {

    private final ExMobil mob;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    public ExPodeConcluir(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        if (mob.isGeralDeProcesso() && mob.doc().isFinalizado())
            mob = mob.doc().getUltimoVolume();
        this.mob = mob;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    @Override
    protected Expression create() {
        return And.of(
                new ExEEletronico(mob.doc()),
                Or.of(
                        new ExEMobilVia(mob),
                        new ExEMobilUltimoVolume(mob)
                ),
                Or.of(
                        And.of(
                                new ExEstaEmTramiteParalelo(mob),
                                new ExPodeMovimentar(mob, titular, lotaTitular)
                        ),
                        new ExEstaNotificado(mob, titular, lotaTitular)
                ),
                Not.of(new ExEstaSemEfeito(mob.doc())),
                Not.of(
                        Or.of(
                                new ExTemAnexosNaoAssinados(mob),
                                new ExTemDespachosNaoAssinados(mob),
                                new ExTemAnexosNaoAssinados(mob.doc().getMobilGeral()),
                                new ExTemDespachosNaoAssinados(mob.doc().getMobilGeral())
                        )
                ),
                Not.of(new ExEstaPendenteDeAssinatura(mob.doc())),
                Not.of(new ExEstaArquivado(mob)),
                Not.of(new ExEstaSobrestado(mob)),
                Not.of(new ExEstaJuntado(mob)),
                Not.of(new ExEstaEmTransito(mob, titular, lotaTitular)),
                new ExPodeMovimentarPorConfiguracao(ExTipoDeMovimentacao.CONCLUSAO, titular, lotaTitular),
                NAnd.of(
                        new ExEstaAindaComOCadastrante(mob),
                        new ExECadastrante(mob.doc(), null, lotaTitular),
                        new ExECadastrante(mob.doc(), titular, null)
                ),
                //Nato: o ideal é que existam diferentes movimentações de conclusão, para tramite paralelo e notificação,
                // da mesma forma que existem diferentes botões para tramitar em paralelo e para notificar. Talvez os
                // recebimentos devessem ser diferentes também. Assim, as lógicas seriam mais simples. Enquanto
                // isso não é feito, melhor impedir a conclusão de uma notificação para um colega da equipe, quando
                // o documento está com o titular, pois estava provocando uma conclusão do trâmite principal.
                Not.of(new ExPodeArquivarCorrente(mob, titular, lotaTitular))

//				And.of(new ExEstaPendenteDeRecebimento(mob, titular, lotaTitular),
//						new ExPodeMovimentarPorConfiguracao(ExTipoDeMovimentacao.RECEBIMENTO, titular, lotaTitular))

        );
    }
}
