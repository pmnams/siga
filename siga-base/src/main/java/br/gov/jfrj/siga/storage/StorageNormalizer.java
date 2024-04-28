package br.gov.jfrj.siga.storage;

import br.gov.jfrj.siga.storage.blob.BlobCategory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class StorageNormalizer {
    private EntityManagerFactory emf;
    private final Logger logger = Logger.getLogger(StorageNormalizer.class.getName());

    public StorageNormalizer(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private String getLogFilePath() {
        Format formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return "/opt/jboss/siga/logs/fieldmigrate" + "_" + formatter.format(new Date()) + ".log";
    }

    public void migrateFromEntitiesField(Map<Class<?>, String[]> entities, int maximumExecutionTime) {
        FileHandler fh;
        try {
            fh = new FileHandler(getLogFilePath());
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Não foi possível definir o arquivo de log para operação.");
            return;
        }

        try {
            for (Class<?> key : entities.keySet()) {
                processEntity(key, entities.get(key), System.currentTimeMillis() / 1000 + maximumExecutionTime);

                if (System.currentTimeMillis() / 1000 > maximumExecutionTime)
                    return;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            logger.info("Processo finalizado.");
            fh.close();
        }
    }

    private void processEntity(Class<?> clazz, String[] config, long limitTime) throws InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        AtomicLong usedMemory = new AtomicLong(runtime.totalMemory() - runtime.freeMemory());
        long memoryLimit = usedMemory.get() + (runtime.maxMemory() - usedMemory.get()) / 2;

        BlobCategory category = BlobCategory.enumOf(Integer.parseInt(config[3]));

        logger.info("Verificando entidade " + clazz.getName());
        BiFunction<Class<?>, String, Field> getField = (target, fieldName) -> {
            Field field;
            try {
                field = target.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                try {
                    field = target.getSuperclass().getDeclaredField(fieldName);
                } catch (NoSuchFieldException ex) {
                    throw new RuntimeException("O field " + fieldName + " não existe na classe " + target.getName());
                }
            }
            field.setAccessible(true);
            return field;
        };


        while (true) {
            AtomicBoolean stopAll = new AtomicBoolean(false);

            EntityManager em = emf.createEntityManager();
            Thread th = new Thread(() -> {
                try {
                    List<?> targets;
                    do {
                        CriteriaBuilder cb = em.getCriteriaBuilder();
                        CriteriaQuery<?> q2 = cb.createQuery(clazz);
                        Root<?> root2 = q2.from(clazz);
                        q2.where(
                                cb.and(
                                        cb.isNotNull(root2.get(config[0])),
                                        cb.isNull(root2.get(config[1]))
                                )
                        );

                        targets = em.createQuery(q2)
                                .setMaxResults(10)
                                .setHint("javax.persistence.cache.storeMode", "BYPASS")
                                .getResultList();

                        if (targets.isEmpty()) {
                            stopAll.set(true);
                            break;
                        }

                        for (Object target : targets) {
                            logger.info("Processando entidade: " + target);

                            Field sourceField = getField.apply(clazz, config[0]);
                            Field targetField = getField.apply(clazz, config[1]);
                            Field dateField = getField.apply(clazz, config[2]);

                            byte[] data = (byte[]) sourceField.get(target);
                            if (data == null)
                                continue;

                            SigaBlob sigaBlob = new SigaBlob(data);
                            sigaBlob.setCreatedAt((Date) dateField.get(target));
                            sigaBlob.setCategory(category);
                            targetField.set(target, sigaBlob);
                            sourceField.set(target, null);

                            em.getTransaction().begin();
                            em.persist(target);
                            em.getTransaction().commit();

                            logger.info("Entidade " + clazz.getSimpleName() + " processada com sucesso. Dados: " + target);

                            Thread.sleep(500);
                        }

                        usedMemory.set(runtime.totalMemory() - runtime.freeMemory());
                        if (usedMemory.get() > memoryLimit) {
                            return;
                        }

                        if (System.currentTimeMillis() / 1000 > limitTime) {
                            logger.info("Processo finalizado por limite de tempo.");
                            stopAll.set(true);
                            return;
                        }

                    } while (true);

                    logger.info("Não restam mais registros para realizar a operação.");
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Erro: " + e.getMessage());
                    stopAll.set(true);
                }
            }, "StorageNormalizer");
            th.start();
            th.join();
            em.close();

            Runtime.getRuntime().runFinalization();
            Runtime.getRuntime().gc();


            if (stopAll.get())
                break;
        }
    }
}
