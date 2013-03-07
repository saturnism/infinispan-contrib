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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.infinispan.atomic.Delta;
import org.infinispan.atomic.DeltaAware;
import org.infinispan.collections.AppendOnlyCollection.Type;
import org.infinispan.collections.ExternalizerIds;
import org.infinispan.marshall.AdvancedExternalizer;
import org.infinispan.util.Util;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

/**
 * Changes that have occurred on an AtomicHashMap
 *
 * @author Manik Surtani (<a href="mailto:manik AT jboss DOT org">manik AT jboss DOT org</a>)
 * @since 4.0
 */
public class AppendOnlyCollectionDelta implements Delta, Serializable {
   /** The serialVersionUID */
   private static final long serialVersionUID = -5959697794206271699L;
   
   private static final Log log = LogFactory.getLog(AppendOnlyCollectionDelta.class);
   private static final boolean trace = log.isTraceEnabled();

   List<Operation<Object>> changeLog;
   private boolean hasClearOperation;
   private Type type;
   
   public AppendOnlyCollectionDelta(Type type) {
      this.type = type;
   }

   @SuppressWarnings("unchecked")
   @Override
   public DeltaAware merge(DeltaAware d) {
      AppendOnlyCollectionImpl<Object> other;
      if (d != null && (d instanceof AppendOnlyCollectionImpl))
         other = (AppendOnlyCollectionImpl<Object>) d;
      else
         other = new AppendOnlyCollectionImpl<Object>(type);
      if (changeLog != null) {
         for (Operation<Object> o : changeLog) o.replay(other.delegate);
      }      
      return other;
   }
   
   @SuppressWarnings("unchecked")
   public void addOperation(Operation<?> o) {
      if (changeLog == null) {
         // lazy init
         changeLog = new LinkedList<Operation<Object>>();
      }
      if(o instanceof ClearOperation) {
         hasClearOperation = true;
      }
      changeLog.add((Operation<Object>) o);
   }
      
   public boolean hasClearOperation(){
      return hasClearOperation;
   }

   public int getChangeLogSize() {
      return changeLog == null ? 0 : changeLog.size();
   }
   
   public static class Externalizer implements AdvancedExternalizer<AppendOnlyCollectionDelta> {
      /** The serialVersionUID */
      private static final long serialVersionUID = -1479391365189519665L;

      @Override
      public void writeObject(ObjectOutput output, AppendOnlyCollectionDelta delta)
            throws IOException {
         if (trace) log.tracef("Serializing changeLog %s", delta.changeLog);
         output.writeObject(delta.type.ordinal());
         output.writeObject(delta.changeLog);
      }
 
      @SuppressWarnings("unchecked")
      @Override
      public AppendOnlyCollectionDelta readObject(ObjectInput input)
            throws IOException, ClassNotFoundException {
         int typeOrdinal = input.readInt();
         Type type = Type.values()[typeOrdinal];
         AppendOnlyCollectionDelta delta = new AppendOnlyCollectionDelta(type);
         delta.changeLog = (List<Operation<Object>>) input.readObject();
         if (trace) log.tracef("Deserialized changeLog %s", delta.changeLog);
         return delta;
      }
 
      @SuppressWarnings("unchecked")
      @Override
      public Set<Class<? extends AppendOnlyCollectionDelta>> getTypeClasses() {
         return Util.<Class<? extends AppendOnlyCollectionDelta>>asSet(AppendOnlyCollectionDelta.class);
      }
 
      @Override
      public Integer getId() {
         return ExternalizerIds.APPEND_ONLY_COLLECTION_DELTA;
      }
   }


}