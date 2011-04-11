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

package org.eclipse.virgo.kernel.shell.internal.commands;

import org.eclipse.virgo.kernel.model.management.ManageableCompositeArtifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.kernel.shell.Command;
import org.eclipse.virgo.kernel.shell.internal.formatting.CompositeInstallArtifactCommandFormatter;

@Command("plan")
final class PlanCommands extends AbstractInstallArtifactBasedCommands<ManageableCompositeArtifact> {

    private static final String TYPE = "plan";

    public PlanCommands(RuntimeArtifactModelObjectNameCreator objectNameCreator) {
        super(TYPE, objectNameCreator, new CompositeInstallArtifactCommandFormatter(), ManageableCompositeArtifact.class, null);
    }

}
