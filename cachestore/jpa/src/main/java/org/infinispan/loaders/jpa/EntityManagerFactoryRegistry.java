/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010 Red Hat Inc. and/or its affiliates and other
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
 * 
 */
package org.infinispan.loaders.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * 
 * @author <a href="mailto:rtsang@redhat.com">Ray Tsang</a>
 *
 */
public class EntityManagerFactoryRegistry {
	private static final EntityManagerFactoryRegistry INSTANCE = new EntityManagerFactoryRegistry();
	
	private Map<String, EntityManagerFactory> registry = new HashMap<String, EntityManagerFactory>();
	public Map<String, Long> usage =  new HashMap<String, Long>();
	
	private EntityManagerFactoryRegistry() {
	}
	
	public EntityManagerFactory getEntityManagerFactory(String persistenceUnitName) {
		synchronized (registry) {
			if (!registry.containsKey(persistenceUnitName)) {
				EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName);
				registry.put(persistenceUnitName, emf);	
			}
			
			incrementUsage(persistenceUnitName);
			return registry.get(persistenceUnitName);
		}
	}
	
	public void closeEntityManagerFactory(String persistenceUnitName) {
		synchronized (registry) {
			if (!registry.containsKey(persistenceUnitName)) {
				return;
			}
			
			decrementUsage(persistenceUnitName);
			if (getUsage(persistenceUnitName) == 0) {
				EntityManagerFactory emf = registry.remove(persistenceUnitName);
				emf.close();
			}
		}
	}
	
	protected void incrementUsage(String persistenceUnitName) {
		synchronized (usage) {
			if (usage.containsKey(persistenceUnitName))
				usage.put(persistenceUnitName, usage.get(persistenceUnitName) + 1);
			else
				usage.put(persistenceUnitName, 1L);
		}
	}
	
	protected void decrementUsage(String persistenceUnitName) {
		synchronized (usage) {
			if (usage.containsKey(persistenceUnitName)) {
				long x = usage.get(persistenceUnitName);
				if (x > 0)
					usage.put(persistenceUnitName, x - 1);
			}
		}
	}
	
	protected long getUsage(String persistenceUnitName) {
		synchronized (usage) {
			if (usage.containsKey(persistenceUnitName))
				return usage.get(persistenceUnitName);
			else
				return 0;
		}
	}
	
	public static EntityManagerFactoryRegistry getInstance() {
		return INSTANCE;
	}
	
}
