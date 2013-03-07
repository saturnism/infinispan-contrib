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
 * An atomic clear operation.
 * <p/>
 *
 * @author (various)
 * @param <K>
 * @param <V>
 * @since 4.0
 */
public class ClearOperation<E> extends Operation<E> {
   /** The serialVersionUID */
   private static final long serialVersionUID = -4982742211883004626L;
   
   Collection<E> originalEntries;

   ClearOperation() {
   }

   ClearOperation(Collection<E> originalEntries) {
      this.originalEntries = originalEntries;
   }

   @Override
   public void rollback(Collection<E> delegate) {
      if (!originalEntries.isEmpty()) delegate.addAll(originalEntries);
   }

   @Override
   public void replay(Collection<E> delegate) {
      delegate.clear();
   }
   
   @SuppressWarnings("rawtypes")
   public static class Externalizer extends AbstractExternalizer<ClearOperation> {
      /** The serialVersionUID */
      private static final long serialVersionUID = -3803678563446779547L;

      @Override
      public void writeObject(ObjectOutput output, ClearOperation object) throws IOException {
         output.writeObject(object.originalEntries);
      }

      @SuppressWarnings("unchecked")
      @Override
      public ClearOperation readObject(ObjectInput input) throws IOException, ClassNotFoundException {
         ClearOperation op = new ClearOperation();
         op.originalEntries = (Collection<Object>) input.readObject();
         return op;
      }

      @Override
      public Integer getId() {
         return ExternalizerIds.CLEAR_OPERATION;
      }

      @SuppressWarnings("unchecked")
      @Override
      public Set<Class<? extends ClearOperation>> getTypeClasses() {
         return Util.<Class<? extends ClearOperation>>asSet(ClearOperation.class);
      }
   }
}