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
import java.util.Collection;
import java.util.Set;

import org.infinispan.collections.ExternalizerIds;
import org.infinispan.marshall.AbstractExternalizer;
import org.infinispan.util.Util;

/**
 * An atomic put operation.
 * <p/>
 *
 * @author (various)
 * @param <K>
 * @param <V>
 * @since 4.0
 */
public class AddOperation<E> extends Operation<E> {
   /** The serialVersionUID */
   private static final long serialVersionUID = -4696701825417389878L;
   
   E newValue;

   public AddOperation() {
   }

   AddOperation(E newValue) {
      this.newValue = newValue;
   }

   @Override
   public void rollback(Collection<E> delegate) {
      delegate.remove(newValue);
   }

   @Override
   public void replay(Collection<E> delegate) {
      delegate.add(newValue);
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      
      @SuppressWarnings("unchecked")
      AddOperation<Object> other = (AddOperation<Object>) obj;
      
      if (newValue == null) {
         if (other.newValue != null)
            return false;
      } else if (!newValue.equals(other.newValue))
         return false;
      return true;
   }

   @Override
   public String toString() {
      return "AddOperation [newValue=" + newValue + "]";
   }
   
   @SuppressWarnings("rawtypes")
   public static class Externalizer extends AbstractExternalizer<AddOperation> {
      /** The serialVersionUID */
      private static final long serialVersionUID = -7353471387320198259L;

      @Override
      public void writeObject(ObjectOutput output, AddOperation object) throws IOException {
         output.writeObject(object.newValue);
      }

      @SuppressWarnings("unchecked")
      @Override
      public AddOperation readObject(ObjectInput input) throws IOException, ClassNotFoundException {
         AddOperation op = new AddOperation();
         op.newValue = (Collection<Object>) input.readObject();
         return op;
      }

      @Override
      public Integer getId() {
         return ExternalizerIds.ADD_OPERATION;
      }

      @SuppressWarnings("unchecked")
      @Override
      public Set<Class<? extends AddOperation>> getTypeClasses() {
         return Util.<Class<? extends AddOperation>>asSet(AddOperation.class);
      }
   }
}