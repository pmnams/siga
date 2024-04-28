package br.gov.jfrj.siga.ex.storage;

import br.gov.jfrj.siga.storage.blob.BlobCategory;
import br.gov.jfrj.siga.storage.StorageConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;

@Specializes
@ApplicationScoped
public class ExBlobConfig extends StorageConfig {

    public Class<? extends BlobCategory> getCategoryEnumType() {
        return ExBlobCategory.class;
    }
}
