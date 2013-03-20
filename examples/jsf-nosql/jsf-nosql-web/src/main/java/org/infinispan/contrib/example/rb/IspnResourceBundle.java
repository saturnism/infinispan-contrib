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
package org.infinispan.contrib.example.rb;

import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.resource.InfinispanResourceBundleControl;

public class IspnResourceBundle extends ResourceBundle {
   public static final String BUNDLE_NAME = "app";
   
   public IspnResourceBundle() throws NamingException {
      Context ctx = new InitialContext();
      BeanManager beanManager = (BeanManager)ctx.lookup("java:comp/BeanManager");
      Bean<?> bean = beanManager.resolve(beanManager.getBeans(EmbeddedCacheManager.class));
      EmbeddedCacheManager cacheManager = (EmbeddedCacheManager) beanManager.getReference(bean, EmbeddedCacheManager.class, beanManager.createCreationalContext(bean));

      setParent(ResourceBundle.getBundle(BUNDLE_NAME, 
            FacesContext.getCurrentInstance().getViewRoot().getLocale(), 
            new InfinispanResourceBundleControl(cacheManager)));
   }

   @Override
   public Enumeration<String> getKeys() {
      return parent.getKeys();
   }

   @Override
   protected Object handleGetObject(String key) {
      return parent.getObject(key);
   }

}
