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

import java.util.Iterator;

import org.infinispan.Cache;
import org.infinispan.collections.IspnSet;
import org.infinispan.collections.MapBackedSet;
import org.infinispan.collections.tasks.KeySetTask;
import org.infinispan.collections.util.MapReduceUtil;

public class IspnSetImpl<E> extends MapBackedSet<E> implements IspnSet<E> {
	private final Cache<E, Boolean> cache;
	private final boolean needsMapReduce;
	
	public IspnSetImpl(Cache<E, Boolean> cache) {
		super(new IspnMapImpl<E, Boolean>(cache));
		this.cache = cache;
		this.needsMapReduce = MapReduceUtil.needsMapReduce(cache);
	}
	
	@Override
	public Iterator<E> iterator() {
		if (!needsMapReduce)
			return super.iterator();
		
		return new KeySetTask<E, Boolean>(cache).executeSync().iterator();
	}
}
