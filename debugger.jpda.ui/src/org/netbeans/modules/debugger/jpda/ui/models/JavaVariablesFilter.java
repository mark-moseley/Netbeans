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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.util.HashSet;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.jpda.VariablesFilterAdapter;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ComputingException;
import org.netbeans.spi.viewmodel.NoInformationException;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 *
 * @author   Jan Jancura
 */
public class JavaVariablesFilter extends VariablesFilterAdapter {
    
    public String[] getSupportedTypes () {
        return new String[] {
            "java.lang.String",
            "java.lang.StringBuffer",
            "java.lang.Character",
            "java.lang.Integer",
            "java.lang.Float",
            "java.lang.Byte",
            "java.lang.Boolean",
            "java.lang.Double",
            "java.lang.Long",
            "java.lang.Short",
            
            "java.lang.ref.WeakReference",
            
            "java.util.ArrayList",
            "java.util.HashSet",
            "java.util.LinkedHashSet",
            "java.util.LinkedList",
            "java.util.Stack",
            "java.util.TreeSet",
            "java.util.Vector",
            "java.util.Hashtable",
            "java.util.Hashtable$Entry",
            "java.util.HashMap",
            "java.util.HashMap$Entry",
            "java.util.IdentityHashMap",
            "java.util.AbstractMap$SimpleEntry",
            "java.util.TreeMap",
            "java.util.TreeMap$Entry",
            "java.util.WeakHashMap",
            "java.util.LinkedHashMap",
            "java.util.LinkedHashMap$Entry",
            "java.beans.PropertyChangeSupport"
        };
    }
    
    public String[] getSupportedAncestors () {
        return new String[] {
        };
    }
    
    /** 
     * Returns filtered children for given parent on given indexes.
     *
     * @param   original the original tree model
     * @throws  NoInformationException if the set of children can not be
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModelFilter implementation is not
     *          able to resolve dchildren for given node type
     *
     * @return  children for given parent on given indexes
     */
    public Object[] getChildren (
        TreeModel original, 
        Variable variable, 
        int from, 
        int to
    ) throws NoInformationException, ComputingException, UnknownTypeException {
        
        String type = variable.getType ();
        
        if ( isToArrayType (type)
        ) 
            try {
                ObjectVariable ov = (ObjectVariable) variable;
                return ((ObjectVariable) ov.invokeMethod (
                    "toArray",
                    "()[Ljava/lang/Object;",
                    new Variable [0]
                )).getFields (from, to);
            } catch (NoSuchMethodException e) {
                e.printStackTrace ();
            }
        if ( isMapMapType (type)
        ) 
            try {
                ObjectVariable ov = (ObjectVariable) variable;
                ov = (ObjectVariable) ov.invokeMethod (
                    "entrySet",
                    "()Ljava/util/Set;",
                    new Variable [0]
                );
                return ((ObjectVariable) ov.invokeMethod (
                    "toArray",
                    "()[Ljava/lang/Object;",
                    new Variable [0]
                )).getFields (from, to);
            } catch (NoSuchMethodException e) {
                e.printStackTrace ();
            }
        if ( isMapEntryType (type)
        ) {
            ObjectVariable ov = (ObjectVariable) variable;
            Field[] fs = new Field [2];
            fs [0] = ov.getField ("key");
            fs [1] = ov.getField ("value");
            return fs;
        }
        if ( type.equals ("java.beans.PropertyChangeSupport")
        ) 
            try {
                ObjectVariable ov = (ObjectVariable) variable;
                return ((ObjectVariable) ov.invokeMethod (
                    "getPropertyChangeListeners",
                    "()[Ljava/beans/PropertyChangeListener;",
                    new Variable [0]
                )).getFields (from, to);
            } catch (NoSuchMethodException e) {
                e.printStackTrace ();
            }
//        if ( type.equals ("java.lang.ref.WeakReference")
//        ) 
//            try {
//                ObjectVariable ov = (ObjectVariable) variable;
//                return new Object [] {ov.invokeMethod (
//                    "get",
//                    "()Ljava/lang/Object;",
//                    new Variable [0]
//                )};
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace ();
//            }
        if ( type.equals ("java.lang.ref.WeakReference")
        ) {
            ObjectVariable ov = (ObjectVariable) variable;
            return new Object [] {ov.getField ("referent")};
        }
        return original.getChildren (variable, from, to);
    }
    
    /**
     * Returns true if node is leaf.
     * 
     * @param   original the original tree model
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve dchildren for given node type
     * @return  true if node is leaf
     */
    public boolean isLeaf (TreeModel original, Variable variable) 
    throws UnknownTypeException {
        String type = variable.getType ();
        if ( isPrimitiveLikeType (type)
        ) return true;
        return original.isLeaf (variable);
    }
    
    public Object getValueAt (
        TableModel original, 
        Variable variable, 
        String columnID
    ) throws ComputingException, UnknownTypeException {

        String type = variable.getType ();
        ObjectVariable ov = (ObjectVariable) variable;
        if ( isMapEntryType (type) &&
             ( columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
               columnID == Constants.WATCH_VALUE_COLUMN_ID)
        ) {
            return ov.getField ("key").getValue () + "=>" + 
                   ov.getField ("value").getValue ();
        }
        if ( isPrimitiveLikeType (type) &&
             ( columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
               columnID == Constants.WATCH_VALUE_COLUMN_ID)
        ) {
            try {
                return ov.getToStringValue ();
            } catch (InvalidExpressionException ex) {
                return ex.getLocalizedMessage ();
            }
        }
        if (type.equals("java.lang.StringBuffer") &&
             ( columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
               columnID == Constants.WATCH_VALUE_COLUMN_ID)
        ) {
            try {
                return ov.getToStringValue ();
            } catch (InvalidExpressionException ex) {
                return ex.getLocalizedMessage ();
            }
        }
        return original.getValueAt (variable, columnID);
    }

    
    // other methods ...........................................................
    
    private HashSet primitiveLikeType;
    private boolean isPrimitiveLikeType (String type) {
        if (primitiveLikeType == null) {
            primitiveLikeType = new HashSet ();
            primitiveLikeType.add ("java.lang.String");
            primitiveLikeType.add ("java.lang.Character");
            primitiveLikeType.add ("java.lang.Integer");
            primitiveLikeType.add ("java.lang.Float");
            primitiveLikeType.add ("java.lang.Byte");
            primitiveLikeType.add ("java.lang.Boolean");
            primitiveLikeType.add ("java.lang.Double");
            primitiveLikeType.add ("java.lang.Long");
            primitiveLikeType.add ("java.lang.Short");
        }
        return primitiveLikeType.contains (type);
    }
    
    private HashSet mapEntryType;
    private boolean isMapEntryType (String type) {
        if (mapEntryType == null) {
            mapEntryType = new HashSet ();
            mapEntryType.add ("java.util.HashMap$Entry");
            mapEntryType.add ("java.util.Hashtable$Entry");
            mapEntryType.add ("java.util.AbstractMap$SimpleEntry");
            mapEntryType.add ("java.util.LinkedHashMap$Entry");
            mapEntryType.add ("java.util.TreeMap$Entry");
        }
        return mapEntryType.contains (type);
    }
    
    private HashSet mapMapType;
    private boolean isMapMapType (String type) {
        if (mapMapType == null) {
            mapMapType = new HashSet ();
            mapMapType.add ("java.util.HashMap");
            mapMapType.add ("java.util.IdentityHashMap");
            mapMapType.add ("java.util.Hashtable");
            mapMapType.add ("java.util.TreeMap");
            mapMapType.add ("java.util.WeakHashMap");
            mapMapType.add ("java.util.LinkedHashMap");
        }
        return mapMapType.contains (type);
    }
    
    private HashSet toArrayType;
    private boolean isToArrayType (String type) {
        if (toArrayType == null) {
            toArrayType = new HashSet ();
            toArrayType.add ("java.util.ArrayList");
            toArrayType.add ("java.util.HashSet");
            toArrayType.add ("java.util.LinkedHashSet");
            toArrayType.add ("java.util.LinkedList");
            toArrayType.add ("java.util.Stack");
            toArrayType.add ("java.util.TreeSet");
            toArrayType.add ("java.util.Vector");
        }
        return toArrayType.contains (type);
    }
}
