package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.*;

public class ExPodeVincular extends CompositeExpressionSupport {

    private final ExMobil mob;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    public ExPodeVincular(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        this.mob = mob;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    /**
     * Retorna se é possível fazer vinculação deste mobil a outro, conforme as
     * seguintes regras para <i>este</i> móbil:
     * <ul>
     * <li>Precisa ser via ou volume (não pode ser geral)</li>
     * <li>Não pode estar em trânsito</li>
     * <li>Não pode estar juntado.</li>
     * <li><i>podeMovimentar()</i> precisa retornar verdadeiro para ele</li>
     * </ul>
     * Não é levada em conta, aqui, a situação do mobil ao qual se pertende
     * vincular.
     */
    @Override
    protected Expression create() {

        return And.of(

                Or.of(new ExEMobilVia(mob), new ExEMobilVolume(mob)),

                Not.of(new ExEstaEmTransito(mob, titular, lotaTitular)),

                new ExPodeMovimentar(mob, titular, lotaTitular),

                Not.of(new ExEstaJuntado(mob)));
    }
}
