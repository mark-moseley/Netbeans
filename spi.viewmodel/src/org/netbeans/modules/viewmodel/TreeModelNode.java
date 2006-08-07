/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.viewmodel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyEditor;
import java.lang.IllegalAccessException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.ErrorManager;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author   Jan Jancura
 */
public class TreeModelNode extends AbstractNode {

    /**
     * The maximum length of text that is interpreted as HTML.
     * This is documented at openide/explorer/src/org/openide/explorer/doc-files/propertyViewCustomization.html
     */
    private static final int MAX_HTML_LENGTH = 511;
    
    // variables ...............................................................

    private Models.CompoundModel model;
    private TreeModelRoot       treeModelRoot;
    private Object              object;
    
    private String              htmlDisplayName;
    private Map                 properties = new HashMap ();

    
    // init ....................................................................

    /**
    * Creates root of call stack for given producer.
    */
    public TreeModelNode ( 
        final Models.CompoundModel model, 
        final TreeModelRoot treeModelRoot,
        final Object object
    ) {
        super (
            createChildren (model, treeModelRoot, object),
            Lookups.singleton (object)
        );
        this.model = model;
        this.treeModelRoot = treeModelRoot;
        this.object = object;
        
        // <RAVE>
        // Use the modified CompoundModel class's field to set the 
        // propertiesHelpID for properties sheets if the model's helpID
        // has been set
        if (model.getHelpId() != null) {
            this.setValue("propertiesHelpID", model.getHelpId()); // NOI18N
        }
        // </RAVE>
        
        treeModelRoot.registerNode (object, this); 
        refreshNode ();
        initProperties ();
    }

    
    // Node implementation .....................................................
    
    private void initProperties () {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet ();
        ColumnModel[] columns = model.getColumns ();
        int i, k = columns.length;
        for (i = 0; i < k; i++)
            ps.put (new MyProperty (columns [i], treeModelRoot));
        sheet.put (ps);
        setSheet (sheet);
    }
    
    private static Children createChildren (
        Models.CompoundModel model, 
        TreeModelRoot treeModelRoot,
        Object object
    ) {
        if (object == null) 
            throw new NullPointerException ();
        try {
            return model.isLeaf (object) ? 
                Children.LEAF : 
                new TreeModelChildren (model, treeModelRoot, object);
        } catch (UnknownTypeException e) {
            if (!(object instanceof String)) {
                Throwable t = ErrorManager.getDefault().annotate(e, "Model: "+model);
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
            }
            return Children.LEAF;
        }
    }
    
    public String getShortDescription () {
        try {
            String shortDescription = model.getShortDescription (object);
            return shortDescription;
        } catch (UnknownTypeException e) {
            if (!(object instanceof String)) {
                Throwable t = ErrorManager.getDefault().annotate(e, "Model: "+model);
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
            }
            return null;
        }
    }
    
    public String getHtmlDisplayName () {
        return htmlDisplayName;
    }
    
    public Action[] getActions (boolean context) {
        if (context) 
            return treeModelRoot.getRootNode ().getActions (false);
        try {
            return model.getActions (object);
        } catch (UnknownTypeException e) {
            // NodeActionsProvider is voluntary
            return new Action [0];
        }
    }
    
    public Action getPreferredAction () {
        return new AbstractAction () {
            public void actionPerformed (ActionEvent e) {
                try {
                    model.performDefaultAction (object);
                } catch (UnknownTypeException ex) {
                    // NodeActionsProvider is voluntary
                }
            }
        };
    }
    
    public boolean canDestroy () {
        try {
            Action[] as = model.getActions (object);
            int i, k = as.length;
            for (i = 0; i < k; i++) {
                if (as [i] == null) continue;
                Object key = as [i].getValue (Action.ACCELERATOR_KEY);
                if ( (key != null) &&
                     (key.equals (KeyStroke.getKeyStroke ("DELETE")))
                ) return as [i].isEnabled ();
            }
            return false;
        } catch (UnknownTypeException e) {
            // NodeActionsProvider is voluntary
            return false;
        }
    }
    
    public boolean canCopy () {
        return false;
    }
    
    public boolean canCut () {
        return false;
    }
    
    public void destroy () {
        try {
            Action[] as = model.getActions (object);
            int i, k = as.length;
            for (i = 0; i < k; i++) {
                if (as [i] == null) continue;
                Object key = as [i].getValue (Action.ACCELERATOR_KEY);
                if ( (key != null) &&
                     (key.equals (KeyStroke.getKeyStroke ("DELETE")))
                ) {
                    as [i].actionPerformed (null);
                    return;
                }
            }
        } catch (UnknownTypeException e) {
            Throwable t = ErrorManager.getDefault().annotate(e, "Model: "+model);
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
        }
    }

    
    // other methods ...........................................................
    
    void setObject (Object o) {
        setObjectNoRefresh (o);
        refresh ();
    }
    
    private void setObjectNoRefresh (Object o) {
        object = o;
        Children ch = getChildren ();
        if (ch instanceof TreeModelChildren)
            ((TreeModelChildren) ch).object = o;
    }
    
    public Object getObject () {
        return object;
    }

    private Task task;
    
    void refresh () {
        // 1) empty cache
        htmlDisplayName = null;
        synchronized (properties) {
            properties.clear();
        }
        
        
        // 2) refresh name, displayName and iconBase
        if (task == null) {
            task = getRequestProcessor ().create (new Runnable () {
                public void run () {
                    refreshNode ();
                    fireShortDescriptionChange(null, null);
                    
                    // 3) refresh children
                    refreshTheChildren(true);
                }
            });
        }
        task.schedule(0);
    }
    
    void refresh (int changeMask) {
        if (changeMask == 0xFFFFFFFF) {
            refresh();
            return ;
        }
        if ((ModelEvent.NodeChanged.DISPLAY_NAME_MASK & changeMask) != 0) {
            htmlDisplayName = null;
            try {
                String name = model.getDisplayName (object);
                if (name == null) {
                    Throwable t = 
                        new NullPointerException (
                            "Model: " + model + ".getDisplayName (" + object + 
                            ") = null!"
                        );
                    ErrorManager.getDefault().notify(t);
                }
                setName (name, false);
            } catch (UnknownTypeException e) {
                Throwable t = ErrorManager.getDefault().annotate(e, "Model: "+model);
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
            }
        } else if ((ModelEvent.NodeChanged.ICON_MASK & changeMask) != 0) {
            try {
                String iconBase = model.getIconBase (object);
                if (iconBase != null)
                    setIconBase (iconBase);
                else
                    setIconBaseWithExtension ("org/openide/resources/actions/empty.gif");
            } catch (UnknownTypeException e) {
                Throwable t = ErrorManager.getDefault().annotate(e, "Model: "+model);
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
            }
        } else if ((ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK & changeMask) != 0) {
            fireShortDescriptionChange(null, null);
        } else if ((ModelEvent.NodeChanged.CHILDREN_MASK & changeMask) != 0) {
            getRequestProcessor ().post (new Runnable () {
                public void run () {
                    refreshTheChildren(false);
                }
            });
        } else {
            refresh();
        }
    }
    
    private static RequestProcessor requestProcessor;
    public static RequestProcessor getRequestProcessor () {
        if (requestProcessor == null)
            requestProcessor = new RequestProcessor ("TreeModel");
        return requestProcessor;
    }

    private void setName (String name, boolean italics) {
        // XXX HACK: HTMLDisplayName is missing in the models!
        if (name.startsWith ("<html>")) {
            htmlDisplayName = name;
            setDisplayName (removeHTML(name));
        } else {
            htmlDisplayName = null;
            setDisplayName (name);
        }
    }
    
    private void refreshNode () {
        try {
            String name = model.getDisplayName (object);
            if (name == null) {
                Throwable t = 
                    new NullPointerException (
                        "Model: " + model + ".getDisplayName (" + object + 
                        ") = null!"
                    );
                ErrorManager.getDefault().notify(t);
            }
            setName (name, false);
            String iconBase = model.getIconBase (object);
            if (iconBase != null)
                setIconBase (iconBase);
            else
                setIconBaseWithExtension ("org/openide/resources/actions/empty.gif");
            firePropertyChange(null, null, null);
        } catch (UnknownTypeException e) {
            Throwable t = ErrorManager.getDefault().annotate(e, "Model: "+model);
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
        }
    }
    
    void refreshColumn(String column) {
        synchronized (properties) {
            properties.remove(column);
            properties.remove(column + "#html");
        }
        firePropertyChange(column, null, null);
    }
    
    private void refreshTheChildren(boolean refreshSubNodes) {
        Children ch = getChildren();
        try {
            if (ch instanceof TreeModelChildren) {
                ((TreeModelChildren) ch).refreshChildren(refreshSubNodes);
            } else if (!model.isLeaf (object)) {
                setChildren(new TreeModelChildren (model, treeModelRoot, object));
            }
        } catch (UnknownTypeException utex) {
            // not known - do not change children
            if (!(object instanceof String)) {
                Throwable t = ErrorManager.getDefault().annotate(utex, "Model: "+model);
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
            }
            setChildren(Children.LEAF);
        }
    }
    
    private static String i (String text) {
        if (text.startsWith ("<html>")) {
            if (text.indexOf ("<i>") > 0) return text;
            text = text.substring (6, text.length () - 7);
        }
        return "<html><font color=666666>" + text + "</font></html>";
    }
    
    private static String htmlValue (String name) {
        if (!name.startsWith ("<html>")) return null;
        if (name.length() > MAX_HTML_LENGTH) {
            int endTagsPos = findEndTagsPos(name);
            String ending = name.substring(endTagsPos + 1);
            name = name.substring(0, MAX_HTML_LENGTH - 3 - ending.length());
            // Check whether we haven't cut "&...;" in between:
            int n = name.length();
            for (int i = n - 1; i > n - 6; i--) {
                if (name.charAt(i) == ';') {
                    break; // We have an end of the group
                }
                if (name.charAt(i) == '&') {
                    name = name.substring(0, i);
                    break;
                }
            }
            name += "..." + ending;
        }
        return name;
    }
    
    private static int findEndTagsPos(String s) {
        int openings = 0;
        int i;
        for (i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == '>') openings++;
            else if (s.charAt(i) == '<') openings--;
            else if (openings == 0) break;
        }
        return i;
    }
    
    private static String removeHTML (String text) {
        text = text.replaceAll ("<i>", "");
        text = text.replaceAll ("</i>", "");
        text = text.replaceAll ("<b>", "");
        text = text.replaceAll ("</b>", "");
        text = text.replaceAll ("<html>", "");
        text = text.replaceAll ("</html>", "");
        text = text.replaceAll ("</font>", "");
        int i = text.indexOf ("<font");
        while (i >= 0) {
            int j = text.indexOf (">", i);
            text = text.substring (0, i) + text.substring (j + 1);
            i = text.indexOf ("<font");
        }
        text = text.replaceAll ("&lt;", "<");
        text = text.replaceAll ("&gt;", ">");
        text = text.replaceAll ("&amp;", "&");
        return text;
    }
    
    
    // innerclasses ............................................................
    
    /** Special locals subnodes (children) */
    private static final class TreeModelChildren extends Children.Keys
                                                 implements LazyEvaluator.Evaluable {
            
        private boolean             initialezed = false;
        private Models.CompoundModel model;
        private TreeModelRoot       treeModelRoot;
        private Object              object;
        private WeakHashMap         objectToNode = new WeakHashMap ();
        private int[]               evaluated = { 0 }; // 0 - not yet, 1 - evaluated, -1 - timeouted
        private Object[]            children_evaluated;
        private boolean refreshingSubNodes = true;
        private boolean refreshingStarted = true;
        
        private static final Object WAIT_KEY = new Object();
        
        
        TreeModelChildren (
            Models.CompoundModel model,
            TreeModelRoot   treeModelRoot,
            Object          object
        ) {
            this.model = model;
            this.treeModelRoot = treeModelRoot;
            this.object = object;
        }
        
        protected void addNotify () {
            initialezed = true;
            refreshChildren (true);
        }
        
        protected void removeNotify () {
            initialezed = false;
            setKeys (Collections.EMPTY_SET);
        }
        
        void refreshChildren (boolean refreshSubNodes) {
            if (!initialezed) return;

            refreshLazyChildren(refreshSubNodes);
        }
        
        public void evaluateLazily(Runnable evaluatedNotify) {
            synchronized (evaluated) {
                refreshingStarted = false;
            }
            Object[] ch;
            try {
                int count = model.getChildrenCount (object);
                ch = model.getChildren (
                    object, 
                    0, 
                    count
                );
            } catch (UnknownTypeException e) {
                ch = new Object [0];
                if (!(object instanceof String)) {
                    Throwable t = ErrorManager.getDefault().annotate(e, "Model: "+model);
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
                }
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                // recover from defect in getChildren()
                // Otherwise there would remain "Please wait..." node.
                ErrorManager.getDefault().notify(t);
                ch = new Object[0];
            }
            evaluatedNotify.run();
            boolean fire;
            synchronized (evaluated) {
                int eval = evaluated[0];
                if (refreshingStarted) {
                    fire = false;
                } else {
                    fire = evaluated[0] == -1;
                    if (!fire) {
                        children_evaluated = ch;
                    }
                    evaluated[0] = 1;
                    evaluated.notifyAll();
                }
                //System.err.println(this.hashCode()+" evaluateLazily() ready, evaluated[0] = "+eval+" => fire = "+fire+", refreshingStarted = "+refreshingStarted+", children_evaluated = "+(children_evaluated != null));
            }
            if (fire) {
                applyChildren(ch, refreshingSubNodes);
            }
        }
        
        private void refreshLazyChildren (boolean refreshSubNodes) {
            synchronized (evaluated) {
                evaluated[0] = 0;
                refreshingStarted = true;
                this.refreshingSubNodes = refreshSubNodes;
                //System.err.println(this.hashCode()+" refreshLazyChildren() started = true, evaluated = 0");
            }
            // It's refresh => do not check for this children already being evaluated
            treeModelRoot.getChildrenEvaluator().evaluate(this, false);
            Object[] ch;
            synchronized (evaluated) {
                if (evaluated[0] != 1) {
                    try {
                        evaluated.wait(200);
                    } catch (InterruptedException iex) {}
                    if (evaluated[0] != 1) {
                        evaluated[0] = -1; // timeout
                        ch = null;
                    } else {
                        ch = children_evaluated;
                    }
                } else {
                    ch = children_evaluated;
                }
                //System.err.println(this.hashCode()+" refreshLazyChildren() ending, evaluated[0] = "+evaluated[0]+", refreshingStarted = "+refreshingStarted+", children_evaluated = "+(children_evaluated != null)+", ch = "+(ch != null));
                // Do nothing when it's evaluated, but already unset.
                if (children_evaluated == null && evaluated[0] == 1) return;
                children_evaluated = null;
            }
            if (ch == null) {
                applyWaitChildren();
            } else {
                applyChildren(ch, refreshSubNodes);
            }
        }
        
        private void applyChildren(final Object[] ch, boolean refreshSubNodes) {
            //System.err.println(this.hashCode()+" applyChildren("+refreshSubNodes+")");
            int i, k = ch.length; 
            WeakHashMap newObjectToNode = new WeakHashMap ();
            for (i = 0; i < k; i++) {
                if (ch [i] == null) {
                    throw (NullPointerException) ErrorManager.getDefault().annotate(
                            new NullPointerException(),
                            "model: " + model + "\nparent: " + object);
                }
                WeakReference wr = (WeakReference) objectToNode.get 
                    (ch [i]);
                if (wr == null) continue;
                TreeModelNode tmn = (TreeModelNode) wr.get ();
                if (tmn == null) continue;
                if (refreshSubNodes) {
                    tmn.setObject (ch [i]);
                } else {
                    tmn.setObjectNoRefresh(ch[i]);
                }
                newObjectToNode.put (ch [i], wr);
            }
            objectToNode = newObjectToNode;
            setKeys (ch);

            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    int i, k = ch.length;
                    for (i = 0; i < k; i++)
                        try {
                            if (model.isExpanded (ch [i])) {
                                TreeTable treeTable = treeModelRoot.getTreeTable ();
                                if (treeTable.isExpanded(object)) {
                                    // Expand the child only if the parent is expanded
                                    treeTable.expandNode (ch [i]);
                                }
                            }
                        } catch (UnknownTypeException ex) {
                        }
                }
            });
        }
        
        private void applyWaitChildren() {
            //System.err.println(this.hashCode()+" applyWaitChildren()");
            setKeys(new Object[] { WAIT_KEY });
        }
        
//        protected void destroyNodes (Node[] nodes) {
//            int i, k = nodes.length;
//            for (i = 0; i < k; i++) {
//                TreeModelNode tmn = (TreeModelNode) nodes [i];
//                String name = null;
//                try {
//                    name = model.getDisplayName (tmn.object);
//                } catch (UnknownTypeException e) {
//                }
//                if (name != null)
//                    nameToChild.remove (name);
//            }
//        }
        
        protected Node[] createNodes (Object object) {
            if (object == WAIT_KEY) {
                AbstractNode n = new AbstractNode(Children.LEAF);
                n.setName(NbBundle.getMessage(TreeModelNode.class, "WaitNode"));
                n.setIconBaseWithExtension("org/netbeans/modules/viewmodel/wait.gif");
                return new Node[] { n };
            }
            if (object instanceof Exception)
                return new Node[] {
                    new ExceptionNode ((Exception) object)
                };
            TreeModelNode tmn = new TreeModelNode (
                model, 
                treeModelRoot, 
                object
            );
            objectToNode.put (object, new WeakReference (tmn));
            return new Node[] {tmn};
        }
    } // ItemChildren
    
    private class MyProperty extends PropertySupport implements LazyEvaluator.Evaluable {
        
        private final String EVALUATING_STR = NbBundle.getMessage(TreeModelNode.class, "EvaluatingProp");
        private String      id;
        private ColumnModel columnModel;
        private TreeModelRoot treeModelRoot;
        private int[]       evaluated = { 0 }; // 0 - not yet, 1 - evaluated, -1 - timeouted
        
        
        MyProperty (
            ColumnModel columnModel, TreeModelRoot treeModelRoot
        ) {
            super (
                columnModel.getID (),
                columnModel.getType (),
                columnModel.getDisplayName (),
                columnModel.getShortDescription (), 
                true,
                true
            );
            this.columnModel = columnModel;
            this.treeModelRoot = treeModelRoot;
            id = columnModel.getID ();
        }
        

        /* Can write the value of the property.
        * Returns the value passed into constructor.
        * @return <CODE>true</CODE> if the read of the value is supported
        */
        public boolean canWrite () {
            try {
                return !model.isReadOnly (object, columnModel.getID ());
            } catch (UnknownTypeException e) {
                if (!(object instanceof String)) {
                    Throwable t = ErrorManager.getDefault().annotate(e, "Column id:" + columnModel.getID ()+"\nModel: "+model);
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
                }
                return false;
            }
        }
        
        public void evaluateLazily(Runnable evaluatedNotify) {
            Object value = "";
            String htmlValue = null;
            String nonHtmlValue = null;
            try {
                value = model.getValueAt (object, id);
                //System.out.println("  evaluateLazily("+TreeModelNode.this.getDisplayName()+", "+id+"): have value = "+value);
                if (value instanceof String) {
                    htmlValue = htmlValue ((String) value);
                    nonHtmlValue = removeHTML ((String) value);
                }
            } catch (UnknownTypeException e) {
                if (!(object instanceof String)) {
                    e.printStackTrace ();
                    System.out.println("  Column id:" + columnModel.getID ());
                    System.out.println (model);
                    System.out.println ();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                evaluatedNotify.run();
                boolean fire;
                synchronized (properties) {
                    if (value instanceof String) {
                        properties.put (id, nonHtmlValue);
                        properties.put (id + "#html", htmlValue);
                    } else {
                        properties.put (id, value);
                    }
                    synchronized (evaluated) {
                        fire = evaluated[0] == -1;
                        evaluated[0] = 1;
                        evaluated.notifyAll();
                    }
                }
                //System.out.println("\nTreeModelNode.evaluateLazily("+TreeModelNode.this.getDisplayName()+", "+id+"): value = "+value+", fire = "+fire);
                if (fire) {
                    firePropertyChange (id, null, value);
                    refreshTheChildren(true);
                }
                
            }
        }
        
        public synchronized Object getValue () { // Sync the calls
            // 1) return value from cache
            synchronized (properties) {
                //System.out.println("getValue("+TreeModelNode.this.getDisplayName()+", "+id+"): contains = "+properties.containsKey (id)+", value = "+properties.get (id));
                if (properties.containsKey (id))
                    return properties.get (id);
            }
            
            synchronized (evaluated) {
                evaluated[0] = 0;
            }
            treeModelRoot.getValuesEvaluator().evaluate(this);
            
            Object ret = null;
            boolean refreshChildren = false;
            
            synchronized (evaluated) {
                if (evaluated[0] != 1) {
                    try {
                        evaluated.wait(25);
                    } catch (InterruptedException iex) {}
                    if (evaluated[0] != 1) {
                        evaluated[0] = -1; // timeout
                        ret = EVALUATING_STR;
                    } else {
                        refreshChildren = true;
                    }
                }
            }
            if (ret == null) {
                synchronized (properties) {
                    ret = properties.get(id);
                }
            }
            
            if (refreshChildren) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        refreshTheChildren(true);
                    }
                });
            }
            if (ret == EVALUATING_STR &&
                    getValueType() != null && getValueType() != String.class) {
                ret = null; // Must not provide String when the property type is different.
                            // htmlDisplayValue attr will assure that the Evaluating str is there.
            }
            return ret;
        }
        
        public Object getValue (String attributeName) {
            if (attributeName.equals ("htmlDisplayValue")) {
                synchronized (evaluated) {
                    if (evaluated[0] != 1) {
                        return "<html><font color=\"0000CC\">"+EVALUATING_STR+"</font></html>";
                    }
                }
                synchronized (properties) {
                    return properties.get (id + "#html");
                }
            }
            return super.getValue (attributeName);
        }

        public String getShortDescription() {
            synchronized (properties) {
                if (!properties.containsKey(id)) {
                    return null; // The same as value => EVALUATING_STR
                }
            }
            try {
                javax.swing.JToolTip tooltip = new javax.swing.JToolTip();
                tooltip.putClientProperty("getShortDescription", object); // NOI18N
                Object tooltipObj = model.getValueAt(tooltip, id);
                if (tooltipObj == null) {
                    return null;
                } else {
                    return tooltipObj.toString();
                }
            } catch (UnknownTypeException e) {
                // Ignore models that do not define tooltips for values.
                return null;
            }
        }
        
        public void setValue (Object v) throws IllegalAccessException, 
        IllegalArgumentException, java.lang.reflect.InvocationTargetException {
            try {
                model.setValueAt (object, id, v);
                synchronized (properties) {
                    properties.put (id, v);
                }
                firePropertyChange (id, null, null);
            } catch (UnknownTypeException e) {
                Throwable t = ErrorManager.getDefault().annotate(e, "Column id:" + columnModel.getID ()+"\nModel: "+model);
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
            }
        }
        
        public PropertyEditor getPropertyEditor () {
            return columnModel.getPropertyEditor ();
        }
    }
    
    /** The single-threaded evaluator of lazy models. */
    static class LazyEvaluator implements Runnable {
        
        /** Release the evaluator task after this time. */
        private static final long EXPIRE_TIME = 60000L;

        private List objectsToEvaluate = new LinkedList();
        private Evaluable currentlyEvaluating;
        private Task evalTask;
        
        public LazyEvaluator() {
            evalTask = new RequestProcessor("Debugger Values Evaluator", 1).post(this);
        }
        
        public void evaluate(Evaluable eval) {
            evaluate(eval, true);
        }
        
        public void evaluate(Evaluable eval, boolean checkForEvaluating) {
            synchronized (objectsToEvaluate) {
                for (Iterator it = objectsToEvaluate.iterator(); it.hasNext(); ) {
                    if (eval == it.next()) return ; // Already scheduled
                }
                if (checkForEvaluating && currentlyEvaluating == eval) return ; // Is being evaluated
                objectsToEvaluate.add(eval);
                objectsToEvaluate.notify();
                if (evalTask.isFinished()) {
                    evalTask.schedule(0);
                }
            }
        }

        public void run() {
            while(true) {
                Evaluable eval;
                synchronized (objectsToEvaluate) {
                    if (objectsToEvaluate.size() == 0) {
                        try {
                            objectsToEvaluate.wait(EXPIRE_TIME);
                        } catch (InterruptedException iex) {
                            return ;
                        }
                        if (objectsToEvaluate.size() == 0) { // Expired
                            return ;
                        }
                    }
                    eval = (Evaluable) objectsToEvaluate.remove(0);
                    currentlyEvaluating = eval;
                }
                Runnable evaluatedNotify = new Runnable() {
                    public void run() {
                        synchronized (objectsToEvaluate) {
                            currentlyEvaluating = null;
                        }
                    }
                };
                eval.evaluateLazily(evaluatedNotify);
            }
        }

        public interface Evaluable {

            public void evaluateLazily(Runnable evaluatedNotify);

        }

    }

}

