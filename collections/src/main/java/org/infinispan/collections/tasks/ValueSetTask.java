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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.infinispan.Cache;

public class ValueSetTask<K, V> extends AbstractDistribtuedTask<K, V, Set<V>> {

	public ValueSetTask(Cache<K, V> cache) {
		super(cache);
	}

	@Override
	public Set<V> executeSync() {
		Set<Entry<K, V>> entries = new EntrySetTask<K, V>(getCache()).executeSync();
		return Collections.unmodifiableSet(new MemoryEfficientValueSet<K, V>(getCache(), entries));
	}
	
	static class MemoryEfficientValueSet<K, V> extends AbstractSet<V> implements Set<V> {
		private final Cache<K, V> cache;
		private final Set<Entry<K, V>> entries;
		public MemoryEfficientValueSet(Cache<K, V> cache, Set<Entry<K, V>> entries) {
			this.cache = cache;
			this.entries = entries;
		}

		@Override
		public boolean contains(Object value) {
			Set<Object> values = new HashSet<Object>();
			values.add(value);
			return containsAll(values);
		}

		@Override
		public boolean containsAll(Collection<?> values) {
			return new ContainsValueTask<K, V>(cache, values).executeSync();
		}

		@Override
		public Iterator<V> iterator() {
			final Iterator<Entry<K, V>> it = entries.iterator();
			return new Iterator<V>() {
				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public V next() {
					return it.next().getValue();
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		
		@Override
		public int size() {
			return entries.size();
		}
	}

}
