package br.gov.jfrj.siga.ex.xjus;

import br.gov.jfrj.siga.base.Prop;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.Reference;
import br.jus.trf2.xjus.record.api.XjusRecordAPIContext;
import com.crivano.swaggerservlet.SwaggerAsyncResponse;
import com.crivano.swaggerservlet.SwaggerCall;
import com.crivano.swaggerservlet.SwaggerException;
import com.crivano.swaggerservlet.SwaggerServlet;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AllReferencesGet implements IXjusRecordAPI.IAllReferencesGet {
    static public final long TIMEOUT_MILLISECONDS = 15000;

    @Override
    public void run(Request req, Response resp, XjusRecordAPIContext ctx) throws Exception {
        resp.list = new ArrayList<>();
        if (req.lastid == null)
            req.lastid = defaultLastId();

        Map<RecordServiceEnum, Future<SwaggerAsyncResponse<Response>>> map = new HashMap<>();

        // Call Each System
        for (RecordServiceEnum service : RecordServiceEnum.values()) {
            String url = serviceUrl(service);

            Request q = new Request();
            q.max = req.max;
            String[] split = req.lastid.split("-");
            q.lastid = split[0];
            if (service.ordinal() > Integer.parseInt(split[1]))
                q.lastid = Utils.formatId(Long.parseLong(q.lastid) - 1);
            Future<SwaggerAsyncResponse<Response>> future = SwaggerCall.callAsync(
                    service.name().toLowerCase() + "-all-references", Prop.get("/xjus.password"), "GET", url, q,
                    Response.class);
            map.put(service, future);
        }

        Date dt1 = new Date();

        for (RecordServiceEnum service : RecordServiceEnum.values()) {
            long timeout = TIMEOUT_MILLISECONDS - ((new Date()).getTime() - dt1.getTime());
            if (timeout < 0L)
                timeout = 0;
            SwaggerAsyncResponse<Response> futureresponse = map.get(service).get(timeout, TimeUnit.MILLISECONDS);
            Response o = futureresponse.getResp();

            SwaggerException ex = futureresponse.getException();
            if (ex != null)
                throw ex;

            for (Reference r : o.list) {
                r.id += "-" + service.ordinal();
                resp.list.add(r);
            }
        }

        // Sort items by compound ID
        resp.list.sort(Comparator.comparing(o -> o.id));

        // Drop items that exceed the maximum size
        while (resp.list.size() > Integer.parseInt(req.max))
            resp.list.remove(resp.list.size() - 1);
    }

    static public String serviceUrl(RecordServiceEnum service) {
        return SwaggerServlet.getHttpServletRequest().getRequestURL().toString().replace("/x-jus/v1/",
                "/x-jus/" + service.name().toLowerCase() + "/v1/");
    }

    static public String defaultLastId() {
        return Utils.formatId(0L) + "-" + RecordServiceEnum.values()[RecordServiceEnum.values().length - 1].ordinal();
    }

    public String getContext() {
        return "obter a lista de índices";
    }
}
