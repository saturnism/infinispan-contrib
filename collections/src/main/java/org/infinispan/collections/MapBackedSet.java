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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MapBackedSet<E> implements Set<E> {
	private final Map<E, Object> map;
	
	private static final Boolean DUMMY = Boolean.TRUE;
	
	@SuppressWarnings("unchecked")
   public MapBackedSet(Map<E, ?> map) {
		this.map = (Map<E, Object>) map;
	}

	@Override
	public boolean add(E e) {
		Object old = map.put(e, DUMMY);
		return old == null;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean changed = false;
		for (E e : c) {
			changed |= add(e);
		}
		
		return changed;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		
		if (c == null) throw new NullPointerException();
		if (c.isEmpty()) return false;
		
		boolean contains = true;
		for (Object o : c) {
			contains &= map.containsKey(o);
		}
		
		return contains;
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public boolean remove(Object o) {
		Object old = map.remove(o);
		return old != null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (c == null) throw new NullPointerException();
		if (c.isEmpty()) return false;
		
		boolean changed = false;
		for (Object o : c) {
			changed |= remove(o);
		}
		
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Object[] toArray() {
		return map.entrySet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return map.entrySet().toArray(a);
	}

}
