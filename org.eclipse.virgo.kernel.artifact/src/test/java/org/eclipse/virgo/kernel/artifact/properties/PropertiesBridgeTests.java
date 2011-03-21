/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.artifact.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.Set;

import org.eclipse.virgo.kernel.artifact.StubHashGenerator;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.Attribute;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 */
public class PropertiesBridgeTests {

    @Test
    public void testGeneratePropertiesFile() throws ArtifactGenerationException {
        PropertiesBridge bridge = new PropertiesBridge(new StubHashGenerator());
        ArtifactDescriptor result = bridge.generateArtifactDescriptor(new File("src/test/resources/properties/foo.properties"));
        assertNotNull(result);
    }

    @Test(expected = ArtifactGenerationException.class)
    public void testFileDoesNotExist() throws ArtifactGenerationException {
        PropertiesBridge bridge = new PropertiesBridge(new StubHashGenerator());

        File file = new File("src/test/resources/properties/not.exist.properties");
        bridge.generateArtifactDescriptor(file);
    }

    @Test
    public void testGenerateNotPropertiesFile() throws ArtifactGenerationException {
        PropertiesBridge bridge = new PropertiesBridge(new StubHashGenerator());
        ArtifactDescriptor descriptor = bridge.generateArtifactDescriptor(new File("src/test/resources/bar.noterties"));
        assertNull(descriptor);
    }

    @Test
    public void testGenerateWithFactoryPid() throws ArtifactGenerationException {
        final String factoryPid = "test.factory.pid";
        final File propertiesFile = new File("src/test/resources/properties/factoryPid.properties");

        final String expectedName = PropertiesBridge.generateFactoryPid(factoryPid, propertiesFile);

        PropertiesBridge bridge = new PropertiesBridge(new StubHashGenerator());
        ArtifactDescriptor descriptor = bridge.generateArtifactDescriptor(propertiesFile);

        // asserts
        assertNotNull(descriptor);
        assertEquals(expectedName, descriptor.getName());
        // only expect one attribute
        Set<Attribute> attrSet = descriptor.getAttribute(ConfigurationAdmin.SERVICE_FACTORYPID);
        assertEquals(1, attrSet.size());
        Attribute attr = attrSet.iterator().next();
        assertNotNull(factoryPid, attr.getValue());
    }

    @Test
    public void makeSureThatServicePidIsTakenFromTheFileProvidedProperties() throws ArtifactGenerationException {
        final String name = "service.pid.in.the.file";
        PropertiesBridge bridge = new PropertiesBridge(new StubHashGenerator());
        ArtifactDescriptor result = bridge.generateArtifactDescriptor(new File("src/test/resources/properties/with-service-pid.properties"));
        assertNotNull(result);
        assertEquals(name, result.getName());
    }
}
