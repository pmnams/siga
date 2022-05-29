package br.gov.jfrj.siga.cp.logic;

import br.gov.jfrj.siga.base.Prop;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class CpEGovSP implements Expression {

    @Override
    public boolean eval() {
        return Prop.isGovSP();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.NOT + " é GovSP";
    }

}
