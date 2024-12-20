package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExDocumento;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEstaAutenticadoComTokenOuSenha implements Expression {
    ExDocumento doc;

    public ExEstaAutenticadoComTokenOuSenha(ExDocumento doc) {
        this.doc = doc;
    }

    @Override
    public boolean eval() {
        return doc.getAutenticacoesComTokenOuSenha().size() > 0;
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("está autenticado com token ou senha", result);
    }

}
