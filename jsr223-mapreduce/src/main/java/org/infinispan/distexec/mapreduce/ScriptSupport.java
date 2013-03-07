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
package org.infinispan.distexec.mapreduce;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.infinispan.CacheException;
import org.infinispan.marshall.AdvancedExternalizer;

public abstract class ScriptSupport {
   final String engineName;
   final String fileName;
   final String script;
   final String functionName;
   
   final Map<String, Object> variables;
   protected final transient Invocable invocable;
   
   public ScriptSupport(String engineName, String fileName, String script, Map<String, Object> variables, String functionName) {
      this(new ScriptEngineManager().getEngineByName(engineName), fileName, script, variables, functionName);
   }
   public ScriptSupport(ScriptEngine engine, String fileName, String script, Map<String, Object> variables, String functionName) {
      if (engine == null)
         throw new NullPointerException("engine cannot be null");
      if (script == null)
         throw new NullPointerException("script cannot be null");
      
      this.engineName = engine.getFactory().getLanguageName();
      this.script = script;
      this.variables = variables;
      this.fileName = fileName;
      this.functionName = (functionName == null ? getDefaultFunctionName() : functionName);
      
      // This is useful for stack trace
      if (fileName != null)
         engine.put(ScriptEngine.FILENAME, fileName);

      try {
         if (variables != null) {
            for (Entry<String, Object> entry : variables.entrySet()) {
               engine.put(entry.getKey(), entry.getValue());
            }
         }
         engine.eval(script);
         if (!(engine instanceof Invocable)) {
            throw new IllegalArgumentException("engine name '" + engineName
                  + "' does not implement Invocable interface");
         }

         invocable = (Invocable) engine;
      } catch (ScriptException e) {
         throw new IllegalArgumentException("script couldn't be evaluated", e);
      }
   }
   
   abstract protected String getDefaultFunctionName();
   
   protected Object invoke(String function, Object ... args) {
      try {
         return invocable.invokeFunction(function, args);
      } catch (ScriptException e) {
         throw new CacheException("script exception", e);
      } catch (NoSuchMethodException e) {
         throw new CacheException("no such method", e);
      }
   }
   
   public abstract static class ExternalizerSupport<T extends ScriptSupport> implements AdvancedExternalizer<T> {
      /** The serialVersionUID */
      private static final long serialVersionUID = 8834420819055541815L;
      
      @Override
      public void writeObject(ObjectOutput output, T object) throws IOException {
         output.writeObject(object.engineName);
         output.writeObject(object.fileName);
         output.writeObject(object.script);
         output.writeObject(object.functionName);
         output.writeObject(object.variables);
      }
      
      @SuppressWarnings("unchecked")
      @Override
      public T readObject(ObjectInput input) throws IOException, ClassNotFoundException {
         String engineName = (String) input.readObject();
         String fileName = (String) input.readObject();
         String script = (String) input.readObject();
         String functionName =  (String) input.readObject();
         Map<String, Object> variables = (Map<String, Object>) input.readObject();
         return createObject(engineName, fileName, script, variables, functionName);
      }
      
      protected abstract T createObject(String engineName, String fileName, String script, Map<String, Object> variables, String functionName);
   }
}
