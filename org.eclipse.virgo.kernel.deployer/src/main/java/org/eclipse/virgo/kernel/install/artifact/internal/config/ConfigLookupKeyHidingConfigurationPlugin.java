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

package org.eclipse.virgo.kernel.install.artifact.internal.config;

import java.util.Dictionary;

import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationPlugin;


/**
 * This plugin is used to hide {@link ConfigLifecycleEngine#ARTIFACT_ID_PRIVATE_PROPERTY} from target of configuration.
 * <p />
 * That property is used internally by container and should not be visible to the configuration target.
 * This is only applicable to targets that are of type {@link org.osgi.service.cm.ManagedService} or {@link org.osgi.service.cm.ManagedServiceFactory}
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread Safe
 */
public class ConfigLookupKeyHidingConfigurationPlugin implements ConfigurationPlugin {

    /** 
     * {@inheritDoc}
     */
    @Override
    public void modifyConfiguration(ServiceReference reference, Dictionary properties) {
        properties.remove(ConfigLifecycleEngine.ARTIFACT_ID_PRIVATE_PROPERTY);
    }

}
