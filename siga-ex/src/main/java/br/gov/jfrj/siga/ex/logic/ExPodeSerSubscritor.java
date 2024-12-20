package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExDocumento;
import com.crivano.jlogic.CompositeExpressionSupport;
import com.crivano.jlogic.Expression;
import com.crivano.jlogic.Or;

public class ExPodeSerSubscritor extends CompositeExpressionSupport {

    private final ExDocumento doc;

    public ExPodeSerSubscritor(ExDocumento doc) {
        this.doc = doc;
    }

    public ExPodeSerSubscritor(ExDocumento doc, DpPessoa titular, DpLotacao lotaTitular) {
        this.doc = doc;
    }

    @Override
    protected Expression create() {
        return Or.of(

                new ExEExterno(doc),

                new ExEExternoCapturado(doc),

                new ExPodeSerSubscritorDeModelo(doc.getExModelo(), doc.getTitular(), doc.getLotaTitular()));
    }

}
