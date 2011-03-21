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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.virgo.kernel.core.AbortableSignal;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.internal.AbortableSignalJunction;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.AbstractInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.ArtifactStateMonitor;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.util.common.Tree;

/**
 * A factory configuration is a "virtual" artifact that cannot be looked up individually in Repository, but acts as a
 * container for individual instances of configurations and manages their lifecycle.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread Safe
 */
public final class FactoryConfigInstallArtifact extends AbstractInstallArtifact {
        
    private final Object monitor = new Object();

    private Set<RepositoryAwareArtifactDescriptor> artifacts;

    /**
     * @throws DeploymentException
     */
    FactoryConfigInstallArtifact(@NonNull ArtifactIdentity identity, @NonNull ArtifactStateMonitor artifactStateMonitor, EventLogger eventLogger,
        @NonNull Set<RepositoryAwareArtifactDescriptor> artifacts) throws DeploymentException {

        // there is no repository name - as this artifact can contain artifacts from multiple repositories.
        super(identity, new NoOpArtifactStorage() , artifactStateMonitor, null, eventLogger);

        // make a copy of input
        this.artifacts = new HashSet<RepositoryAwareArtifactDescriptor>(artifacts);

    }

    protected final List<Tree<InstallArtifact>> getChildrenSnapshot() {
        List<Tree<InstallArtifact>> children = new ArrayList<Tree<InstallArtifact>>();
        synchronized (this.monitor) {
            children.addAll(getTree().getChildren());
        }
        return children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart(AbortableSignal signal) throws DeploymentException {
        List<Tree<InstallArtifact>> children = getChildrenSnapshot();
        int numChildren = children.size();

        // The SignalJunction constructor will drive the signal if numChildren == 0.
        AbortableSignalJunction signalJunction = new AbortableSignalJunction(signal, numChildren);

        logger.debug("Created {} that will notify {} to track start of {}", new Object[] { signalJunction, signal, this });

        List<AbortableSignal> subSignals = signalJunction.getSignals();

        for (int childIndex = 0; childIndex < numChildren && !signalJunction.failed(); childIndex++) {
            InstallArtifact childArtifact = children.get(childIndex).getValue();
            AbortableSignal subSignal = subSignals.get(childIndex);

            logger.debug("Starting {} with signal {} from {}", new Object[] { childArtifact, subSignal, signalJunction });

            childArtifact.start(subSignal);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStop() throws DeploymentException {
        DeploymentException firstFailure = null;
        for (Tree<InstallArtifact> child : getChildrenSnapshot()) {
            try {
                child.getValue().stop();
            } catch (DeploymentException e) {
                firstFailure = e;
            }
        }
        if (firstFailure != null) {
            throw firstFailure;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doUninstall() throws DeploymentException {
        DeploymentException firstFailure = null;
        for (Tree<InstallArtifact> child : getChildrenSnapshot()) {
            try {
                child.getValue().uninstall();
            } catch (DeploymentException e) {
                firstFailure = e;
            }
        }
        if (firstFailure != null) {
            throw firstFailure;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doRefresh() throws DeploymentException {
        return false;
    }

    public final Set<RepositoryAwareArtifactDescriptor> getArtifactDescriptors() {
        return Collections.unmodifiableSet(this.artifacts);
    }
}
