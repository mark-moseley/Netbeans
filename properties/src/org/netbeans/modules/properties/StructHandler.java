/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.properties;

import java.io.*;
import java.lang.ref.SoftReference;
import java.util.LinkedList;
import java.util.HashMap;

import javax.swing.text.BadLocationException;

import com.netbeans.ide.filesystems.FileObject;
import com.netbeans.ide.loaders.MultiDataObject;
import com.netbeans.ide.util.*;
import com.netbeans.ide.nodes.Children;
import com.netbeans.ide.text.PositionRef;
import com.netbeans.ide.text.PositionBounds;

/* Handling of properties structure files
*
* @author Petr Hamernik, Petr Jiricka
*/
class StructHandler extends Element /*implements TaskListener*/ {

  /** Appropriate properties file entry. */
  private PropertiesFileEntry pfe;

  /** If the parsing is in progress this variable is set
  * to the parsing task.
  */
  Task parsingTask;

  /** Soft reference to the data */
  SoftReference dataRef;

  /** This flag is set when somebody is editing the document and it is
  * cleared after reparsing.
  * It is used by parser to decide if parsing is necessary.
  */
  boolean dirty = false;

  // ======================== Public part ====================================

  /** Constructs the implementation of source element for the given
  * java data object.
  */
  public StructHandler(PropertiesFileEntry pfe) {
    super(null);
    this.pfe = pfe;
  }

  /** Getter for the current status of the SourceElement implementation.
  * @return the status.
  */
  public boolean getStatus() {
    return getReferenceData() != null;
  }
         
  /** If necessary parses the file, blocks until the thing is finished */       
  private synchronized void getParsedDataBlocking() {
    if (isDirty() || (getReferenceData() == null)) {
      try {
        PropertiesParser parser = new PropertiesParser(pfe);
        parser.parseFile();   
      }
      catch (IOException e) {
        setPropertiesStructure(null);
      }
      setDirty(false);
    }
  }
  
  
  /** Method that instructs the implementation of the source element
  * to prepare the element. It is non blocking method that returns
  * task that can be used to control if the operation finished or not.
  *
  * @return task to control the preparation of the elemement
  */
/*  public Task prepare () {
    return (Task) Children.MUTEX.writeAccess(new Mutex.Action() {
      public Object run() {
debug("PREPARING : " + (parsingTask == null ? "NULL":(" NOT NULL, FINISHED:" + parsingTask.isFinished()        )));
        if (parsingTask == null) {
          DataRef d = getReferenceData();
          if (d != null) {
debug("returning datatask");          
            return new DataTask(d);
          }  
          
//debug("CALL PARSING IN THREAD " + Thread.currentThread().toString());
if (parsingTask != null)
  debug("CHYBA JAKO KRAVA TYVOLE DO PRDELE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11"); 
debug("SETTING PARSING TASK = SOMETHING");
          parsingTask = createParsingTask(Thread.MAX_PRIORITY);
debug("...SET (SOMETHING)");
        }
        return parsingTask;
      }
    });
  }*/

  /** Get a string representation of the element for printing.
  * @return the string
  */
  public String printString() {
    try {
      return getData().ps.printString();
    }
    catch (PropertiesException e) {
      // PENDING - handle it
      return "";
    }  
  }

  // ======================== Package private part ================================

  /** Sets the dirty flag - if the document was modified after last parsing. */
  void setDirty(boolean b) {
    dirty = b;
  }

  /** Tests the dirty flag. */
  boolean isDirty() {
    return dirty;
  }

  /** Starts the parsing if the this class is 'dirty' and status is true
  * and parsing is not running yet.
  */
  void autoParse() {   
debug("Starting autoparse");
    if (dirty && getStatus()) 
      getParsedDataBlocking();
/*    Children.MUTEX.writeAccess(new Runnable() {
      public void run() {
        if (dirty && (parsingTask == null) && getStatus()) {
          prepareParsing(Thread.MIN_PRIORITY);
        }
      }
    });*/
  }

  /** This method invokes the parsing only. It could be used by
  * editor during saving.
  */
/*  Task prepareParsing(final int priority) {
    return (Task) Children.MUTEX.writeAccess(new Mutex.Action() {
      public Object run() {
        if (parsingTask == null) {
debug("AUTO parsingTask = someTask");
          parsingTask = createParsingTask(priority);
        }
        return parsingTask;
      }
    });
  }*/

  /** When parser finishes its job, it has to call this method to inform
  * everyone about the result. Must be called under mutex.writeaccess
  *
  * @param res resultant structure
  */
  synchronized void setPropertiesStructure(final PropertiesStructure res) {
    if (res == null)
      return;
    
    PropertiesStructure result = res;                                 
    // effectively getReferenceData, but we're under writeAccess, so no readAccess
    DataRef data = (dataRef != null) ? (DataRef) dataRef.get() : null;
 
    if (data == null) {
      data = new DataRef(pfe, res);
      dataRef = new SoftReference(data);
    }
    else {
      data.ps.update(res);
    }
    
    setDirty(false);

        // PENDING
    
        // fires the change of the status - it is required to fire it everytime
        // because of the icon changes in the delegate node.
        // PENDING
        //firePropertyChange (PROP_STATUS, null, null);
    return;
  }

  
  /** Create parsing task.
  * May be called only under mutex.writeAccess()
  */
/*  private Task createParsingTask(final int priority) {
    Runnable parseRunnable = new Runnable() {
      public void run() {
        try {
//debug("PARSING IN THREAD " + Thread.currentThread().toString());        
          PropertiesParser parser = new PropertiesParser(pfe);
          parser.parseFile();   
//debug("FINISHED PARSING IN THREAD " + Thread.currentThread().toString());        
        }
        catch (IOException e) {
          setPropertiesStructure(null);
        }
      }
    };
    RequestProcessor.Task t = RequestProcessor.postRequest(parseRunnable, 0, priority);
    t.addTaskListener(this);
    return t;
  }                             */
    
  /** Gets the referenced object from the dataRef
  */
  private DataRef getReferenceData() {
    return (DataRef) Children.MUTEX.readAccess(new Mutex.Action() {
      public Object run() {
        return (dataRef != null) ? (DataRef) dataRef.get() : null;
      }
    });
  }
  
  /** Clear the parsing task variable */
/*  public void taskFinished(final Task task) {
debug("TASK FINISHED");  
    Children.MUTEX.writeAccess(new Runnable() {
      public void run() {
debug("SETTING parsingTask = null");  
        parsingTask = null;
debug("...SET (null)");  
      }
    });
  }*/

  /** Returns the structure */
  public PropertiesStructure getStructure() {
    try {
      return getData().ps;
    }
    catch (PropertiesException e) {
      // PENDING
debug("STRUCTURE IS NULL");
      return null;
    }  
  }
  
  /**
  *
  * @return the DataRef object holding the parsing information
  * @exception SourceException if parsing failed.
  */
  private DataRef getData() throws PropertiesException {
    DataRef d = getReferenceData();
    if (d != null)
      return d;

//debug("CALL PREPARE IN THREAD " + Thread.currentThread().toString());
/*    Task t = prepare();
    t.waitFinished();*/
    getParsedDataBlocking();
//debug("PARSING TASK CHECK : " + (parsingTask == null ? "NULL":"NOT NULL"));    
//debug("FINISH CALLING PREPARE IN THREAD " + Thread.currentThread().toString());
    
    d = getReferenceData();
    if (d != null)
      return d;

    throw new PropertiesException("Document cannot be modified. Impossible to parse it.");
  }

  /** Informs the SourceElement about releasing data (classes, imports,...)
  * from the memory. This method gets as the parameter DataRef which will be
  * garbage collected and should swap them to the disk.
  */
  private void dataRefReleased(DataRef data) {
    Object oldValue = Children.MUTEX.writeAccess(new Mutex.Action() {
      public Object run() {
        dataRef = null;
        return new Boolean(true);
      }
    });
    // PENDING
    //firePropertyChange (PROP_STATUS, null, null);
  }

  private void debug(String hlaska) {
    System.out.println(pfe.getFile().getName() + " > " + hlaska + "// " + Thread.currentThread().toString());
  }

  // ======================== The real data holder ==========================

  /** Class which is used for holding the parsed information.
  * It is serializable and could be swapped to the disk.
  * The struct handler holds only soft reference to this object.
  */
  private static class DataRef extends Object {
    /** A serial version UID */
    //static final long serialVersionUID = 697350931687937673L;

    /** Appropriate file entry. */
    PropertiesFileEntry pfe;

    // --------------- Data -------------------
                           
    /** The structure holding the data */                       
    PropertiesStructure ps;

    /** Creates new data holder. */
    DataRef(PropertiesFileEntry pfe, PropertiesStructure ps) {
      this.pfe = pfe;
      this.ps  = ps;
    }

    /** Informs the SourceElementImpl about the releasing
    * of this class from the memory.
    */
    public void finalize() {     
System.out.println("Garbage collecting DataRef");
      if (pfe != null) {
        pfe.getHandler().dataRefReleased(this);
      }
    }
  }

  // ======================== Utility - DataTask ==========================

  /** Task which is used for holding the reference to the given data.
  * It prevents them from being garbage collected.
  */
/*  private static class DataTask extends Task {
    private DataRef data;

    public DataTask(DataRef data) {
      super(null);
      this.data = data;
    }
  }*/
}



