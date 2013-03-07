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
package org.infinispan.collections.tasks;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.infinispan.Cache;

public class EntrySetTask<K, V> extends AbstractDistribtuedTask<K, V, Set<java.util.Map.Entry<K, V>>>{

	public EntrySetTask(Cache<K, V> cache) {
		super(cache);
	}

	@Override
	public Set<Entry<K, V>> executeSync() {
		Set<K> keys = new KeySetTask<K, V>(getCache()).executeSync();
		return Collections.unmodifiableSet(new MemoryEfficientEntrySet<K, V>(getCache(), keys));
	}
	
	static class MemoryEfficientEntrySet<K, V> extends AbstractSet<java.util.Map.Entry<K, V>> implements Set<java.util.Map.Entry<K, V>> {
		private final Cache<K, V> cache;
		private final Set<K> keys;
		public MemoryEfficientEntrySet(Cache<K, V> cache, Set<K> keys) {
			this.cache = cache;
			this.keys = keys;
		}

		@Override
		public boolean contains(Object entry) {
			if (!(entry instanceof java.util.Map.Entry<?, ?>)) {
				return false;
			}
			java.util.Map.Entry<?, ?> e = (java.util.Map.Entry<?, ?>) entry;
			return keys.contains(e.getKey());
		}
		
		@Override
		public Iterator<Entry<K, V>> iterator() {
			final Iterator<K> keysIt = keys.iterator();
			return new Iterator<Map.Entry<K,V>>() {
				@Override
				public boolean hasNext() {
					return keysIt.hasNext();
				}

				@Override
				public Entry<K, V> next() {
					final K key = keysIt.next();
					return new Entry<K, V>() {
						@Override
						public K getKey() {
							return key;
						}

						@Override
						public V getValue() {
							return cache.get(key);
						}

						@Override
						public V setValue(V value) {
							throw new UnsupportedOperationException();
						}
					};
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		
		@Override
		public int size() {
			return keys.size();
		}
	}

}
