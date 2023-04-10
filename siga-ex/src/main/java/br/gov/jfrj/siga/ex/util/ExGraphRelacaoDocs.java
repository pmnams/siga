package br.gov.jfrj.siga.ex.util;

import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExDocumento;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.model.enm.ExTipoDeVinculo;

import java.util.*;

public class ExGraphRelacaoDocs extends ExGraph {

    private class NodoMob extends Nodo {

        private ExMobil mob;

        public ExMobil getMob() {
            return this.mob;
        }

        public NodoMob(ExMobil mob, DpPessoa pessoaVisualizando) {
            this(mob, pessoaVisualizando, null);
        }

        public NodoMob(ExMobil mob, DpPessoa pessoaVisualizando,
                       ExDocumento docRef) {
            super(mob.getSigla());
            this.mob = mob;
            setShape("rectangle");
            setLabel(mob.getSiglaResumida(pessoaVisualizando.getOrgaoUsuario(),
                    docRef));
            setTooltip(mob.getSigla());
            setURL("exibir?sigla=" + mob.getSigla() + "");
        }
    }

    private class TransicaoMob extends Transicao {

        private ExMobil mob1;
        private ExMobil mob2;
        private String tipo;

        public ExMobil getMob1() {
            return this.mob1;
        }

        public ExMobil getMob2() {
            return this.mob2;
        }

        public TransicaoMob(ExMobil mob1, ExMobil mob2, String tipo) {
            super(mob1.getSigla(), mob2.getSigla());
            this.mob1 = mob1;
            this.mob2 = mob2;
            this.tipo = tipo;
            switch (tipo) {
                case "vinculacao":
                    setTooltip("Vincula&ccedil;&atilde;o");
                    setEstilo(ESTILO_TRACEJADO);
                    setCor("gray");
                    setDirected(false);
                    break;
                case "alteracao":
                    setTooltip("Alteração");
                    setEstilo(ESTILO_TRACEJADO);
                    setCor("blue");
                    setLabel("Altera");
                    setDirected(true);
                    setAoContrario(true);
                    break;
                case "revogacao":
                    setTooltip("Revogação");
                    setEstilo(ESTILO_TRACEJADO);
                    setCor("orange");
                    setLabel("Revoga");
                    setDirected(true);
                    setAoContrario(true);
                    break;
                case "cancelamento":
                    setTooltip("Cancelamento");
                    setEstilo(ESTILO_TRACEJADO);
                    setCor("red");
                    setLabel("Cancela");
                    setDirected(true);
                    setAoContrario(true);
                    break;
                case "juntada":
                    setTooltip("Juntada");
                    setEstilo(ESTILO_TRACEJADO);
                    setDirected(true).setAoContrario(true);
                    break;
                case "apensacao":
                    setTooltip("Apensa&ccedil;&atilde;o");
                    setEstilo(ESTILO_TRACEJADO);
                    setDirected(true).setAoContrario(true);
                    break;
                case "paternidade":
                    setTooltip(mob2.doc().isProcesso() ? "Subprocesso"
                            : "Documento filho");
                    setDirected(false);
                    break;
            }
        }
    }

    private final ExMobil mobBase;

    public List<NodoMob> getNodosExcetoMobBase() {
        List<NodoMob> listaFinal = new ArrayList<NodoMob>();
        for (Nodo nodo : getNodos()) {
            NodoMob nodoDoc = (NodoMob) nodo;
            if (!nodoDoc.getMob().equals(mobBase))
                listaFinal.add(nodoDoc);
        }
        return listaFinal;
    }

    public ExGraphRelacaoDocs(ExMobil mobBase, DpPessoa pessVendo) {

        this.mobBase = mobBase;

        adicionar(new NodoMob(mobBase, pessVendo).setDestacar(true));

        // Apensações
        for (ExMobil apenso : mobBase.getApensos(false, true)) {
            adicionar(new NodoMob(apenso, pessVendo, mobBase.doc()));
            adicionar(new TransicaoMob(mobBase, apenso, "apensacao"));
        }
        ExMobil mestre = mobBase.getMestre();
        if (mestre != null && !mestre.doc().equals(mobBase)) {
            adicionar(new NodoMob(mestre, pessVendo, mobBase.doc()));
            adicionar(new TransicaoMob(mestre, mobBase, "apensacao"));
        }

        // Vinculações
        for (ExMobil vinculado : mobBase.getVinculados(ExTipoDeVinculo.RELACIONAMENTO)) {
            adicionar(new NodoMob(vinculado, pessVendo, mobBase.doc()));
            adicionar(new TransicaoMob(mobBase, vinculado, "vinculacao"));
        }

        // Alterações
        for (ExMobil vinculado : mobBase.getVinculados(ExTipoDeVinculo.ALTERACAO)) {
            adicionar(new NodoMob(vinculado, pessVendo, mobBase.doc()));
            adicionar(new TransicaoMob(mobBase, vinculado, "alteracao"));
        }

        // Cancelamentos
        for (ExMobil vinculado : mobBase.getVinculados(ExTipoDeVinculo.CANCELAMENTO)) {
            adicionar(new NodoMob(vinculado, pessVendo, mobBase.doc()));
            adicionar(new TransicaoMob(mobBase, vinculado, "cancelamento"));
        }

        // Revogações
        for (ExMobil vinculado : mobBase.getVinculados(ExTipoDeVinculo.REVOGACAO)) {
            adicionar(new NodoMob(vinculado, pessVendo, mobBase.doc()));
            adicionar(new TransicaoMob(mobBase, vinculado, "revogacao"));
        }

        // Juntadas
        ExMobil pai = mobBase.getExMobilPai();
        if (pai != null) {
            adicionar(new NodoMob(pai, pessVendo, mobBase.doc()));
            adicionar(new TransicaoMob(pai, mobBase, "juntada"));
        }

        // Enquanto o principal não estiver assinado
        boolean pendenteDeAssinatura = mobBase.doc().isPendenteDeAssinatura();
        if (pendenteDeAssinatura) {

            // Incluir os documentos filhos juntados (ou não)
            for (ExMobil m : mobBase.getJuntados()) {
                adicionar(new NodoMob(m, pessVendo, mobBase.doc()));
                adicionar(new TransicaoMob(mobBase, m, "juntada"));
            }
        }

        if (pendenteDeAssinatura || mobBase.doc().isProcesso()) {
            // Paternidades
            for (ExDocumento sub : mobBase.doc().getMobilGeral()
                    .getExDocumentoFilhoSet()) {
                if (!pendenteDeAssinatura && !sub.isProcesso())
                    continue;
                boolean jaTemNodo = false;
                for (Nodo n : getNodos())
                    if (((NodoMob) n).getMob().doc().equals(sub)) {
                        jaTemNodo = true;
                        break;
                    }
                if (!jaTemNodo) {
                    adicionar(new NodoMob(sub.getMobilGeral(), pessVendo,
                            mobBase.doc()));
                    adicionar(new TransicaoMob(mobBase, sub.getMobilGeral(),
                            "paternidade"));
                }
            }
        }

        if (mobBase.doc().getExMobilPai() != null) {
            boolean jaTemNodo = false;
            for (Nodo n : getNodos())
                if (((NodoMob) n).getMob().doc()
                        .equals(mobBase.doc().getExMobilPai().doc())) {
                    jaTemNodo = true;
                    break;
                }
            if (!jaTemNodo) {
                adicionar(new NodoMob(mobBase.doc().getExMobilPai(), pessVendo,
                        mobBase.doc()));
                adicionar(new TransicaoMob(mobBase.doc().getExMobilPai(),
                        mobBase, "paternidade"));
            }
        }
    }

    public Map<String, List<ExMobil>> getAsMap() {
        Map<String, List<ExMobil>> mapa = new TreeMap<>();
        String cat = "";
        ExMobil mobilAAdicionar = null;
        for (Transicao t : getTransicoes()) {
            TransicaoMob tMob = (TransicaoMob) t;
            switch (tMob.tipo) {
                case "vinculacao":
                    cat = "Veja também";
                    mobilAAdicionar = tMob.mob2;
                    break;
                case "alteracao":
                    cat = "Alterado por";
                    mobilAAdicionar = tMob.mob2;
                    break;
                case "revogacao":
                    cat = "Revogado por";
                    mobilAAdicionar = tMob.mob2;
                    break;
                case "cancelamento":
                    cat = "Cancelado por";
                    mobilAAdicionar = tMob.mob2;
                    break;
                case "juntada":
                    cat = "Juntado ao documento";
                    mobilAAdicionar = tMob.mob1;
                    break;
                case "apensacao":
                    if (tMob.mob1.equals(mobBase)) {
                        cat = "Documentos Apensados";
                        mobilAAdicionar = tMob.mob2;
                    } else {
                        cat = "Apensado ao Documento";
                        mobilAAdicionar = tMob.mob1;
                    }
                    break;
                case "paternidade":
                    if (tMob.mob1.equals(mobBase)) {
                        cat = "Subprocessos";
                        mobilAAdicionar = tMob.mob2;
                    } else {
                        cat = "Documento pai";
                        mobilAAdicionar = tMob.mob1;
                    }
                    break;
            }

            mapa.computeIfAbsent(cat, k -> new ArrayList<>()).add(mobilAAdicionar);
        }
        return mapa;
    }

    public Map<String, List<ExMobil>> getPrincipaisAsMap() {
        Map<String, List<ExMobil>> mapa = getAsMap();
        Set<String> toRemove = new HashSet<>();
        for (String key : mapa.keySet())
            if (!isPrincipal(key))
                toRemove.add(key);
        toRemove.forEach(mapa::remove);
        return mapa;
    }

    public Map<String, List<ExMobil>> getSecundariosAsMap() {
        Map<String, List<ExMobil>> mapa = getAsMap();
        Set<String> toRemove = new HashSet<>();
        for (String key : mapa.keySet())
            if (isPrincipal(key))
                toRemove.add(key);
        toRemove.forEach(mapa::remove);
        return mapa;
    }

    private boolean isPrincipal(String key) {
        return key.equals("Alterado por")
                || key.equals("Revogado por")
                || key.equals("Cancelado por");
    }

}
