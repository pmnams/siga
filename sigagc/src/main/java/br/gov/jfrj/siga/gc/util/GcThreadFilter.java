package br.gov.jfrj.siga.gc.util;

import java.io.IOException;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.gov.jfrj.siga.base.CurrentRequest;
import br.gov.jfrj.siga.base.RequestInfo;
import br.gov.jfrj.siga.cp.bl.Cp;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.model.ContextoPersistencia;
import br.gov.jfrj.siga.model.dao.ModeloDao;

public class GcThreadFilter implements Filter {
	
	private EntityManager em;
	
	/**
	 * @deprecated CDI eyes only
	 */
	public GcThreadFilter() {
		super();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {


		EntityManager em = GcStarter.emf.createEntityManager();
		ContextoPersistencia.setEntityManager(em);

		ModeloDao.freeInstance();
		CpDao.getInstance();

		em.getTransaction().begin();

		try {
			ContextoPersistencia.setEntityManager(em);
			ModeloDao.freeInstance();
			CpDao.getInstance();
			Cp.getInstance().getConf().limparCacheSeNecessario();

			chain.doFilter(request, response);

			em.getTransaction().commit();
		} catch (Exception e) {
			if (em.getTransaction().isActive())
				em.getTransaction().rollback();

			throw new ServletException(e);
		} finally {
			em.close();
			ModeloDao.freeInstance();
			ContextoPersistencia.setEntityManager(null);
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}