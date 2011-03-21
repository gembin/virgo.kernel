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

import java.io.File;
import java.net.URI;
import java.util.List;

import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorageFactory;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.framework.OsgiServiceHolder;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.osgi.framework.BundleContext;

/**
 * An {@link ArtifactStorageFactory} that delegates to the <code>ArtifactStorageFactory</code>s available in the OSGi
 * service registry with fall back on default implementation.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 */
final class DelegatingServiceRegistryBackedArtifactStorageFactory implements ArtifactStorageFactory {

    private final BundleContext context;

    /*
     * This is a default implementation provided by the configuration.  
     * Otherwise some way of ordering must devised (i.e. service ranking and etc).
     * That might not be 100% reliable. 
     */
    private final ArtifactStorageFactory defaultArtifactStorageFactory;

    /**
     * @param bundleContext
     */
    public DelegatingServiceRegistryBackedArtifactStorageFactory(@NonNull BundleContext bundleContext, @NonNull ArtifactStorageFactory defaultFactory) {
        this.context = bundleContext;
        this.defaultArtifactStorageFactory = defaultFactory;
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public ArtifactStorage create(URI artifact, ArtifactIdentity artifactIdentity) {
        ArtifactStorage result = null;

        List<OsgiServiceHolder<ArtifactStorageFactory>> serviceHolders = OsgiFrameworkUtils.getServices(this.context, ArtifactStorageFactory.class);
        for (OsgiServiceHolder<ArtifactStorageFactory> holder : serviceHolders) {

            ArtifactStorageFactory factory = holder.getService();
            try {
                if (factory != null) {
                    result = factory.create(artifact, artifactIdentity);
                    if (result != null) {
                        break;
                    }
                }
            } finally {
                this.context.ungetService(holder.getServiceReference());
            }
        }
        return result != null ? result : this.defaultArtifactStorageFactory.create(artifact, artifactIdentity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArtifactStorage create(File artifact, ArtifactIdentity artifactIdentity) {
        ArtifactStorage result = null;

        List<OsgiServiceHolder<ArtifactStorageFactory>> serviceHolders = OsgiFrameworkUtils.getServices(this.context, ArtifactStorageFactory.class);
        for (OsgiServiceHolder<ArtifactStorageFactory> holder : serviceHolders) {

            ArtifactStorageFactory factory = holder.getService();
            try {
                if (factory != null) {
                    result = factory.create(artifact, artifactIdentity);
                    if (result != null) {
                        break;
                    }
                }
            } finally {
                this.context.ungetService(holder.getServiceReference());
            }
        }
        return result != null ? result : this.defaultArtifactStorageFactory.create(artifact, artifactIdentity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArtifactStorage createDirectoryStorage(ArtifactIdentity artifactIdentity, String directoryName) {
        ArtifactStorage result = null;

        List<OsgiServiceHolder<ArtifactStorageFactory>> serviceHolders = OsgiFrameworkUtils.getServices(this.context, ArtifactStorageFactory.class);
        for (OsgiServiceHolder<ArtifactStorageFactory> holder : serviceHolders) {

            ArtifactStorageFactory factory = holder.getService();
            try {
                if (factory != null) {
                    result = factory.createDirectoryStorage(artifactIdentity, directoryName);
                    if (result != null) {
                        break;
                    }
                }
            } finally {
                this.context.ungetService(holder.getServiceReference());
            }
        }
        return result != null ? result : this.defaultArtifactStorageFactory.createDirectoryStorage(artifactIdentity, directoryName);
    }
}
