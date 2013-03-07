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

import org.infinispan.Cache;
import org.infinispan.collections.tasks.SizeTask;
import org.infinispan.collections.tasks.StartCacheTask;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.test.MultipleCacheManagersTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test (groups = "functional", testName = "collections.BaseMapTest")
public class TaskTest extends MultipleCacheManagersTest {
	protected static final String CACHE_NAME = "test";
	
	@BeforeMethod(dependsOnMethods="createBeforeMethod")
	public void setUp() {
	}

	@AfterMethod
	public void tearDown() {
	}

   @Override
   protected void createCacheManagers() throws Throwable {
      ConfigurationBuilder builder = getDefaultClusteredCacheConfig(
            CacheMode.DIST_SYNC, true);
      createCluster(builder, 2);

      caches(CACHE_NAME);
   }
   
   public void testStartTaskWithExecutor() {
      for (EmbeddedCacheManager cm : cacheManagers) {
         Cache<Object, Object> newCache = cm.getCache("cache2", false);
         assert newCache == null;
      }
      
      EmbeddedCacheManager ecm = manager(0);
      Cache<Object, Object> coordinator = cache(0, CACHE_NAME);
      StartCacheTask<Object, Object> startCacheTask = new StartCacheTask<Object, Object>(coordinator, "cache2");
      startCacheTask.executeSync();
      
      for (EmbeddedCacheManager cm : cacheManagers) {
         Cache<Object, Object> cache = cm.getCache("cache2", false);
         assert cache != null;
      }
      
      Cache<Integer, String> newCache = ecm.getCache("cache2", false);
      for (int i = 0; i < 100; i++) {
         newCache.put(i, "hello");
      }
      
      for (EmbeddedCacheManager cm : cacheManagers) {
         Cache<Object, Object> cache = cm.getCache("cache2", false);
         assert cache.size() > 0;
      }
      
      SizeTask<Integer, String> sizeTask = new SizeTask<Integer, String>(newCache);
      int size = sizeTask.executeSync();
      assert size == 100;
      
      ecm.removeCache("cache2");
      
      for (EmbeddedCacheManager cm : cacheManagers) {
         Cache<Object, Object> cache = cm.getCache("cache2", false);
         assert cache == null;
      }
   }
}
