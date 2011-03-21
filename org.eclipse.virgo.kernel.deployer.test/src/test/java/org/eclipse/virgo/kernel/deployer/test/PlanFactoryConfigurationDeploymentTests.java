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

package org.eclipse.virgo.kernel.deployer.test;

import static org.eclipse.virgo.kernel.deployer.test.ConfigurationTestUtils.countFactoryConfigurations;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer;
import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.eclipse.virgo.util.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

/**
 * Test to validate deployment of plans with "factory-configuration" artifact type.
 * <p />
 * 
 */
public class PlanFactoryConfigurationDeploymentTests extends AbstractDeployerIntegrationTest {

    private ServiceReference<ApplicationDeployer> appDeployerServiceReference;

    private ApplicationDeployer appDeployer;

    private ServiceReference<ConfigurationAdmin> configAdminServiceReference;

    private ConfigurationAdmin configAdmin;

    private static final String factoryPid = "org.eclipse.virgo.kernel.deployer.test.factory.config";

    private static final File watchedRepository = new File("target/watched");

    private static final String fileNameTemplate = factoryPid + "-%s.properties";

    private static final FileFilter fileFilter = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            final String name = pathname.getName();
            return name.startsWith(factoryPid) && name.endsWith(".properties");
        }
    };

    @BeforeClass
    public static void setupConfigurationInWatchedRepository() throws Exception {
        int count = 2;
        for (int index = 0; index < count; index++) {
            setupConfiguration(index);
        }
    }

    private static void setupConfiguration(int index) throws FileNotFoundException, IOException {
        File config = new File(watchedRepository, String.format(fileNameTemplate, index));
        if (!config.exists()) {
            config.createNewFile();
        }
        Properties props = new Properties();
        props.setProperty(ConfigurationAdmin.SERVICE_FACTORYPID, factoryPid);
        props.setProperty("index", String.valueOf(index));

        OutputStream stream = new FileOutputStream(config);
        try {
            props.store(new FileOutputStream(config), "Config " + index);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    @AfterClass
    public static void cleanupConfigurationFromWatchedDirectory() {
        try {
            File[] files = watchedRepository.listFiles(fileFilter);
            for (File f : files) {
                f.delete();
            }
        } catch (Exception ex) {
            // do anything?
        }
    }

    @Before
    public void setUp() throws Exception {
        this.appDeployerServiceReference = this.context.getServiceReference(ApplicationDeployer.class);
        this.appDeployer = this.context.getService(this.appDeployerServiceReference);
        this.configAdminServiceReference = this.context.getServiceReference(ConfigurationAdmin.class);
        this.configAdmin = this.context.getService(this.configAdminServiceReference);

    }

    @After
    public void tearDown() throws Exception {
        if (this.appDeployerServiceReference != null) {
            this.context.ungetService(this.appDeployerServiceReference);
        }
        if (this.configAdminServiceReference != null) {
            this.context.ungetService(this.configAdminServiceReference);
        }
    }

    @SuppressWarnings("rawtypes")
    private static class TestManagedServiceFactory implements ManagedServiceFactory {

        private final Map<String, Dictionary> properties = new HashMap<String, Dictionary>();

        private final AtomicInteger updateCallCount = new AtomicInteger(0);

        private final AtomicInteger deleteCallCount = new AtomicInteger(0);

        @Override
        public String getName() {
            return "Test Managed Service Factory";
        }

        @Override
        public void updated(String pid, Dictionary properties) throws ConfigurationException {
            this.updateCallCount.incrementAndGet();
            this.properties.put(pid, properties);
        }

        @Override
        public void deleted(String pid) {
            this.deleteCallCount.incrementAndGet();
            this.properties.remove(pid);
        }

        Map<String, Dictionary> getProperties() {
            return this.properties;
        }

        int updateCount() {
            return this.updateCallCount.get();
        }

        int deleteCount() {
            return this.deleteCallCount.get();
        }
    }

    @Test
    public void validateThatPlanWithFactoryConfigDeploys() throws Exception {

        TestManagedServiceFactory service = new TestManagedServiceFactory();
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(Constants.SERVICE_PID, factoryPid);
        ServiceRegistration<ManagedServiceFactory> registration = this.context.registerService(ManagedServiceFactory.class, service, properties);

        // let repository indexing complete
        Thread.sleep(2000);

        // deploy the plan
        DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(new File("src/test/resources/configuration.deployment/factory.config.plan").toURI());
        assertNotNull(deploymentIdentity);

        // let config admin events propagate
        Thread.sleep(1000);

        int factoryConfigCount = countFactoryConfigurations(this.configAdmin, factoryPid);
        assertEquals(2, factoryConfigCount);

        // check managed service factory for update events
        assertEquals(2, service.updateCount());
        assertEquals(2, service.getProperties().size());

        this.appDeployer.undeploy(deploymentIdentity);

        // let config admin events propagate
        Thread.sleep(300);

        factoryConfigCount = countFactoryConfigurations(this.configAdmin, factoryPid);
        assertEquals(0, factoryConfigCount);

        // check managed service factory on delete count
        assertEquals(2, service.deleteCount());
        assertEquals(2, service.updateCount());
        assertEquals(0, service.getProperties().size());

        registration.unregister();
    }

    @Test
    public void validateScopedPlanWithFactoryConfigDeployment() throws Exception {

        TestManagedServiceFactory service = new TestManagedServiceFactory();
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(Constants.SERVICE_PID, factoryPid);
        ServiceRegistration<ManagedServiceFactory> registration = this.context.registerService(ManagedServiceFactory.class, service, properties);

        // let repository indexing complete
        Thread.sleep(2000);

        // deploy the plan
        DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(new File(
            "src/test/resources/configuration.deployment/scoped.factory.config.plan").toURI());
        assertNotNull(deploymentIdentity);

        // let config admin events propagate
        Thread.sleep(300);

        // this will fail if factory or configurations got scoped as BSN/PID will change.
        int factoryConfigCount = countFactoryConfigurations(this.configAdmin, factoryPid);
        assertEquals(2, factoryConfigCount);

        // check managed service factory for update events
        assertEquals(2, service.updateCount());
        assertEquals(2, service.getProperties().size());

        this.appDeployer.undeploy(deploymentIdentity);

        // let config admin events propagate
        Thread.sleep(300);

        factoryConfigCount = countFactoryConfigurations(this.configAdmin, factoryPid);
        assertEquals(0, factoryConfigCount);

        // check managed service factory on delete count
        assertEquals(2, service.deleteCount());
        assertEquals(2, service.updateCount());
        assertEquals(0, service.getProperties().size());

        registration.unregister();
    }
}
