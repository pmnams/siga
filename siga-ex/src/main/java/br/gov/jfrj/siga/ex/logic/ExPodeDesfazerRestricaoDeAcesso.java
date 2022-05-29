package br.gov.jfrj.siga.ex.logic;

import br.gov.jfrj.siga.cp.logic.CpDiferente;
import br.gov.jfrj.siga.cp.logic.CpNaoENulo;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.ExMovimentacao;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeMovimentacao;
import com.crivano.jlogic.And;
import com.crivano.jlogic.CompositeExpressionSupport;
import com.crivano.jlogic.Expression;

import java.util.ArrayList;
import java.util.List;

public class ExPodeDesfazerRestricaoDeAcesso extends CompositeExpressionSupport {

    private final ExMobil mob;
    private final DpPessoa titular;
    private final DpLotacao lotaTitular;
    private final List<ExMovimentacao> lista;

    public ExPodeDesfazerRestricaoDeAcesso(ExMobil mob, DpPessoa titular, DpLotacao lotaTitular) {
        this.mob = mob;
        this.titular = titular;
        this.lotaTitular = lotaTitular;

        lista = new ArrayList<>();
        lista.addAll(mob.getMovsNaoCanceladas(ExTipoDeMovimentacao.RESTRINGIR_ACESSO));
    }

    @Override
    protected Expression create() {
        return And.of(

                new CpNaoENulo(lista, "restrições de acesso"),

                new CpDiferente(lista == null ? 0 : lista.size(), "restrições de acesso", 0, "zero"),

                new ExPodeRestringirAcesso(mob, titular, lotaTitular));
    }
}
