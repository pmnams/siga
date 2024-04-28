package br.gov.jfrj.siga.storage;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Manager {

    StorageType value();

    class Literal extends AnnotationLiteral<Manager> implements Manager {

        private final StorageType value;

        public Literal(StorageType value) {
            this.value = value;
        }

        @Override
        public StorageType value() {
            return value;
        }
    }
}
