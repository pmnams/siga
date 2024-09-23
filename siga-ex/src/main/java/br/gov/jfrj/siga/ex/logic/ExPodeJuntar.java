package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.cp.logic.CpNaoENulo;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExDocumento;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.ExMovimentacao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import com.crivano.jlogic.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExPodeJuntar extends CompositeExpressionSupport {

    private final ExMobil mob;
    private ExDocumento docPai;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;
    private final ExDocumento doc;

    public ExPodeJuntar(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        this.mob = mob;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
        this.doc = mob.getDoc();
    }

    public ExPodeJuntar(ExDocumento docPai, ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        this(mob, titular, lotaTitular);
        this.docPai = docPai;
    }

    /**
     * Retorna se é possível junta este móbil a outro. Seguem as regras:
     * <ul>
     * <li>Móbil não pode estar cancelado</li>
     * <li>Volume não pode estar encerrado</li>
     * <li>Móbil não pode estar em trânsito</li>
     * <li><i>podeMovimentar()</i> tem de ser verdadeiro para o móbil/usuário</li>
     * <li>Documento tem de estar assinado</li>
     * <li>Móbil não pode estar juntado <b>(mas pode ser juntado estando
     * apensado?)</b></li>
     * <li>Móbil não pode estar em algum arquivo</li>
     * <li>Não pode haver configuração impeditiva</li>
     * </ul>
     */
    @Override
    protected Expression create() {
        Expression canMoveExp = new ExPodeMovimentar(mob, titular, lotaTitular);

        List<Expression> expressions = new ArrayList<>();
        if (Objects.nonNull(doc)) {
            for (ExMovimentacao mov : doc.getMobilGeral().getExMovimentacaoSet()) {
                if (mov.isCancelada() || mov.getExTipoMovimentacao() != ExTipoDeMovimentacao.VINCULACAO_PAPEL)
                    continue;

                if (Objects.equals(mov.getSubscritor(), titular))
                    expressions.add(
                            new ExPodePorConfiguracao(titular, lotaTitular)
                                    .withIdTpConf(ExTipoDeConfiguracao.MOVIMENTAR)
                                    .withExTpMov(ExTipoDeMovimentacao.JUNTADA)
                                    .withExMod(mob.doc().getExModelo())
                                    .withExPapel(mov.getExPapel())
                                    .withDefaultExpression(canMoveExp)
                    );
            }
        }

        Expression roleExpression;
        if (!expressions.isEmpty()) {
            roleExpression = Or.of(expressions.toArray(new Expression[0]));
        } else {
            roleExpression = canMoveExp;
        }

        return And.of(
                new ExEMobilVia(mob),
                Not.of(new ExEstaPendenteDeAnexacao(mob)),
                Not.of(new ExEMobilCancelado(mob)),
                Not.of(new ExEstaEncerrado(mob)),
                Not.of(new ExEstaEmTransito(mob, titular, lotaTitular)),
                Or.of(
                        And.of(
                                new ExTemMobilPai(mob.doc()),
                                new ExESubscritorOuCossignatario(mob.doc(), titular)
                        ),
                        And.of(
                                new CpNaoENulo(docPai, "documento onde foi autuado"),
                                new ExEMobilAutuado(docPai, mob)
                        ),
                        roleExpression
                ),
                Or.of(
                        Not.of(new ExEstaPendenteDeAssinatura(mob.doc())),
                        new ExEInternoCapturado(mob.doc())
                ),
                Not.of(new ExEstaJuntado(mob)),
                Not.of(new ExEstaApensado(mob)),
                Not.of(new ExEstaArquivado(mob)),
                Not.of(new ExEstaSobrestado(mob)),
                Not.of(new ExEstaSemEfeito(mob.doc())),
                Not.of(new ExEstaEmTramiteParalelo(mob)),
                new ExPodePorConfiguracao(titular, lotaTitular)
                        .withIdTpConf(ExTipoDeConfiguracao.MOVIMENTAR)
                        .withExTpMov(ExTipoDeMovimentacao.JUNTADA)
                        .withExMod(mob.doc().getExModelo())
        );
    }
}