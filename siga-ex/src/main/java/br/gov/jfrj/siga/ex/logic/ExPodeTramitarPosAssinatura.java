package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.cp.logic.CpENulo;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import com.crivano.jlogic.*;

public class ExPodeTramitarPosAssinatura extends CompositeExpressionSupport {

    private final ExMobil mob;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;
    private final DpPessoa destinatario;
    private final DpLotacao lotaDestinatario;

    public ExPodeTramitarPosAssinatura(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular, DpPessoa destinatario,
                                       DpLotacao lotaDestinatario) {
        this.mob = mob;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
        this.destinatario = destinatario;
        this.lotaDestinatario = lotaDestinatario;
    }

    /**
     * Retorna se é possível fazer transferência imediatamente antes da tela de
     * assinatura. As regras são as seguintes para este móbil:
     * <ul>
     * <li><i>Destinatario esta definido</i>
     * <li><i>Destinatario pode receber documento</i>
     * <li><i>Se temporário, o documento está na mesma lotação do titular</i>
     * <li><i>Se finalizado, podeMovimentar()</i>
     * <li>Não pode haver configuração impeditiva</li>
     * </ul>
     *
     */
    @Override
    protected Expression create() {

        return And.of(
                Not.of(new ExEstaOrquestradoPeloWF(mob.doc())),
                NAnd.of(
                        new CpENulo(destinatario, "destinatário"),
                        new CpENulo(lotaDestinatario, "lotação destinatária")
                ),
                new ExPodeReceberPorConfiguracao(mob, destinatario, lotaDestinatario),
                new ExPodePorConfiguracao(titular, lotaTitular)
                        .withIdTpConf(ExTipoDeConfiguracao.MOVIMENTAR)
                        .withExTpMov(ExTipoDeMovimentacao.TRANSFERENCIA),
                If.of(
                        new ExEstaFinalizado(mob.doc()),
                        new ExPodeMovimentar(mob, titular, lotaTitular),
                        Or.of(
                                new ExECadastrante(mob.doc(), titular, lotaTitular),
                                new ExESubscritor(mob.doc(), titular, lotaTitular)
                        )
                ),
                Or.of(
                        Not.of(new ExTemMobilPai(mob.doc())),
                        new ExEstaResponsavel(mob.doc().getExMobilPai(), titular, lotaTitular)
                )
        );
    }
}
