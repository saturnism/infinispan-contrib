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
package org.infinispan.collections.impl;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.infinispan.atomic.Delta;
import org.infinispan.atomic.NullDelta;
import org.infinispan.collections.AppendOnlyCollection;
import org.infinispan.collections.ExternalizerIds;
import org.infinispan.marshall.AdvancedExternalizer;
import org.infinispan.util.Util;

public class AppendOnlyCollectionImpl<E> implements AppendOnlyCollection<E> {
   protected Collection<E> delegate;
   private AppendOnlyCollectionDelta delta;
   private Type type;
   
   volatile boolean copied = false;
   volatile boolean removed = false;
   
   public AppendOnlyCollectionImpl() {
      this(Type.LIST);
   }
   
   public AppendOnlyCollectionImpl(Type type) {
      if (type == null)
         throw new NullPointerException("type cannot be null");
      
      switch (type) {
      case SET:
         this.delegate = new HashSet<E>();
         break;
      case SORTED:
         this.delegate = new ConcurrentSkipListSet<E>();
         break;
      case LIST:
      default:
         this.delegate = new LinkedList<E>();
      }
      
      this.type = type;
   }
   
   public AppendOnlyCollectionImpl(Collection<E> delegate) {
      if (delegate == null)
         throw new NullPointerException("delegate cannot be null");
      if (delegate instanceof List) {
         this.type = Type.LIST;
      } else if (delegate instanceof SortedSet) {
         this.type = Type.SORTED;
      } else if (delegate instanceof Set) {
         this.type = Type.SET;
      } else {
         throw new IllegalArgumentException("sorry, this collection type is not supported: " + delegate.getClass().getName());
      }
      
      this.delegate = delegate;
   }

   @Override
   public boolean add(E e) {
      boolean changed = delegate.add(e);
      if (!changed) return false;
      
      AddOperation<E> op = new AddOperation<E>(e);
      getDelta().addOperation(op);
      return true;
   }

   @Override
   public void clear() {
      Collection<E> originalEntries = new ArrayList<E>(delegate);
      ClearOperation<E> op = new ClearOperation<E>(originalEntries);
      getDelta().addOperation(op);
      delegate.clear();
   }
   
   AppendOnlyCollectionDelta getDelta() {
      if (delta == null) delta = new AppendOnlyCollectionDelta(type);
      return delta;
   }

   @Override
   public Delta delta() {
      Delta toReturn = delta == null ? NullDelta.INSTANCE : delta;
      delta = null; // reset
      return toReturn;
   }

   @Override
   public void commit() {
      copied = false;
      delta = null;
   }
   
   @Override
   public boolean isEmpty() {
      return delegate.isEmpty();
   }

   @Override
   public Iterator<E> iterator() {
      return delegate.iterator();
   }
   
   @Override
   public int size() {
      return delegate.size();
   }

   @Override
   public Object[] toArray() {
      return delegate.toArray();
   }

   @Override
   public <T> T[] toArray(T[] a) {
      return delegate.toArray(a);
   }
   
   @Override
   public boolean contains(Object o) {
      return delegate.contains(o);
   }

   @Override
   public boolean containsAll(Collection<?> c) {
      return delegate.containsAll(c);
   }

   @Override
   public boolean addAll(Collection<? extends E> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean remove(Object o) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }
   
   @SuppressWarnings("rawtypes")
   public static class Externalizer implements AdvancedExternalizer<AppendOnlyCollectionImpl> {
      /** The serialVersionUID */
      private static final long serialVersionUID = -5979582295533518349L;

      @Override
      public void writeObject(ObjectOutput output, AppendOnlyCollectionImpl collection)
            throws IOException {
         output.writeObject(collection.delegate);
      }
 
      @SuppressWarnings("unchecked")
      @Override
      public AppendOnlyCollectionImpl readObject(ObjectInput input)
            throws IOException, ClassNotFoundException {
         Collection<?> delegate = (Collection<?>) input.readObject();
         return new AppendOnlyCollectionImpl(delegate);
      }
 
      @SuppressWarnings("unchecked")
      @Override
      public Set<Class<? extends AppendOnlyCollectionImpl>> getTypeClasses() {
         return Util.<Class<? extends AppendOnlyCollectionImpl>>asSet(AppendOnlyCollectionImpl.class);
      }
 
      @Override
      public Integer getId() {
         return ExternalizerIds.APPEND_ONLY_COLLECTION;
      }
   }

   @Override
   public Collection<E> getDelegate() {
      return delegate;
   }
   
   @Override
   public String toString() {
      return delegate.toString();
   }
}
