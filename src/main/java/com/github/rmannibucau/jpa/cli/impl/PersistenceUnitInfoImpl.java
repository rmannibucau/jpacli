package com.github.rmannibucau.jpa.cli.impl;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {
    public static final ClassLoader LOADER = Thread.currentThread().getContextClassLoader();

    private String providerClassName = "org.apache.openjpa.persistence.PersistenceProviderImpl";
    private DataSource dataSource;
    private List<String> mappingFiles = new LinkedList<>();
    private List<URL> jarFileUrls = new LinkedList<>();
    private List<String> managedClasses = new LinkedList<>();
    private boolean excludeUnlistedClasses;
    private final Properties properties = new Properties();

    public PersistenceUnitInfoImpl() {
        properties.setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        properties.setProperty("openjpa.Log", "false");
    }

    public String getPersistenceUnitName() {
        return "cli";
    }

    public String getPersistenceProviderClassName() {
        return providerClassName;
    }

    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.RESOURCE_LOCAL;
    }

    public DataSource getJtaDataSource() {
        return dataSource;
    }

    public DataSource getNonJtaDataSource() {
        return dataSource;
    }

    public List<String> getMappingFileNames() {
        return mappingFiles;
    }

    public List<URL> getJarFileUrls() {
        return jarFileUrls;
    }

    public URL getPersistenceUnitRootUrl() {
        return null;
    }

    public List<String> getManagedClassNames() {
        return managedClasses;
    }

    public boolean excludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    public SharedCacheMode getSharedCacheMode() {
        return SharedCacheMode.NONE;
    }

    public ValidationMode getValidationMode() {
        return ValidationMode.AUTO;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getPersistenceXMLSchemaVersion() {
        return "2.0";
    }

    public ClassLoader getClassLoader() {
        return LOADER;
    }

    public void addTransformer(final ClassTransformer transformer) {
        // no-op
    }

    public ClassLoader getNewTempClassLoader() {
        return new URLClassLoader(new URL[0], LOADER);
    }

    public void setProviderClassName(final String providerClassName) {
        this.providerClassName = providerClassName;
    }

    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setExcludeUnlistedClasses(final boolean excludeUnlistedClasses) {
        this.excludeUnlistedClasses = excludeUnlistedClasses;
    }
}
