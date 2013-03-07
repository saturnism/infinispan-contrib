/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other
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
package org.infinispan.collections.impl;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Set;

import org.infinispan.atomic.Delta;
import org.infinispan.atomic.DeltaAware;
import org.infinispan.collections.AppendOnlyCollection;
import org.infinispan.collections.ExternalizerIds;
import org.infinispan.collections.AppendOnlyCollection.Type;
import org.infinispan.marshall.AdvancedExternalizer;
import org.infinispan.util.Util;

/**
 * Changes that have occurred on an AtomicHashMap
 *
 * @author Manik Surtani (<a href="mailto:manik AT jboss DOT org">manik AT jboss DOT org</a>)
 * @since 4.0
 */
public class AppendDelta<E> implements Delta, Serializable {
   /** The serialVersionUID */
   private static final long serialVersionUID = 3250977717093617051L;

   AddOperation<Object> addOperation;
   Type type = Type.LIST;
   
   public AppendDelta() {
   }
   
   public AppendDelta(E e) {
      this.addOperation = new AddOperation<Object>(e);
   }
   
   public AppendDelta(Type type, E e) {
      this.type = type;
      this.addOperation = new AddOperation<Object>(e);
   }

   @SuppressWarnings("unchecked")
   @Override
   public DeltaAware merge(DeltaAware d) {
      AppendOnlyCollection<Object> other;
      if (d != null && (d instanceof AppendOnlyCollection))
         other = (AppendOnlyCollection<Object>) d;
      else
         other = new AppendOnlyCollectionImpl<Object>(type);
      
      if (addOperation != null) {
         addOperation.replay(other.getDelegate());
      }      
      return other;
   }
   
   @SuppressWarnings({ "rawtypes" })
   public static class Externalizer implements AdvancedExternalizer<AppendDelta> {

      /** The serialVersionUID */
      private static final long serialVersionUID = -8301128219029112651L;

      @Override
      public void writeObject(ObjectOutput output, AppendDelta delta)
            throws IOException {
         output.writeInt(delta.type.ordinal());
         output.writeObject(delta.addOperation);
      }
 
      @SuppressWarnings("unchecked")
      @Override
      public AppendDelta readObject(ObjectInput input)
            throws IOException, ClassNotFoundException {
         AppendDelta delta = new AppendDelta();
         int typeOrdinal = input.readInt();
         delta.type = Type.values()[typeOrdinal];
         delta.addOperation = (AddOperation<Object>) input.readObject();
         return delta;
      }
 
      @SuppressWarnings("unchecked")
      @Override
      public Set<Class<? extends AppendDelta>> getTypeClasses() {
         return Util.<Class<? extends AppendDelta>>asSet(AppendDelta.class);
      }
 
      @Override
      public Integer getId() {
         return ExternalizerIds.APPEND_DELTA;
      }
   }
}