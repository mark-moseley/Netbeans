/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.awt.Color;
import java.beans.*;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;

/**
 * Defines editor for choosing of any object using lookup.
 *
 * @author Jaroslav Tulach
 */
public final class ObjectEditor extends PropertyEditorSupport 
implements ExPropertyEditor {
    /** Name of the custom property that can be passed in PropertyEnv. 
     * Should contain superclass that is allowed to be 
     */
    private static final String PROP_SUPERCLASS = "superClass"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. 
     * Either Boolean.TRUE or a String, in such case the string represents
     * human readable name of the value.
     */
    private static final String PROP_NULL = "nullValue"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. 
     * A lookup to use to query for results.
     */
    private static final String PROP_LOOKUP = "lookup"; // NOI18N
    
    /** custom editor */
    private ObjectPanel customEditor;
    
    /** super class to search for */
    private Lookup.Template template;
    
    /** null or name to use for null value */
    private String nullValue;
    
    /** a special lookup to use or null */
    private Lookup lookup;
    
    /** Creates new ObjectEditor  */
    public ObjectEditor() {
    }

    /**
     * This method is called by the IDE to pass
     * the environment to the property editor.
     */
    public synchronized void attachEnv(PropertyEnv env) {
        Object obj = env.getFeatureDescriptor ().getValue (PROP_SUPERCLASS);
        if (obj instanceof Class) {
            template = new Lookup.Template ((Class)obj);
        } else {
            template = null;
        }
        
        obj = env.getFeatureDescriptor ().getValue (PROP_NULL);
        if (Boolean.TRUE.equals (obj)) {
            nullValue = NbBundle.getMessage (ObjectEditor.class, "CTL_NullValue");
        } else {
            if (obj instanceof String) {
                nullValue = (String)obj;
            } else {
                nullValue = null;
            }
        }
        
        obj = env.getFeatureDescriptor ().getValue (PROP_LOOKUP);
        lookup = obj instanceof Lookup ? (Lookup)obj : null;
        //Don't allow editing in the case only one item and tags are null
        if (getTags()==null || getTags().length <= 1) {
            env.getFeatureDescriptor().setValue("canEditAsText",Boolean.FALSE); //NOI18N
        }
    }
    
    /** A lookup to work on.
     * @return a lookup.
     */
    protected Lookup lookup () {
        Lookup l = lookup;
        return l == null ? Lookup.getDefault () : l;
    }
    
    /** A template to use.
     */
    protected Lookup.Template template () {
        if (template == null) {
            template = new Lookup.Template ();
        }
         
        return template;
    }
    
    public String getAsText() {
        Object value = getValue ();
        if (value == null) {
            return nullValue == null ? 
                NbBundle.getMessage (ObjectEditor.class, "CTL_NullValue")
            :
                nullValue;
        }
        
        Lookup.Template t = new Lookup.Template (
            template ().getType (),
            template ().getId (),
            value // instance to search for
        );
        Lookup.Item item = lookup ().lookupItem (t);
        
        if (item == null) {
            return NbBundle.getMessage (ObjectEditor.class, "CTL_NullItem");
        }
        
        return item.getDisplayName();
    }
    
    /** Searches between items whether there is one with the same display name.
     * @param str item name
     */
    public void setAsText(java.lang.String str) throws java.lang.IllegalArgumentException {
        if (nullValue != null && nullValue.equals (str)) {
            setValue (null);
            return;
        }
        
        
        Collection allItems = lookup ().lookup (template ()).allItems ();
        
        Iterator it = allItems.iterator ();
        while (it.hasNext ()) {
            Lookup.Item item = (Lookup.Item)it.next ();
            
            if (item.getDisplayName().equals (str)) {
                setValue (item.getInstance ());
                firePropertyChange();
                return;
            }
        }
        IllegalArgumentException iae = new IllegalArgumentException (str);
        String msg = MessageFormat.format(
            NbBundle.getMessage(
                ObjectEditor.class, "FMT_EXC_GENERIC_BAD_VALUE"),  //NOI18N
                new Object[] {str});
        ErrorManager.getDefault().annotate(iae, ErrorManager.USER, str, 
         msg, null, new java.util.Date());
        throw iae;        
    }
    
    /** List of all display names for items.
     * @return array of strings
     */
    public java.lang.String[] getTags() {
        Collection allItems = lookup ().lookup (template ()).allItems ();
        if (allItems.size() <= 1) {
            return null;
        }
   
        ArrayList list = new ArrayList (allItems.size () + 1);
        if (nullValue != null) {
            list.add (nullValue);
        }
        
        Iterator it = allItems.iterator ();
        while (it.hasNext ()) {
            Lookup.Item item = (Lookup.Item)it.next ();
            list.add (item.getDisplayName());
        }
        
        String[] retValue = new String[list.size()];
        list.toArray(retValue);
        return retValue;
    }

    /** Yes we have custom editor.
     */
    public boolean supportsCustomEditor() {
        //Don't allow custom editor if there will be nothing to show
        return getTags()!= null && getTags().length > 1;
    }
    
    public synchronized Component getCustomEditor () {
        if (!supportsCustomEditor()) {
            return null;
        }
        if (customEditor != null) {
            return customEditor;
        }
        Lookup.Result contents = lookup().lookup(template());
        ObjectPanel panel = new ObjectPanel(contents);
        return customEditor = panel;
    }

    private class ObjectPanel extends JPanel implements ActionListener {
        public ObjectPanel(Lookup.Result res) {
            getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(ObjectEditor.class, 
                "ACSN_ObjectTree")); //NOI18N
            getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ObjectEditor.class, "ACSD_ObjectTree")); //NOI18N
            
            setLayout (new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            int row = 0;
            ButtonGroup bg = new ButtonGroup();
            Font bold = getFont().deriveFont(Font.BOLD);
            //For default metal L&F where labels are by default bold
            Font plain = getFont().deriveFont(Font.PLAIN); 
            
            Collection c = res.allItems();
            Lookup.Item[] items = new Lookup.Item[c.size()];
            items = (Lookup.Item[]) c.toArray(items);

            int BASE_LEFT_INSET=7;
            for (int i=0; i < items.length; i++) {
                JRadioButton rb = new ItemRadioButton(items[i], bold);
                if (items[i].getInstance().equals(getValue())) {
                    rb.setSelected(true);
                }
                rb.addActionListener(this);
                bg.add(rb);
                String description = getDescription(items[i]);
                
                gbc.gridx=0;
                gbc.gridy=row;
                gbc.insets = new Insets(i==0 ? 7 : 0, BASE_LEFT_INSET, 
                    description != null ? 1 : i==items.length-1 ? 7: 4, BASE_LEFT_INSET);
                gbc.fill=gbc.HORIZONTAL;
                add(rb, gbc);
                row++;
                if (description != null) {
                    JLabel lbl = new JLabel(description);
                    lbl.setLabelFor(rb);
                    lbl.setFont(plain);
                    int left = rb.getIcon() != null ? rb.getIcon().getIconWidth() : 20;
                    gbc.insets = new Insets(0, BASE_LEFT_INSET + 
                        left, 4, BASE_LEFT_INSET + left);
                    gbc.gridx=0;
                    gbc.gridy=row;
                    add(lbl, gbc);
                    row++;
               }
            }
        }
        
        private String getDescription (Lookup.Item item) {
            String id = item.getId ();
            String result = null;
            try {
                result = Introspector.getBeanInfo(item.getInstance().getClass()).getBeanDescriptor().getShortDescription();
            } catch (IntrospectionException ie) {
                //do nothing
            }
            String toCheck = item.getInstance().getClass().getName();
            toCheck = toCheck.lastIndexOf('.')!=-1 ? 
                toCheck.substring(toCheck.lastIndexOf('.')+1) : toCheck; //NOI18N
            if (toCheck.equals(result)) {
                result = null;
            } 
            return result;
        }
        
        public void actionPerformed(ActionEvent ae) {
            Lookup.Item item = ((ItemRadioButton) ae.getSource()).item;
            Object o = item.getInstance();
            setValue (item.getInstance());
            ObjectEditor.this.firePropertyChange();
        }
        
        private class ItemRadioButton extends JRadioButton {
            Lookup.Item item;
            public ItemRadioButton(Lookup.Item item, Font font) {
                this.item = item;
                setName(item.getId());
                setText(item.getDisplayName());
                setFont(font);
                getAccessibleContext().setAccessibleName(getName());
                getAccessibleContext().setAccessibleDescription(
                    getText());
            }
        }
    }
}

