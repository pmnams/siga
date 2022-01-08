package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.CpMarcador;
import com.crivano.jlogic.CompositeExpressionSupport;
import com.crivano.jlogic.Expression;

public class ExEMarcadorUnico extends CompositeExpressionSupport {

    private final CpMarcador marcador;

    public ExEMarcadorUnico(CpMarcador marcador) {
        this.marcador = marcador;
    }

    @Override
    protected Expression create() {
        return new ExEMarcadorDeAtendente(marcador);
    }

}
