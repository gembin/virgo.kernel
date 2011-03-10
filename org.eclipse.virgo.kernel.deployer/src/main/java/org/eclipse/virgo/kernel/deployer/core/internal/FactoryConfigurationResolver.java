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

package org.eclipse.virgo.kernel.deployer.core.internal;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactTreeFactory;
import org.eclipse.virgo.kernel.install.artifact.internal.AbstractInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.ArtifactStorageFactory;
import org.eclipse.virgo.kernel.install.artifact.internal.FactoryConfigInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.ArtifactIdentityScoper;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.util.common.Tree;
import org.eclipse.virgo.util.common.Tree.ExceptionThrowingTreeVisitor;
import org.osgi.framework.Version;

/**
 * {@link FactoryConfigurationResolver} adds the immediate child nodes to a root factory config node.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 */
public class FactoryConfigurationResolver implements Transformer {

    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

    private final InstallArtifactTreeFactory configInstallArtifactTreeFactory;

    private final ArtifactStorageFactory artifactStorageFactory;

    /**
     * 
     */
    public FactoryConfigurationResolver(@NonNull ArtifactStorageFactory artifactStorageFactory,
        @NonNull InstallArtifactTreeFactory configInstallArtifactTreeFactory) {
        this.artifactStorageFactory = artifactStorageFactory;
        this.configInstallArtifactTreeFactory = configInstallArtifactTreeFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transform(Tree<InstallArtifact> installTree, InstallEnvironment installEnvironment) throws DeploymentException {
        installTree.visit(new ExceptionThrowingTreeVisitor<InstallArtifact, DeploymentException>() {

            public boolean visit(Tree<InstallArtifact> tree) throws DeploymentException {
                FactoryConfigurationResolver.this.operate(tree.getValue());
                return true;
            }
        });

    }

    private void operate(InstallArtifact installArtifact) throws DeploymentException {
        if (installArtifact instanceof FactoryConfigInstallArtifact) {
            FactoryConfigInstallArtifact factoryConfigInstallArtifact = (FactoryConfigInstallArtifact) installArtifact;
            if (factoryConfigInstallArtifact.getTree().getChildren().isEmpty()) {
                Tree<InstallArtifact> tree = factoryConfigInstallArtifact.getTree();
                final String scope = factoryConfigInstallArtifact.getScopeName();
                Set<RepositoryAwareArtifactDescriptor> artifactSpecifications = factoryConfigInstallArtifact.getArtifactDescriptors();
                for (RepositoryAwareArtifactDescriptor artifactSpecification : artifactSpecifications) {
                    Tree<InstallArtifact> childInstallArtifactTree = createInstallArtifactTree(artifactSpecification, scope);
                    TreeUtils.addChild(tree, childInstallArtifactTree);

                    // Put child into the INSTALLING state as Transformers (like this) are after the "begin install"
                    // pipeline stage.
                    InstallArtifact childInstallArtifact = childInstallArtifactTree.getValue();
                    ((AbstractInstallArtifact) childInstallArtifact).beginInstall();
                }
            }
        }
    }

    /**
     * @param artifactSpecification
     * @return
     * @throws DeploymentException
     */
    private Tree<InstallArtifact> createInstallArtifactTree(RepositoryAwareArtifactDescriptor artifactSpecification, String scope)
        throws DeploymentException {
        final String type = artifactSpecification.getType();
        final String name = artifactSpecification.getName();
        final Version version = artifactSpecification.getVersion();
        URI artifactURI = artifactSpecification.getUri();

        ArtifactIdentity identity = new ArtifactIdentity(type, name, version, scope);
        identity = ArtifactIdentityScoper.scopeArtifactIdentity(identity);

        ArtifactStorage artifactStorage = this.artifactStorageFactory.create(new File(artifactURI), identity);
        return this.configInstallArtifactTreeFactory.constructInstallArtifactTree(identity, artifactStorage, EMPTY_MAP,
            artifactSpecification.getRepositoryName());
    }
}
