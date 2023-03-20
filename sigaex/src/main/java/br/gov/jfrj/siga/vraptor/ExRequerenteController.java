package br.gov.jfrj.siga.vraptor;

import java.util.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Result;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.base.SigaCalendar;
import br.gov.jfrj.siga.dp.CpLocalidade;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.ex.ExRequerenteDoc;
import br.gov.jfrj.siga.hibernate.ExDao;
import br.gov.jfrj.siga.model.ContextoPersistencia;
import br.gov.jfrj.siga.persistencia.ExRequerenteSearch;

@Controller
public class ExRequerenteController extends ExController {
	/**
	 * @deprecated  CDI eyes only
	 */
	public ExRequerenteController() {
		super();
	}

	@Inject
	public ExRequerenteController(HttpServletRequest request, HttpServletResponse response, ServletContext context,
			Result result, SigaObjects so, EntityManager em) {
		super(request, response, context, result, CpDao.getInstance(), so, em);
	}

	@Get("/app/requerente/listar")
	public void listar(String nome, String cpfPesquisa, Integer tamanho, Integer page) {
		String ref = "";

		if (nome != null)
			ref += nome;

		if (cpfPesquisa != null) {
			cpfPesquisa = cpfPesquisa.replace(".", "").replace("-", "").replace("/", "");
			ref += (ref.isEmpty()) ? cpfPesquisa : " " + cpfPesquisa;
		}

		if (tamanho == null || tamanho < 1)
			tamanho = 10;

		if (page == null)
			page = 0;

		ExRequerenteSearch busca = new ExRequerenteSearch(ExDao.getInstance().em());

		result.include("itens", busca.search(page, tamanho, ref));
		result.include("totalItens", busca.getTotalResultados());
		result.include("nome", nome);
		result.include("cpfPesquisa", cpfPesquisa);
	}

	@Post("/app/requerente/gravar")
	public void gravar(Long id, String nmRequerente, String email, Integer tipoRequerente, String endereco, Integer numero,
			String bairro, String cidade, String contato, String cdRequerente, String registro, String dtNascimento,
			String nacionalidade, Integer estadoCivil, String nmConjugue) {
		ExRequerenteDoc requerente;
		if (id == null) {
			requerente = new ExRequerenteDoc();
		} else {
			requerente = dao().consultar(id, ExRequerenteDoc.class, false);
		}

		if (nmRequerente == null || nmRequerente.trim().isEmpty())
			throw new AplicacaoException("Nome não informado");

		if (cdRequerente == null || cdRequerente.trim().isEmpty())
			throw new AplicacaoException("CPF/CNPJ não informado");

		if (!nmRequerente.matches("[a-zA-ZáâãéêíóôõúçÁÂÃÉÊÍÓÔÕÚÇ' ]+"))
			throw new AplicacaoException("Nome com caracteres não permitidos");

		requerente.setNomeRequerente(nmRequerente);
		String cadastroReq = cdRequerente.replace(".", "").replace("-", "").replace("/", "");
		requerente.setCpfRequerente(cadastroReq.trim());

		if (email != null && !email.trim().isEmpty())
			requerente.setEmailRequerente(email);

		if (tipoRequerente != null)
			requerente.setTipoRequerente(tipoRequerente);

		if (endereco != null && !endereco.trim().isEmpty())
			requerente.setEndereco(endereco);

		if (numero != null && numero != 0)
			requerente.setNumeroEndereco(numero);

		if (bairro != null && !bairro.trim().isEmpty())
			requerente.setBairro(bairro);

		if (cidade != null && !cidade.trim().isEmpty())
			requerente.setCidade(cidade);

		if (contato != null && !contato.trim().isEmpty())
			requerente
					.setTelefoneRequerente(contato.replace("(", "").replace(")", "").replace(" ", "").replace("-", ""));

		if (registro != null && !registro.trim().isEmpty())
			requerente.setRgRequerente(registro);

		if (nacionalidade != null && !nacionalidade.trim().isEmpty())
			requerente.setNacionalidadeRequerente(nacionalidade);

		if (estadoCivil != null)
			requerente.setEstadoCivil(estadoCivil);

		if (nmConjugue != null && !nmConjugue.trim().isEmpty())
			requerente.setNomeConjugue(nmConjugue);

		if (dtNascimento != null && !dtNascimento.isEmpty()) {
			Date dtNasc = SigaCalendar.converteStringEmData(dtNascimento);

			Calendar hj = Calendar.getInstance();
			Calendar dtNasci = new GregorianCalendar();
			assert dtNasc != null;
			dtNasci.setTime(dtNasc);

			if (hj.before(dtNasci)) {
				throw new AplicacaoException("Data de nascimento inválida");
			}
			requerente.setDataNascimento(dtNasc);
		} else {
			requerente.setDataNascimento(null);
		}

		try {
			ContextoPersistencia.begin();
			dao().gravar(requerente);
			ContextoPersistencia.commit();
		} catch (final Exception e) {
			ExDao.rollbackTransacao();
			throw new AplicacaoException("Erro ao salvar o requerente", 0, e);
		}
		result.redirectTo(ExRequerenteController.class).listar("", "", 0, 0);
	}

	@Get("/app/requerente/remover")
	public void remover(Long id) {
		if (id != null) {
			try {
				ContextoPersistencia.begin();
				ExRequerenteDoc requerente = daoRequerente(id);
				dao().excluir(requerente);
				ContextoPersistencia.commit();
			} catch (final Exception e) {
				ContextoPersistencia.em().getTransaction().rollback();
				throw new AplicacaoException("Erro na exclusão do requerente", 0, e);
			}
		} else
			throw new AplicacaoException("ID não informada");

		result.redirectTo(ExRequerenteController.class).listar("", "", 0, 0);
	}

	@Get("/app/requerente/editar")
	public void editar(final Long id) {

		if (id != null) {
			ExRequerenteDoc requerente = dao().consultar(id, ExRequerenteDoc.class, false);
			
			result.include("editar", true);
			result.include("id", requerente.getId());
			result.include("nmRequerente", requerente.getNomeRequerente());
			result.include("email", requerente.getEmailRequerente());
			result.include("tipoRequerente", requerente.getTipoRequerente());
			result.include("endereco", requerente.getEndereco());
			result.include("numero", requerente.getNumeroEndereco());
			result.include("bairro", requerente.getBairro());
			result.include("cidade", requerente.getCidade());
			result.include("contato", requerente.getTelefoneRequerente());
			result.include("cdRequerente", requerente.getCpfFormatado());
			result.include("registro", requerente.getRgRequerente());
			result.include("dtNascimento", requerente.getDtNascimentoFormatado());
			result.include("nacionalidade", requerente.getNacionalidadeRequerente());
			result.include("estadoCivil", requerente.getEstadoCivil());
			result.include("nmConjugue", requerente.getNomeConjugue());
		} else {
			CpLocalidade localidade = getLotaTitular().getLocalidade();

			if (Objects.nonNull(localidade)) {
				result.include("cidade", localidade.getDescricao() + " - " + localidade.getUF().getSigla());
			}

			result.include("nacionalidade", "Brasileiro");
		}

		result.include("request", getRequest());
	}

	@Get("/app/requerente/buscar")
	public void buscar(String ref, Integer page, Integer numItens) {
		if (numItens == null || numItens < 1)
			numItens = 10;

		if (page == null)
			page = 0;

		if (ref != null) {
			ref = ref.replace(".", "").replace("-", "").replace("/", "");
		}

		ExRequerenteSearch busca = new ExRequerenteSearch(ExDao.getInstance().em());


		result.include("requerentes", busca.search(page, numItens, ref));
		result.include("numResultados", busca.getTotalResultados());
		result.include("paginaAtual", page);
	}

	private ExRequerenteDoc daoRequerente(long id) {
		return dao().consultar(id, ExRequerenteDoc.class, false);
	}

}
