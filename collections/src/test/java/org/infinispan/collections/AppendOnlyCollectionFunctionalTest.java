/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other
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

import java.util.Collection;
import java.util.Set;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.infinispan.Cache;
import org.infinispan.atomic.AtomicMapLookup;
import org.infinispan.collections.impl.AppendDelta;
import org.infinispan.collections.impl.DefaultCollectionsManager;
import org.infinispan.config.Configuration;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.test.AbstractInfinispanTest;
import org.infinispan.test.TestingUtil;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "functional", testName = "collections.AppendOnlyCollection")
public class AppendOnlyCollectionFunctionalTest extends AbstractInfinispanTest {
   private static final Log log = LogFactory.getLog(AppendOnlyCollectionFunctionalTest.class);
   Cache<String, Object> cache;
   TransactionManager tm;
   private EmbeddedCacheManager cm;
   CollectionsManager collectionsManager;

   @BeforeMethod
   @SuppressWarnings("unchecked")
   public void setUp() {
      Configuration c = new Configuration();
      // these 2 need to be set to use the AtomicMapCache
      c.setInvocationBatchingEnabled(true);
      cm = TestCacheManagerFactory.createCacheManager(c);
      cache = cm.getCache();
      tm = TestingUtil.getTransactionManager(cache);
      collectionsManager = new DefaultCollectionsManager(cm);
   }

   @AfterMethod(alwaysRun = true)
   public void tearDown() {
      TestingUtil.killCacheManagers(cm);
      cache = null;
      tm = null;
   }

   public void testAppend() {
      AppendOnlyCollection<String> collection = collectionsManager.getAppendOnlyCollection(cache, "collection");
      assert collection.isEmpty();
      collection.add("Hello");;
      assert collection.contains("Hello");
      cache.put("collection", collection.delta());
      
      // now re-retrieve the map and make sure we see the diffs
      collection = collectionsManager.getAppendOnlyCollection(cache, "collection");
      assert collection.contains("Hello");
      assert collection.size() == 1;
      
      cache.put("collection", new AppendDelta<String>("Hey!"));
      collection = collectionsManager.getAppendOnlyCollection(cache, "collection");
      assert collection.contains("Hey!");
      assert collection.size() == 2;
      
   }

   /*
   public void testTxChangesOnAtomicMap() throws Exception {
      Set<String> set = collectionsManager.getAtomicHashSet(cache, "set");
      tm.begin();
      assert set.isEmpty();
      set.add("a");
      assert set.contains("a");
      Transaction t = tm.suspend();

      assert !collectionsManager.getAtomicHashSet(cache, "set").contains("a");

      tm.resume(t);
      tm.commit();

      // now re-retrieve the map and make sure we see the diffs
      assert collectionsManager.getAtomicHashSet(cache, "set").contains("a");
   }

   public void testChangesOnAtomicMapNoLocks() {
      Set<String> set = collectionsManager.getAtomicHashSet(cache, "set");
      
      assert set.isEmpty();

      set.add("a");
      assert set.contains("a");

      // now re-retrieve the map and make sure we see the diffs
      assert collectionsManager.getAtomicHashSet(cache, "set").contains("a");
   }

   public void testTxChangesOnAtomicMapNoLocks() throws Exception {
      Set<String> set = collectionsManager.getAtomicHashSet(cache, "set");
      
      tm.begin();
      assert set.isEmpty();
//      TestingUtil.extractComponent(cache, InvocationContextContainer.class).createInvocationContext(true, -1).setFlags(SKIP_LOCKING);
      set.add("a");
      assert set.contains("a");
      Transaction t = tm.suspend();

      assert !collectionsManager.getAtomicHashSet(cache, "set").contains("a");

      tm.resume(t);
      tm.commit();

      // now re-retrieve the map and make sure we see the diffs
      assert collectionsManager.getAtomicHashSet(cache, "set").contains("a");
   }

   public void testChangesOnAtomicMapNoLocksExistingData() {
      Set<String> set = collectionsManager.getAtomicHashSet(cache, "set");
      
      assert set.isEmpty();
      set.add("x");
      assert set.contains("x");
//      TestingUtil.extractComponent(cache, InvocationContextContainer.class).createInvocationContext(false, -1).setFlags(SKIP_LOCKING);
      
      set.add("a");
      assert set.contains("a");
      assert set.contains("x");

      // now re-retrieve the map and make sure we see the diffs
      assert collectionsManager.getAtomicHashSet(cache, "set").contains("x");
      assert collectionsManager.getAtomicHashSet(cache, "set").contains("a");
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testRemovalOfAtomicMap() throws SystemException, NotSupportedException, RollbackException, HeuristicRollbackException, HeuristicMixedException {
      Set<String> set = collectionsManager.getAtomicHashSet(cache, "set");
      
      set.add("hello");
      TransactionManager tm = cache.getAdvancedCache().getTransactionManager();
      tm.begin();
      set = collectionsManager.getAtomicHashSet(cache, "set");
      set.add("hello2");
      assert set.size() == 2;
      AtomicMapLookup.removeAtomicMap(cache, "set");
      set.size();
      tm.commit();

   }
   */
}
