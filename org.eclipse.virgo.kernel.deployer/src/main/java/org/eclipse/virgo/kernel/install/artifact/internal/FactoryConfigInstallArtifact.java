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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.virgo.kernel.core.AbortableSignal;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.internal.AbortableSignalJunction;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.util.common.Tree;

/**
 * TODO Document FactoryConfigInstallArtifact
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * TODO Document concurrent semantics of FactoryConfigInstallArtifact
 */
public final class FactoryConfigInstallArtifact extends AbstractInstallArtifact {

    private final Object monitor = new Object();

    private Set<RepositoryAwareArtifactDescriptor> artifacts;

    /**
     * @throws DeploymentException
     */
    FactoryConfigInstallArtifact(@NonNull ArtifactIdentity identity, @NonNull ArtifactStorage artifactStorage,
        @NonNull ArtifactStateMonitor artifactStateMonitor, EventLogger eventLogger, @NonNull Set<RepositoryAwareArtifactDescriptor> artifacts)
        throws DeploymentException {

        super(identity, artifactStorage, artifactStateMonitor, null, eventLogger);

        // make a copy of input list
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
        // TODO: potentially re-fetch configurations from repository and doStop/doStart on those???
        return false;
    }

    public final Set<RepositoryAwareArtifactDescriptor> getArtifactDescriptors() {
        return Collections.unmodifiableSet(this.artifacts);
    }
}
