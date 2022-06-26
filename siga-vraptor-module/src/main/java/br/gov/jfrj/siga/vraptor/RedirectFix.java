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
import java.net.URI;
import java.net.URISyntaxException;

@Specializes
@Dependent
public class RedirectFix extends DefaultPageResult {

    @Inject
    private HttpServletRequest request;

    /**
     * @deprecated
     */
    protected RedirectFix() {
        this(null, null, null, null, null);
    }

    @Inject
    public RedirectFix(MutableRequest req, MutableResponse res, MethodInfo methodInfo, PathResolver resolver, Proxifier proxifier) {
        super(req, res, methodInfo, resolver, proxifier);
        this.request = req;
    }

    @Override
    public void redirectTo(final String url) {
        String proto = StringUtils.defaultIfBlank(request.getHeader("X-Forwarded-Proto"), request.getScheme());
        String host = StringUtils.defaultIfBlank(request.getHeader("X-Forwarded-Host"), request.getServerName());
        String port = StringUtils.defaultIfBlank(request.getHeader("X-Forwarded-Port"), String.valueOf(request.getServerPort()));
        String path = request.getContextPath() + request.getServletPath();

        String target;
        try {
            target = new URI(proto, null, host, Integer.parseInt(port), path, null, null)
                    .resolve(url)
                    .toString();
        } catch (URISyntaxException e) {
            target = url;
        }

        super.redirectTo(target);
    }

}
