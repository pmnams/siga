
package br.gov.jfrj.siga.integracao.ws.siafem;

import javax.xml.namespace.QName;
import javax.xml.ws.*;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * PRODESP - Cia de Processamento de Dados do Estado de São Paulo  <br>--------------------------------------<br>Serviço para comunicação com sistema SIAFEM/SIAFISICO, utilizando mensagens XML;<br><br>10/02/2020 v8.01.015(g)- MWAS - Inclusão mensagens: SIAFNLRETIR, SIAFNLDEVOLVIR, SIAFNLTRANSFIR, SIAFEMALTERACAOPROCESSONL<br>20/02/2020 v8.01.016(a)- MWAS - Inclusão mensagens: SIAFLISTAOBNCUMP, SIAFINCDOMCREDWEB12/03/2020 v8.02.001(a)- MWAS - Migração do codigo para novo repositorio, simplificando a manutenção do serviço.02/07/2020 v8.02.002(a)- MWAS - Alteração acesso servidor com SSL, devido problema certificado SEFAZ.31/07/2020 v8.02.003(d )- MWAS - Inclusao de LOG de Acesso e lista negra de IP
 * <p>
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 */
@WebServiceClient(name = "recebeMSG", targetNamespace = "https://www.bec.sp.gov.br/SIAFISICO/RecebeMSG/", wsdlLocation = "https://siafemhom.intra.fazenda.sp.gov.br/siafisico/RecebeMSG.asmx?WSDL")
public class RecebeMSG
        extends Service {

    private final static URL RECEBEMSG_WSDL_LOCATION;
    private final static WebServiceException RECEBEMSG_EXCEPTION;
    private final static QName RECEBEMSG_QNAME = new QName(PropriedadeSiafem.URL_NAMESPACE, "recebeMSG");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL(PropriedadeSiafem.URL_WSDL);
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        RECEBEMSG_WSDL_LOCATION = url;
        RECEBEMSG_EXCEPTION = e;
    }

    public RecebeMSG() {
        super(__getWsdlLocation(), RECEBEMSG_QNAME);
    }

    public RecebeMSG(WebServiceFeature... features) {
        super(__getWsdlLocation(), RECEBEMSG_QNAME, features);
    }

    public RecebeMSG(URL wsdlLocation) {
        super(wsdlLocation, RECEBEMSG_QNAME);
    }

    public RecebeMSG(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, RECEBEMSG_QNAME, features);
    }

    public RecebeMSG(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public RecebeMSG(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * @return returns RecebeMSGSoap
     */
    @WebEndpoint(name = "recebeMSGSoap")
    public RecebeMSGSoap getRecebeMSGSoap() {
        return super.getPort(new QName(PropriedadeSiafem.URL_NAMESPACE, "recebeMSGSoap"), RecebeMSGSoap.class);
    }

    /**
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return returns RecebeMSGSoap
     */
    @WebEndpoint(name = "recebeMSGSoap")
    public RecebeMSGSoap getRecebeMSGSoap(WebServiceFeature... features) {
        return super.getPort(new QName(PropriedadeSiafem.URL_NAMESPACE, "recebeMSGSoap"), RecebeMSGSoap.class, features);
    }

    /**
     * @return returns RecebeMSGSoap
     */
    @WebEndpoint(name = "recebeMSGSoap12")
    public RecebeMSGSoap getRecebeMSGSoap12() {
        return super.getPort(new QName(PropriedadeSiafem.URL_NAMESPACE, "recebeMSGSoap12"), RecebeMSGSoap.class);
    }

    /**
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return returns RecebeMSGSoap
     */
    @WebEndpoint(name = "recebeMSGSoap12")
    public RecebeMSGSoap getRecebeMSGSoap12(WebServiceFeature... features) {
        return super.getPort(new QName(PropriedadeSiafem.URL_NAMESPACE, "recebeMSGSoap12"), RecebeMSGSoap.class, features);
    }

    /**
     * @return returns RecebeMSGHttpGet
     */
    @WebEndpoint(name = "recebeMSGHttpGet")
    public RecebeMSGHttpGet getRecebeMSGHttpGet() {
        return super.getPort(new QName(PropriedadeSiafem.URL_NAMESPACE, "recebeMSGHttpGet"), RecebeMSGHttpGet.class);
    }

    /**
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return returns RecebeMSGHttpGet
     */
    @WebEndpoint(name = "recebeMSGHttpGet")
    public RecebeMSGHttpGet getRecebeMSGHttpGet(WebServiceFeature... features) {
        return super.getPort(new QName(PropriedadeSiafem.URL_NAMESPACE, "recebeMSGHttpGet"), RecebeMSGHttpGet.class, features);
    }

    /**
     * @return returns RecebeMSGHttpPost
     */
    @WebEndpoint(name = "recebeMSGHttpPost")
    public RecebeMSGHttpPost getRecebeMSGHttpPost() {
        return super.getPort(new QName(PropriedadeSiafem.URL_NAMESPACE, "recebeMSGHttpPost"), RecebeMSGHttpPost.class);
    }

    /**
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return returns RecebeMSGHttpPost
     */
    @WebEndpoint(name = "recebeMSGHttpPost")
    public RecebeMSGHttpPost getRecebeMSGHttpPost(WebServiceFeature... features) {
        return super.getPort(new QName(PropriedadeSiafem.URL_NAMESPACE, "recebeMSGHttpPost"), RecebeMSGHttpPost.class, features);
    }

    private static URL __getWsdlLocation() {
        if (RECEBEMSG_EXCEPTION != null) {
            throw RECEBEMSG_EXCEPTION;
        }
        return RECEBEMSG_WSDL_LOCATION;
    }

}