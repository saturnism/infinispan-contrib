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

/**
 * 
 * @author <a href="mailto:rtsang@redhat.com">Ray Tsang</a>
 *
 */
public interface CollectionsManager {
	public <T> IspnSet<T> getSet(String name);
	public <T> IspnSet<T> getSet(Cache<T, Boolean> cache);
	
	public <K, V> IspnMap<K, V> getMap(String name);
	public <K, V> IspnMap<K, V> getMap(Cache<K, V> cache);
	
	public <MK, E> AtomicHashSet<E> getAtomicHashSet(String name, MK key);
	public <MK, E> AtomicHashSet<E> getAtomicHashSet(Cache<MK, ?> cache, MK key);
	
	public <MK, E> void removeAtomicHashSet(Cache<MK, ?> cache, MK key);
	public <MK, E> AppendOnlyCollection<E> getAppendOnlyCollection(Cache<MK, ?> cache, MK key);
   public <MK, E> AppendOnlyCollection<E> getAppendOnlyCollection(Cache<MK, ?> cache, MK key, boolean createIfAbsent);
}
