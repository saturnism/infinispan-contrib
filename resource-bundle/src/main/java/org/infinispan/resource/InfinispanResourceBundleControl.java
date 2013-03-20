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

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import org.infinispan.Cache;
import org.infinispan.api.BasicCacheContainer;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class InfinispanResourceBundleControl extends Control {
   public static final String DEFAULT_PREFIX = "RB_";

   private final BasicCacheContainer cacheManager;
   private final String prefix;

   public InfinispanResourceBundleControl(BasicCacheContainer cacheManager) {
      this(cacheManager, DEFAULT_PREFIX);
   }

   public InfinispanResourceBundleControl(BasicCacheContainer cacheManager, String prefix) {
      if (!(cacheManager instanceof EmbeddedCacheManager) && !(cacheManager instanceof RemoteCacheManager))
         throw new IllegalArgumentException("you can only use EmbeddedCacheManager or RemoteCacheManager");

      this.cacheManager = cacheManager;
      this.prefix = prefix;
   }

   @Override
   public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
         throws IllegalAccessException, InstantiationException, IOException {
      
      if (baseName == null || locale == null || format == null || loader == null)
         throw new NullPointerException();
      
      String localeString = locale.toString();
      String cacheName = prefix + baseName + ("".equals(localeString) ? "" : "_" + locale.toString());
      
      if (cacheManager instanceof EmbeddedCacheManager) {
         EmbeddedCacheManager ecm = (EmbeddedCacheManager) cacheManager;
         if (!ecm.cacheExists(cacheName)) return null;
         Cache<String, String> cache = ecm.getCache(cacheName);
         
         return new InfinispanResourceBundle(cache);
      } else if (cacheManager instanceof RemoteCacheManager) {
         RemoteCacheManager rcm =  (RemoteCacheManager) cacheManager;
         RemoteCache<String, String> cache = rcm.getCache(cacheName);
         if (cache == null) return null;
         
         return new InfinispanResourceBundle(cache);
      } else {
         throw new IllegalStateException("this shouldn't happen...");
      }
   }
}
