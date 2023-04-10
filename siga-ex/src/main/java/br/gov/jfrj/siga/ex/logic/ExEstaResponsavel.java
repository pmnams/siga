package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEstaResponsavel implements Expression {

    private ExMobil mob;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;

    public ExEstaResponsavel(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        this.mob = mob;

        if (this.mob != null && this.mob.isGeral()) {
            if (this.mob.doc().isProcesso())
                this.mob = this.mob.doc().getUltimoVolume();
            else {
                for (ExMobil m : this.mob.doc().getExMobilSet()) {
                    if (!m.isGeral() && m.isAtendente(titular, lotaTitular)) {
                        this.mob = m;
                        break;
                    }
                }
            }
        }

        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    @Override
    public boolean eval() {
        return mob.isAtendente(titular, lotaTitular);
    }

    @Override
    public String explain(boolean result) {
        return (titular != null ? titular.getSiglaCompleta() + "/" : "") + lotaTitular.getSiglaCompleta() + (result ? "" : JLogic.NOT)
                + " é responsável por " + mob.getCodigo();
    }
}
