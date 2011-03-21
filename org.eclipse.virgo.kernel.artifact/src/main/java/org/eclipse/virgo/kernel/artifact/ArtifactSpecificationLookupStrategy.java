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

package org.eclipse.virgo.kernel.artifact;

import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;


/**
* <p>
 * Implementations of this interface should have knowledge of how to lookup {@link RepositoryAwareArtifactDescriptor} 
 * based on the {@link ArtifactSpecification}.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be thread-safe.
 *  
 */
public interface ArtifactSpecificationLookupStrategy {

    RepositoryAwareArtifactDescriptor lookup(ArtifactSpecification artifactSpecification);
}
