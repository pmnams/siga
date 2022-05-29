package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExDocumento;
import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;

public class ExEMobilAutuado implements Expression {

    private final ExDocumento docPai;
    private final ExMobil mob;

    public ExEMobilAutuado(ExDocumento docPai, ExMobil mob) {
        this.docPai = docPai;
        this.mob = mob;
    }

    @Override
    public boolean eval() {
        return mob != null && docPai != null && mob.equals(docPai.getExMobilAutuado());
    }

    @Override
    public String explain(boolean result) {
        return null;
    }

}
