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

package org.eclipse.virgo.kernel.install.artifact.internal.bundle;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.kernel.shim.serviceability.TracingService;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.kernel.osgi.framework.BundleClassLoaderUnavailableException;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;

/**
 * {@link BundleThreadContextManager} is a utility used by {@link StandardBundleDriver} to manage the thread context
 * relating to a bundle.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * TODO Document concurrent semantics of BundleThreadContextManager
 * 
 */
final class BundleThreadContextManager {

    private static final String WRAPPED_NULL = "WRAPPED_NULL";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Object monitor = new Object();

    private final OsgiFramework osgi;

    private final Bundle contextBundle;

    private final String scopeName;

    private final TracingService tracingService;

    private final ThreadLocal<BlockingDeque<ClassLoader>> contextClassLoaderStack = new ThreadLocal<BlockingDeque<ClassLoader>>() {

        @Override
        protected BlockingDeque<ClassLoader> initialValue() {
            return new LinkedBlockingDeque<ClassLoader>();
        }
    };

    private final ThreadLocal<BlockingDeque<String>> applicationTraceNameStack = new ThreadLocal<BlockingDeque<String>>() {

        @Override
        protected BlockingDeque<String> initialValue() {
            return new LinkedBlockingDeque<String>();
        }
    };

    public BundleThreadContextManager(@NonNull OsgiFramework osgi, @NonNull Bundle contextBundle, String scopeName,
        @NonNull TracingService tracingService) {
        this.osgi = osgi;
        this.contextBundle = contextBundle;
        this.scopeName = scopeName;
        this.tracingService = tracingService;
    }

    public void pushThreadContext() {
        synchronized (this.monitor) {
            pushThreadContextClassLoader();
            this.applicationTraceNameStack.get().push(wrapNull(this.tracingService.getCurrentApplicationName()));
            this.tracingService.setCurrentApplicationName(this.scopeName);
        }
    }

    private String wrapNull(String string) {
        return string != null ? string : WRAPPED_NULL;
    }

    private String unwrapNull(String string) {
        return WRAPPED_NULL.equals(string) ? null : string;
    }

    public void popThreadContext() {
        synchronized (this.monitor) {
            popThreadContextClassLoader();
            this.tracingService.setCurrentApplicationName(unwrapNull(this.applicationTraceNameStack.get().pop()));
        }
    }

    private void pushThreadContextClassLoader() {
        Thread currentThread = Thread.currentThread();
        ClassLoader oldContextClassLoader = currentThread.getContextClassLoader();
        this.contextClassLoaderStack.get().push(oldContextClassLoader);

        int state = this.contextBundle.getState();

        if (state != Bundle.INSTALLED && state != Bundle.UNINSTALLED) {
            ClassLoader newContextClassLoader = null;
            try {
                newContextClassLoader = this.osgi.getBundleClassLoader(this.contextBundle);
            } catch (BundleClassLoaderUnavailableException _) {
                this.logger.info("Bundle class loader not available, it may not be resolved");
            }
            if (newContextClassLoader != null) {
                currentThread.setContextClassLoader(newContextClassLoader);
                this.logger.info("Thread context class loader '{}' pushed and set to '{}'", oldContextClassLoader, newContextClassLoader);
            } else {
                this.logger.info("Thread context class loader not found for bundle '{}'", this.contextBundle.getSymbolicName());
            }
        }
    }

    private void popThreadContextClassLoader() {
        Thread currentThread = Thread.currentThread();
        ClassLoader oldContextClassLoader = currentThread.getContextClassLoader();
        ClassLoader newContextClassLoader = this.contextClassLoaderStack.get().pop();
        currentThread.setContextClassLoader(newContextClassLoader);
        this.logger.info("Thread context class loader '{}' popped and set to '{}'", oldContextClassLoader, newContextClassLoader);
    }

}
