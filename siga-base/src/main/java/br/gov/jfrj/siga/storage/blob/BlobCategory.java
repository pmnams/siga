package br.gov.jfrj.siga.storage.blob;

import br.gov.jfrj.siga.storage.StorageConfig;

import javax.enterprise.inject.spi.CDI;

public interface BlobCategory {
    int ordinal();

    Wrapper wrapper = new Wrapper();

    class Wrapper {
        private StorageConfig value = null;

        public StorageConfig getValue() {
            return value;
        }

        public void setValue(StorageConfig value) {
            this.value = value;
        }
    }

    enum DefaultBlobCategory implements BlobCategory {
        DEFAULT(1);

        public final int value;

        DefaultBlobCategory(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    static BlobCategory enumOf(int value) {
        StorageConfig config = wrapper.getValue();

        if (config == null) {
            try {
                config = CDI.current().select(StorageConfig.class).get();
                wrapper.setValue(config);
            } catch (Exception ignored) {
                return DefaultBlobCategory.DEFAULT;
            }
        }

        return config.getStorageType(value);
    }

    int getValue();
}