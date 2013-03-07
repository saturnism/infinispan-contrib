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

import org.infinispan.distexec.mapreduce.Collector;
import org.infinispan.distexec.mapreduce.Mapper;

public class ContainsValueMapper<K, V> implements Mapper<K, V, String, Boolean> {
	private static final long serialVersionUID = -7525270924253342546L;
	
	private final Collection<?> valuesToFind;

	public ContainsValueMapper(Collection<?> valuesToFind) {
		this.valuesToFind = valuesToFind;
	}

	@Override
	public void map(K key, V value, Collector<String, Boolean> collector) {
		collector.emit(ContainsValueMapReduceTask.RESULT_KEY, valuesToFind.contains(value));		
	}
}
