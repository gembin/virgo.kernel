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

package org.eclipse.virgo.kernel.model.internal.deployer;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;


import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.deployer.model.RuntimeArtifactModel;
import org.eclipse.virgo.kernel.model.StubArtifactRepository;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.kernel.model.internal.deployer.ModelInstallArtifactLifecycleListenerInitializer;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
import org.eclipse.virgo.kernel.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.kernel.stubs.StubInstallArtifact;
import org.eclipse.virgo.kernel.stubs.StubPlanInstallArtifact;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.support.TrueFilter;

public class ModelInstallArtifactLifecycleListenerInitializerTests {

    private final StubArtifactRepository artifactRepository = new StubArtifactRepository();

    private final RuntimeArtifactModel runtimeArtifactModel = createMock(RuntimeArtifactModel.class);
    
    private final RegionDigraph regionDigraph = createMock(RegionDigraph.class);

    private final StubBundleContext bundleContext;
    {
        this.bundleContext = new StubBundleContext();
        String filterString1 = String.format("(&(objectClass=%s)(artifactType=bundle))", DependencyDeterminer.class.getCanonicalName());
        this.bundleContext.addFilter(filterString1, new TrueFilter(filterString1));
        String filterString2 = String.format("(&(objectClass=%s)(artifactType=plan))", DependencyDeterminer.class.getCanonicalName());
        this.bundleContext.addFilter(filterString2, new TrueFilter(filterString2));
    }

    private final ModelInstallArtifactLifecycleListenerInitializer initializer = new ModelInstallArtifactLifecycleListenerInitializer(
        artifactRepository, bundleContext, runtimeArtifactModel, regionDigraph);

    @Test(expected = FatalAssertionException.class)
    public void nullArtifactRepository() {
        new ModelInstallArtifactLifecycleListenerInitializer(null, bundleContext, runtimeArtifactModel, regionDigraph);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullBundleContext() {
        new ModelInstallArtifactLifecycleListenerInitializer(artifactRepository, null, runtimeArtifactModel, regionDigraph);
    }
    
    @Test(expected = FatalAssertionException.class)
    public void nullRegionDigraph() {
        new ModelInstallArtifactLifecycleListenerInitializer(artifactRepository, bundleContext, runtimeArtifactModel, null);
    }

    @Test
    public void initialize() throws IOException, InvalidSyntaxException {
        expect(this.runtimeArtifactModel.getDeploymentIdentities()).andReturn(
            new DeploymentIdentity[] { new StubDeploymentIdentity("plan"), new StubDeploymentIdentity("bundle") });
        expect(this.runtimeArtifactModel.get(isA(DeploymentIdentity.class))).andReturn(new StubInstallArtifact("bundle"));
        expect(this.runtimeArtifactModel.get(isA(DeploymentIdentity.class))).andReturn(new StubPlanInstallArtifact());
        replay(this.runtimeArtifactModel);
        assertEquals(0, this.bundleContext.getServiceRegistrations().size());
        this.initializer.initialize();
        assertEquals(1, this.bundleContext.getServiceRegistrations().size());
        assertEquals(2, this.artifactRepository.getArtifacts().size());
        verify(this.runtimeArtifactModel);
    }

    @Test
    public void destroy() throws IOException, InvalidSyntaxException {
        expect(this.runtimeArtifactModel.getDeploymentIdentities()).andReturn(new DeploymentIdentity[0]);
        replay(this.runtimeArtifactModel);
        this.initializer.initialize();
        assertEquals(1, this.bundleContext.getServiceRegistrations().size());
        this.initializer.destroy();
        assertEquals(0, this.bundleContext.getServiceRegistrations().size());
        verify(this.runtimeArtifactModel);
    }

    private static class StubDeploymentIdentity implements DeploymentIdentity {

        private static final long serialVersionUID = 1L;

        private final String type;

        public StubDeploymentIdentity(String type) {
            this.type = type;
        }

        public String getSymbolicName() {
            return "test";
        }

        public String getType() {
            return this.type;
        }

        public String getVersion() {
            return Version.emptyVersion.toString();
        }

    }
}
