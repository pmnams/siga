package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.*;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import com.crivano.jlogic.CompositeExpressionSupport;
import com.crivano.jlogic.Expression;

public class ExPodeRestringirTramitacao extends CompositeExpressionSupport {

    private final DpPessoa pessoa;
    private final DpLotacao lotacao;
    private final DpPessoa pessoaObjeto;
    private final DpLotacao lotacaoObjeto;
    private final DpCargo cargoObjeto;
    private final DpFuncaoConfianca funcaoConfiancaObjeto;
    private final CpOrgaoUsuario orgaoObjeto;

    public ExPodeRestringirTramitacao(final DpPessoa pessoa, final DpLotacao lotacao,
                                      final DpPessoa pessoaObjeto, final DpLotacao lotacaoObjeto, final DpCargo cargoObjeto,
                                      final DpFuncaoConfianca funcaoConfiancaObjeto, final CpOrgaoUsuario orgaoObjeto) {
        this.pessoa = pessoa;
        this.lotacao = lotacao;
        this.pessoaObjeto = pessoaObjeto;
        this.lotacaoObjeto = lotacaoObjeto;
        this.cargoObjeto = cargoObjeto;
        this.funcaoConfiancaObjeto = funcaoConfiancaObjeto;
        this.orgaoObjeto = orgaoObjeto;
    }

    /**
     * Retorna se é possível tramitar o documento. Basta não estar eliminado
     * o documento e não haver configuração impeditiva, o que significa que, tendo
     * acesso a um documento não eliminado, qualquer usuário pode se cadastrar como
     * cossignatario ou subscritor.
     */
    @Override
    protected Expression create() {
        return new ExPodePorConfiguracao(pessoa, lotacao)
                .withIdTpConf(ExTipoDeConfiguracao.RESTRINGIR_TRAMITACAO).withPessoaObjeto(pessoaObjeto)
                .withLotacaoObjeto(lotacaoObjeto).withCargoObjeto(cargoObjeto)
                .withFuncaoConfiancaObjeto(funcaoConfiancaObjeto).withOrgaoObjeto(orgaoObjeto);
    }
}
