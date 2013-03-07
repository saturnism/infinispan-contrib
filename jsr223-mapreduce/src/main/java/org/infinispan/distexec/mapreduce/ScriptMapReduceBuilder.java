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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class ScriptMapReduceBuilder {
   public static final char EXTENSION_SEPARATOR = '.';

   private ScriptEngineManager engineManager = new ScriptEngineManager();
   private StringBuffer script = new StringBuffer();
   private String fileName;
   private String functionName;
   private ScriptEngine engine;
   private Map<String, Object> variables;

   public ScriptMapReduceBuilder() {
   }

   public ScriptEngine getEngine() {
      return engine;
   }

   public String getScript() {
      return script.toString();
   }

   public String getFileName() {
      return fileName;
   }

   public ScriptMapReduceBuilder fileName(String fileName) {
      this.fileName = fileName;

      return this;
   }

   public ScriptMapReduceBuilder loadFromFileName(String fileName) throws FileNotFoundException, IOException {
      return loadFromFile(new File(fileName));
   }

   public ScriptMapReduceBuilder loadFromFile(File file) throws IOException, FileNotFoundException {
      return loadFromFile(file, true);
   }

   public ScriptMapReduceBuilder loadFromFile(File file, boolean engineByExtension) throws IOException, FileNotFoundException {
      FileInputStream fis = null;
      try {
         fis = new FileInputStream(file);
         ScriptMapReduceBuilder builder = loadFromInputStream(file.getPath(), fis);
         if (engineByExtension)
            builder.engineByExtension(getExtension(file.getName()));
         
         return builder;
      } finally {
         if (fis != null) {
            try {
               fis.close();
            } catch (IOException e) {
               // do nothing
            }
         }
      }
   }

   public ScriptMapReduceBuilder loadFromInputStream(InputStream is) throws IOException {
      return loadFromInputStream(null, is);
   }

   public ScriptMapReduceBuilder loadFromInputStream(String fileName, InputStream is) throws IOException {
      InputStreamReader reader = null;

      try {
         reader = new InputStreamReader(is);
         return loadFromReader(fileName, reader);
      } finally {
         if (reader != null) {
            try {
               reader.close();
            } catch (IOException e) {
               // do nothing
            }
         }
      }
   }

   public ScriptMapReduceBuilder loadFromReader(Reader reader) throws IOException {
      return loadFromReader(null, reader);
   }

   public ScriptMapReduceBuilder loadFromReader(String fileName, Reader reader) throws IOException {
      if (!(reader instanceof BufferedReader)) {
         BufferedReader bufferedReader = null;
         try {
            bufferedReader = new BufferedReader(reader);
            return loadFromReader(fileName, bufferedReader);
         } finally {
            if (bufferedReader != null) {
               try {
                  bufferedReader.close();
               } catch (IOException e) {
                  // do nothing;
               }
            }
         }
      }

      BufferedReader br = (BufferedReader) reader;
      checkAndSetFileName(fileName);
      
      if (script.length() > 0)
         this.script.append("\n");

      String line;
      while ((line = br.readLine()) != null) {
         script.append(line);
         script.append("\n"); // newline, apparently, is important
      }

      return this;
   }

   public ScriptMapReduceBuilder loadFromResource(String resourceName) throws IOException {
      return loadFromResource(resourceName, true);
   }
   
   public ScriptMapReduceBuilder loadFromResource(String resourceName, boolean engineByExtension) throws IOException {
      if (resourceName == null)
         throw new NullPointerException("resourceName cannot be null");
      InputStream is = null;

      try {
         is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
         if (is == null)
            throw new IllegalArgumentException(resourceName + " could not be opened");
         ScriptMapReduceBuilder builder = loadFromInputStream(resourceName, is);
         
         if (engineByExtension)
            builder.engineByExtension(getExtension(resourceName));
         
         return builder;
         
      } finally {
         if (is != null) {
            try {
               is.close();
            } catch (IOException e) {
               // do nothing
            }
         }
      }
   }

   public ScriptMapReduceBuilder loadFromString(String script) {
      return loadFromString(null, script);
   }

   public ScriptMapReduceBuilder loadFromString(String fileName, String script) {
      if (this.script.length() > 0)
         this.script.append("\n");
      
      this.script.append(script);
      
      checkAndSetFileName(fileName);

      return this;
   }

   public ScriptMapReduceBuilder engineByName(String engineName) {
      engine = engineManager.getEngineByName(engineName);
      if (engine == null)
         throw new IllegalArgumentException("Cannot find engine for name: " + engineName);
      return this;
   }

   public ScriptMapReduceBuilder engineByExtension(String extension) {
      engine = engineManager.getEngineByExtension(extension);
      if (engine == null)
         throw new IllegalArgumentException("Cannot find engine for extension: " + extension);

      return this;
   }

   public ScriptMapReduceBuilder engineByMimeType(String mimeType) {
      engine = engineManager.getEngineByMimeType(mimeType);
      if (engine == null)
         throw new IllegalArgumentException("Cannot find engine for mimeType: " + mimeType);
      return this;
   }

   public ScriptMapReduceBuilder engine(ScriptEngine engine) {
      if (engine == null)
         throw new NullPointerException("engine cannot be null");

      this.engine = engine;
      return this;
   }

   public ScriptMapReduceBuilder variables(Map<String, Object> variables) {
      this.variables = variables;
      return this;
   }
   
   public ScriptMapReduceBuilder functionName(String functionName) {
      this.functionName = functionName;
      return this;
   }

   public <KIn, VIn, KOut, VOut> ScriptMapper<KIn, VIn, KOut, VOut> buildMapper() {
      return new ScriptMapper<KIn, VIn, KOut, VOut>(engine, fileName, script.toString(), variables, functionName);
   }

   public <KOut, VOut> ScriptReducer<KOut, VOut> buildReducer() {
      return new ScriptReducer<KOut, VOut>(engine, fileName, script.toString(), variables, functionName);
   }
   
   public <KOut, VOut, R> ScriptCollator<KOut, VOut, R> buildCollator() {
      return new ScriptCollator<KOut, VOut, R>(engine, fileName, script.toString(), variables, functionName);
   }

   protected void checkAndSetFileName(String fileName) {
      if (this.fileName != null)
         return;

      if (fileName != null)
         this.fileName = fileName;
   }

   private String getExtension(String filename) {
      if (filename == null) {
         return null;
      }
      int index = filename.lastIndexOf(EXTENSION_SEPARATOR);
      if (index == -1) {
         return "";
      } else {
         return filename.substring(index + 1);
      }
   }
}
