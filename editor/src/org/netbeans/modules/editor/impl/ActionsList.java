/*
 * ActionsList.java
 *
 * Created on February 16, 2007, 2:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.editor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JSeparator;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Utilities;

/**
 *
 * @author Vita Stejskal
 */
public class ActionsList {

    // -J-Dorg.netbeans.modules.editor.impl.ActionsList.level=FINE
    private static final Logger LOG = Logger.getLogger(ActionsList.class.getName());
    
    private final List<Object> all;
    private final List<Action> actions;

    /**
     * Create a new <code>ActionList</code> instance. The <code>ActionList</code>
     * converts a list of objects (keys) to the list of <code>Action</code>s
     * or other instances that can potentially be used in actions based UI such
     * as popup menus, toolbars, etc. The other instances can be anything, but
     * usually they are things like <code>JSeparator</code>, <code>DataFolder</code>
     * or plain <code>String</code> with the name of an editor action.
     * 
     * @param keys The list of objects to convert to <code>Action</code>s
     * @param ignoreFolders <code>true</code> if the conversion should skipp folders
     * @param prohibitSeparatorsAndActionNames Treat separators and references to actions
     *  by using their Action.NAME as errors. This is useful for
     */
    protected ActionsList(List<FileObject> keys, boolean ignoreFolders,
            boolean prohibitSeparatorsAndActionNames)
    {
        Pair p = convertImpl(keys == null ? Collections.<FileObject>emptyList() : keys, ignoreFolders,
                prohibitSeparatorsAndActionNames);
        this.all = p.all;
        this.actions = p.actions;
    }

    public List<Object> getAllInstances() {
        return all;
    }

    public List<Action> getActionsOnly() {
        return actions;
    }

    public static List<Object> convert(List<FileObject> keys, boolean prohibitSeparatorsAndActionNames) {
        return convertImpl(keys, false, prohibitSeparatorsAndActionNames).all;
    }
    
    private static class Pair {
        List<Object> all;
        List<Action> actions;
    }
    
    private static Pair convertImpl(List<FileObject> keys, boolean ignoreFolders,
            boolean prohibitSeparatorsAndActionNames)
    {
        List<Object> all = new ArrayList<Object>();
        List<Action> actions = new ArrayList<Action>();

        for (FileObject item : keys) {
            DataObject dob;
            try {
                dob = DataObject.find(item);
                if (dob == null && prohibitSeparatorsAndActionNames) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.warning("ActionsList: DataObject is null for item=" + item + "\n");
                    }
                }
            } catch (DataObjectNotFoundException dnfe) {
                if (prohibitSeparatorsAndActionNames) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.warning("ActionsList: DataObject not found for item=" + item + "\n");
                    }
                } else {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "DataObject not found for action fileObject=" + item);
                    }
                }
                continue; // ignore
            }

            Object toAdd = null;
            InstanceCookie ic = dob.getLookup().lookup(InstanceCookie.class);
            if (prohibitSeparatorsAndActionNames && ic == null) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.warning("ActionsList: InstanceCookie not found for item=" + item + "\n");
                }
                continue;
            }
            if (ic != null) {
                try {
                    if (!isSeparator(ic)) {
                        toAdd = ic.instanceCreate();
                        if (toAdd == null && prohibitSeparatorsAndActionNames) {
                            if (LOG.isLoggable(Level.WARNING)) {
                                LOG.warning("ActionsList: InstanceCookie.instanceCreate() null for item=" +
                                        item + "\n");
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Can't instantiate object", e); //NOI18N
                    continue;
                }
            } else if (dob instanceof DataFolder) {
                toAdd = dob;
            } else {
                toAdd = dob.getName();
            }

            // Filter out the same succeding items
            if (all.size() > 0) {
                Object lastOne = all.get(all.size() - 1);
                if (Utilities.compareObjects(lastOne, toAdd)) {
                    continue;
                }
                if (isSeparator(lastOne) && isSeparator(toAdd)) {
                    continue;
                }
            }
            
            if (toAdd instanceof Action) {
                actions.add((Action) toAdd);
            } else if (isSeparator(toAdd)) {
                if (prohibitSeparatorsAndActionNames) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.warning("ActionsList: Separator for item=" + item + "\n");
                    }
                }
                actions.add(null);
            }
            all.add(toAdd);
        }

        Pair p = new Pair();
        p.all = all.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(all);
        p.actions = actions.isEmpty() ? Collections.<Action>emptyList() : Collections.unmodifiableList(actions);
        return p;
    }
    
    private static boolean isSeparator(Object o) {
        return o == null || o instanceof JSeparator;
    }
    
    private static boolean isSeparator(InstanceCookie o) throws Exception {
        return (o instanceof InstanceCookie.Of && ((InstanceCookie.Of) o).instanceOf(JSeparator.class))
            || JSeparator.class.isAssignableFrom(o.instanceClass());
    }
}
