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

package org.eclipse.virgo.kernel.deployer.core.internal;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import static org.easymock.EasyMock.*;


import org.eclipse.virgo.kernel.deployer.core.internal.BundleDeploymentPropertiesTransformer;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.util.common.ThreadSafeArrayListTree;
import org.eclipse.virgo.util.common.Tree;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;


public class BundleDeploymentPropertiesTransformerTests {

    @Test
    public void testTransformWithWebContextPath() throws Exception {
        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        
        Map<String, String> props = new HashMap<String, String>();
        props.put("header:Web-ContextPath", "/foo");
        
        BundleInstallArtifact artifact = createMock(BundleInstallArtifact.class);
        expect(artifact.getDeploymentProperties()).andReturn(props);
        expect(artifact.getBundleManifest()).andReturn(manifest);
        
        replay(artifact);
        Tree<InstallArtifact> tree = new ThreadSafeArrayListTree<InstallArtifact>(artifact);
        
        BundleDeploymentPropertiesTransformer transformer = new BundleDeploymentPropertiesTransformer();
        transformer.transform(tree, null);
        verify(artifact);
        
        assertEquals("/foo", manifest.getHeader("Web-ContextPath"));
    }
    
    @Test
    public void testTransformWithNullProperties() throws Exception {
        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        
        BundleInstallArtifact artifact = createMock(BundleInstallArtifact.class);
        expect(artifact.getDeploymentProperties()).andReturn(null);
        
        replay(artifact);
        Tree<InstallArtifact> tree = new ThreadSafeArrayListTree<InstallArtifact>(artifact);
        
        BundleDeploymentPropertiesTransformer transformer = new BundleDeploymentPropertiesTransformer();
        transformer.transform(tree, null);
        verify(artifact);
        
        assertNull(manifest.getHeader("Web-ContextPath"));
    }
    
    @Test
    public void testTransformWithNonHeaderProperties() throws Exception {
        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        
        Map<String, String> props = new HashMap<String, String>();
        props.put("Web-ContextPath", "/foo");
        
        BundleInstallArtifact artifact = createMock(BundleInstallArtifact.class);
        expect(artifact.getBundleManifest()).andReturn(manifest);
        expect(artifact.getDeploymentProperties()).andReturn(props);
        
        replay(artifact);
        Tree<InstallArtifact> tree = new ThreadSafeArrayListTree<InstallArtifact>(artifact);
        
        BundleDeploymentPropertiesTransformer transformer = new BundleDeploymentPropertiesTransformer();
        transformer.transform(tree, null);
        verify(artifact);
        
        assertNull(manifest.getHeader("Web-ContextPath"));
    }
}
