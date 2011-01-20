/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.easymock.EasyMock;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph.FilteredRegion;
import org.eclipse.virgo.util.math.OrderedPair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

public class BundleIdBasedRegionTests {

    private static final String OTHER_REGION_NAME = "other";

    private static final String BUNDLE_SYMBOLIC_NAME = "b";

    private static final String BUNDLE_SYMBOLIC_NAME_2 = "c";

    private static final Version BUNDLE_VERSION = new Version("1");

    private static final long BUNDLE_ID = 1L;

    private static final String REGION_NAME = "reg";

    private Bundle mockBundle;

    private RegionDigraph mockGraph;

    private Iterator<Region> regionIterator;

    private BundleContext mockBundleContext;

    private Region mockRegion;

    private Region mockRegion2;

    private RegionFilter mockRegionFilter;

    @Before
    public void setUp() throws Exception {
        this.mockBundle = EasyMock.createMock(Bundle.class);
        EasyMock.expect(this.mockBundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME).anyTimes();
        EasyMock.expect(this.mockBundle.getVersion()).andReturn(BUNDLE_VERSION).anyTimes();
        EasyMock.expect(this.mockBundle.getBundleId()).andReturn(BUNDLE_ID).anyTimes();

        this.mockBundleContext = EasyMock.createMock(BundleContext.class);
        EasyMock.expect(this.mockBundleContext.getBundle(BUNDLE_ID)).andReturn(this.mockBundle).anyTimes();

        this.mockRegion = EasyMock.createMock(Region.class);
        this.mockRegion2 = EasyMock.createMock(Region.class);

        this.mockRegionFilter = EasyMock.createMock(RegionFilter.class);

        this.regionIterator = new Iterator<Region>() {

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Region next() {
                return null;
            }

            @Override
            public void remove() {
            }
        };
        this.mockGraph = EasyMock.createMock(RegionDigraph.class);
        this.mockGraph.connect(EasyMock.isA(Region.class), EasyMock.eq(this.mockRegionFilter), EasyMock.eq(this.mockRegion));
        EasyMock.expectLastCall().anyTimes();
    }

    private void replayMocks() {
        EasyMock.replay(this.mockBundleContext, this.mockBundle, this.mockRegion, this.mockRegion2, this.mockRegionFilter,
            this.mockGraph);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(this.mockBundleContext, this.mockBundle, this.mockRegion, this.mockRegion2, this.mockRegionFilter,
            this.mockGraph);
    }

    @Test
    public void testGetName() {
        defaultSetUp();

        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        assertEquals(REGION_NAME, r.getName());
    }

    private void defaultSetUp() {
        EasyMock.expect(this.mockGraph.iterator()).andReturn(this.regionIterator).anyTimes();
        EasyMock.expect(this.mockGraph.getEdges(EasyMock.isA(Region.class))).andReturn(new HashSet<FilteredRegion>()).anyTimes();
        replayMocks();
    }

    @Test
    public void testAddBundle() throws BundleException {
        EasyMock.expect(this.mockGraph.iterator()).andReturn(this.regionIterator).anyTimes();

        HashSet<FilteredRegion> edges = new HashSet<FilteredRegion>();
        edges.add(new FilteredRegion() {

            @Override
            public Region getRegion() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public RegionFilter getFilter() {
                return mockRegionFilter;
            }
        });
        EasyMock.expect(this.mockGraph.getEdges(EasyMock.isA(Region.class))).andReturn(edges).anyTimes();
        Set<OrderedPair<String, Version>> allowedBundles = new HashSet<OrderedPair<String, Version>>();
        allowedBundles.add(new OrderedPair<String, Version>(BUNDLE_SYMBOLIC_NAME_2, BUNDLE_VERSION));
        EasyMock.expect(this.mockRegionFilter.getAllowedBundles()).andReturn(allowedBundles).anyTimes();
        replayMocks();

        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        r.addBundle(this.mockBundle);
    }

    @Test(expected = BundleException.class)
    public void testAddExistingBundle() throws BundleException {
        defaultSetUp();

        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        r.addBundle(this.mockBundle);
        r.addBundle(this.mockBundle);
    }

    @Test(expected = BundleException.class)
    public void testAddBundlePresentInAnotherRegion() throws BundleException {
        this.regionIterator = new Iterator<Region>() {

            private int next = 2;

            @Override
            public boolean hasNext() {
                return this.next > 0;
            }

            @Override
            public Region next() {
                switch (next--) {
                    case 2:
                        return mockRegion;
                    default:
                        return mockRegion2;
                }
            }

            @Override
            public void remove() {
            }
        };
        EasyMock.expect(this.mockGraph.iterator()).andReturn(this.regionIterator).anyTimes();
        EasyMock.expect(this.mockGraph.getEdges(EasyMock.isA(Region.class))).andReturn(new HashSet<FilteredRegion>()).anyTimes();
        EasyMock.expect(this.mockRegion.contains(EasyMock.eq(this.mockBundle))).andReturn(true).anyTimes();
        EasyMock.expect(this.mockRegion2.contains(EasyMock.eq(this.mockBundle))).andReturn(false).anyTimes();

        replayMocks();

        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        r.addBundle(this.mockBundle);
    }

    @Test(expected = BundleException.class)
    public void testAddImportedBundle() throws BundleException {
        EasyMock.expect(this.mockGraph.iterator()).andReturn(this.regionIterator).anyTimes();

        HashSet<FilteredRegion> edges = new HashSet<FilteredRegion>();
        edges.add(new FilteredRegion() {

            @Override
            public Region getRegion() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public RegionFilter getFilter() {
                return mockRegionFilter;
            }
        });
        EasyMock.expect(this.mockGraph.getEdges(EasyMock.isA(Region.class))).andReturn(edges).anyTimes();
        Set<OrderedPair<String, Version>> allowedBundles = new HashSet<OrderedPair<String, Version>>();
        allowedBundles.add(new OrderedPair<String, Version>(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION));
        EasyMock.expect(this.mockRegionFilter.getAllowedBundles()).andReturn(allowedBundles).anyTimes();
        replayMocks();

        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        r.addBundle(this.mockBundle);
    }

    @Test
    public void testInstallBundleStringInputStream() {
        defaultSetUp();

        // TODO
    }

    @Test
    public void testInstallBundleString() {
        defaultSetUp();

        // TODO
    }

    @Test
    public void testContains() throws BundleException {
        defaultSetUp();

        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        r.addBundle(this.mockBundle);
        assertTrue(r.contains(this.mockBundle));
    }

    @Test
    public void testDoesNotContain() throws BundleException {
        defaultSetUp();

        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        assertFalse(r.contains(this.mockBundle));
    }

    @Test
    public void testGetBundle() throws BundleException {
        defaultSetUp();

        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        r.addBundle(this.mockBundle);
        assertEquals(this.mockBundle, r.getBundle(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION));
    }

    @Test
    public void testGetBundleNotFound() throws BundleException {
        defaultSetUp();

        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        r.addBundle(this.mockBundle);
        assertNull(r.getBundle(BUNDLE_SYMBOLIC_NAME_2, BUNDLE_VERSION));
    }

    @Test
    public void testConnectRegion() throws BundleException {
        defaultSetUp();

        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        r.connectRegion(this.mockRegion, this.mockRegionFilter);
    }

    @Test
    public void testEquals() {
        defaultSetUp();

        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        Region s = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        assertEquals(r, r);
        assertEquals(r, s);
        assertEquals(r.hashCode(), s.hashCode());
    }

    @Test
    public void testNotEqual() {
        defaultSetUp();

        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        Region s = new BundleIdBasedRegion(OTHER_REGION_NAME, this.mockGraph, this.mockBundleContext);
        assertFalse(r.equals(s));
        assertFalse(r.equals(null));
    }

}
