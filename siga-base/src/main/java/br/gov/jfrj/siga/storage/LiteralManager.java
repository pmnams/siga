package br.gov.jfrj.siga.storage;

import javax.enterprise.util.AnnotationLiteral;

public class LiteralManager extends AnnotationLiteral<Manager> implements Manager {

    private final StorageType type;

    public LiteralManager(StorageType type) {
        this.type = type;
    }

    @Override
    public StorageType type() {
        return type;
    }
}
