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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.infinispan.Cache;
import org.infinispan.collections.IspnMap;
import org.infinispan.collections.tasks.IsEmptyTask;
import org.infinispan.collections.tasks.KeySetTask;
import org.infinispan.collections.tasks.SizeTask;
import org.infinispan.collections.tasks.ValueSetTask;
import org.infinispan.collections.util.MapReduceUtil;

public class IspnMapImpl<K, V> implements IspnMap<K, V>, ConcurrentMap<K, V> {
	private final Cache<K, V> cache;
	private final boolean needsMapReduce;
	
	public IspnMapImpl(Cache<K, V> cache) {
		this.cache = cache;
		needsMapReduce = MapReduceUtil.needsMapReduce(cache);
	}
	
	@Override
	public Set<K> keySet() {
		if (!needsMapReduce)
			return cache.keySet();
		
		return new KeySetTask<K, V>(cache).executeSync();
	}
	
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return cache.entrySet();
	}
	
	@Override
	public int size() {
		if (!needsMapReduce)
			return cache.size();
		
		return new SizeTask<K, V>(cache).executeSync();
	}
	
	@Override
	public boolean isEmpty() {
		if (!needsMapReduce)
			return cache.isEmpty();
		
		return new IsEmptyTask<K, V>(cache).executeSync();
	}
	
	@Override
	public Collection<V> values() {
		if (!needsMapReduce)
			return cache.values();
		
		return new ValueSetTask<K, V>(cache).executeSync();
	}
	

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return cache.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return cache.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return cache.get(key);
	}

	@Override
	public V put(K key, V value) {
		return cache.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		cache.putAll(map);
	}

	@Override
	public V remove(Object key) {
		return cache.remove(key);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return cache.putIfAbsent(key, value);
	}

	@Override
	public boolean remove(Object key, Object value) {
		return cache.remove(key, value);
	}

	@Override
	public V replace(K key, V value) {
		return cache.replace(key, value);
	}

	@Override
	public boolean replace(K key, V value1, V value2) {
		return cache.replace(key, value1, value2);
	}

}
