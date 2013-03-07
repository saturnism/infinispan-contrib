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
package org.infinispan.collections.impl;

import org.infinispan.Cache;
import org.infinispan.atomic.AtomicMap;
import org.infinispan.atomic.AtomicMapLookup;
import org.infinispan.collections.AppendOnlyCollection;
import org.infinispan.collections.AtomicHashSet;
import org.infinispan.collections.CollectionsManager;
import org.infinispan.collections.IspnMap;
import org.infinispan.collections.IspnSet;
import org.infinispan.context.Flag;
import org.infinispan.manager.EmbeddedCacheManager;

public class DefaultCollectionsManager implements CollectionsManager {
   private final EmbeddedCacheManager cacheManager;

   public DefaultCollectionsManager(EmbeddedCacheManager cacheManager) {
      this.cacheManager = cacheManager;
   }

   @Override
   public <T> IspnSet<T> getSet(String name) {
      Cache<T, Boolean> cache = cacheManager.getCache(name);
      return getSet(cache);
   }

   @Override
   public <T> IspnSet<T> getSet(Cache<T, Boolean> cache) {
      return new IspnSetImpl<T>(cache);
   }

   @Override
   public <K, V> IspnMap<K, V> getMap(String name) {
      Cache<K, V> cache = cacheManager.getCache(name);
      return getMap(cache);
   }

   @Override
   public <K, V> IspnMap<K, V> getMap(Cache<K, V> cache) {
      return new IspnMapImpl<K, V>(cache);
   }
   
   @Override
   public <MK, E> AtomicHashSet<E> getAtomicHashSet(String name, MK key) {
      Cache<MK, ?> cache = cacheManager.getCache(name);
      return getAtomicHashSet(cache, key);
   }

   @Override
   public <MK, E> AtomicHashSet<E> getAtomicHashSet(Cache<MK, ?> cache, MK key) {
      AtomicMap<E, ?> map = AtomicMapLookup.getAtomicMap(cache, key);
      return new AtomicHashSet<E>(map);
   }

   @Override
   public <MK, E> void removeAtomicHashSet(Cache<MK, ?> cache, MK key) {
      cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).remove(key);
   }
   
   @Override
   public <MK, E> AppendOnlyCollection<E> getAppendOnlyCollection(Cache<MK, ?> cache, MK key) {
      return getAppendOnlyCollection(cache, key, true);
   }
   
   @Override
   public <MK, E> AppendOnlyCollection<E> getAppendOnlyCollection(Cache<MK, ?> cache, MK key, boolean createIfAbsent) {
      Object value = cache.get(key);
      if (value == null) {
         if (createIfAbsent)
            value = new AppendOnlyCollectionImpl<E>();
         else return null;
      }
      @SuppressWarnings("unchecked")
      AppendOnlyCollectionImpl<E> castValue = (AppendOnlyCollectionImpl<E>) value;

      return castValue;
   }
}
