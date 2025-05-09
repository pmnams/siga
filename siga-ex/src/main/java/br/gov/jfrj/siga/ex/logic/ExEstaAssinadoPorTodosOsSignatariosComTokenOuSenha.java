package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.ex.ExDocumento;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

public class ExEstaAssinadoPorTodosOsSignatariosComTokenOuSenha implements Expression {
    ExDocumento doc;

    public ExEstaAssinadoPorTodosOsSignatariosComTokenOuSenha(ExDocumento doc) {
        this.doc = doc;
    }

    @Override
    public boolean eval() {
        return doc.isAssinadoPorTodosOsSignatariosComTokenOuSenha();
    }

    @Override
    public String explain(boolean result) {
        return JLogic.explain("está assinado por todos os signatários", result);
    }

}
