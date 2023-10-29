package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.And;
import com.crivano.jlogic.CompositeExpressionSupport;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ExPodeAgendarPublicacaoNoBoletim extends CompositeExpressionSupport {

    private final ExMobil mob;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    public ExPodeAgendarPublicacaoNoBoletim(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        this.mob = mob;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    /**
     * Retorna se é possível agendar publicação no Boletim. É necessário que não
     * sejam ainda 17 horas e que <i>podeBotaoAgendarPublicacaoBoletim()</i> seja
     * verdadeiro para este móbil e usuário.
     */
    @Override
    protected Expression create() {

        return And.of(
                new Expression() {
                    @Override
                    public boolean eval() {
                        GregorianCalendar agora = new GregorianCalendar();
                        agora.setTime(new Date());
                        return agora.get(Calendar.HOUR_OF_DAY) < 17;
                    }

                    @Override
                    public String explain(boolean result) {
                        return JLogic.explain("antes das 17hs", result);
                    }

                },
                new ExPodeExibirBotaoDeAgendarPublicacaoNoBoletim(mob, titular, lotaTitular)
        );
    }
}
