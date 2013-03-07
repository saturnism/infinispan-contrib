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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.infinispan.test.MultipleCacheManagersTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test (groups = "functional", testName = "collections.BaseMapTest")
public abstract class BaseMapTest extends MultipleCacheManagersTest {
	protected ConcurrentMap<String, String> map;
	protected Map<String, String> testMap;
	
	abstract protected ConcurrentMap<String, String> createMap();
	
	@BeforeMethod(dependsOnMethods="createBeforeMethod")
	public void setUp() {
		map = createMap();
		
		testMap = new HashMap<String, String>();
		for (int i = 0; i < 100; i++) {
			testMap.put(String.valueOf(i), String.valueOf(i));
		}
	}

	@AfterMethod
	public void tearDown() {
	}
	
	public void testSize() {
		map.putAll(testMap);
		assert map.size() > 0;
		assert map.size() == testMap.size();
	}
	
	public void testIsEmpty() {
		map.putAll(testMap);
		assert !map.isEmpty();
		
		map.clear();
		assert map.isEmpty();
	}
	
	public void testKeySet() {
		map.putAll(testMap);
		Set<String> keys = map.keySet();
		Set<String> testKeys = testMap.keySet();
		assert keys.size() == testKeys.size();
		for (String key : keys) {
			assert testKeys.contains(key);
		}
	}
	
	public void testEntrySet() {
		map.putAll(testMap);
		Set<Entry<String, String>> entries = map.entrySet();
		Set<Entry<String, String>> testEntries = testMap.entrySet();
		
		assert entries.size() == map.size();
		assert entries.size() == testMap.size();
		for (Entry<String, String> testEntry : testEntries) {
			assert entries.contains(testEntry);
		}
		for (Entry<String, String> entry : entries) {
			assert testEntries.contains(entry);
		}
		
		Object [] entriesArray = entries.toArray();
		assert entriesArray.length == testMap.entrySet().size();
		Entry<?, ?> [] entriesArray2 = entries.toArray(new Entry[]{});
		assert entriesArray2.length == testMap.entrySet().size();
		Entry<?, ?> [] entriesArray3 = entries.toArray(new Entry[200]);
		assert entriesArray3.length == 200;
	}
	
	public void testValues() {
		map.putAll(testMap);
		Collection<String> values = map.values();
		Collection<String> testValues = testMap.values();
		assert values.size() > 0;
		assert values.size() == testValues.size();
		for (String value : values) {
			assert testValues.contains(value);
		}
		for (String testValue : testValues) {
			assert values.contains(testValue);
		}
		
		Object [] valuesArray = values.toArray();
		assert valuesArray.length == testMap.values().size();
		String [] valuesArray2 = values.toArray(new String[]{});
		assert valuesArray2.length == testMap.values().size();
		String [] valuesArray3 = values.toArray(new String[200]);
		assert valuesArray3.length == 200;
	}
}
