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
/*
 * Criado em  13/09/2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.gov.jfrj.siga.vraptor;

import br.com.caelum.vraptor.*;
import br.com.caelum.vraptor.observer.download.Download;
import br.com.caelum.vraptor.observer.download.InputStreamDownload;
import br.com.caelum.vraptor.observer.upload.UploadedFile;
import br.com.caelum.vraptor.view.Results;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.base.GeraMessageDigest;
import br.gov.jfrj.siga.base.Prop;
import br.gov.jfrj.siga.base.SigaModal;
import br.gov.jfrj.siga.base.client.Hcaptcha;
import br.gov.jfrj.siga.base.util.CPFUtils;
import br.gov.jfrj.siga.base.util.Texto;
import br.gov.jfrj.siga.cp.CpIdentidade;
import br.gov.jfrj.siga.cp.bl.Cp;
import br.gov.jfrj.siga.cp.bl.CpBL;
import br.gov.jfrj.siga.cp.model.DpLotacaoSelecao;
import br.gov.jfrj.siga.cp.util.SigaUtil;
import br.gov.jfrj.siga.dp.*;
import br.gov.jfrj.siga.dp.dao.*;
import br.gov.jfrj.siga.model.Selecionavel;
import org.apache.commons.io.FileUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.logging.Logger;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
public class DpPessoaController extends SigaSelecionavelControllerSupport<DpPessoa, DpPessoaDaoFiltro> {

    private static final Logger LOG = Logger.getLogger(DpPessoaController.class);

    private Long orgaoUsu;
    private DpLotacaoSelecao lotacaoSel;

    /**
     * @deprecated CDI eyes only
     */
    public DpPessoaController() {
        super();
    }

    @Inject
    public DpPessoaController(HttpServletRequest request, Result result, CpDao dao,
                              SigaObjects so, EntityManager em) {
        super(request, result, dao, so, em);
        setSel(new DpPessoa());
        setItemPagina(10);
    }

    @Get
    @Path({"/app/pessoa/buscar-json/{sigla}"})
    public void busca(String sigla) throws Exception {
        aBuscarJson(sigla);
    }

    @Get
    @Post
    @Path({"/app/pessoa/buscar", "/app/cosignatario/buscar", "/pessoa/buscar.action", "/cosignatario/buscar.action"})
    public void buscar(String sigla, String postback, Integer paramoffset, Long idOrgaoUsu, DpLotacaoSelecao lotacaoSel)
            throws Exception {
        final DpLotacao lotacaoTitular = getLotaTitular();
        if (postback == null && lotacaoTitular != null) {
            orgaoUsu = lotacaoTitular.getIdOrgaoUsuario();
        } else {
            orgaoUsu = idOrgaoUsu;
        }
        if (lotacaoSel != null && lotacaoSel.getId() != null && lotacaoSel.getId() > 0) {
            this.lotacaoSel = lotacaoSel;
        }
        this.getP().setOffset(paramoffset);

        if (sigla == null) {
            sigla = "";
        }

        super.aBuscar(sigla, postback);

        result.include("param", getRequest().getParameterMap());
        result.include("request", getRequest());
        result.include("itens", getItens());
        result.include("tamanho", getTamanho());
        result.include("orgaosUsu", getOrgaosUsu());
        result.include("lotacaoSel", lotacaoSel == null ? new DpLotacaoSelecao() : lotacaoSel);
        result.include("idOrgaoUsu", orgaoUsu);
        result.include("sigla", sigla);
        result.include("postbak", postback);
        result.include("offset", paramoffset);
    }

    @Get("/app/pessoa/exibir")
    public void exibi(String sigla) {
        if (sigla != null) {
            result.include("pessoa", dao().getPessoaPorPrincipal(sigla));
        }
    }

    @Override
    protected DpPessoaDaoFiltro createDaoFiltro() {
        final DpPessoaDaoFiltro flt = new DpPessoaDaoFiltro();
        flt.setNome(Texto.removeAcentoMaiusculas(getNome()));
        if (lotacaoSel != null) {
            flt.setLotacao(lotacaoSel.buscarObjeto());
        }
        flt.setIdOrgaoUsu(orgaoUsu);

        String buscarFechadas = param("buscarFechadas");
        flt.setBuscarFechadas(Boolean.parseBoolean(buscarFechadas));
        flt.setSituacaoFuncionalPessoa("");

        return flt;
    }

    @Override
    protected Selecionavel selecionarPorNome(final DpPessoaDaoFiltro flt) throws AplicacaoException {
        Selecionavel sel;

        // Acrescenta o sesb e repete a busca
        final String sigla = flt.getSigla();
        flt.setSigla(getTitular().getSesbPessoa() + sigla);
        sel = dao().consultarPorSigla(flt);
        if (sel != null)
            return sel;
        flt.setSigla(sigla);

        // Procura por nome
        flt.setIdOrgaoUsu(getLotaTitular().getOrgaoUsuario().getIdOrgaoUsu());
        flt.setNome(Texto.removeAcentoMaiusculas(flt.getSigla()));
        flt.setSigla(null);
        final List<DpPessoa> pessoas = dao().consultarPorFiltro(flt);
        if (pessoas != null)
            if (pessoas.size() == 1)
                return pessoas.get(0);
        return null;
    }

    private boolean temPermissaoParaExportarDados() {
        return Cp.getInstance().getConf().podeUtilizarServicoPorConfiguracao(getTitular(), getTitular().getLotacao(), "SIGA;GI;CAD_PESSOA;EXP_DADOS");
    }

    @Get
    @Post
    @Path({"/public/app/pessoa/selecionar", "/app/pessoa/selecionar", "/app/cosignatario/selecionar",
            "/pessoa/selecionar.action", "/cosignatario/selecionar.action"})
    public void selecionar(String sigla, String matricula) {
        String resultado = super.aSelecionar(sigla);
        if (Objects.equals(resultado, "ajax_retorno")) {
            result.include("sel", getSel());
            result.use(Results.page()).forwardTo("/WEB-INF/jsp/ajax_retorno.jsp");
        } else {
            result.use(Results.page()).forwardTo("/WEB-INF/jsp/ajax_vazio.jsp");
        }
    }

    @Get("app/pessoa/listar")
    public void lista(Integer paramoffset, Long idOrgaoUsu, String nmMatricula, String nome, String cpfPesquisa, Long idCargoPesquisa, Long idFuncaoPesquisa, Long idLotacaoPesquisa, String emailPesquisa, String identidadePesquisa) throws Exception {

        result.include("request", getRequest());
        List<CpOrgaoUsuario> list = new ArrayList<>();
        CpOrgaoUsuario ou;

        result.include("temPermissaoParaExportarDados", temPermissaoParaExportarDados());

        if ("ZZ".equals(getTitular().getOrgaoUsuario().getSigla())) {
            list = dao().listarOrgaosUsuarios();
            result.include("orgaosUsu", list);
            if (idOrgaoUsu == null) {
                carregarCombos(null, list.get(0).getId(), null, null, null, null, null, null, 0, Boolean.FALSE);
            }
        } else {
            ou = CpDao.getInstance().consultarPorSigla(getTitular().getOrgaoUsuario());
            list.add(ou);
            result.include("orgaosUsu", list);
            if (idOrgaoUsu == null) {
                carregarCombos(null, ou.getId(), null, null, null, null, null, null, 0, Boolean.FALSE);
            }
        }
        if (idOrgaoUsu != null && ("ZZ".equals(getTitular().getOrgaoUsuario().getSigla())
                || CpDao.getInstance().consultarPorSigla(getTitular().getOrgaoUsuario()).getId().equals(idOrgaoUsu))) {
            DpPessoaDaoFiltro dpPessoa = new DpPessoaDaoFiltro();
            if (paramoffset == null) {
                paramoffset = 0;
            }
            dpPessoa.setIdOrgaoUsu(idOrgaoUsu);
            dpPessoa.setNmMatricula(nmMatricula);
            dpPessoa.setNome(Texto.removeAcento(nome != null ? nome : ""));
            dpPessoa.setEmail(Texto.removeAcento(emailPesquisa != null ? emailPesquisa : ""));
            dpPessoa.setIdentidade(identidadePesquisa);

            if (idCargoPesquisa != null) {
                DpCargo cargo = new DpCargo();
                cargo.setId(idCargoPesquisa);
                dpPessoa.setCargo(cargo);
            }
            if (idLotacaoPesquisa != null) {
                DpLotacao lotacao = new DpLotacao();
                lotacao.setId(idLotacaoPesquisa);
                dpPessoa.setLotacao(lotacao);
            }
            if (idFuncaoPesquisa != null) {
                DpFuncaoConfianca funcao = new DpFuncaoConfianca();
                funcao.setIdFuncao(idFuncaoPesquisa);
                dpPessoa.setFuncaoConfianca(funcao);
            }
            if (cpfPesquisa != null && !"".equals(cpfPesquisa)) {
                dpPessoa.setCpf(Long.valueOf(cpfPesquisa.replace(".", "").replace("-", "")));
            }
            dpPessoa.setBuscarFechadas(Boolean.TRUE);
            dpPessoa.setId(0L);
            setItens(CpDao.getInstance().consultarPorFiltro(dpPessoa, paramoffset, 15));
            result.include("itens", getItens());
            Integer tamanho = dao().consultarQuantidade(dpPessoa);
            result.include("tamanho", tamanho);

            result.include("idOrgaoUsu", idOrgaoUsu);
            result.include("nome", nome);
            result.include("emailPesquisa", emailPesquisa);
            result.include("identidadePesquisa", identidadePesquisa);
            result.include("cpfPesquisa", cpfPesquisa);
            result.include("idCargoPesquisa", idCargoPesquisa);
            result.include("idFuncaoPesquisa", idFuncaoPesquisa);
            result.include("idLotacaoPesquisa", idLotacaoPesquisa);
            if (getItens().size() == 0) result.include("mensagemPesquisa", "Nenhum resultado encontrado.");

            carregarCombos(null, idOrgaoUsu, null, null, null, null, null, cpfPesquisa, paramoffset, Boolean.FALSE);
        }
    }

    @Transacional
    @Get("/app/pessoa/ativarInativar")
    public void ativarInativar(final Long id, Integer offset, Long idOrgaoUsu, String nome, String cpfPesquisa, Long idCargoPesquisa, Long idFuncaoPesquisa, Long idLotacaoPesquisa, String emailPesquisa, String identidadePesquisa) throws Exception {
        CpOrgaoUsuario ou = new CpOrgaoUsuario();
        DpPessoa pessoaAnt = dao().consultar(id, DpPessoa.class, false).getPessoaAtual();
        DpPessoa pessoa = new DpPessoa();
        ou.setIdOrgaoUsu(pessoaAnt.getOrgaoUsuario().getId());
        ou = CpDao.getInstance().consultarPorId(ou);

        if ("ZZ".equals(getTitular().getOrgaoUsuario().getSigla())
                || CpDao.getInstance().consultarPorSigla(getTitular().getOrgaoUsuario()).getId().equals(ou.getId())) {
            pessoaAnt = dao().consultar(id, DpPessoa.class, false).getPessoaAtual();
            // inativar
            if (pessoaAnt.getDataFimPessoa() == null) {
                Calendar calendar = new GregorianCalendar();
                Date date = new Date();
                calendar.setTime(date);
                pessoaAnt.setDataFimPessoa(calendar.getTime());
                pessoaAnt.setHisIdcFim(getIdentidadeCadastrante());

                try {
                    CpDao.iniciarTransacao();
                    dao().gravar(pessoaAnt);
                    CpDao.commitTransacao();
                } catch (final Exception e) {
                    if (e.getCause() instanceof ConstraintViolationException
                            && ((ConstraintViolationException) e.getCause()).getConstraintName().toUpperCase().contains("DP_PESSOA_UNIQUE_PESSOA_ATIVA")) {
                        result.include(SigaModal.ALERTA, SigaModal.mensagem("Ocorreu um problema no cadastro da pessoa"));
                    } else {
                        throw new AplicacaoException("Erro na gravação", 0, e);
                    }
                    CpDao.rollbackTransacao();
                }

            } else {// ativar
                // não pode ativar caso já exista uma pessoa com mesmo órgão, cargo, função de
                // confiança, lotação e cpf

                DpPessoaDaoFiltro dpPessoa = new DpPessoaDaoFiltro();
                dpPessoa.setIdOrgaoUsu(pessoaAnt.getOrgaoUsuario().getIdOrgaoUsu());
                dpPessoa.setCargo(pessoaAnt.getCargo());
                dpPessoa.setFuncaoConfianca(pessoaAnt.getFuncaoConfianca());
                dpPessoa.setLotacao(pessoaAnt.getLotacao());
                dpPessoa.setCpf(pessoaAnt.getCpfPessoa());
                dpPessoa.setNome("");
                dpPessoa.setId(id);

                dpPessoa.setBuscarFechadas(Boolean.FALSE);
                int tamanho = dao().consultarQuantidade(dpPessoa);

                if (tamanho > 0) {
                    throw new AplicacaoException(
                            "Já existe outro usuário ativo com estes dados: Órgão, Cargo, Função, Unidade e CPF");
                }

                DpLotacao lotacaoAtual = dpPessoa.getLotacao().getLotacaoAtual();
                if (lotacaoAtual == null || lotacaoAtual.getDataFim() != null) {
                    throw new AplicacaoException(
                            "Não é possível ativar pessoa. Lotação inexistente ou inativada.");
                }
                pessoa.setLotacao(lotacaoAtual);

                DpCargo cargoAtual = CpDao.getInstance().consultarPorIdInicialDpCargoAtual(pessoaAnt.getCargo().getIdCargoIni());
                if (cargoAtual == null || cargoAtual.getDataFim() != null) {
                    throw new AplicacaoException(
                            "Não é possível ativar pessoa. Cargo inexistente ou inativado.");
                }
                pessoa.setCargo(cargoAtual);

                DpFuncaoConfianca funcaoConfiancaAtual = null;
                if (pessoaAnt.getFuncaoConfianca() != null) {
                    funcaoConfiancaAtual = CpDao.getInstance().consultarPorIdInicialDpFuncaoConfiancaAtual(pessoaAnt.getFuncaoConfianca().getIdFuncaoIni());
                    if (funcaoConfiancaAtual == null || funcaoConfiancaAtual.getDataFim() != null) {
                        throw new AplicacaoException(
                                "Não é possível ativar pessoa. Função de Confiança encontra-se inativada.");

                    }
                }
                pessoa.setFuncaoConfianca(funcaoConfiancaAtual);

                pessoa.setNomePessoa(pessoaAnt.getNomePessoa());
                pessoa.setCpfPessoa(pessoaAnt.getCpfPessoa());
                pessoa.setOrgaoUsuario(pessoaAnt.getOrgaoUsuario());
                pessoa.setDataNascimento(pessoaAnt.getDataNascimento());
                pessoa.setMatricula(pessoaAnt.getMatricula());
                pessoa.setSituacaoFuncionalPessoa(pessoaAnt.getSituacaoFuncionalPessoa());
                pessoa.setSesbPessoa(pessoaAnt.getSesbPessoa());
                pessoa.setEmailPessoa(pessoaAnt.getEmailPessoa());
                pessoa.setIdInicial(pessoaAnt.getIdInicial());
                try {
                    dao().gravarComHistorico(pessoa, pessoaAnt, dao().consultarDataEHoraDoServidor(), getIdentidadeCadastrante());
                } catch (Exception e) {
                    if (e.getCause() instanceof ConstraintViolationException
                            && ((ConstraintViolationException) e.getCause()).getConstraintName().toUpperCase().contains("DP_PESSOA_UNIQUE_PESSOA_ATIVA")) {
                        result.include(SigaModal.ALERTA, SigaModal.mensagem("Ocorreu um problema no cadastro da pessoa"));
                    } else {
                        LOG.error("Erro ao ativar pessoa " + pessoa + ": " + e.getMessage(), e);
                        throw new AplicacaoException("Erro na gravação", 0, e);
                    }
                }
            }


            this.result.redirectTo(this).lista(offset, idOrgaoUsu, null, nome, cpfPesquisa, idCargoPesquisa, idFuncaoPesquisa, idLotacaoPesquisa, emailPesquisa, identidadePesquisa);
        }
    }

    @Get("/app/pessoa/editar")
    public void edita(final Long id) {
        CpOrgaoUsuario ou = new CpOrgaoUsuario();

        /*Carrega lista UF*/
        List<CpUF> ufList = dao().consultarUF();
        result.include("ufList", ufList);

        if (id != null) {
            DpPessoa pessoa = dao().consultar(id, DpPessoa.class, false);
            ou.setIdOrgaoUsu(pessoa.getOrgaoUsuario().getId());
            ou = CpDao.getInstance().consultarPorId(ou);
            if ("ZZ".equals(getTitular().getOrgaoUsuario().getSigla()) || CpDao.getInstance()
                    .consultarPorSigla(getTitular().getOrgaoUsuario()).getId().equals(ou.getId())) {

                /*
                 * Envio da sigla do usuário para validação no front
                 * Referente ao cartão 859
                 */
                result.include(
                        "temPermissaoParaEditarMatricula",
                        Cp.getInstance().getConf().podeUtilizarServicoPorConfiguracao(getTitular(), getTitular().getLotacao(), "SIGA;GI;CAD_PESSOA;ALT_MATRICULA")
                );

                result.include("sigla", getUsuario().getOrgaoUsuario().getSigla());
                result.include("nmPessoa", pessoa.getNomePessoa());
                result.include("cpf", pessoa.getCpfFormatado());
                result.include("email", pessoa.getEmailPessoa());
                result.include("idOrgaoUsu", pessoa.getOrgaoUsuario().getId());
                result.include("nmOrgaousu", pessoa.getOrgaoUsuario().getNmOrgaoUsu());

                /*
                 * Adicao de campos RG
                 * Cartao 1057
                 */


                result.include("identidade", pessoa.getIdentidade());
                result.include("orgaoIdentidade", pessoa.getOrgaoIdentidade());
                result.include("nmMatricula", pessoa.getMatricula());

                if (pessoa.getDataNascimento() != null) {
                    result.include("dtNascimento", pessoa.getDtNascimentoDDMMYYYY());
                }

                if (pessoa.getNomeExibicao() != null) {
                    result.include("nomeExibicao", pessoa.getNomeExibicao());
                }

                if (pessoa.getCargo() != null) {
                    result.include("idCargo", pessoa.getCargo().getCargoAtual().getId());
                }

                if (pessoa.getFuncaoConfianca() != null) {
                    result.include("idFuncao", pessoa.getFuncaoConfianca().getFuncaoConfiancaAtual().getId());
                }

                if (pessoa.getLotacao() != null) {
                    result.include("idLotacao", pessoa.getLotacao().getLotacaoAtual().getId());
                }

                if (pessoa.getUfIdentidade() != null) {
                    result.include("ufIdentidade", pessoa.getUfIdentidade());
                }

                if (pessoa.getDataExpedicaoIdentidade() != null) {
                    result.include("dataExpedicaoIdentidade", pessoa.getDataExpedicaoIdentidadeDDMMYYYY());
                }
            }
        } else
            result.include("temPermissaoParaEditarMatricula", true);

        if (id == null || (ou.getId() != null && ("ZZ".equals(getTitular().getOrgaoUsuario().getSigla())
                || CpDao.getInstance().consultarPorSigla(getTitular().getOrgaoUsuario()).getId().equals(ou.getId())))) {
            if (ou.getId() == null) {
                ou = CpDao.getInstance().consultarPorSigla(getTitular().getOrgaoUsuario());
            }

            if (ou.getId() != null) {
                DpCargoDaoFiltro cargo = new DpCargoDaoFiltro();
                cargo.setNome("");
                cargo.setIdOrgaoUsu(ou.getId());
                List<DpCargo> lista = new ArrayList<>();
                DpCargo c = new DpCargo();
                c.setId(0L);
                c.setDescricao("Selecione");
                lista.add(c);
                lista.addAll(CpDao.getInstance().consultarPorFiltro(cargo));
                result.include("listaCargo", lista);

                DpLotacaoDaoFiltro lotacao = new DpLotacaoDaoFiltro();
                lotacao.setNome("");
                lotacao.setIdOrgaoUsu(ou.getId());
                List<DpLotacao> listaLotacao = new ArrayList<>();
                DpLotacao l = new DpLotacao();
                l.setNomeLotacao("Selecione");
                l.setId(0L);
                l.setSiglaLotacao("");
                CpOrgaoUsuario cpOrgaoUsuario = new CpOrgaoUsuario();
                cpOrgaoUsuario.setIdOrgaoUsu(0L);
                cpOrgaoUsuario.setSiglaOrgaoUsu("");
                l.setOrgaoUsuario(cpOrgaoUsuario);
                listaLotacao.add(l);
                listaLotacao.addAll(CpDao.getInstance().consultarPorFiltro(lotacao));
                result.include("listaLotacao", listaLotacao);

                DpFuncaoConfiancaDaoFiltro funcao = new DpFuncaoConfiancaDaoFiltro();
                funcao.setNome("");
                funcao.setIdOrgaoUsu(ou.getId());
                List<DpFuncaoConfianca> listaFuncao = new ArrayList<>();
                DpFuncaoConfianca f = new DpFuncaoConfianca();
                f.setNomeFuncao("Selecione");
                f.setIdFuncao(0L);
                listaFuncao.add(f);
                listaFuncao.addAll(CpDao.getInstance().consultarPorFiltro(funcao));
                result.include("listaFuncao", listaFuncao);

                result.include("request", getRequest());
                result.include("id", id);
            }
        }
        List<CpOrgaoUsuario> list = new ArrayList<>();
        if ("ZZ".equals(getTitular().getOrgaoUsuario().getSigla())
                || (id != null && Cp.getInstance().getConf().podeUtilizarServicoPorConfiguracao(
                getTitular(),
                getLotaTitular(),
                "SIGA:Sistema Integrado de Gestão Administrativa;GI:Módulo de Gestão de Identidade;CAD_PESSOA:Cadastrar Pessoa;ALT:Alterar Órgão Cadastro Pessoa"
        ))
        ) {
            list = dao().listarOrgaosUsuarios();

            List<CpOrgaoUsuario> list1 = new ArrayList<>();
            for (CpOrgaoUsuario cpOrgaoUsuario : list) {
                if (!"ZZ".equals(cpOrgaoUsuario.getSiglaOrgaoUsu())) {
                    list1.add(cpOrgaoUsuario);
                }
            }

            CpOrgaoUsuario org = new CpOrgaoUsuario();
            org.setNmOrgaoUsu("Selecione");
            org.setIdOrgaoUsu(0L);
            list1.add(0, org);
            result.include("orgaosUsu", list1);
        } else {
            ou = CpDao.getInstance().consultarPorSigla(getTitular().getOrgaoUsuario());
            list.add(ou);
            result.include("orgaosUsu", list);

        }
    }

    @Post("/app/pessoa/carregarCombos")
    public void carregarCombos(final Long id, final Long idOrgaoUsu, final String nmMatricula, final String nmPessoa, final String dtNascimento,
                               final String cpf, final String email, final String cpfPesquisa, final Integer paramoffset,
                               Boolean retornarEnvioEmail) {
        result.include("request", getRequest());
        result.include("id", id);
        result.include("idOrgaoUsu", idOrgaoUsu);
        result.include("nmMatricula", nmMatricula);
        result.include("nmPessoa", nmPessoa);
        result.include("dtNascimento", dtNascimento);
        result.include("cpf", cpf);
        result.include("email", email);
        result.include("cpfPesquisa", cpfPesquisa);
        setItemPagina(15);
        result.include("currentPageNumber", calculaPaginaAtual(paramoffset));
        List<CpOrgaoUsuario> list = new ArrayList<>();

        if ("ZZ".equals(getTitular().getOrgaoUsuario().getSigla())
                || (id != null && Cp.getInstance().getConf().podeUtilizarServicoPorConfiguracao(
                getTitular(),
                getLotaTitular(),
                "SIGA:Sistema Integrado de Gestão Administrativa;GI:Módulo de Gestão de Identidade;CAD_PESSOA:Cadastrar Pessoa;ALT:Alterar Órgão Cadastro Pessoa"
        ))
        ) {
            List<CpOrgaoUsuario> list1 = new ArrayList<>();
            list = dao().consultaCpOrgaoUsuario();

            for (CpOrgaoUsuario cpOrgaoUsuario : list) {
                if (!"ZZ".equals(cpOrgaoUsuario.getSiglaOrgaoUsu())) {
                    list1.add(cpOrgaoUsuario);
                }
            }
            result.include("orgaosUsu", list1);
        } else {
            CpOrgaoUsuario ou = CpDao.getInstance().consultarPorSigla(getTitular().getOrgaoUsuario());
            list.add(ou);
            result.include("orgaosUsu", list);
        }

        if (retornarEnvioEmail == null || !retornarEnvioEmail) {
            DpCargoDaoFiltro cargo = new DpCargoDaoFiltro();
            cargo.setNome("");
            cargo.setIdOrgaoUsu(idOrgaoUsu);

            DpCargo c = new DpCargo();
            c.setId(0L);
            c.setDescricao("Selecione");

            List<DpCargo> lista = new ArrayList<>();
            lista.add(c);
            lista.addAll(CpDao.getInstance().consultarPorFiltro(cargo));

            result.include("listaCargo", lista);
        }

        DpLotacaoDaoFiltro lotacao = new DpLotacaoDaoFiltro();
        lotacao.setNome("");
        lotacao.setIdOrgaoUsu(idOrgaoUsu);
        List<DpLotacao> listaLotacao = new ArrayList<>();
        DpLotacao l = new DpLotacao();
        l.setNomeLotacao("Selecione");
        l.setId(0L);
        l.setSiglaLotacao("");
        CpOrgaoUsuario cpOrgaoUsuario = new CpOrgaoUsuario();
        cpOrgaoUsuario.setIdOrgaoUsu(0L);
        cpOrgaoUsuario.setSiglaOrgaoUsu("");
        l.setOrgaoUsuario(cpOrgaoUsuario);
        listaLotacao.add(l);
        if (idOrgaoUsu != null && idOrgaoUsu != 0)
            listaLotacao.addAll(CpDao.getInstance().consultarPorFiltro(lotacao));
        result.include("listaLotacao", listaLotacao);

        if (retornarEnvioEmail == null || !retornarEnvioEmail) {
            DpFuncaoConfiancaDaoFiltro funcao = new DpFuncaoConfiancaDaoFiltro();
            funcao.setNome("");
            funcao.setIdOrgaoUsu(idOrgaoUsu);
            List<DpFuncaoConfianca> listaFuncao = new ArrayList<>();
            DpFuncaoConfianca f = new DpFuncaoConfianca();
            f.setNomeFuncao("Selecione");
            f.setIdFuncao(0L);
            listaFuncao.add(f);
            listaFuncao.addAll(CpDao.getInstance().consultarPorFiltro(funcao));
            result.include("listaFuncao", listaFuncao);
        }

        if (retornarEnvioEmail == null || !retornarEnvioEmail) {
            /*Carrega lista UF*/
            List<CpUF> ufList = dao().consultarUF();
            result.include("ufList", ufList);
        }

        if (Objects.nonNull(id)) {
            result.include(
                    "temPermissaoParaEditarMatricula",
                    Cp.getInstance().getConf().podeUtilizarServicoPorConfiguracao(getTitular(), getTitular().getLotacao(), "SIGA;GI;CAD_PESSOA;ALT_MATRICULA")
            );
        }
        else
            result.include("temPermissaoParaEditarMatricula", true);

        if (paramoffset == null) {
            result.use(Results.page()).forwardTo("/WEB-INF/page/dpPessoa/edita.jsp");
        } else if (retornarEnvioEmail != null && retornarEnvioEmail) {
            result.use(Results.page()).forwardTo("/WEB-INF/page/dpPessoa/enviaEmail.jsp");
        } else {
            result.use(Results.page()).forwardTo("/WEB-INF/page/dpPessoa/lista.jsp");
        }
    }

    @Transacional
    @Post("/app/pessoa/gravar")
    public void editarGravar(final Long id, final Long idOrgaoUsu, final String nmMatricula, final Long idCargo, final Long idFuncao,
                             final Long idLotacao, final String nmPessoa, final String dtNascimento, final String cpf,
                             final String email, final String identidade, final String orgaoIdentidade, final String ufIdentidade,
                             final String dataExpedicaoIdentidade, final String nomeExibicao, final String enviarEmail) throws Exception {

        assertAcesso("GI:Módulo de Gestão de Identidade;CAD_PESSOA:Cadastrar Pessoa");

        new CpBL().criarUsuario(
                id, getIdentidadeCadastrante(),
                idOrgaoUsu, nmMatricula, idCargo,
                idFuncao, idLotacao, nmPessoa,
                dtNascimento, cpf, email, identidade,
                orgaoIdentidade, ufIdentidade,
                dataExpedicaoIdentidade, nomeExibicao,
                enviarEmail
        );

        lista(
                0, null, null,
                "", "", null,
                null, null,
                "", null
        );
    }


    @Get({"/app/pessoa/check_nome_por_cpf"})
    public void checkNome(String nome, String cpf, String id) throws AplicacaoException {
        Long idd = 0L;
        if (id != null && !"".equals(id.trim())) {
            idd = Long.valueOf(id);
        }
        List<DpPessoa> lista = new ArrayList<>();
        if (cpf != null && !"".equals(cpf.trim())) {
            lista = dao().listarCpfAtivoInativo(Long.parseLong(cpf.replace(".", "").replace("-", "")));
        }

        List<DpPessoa> pessoas = new ArrayList<>();
        for (DpPessoa dpPessoa : lista) {
            if (!dpPessoa.getNomePessoa().equalsIgnoreCase(nome.trim()) && !idd.equals(dpPessoa.getId())) {
                pessoas.add(dpPessoa);
            }
        }
        if (pessoas.isEmpty()) {
            result.use(Results.page()).forwardTo("/WEB-INF/jsp/ajax_vazio.jsp");
        } else {
            result.include("pessoas", pessoas);
            result.use(Results.page()).forwardTo("/WEB-INF/page/dpPessoa/pessoas.jsp");
        }
    }


    @Get("/app/pessoa/carregarExcel")
    public void carregarExcel() {
        if ("ZZ".equals(getTitular().getOrgaoUsuario().getSigla())) {
            result.include("orgaosUsu", dao().listarOrgaosUsuarios());
        } else {
            result.include("nmOrgaousu", getTitular().getOrgaoUsuario().getNmOrgaoUsu());
        }

        result.use(Results.page()).forwardTo("/WEB-INF/page/dpPessoa/cargaPessoa.jsp");
    }

    @Transacional
    @Post("/app/pessoa/carga")
    public Download carga(final UploadedFile arquivo, Long idOrgaoUsu) {
        InputStream inputStream;
        try {
            String nomeArquivo = arquivo.getFileName();
            String extensao = nomeArquivo.substring(nomeArquivo.lastIndexOf("."));

            File file = new File("arq" + extensao);

            file.createNewFile();
            FileUtils.copyInputStreamToFile(arquivo.getFile(), file);

            CpOrgaoUsuario orgaoUsuario = new CpOrgaoUsuario();
            if (idOrgaoUsu != null) {
                orgaoUsuario.setIdOrgaoUsu(idOrgaoUsu);
            } else {
                orgaoUsuario = getTitular().getOrgaoUsuario();
            }

            CpBL cpbl = new CpBL();
            inputStream = cpbl.uploadPessoa(file, orgaoUsuario, extensao, getIdentidadeCadastrante());
        } catch (Exception e) {
            throw new AplicacaoException("Problemas ao salvar pessoa(s)", 0, e);
        }
        if (inputStream == null) {
            result.include(SigaModal.ALERTA, SigaModal.mensagem("Arquivo processado com sucesso!").titulo("Sucesso"));
            carregarExcel();
        } else {
            return new InputStreamDownload(inputStream, "application/text", "inconsistencias.txt");
        }
        return null;
    }

    @Consumes("application/json")
    @Post("/app/pessoa/usuarios/envioDeEmailPendente")
    public void buscarUsuariosComEnvioDeEmailPendente(DpPessoaUsuarioDTO dados) {
        DpPessoaDaoFiltro dpPessoa = new DpPessoaDaoFiltro();

        dpPessoa.setIdOrgaoUsu(dados.getIdOrgaoUsu());
        dpPessoa.prepararLotacao(dados.getIdLotacaoSelecao());

        List<DpPessoaUsuarioDTO> usuarios = dao().consultarUsuariosComEnvioDeEmailPendenteFiltrandoPorLotacao(dpPessoa);

        result.use(Results.json()).from(usuarios).serialize();
    }

    @Get
    @Post
    @Path({"app/pessoa/enviarEmail", "/pessoa/enviarEmail.action"})
    public void enviaEmail(Integer paramoffset, Long idOrgaoUsu, String nome, String cpfPesquisa,
                           String idLotacaoPesquisa, String idUsuarioPesquisa, Integer paramTamanho) {
        result.include("request", getRequest());
        List<CpOrgaoUsuario> list = new ArrayList<>();
        CpOrgaoUsuario ou;
        if ("ZZ".equals(getTitular().getOrgaoUsuario().getSigla())) {
            list = dao().listarOrgaosUsuarios();
            list.remove(0);
            result.include("orgaosUsu", list);
            if (idOrgaoUsu == null) {
                carregarCombos(null, idOrgaoUsu, null, null, null, null, null, null, 0, Boolean.TRUE);
            }
        } else {
            ou = CpDao.getInstance().consultarPorSigla(getTitular().getOrgaoUsuario());
            list.add(ou);
            result.include("orgaosUsu", list);
            if (idOrgaoUsu == null) {
                carregarCombos(null, ou.getId(), null, null, null, null, null, null, 0, Boolean.TRUE);
            }
        }
        if (idOrgaoUsu != null && ("ZZ".equals(getTitular().getOrgaoUsuario().getSigla())
                || CpDao.getInstance().consultarPorSigla(getTitular().getOrgaoUsuario()).getId().equals(idOrgaoUsu))) {
            DpPessoaDaoFiltro dpPessoa = new DpPessoaDaoFiltro();
            if (paramoffset == null) {
                paramoffset = 0;
            }
            dpPessoa.setIdOrgaoUsu(idOrgaoUsu);
            dpPessoa.setNome(Texto.removeAcento(nome != null ? nome : ""));
            dpPessoa.prepararLotacao(idLotacaoPesquisa);
            dpPessoa.prepararPessoa(idUsuarioPesquisa);

            if (cpfPesquisa != null && !"".equals(cpfPesquisa)) {
                dpPessoa.setCpf(Long.valueOf(cpfPesquisa.replace(".", "").replace("-", "")));
            }
            dpPessoa.setBuscarFechadas(Boolean.TRUE);
            setItens(CpDao.getInstance().consultarPorFiltroSemIdentidade(dpPessoa, paramoffset, 15));
            result.include("itens", getItens());

            int tamanho;
            if (paramTamanho == null) {
                tamanho = dao().consultarQuantidadeDpPessoaSemIdentidade(dpPessoa);
            } else {
                tamanho = paramTamanho;
            }
            result.include("tamanho", tamanho);

            result.include("idOrgaoUsu", idOrgaoUsu);
            result.include("nome", nome);
            result.include("cpfPesquisa", cpfPesquisa);
            result.include("idLotacaoPesquisa", idLotacaoPesquisa);
            result.include("idUsuarioPesquisa", idUsuarioPesquisa);

            carregarCombos(null, idOrgaoUsu, null, null, null, null, null, cpfPesquisa, paramoffset, Boolean.TRUE);
        }
    }

    @Transacional
    @Post("app/pessoa/enviar")
    public void enviar(Long idOrgaoUsu, String nome, String cpfPesquisa, String idLotacaoPesquisa, String idUsuarioPesquisa) throws Exception {
        String[] senhaGerada = new String[1];

        if (idOrgaoUsu == null || idOrgaoUsu == 0)
            throw new AplicacaoException("Órgão não informado");

        DpPessoaDaoFiltro dpPessoa = new DpPessoaDaoFiltro();

        dpPessoa.setIdOrgaoUsu(idOrgaoUsu);
        dpPessoa.setNome(Texto.removeAcento(nome != null ? nome : ""));
        dpPessoa.prepararLotacao(idLotacaoPesquisa);
        dpPessoa.prepararPessoa(idUsuarioPesquisa);

        if (cpfPesquisa != null && !"".equals(cpfPesquisa)) {
            dpPessoa.setCpf(Long.valueOf(cpfPesquisa.replace(".", "").replace("-", "")));
        }

        List<DpPessoa> lista = CpDao.getInstance().consultarPorFiltroSemIdentidade(dpPessoa, 0, 0);
        String cpfAnterior = "";
        for (DpPessoa dpPessoa2 : lista) {

            if (!cpfAnterior.equals(dpPessoa2.getCpfPessoa().toString())) {
                senhaGerada[0] = GeraMessageDigest.geraSenha();
            }
            Cp.getInstance().getBL().criarIdentidade(dpPessoa2.getSesbPessoa() + dpPessoa2.getMatricula(),
                    dpPessoa2.getCpfFormatado(), getIdentidadeCadastrante(), null, senhaGerada, Boolean.FALSE);
            cpfAnterior = dpPessoa2.getCpfPessoa().toString();
        }
        this.result.redirectTo(this).enviaEmail(0, idOrgaoUsu, nome, cpfPesquisa, idLotacaoPesquisa, idUsuarioPesquisa, 0);
    }

    @Post
    @Path("app/pessoa/exportarCsv")
    public Download exportarCsv(Long idOrgaoUsu, String nome, String cpfPesquisa, Long idCargoPesquisa,
                                Long idFuncaoPesquisa, Long idLotacaoPesquisa, String emailPesquisa, String identidadePesquisa) {

        CpOrgaoUsuario ou = new CpOrgaoUsuario();

        if (idOrgaoUsu != null && ("ZZ".equals(getTitular().getOrgaoUsuario().getSigla())
                || CpDao.getInstance().consultarPorSigla(getTitular().getOrgaoUsuario()).getId().equals(idOrgaoUsu))) {
            DpPessoa dpPessoa = new DpPessoa();

            if (ou.getId() == null) {
                ou.setIdOrgaoUsu(idOrgaoUsu);
                ou = CpDao.getInstance().consultarPorId(ou);
            }
            dpPessoa.setOrgaoUsuario(ou);
            dpPessoa.setNomePessoa(Texto.removeAcento(nome != null ? nome : ""));
            dpPessoa.setEmailPessoa(Texto.removeAcento(emailPesquisa != null ? emailPesquisa : ""));
            dpPessoa.setIdentidade(identidadePesquisa);
            if (idCargoPesquisa != null) {
                DpCargo cargo = new DpCargo();
                cargo.setId(idCargoPesquisa);
                dpPessoa.setCargo(cargo);
            }
            if (idLotacaoPesquisa != null) {
                DpLotacao lotacao = new DpLotacao();
                lotacao.setId(idLotacaoPesquisa);
                dpPessoa.setLotacao(lotacao);
            }
            if (idFuncaoPesquisa != null) {
                DpFuncaoConfianca funcao = new DpFuncaoConfianca();
                funcao.setIdFuncao(idFuncaoPesquisa);
                dpPessoa.setFuncaoConfianca(funcao);
            }
            if (cpfPesquisa != null && !"".equals(cpfPesquisa)) {
                dpPessoa.setCpfPessoa(Long.valueOf(cpfPesquisa.replace(".", "").replace("-", "")));
            }
            dpPessoa.setId(0L);
            List<DpPessoa> lista = CpDao.getInstance().consultarPessoaComOrgaoFuncaoCargo(dpPessoa);

            if (lista.size() > 0) {
                InputStream inputStream;
                StringBuilder texto = new StringBuilder()
                        .append("Sigla do Órgão;Cargo;Função de Confiança;Sigla da Unidade;Nome;Data de Nascimento;CPF;E-mail;Matrícula;RG;Órgão Expedidor;UF;Data de Expedição;Status")
                        .append(System.lineSeparator());

                for (DpPessoa p : lista) {
                    texto.append(p.getOrgaoUsuario().getSiglaOrgaoUsu()).append(";");
                    texto.append(p.getCargo().getNomeCargo()).append(";");
                    texto.append(p.getFuncaoConfianca() != null ? p.getFuncaoConfianca().getNomeFuncao() + ";" : ";");
                    texto.append(p.getLotacao().getSiglaLotacao()).append(";");
                    texto.append(p.getNomePessoa()).append(";");
                    texto.append(p.getDataNascimento() == null ? ";" : p.getDataNascimento() + ";");
                    texto.append(p.getCpfFormatado()).append(";");
                    texto.append(p.getEmailPessoa()).append(";");
                    texto.append(p).append(";");
                    texto.append(p.getIdentidade() != null ? p.getIdentidade() + ";" : ";");
                    texto.append(p.getOrgaoIdentidade() != null ? p.getOrgaoIdentidade() + ";" : ";");
                    texto.append(p.getUfIdentidade() != null ? p.getUfIdentidade() + ";" : ";");
                    texto.append(p.getDataExpedicaoIdentidadeDDMMYYYY() != null ? p.getDataExpedicaoIdentidadeDDMMYYYY() + ";" : ";");
                    texto.append(p.getDataFimPessoa() == null ? "Ativo" : "Inativo").append(";");
                    texto.append(System.lineSeparator());
                }

                inputStream = new ByteArrayInputStream(texto.toString().getBytes(StandardCharsets.ISO_8859_1));

                return new InputStreamDownload(inputStream, "text/csv", "pessoas.csv");
            } else {
                if ("ZZ".equals(getTitular().getOrgaoUsuario().getSigla())) {
                    result.include("orgaosUsu", dao().listarOrgaosUsuarios());
                } else {
                    List<CpOrgaoUsuario> list = new ArrayList<>();
                    result.include("orgaosUsu", list.add(CpDao.getInstance().consultarPorSigla(getTitular().getOrgaoUsuario())));
                }
                result.include("idOrgaoUsu", idOrgaoUsu);
                result.include("nome", nome);
                result.include("cpfPesquisa", cpfPesquisa);
                result.include("idCargoPesquisa", idCargoPesquisa);
                result.include("idFuncaoPesquisa", idFuncaoPesquisa);
                result.include("idLotacaoPesquisa", idLotacaoPesquisa);
                result.include("emailPesquisa", emailPesquisa);
                result.include("identidadePesquisa", identidadePesquisa);
                result.include("mensagemPesquisa", "Nenhum resultado encontrado.");
                result.include("temPermissaoParaExportarDados", temPermissaoParaExportarDados());

                carregarCombos(null, idOrgaoUsu, null, null, null, null, null, cpfPesquisa, 0, Boolean.FALSE);
            }
        }

        return null;
    }

    /*
     * Pega informações do usuário logado
     * Referente ao cartão 859
     */
    protected DpPessoa getUsuario() {
        return so.getCadastrante();
    }

    @Consumes("application/json")
    @Get("/public/app/pessoa/usuarios/buscarEmailParcialmenteOculto/{cpf}")
    public void buscarEmailUsuarioPorCpf(@PathParam("cpf") String cpf) {
        String captchaSiteKey = Prop.get("/siga.hcaptcha.key");
        String captchaSitePassword = Prop.get("/siga.hcaptcha.pwd");
        result.include("captchaSiteKey", captchaSitePassword);

        try {
            if (captchaSiteKey == null || captchaSitePassword == null) {
                throw new RuntimeException("Captcha não definido");
            }

            if (cpf == null) {
                result.include("request", getRequest());
                return;
            }

            CPFUtils.efetuaValidacaoSimples(cpf);

            try {
                String captchaResponse = request.getParameter("captcha-response");
                boolean success = false;
                if (captchaResponse != null) {
                    JSONObject body = Hcaptcha.validar(captchaSitePassword, captchaResponse, request.getRemoteAddr());

                    String hostname = request.getServerName();
                    if (body.getBoolean("success")) {
                        String retHostname = body.getString("hostname");
                        success = retHostname.equals(hostname);
                    }
                }
                if (!success) {
                    result.include("request", getRequest());
                    return;
                }
            } catch (final Exception e) {
                throw new RuntimeException("Não é possível realizar a verificação de segurança com o Captcha: " + e.getMessage());
            }

            DpPessoaDaoFiltro dpPessoa = new DpPessoaDaoFiltro();
            dpPessoa.setBuscarFechadas(false);
            dpPessoa.setCpf(Long.valueOf(cpf));
            dpPessoa.setNome("");

            List<DpPessoa> usuarios = dao().consultarPorFiltro(dpPessoa);
            Set<String> emailsOculto = new HashSet<>();

            final String mensagemUsuarioOuIdentidadeInexistente = "Usuário não localizado ou não possui um acesso ativo. "
                    + "Verifique os dados informados e caso necessite, entre em contato com o Administrador Local";

            if (!usuarios.isEmpty()) {
                List<CpIdentidade> listaIdentidadesCpf = CpDao.getInstance().consultaIdentidadesPorCpf(cpf);

                if (!listaIdentidadesCpf.isEmpty()) {
                    Set<String> emailsDistintos = new HashSet<>();
                    //Adiciona email normal de forma distinta
                    for (DpPessoa usuario : usuarios) {
                        emailsDistintos.add(usuario.getEmailPessoaAtual());
                    }
                    //Troca emails distintos por Email estenografado
                    for (String email : emailsDistintos) {
                        emailsOculto.add(SigaUtil.ocultaParcialmenteEmail(email));
                    }
                } else {
                    throw new RuntimeException(mensagemUsuarioOuIdentidadeInexistente);
                }
            } else {
                throw new RuntimeException(mensagemUsuarioOuIdentidadeInexistente);
            }

            String jwt = SigaUtil.buildJwtToken("RESET-SENHA", cpf);
            HashMap<String, Object> json = new HashMap<>();

            json.put("emails", emailsOculto);
            json.put("jwt", jwt);

            result.use(Results.json()).withoutRoot().from(json).serialize();
        } catch (RuntimeException ex) {
            result.use(Results.http()).sendError(400, ex.getMessage());
        } catch (Exception ex) {
            result.use(Results.http()).sendError(500, ex.getMessage());
        }
    }
}
