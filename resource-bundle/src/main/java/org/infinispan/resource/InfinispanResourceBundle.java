/* 
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.infinispan.resource;

import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.infinispan.Cache;
import org.infinispan.api.BasicCache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.collections.impl.IspnMapImpl;

public class InfinispanResourceBundle extends ResourceBundle {
   private final Map<String, String> cache;
   
   public InfinispanResourceBundle(BasicCache<String, String> cache) {
      if (cache instanceof Cache)
         this.cache = new IspnMapImpl<String, String>((Cache<String, String>) cache);
      else if (cache instanceof RemoteCache)
         this.cache = cache;
      else
         throw new IllegalArgumentException("you can only use Cache or RemoteCache");
   }

   @Override
   public Enumeration<String> getKeys() {
      return new ResourceBundleEnumeration(cache.keySet(), parent == null ? null : parent.getKeys());
   }
   
   @Override
   protected Set<String> handleKeySet() {
      return cache.keySet();
   }

   @Override
   protected Object handleGetObject(String key) {
      return cache.get(key);
   }

}
