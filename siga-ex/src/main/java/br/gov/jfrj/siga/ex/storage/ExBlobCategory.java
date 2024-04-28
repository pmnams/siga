package br.gov.jfrj.siga.ex.storage;

import br.gov.jfrj.siga.storage.blob.BlobCategory;

public enum ExBlobCategory implements BlobCategory {
    DEFAULT(1),
    DOCUMENTS(2);

    public final int value;

    ExBlobCategory(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

}
