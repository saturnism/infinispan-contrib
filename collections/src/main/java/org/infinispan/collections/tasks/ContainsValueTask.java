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

import java.util.Collection;
import java.util.Map;

import org.infinispan.Cache;
import org.infinispan.distexec.mapreduce.Collator;

public class ContainsValueTask<K, V> extends AbstractDistribtuedTask<K, V, Boolean> {
	private final Collection<?> valuesToFind;
	
	public ContainsValueTask(Cache<K, V> cache, Collection<?> valuesToFind) {
		super(cache);
		this.valuesToFind = valuesToFind;
	}

	@Override
	public Boolean executeSync() {
		return new ContainsValueMapReduceTask<K, V>(getCache(), valuesToFind).execute(
				new Collator<String, Boolean, Boolean>() {
					@Override
					public Boolean collate(Map<String, Boolean> reducedResults) {
						if (!reducedResults.containsKey(ContainsValueMapReduceTask.RESULT_KEY)) return false;
						return reducedResults.get(ContainsValueMapReduceTask.RESULT_KEY);
					}
		});
	}

	
}
