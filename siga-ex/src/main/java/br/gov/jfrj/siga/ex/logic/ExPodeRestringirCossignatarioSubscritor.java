package br.gov.jfrj.siga.ex.logic;

import com.crivano.jlogic.CompositeExpressionSupport;
import com.crivano.jlogic.Expression;

import br.gov.jfrj.siga.dp.CpOrgaoUsuario;
import br.gov.jfrj.siga.dp.DpCargo;
import br.gov.jfrj.siga.dp.DpFuncaoConfianca;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeConfiguracao;

public class ExPodeRestringirCossignatarioSubscritor extends CompositeExpressionSupport {

    private final DpPessoa pessoa;
    private final DpLotacao lotacao;
    private final DpPessoa pessoaObjeto;
    private final DpLotacao lotacaoObjeto;
    private final DpCargo cargoObjeto;
    private final DpFuncaoConfianca funcaoConfiancaObjeto;
    private final CpOrgaoUsuario orgaoObjeto;

    public ExPodeRestringirCossignatarioSubscritor(final DpPessoa pessoa, final DpLotacao lotacao,
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
     * Retorna se é possível incluir o cossignatario ou subscritor ao documento. Basta não estar eliminado
     * o documento e não haver configuração impeditiva, o que significa que, tendo
     * acesso a um documento não eliminado, qualquer usuário pode se cadastrar como
     * cossignatario ou subscritor.
     *
     */
    @Override
    protected Expression create() {
        return new ExPodePorConfiguracao(pessoa, lotacao)
                .withIdTpConf(ExTipoDeConfiguracao.RESTRINGIR_COSSIGNATARIO_SUBSCRITOR).withPessoaObjeto(pessoaObjeto)
                .withLotacaoObjeto(lotacaoObjeto).withCargoObjeto(cargoObjeto)
                .withFuncaoConfiancaObjeto(funcaoConfiancaObjeto).withOrgaoObjeto(orgaoObjeto);
    }
}
