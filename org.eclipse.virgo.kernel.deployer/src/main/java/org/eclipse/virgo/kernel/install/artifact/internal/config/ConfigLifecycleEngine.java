/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact.internal.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.internal.RefreshEngine;
import org.eclipse.virgo.kernel.install.artifact.internal.RefreshException;
import org.eclipse.virgo.kernel.install.artifact.internal.StartEngine;
import org.eclipse.virgo.kernel.install.artifact.internal.StartException;
import org.eclipse.virgo.kernel.install.artifact.internal.StopEngine;
import org.eclipse.virgo.kernel.install.artifact.internal.StopException;
import org.eclipse.virgo.util.common.StringUtils;
import org.eclipse.virgo.util.io.IOUtils;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigLifecycleEngine implements StartEngine, RefreshEngine, StopEngine {

    static final String ARTIFACT_ID_PRIVATE_PROPERTY = ".org.eclispe.virgo.kernel.install.artifact.id";

    private static final String FILTER_FORMAT = "(%s=%s)";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConfigurationAdmin configurationAdmin;

    public ConfigLifecycleEngine(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }

    public void start(ArtifactIdentity artifactIdentity, ArtifactFS artifactFS) throws StartException {
        try {
            createConfiguration(artifactIdentity, artifactFS);
        } catch (Exception e) {
            String message = String.format("Unable to start configuration '%s' with '%s'", artifactIdentity.getName(), artifactFS);
            logger.error(message);
            throw new StartException(message, e);
        }
    }

    public void refresh(ArtifactIdentity artifactIdentity, ArtifactFS artifactFS) throws RefreshException {
        try {
            Configuration configuration = findExistingConfiguration(artifactIdentity);
            if (configuration == null) {
                throw new RuntimeException("Configuration not found");
            }
            Properties properties = getProperties(artifactFS);
            enhancePropertiesWithLookupKey(artifactIdentity, properties);
            configuration.update(properties);
        } catch (Exception e) {
            String message = String.format("Unable to refresh configuration '%s' with '%s'", artifactIdentity.getName(), artifactFS);
            logger.error(message);
            throw new RefreshException(message, e);
        }
    }

    public void stop(ArtifactIdentity artifactIdentity, ArtifactFS artifactFS) throws StopException {
        try {
            Configuration configuration = findExistingConfiguration(artifactIdentity);
            if (configuration == null) {
                throw new RuntimeException("Configuration is not found");
            }
            configuration.delete();
        } catch (Exception e) {
            String message = String.format("Unable to stop configuration '%s'", artifactIdentity.getName());
            logger.error(message);
            throw new StopException(message, e);
        }
    }

    private void createConfiguration(ArtifactIdentity artifactIdentity, ArtifactFS artifactFS) throws Exception {

        Properties properties = getProperties(artifactFS);
        Configuration configuration = null;
        String pid = properties.getProperty(ConfigurationAdmin.SERVICE_FACTORYPID);
        if (StringUtils.hasText(pid)) {
            configuration = configurationAdmin.createFactoryConfiguration(pid, null);
        } else {
            configuration = configurationAdmin.getConfiguration(artifactIdentity.getName(), null);
        }
        enhancePropertiesWithLookupKey(artifactIdentity, properties);
        configuration.update(properties);
    }

    private Configuration findExistingConfiguration(ArtifactIdentity artifactIdentity) throws Exception {

        Configuration configuration = null;

        Configuration[] listResults = this.configurationAdmin.listConfigurations(String.format(FILTER_FORMAT, ARTIFACT_ID_PRIVATE_PROPERTY,
            getLookupKeyValue(artifactIdentity)));

        if (listResults != null && listResults.length > 0) {
            configuration = listResults[0];
        }
        return configuration;
    }

    private Properties getProperties(ArtifactFS artifactFS) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = artifactFS.getEntry("").getInputStream();
            return getProperties(inputStream);
        } finally {
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    private Properties getProperties(InputStream inputStream) throws IOException {
        Properties p = new Properties();
        p.load(inputStream);
        return p;
    }

    private String getLookupKeyValue(ArtifactIdentity identity) {
        return identity.getName();
    }

    private void enhancePropertiesWithLookupKey(ArtifactIdentity artifactIdentity, Properties properties) {
        properties.setProperty(ARTIFACT_ID_PRIVATE_PROPERTY, getLookupKeyValue(artifactIdentity));
    }
}
