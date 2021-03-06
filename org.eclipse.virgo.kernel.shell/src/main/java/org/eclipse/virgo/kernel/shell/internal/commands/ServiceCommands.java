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

import java.util.Arrays;
import java.util.List;

import org.eclipse.virgo.kernel.shell.Command;
import org.eclipse.virgo.kernel.shell.internal.formatting.ServiceCommandFormatter;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;
import org.eclipse.virgo.kernel.shell.state.StateService;


@Command("service")
public final class ServiceCommands {

    private final StateService stateService;

    private final ServiceCommandFormatter formatter;

    public ServiceCommands(StateService stateService) {
        this.stateService = stateService;
        this.formatter = new ServiceCommandFormatter();
    }

    @Command("list")
    public List<String> list() {
        return this.formatter.formatList(this.stateService.getAllServices(null));
    }

    @Command("examine")
    public List<String> examine(long serviceId) {
        QuasiLiveService service = this.stateService.getService(null, serviceId);
        if (service == null) {
            return Arrays.asList(String.format("No service with id '%s' was found", serviceId));
        } else {
            return this.formatter.formatExamine(service);
        }
    }
}
