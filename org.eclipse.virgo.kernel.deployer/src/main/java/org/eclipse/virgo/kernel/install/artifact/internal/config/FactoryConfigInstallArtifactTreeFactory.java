/*
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2011 Chariot Solutions, LLC
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    dsklyut - initial contribution
 */

package org.eclipse.virgo.kernel.install.artifact.internal.config;

import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.kernel.deployer.core.DeployerLogEvents;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactTreeFactory;
import org.eclipse.virgo.kernel.install.artifact.internal.ArtifactStateMonitor;
import org.eclipse.virgo.kernel.install.artifact.internal.StandardArtifactStateMonitor;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Query;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.util.common.ThreadSafeArrayListTree;
import org.eclipse.virgo.util.common.Tree;
import org.eclipse.virgo.util.osgi.VersionRange;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * {@link FactoryConfigInstallArtifactTreeFactory} is an {@link InstallArtifactTreeFactory} for crating container that
 * holds ConfigurationAdmin factory configuration properties file {@link InstallArtifact InstallArtifacts}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 */
class FactoryConfigInstallArtifactTreeFactory implements InstallArtifactTreeFactory {

    private static final String ARTIFACT_TYPE = ArtifactIdentityDeterminer.FACTORY_CONFIGURATION_TYPE;

    private final Repository repository;

    private final EventLogger eventLogger;

    private final BundleContext bundleContext;

    FactoryConfigInstallArtifactTreeFactory(@NonNull BundleContext bundleContext, @NonNull Repository repository, @NonNull EventLogger eventLogger) {
        this.bundleContext = bundleContext;
        this.repository = repository;
        this.eventLogger = eventLogger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tree<InstallArtifact> constructInstallArtifactTree(ArtifactIdentity artifactIdentity, ArtifactStorage artifactStorage,
        Map<String, String> deploymentProperties, String repositoryName) throws DeploymentException {

        if (!ARTIFACT_TYPE.equals(artifactIdentity.getType())) {
            return null;
        }
        final VersionRange versionRange = new VersionRange(artifactIdentity.getVersion().toString());

        // TODO: do we allow installation if there are no factory configurations in repository?
        // TODO: we can provide a property to control this behavior (i.e. factory-configuration.deployment.strict = true/false) with true as default.
        // TODO: this could be useful as configurations can be deployed already through pickup, kernel/config directory or programmatically. 
        Set<RepositoryAwareArtifactDescriptor> factoryConfigurationArtifacts = lookupFactoryConfigurations(artifactIdentity, versionRange);
        if (factoryConfigurationArtifacts.isEmpty()) {
            // TODO: do we need a more descriptive message here???
            // TODO: should this be postponed till transformer kicks in???
            this.eventLogger.log(DeployerLogEvents.ARTIFACT_NOT_FOUND, artifactIdentity.getType(), artifactIdentity.getName(), versionRange,
                this.repository.getName());
            throw new DeploymentException(artifactIdentity.getType() + " '" + artifactIdentity.getName() + "' version '" + versionRange
                + "' not found");
        }
        ArtifactStateMonitor artifactStateMonitor = new StandardArtifactStateMonitor(this.bundleContext);
        FactoryConfigInstallArtifact result = new FactoryConfigInstallArtifact(artifactIdentity, artifactStateMonitor, eventLogger,
            factoryConfigurationArtifacts);
        return constructInstallTree(result);
    }

    /**
     * @param artifactIdentity
     * @param versionRange
     * @return
     */
    private Set<RepositoryAwareArtifactDescriptor> lookupFactoryConfigurations(ArtifactIdentity artifactIdentity, VersionRange versionRange) {

        Query query = repository.createQuery(ArtifactDescriptor.TYPE, ArtifactIdentityDeterminer.CONFIGURATION_TYPE).addFilter(
            ConfigurationAdmin.SERVICE_FACTORYPID, artifactIdentity.getName()).setVersionRangeFilter(versionRange,
            Query.VersionRangeMatchingStrategy.HIGHEST);

        return query.run();
    }

    private Tree<InstallArtifact> constructInstallTree(InstallArtifact rootArtifact) {
        return new ThreadSafeArrayListTree<InstallArtifact>(rootArtifact);
    }

}
