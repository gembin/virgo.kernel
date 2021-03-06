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

package org.eclipse.virgo.kernel.install.pipeline.stage.transform.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import org.junit.Test;
import org.osgi.framework.Version;


import org.eclipse.virgo.kernel.artifact.fs.StandardArtifactFSFactory;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactTreeFactory;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.ArtifactStorageFactory;
import org.eclipse.virgo.kernel.install.artifact.internal.StandardArtifactStorageFactory;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.ScopeNameFactory;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.internal.SyntheticContextBundleCreatingTransformer;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.util.common.ThreadSafeArrayListTree;
import org.eclipse.virgo.util.common.Tree;
import org.eclipse.virgo.util.io.PathReference;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.ImportedBundle;

/**
 */
public final class SyntheticContextBundleCreatingTransformerTests {

    private final InstallArtifactTreeFactory installArtifactTreeFactory = createMock(InstallArtifactTreeFactory.class);

    private final InstallEnvironment installEnvironment = createMock(InstallEnvironment.class);

    private final ArtifactStorageFactory artifactStorageFactory = new StandardArtifactStorageFactory(new PathReference("target/work"),
        new StandardArtifactFSFactory(), new MockEventLogger());

    private final Transformer transformer = new SyntheticContextBundleCreatingTransformer(this.installArtifactTreeFactory,
        this.artifactStorageFactory);

    @SuppressWarnings("unchecked")
    @Test
    public void basicSyntheticContextCreation() throws DeploymentException, FileNotFoundException, IOException {
        Tree<InstallArtifact> planInstallTree = createMockPlan(true, new Version(1, 0, 0), "plan-name", "bundle1", "bundle2", "bundle3");
        InstallArtifact syntheticContextInstallArtifact = createMock(InstallArtifact.class);

        File syntheticBundleDir = new File("target/work/staging/plan-name-1/bundle/plan-name-1-synthetic.context/1.0.0/plan-name-1-synthetic.context.jar").getAbsoluteFile();
        expect(
            this.installArtifactTreeFactory.constructInstallArtifactTree(eq(new ArtifactIdentity("bundle", "plan-name-1-synthetic.context",
                new Version(1, 0, 0), ScopeNameFactory.createScopeName("plan-name", new Version(1, 0, 0)))), isA(ArtifactStorage.class),
                (Map<String, String>) isNull(), (String) isNull())).andReturn(
            new ThreadSafeArrayListTree<InstallArtifact>(syntheticContextInstallArtifact));

        replay(this.installEnvironment, this.installArtifactTreeFactory);

        this.transformer.transform(planInstallTree, this.installEnvironment);

        verify(this.installEnvironment, this.installArtifactTreeFactory);

        File manifest = new File(syntheticBundleDir, JarFile.MANIFEST_NAME);
        assertTrue(manifest.exists());

        assertBundlesImported(manifest, "bundle1", "bundle2", "bundle3");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void nestedPlanSyntheticContextCreation() throws DeploymentException, FileNotFoundException, IOException {
        Tree<InstallArtifact> rootPlanInstallTree = createMockPlan(true, new Version(1, 0, 0), "plan-name", "bundle1");
        rootPlanInstallTree.addChild(createMockPlan(true, new Version(1, 0, 0), "nested-plan", "bundle2", "bundle3"));

        InstallArtifact syntheticContextInstallArtifact = createMock(InstallArtifact.class);

        File syntheticBundleDir = new File("target/work/staging/plan-name-1/bundle/plan-name-1-synthetic.context/1.0.0/plan-name-1-synthetic.context.jar").getAbsoluteFile();
        expect(
            this.installArtifactTreeFactory.constructInstallArtifactTree(eq(new ArtifactIdentity("bundle", "plan-name-1-synthetic.context",
                new Version(1, 0, 0), ScopeNameFactory.createScopeName("plan-name", new Version(1, 0, 0)))), isA(ArtifactStorage.class),
                (Map<String, String>) isNull(), (String) isNull())).andReturn(
            new ThreadSafeArrayListTree<InstallArtifact>(syntheticContextInstallArtifact));

        replay(this.installEnvironment, this.installArtifactTreeFactory);

        this.transformer.transform(rootPlanInstallTree, this.installEnvironment);

        verify(this.installEnvironment, this.installArtifactTreeFactory);

        File manifest = new File(syntheticBundleDir, JarFile.MANIFEST_NAME);
        assertTrue(manifest.exists());

        assertBundlesImported(manifest, "bundle1", "bundle2", "bundle3");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void syntheticContextOnlyCreatedForScopedPlans() throws DeploymentException, FileNotFoundException, IOException {
        Tree<InstallArtifact> rootPlanInstallTree = createMockPlan(false, new Version(1, 0, 0), "plan-name", "bundle1");
        // This test need not use TreeUtils.addChild
        rootPlanInstallTree.addChild(createMockPlan(true, new Version(1, 0, 0), "nested-plan", "bundle2", "bundle3"));

        InstallArtifact syntheticContextInstallArtifact = createMock(InstallArtifact.class);

        File syntheticBundleDir = new File(
            "target/work/staging/nested-plan-1/bundle/nested-plan-1-synthetic.context/1.0.0/nested-plan-1-synthetic.context.jar").getAbsoluteFile();
        expect(
            this.installArtifactTreeFactory.constructInstallArtifactTree(eq(new ArtifactIdentity("bundle", "nested-plan-1-synthetic.context",
                new Version(1, 0, 0), ScopeNameFactory.createScopeName("nested-plan", new Version(1, 0, 0)))), isA(ArtifactStorage.class),
                (Map<String, String>) isNull(), (String) isNull())).andReturn(
            new ThreadSafeArrayListTree<InstallArtifact>(syntheticContextInstallArtifact));

        replay(this.installEnvironment, this.installArtifactTreeFactory);

        this.transformer.transform(rootPlanInstallTree, this.installEnvironment);

        verify(this.installEnvironment, this.installArtifactTreeFactory);

        File manifest = new File(syntheticBundleDir, JarFile.MANIFEST_NAME);
        assertTrue(manifest.exists());

        assertBundlesImported(manifest, "bundle2", "bundle3");
    }

    private void assertBundlesImported(File manifestFile, String... symbolicNames) throws FileNotFoundException, IOException {
        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest(new FileReader(manifestFile));
        List<ImportedBundle> importedBundles = bundleManifest.getImportBundle().getImportedBundles();
        assertEquals(symbolicNames.length, importedBundles.size());

        for (String symbolicName : symbolicNames) {
            assertBundleImported(importedBundles, symbolicName);
        }
    }

    private void assertBundleImported(List<ImportedBundle> importedBundles, String symbolicName) {
        for (ImportedBundle importedBundle : importedBundles) {
            if (symbolicName.equals(importedBundle.getBundleSymbolicName())) {
                return;
            }
        }
        fail("No import for symbolic name '" + symbolicName + "' was found among imported bundles " + importedBundles);
    }

    private InstallArtifact createMockBundleInstallArtifact(String symbolicName) {
        InstallArtifact bundle = createMock(BundleInstallArtifact.class);
        expect(bundle.getName()).andReturn(symbolicName).anyTimes();
        replay(bundle);
        return bundle;
    }

    private Tree<InstallArtifact> createMockPlan(boolean scoped, Version version, String name, String... bundleSymbolicNames) {
        PlanInstallArtifact plan = createMock(PlanInstallArtifact.class);

        expect(plan.isScoped()).andReturn(scoped).anyTimes();
        expect(plan.getVersion()).andReturn(version).anyTimes();
        expect(plan.getName()).andReturn(name).anyTimes();

        replay(plan);

        Tree<InstallArtifact> installTree = new ThreadSafeArrayListTree<InstallArtifact>(plan);

        for (String bundleSymbolicName : bundleSymbolicNames) {
            InstallArtifact bundle = createMockBundleInstallArtifact(bundleSymbolicName);
            // This test need not use TreeUtils.addChild
            installTree.addChild(new ThreadSafeArrayListTree<InstallArtifact>(bundle));
        }
        return installTree;
    }
}
