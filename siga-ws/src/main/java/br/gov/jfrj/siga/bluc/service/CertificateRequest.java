package br.gov.jfrj.siga.bluc.service;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CertificateRequest {
    public String certificate;

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }
}
