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
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;


/**
* <p>
 * Implementations of this interface should have knowledge of how to generate {@link RepositoryAwareArtifactDescriptor} 
 * based on the {@link ArtifactSpecification}.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be thread-safe.
 * 
 * TODO: Validate usefulness of this interface.  Currently it is used to abstract look up ArtifactDescriptors in StandardInstallArtifactTreeInclosure
 * 
 */
public interface ArtifactSpecificationBridge {

    RepositoryAwareArtifactDescriptor generateArtifactDescriptor(ArtifactSpecification artifactSpecification);
}
