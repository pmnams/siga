package br.gov.jfrj.siga.storage;

public enum StorageType {
    DATABASE(1);

    public final int type;

    StorageType(int type) {
        this.type = type;
    }
}
