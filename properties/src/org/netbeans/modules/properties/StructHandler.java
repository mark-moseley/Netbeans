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


package org.netbeans.modules.properties;


import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;


/** Class for handling properties structure of one .properties file.
 *
 * @author Petr Hamernik, Petr Jiricka
 * @see PropertiesStructure
 */
public class StructHandler extends Object {

    /** Appropriate properties file entry. */
    private PropertiesFileEntry propFileEntry;

    /** Weak reference to parsing task. */
    private WeakReference parsingTaskWRef = new WeakReference(null);

    /** Soft reference to the underlying properties structure. */
    private SoftReference propStructureSRef = new SoftReference(null);

    /** Parser performing actual parsing task. */
    private WeakReference parserWRef = new WeakReference(null);
    
    /** Flag indicating if parsing isAllowed. */
    private boolean parsingAllowed = true;

    /** Generated serialized version UID. */
    static final long serialVersionUID =-3367087822606643886L;

    
    /** Constructor. */
    public StructHandler(PropertiesFileEntry propFileEntry) {
        this.propFileEntry = propFileEntry;
    }

    
    /** Reparses file. Fires changes. */
    PropertiesStructure reparseNowBlocking() {
        return reparseNowBlocking(true);
    }
    
    /** Reparses file. 
     * @param fire true if should fire changes */
    private synchronized PropertiesStructure reparseNowBlocking(boolean fire) {
        if(!parsingAllowed) {
            return null;
        }
        
        PropertiesParser parser = new PropertiesParser(propFileEntry);

        try {
            parserWRef = new WeakReference(parser);

            parser.initParser();
            PropertiesStructure propStructure = parser.parseFile();
            
            updatePropertiesStructure(propStructure, fire);
            
            return propStructure;
        } catch (IOException ioe) {
            updatePropertiesStructure(null, fire);
            
            return null;
        } finally {
            parser.clean();
        }
    }
    
    /** Stops parsing and prevent any other sheduled ones. File object is going to be
     * deleted. Due actual delete or move operation. */
    synchronized void stopParsing() {
        parsingAllowed = false;
        
        PropertiesParser parser = (PropertiesParser)parserWRef.get();
        
        if(parser != null)
            parser.stop();
    }
    
    /** Allows parsing when error during deleting or moving occured and the operation didn't succed. */
    synchronized void allowParsing() {
        parsingAllowed = true;
    }

    /** Getter for <code>propFileEntry</code> property. */
    public PropertiesFileEntry getEntry() {
        return propFileEntry;
    }

    /** Starts parsing task. Tries to cancel previous parsing task if it is not running yet. */
    void autoParse() {
        Task previousTask = (Task)parsingTaskWRef.get();
        if(previousTask != null) {
            // There was previous task already, reschedule it 500 ms later.
            previousTask.schedule(500);
        } else {
            // Create a new task, and schedule it immediatelly.
            parsingTaskWRef = new WeakReference(
                RequestProcessor.postRequest(
                    new Runnable() {
                        public void run() {
                            reparseNowBlocking();
                        }
                    }
                )
            );
        }
    }

    /** When parser finishes its job, it's called this method to set new values.
     *
     * @param newPropStructure new properties structure
     * @param fire if should fire change when structure created anew
     */
    private void updatePropertiesStructure(PropertiesStructure newPropStructure, boolean fire) {
        if(newPropStructure == null) {
            propStructureSRef = new SoftReference(null);
            return;
        }
        
        PropertiesStructure propStructure = (PropertiesStructure)propStructureSRef.get();

        if(propStructure == null) {
            // Set the parent.
            newPropStructure.setParent(this);
            propStructure = newPropStructure;
            propStructureSRef = new SoftReference(propStructure);
            
            if(fire)
                propStructure.structureChanged();
        } else {
            // Update calls notification methods according to changes.
            propStructure.update(newPropStructure);
        }
    }

    /** Gets properties structure handled by this handler. */
    public PropertiesStructure getStructure() {
        PropertiesStructure propStructure = (PropertiesStructure)propStructureSRef.get();
        
        if(propStructure != null)
            return propStructure;

        // No data available -> reparse file.
        // PENDING don;t send change event when requesting data only.
        // They could be garbaged before so no fire changes.
        return reparseNowBlocking(false); 
    }

}