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

import java.util.Locale;
import java.util.ResourceBundle;

import org.infinispan.api.BasicCache;
import org.infinispan.api.BasicCacheContainer;
import org.infinispan.client.hotrod.test.MultiHotRodServersTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

@Test (groups = "functional", testName = "resources.BaseResourceBundleTest")
public abstract class BaseResourceBundleTest extends MultiHotRodServersTest {

   protected abstract BasicCacheContainer getCacheContainer();
   
   @BeforeMethod(alwaysRun = true)
   public void populateCache() {
      BasicCacheContainer container = getCacheContainer();
      BasicCache<String, String> defaultBundle = container.getCache("RB_test");
      defaultBundle.put("hello", "Hello");
      defaultBundle.put("bye", "Goodbye");
      defaultBundle.put("oops", "Oops");
      
      BasicCache<String, String> frenchBundle = container.getCache("RB_test_fr");
      frenchBundle.put("hello", "Bonjour");
      frenchBundle.put("bye", "Adieu");
      
      BasicCache<String, String> frenchBundle2 = container.getCache("RB_test_fr_CA");
      frenchBundle2.put("bye", "Au Revoir");
   }
   
   @Test
   public void testResourceBundle() {
      ResourceBundle bundle = null;
      bundle = ResourceBundle.getBundle("test", new Locale("fr", "FR"), new InfinispanResourceBundleControl(getCacheContainer()));
      
      Assert.assertEquals(bundle.getString("hello"), "Bonjour");
      Assert.assertEquals(bundle.getString("bye"), "Adieu");
      Assert.assertEquals(bundle.getString("oops"), "Oops");
   }
}
