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

import java.util.List;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.artifact.ArtifactSpecificationLookupStrategy;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.framework.OsgiServiceHolder;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.osgi.framework.BundleContext;

/**
 * An {@link ArtifactSpecificationLookupStrategy} that delegates to the <code>ArtifactSpecificationBridge</code>s
 * available in the OSGi service registry with fall back on default implementation.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 */
final class DelegatingServiceRegistryBackedArtifactSpecificationLookup implements ArtifactSpecificationLookupStrategy {

    private final BundleContext bundleContext;

    /*
     * This is a default implementation provided by the configuration. Otherwise some way of ordering must devised (i.e.
     * service ranking and etc). That might not be 100% reliable.
     */
    private final ArtifactSpecificationLookupStrategy defaultLookupStrategy;

    /**
     * @param bundleContext
     */
    public DelegatingServiceRegistryBackedArtifactSpecificationLookup(BundleContext bundleContext,
        ArtifactSpecificationLookupStrategy defaultLookupStrategy) {
        this.bundleContext = bundleContext;
        this.defaultLookupStrategy = defaultLookupStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RepositoryAwareArtifactDescriptor lookup(ArtifactSpecification artifactSpecification) {

        RepositoryAwareArtifactDescriptor result = lookupInServiceRegistry(artifactSpecification);

        return result != null ? result : this.defaultLookupStrategy.lookup(artifactSpecification);

    }

    /**
     * @param artifactSpecification
     * @return
     */
    private RepositoryAwareArtifactDescriptor lookupInServiceRegistry(ArtifactSpecification artifactSpecification) {
        RepositoryAwareArtifactDescriptor result = null;
        List<OsgiServiceHolder<ArtifactSpecificationLookupStrategy>> bridgeHolders = OsgiFrameworkUtils.getServices(this.bundleContext,
            ArtifactSpecificationLookupStrategy.class);

        for (OsgiServiceHolder<ArtifactSpecificationLookupStrategy> holder : bridgeHolders) {

            ArtifactSpecificationLookupStrategy bridge = holder.getService();
            try {
                if (bridge != null) {
                    result = bridge.lookup(artifactSpecification);
                    if (result != null) {
                        break;
                    }
                }
            } finally {
                this.bundleContext.ungetService(holder.getServiceReference());
            }
        }
        return result;
    }
}
