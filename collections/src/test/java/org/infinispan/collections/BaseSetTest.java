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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.infinispan.test.MultipleCacheManagersTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test (groups = "functional", testName = "collections.BaseSetTest")
public abstract class BaseSetTest extends MultipleCacheManagersTest {
	protected Set<String> set;
	
	abstract protected Set<String> createSet();
	
	@BeforeMethod(dependsOnMethods="createBeforeMethod")
	public void setUp() {
		set = createSet();
	}

	@AfterMethod
	public void tearDown() {
	}
	
	public void testAdd() {
		assert set.add("Hello");
		assert set.contains("Hello");
	}
	
	public void testRemove() {
		assert set.add("Hello");
		assert set.contains("Hello");
		assert set.remove("Hello");
		assert !set.contains("Hello");
		assert set.remove("none") == false;
	}
	
	public void testAddAll() {
		List<String> items = Arrays.asList("1", "2", "3", "4");
		assert set.addAll(items);
		for (String item : items) {
			assert set.contains(item);
		}
	}
	
	public void testSize() {
		final int count = 100;
		for (int i = 0; i < count; i++) {
			assert set.add(String.valueOf(i));
		}
		
		assert set.size() == count;
	}
	
	public void testRetainAll() {
		List<String> items = Arrays.asList("1", "2", "3", "4");
		assert set.addAll(items);
		assert set.add("5");
		
		boolean expectedExceptionCaught = false;
		try {
			set.retainAll(items);
		} catch (UnsupportedOperationException e) {
			expectedExceptionCaught = true;
		}
		
		assert expectedExceptionCaught;
	}
	
	public void testClear() {
		assert set.add("Hello");
		assert set.size() == 1;
		set.clear();
		assert set.isEmpty();
		assert set.size() == 0;
	}
}
