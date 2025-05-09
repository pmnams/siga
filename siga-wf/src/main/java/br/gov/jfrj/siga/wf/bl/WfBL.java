/*-*****************************************************************************
 * Copyright (c) 2006 - 2011 SJRJ.
 *
 *     This file is part of SIGA.
 *
 *     SIGA is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     SIGA is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with SIGA.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package br.gov.jfrj.siga.wf.bl;

import br.gov.jfrj.siga.Service;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.base.SigaCalendar;
import br.gov.jfrj.siga.cp.CpIdentidade;
import br.gov.jfrj.siga.cp.bl.CpBL;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.service.ExService;
import br.gov.jfrj.siga.wf.dao.WfDao;
import br.gov.jfrj.siga.wf.logic.*;
import br.gov.jfrj.siga.wf.model.*;
import br.gov.jfrj.siga.wf.model.enm.*;
import br.gov.jfrj.siga.wf.util.*;
import com.crivano.jflow.model.enm.VariableEditingKind;
import com.crivano.jflow.model.util.MissingParameterException;
import com.crivano.jlogic.Expression;
import org.apache.commons.beanutils.PropertyUtils;

import java.util.*;

/**
 * Classe que representa a lógica do negócio do sistema de workflow.
 *
 * @author kpf
 */
public class WfBL extends CpBL {
    public static final String WF_CADASTRANTE = "wf_cadastrante";
    public static final String WF_LOTA_CADASTRANTE = "wf_lota_cadastrante";
    public static final String WF_TITULAR = "wf_titular";
    public static final String WF_LOTA_TITULAR = "wf_lota_titular";
    private static WfTarefaComparator tic = new WfTarefaComparator();

    /**
     * Cria uma instância de processo. Ao final da criação, define as seguintes
     * variáveis na instância do processo: WF_CADASTRANTE - Pessoa responsável (que
     * responde) pelas ações realizadas no sistema no momento da criação da
     * instância do processo WF_LOTA_CADASTRANTE - Lotação da pessoa que está
     * operando o sistema no momento da criação da instância do processo WF_TITULAR
     * - Pessoa responsável (que responde) pelas ações realizadas no sistema no
     * momento da criação da instância do processo WF_LOTA_TITULAR - Lotação da
     * pessoa responsável (que responde) pelas ações realizadas no sistema no
     * momento da criação da instância do processo
     * <p>
     * Essas variáveis são do banco de dados corporativo.
     */
    public WfProcedimento criarProcedimento(long pdId, Integer idxPrimeiraTarefa, DpPessoa titular,
                                            DpLotacao lotaTitular, CpIdentidade identidade, WfTipoDePrincipal tipoDePrincipal, String principal,
                                            List<String> keys, List<Object> values, boolean fCreateStartTask) throws Exception {

        // Create the process definition,
        WfDefinicaoDeProcedimento pd = WfDao.getInstance().consultar(pdId, WfDefinicaoDeProcedimento.class, false);

        // Create the process instance without responsible support
        HashMap<String, Object> variable = new HashMap<>();
//		variable.put(WF_CADASTRANTE, cadastrante.getSigla());
//		variable.put(WF_LOTA_CADASTRANTE, lotaCadastrante.getSiglaCompleta());
//		variable.put(WF_TITULAR, titular.getSigla());
//		variable.put(WF_LOTA_TITULAR, lotaTitular.getSiglaCompleta());

        if (keys != null && values != null) {
            for (int n = 0; n < keys.size(); n++) {
                variable.put(keys.get(n), values.get(n));
            }
        }

        if (variable.containsKey("doc_document"))
            principal = (String) variable.get("doc_document");

        if (tipoDePrincipal == null)
            tipoDePrincipal = WfTipoDePrincipal.NENHUM;

        WfProcedimento pi = new WfProcedimento(pd, variable);

        // Impedir a inicialização de procedimento sem informar o principal, quando este
        // é obrigatório
        if (principal == null && tipoDePrincipal != WfTipoDePrincipal.NENHUM
                && (pd.getTipoDeVinculoComPrincipal() == WfTipoDeVinculoComPrincipal.OBRIGATORIO
                || pd.getTipoDeVinculoComPrincipal() == WfTipoDeVinculoComPrincipal.OBRIGATORIO_E_EXCLUSIVO))
            throwErroDeInicializacao(pi, null, "não é permitido instanciar este procedimento sem informar o principal");

        // Impedir que o mesmo documento seja referenciado por 2 procedimentos
        // diferentes ativos, quando o vínculo é exclusivo
        if (principal != null && (pd.getTipoDeVinculoComPrincipal() == WfTipoDeVinculoComPrincipal.OPCIONAL_E_EXCLUSIVO
                || pd.getTipoDeVinculoComPrincipal() == WfTipoDeVinculoComPrincipal.OBRIGATORIO_E_EXCLUSIVO)) {
            List<WfProcedimento> l = dao().consultarProcedimentosAtivosPorPrincipal(principal);
            if (l.size() > 0)
                throwErroDeInicializacao(pi, null,
                        "não é permitido instanciar este procedimento com o principal " + principal
                                + " que já está sendo orquestrado por pelo procedimento ativo " + l.get(0).getSigla());
        }

        pi.setTipoDePrincipal(tipoDePrincipal);
        pi.setPrincipal(principal);
        pi.setTitular(titular);
        pi.setLotaTitular(lotaTitular);
        pi.setHisIdcIni(identidade);
        pi.setOrgaoUsuario(titular.getOrgaoUsuario());
        pi.setHisDtIni(dao().consultarDataEHoraDoServidor());

        for (WfDefinicaoDeTarefa td : pi.getDefinicaoDeProcedimento().getDefinicaoDeTarefa()) {
            if (td.getTipoDeTarefa() == null)
                throwErroDeInicializacao(pi, td, "não foi possível identificar o tipo da tarefa");
            if (td.getTipoDeTarefa().isExigirResponsavel()) {
                WfResp r = pi.calcResponsible(td);
                if (r == null)
                    throwErroDeInicializacao(pi, td, "não foi possível calcular o responsável pela tarefa");
            }
            if (td.getTipoDeTarefa() == WfTipoDeTarefa.INCLUIR_DOCUMENTO
                    || td.getTipoDeTarefa() == WfTipoDeTarefa.CRIAR_DOCUMENTO
                    || td.getTipoDeTarefa() == WfTipoDeTarefa.AUTUAR_DOCUMENTO) {
                if (td.getRefId() == null)
                    throwErroDeInicializacao(pi, td, "não foi definido o modelo de documento na tarefa");
            }

            if (td.getTipoDeTarefa() == WfTipoDeTarefa.INCLUIR_DOCUMENTO
                    || td.getTipoDeTarefa() == WfTipoDeTarefa.AUTUAR_DOCUMENTO) {
                if (pi.getPrincipal() == null)
                    throwErroDeInicializacao(pi, td,
                            "não foi definido o principal para a inclusão de documento na tarefa");
                if (pi.getTipoDePrincipal() != WfTipoDePrincipal.DOCUMENTO)
                    throwErroDeInicializacao(pi, td,
                            "o principal não é um documento para a inclusão de documento na tarefa");
            }
        }

        if (pi.getPrincipal() != null && pi.getTipoDePrincipal() == WfTipoDePrincipal.DOCUMENTO) {
            dao().gravar(pi); // Precisa gravar para gerar uma sigla válida para o procedimento
            Service.getExService().atualizarPrincipal(pi.getPrincipal(), "PROCEDIMENTO", pi.getSigla());
        }

        WfEngine engine = new WfEngine(dao(), new WfHandler(titular, lotaTitular, identidade));

        // Start the process instance
        if (idxPrimeiraTarefa == null)
            engine.start(pi);
        else {
            pi.start();
            engine.execute(pi, pi.getCurrentIndex(), idxPrimeiraTarefa);
        }

        return pi;
    }

    private String throwErroDeInicializacao(WfProcedimento pi, WfDefinicaoDeTarefa td, String mensagem) {
        throw new AplicacaoException("Erro na inicialização de um procedimento de workflow do diagrama '"
                + pi.getDefinicaoDeProcedimento().getSigla() + "', " + mensagem
                + (td != null && td.getTitle() != null ? " '" + td.getTitle() + "'" : ""));
    }

    public void prosseguir(String event, Integer detourIndex, Map<String, Object> param, DpPessoa titular,
                           DpLotacao lotaTitular, CpIdentidade identidade) throws Exception {
        WfEngine engine = new WfEngine(dao(), new WfHandler(titular, lotaTitular, identidade));
        engine.resume(event, detourIndex, param);
    }

    public void salvar(WfProcedimento pi, WfDefinicaoDeTarefa td, Map<String, Object> param, DpPessoa titular,
                       DpLotacao lotaTitular, CpIdentidade identidade) throws Exception {
        if (td.getVariable() != null && td.getVariable().size() > 0) {
            for (WfDefinicaoDeVariavel v : (List<WfDefinicaoDeVariavel>) td.getVariable()) {
                if (v.getEditingKind() == VariableEditingKind.READ_ONLY)
                    continue;
                Object value = param != null ? param.get(v.getIdentifier()) : null;
                if (v.getEditingKind() == VariableEditingKind.READ_WRITE_REQUIRED && value == null)
                    throw new MissingParameterException(v.getIdentifier());
                pi.getVariable().put(v.getIdentifier(), value);
            }
        }
        WfDao.getInstance().gravarInstanciaDeProcedimento(pi);
    }

    public static WfProcedimento converterVariaveisEProsseguir(WfProcedimento pi, Map<String, String> paramsAsStrings,
                                                               Integer indiceDoDesvio, DpPessoa titular, DpLotacao lotaTitular, CpIdentidade idc) throws Exception {
        WfDefinicaoDeTarefa td = pi.getCurrentTaskDefinition();

        // TODO Pegar automaticamente

        // WfBL.assertPodeTransferirDocumentosVinculados(new WfTarefa(pi), cadastrante);

        Map<String, Object> paramsAsObjects = new HashMap<>();

        if (td.getVariable() != null) {
            // Associa cada variavel com seu valore especifico
            for (WfDefinicaoDeVariavel variable : td.getVariable()) {
                String identificador = variable.getIdentifier();
                if (!paramsAsStrings.containsKey(identificador))
                    continue;
                if (variable.getEditingKind() != VariableEditingKind.READ_WRITE
                        && variable.getEditingKind() != VariableEditingKind.READ_WRITE_REQUIRED)
                    continue;

                String campo = paramsAsStrings.get(identificador);
                Object value = campo;

                if (variable.getTipo() == WfTipoDeVariavel.DATE)
                    value = SigaCalendar.converteStringEmData(campo.toString());
                else if (variable.getTipo() == WfTipoDeVariavel.BOOLEAN)
                    value = converterParaBoolean(campo);
                else if (variable.getTipo() == WfTipoDeVariavel.DOUBLE)
                    value = converterParaDouble(campo);
                else if (variable.getTipo() == WfTipoDeVariavel.SELECAO)
                    value = campo;

                // TODO: Verifica se as variáveis "required" foram preenchidas
                if (variable.isRequired()
                        && (value == null || (value instanceof String && (((String) value).trim().length() == 0)))) {
                    throw new AplicacaoException("O campo " + variable.getTitle() + " deve ser preenchido");
                }

                paramsAsObjects.put(identificador, value);
            }
        }

        Integer desvio = null;
        if (indiceDoDesvio != null && td.getDetour() != null && td.getDetour().size() > indiceDoDesvio) {
            desvio = indiceDoDesvio;
        }

        Wf.getInstance().getBL().prosseguir(pi.getIdEvent(), desvio, paramsAsObjects, titular, lotaTitular, idc);
        return pi;
    }

    private static Boolean converterParaBoolean(String campo) {
        if (campo == null)
            return null;
        String s = campo.trim();
        if (s.length() == 0)
            return null;
        return "1".equals(s) || "true".equals(s);
    }

    private static Double converterParaDouble(String campo) {
        if (campo == null)
            return null;
        String s = campo.trim();
        if (s.length() == 0)
            return null;
        if (s.contains("."))
            s = s.replace(".", "");
        if (s.contains(","))
            s = s.replace(",", ".");
        return Double.parseDouble(s);
    }

    /**
     * Retorna o conjunto de tarefas que estão na responsabilidade do usuário.
     *
     * @throws AplicacaoException
     */
    public SortedSet<WfTarefa> getTaskList(DpPessoa cadastrante, DpPessoa titular, DpLotacao lotaTitular)
            throws AplicacaoException {
        SortedSet<WfTarefa> tasks = WfDao.getInstance().consultarTarefasDeLotacao(lotaTitular);
        return tasks;
    }

    public List<WfProcedimento> getTaskList(String siglaDoc) {
        List<WfProcedimento> pis = WfDao.getInstance().consultarProcedimentosAtivosPorEvento(siglaDoc);
        List<WfProcedimento> pis2 = WfDao.getInstance().consultarProcedimentosAtivosPorPrincipal(siglaDoc);
        for (WfProcedimento pi : pis2)
            if (!pis.contains(pi))
                pis.add(pi);
        Collections.sort(pis);
        return pis;
    }

    public static void transferirDocumentosVinculados(WfProcedimento pi, String siglaTitular) throws Exception {
        String principal = pi.getPrincipal();
        WfTipoDePrincipal tipo = pi.getTipoDePrincipal();
        if (principal == null || tipo == null)
            return;

        if (tipo != WfTipoDePrincipal.DOCUMENTO)
            return;

        if (pi.getResponsible() == null)
            return;

        String destino = pi.getResponsible().getCodigo();

        ExService service = Service.getExService();
        if (service.podeTransferir(principal, siglaTitular, true))
            service.transferir(principal, destino, siglaTitular, true);
    }

    public static void assertPodeTransferirDocumentosVinculados(WfTarefa ti, String siglaTitular) throws Exception {
        String principal = ti.getInstanciaDeProcedimento().getPrincipal();
        WfTipoDePrincipal tipo = ti.getInstanciaDeProcedimento().getTipoDePrincipal();
        if (principal == null || tipo == null)
            return;

        if (tipo != WfTipoDePrincipal.DOCUMENTO)
            return;

        ExService service = Service.getExService();
        if (!service.podeTransferir(principal, siglaTitular, true)) {
            throw new AplicacaoException("A tarefa não pode prosseguir porque o documento '" + principal
                    + "' não pode ser transferido. Por favor, verifique se o documento está em sua lotação e se está 'Aguardando andamento'.");
        }
    }

    public static boolean assertLotacaoAscendenteOuDescendente(DpLotacao lotAtual, DpLotacao lotFutura)
            throws AplicacaoException {
        if (lotAtual.getIdInicial().equals(lotFutura.getIdInicial()))
            return true;

        // Linha ascendente
        DpLotacao lot = lotAtual;
        while (lot.getLotacaoPai() != null) {
            lot = lot.getLotacaoPai();
            if (lot.getIdInicial().equals(lotFutura.getIdInicial()))
                return true;
        }

        // Descendente direta
        lot = lotFutura;
        while (lot.getLotacaoPai() != null) {
            lot = lot.getLotacaoPai();
            if (lot.getIdInicial().equals(lotAtual.getIdInicial()))
                return true;
        }

        throw new AplicacaoException("A designação de '" + lotAtual.getSigla() + "' para '" + lotFutura.getSigla()
                + "' não é permitida pois só são aceitas lotações ascendentes seguindo a linha do organograma ou descendentes diretas.");
    }

    public static Boolean podePegarTarefa(DpPessoa cadastrante, DpPessoa titular, DpLotacao lotaCadastrante,
                                          DpLotacao lotaTitular, WfTarefa ti) {
        return false;
    }

    private WfDao dao() {
        return (WfDao) getComp().getConfiguracaoBL().dao();
    }

    public void encerrarProcessInstance(Long id, Date consultarDataEHoraDoServidor) {
        // TODO Auto-generated method stub
    }

    public WfDefinicaoDeProcedimento getCopia(WfDefinicaoDeProcedimento original) {
        WfDefinicaoDeProcedimento copia = new WfDefinicaoDeProcedimento();
        try {

            PropertyUtils.copyProperties(copia, original);

            // novo id
            copia.setId(null);
            copia.setHisDtFim(null);
            copia.setHisDtIni(null);
            copia.updateAtivo();

        } catch (Exception e) {
            throw new AplicacaoException("Erro ao copiar as propriedades anteriores.");
        }

        return copia;
    }

    public void gravar(WfDefinicaoDeProcedimento novo, WfDefinicaoDeProcedimento antigo, Date dt,
                       CpIdentidade identidadeCadastrante) throws AplicacaoException {
        if (novo.getNome() == null || novo.getNome().trim().length() == 0)
            throw new AplicacaoException("não é possível salvar sem informar o nome.");
        dao().gravarComHistorico(novo, antigo, dt, identidadeCadastrante);
    }

    private void gravarMovimentacao(final WfMov mov) throws AplicacaoException {
        dao().gravar(mov);
        WfProcedimento pi = mov.getProcedimento();
        if (pi.getMovimentacoes() == null)
            pi.setMovimentacoes(new TreeSet<WfMov>());
        pi.getMovimentacoes().add(mov);
    }

    private void atualizarResponsavel(WfProcedimento pi) {
        WfResp atual = pi.localizarResponsavelAtual(pi.getCurrentTaskDefinition());
        if (atual == null)
            return;
        if (!atual.equals(pi.getResponsible())) {
            pi.setEventoPessoa(atual.getPessoa());
            pi.setEventoLotacao(atual.getLotacao());
            dao().gravarInstanciaDeProcedimento(pi);
        }
    }

    public void anotar(WfProcedimento pi, String descrMov, DpPessoa titular, DpLotacao lotaTitular,
                       CpIdentidade identidade) {
        try {

            WfMovAnotacao mov = new WfMovAnotacao(pi, descrMov, dao().consultarDataEHoraDoServidor(), titular,
                    lotaTitular, identidade);
            gravarMovimentacao(mov);
        } catch (final Exception e) {
            throw new AplicacaoException("Erro ao fazer anotação.", 0, e);
        }
    }

    public void excluirAnotacao(WfProcedimento pi, WfMovAnotacao mov) {
        pi.getMovimentacoes().remove(mov);
        mov.delete();
    }

    public void registrarTransicao(WfProcedimento pi, Integer de, Integer para, DpPessoa titular, DpLotacao lotaTitular,
                                   CpIdentidade identidade) {
        WfMovTransicao mov = new WfMovTransicao(pi, dao().consultarDataEHoraDoServidor(), titular, lotaTitular,
                identidade, de, para);

        // Não registraremos a transição quando a movimentação anterior for um
        // redirecionamento com a mesma origem e destino
        WfMov last = pi.getUltimaMovimentacao();
        if (last != null && last instanceof WfMovRedirecionamento) {
            WfMovRedirecionamento lastr = (WfMovRedirecionamento) last;
            if (lastr.getDefinicaoDeTarefaDe() != null && lastr.getDefinicaoDeTarefaPara() != null
                    && lastr.getDefinicaoDeTarefaDe().equals(mov.getDefinicaoDeTarefaDe())
                    && lastr.getDefinicaoDeTarefaPara().equals(mov.getDefinicaoDeTarefaPara()))
                return;
        }

        gravarMovimentacao(mov);
    }

    public void pegar(WfProcedimento pi, DpPessoa titular, DpLotacao lotaTitular, CpIdentidade identidade) {
        assertLogic(new WfPodePegar(pi, titular, lotaTitular), "pegar");
        WfMovDesignacao mov = new WfMovDesignacao(pi, dao().consultarDataEHoraDoServidor(), titular, lotaTitular,
                identidade, pi.getEventoPessoa(), pi.getEventoLotacao(), titular, lotaTitular);
        gravarMovimentacao(mov);

        atualizarResponsavel(pi);
    }

    public void redirecionar(WfProcedimento pi, int para, DpPessoa titular, DpLotacao lotaTitular,
                             CpIdentidade identidade) throws Exception {
        assertLogic(new WfPodeRedirecionar(pi, titular, lotaTitular), "redirecionar");
        WfMovRedirecionamento mov = new WfMovRedirecionamento(pi, dao().consultarDataEHoraDoServidor(), titular,
                lotaTitular, identidade, pi.getCurrentIndex(), para);
        gravarMovimentacao(mov);

        WfEngine engine = new WfEngine(dao(), new WfHandler(titular, lotaTitular, identidade));
        engine.execute(pi, pi.getCurrentIndex(), para);
    }

    public void terminar(WfProcedimento pi, DpPessoa titular, DpLotacao lotaTitular, CpIdentidade identidade)
            throws Exception {
        assertLogic(new WfPodeTerminar(pi, titular, lotaTitular), "terminar");
        WfMovTermino mov = new WfMovTermino(pi, dao().consultarDataEHoraDoServidor(), titular, lotaTitular, identidade,
                pi.getCurrentIndex());
        gravarMovimentacao(mov);
        pi.end();
        dao().gravar(pi);
        new WfHandler(titular, lotaTitular, identidade).signalToOtherProcessInstances(pi);
    }

    public void retomar(WfProcedimento pi, DpPessoa titular, DpLotacao lotaTitular, CpIdentidade identidade)
            throws Exception {
        assertLogic(new WfPodeRetomar(pi, titular, lotaTitular), "retomar");
        WfEngine engine = new WfEngine(dao(), new WfHandler(titular, lotaTitular, identidade));
        engine.resume(pi);
    }

    public void priorizar(WfProcedimento pi, WfPrioridade prioridade, DpPessoa titular, DpLotacao lotaTitular,
                          CpIdentidade identidade) {
        assertLogic(new WfPodePriorizar(pi, titular, lotaTitular), "priorizar");
        if (pi.getPrioridade() == prioridade)
            return;
        WfMovPriorizacao mov = new WfMovPriorizacao(pi, dao().consultarDataEHoraDoServidor(), titular, lotaTitular,
                identidade, pi.getPrioridade(), prioridade);
        gravarMovimentacao(mov);
        pi.setPrioridade(prioridade);
        dao().gravar(pi);
    }

    private static void assertLogic(Expression expr, String descr) {
        if (!expr.eval())
            throw new AplicacaoException("Não pode " + descr + " porque " + expr.explain(false));
    }

}
