package br.gov.jfrj.siga.vraptor;

import br.com.caelum.vraptor.core.MethodInfo;
import br.com.caelum.vraptor.http.MutableRequest;
import br.com.caelum.vraptor.http.MutableResponse;
import br.com.caelum.vraptor.proxy.Proxifier;
import br.com.caelum.vraptor.view.DefaultPageResult;
import br.com.caelum.vraptor.view.PathResolver;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@Specializes
@Dependent
public class RedirectFix extends DefaultPageResult {

    @Inject
    private HttpServletRequest request;

    /** @deprecated */
    protected RedirectFix() {
        this((MutableRequest)null, (MutableResponse)null, (MethodInfo)null, (PathResolver)null, (Proxifier)null);
    }

    @Inject
    public RedirectFix(MutableRequest req, MutableResponse res, MethodInfo methodInfo, PathResolver resolver, Proxifier proxifier) {
        super(req, res, methodInfo, resolver, proxifier);
        this.request = req;
    }

    @Override
    public void redirectTo(String url) {
        if (url.startsWith("/") && !StringUtils.isEmpty(request.getHeader("X-Forwarded-Proto")))
            url = request.getHeader("X-Forwarded-Proto") + "://" +
                    request.getHeader("X-Forwarded-Host") + ":" +
                    request.getHeader("X-Forwarded-Port") +
                    request.getContextPath() + url;

        super.redirectTo(url);
    }

}
