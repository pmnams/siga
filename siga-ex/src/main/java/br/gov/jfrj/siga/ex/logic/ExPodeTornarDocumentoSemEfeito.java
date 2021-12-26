package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import com.crivano.jlogic.*;

public class ExPodeTornarDocumentoSemEfeito extends CompositeExpressionSupport {

    private final ExMobil mob;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    public ExPodeTornarDocumentoSemEfeito(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        if (mob.isGeralDeProcesso() && mob.doc().isFinalizado())
            mob = mob.doc().getUltimoVolume();
        this.mob = mob;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    /**
     * Retorna se é possível tornar um documento sem efeito, segundo as regras a
     * seguir:
     *
     *
     * <ul>
     * <li>Documento tem de estar assinado</li>
     * <li>Móbil tem de ser via ou volume (não pode ser geral)</li>
     * <li><i>podeMovimentar()</i> tem de ser verdadeiro para o usuário / móbil</li>
     * <li>Móbil não pode estar juntado</li>
     * <li>Móbil não pode estar em trânsito</li>
     * <li>Não pode haver configuração impeditiva</li>
     * </ul>
     *
     */
    @Override
    protected Expression create() {
        return And.of(

                Not.of(new ExEstaSemEfeito(mob.doc())),

                new ExEEletronico(mob.doc()),

                Not.of(new ExEstaPendenteDeAssinatura(mob.doc())),

                If.of(new ExECapturado(mob.doc()),

                        And.of(Not.of(new ExEDocFilhoJuntadoAoPai(mob.doc())),
                                Or.of(new ExECadastrante(mob.doc(), titular), new ExESubscritor(mob.doc(), titular)),
                                new ExPodePorConfiguracao(titular, lotaTitular)
                                        .withIdTpConf(ExTipoDeConfiguracao.MOVIMENTAR)
                                        .withExTpMov(ExTipoDeMovimentacao.TORNAR_SEM_EFEITO)
                                        .withCargo(titular.getCargo())
                                        .withDpFuncaoConfianca(titular.getFuncaoConfianca())
                                        .withExFormaDoc(mob.doc().getExFormaDocumento())
                                        .withExMod(mob.doc().getExModelo())),

                        new ExESubscritor(mob.doc(), titular)

                ),

                Not.of(new ExEstaAgendadaPublicacaoNoDiario(mob.doc())),
                Not.of(new ExEstaSolicitadaPublicacaoNoDiario(mob.doc())),
                Not.of(new ExEstaPublicadoNoBoletim(mob.doc())), Not.of(new ExEstaPublicadoNoDiario(mob.doc())),

                new ExPodeMovimentarPorConfiguracao(ExTipoDeMovimentacao.TORNAR_SEM_EFEITO, titular,
                        lotaTitular));
    }
}
