package br.gov.jfrj.siga.storage;

import br.gov.jfrj.siga.storage.blob.BlobCategory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@ApplicationScoped
public class StorageNormalizer {
    @Inject
    EntityManager em;

    private String getLogFilePath() {
        Format formatter = new SimpleDateFormat("ddMMyyyyHHmmss");
        return "/opt/jboss/siga/logs/fieldmigrate" + "_" + formatter.format(new Date()) + ".log";

    }

    public void migrateFromEntitiesField(Map<Class<?>, String[]> entities, int maximumExecutionTime) {
        final long maxTime = System.currentTimeMillis() / 1000 + maximumExecutionTime;
        Logger logger = Logger.getLogger(StorageNormalizer.class.getName());
        FileHandler fh;
        try {
            fh = new FileHandler(getLogFilePath());
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Não foi possível definir o arquivo de log para operação.");
            return;
        }

        for (Class<?> key : entities.keySet()) {
            try {
                BlobCategory category = BlobCategory.enumOf(Integer.parseInt(entities.get(key)[3]));

                logger.info("Verificando entidade " + key.getName());
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<Long> q = cb.createQuery(Long.class);

                Root<?> root = q.from(key);
                q.select(cb.count(root));
                q.where(
                        cb.and(
                                cb.isNotNull(root.get(entities.get(key)[0])),
                                cb.isNull(root.get(entities.get(key)[1]))
                        )
                );

                long results = em.createQuery(q).getSingleResult();

                if (results == 0) {
                    logger.info("Nada a fazer com essa entidade");
                    continue;
                }
                logger.info("Localizados " + results + " registros válidos para migração");


                BiFunction<Class<?>, String, Field> getField = (clazz, fieldName) -> {
                    Field field;
                    try {
                        field = clazz.getDeclaredField(fieldName);
                    } catch (NoSuchFieldException e) {
                        try {
                            field = clazz.getSuperclass().getDeclaredField(fieldName);
                        } catch (NoSuchFieldException ex) {
                            throw new RuntimeException("O field " + fieldName + " não existe na classe " + clazz.getName());
                        }
                    }
                    field.setAccessible(true);
                    return field;
                };

                long remaining = results;
                while (remaining > 0) {
                    if (System.currentTimeMillis() / 1000 > maxTime) {
                        logger.info("Processo finalizado por limite de tempo.");
                        return;
                    }

                    CriteriaQuery<?> q2 = cb.createQuery(key);
                    Root<?> root2 = q2.from(key);
                    q2.where(
                            cb.and(
                                    cb.isNotNull(root2.get(entities.get(key)[0])),
                                    cb.isNull(root2.get(entities.get(key)[1]))
                            )
                    );

                    List<?> targets = em.createQuery(q2).setMaxResults(20).getResultList();
                    if (targets.isEmpty())
                        continue;

                    for (Object target : targets) {
                        Field sourceField = getField.apply(key, entities.get(key)[0]);
                        Field targetField = getField.apply(key, entities.get(key)[1]);
                        Field dateField = getField.apply(key, entities.get(key)[2]);

                        byte[] data = (byte[]) sourceField.get(target);
                        if (data == null)
                            continue;

                        SigaBlob sigaBlob = new SigaBlob(data);
                        sigaBlob.setCreatedAt((Date) dateField.get(target));
                        sigaBlob.setCategory(category);
                        targetField.set(target, sigaBlob);

                        em.getTransaction().begin();
                        em.persist(target);
                        em.getTransaction().commit();
                        em.getTransaction().begin();
                        sourceField.set(target, null);
                        em.persist(target);
                        em.getTransaction().commit();

                        logger.info("Entidade " + key.getSimpleName() + " processada com sucesso. Dados: " + target);
                        Thread.sleep(500);
                    }
                    remaining -= 20;
                }
                logger.info("Não restam mais registros para realizar a operação.");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erro: " + e.getMessage());
            }
        }
        logger.info("Processo finalizado.");
        fh.close();
    }
}
