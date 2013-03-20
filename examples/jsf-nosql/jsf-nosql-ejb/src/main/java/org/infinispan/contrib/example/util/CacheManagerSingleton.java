/* 
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.infinispan.contrib.example.util;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.lookup.GenericTransactionManagerLookup;

public final class CacheManagerSingleton {
   private final EmbeddedCacheManager cacheManager;
   
   private CacheManagerSingleton(){
      GlobalConfigurationBuilder builder = new GlobalConfigurationBuilder();
      
      builder.globalJmxStatistics().enable().jmxDomain("infinispan-demo")
         .transport().defaultTransport().nodeName(System.getProperty("jboss.node.name"));
      
      GlobalConfiguration globalConfig = builder.build();

      ConfigurationBuilder configBuilder = new ConfigurationBuilder();
      configBuilder.clustering()
            .cacheMode(CacheMode.DIST_SYNC)
            .jmxStatistics().enable()
            .invocationBatching().enable()
            .transaction()
               .transactionManagerLookup(new GenericTransactionManagerLookup())
               .transactionMode(TransactionMode.TRANSACTIONAL)
               .lockingMode(LockingMode.PESSIMISTIC)
               .syncCommitPhase(false)
               .syncRollbackPhase(false);
      
      cacheManager = new DefaultCacheManager(
            globalConfig);

      cacheManager.defineConfiguration("RB_app", configBuilder.build());
   };
   
   private static class Holder {
      static final CacheManagerSingleton INSTANCE = new CacheManagerSingleton();
   }
   
   public static EmbeddedCacheManager getInstance() {
       return Holder.INSTANCE.cacheManager;
   }
}
