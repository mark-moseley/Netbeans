/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.*;
import org.openide.util.*;

import java.beans.PropertyChangeEvent;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Node.PropertySet;


/**
 * A node used by PropertySheet to display common properties of
 * more nodes.
 * @author David Strupl
 */
final class ProxyNode extends AbstractNode {
    private static final int MAX_NAMES = 2;
    private Node[] original;
    private ArrayList<Node.PropertySet[]> originalPropertySets;
    private NodeListener pcl;
    String displayName = null;
    private String shortDescription = null;

    ProxyNode(Node[] original) {
        super(Children.LEAF);
        this.original = original;
        pcl = new NodeAdapter() {
                    public void propertyChange(PropertyChangeEvent pce) {
                        String nm = pce.getPropertyName();

                        if (PROP_COOKIE.equals(nm)) {
                            fireCookieChange();
                        } else if (PROP_DISPLAY_NAME.equals(nm)) {
                            displayName = null;
                            fireDisplayNameChange((String) pce.getOldValue(), getDisplayName());
                        } else if (PROP_ICON.equals(nm)) {
                            fireIconChange();
                        } else if (PROP_OPENED_ICON.equals(nm)) {
                            fireOpenedIconChange();
                        } else if (PROP_NAME.equals(nm)) {
                            fireNameChange((String) pce.getOldValue(), getName());
                        } else if (PROP_PROPERTY_SETS.equals(nm)) {
                            PropertySet[] old = getPropertySets();
                            setSheet(createSheet());
                            firePropertySetsChange(old, getPropertySets());
                        } else if (PROP_SHORT_DESCRIPTION.equals(nm)) {
                            fireShortDescriptionChange((String) pce.getOldValue(), getShortDescription());
                        } else if (PROP_LEAF.equals(nm)) {
                            //Not interesting to property sheet
                        } else if (PROP_PARENT_NODE.equals(nm)) {
                            //Not interesting to property sheet
                        } else {
                            Node.PropertySet[] pss = getPropertySets();
                            boolean exists = false;

                            for (int i = 0; i < pss.length && !exists; i++) {
                                Node.Property[] ps = pss[i].getProperties();

                                for (int j = 0; j < ps.length && !exists; j++) {
                                    if (ps[j].getName().equals(nm)) {
                                        exists = true;
                                    }
                                }
                            }
                            if( exists ) {
                                firePropertyChange(pce.getPropertyName(), pce.getOldValue(), pce.getNewValue());
                            }
                        }
                    }

                    public void nodeDestroyed(NodeEvent ev) {
                        int idx = Arrays.asList(ProxyNode.this.original).indexOf((Node) ev.getSource());

                        if (idx != -1) {
                            HashSet<Node> set = new HashSet<Node>(Arrays.asList(ProxyNode.this.original));
                            set.remove(ev.getSource());
                            ProxyNode.this.original = set.toArray(new Node[0]);

                            if (set.size() == 0) {
                                ProxyNode.this.fireNodeDestroyed();
                            }
                        }
                    }
                };

        for (int i = 0; i < original.length; i++) {
            original[i].addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(pcl, original[i]));
            original[i].addNodeListener(
                org.openide.util.WeakListeners.create(NodeListener.class, pcl, original[i])
            );
        }
    }

    public HelpCtx getHelpCtx() {
        for (int i = 0; i < original.length; i++) {
            if (original[i].getHelpCtx() != HelpCtx.DEFAULT_HELP) {
                return original[i].getHelpCtx();
            }
        }

        return HelpCtx.DEFAULT_HELP;
    }

    public Node cloneNode() {
        return new ProxyNode(original);
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set[] computedSet = computePropertySets();

        for (int i = 0; i < computedSet.length; i++) {
            sheet.put(computedSet[i]);
        }

        return sheet;
    }

    /** */
    Node[] getOriginalNodes() {
        return original;
    }

    public String getDisplayName() {
        if (displayName == null) {
            //Issue 40821, don't display extremely long names, they make
            //the property sheet huge if opened in a window
            displayName = getConcatenatedName(MAX_NAMES);
        }

        return displayName;
    }

    private String getConcatenatedName(int limit) {
        Node[] n = getOriginalNodes();
        StringBuffer name = new StringBuffer();
        String delim = NbBundle.getMessage(ProxyNode.class, "CTL_List_Delimiter"); //NOI18N

        for (int i = 0; i < n.length; i++) {
            name.append(n[i].getDisplayName());

            if (i != (n.length - 1)) {
                name.append(delim);
            }

            if ((i >= limit) && (i != (n.length - 1))) {
                name.append(NbBundle.getMessage(ProxyNode.class, "MSG_ELLIPSIS"));

                break;
            }
        }

        return name.toString();
    }

    public String getShortDescription() {
        if (getOriginalNodes().length < MAX_NAMES) {
            return NbBundle.getMessage(ProxyNode.class, "CTL_Multiple_Selection"); //NOI18N
        } else {
            if (shortDescription == null) {
                shortDescription = getConcatenatedName(Integer.MAX_VALUE);
            }

            return shortDescription;
        }
    }
    
    private ArrayList<Node.PropertySet[]> getOriginalPropertySets() {
        if( null == originalPropertySets ) {
            originalPropertySets = new ArrayList<Node.PropertySet[]>( original.length );
            
            for( int i=0; i<original.length; i++) {	    
                Node.PropertySet[] p = original[i].getPropertySets();
                originalPropertySets.add( p );
            }
            
        }
        return originalPropertySets;
    }

    /** Computes intersection of tabs and intersection
     * of properties in those tabs.
     */
    private Sheet.Set[] computePropertySets() {
        if (original.length > 0) {
            Node.PropertySet[] firstSet = getOriginalPropertySets().get( 0 );
            java.util.Set<Node.PropertySet> sheets = new HashSet<Node.PropertySet>(Arrays.asList(firstSet));

            // compute intersection of all Node.PropertySets for given nodes
            for (int i = 1; i < original.length; i++) {
                sheets.retainAll(new HashSet(Arrays.asList(getOriginalPropertySets().get(i))));
            }

            ArrayList<Sheet.Set> resultSheets = new ArrayList<Sheet.Set>(sheets.size());

            // now for all resulting sheets take common properties
            for (int i = 0; i < firstSet.length; i++) {
                if (!sheets.contains(firstSet[i]) || firstSet[i].isHidden()) {
                    continue;
                }

                Node.PropertySet current = firstSet[i];

                // creates an empty Sheet.Set with same names as current
                Sheet.Set res = new Sheet.Set();
                res.setName(current.getName());
                res.setDisplayName(current.getDisplayName());
                res.setShortDescription(current.getShortDescription());

                String tabName = (String) current.getValue("tabName"); //NOI18N

                if (tabName != null) {
                    res.setValue("tabName", tabName); //NOI18N
                }

                java.util.Set<Property> props = new HashSet<Property>(Arrays.asList(current.getProperties()));

                String propsHelpID = null;

                // intersection of properties from the corresponding tabs
                for (int j = 0; j < original.length; j++) {
                    Node.PropertySet[] p = getOriginalPropertySets().get(j);

                    for (int k = 0; k < p.length; k++) {
                        if (current.getName().equals(p[k].getName())) {
                            props.retainAll(new HashSet<Property>(Arrays.asList(p[k].getProperties())));
                        }
                    }
                }

                Node.Property[] p = current.getProperties();

                for (int j = 0; j < p.length; j++) {
                    if (!props.contains(p[j])) {
                        continue;
                    }

                    if (p[j].isHidden()) {
                        continue;
                    }

                    ProxyProperty pp = createProxyProperty(p[j].getName(), res.getName());
                    res.put(pp);
                }

                resultSheets.add(res);
            }

            return resultSheets.toArray(new Sheet.Set[resultSheets.size()]);
        }

        return new Sheet.Set[0];
    }

    /** Finds properties in original with specified
     * name in all tabs and constructs a ProxyProperty instance.
     */
    private ProxyProperty createProxyProperty(String propName, String setName) {
        Node.Property[] arr = new Node.Property[original.length];

        for (int i = 0; i < original.length; i++) {
            Node.PropertySet[] p = getOriginalPropertySets().get(i);

            for (int j = 0; j < p.length; j++) {
                if (p[j].getName().equals(setName)) {
                    Node.Property[] np = p[j].getProperties();

                    for (int k = 0; k < np.length; k++) {
                        if (np[k].getName().equals(propName)) {
                            arr[i] = np[k];
                        }
                    }
                }
            }
        }

        return new ProxyProperty(arr);
    }

    /** Property delegating to an array of Properties. It either
     * delegates to original[0] or applies changes to all
     * original properties.
     */
    private static class ProxyProperty extends Node.Property {
        private Node.Property[] original;

        /** It sets name, displayName and short description.
         * Remembers original.
         */
        public ProxyProperty(Node.Property[] original) {
            super(original[0].getValueType());
            this.original = original;
            setName(original[0].getName());
            setDisplayName(original[0].getDisplayName());
            setShortDescription(original[0].getShortDescription());
        }

        /** Test whether the property is writable.Calls all delegates.
         * If any of them returns false returns false, otherwise return true.
         */
        public boolean canWrite() {
            for (int i = 0; i < original.length; i++) {
                if (!original[i].canWrite()) {
                    return false;
                }
            }

            return true;
        }

        /** Test whether the property is readable. Calls all delegates.
         * If any of them returns false returns false, otherwise return true.
         * @return <CODE>true</CODE> if all delegates returned true
         */
        public boolean canRead() {
            for (int i = 0; i < original.length; i++) {
                if (!original[i].canRead()) {
                    return false;
                }
            }

            return true;
        }

        /** If all values are the same returns the value otherwise returns null.
         * @return the value of the property
         * @exception IllegalAccessException cannot access the called method
         * @exception InvocationTargetException an exception during invocation
         */
        public Object getValue() throws IllegalAccessException, java.lang.reflect.InvocationTargetException {
            Object o = original[0].getValue();

            if (o == null) {
                return null;
            }

            for (int i = 0; i < original.length; i++) {
                if (!o.equals(original[i].getValue())) {
                    throw new DifferentValuesException();
                }
            }

            return o;
        }

        /** Set the value. Calls setValue on all delegates.
         * @param val the new value of the property
         * @exception IllegalAccessException cannot access the called method
         * @exception IllegalArgumentException wrong argument
         * @exception InvocationTargetException an exception during invocation
         */
        public void setValue(Object val)
        throws IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
            for (int i = 0; i < original.length; i++) {
                original[i].setValue(val);
            }
        }

        /** Retrieve a named attribute with this feature.
         * If all values are the same returns the value otherwise returns null.
         * @param attributeName  The locale-independent name of the attribute
         * @return The value of the attribute.  May be null if
         *      the attribute is unknown.
         */
        public Object getValue(String attributeName) {
            Object o = original[0].getValue(attributeName);

            if (Boolean.FALSE.equals(o)) {
                //issue 38319 - Boolean.FALSE should override even null -
                //relevant primarily to the general hint canEditAsText,
                //but makes sense generally
                return o;
            }

            if (o == null) {
                return null;
            }

            for (int i = 1; i < original.length; i++) {
                if (Boolean.FALSE.equals(original[i])) {
                    // issue 38319, see comment above
                    return original[i];
                }
                if (!o.equals(original[i].getValue(attributeName))) {
                    // Optionally log it and return null
                    if (Boolean.getBoolean("netbeans.ps.logDifferentValues")) {
                        Logger.getLogger(ProxyNode.class.getName()).log(Level.WARNING, null,
                                          new DifferentValuesException("Different values in attribute " +
                                                                       attributeName +
                                                                       " for proxy property " +
                                                                       getDisplayName() +
                                                                       "(" +
                                                                       this +
                                                                       ") first value=" +
                                                                       o +
                                                                       " property " +
                                                                       i + "(" +
                                                                       original[i].getClass().getName() +
                                                                       " returns " +
                                                                       original[i].getValue(attributeName)));
                    }
                    return null;
                }
            }

            return o;
        }

        /** Associate a named attribute with this feature. Calls setValue on all delegates.
         * @param attributeName  The locale-independent name of the attribute
         * @param value  The value.
         */
        public void setValue(String attributeName, Object value) {
            for (int i = 0; i < original.length; i++) {
                original[i].setValue(attributeName, value);
            }
        }

        /**
         * @returns property editor from the first delegate
         */
        public java.beans.PropertyEditor getPropertyEditor() {
            return original[0].getPropertyEditor();
        }

        /** Test whether the property has a default value. If any of
         * the delegates does not support default value returns false,
         * otherwise returns true.
         * @return <code>true</code> if all delegates returned true
         */
        public boolean supportsDefaultValue() {
            for (int i = 0; i < original.length; i++) {
                if (!original[i].supportsDefaultValue()) {
                    return false;
                }
            }

            return true;
        }

        /**
         * Calls restoreDefaultValue on all delegates (original).
         * @exception IllegalAccessException cannot access the called method
         * @exception InvocationTargetException an exception during invocation
         */
        public void restoreDefaultValue() throws IllegalAccessException, java.lang.reflect.InvocationTargetException {
            for (int i = 0; i < original.length; i++) {
                original[i].restoreDefaultValue();
            }
        }

        public String toString() {
            StringBuffer sb = new StringBuffer("Proxy property for: ");
            sb.append(getDisplayName());
            sb.append('[');

            for (int i = 0; i < original.length; i++) {
                sb.append(original[i].getClass().getName());

                if (i < (original.length - 1)) {
                    sb.append(',');
                }
            }

            sb.append(']');

            return sb.toString();
        }
    }

    /** We cannot return a single value when there are different values */
    static class DifferentValuesException extends RuntimeException {
        public DifferentValuesException() {
            super();
        }

        public DifferentValuesException(String message) {
            super(message);
        }
    }
}
