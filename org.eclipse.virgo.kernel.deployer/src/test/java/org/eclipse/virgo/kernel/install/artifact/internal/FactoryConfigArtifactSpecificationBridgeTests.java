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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.internal.config.FactoryConfigArtifactSpecificationLookup;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.util.osgi.VersionRange;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for FactoryConfigArtifactSpecificationBridge
 * <p />
 */
public class FactoryConfigArtifactSpecificationBridgeTests {

    private FactoryConfigArtifactSpecificationLookup bridge;

    @Before
    public void onSetUp() {
        bridge = new FactoryConfigArtifactSpecificationLookup();
    }

    @Test
    public void validateArtifactType() {

        final String artifactType = ArtifactIdentityDeterminer.CONFIGURATION_TYPE;
        ArtifactSpecification spec = new ArtifactSpecification(artifactType, "spec-1", VersionRange.naturalNumberRange());

        assertNull(bridge.lookup(spec));
    }

    @Test
    public void validateGenerationOfFactryContainer() {
        final String artifactType = ArtifactIdentityDeterminer.FACTORY_CONFIGURATION_TYPE;
        ArtifactSpecification spec = new ArtifactSpecification(artifactType, "spec-1", VersionRange.naturalNumberRange());

        RepositoryAwareArtifactDescriptor descriptor = bridge.lookup(spec);

        assertNotNull(descriptor);
        assertEquals(artifactType, descriptor.getType());
        assertEquals("spec-1", descriptor.getName());
        assertEquals(VersionRange.naturalNumberRange().toParseString(), descriptor.getVersion().toString());
        assertNotNull(descriptor.getUri());
    }
}
