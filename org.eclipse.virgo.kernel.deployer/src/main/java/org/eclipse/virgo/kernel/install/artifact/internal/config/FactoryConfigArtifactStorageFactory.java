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

import java.io.File;
import java.net.URI;

import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorageFactory;

/**
 * {@link ArtifactStorageFactory} specific to {@link FactoryConfigInstallArtifact}. Factory Configuration is a virtual
 * artifact that does not have a source file and does not need to be managed in the staging locations
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread Safe
 */
final class FactoryConfigArtifactStorageFactory implements ArtifactStorageFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public ArtifactStorage create(File artifact, ArtifactIdentity artifactIdentity) {
        return createInternal(artifactIdentity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArtifactStorage createDirectoryStorage(ArtifactIdentity artifactIdentity, String directoryName) {
        return createInternal(artifactIdentity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArtifactStorage create(URI artifact, ArtifactIdentity artifactIdentity) {
        return createInternal(artifactIdentity);
    }

    private ArtifactStorage createInternal(ArtifactIdentity artifactIdentity) {
        if (ArtifactIdentityDeterminer.FACTORY_CONFIGURATION_TYPE.equals(artifactIdentity.getType())) {
            return new NoOpArtifactStorage();
        }
        return null;
    }
}
