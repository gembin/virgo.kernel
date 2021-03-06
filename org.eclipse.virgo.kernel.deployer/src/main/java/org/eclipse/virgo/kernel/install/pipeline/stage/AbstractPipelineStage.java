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

package org.eclipse.virgo.kernel.install.pipeline.stage;

import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallLog;
import org.eclipse.virgo.util.common.Tree;

/**
 * {@link AbstractPipelineStage} is a common base class for {@link PipelineStage} implementations.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public abstract class AbstractPipelineStage implements PipelineStage {

    /**
     * {@inheritDoc}
     */
    public final void process(Tree<InstallArtifact> installTree, InstallEnvironment installEnvironment) throws DeploymentException,
        UnableToSatisfyBundleDependenciesException {
        InstallLog installLog = installEnvironment.getInstallLog();
        installLog.log(this, "process entry with installTree '%s'", installTree.toString());
        try {
            doProcessTree(installTree, installEnvironment);
        } catch (DeploymentException de) {
            installLog.log(this, "process exit with installTree '%s', exception '%s' thrown", installTree.toString(), de.toString());
            throw de;
        } catch (UnableToSatisfyBundleDependenciesException utsbde) {
            installLog.log(this, "process exit with installTree '%s', exception '%s' thrown", installTree.toString(), utsbde.toString());
            throw utsbde;
        } catch (RuntimeException re) {
            installLog.log(this, "process exit with installTree '%s', exception '%s' thrown", installTree.toString(), re.toString());
            throw re;
        } 
        installLog.log(this, "process exit with installTree '%s'", installTree.toString());
    }

    /**
     * Processes the given install tree in the context of the given {@link InstallEnvironment}. The default
     * implementation simply calls the <code>doProcessNode</code> method for each node in the tree. If a different
     * behaviour is required, the subclass should override this method.
     * 
     * @param installTree the tree to be processed
     * @param installEnvironment the <code>InstallEnvironment</code> in the context of which to do the processing
     * @throws {@link UnableToSatisfyBundleDependenciesException} if a bundle's dependencies cannot be satisfied
     * @throws DeploymentException if a failure occurs
     */
    protected void doProcessTree(Tree<InstallArtifact> installTree, InstallEnvironment installEnvironment) throws DeploymentException,
        UnableToSatisfyBundleDependenciesException {
        InstallArtifact value = installTree.getValue();
        doProcessNode(value, installEnvironment);
        for (Tree<InstallArtifact> child : installTree.getChildren()) {
            doProcessTree(child, installEnvironment);
        }
    }

    /**
     * Processes the given {@link InstallArtifact} in the context of the given {@link InstallEnvironment}. Subclasses
     * should override this method if they do not override the doProcessTree method.
     * 
     * @param installArtifact the tree node to be processed
     * @param installEnvironment the <code>InstallEnvironment</code> in the context of which to do the processing
     * @throws DeploymentException if a failure occurs
     */
    protected void doProcessNode(InstallArtifact installArtifact, InstallEnvironment installEnvironment) throws DeploymentException {
    }

}
