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
package org.infinispan.distexec.mapreduce;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.testng.annotations.Test;

@Test(groups = "functional", testName = "groovy.BaseScriptWordCountMapReduceTest")
public abstract class BaseScriptWordCountMapReduceTest extends BaseWordCountMapReduceTest {
   @Override
   protected void createCacheManagers() throws Throwable {
      GlobalConfigurationBuilder globalBuilder = GlobalConfigurationBuilder.defaultClusteredBuilder();
      globalBuilder.serialization()
         .addAdvancedExternalizer(new ScriptMapper.Externalizer())
         .addAdvancedExternalizer(new ScriptReducer.Externalizer())
         .addAdvancedExternalizer(new ScriptCollator.Externalizer());
      ConfigurationBuilder builder = getDefaultClusteredCacheConfig(getCacheMode(), true);
      
      createCluster(globalBuilder, builder, 2);
      for (EmbeddedCacheManager cm : cacheManagers) {
         cm.defineConfiguration(cacheName(), builder.build());
         cm.getCache(cacheName());;
      }
      waitForClusterToForm(cacheName());
   }
   
   @Override
   public MapReduceTask<String, String, String, Integer> invokeMapReduce(String keys[], boolean useCombiner)
         throws Exception {
      return invokeMapReduce(keys, createWordCountMapper(), createWordCountReducer(), useCombiner);
   }
   
   @Override
   public void testMapperReducerIsolation() throws Exception {
      // this test doesn't really execute in the super class...
   }
   
   public void testScriptCollator() throws Exception {
      MapReduceTask<String,String,String,Integer> task = invokeMapReduce(null);
      Collator<String, Integer, Integer> collator = createCollator();
      Integer totalWords = task.execute(collator);
      assertWordCount(totalWords, 56);  
   }
   
   abstract protected Mapper<String, String, String, Integer> createWordCountMapper() throws Exception;
   abstract protected Reducer<String, Integer> createWordCountReducer() throws Exception;
   abstract protected Collator<String, Integer, Integer> createCollator()  throws Exception;
}