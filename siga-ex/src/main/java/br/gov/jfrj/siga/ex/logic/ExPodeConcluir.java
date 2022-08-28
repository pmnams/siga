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
                Not.of(new ExEstaSemEfeito(mob.doc())),
                Not.of(new ExTemAnexosNaoAssinados(mob)),
                Not.of(new ExTemDespachosNaoAssinados(mob)),
                Not.of(new ExTemAnexosNaoAssinados(mob.doc().getMobilGeral())),
                Not.of(new ExTemDespachosNaoAssinados(mob.doc().getMobilGeral())),
                Not.of(new ExEstaPendenteDeAssinatura(mob.doc())),
                Not.of(new ExEstaSobrestado(mob)),
                Not.of(new ExEstaJuntado(mob)),
                Not.of(new ExEstaEmTransito(mob, titular, lotaTitular)),
                Or.of(
                        And.of(
                                Not.of(new ExEstaArquivado(mob)),
                                new ExEstaEmTramiteParalelo(mob),
                                new ExPodeMovimentar(mob, titular, lotaTitular)
                        ),
                        new ExEstaNotificado(mob, titular, lotaTitular)
                ),
                new ExPodeMovimentarPorConfiguracao(ExTipoDeMovimentacao.CONCLUSAO, titular, lotaTitular)
        );
    }
}
