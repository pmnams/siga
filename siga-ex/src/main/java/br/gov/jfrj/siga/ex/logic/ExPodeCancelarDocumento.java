package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExDocumento;
import com.crivano.jlogic.*;

public class ExPodeCancelarDocumento extends CompositeExpressionSupport {

    private ExDocumento doc;
    private DpPessoa titular;
    private DpLotacao lotaTitular;

    /**
     * Retorna se é possível cancelar documento pendente de assinatura, segundo as
     * regras a seguir:
     *
     *
     * <ul>
     * <li>Documento tem de estar pendente de assinatura</li>
     * <li>Móbil tem de ser via ou volume (não pode ser geral)</li>
     * <li><i>podeMovimentar()</i> tem de ser verdadeiro para o usuário / móbil</li>
     * *
     * <li>Não pode haver configuração impeditiva</li>
     * </ul>
     *
     */
    public ExPodeCancelarDocumento(ExDocumento doc, DpPessoa titular, DpLotacao lotaTitular) {
        this.doc = doc;
        this.titular = titular;
        this.lotaTitular = lotaTitular;
    }

    @Override
    protected Expression create() {
        return And.of(

                Not.of(new ExEstaCancelado(doc)),

                new ExEstaFinalizado(doc),

                Or.of(Not.of(new ExEEletronico(doc)), new ExEstaPendenteDeAssinatura(doc)),

                If.of(new ExECapturado(doc),

                        And.of(Not.of(new ExEDocFilhoJuntadoAoPai(doc)), new ExECadastrante(doc, titular)),

                        new ExESubscritor(doc, titular)));
    }
}
