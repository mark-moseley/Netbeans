/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/** 
*
* @author Ian Formanek
*/
public abstract class PersistenceManager {
  /** Magic header number for recognition of binary forms */
  public static final long FORM_MAGIC=0x42424242l;

  private static ArrayList managers = new ArrayList (5);

// -----------------------------------------------------------------------------
// Static accessors to registered PersistenceManagers

  public static void registerManager (PersistenceManager manager) {
    managers.add (manager);
  }

  public static Iterator getManagers () {
    return managers.iterator ();
  }
  
// -----------------------------------------------------------------------------
// abstract interface
  
  /** A method which allows the persistence manager to check whether it can read
  * given form format.
  * @return true if this PersistenceManager can load form stored in the specified form, false otherwise
  * @exception IOException if any problem occured when accessing the form
  */
  public abstract boolean canLoadForm (FormDataObject formObject) throws IOException;

  /** Called to actually load the form stored in specified formObject.
  * @param formObject the FormDataObject which represents the form files
  * @return the FormManager representing the loaded form or null if some problem occured
  * @exception IOException if any problem occured when loading the form
  */
  public abstract FormManager loadForm (FormDataObject formObject) throws IOException;

  /** Called to actually save the form represented by specified FormManager into specified formObject.
  * @param formObject the FormDataObject which represents the form files
  * @param manager the FormManager representing the form to be saved
  * @exception IOException if any problem occured when saving the form
  */
  public abstract void saveForm (FormDataObject formObject, FormManager manager) throws IOException;
}

/*
 * Log
 *  2    Gandalf   1.1         5/4/99   Ian Formanek    Package change
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */
