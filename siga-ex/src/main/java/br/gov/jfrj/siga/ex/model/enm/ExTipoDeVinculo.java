package br.gov.jfrj.siga.ex.model.enm;

import br.gov.jfrj.siga.cp.converter.IEnumWithId;

import java.util.Map;
import java.util.TreeMap;

public enum ExTipoDeVinculo implements IEnumWithId {
    RELACIONAMENTO(1, "Ver também", "Veja também"), ALTERACAO(2, "Alteracao", "Alterado por"), REVOGACAO(3, "Revogação", "Revogado por"), CANCELAMENTO(4, "Cancelamento", "Cancelado por");

    private final int id;
    private final String descr;
    private final String acao;

    ExTipoDeVinculo(int id, String descr, String acao) {
        this.id = id;
        this.descr = descr;
        this.acao = acao;
    }

    @Override
    public Integer getId() {
        return this.id;
    }

    public String getDescr() {
        return this.descr;
    }

    public String getAcao() {
        return this.acao;
    }

    public static Map<String, String> toMap() {
        final Map<String, String> map = new TreeMap<>();
        for (ExTipoDeVinculo i : values())
            map.put(i.name(), i.descr);
        return map;
    }

}
