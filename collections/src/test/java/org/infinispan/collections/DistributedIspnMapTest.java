/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.infinispan.collections;

import java.util.concurrent.ConcurrentMap;

import org.infinispan.collections.impl.DefaultCollectionsManager;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;

public class DistributedIspnMapTest extends BaseMapTest {
	protected static final String CACHE_NAME = "testMap";
	protected CollectionsManager collectionsManager;
	
	@Override
	protected ConcurrentMap<String, String> createMap() {
		return collectionsManager.getMap(CACHE_NAME);
	}

	@Override
	protected void createCacheManagers() throws Throwable {
		ConfigurationBuilder builder = getDefaultClusteredCacheConfig(
				CacheMode.DIST_SYNC, true);
		createCluster(builder, 2);

		collectionsManager = new DefaultCollectionsManager(cacheManagers.get(0));
		// MUST start all caches
		caches(CACHE_NAME);
	}
	
	public void testDistMap() {
		final int count = 10;
		for (int i = 0; i < count; i++) {
			map.put(String.valueOf(i), String.valueOf(i));
		}
		assert (map.size() == count);
		for (EmbeddedCacheManager cm : cacheManagers) {
			DefaultCollectionsManager dcm = new DefaultCollectionsManager(cm);
			IspnMap<String, String> s = dcm.getMap(CACHE_NAME);
			assert s.size() == count;
		}
	}

}
