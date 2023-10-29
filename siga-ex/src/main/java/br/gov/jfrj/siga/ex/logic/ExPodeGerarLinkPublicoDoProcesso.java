package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.cp.model.enm.CpMarcadorEnum;
import br.gov.jfrj.siga.dp.CpMarcador;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import br.gov.jfrj.siga.hibernate.ExDao;
import com.crivano.jlogic.*;

public class ExPodeGerarLinkPublicoDoProcesso extends CompositeExpressionSupport {

    private final ExMobil mob;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    public ExPodeGerarLinkPublicoDoProcesso(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        super();
        this.mob = mob;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    @Override
    protected Expression create() {
        return And.of(
                Not.of(new ExEstaPendenteDeAssinatura(mob.getDoc())),
                new ExPodeSerMovimentado(mob, titular, lotaTitular),
                Not.of(new ExTemMovimentacaoNaoCanceladaDoTipo(mob, ExTipoDeMovimentacao.GERAR_LINK_PUBLICO_PROCESSO)),
                Or.of(
                        new ExEstaMarcadoComMarcadorOuOGeral(
                                mob,
                                ExDao.getInstance().consultar(
                                        CpMarcadorEnum.COVID_19.getId(),
                                        CpMarcador.class,
                                        false
                                )
                        ),
                        new ExTemVolumeComMovimentacaoNaoCanceladaDoTipo(mob.getDoc(), ExTipoDeMovimentacao.ENVIO_SIAFEM)
                ),
                new ExPodePorConfiguracao(titular, lotaTitular)
                        .withIdTpConf(ExTipoDeConfiguracao.MOVIMENTAR)
                        .withExTpMov(ExTipoDeMovimentacao.GERAR_LINK_PUBLICO_PROCESSO)
                        .withExMod(mob.doc().getExModelo())
        );
    }
}
