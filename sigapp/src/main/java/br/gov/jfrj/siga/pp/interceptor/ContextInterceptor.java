package br.gov.jfrj.siga.pp.interceptor;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import br.com.caelum.vraptor.controller.ControllerMethod;
import org.hibernate.Session;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.Intercepts;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.interceptor.Interceptor;
import br.com.caelum.vraptor.util.jpa.extra.ParameterLoaderInterceptor;
import br.gov.jfrj.siga.model.ActiveRecord;
import br.gov.jfrj.siga.model.ContextoPersistencia;
import br.gov.jfrj.siga.pp.dao.PpDao;
import br.gov.jfrj.siga.vraptor.ParameterOptionalLoaderInterceptor;

/**
 * Interceptor que inicia a instancia do DAO a ser utilizado pelo sistema. O DAO deve ser utilizado quando se deseja realizar operacoes quando nao se pode utilizar o {@link ActiveRecord}.
 * 
 * @author db1.
 *
 */
@RequestScoped
@Intercepts(before = { ParameterLoaderInterceptor.class, ParameterOptionalLoaderInterceptor.class })
public class ContextInterceptor implements Interceptor {

    @Inject
    private EntityManager em;

    @Override
    public void intercept(InterceptorStack stack, ControllerMethod method, Object resourceInstance) throws InterceptionException {
        try {
            ContextoPersistencia.setEntityManager(em);
            PpDao.freeInstance();
            PpDao.getInstance();
            stack.next(method, resourceInstance);
        } catch (Exception e) {
            throw new InterceptionException(e);
        }
    }

    @Override
    public boolean accepts(ControllerMethod method) {
        return Boolean.TRUE;
    }

}