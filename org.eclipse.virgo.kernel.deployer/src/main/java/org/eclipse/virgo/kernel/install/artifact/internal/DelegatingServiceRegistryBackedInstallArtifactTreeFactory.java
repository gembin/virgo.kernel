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

package org.eclipse.virgo.kernel.install.artifact.internal;

import java.util.Map;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactTreeFactory;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.util.common.Tree;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * An {@link InstallArtifactTreeFactory} that delegates to the <code>InstallArtifactTreeFactory</code>s available in the
 * OSGi service registry with fall back on default implementation.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 */
final class DelegatingServiceRegistryBackedInstallArtifactTreeFactory implements InstallArtifactTreeFactory {

    private static final InstallArtifactTreeFactory[] EMPTY_TRACKED_ARRAY = new InstallArtifactTreeFactory[] {};

    private final ServiceTracker<InstallArtifactTreeFactory, InstallArtifactTreeFactory> serviceTracker;

    /**
     * @param bundleContext
     */
    public DelegatingServiceRegistryBackedInstallArtifactTreeFactory(@NonNull BundleContext bundleContext) {
        this.serviceTracker = new ServiceTracker<InstallArtifactTreeFactory, InstallArtifactTreeFactory>(bundleContext,
            InstallArtifactTreeFactory.class.getName(), null);
    }

    public void init() {
        this.serviceTracker.open();
    }

    public void destroy() {
        this.serviceTracker.close();
    }

    /**
     * {@inheritDoc}
     */
    public Tree<InstallArtifact> constructInstallArtifactTree(ArtifactIdentity artifactIdentity, ArtifactStorage artifactStorage,
        Map<String, String> deploymentProperties, String repositoryName) throws DeploymentException {

        Tree<InstallArtifact> tree = null;
        InstallArtifactTreeFactory[] services = this.serviceTracker.getServices(EMPTY_TRACKED_ARRAY);

        if (services != null) {
            for (InstallArtifactTreeFactory service : services) {
                if (service != null) {
                    tree = service.constructInstallArtifactTree(artifactIdentity, artifactStorage, deploymentProperties, repositoryName);
                    if (tree != null) {
                        break;
                    }
                }
            }
        }

        return tree;
    }

}
