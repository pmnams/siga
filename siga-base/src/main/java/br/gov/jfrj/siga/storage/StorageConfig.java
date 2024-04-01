package br.gov.jfrj.siga.storage;

import br.gov.jfrj.siga.base.Prop;
import br.gov.jfrj.siga.storage.blob.BlobCategory;

import javax.enterprise.context.ApplicationScoped;
import java.util.stream.Stream;

@ApplicationScoped
public class StorageConfig {
    private BlobCategory[] categories = null;

    public Class<? extends BlobCategory> getCategoryEnumType() {
        return BlobCategory.DefaultBlobCategory.class;
    }

    public BlobCategory getStorageType(int code) {
        if (categories == null)
            categories = getCategoryEnumType().getEnumConstants();

        return Stream.of(categories)
                .filter(category -> category.getValue() == code)
                .findAny()
                .orElse(BlobCategory.DefaultBlobCategory.DEFAULT);
    }
}
