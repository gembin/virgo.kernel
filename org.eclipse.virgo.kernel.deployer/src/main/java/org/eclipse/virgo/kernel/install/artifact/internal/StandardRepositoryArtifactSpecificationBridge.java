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

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.install.artifact.ArtifactSpecificationBridge;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;

/**
 * Implementation of {@link ArtifactSpecificationBridge} that is backed by {@link Repository}
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread Safe
 */
final class StandardRepositoryArtifactSpecificationBridge implements ArtifactSpecificationBridge {

    private final Repository repository;

    StandardRepositoryArtifactSpecificationBridge(@NonNull Repository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RepositoryAwareArtifactDescriptor generateArtifactDescriptor(ArtifactSpecification artifactSpecification) {
        return this.repository.get(artifactSpecification.getType(), artifactSpecification.getName(), artifactSpecification.getVersionRange());
    }

}
