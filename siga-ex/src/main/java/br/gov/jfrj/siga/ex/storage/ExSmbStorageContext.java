package br.gov.jfrj.siga.ex.storage;

import br.gov.jfrj.siga.storage.smb.SmbStorageContext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;

@ApplicationScoped
@Specializes
public class ExSmbStorageContext extends SmbStorageContext {
    @Override
    public String getContextName() {
        return "sigaex";
    }
}
