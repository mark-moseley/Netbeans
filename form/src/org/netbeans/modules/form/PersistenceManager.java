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

package org.netbeans.modules.form;

import java.util.*;
import org.openide.*;

/**
 * An abstract class which defines interface for persistence managers (being
 * responsible for loading and saving forms) and provides a basic registration
 * facility.
 * PersistenceManager implementations should be able to deal with multiple
 * forms being saved and loaded by one instance of persistence manager (but
 * not concurrently).
 *
 * @author Ian Formanek, Tomas Pavek
 */

public abstract class PersistenceManager {

    // -------------------
    // abstract interface

    /** This method is used to check if the persistence manager can read the
     * given form (if it understands the form file format).
     * @return true if this persistence manager can load the form
     * @exception PersistenceException if any unexpected problem occurred
     */
    public abstract boolean canLoadForm(FormDataObject formObject)
        throws PersistenceException;

    /** This method loads the form from given data object.
     * @param formObject FormDataObject representing the form files
     * @param formModel FormModel to be filled with loaded data
     * @param nonfatalErrors List to be filled with errors occurred during
     *        loading which are not fatal (but should be reported)
     * @exception PersistenceException if some fatal problem occurred which
     *            prevents loading the form
     */
    public abstract void loadForm(FormDataObject formObject,
                                  FormModel formModel,
                                  List nonfatalErrors)
        throws PersistenceException;

    /** This method saves the form to given data object.
     * @param formObject FormDataObject representing the form files
     * @param formModel FormModel to be saved
     * @param nonfatalErrors List to be filled with errors occurred during
     *        saving which are not fatal (but should be reported)
     * @exception PersistenceException if some fatal problem occurred which
     *            prevents saving the form
     */
    public abstract void saveForm(FormDataObject formObject,
                                  FormModel formModel,
                                  List nonfatalErrors)
        throws PersistenceException;

    // ------------
    // static registry [provisional only]

    private static List managers;
    private static List managersByName;

    public static void registerManager(PersistenceManager manager) {
        getManagersList().add(manager);
    }

    public static void unregisterManager(PersistenceManager manager) {
        getManagersList().remove(manager);
    }

    static void registerManager(String managerClassName) {
        getManagersNamesList().add(managerClassName);
    }

    public static Iterator getManagers() {
        ClassLoader classLoader = null;
        Iterator iter = getManagersNamesList().iterator();
        while (iter.hasNext()) { // create managers registered by name
            if (classLoader == null)
                classLoader = TopManager.getDefault().currentClassLoader();
            String pmClassName = (String) iter.next();
            try {
                PersistenceManager manager = (PersistenceManager)
                    classLoader.loadClass(pmClassName).newInstance();
                getManagersList().add(manager);
            }
            catch (Exception ex1) {
                notifyError(ex1, pmClassName);
            }
            catch (LinkageError ex2) {
                notifyError(ex2, pmClassName);
            }
        }
        getManagersNamesList().clear(); // [is it OK to lose unsuccessful managers?]

        return getManagersList().iterator();
    }

    private static List getManagersList() {
        if (managers == null) {
            managers = new ArrayList();
            managers.add(new GandalfPersistenceManager());
        }
        return managers;
    }

    private static List getManagersNamesList() {
        if (managersByName == null)
            managersByName = new ArrayList();
        return managersByName;
    }

    private static void notifyError(Throwable th, String pmClassName) {
        String msg = FormUtils.getFormattedBundleString(
            "FMT_ERR_PersistenceManagerInstantiation", // NOI18N
            new Object[] { pmClassName });

        ErrorManager errorManager = TopManager.getDefault().getErrorManager();
        errorManager.annotate(th, msg);
        errorManager.notify(ErrorManager.EXCEPTION, th);
    }
}
