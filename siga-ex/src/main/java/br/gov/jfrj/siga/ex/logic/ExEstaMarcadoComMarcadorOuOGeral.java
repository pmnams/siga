package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.CpMarcador;
import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.CompositeExpressionSupport;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.Or;

public class ExEstaMarcadoComMarcadorOuOGeral extends CompositeExpressionSupport {

    private final CpMarcador marcador;
    private final ExMobil mob;

    public ExEstaMarcadoComMarcadorOuOGeral(ExMobil mob, CpMarcador marcador) {
        this.mob = mob;
        this.marcador = marcador;
    }

    @Override
    protected Expression create() {
        return Or.of(new ExEstaMarcadoComMarcador(mob, marcador),
                new ExEstaMarcadoComMarcador(mob.doc().getMobilGeral(), marcador));
    }

}
