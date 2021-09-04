package br.com.caelum.vraptor.util.jpa;

import br.com.caelum.vraptor.Intercepts;
import br.com.caelum.vraptor.controller.ControllerMethod;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.interceptor.Interceptor;

/**
 * Disables the JPATransactionInterceptor.
 * 
 * @author Renato Crivano
 */

@Intercepts
public class JPATransactionInterceptor implements Interceptor {
	public JPATransactionInterceptor() {
	}

	public void intercept(InterceptorStack stack, ControllerMethod method, Object instance) {
		stack.next(method, instance);
	}

	public boolean accepts(ControllerMethod method) {
		return false; // Will intercept no requests
	}
}
