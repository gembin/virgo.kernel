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

package org.eclipse.virgo.kernel.install.pipeline.stage.resolve.internal;

import java.io.File;
import java.io.IOException;

import org.osgi.framework.BundleException;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallLog;
import org.eclipse.virgo.kernel.install.pipeline.stage.PipelineStage;
import org.eclipse.virgo.util.common.Tree;
import org.eclipse.virgo.util.common.Tree.TreeVisitor;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * {@link QuasiInstallStage} is a {@link PipelineStage} which installs the bundle artifacts of the install tree in the
 * side state.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public final class QuasiInstallStage implements PipelineStage {

    /**
     * {@inheritDoc}
     */
    public void process(Tree<InstallArtifact> installTree, InstallEnvironment installEnvironment) throws DeploymentException {
        QuasiFramework quasiFramework = installEnvironment.getQuasiFramework();
        installTree.visit(new InstallVisitor(quasiFramework, installEnvironment.getInstallLog()));
    }

    private static class InstallVisitor implements TreeVisitor<InstallArtifact> {

        private final QuasiFramework quasiFramework;

        private final InstallLog installLog;

        public InstallVisitor(QuasiFramework quasiFramework, InstallLog installLog) {
            this.quasiFramework = quasiFramework;
            this.installLog = installLog;
        }

        public boolean visit(Tree<InstallArtifact> tree) {
            InstallArtifact installArtifact = tree.getValue();
            if (installArtifact instanceof BundleInstallArtifact) {
                BundleInstallArtifact bundleInstallArtifact = (BundleInstallArtifact) installArtifact;
                try {
                    BundleManifest bundleManifest = bundleInstallArtifact.getBundleManifest();
                    File location = bundleInstallArtifact.getArtifactFS().getFile();
                    QuasiBundle quasiBundle = this.quasiFramework.install(location.toURI(), bundleManifest);
                    bundleInstallArtifact.setQuasiBundle(quasiBundle);
                } catch (IOException e) {
                    this.installLog.log(bundleInstallArtifact, "failed to read bundle manifest", e.getMessage());
                    throw new RuntimeException("failed to read bundle manifest", e);
                } catch (BundleException e) {
                    this.installLog.log(bundleInstallArtifact, "failed to install bundle in side state", e.getMessage());
                    throw new RuntimeException("failed to install bundle in side state", e);
                }
            }
            return true;
        }

    }

}
