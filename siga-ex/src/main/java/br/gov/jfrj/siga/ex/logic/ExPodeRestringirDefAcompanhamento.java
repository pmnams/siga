package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.dp.*;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;
import com.crivano.jlogic.CompositeExpressionSupport;
import com.crivano.jlogic.Expression;

public class ExPodeRestringirDefAcompanhamento extends CompositeExpressionSupport {

    private DpPessoa pessoa;
    private DpLotacao lotacao;
    private DpPessoa pessoaObjeto;
    private DpLotacao lotacaoObjeto;
    private DpCargo cargoObjeto;
    private DpFuncaoConfianca funcaoConfiancaObjeto;
    private CpOrgaoUsuario orgaoObjeto;

    public ExPodeRestringirDefAcompanhamento(final DpPessoa pessoa, final DpLotacao lotacao,
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
     * Retorna se é possível vincular perfil ao documento. Basta não estar eliminado
     * o documento e não haver configuração impeditiva, o que significa que, tendo
     * acesso a um documento não eliminado, qualquer usuário pode se cadastrar como
     * interessado.
     *
     */
    @Override
    protected Expression create() {
        return new ExPodePorConfiguracao(pessoa, lotacao)
                .withIdTpConf(ExTipoDeConfiguracao.RESTRINGIR_DEF_ACOMPANHAMENTO).withPessoaObjeto(pessoaObjeto)
                .withLotacaoObjeto(lotacaoObjeto).withCargoObjeto(cargoObjeto)
                .withFuncaoConfiancaObjeto(funcaoConfiancaObjeto).withOrgaoObjeto(orgaoObjeto);
    }
}
