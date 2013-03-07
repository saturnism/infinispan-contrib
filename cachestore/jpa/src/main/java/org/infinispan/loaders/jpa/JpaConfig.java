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

/**
 * 
 * @author <a href="mailto:rtsang@redhat.com">Ray Tsang</a>
 *
 */
public class JpaConfig implements Cloneable {
	private String persistenceUnitName;
	private String entityClassName;
	private Class<?> entityClass;

	private static final long DEFAULT_BATCH_SIZE = 2L;
	private long batchSize = DEFAULT_BATCH_SIZE;

	public String getPersistenceUnitName() {
		return persistenceUnitName;
	}

	public void setPersistenceUnitName(String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
	}

	public String getEntityClassName() {
		return entityClassName;
	}

	public void setEntityClassName(String entityClassName)
			throws ClassNotFoundException {
		this.entityClassName = entityClassName;
		entityClass = this.getClass().getClassLoader()
				.loadClass(entityClassName);
	}
	
	public Class<?> getEntityClass() {
		return entityClass;
	}
	
	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
		entityClassName = entityClass.getName();
	}

	@Override
	public JpaConfig clone() {
		try {
			return (JpaConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return "JpaConfig [persistenceUnitName=" + persistenceUnitName
				+ ", entityClassName=" + entityClassName + ", entityClass="
				+ entityClass + ", batchSize=" + batchSize + "]";
	}

	public long getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(long batchSize) {
		this.batchSize = batchSize;
	}
}
