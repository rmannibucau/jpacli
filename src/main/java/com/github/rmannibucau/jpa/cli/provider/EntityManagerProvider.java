package com.github.rmannibucau.jpa.cli.provider;

import java.util.HashMap;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;

public class EntityManagerProvider {
    public EntityManagerFactory newFactory(final PersistenceUnitInfo info) {
        try {
            return PersistenceProvider.class.cast(Thread.currentThread().getContextClassLoader().loadClass(info.getPersistenceProviderClassName()).newInstance())
                    .createContainerEntityManagerFactory(info, new HashMap());
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
