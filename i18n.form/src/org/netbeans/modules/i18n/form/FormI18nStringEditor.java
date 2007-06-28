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

package org.netbeans.modules.i18n.form;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.*;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.api.java.classpath.ClassPath;

import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormAwareEditor;
import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.form.NamedPropertyEditor;
import org.netbeans.modules.form.FormEditorSupport;
import org.netbeans.modules.i18n.I18nPanel;
import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.i18n.java.JavaI18nSupport;

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.form.FormProperty;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Property editor for editing <code>FormI18nString</code> value in form editor.
 * This editor is registered during installing i18n module
 * as editor for form properties of type <code>String</code>.
 * It provides also a capability to store such object in XML form.
 * <B>Note: </B>This class should be named FormI18nStringEditor, but due to forward compatibility 
 * remains that name.
 * 
 * @author  Petr Jiricka
 * @see FormI18nString
 * @see org.netbeans.modules.form.RADComponent
 * @see org.netbeans.modules.form.RADProperty
 */
public class FormI18nStringEditor extends PropertyEditorSupport implements FormAwareEditor, NamedPropertyEditor, XMLPropertyEditor {

    /** <code>DataObject</code> which have <code>SourceCookie</code>, and which document contains 
     * going-to-be-internatioanlized string.
     */
    private FormDataObject sourceDataObject;

    /** Name of resource string XML element. */
    public static final String XML_RESOURCESTRING = "ResourceString"; // NOI18N
    /** Name of plain string XML element (string not internationalized). */
    private static final String XML_PLAINSTRING = "PlainString"; // NOI18N
    /** Name of argument XML element (child element of resource string element). */
    public static final String XML_ARGUMENT = "Argument"; // NOI18N
    /** Name of attribute bundle of resource string XML element. */
    public static final String ATTR_BUNDLE   = "bundle"; // NOI18N
    /** Name of attribute key of resource string XML element. */
    public static final String ATTR_KEY      = "key"; // NOI18N
    /** Name of attribute storing value of non-internationalized string. */
    private static final String ATTR_VALUE = "value"; // NOI18N
    /** Name of attribute identifier of resource string XML element. */
    public static final String ATTR_IDENTIFIER = "identifier"; // NOI18N
    /** Name of attribute replace format XML element. */
    public static final String ATTR_REPLACE_FORMAT = "replaceFormat"; // NOI18N
    /** Name of attribute index of argument XML element. */
    public static final String ATTR_INDEX    = "index"; // NOI18N
    /** Name of attribute java code of argument XML element. */
    public static final String ATTR_JAVACODE = "javacode"; // NOI18N

    /** Maximal index of arguments in one argument XML element. */
    private static final int MAX_INDEX       = 1000;
    
    private final ResourceBundle bundle;

    /** Constructor. Creates new <code>ResourceBundleStringFormEditor</code>. */
    public FormI18nStringEditor() {
        bundle = NbBundle.getBundle(FormI18nStringEditor.class);
    }


    /** Overrides superclass method.
     * @return null as we don't support this feature */
    public String[] getTags() {
        return null;
    }

    /** Sets as text. Overrides superclass method to be dummy -> don't throw
     * <code>IllegalArgumentException</code> . */
    public void setAsText(String text) {
        setValue(text);
    }
        
    
    /** Overrides superclass method. 
     * @return text for the current value */
    public String getAsText() {
        Object value = getValue();
        if (value instanceof String)
            return (String) value;

        FormI18nString i18nString = (FormI18nString) value;
        return i18nString.getValue();
    }

    /** Overrides superclass method. Gets string, piece of code which will replace the hardcoded
     * non-internationalized string in the source. The default form is:
     * <p>
     * <b><identifier name>.getString("<key name>")</b>
     * or if arguments for the ResoureBundleStrin are set the form:
     * <p>
     * <b>java.text.MessageFormat.format(<identifier name>getString("<key name>"), new Object[] {<code set in Parameters and Comments panel>})</b>
     */
    public String getJavaInitializationString() {
        FormI18nString i18nString = (FormI18nString) getValue();
        String javaString = i18nString.getReplaceString();
        if (javaString == null) { // some problem, return plain string (better than null)
            return "\"" + FormI18nSupport.toAscii(i18nString.getValue()) + "\""; // NOI18N
        }

        // calls of ResourceBundle.getBundle should be "cached" in a variable by form editor
        StringBuilder buf = new StringBuilder();
        buf.append(CODE_MARK_LINE_COMMENT + "NOI18N"); // NOI18N
        if (javaString.startsWith("java.util.ResourceBundle.getBundle(")) { // NOI18N
            int end = javaString.indexOf(").") + 1; // NOI18N
            if (end > 0) {
                // use special code marks (*/\n\\2) to encode 3 data elements:
                // - the code to replace
                // - the type of variable to declare for the code
                // - suggested variable name
                buf.append(CODE_MARK_VARIABLE_SUBST);
                buf.append(javaString.substring(0, end)); // the code to substitute
                buf.append(CODE_MARK_VARIABLE_SUBST);
                buf.append("java.util.ResourceBundle"); // NOI18N
                buf.append(CODE_MARK_VARIABLE_SUBST);
                buf.append("bundle"); // NOI18N
                javaString = javaString.substring(end);
            }
        }
        buf.append(CODE_MARK_END); // indicates that real code follows
        buf.append(javaString);
        return buf.toString();
    }

    // special code marks recognized by form editor:
    private static final String CODE_MARK_END = "*/\n\\0"; // NOI18N
    private static final String CODE_MARK_LINE_COMMENT = "*/\n\\1"; // NOI18N
    private static final String CODE_MARK_VARIABLE_SUBST = "*/\n\\2"; // NOI18N

    /** Overrides superclass method.
     * @return <code>ResourceBundlePanel</code> fed with <code>FormI18nString</code> value. */
    public Component getCustomEditor() {
        FormI18nString formI18nString;
        Object value = getValue();
        if (value instanceof FormI18nString) {
            formI18nString = new FormI18nString((FormI18nString)value);
        }
        else {
            formI18nString = createFormI18nString();
            if (value instanceof String)
                formI18nString.setValue((String)value);
            DataObject lastResource = I18nUtil.getOptions().getLastResource2();
            if (lastResource != null) {
                FileObject sourceFile = sourceDataObject.getPrimaryFile();
                FileObject bundleFile = lastResource.getPrimaryFile();
                ClassPath sourceClassPath = ClassPath.getClassPath(
                                                sourceFile, ClassPath.SOURCE);
                if (sourceClassPath.contains(bundleFile)) {
                    formI18nString.getSupport().getResourceHolder().setResource(
                            lastResource);
                }
            }
        }
        return new CustomEditor(formI18nString, getProject(), sourceDataObject.getPrimaryFile());
    }
    
    private Project getProject() {
      return org.netbeans.modules.i18n.Util.getProjectFor(sourceDataObject);
    }


    /** Overrides superclass method. 
     * @return true since we support this feature */
    public boolean supportsCustomEditor() {
        return true;
    }

    /** Overrides superclass method.
     * @param value sets the new value for this editor */
    public void setValue(Object object) {
        if (object instanceof String && getValue() instanceof FormI18nString) {
            FormI18nString i18nString = new FormI18nString((FormI18nString)getValue());
            i18nString.setValue((String)object);
            object = i18nString;
        }
        super.setValue(object);
    }

    /** Creates <code>FormI18nString</code> instance. Helper method. */
    private FormI18nString createFormI18nString() {
        // Note: Only here we can have support without possible document loading.
        return new FormI18nString(sourceDataObject);
    }
    
    /** 
     * Implements <code>FormAwareEditor</code> method. 
     * If a property editor implements the <code>FormAwareEditor</code>
     * interface, this method is called immediately after the PropertyEditor
     * instance is created or the custom editor is obtained from getCustomEditor().
     * @param model the <code>FormModel</code> representing meta-data of current form */
    public void setContext(FormModel model, FormProperty property) {
        if (model != null) {
            sourceDataObject = FormEditorSupport.getFormDataObject(model);
        }
    }

    /**
     * Implements <code>NamePropertyEditor</code> interface method.
     * @return Display name of the property editor 
     */
    public String getDisplayName () {
        return NbBundle.getMessage(FormI18nStringEditor.class, "CTL_PropertyEditorName"); // NOI18N
    }


    /** 
     * Implements <code>XMLPropertyEditor</code> interface method.
     * Called to load property value from specified XML subtree. If succesfully loaded,
     * the value should be available via the getValue method.
     * An IOException should be thrown when the value cannot be restored from the specified XML element
     * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
     * @exception IOException thrown when the value cannot be restored from the specified XML element
     * @see org.w3c.dom.Node
     */
    public void readFromXML(Node domNode) throws IOException {
        FormI18nString formI18nString = createFormI18nString();
        NamedNodeMap namedNodes = domNode.getAttributes ();

        if (XML_PLAINSTRING.equals(domNode.getNodeName())) {
            // plain string was formerly handled in a special way by this
            // property editor, but not anymore
            Node node = namedNodes.getNamedItem(ATTR_VALUE);
            if (node != null) { // just set the plain string value
                setValue(node.getNodeValue());
            }
            return;
        }

        if(!XML_RESOURCESTRING.equals (domNode.getNodeName ())) {
            throw new IOException ();
        }

        try {
            Node node;
            // Retrieve bundle name.
            node = namedNodes.getNamedItem(ATTR_BUNDLE);
            String bundleName = (node == null) ? null : node.getNodeValue();

            // Retrieve key name.
            node = namedNodes.getNamedItem(ATTR_KEY);
            String key = (node == null) ? null : node.getNodeValue();

            // Set the resource bundle.
            if(bundleName != null) {
                DataObject resourceDO = null;
                FileObject sourceFo = sourceDataObject.getPrimaryFile();
                if ( sourceFo != null ) {
                    FileObject fileObject = org.netbeans.modules.i18n.Util.
                        getResource(sourceFo, bundleName);
                    
                    if(fileObject != null) {
                        try {
                            resourceDO = DataObject.find(fileObject);
                            if( resourceDO.getClass().equals(formI18nString.getSupport().getResourceHolder().getResourceClasses()[0])) // PENDING
                                formI18nString.getSupport().getResourceHolder().setResource(resourceDO);
                        } 
                        catch (IOException e) {
                        }
                    }
                }
                if (resourceDO == null)
                    formI18nString.bundleName = bundleName;
            }

            // Set the key property.
            if(key != null && key.length() > 0) {
                formI18nString.setKey(key);
                
                // Set value and comment.
                formI18nString.setValue(formI18nString.getSupport().getResourceHolder().getValueForKey(key));
                formI18nString.setComment(formI18nString.getSupport().getResourceHolder().getCommentForKey(key));
            }

            // Try to get identifier value.
            ((JavaI18nSupport)formI18nString.getSupport()).createIdentifier();            
            
            node = namedNodes.getNamedItem(ATTR_IDENTIFIER);
            if(node != null) {
                String identifier = (node == null) ? null : node.getNodeValue();
                
                if(identifier != null)
                    ((JavaI18nSupport)formI18nString.getSupport()).setIdentifier(identifier);
            }
            
            // Try to get init format string value.
            node = namedNodes.getNamedItem(ATTR_REPLACE_FORMAT);
            if(node != null) {
                String replaceFormat = node.getNodeValue();
                
                if(replaceFormat != null && replaceFormat.length() > 0) {
                    
                    // Note: This part of code is only due to use in some development builds of MessageFormat 
                    // instead of MapFormat. If somebidy used those builds the replace code is in MessageFormat 
                    // so we will convert it to MapFormat.
                    // This could be later extracted.
                    // Note if the replace form at was in the MessageFormat convert to MapFormat
                    // Don't throw away.
                    Map map = new HashMap(6);
                    map.put("0", "{identifier}"); // NOI18N
                    map.put("1", "{key}"); // NOI18N
                    map.put("2", "{bundleNameSlashes}"); // NOI18N
                    map.put("3", "{bundleNameDots}"); // NOI18N
                    map.put("4", "{sourceFileName}"); // NOI18N
                    map.put("fileName", "{sourceFileName}"); // NOI18N
                    
                    String newReplaceFormat = MapFormat.format(replaceFormat, map);
                    
                    formI18nString.setReplaceFormat(newReplaceFormat);
                }
            } else {
                // Read was not succesful (old form or error) -> set old form replace format.
                formI18nString.setReplaceFormat((String)I18nUtil.getDefaultReplaceFormat(false));
            }
        } catch(NullPointerException npe) {
            throw new IOException ();
        }

        // Retrieve the arguments.
        if(domNode instanceof Element) {
            Element domElement = (Element)domNode;
            NodeList argNodeList = domElement.getElementsByTagName(XML_ARGUMENT);

            // Find out the highest index.
            int highest = -1;
            for(int i = 0; i < argNodeList.getLength(); i++) {
                NamedNodeMap attributes = argNodeList.item(i).getAttributes();
                
                Node indexNode = attributes.getNamedItem (ATTR_INDEX);
                String indexString = (indexNode == null) ? null : indexNode.getNodeValue ();

                if(indexString != null) {
                    try {
                        int index = Integer.parseInt(indexString);
                        if (index > highest && index < MAX_INDEX)
                            highest = index;
                    } catch (Exception e) {}
                }
            }

            // Construct the array.
            String[] parameters = new String[highest + 1];

            // Fill the array.
            for (int i = 0; i < argNodeList.getLength(); i++) {
                NamedNodeMap attr = argNodeList.item(i).getAttributes ();
                
                Node indexNode = attr.getNamedItem (ATTR_INDEX);
                String indexString = (indexNode == null) ? null : indexNode.getNodeValue ();
                if (indexString != null) {
                    try {
                        int index = Integer.parseInt(indexString);
                        if (index < MAX_INDEX) {
                            Node javaCodeNode = attr.getNamedItem (ATTR_JAVACODE);
                            String javaCode = (javaCodeNode == null) ? null : javaCodeNode.getNodeValue ();
                            parameters[index] = javaCode;
                        }
                    } catch (Exception e) {}
                }
            }

            // Fill all the values in case some are missing - make it really foolproof.
            for (int i = 0; i < parameters.length; i++)
                if (parameters[i] == null)
                    parameters[i] = ""; // NOI18N

            // Set the parameters.
            formI18nString.setArguments(parameters);
        }

        // Set the value for this editor.
        setValue(formI18nString);
    }

    /**
     * Implements <code>XMLPropertyEditor</code> interface method.
     * Called to store current property value into XML subtree. The property value should be set using the
     * setValue method prior to calling this method.
     * @param doc The XML document to store the XML in - should be used for creating nodes only
     * @return the XML DOM element representing a subtree of XML from which the value should be loaded
     */
    public Node storeToXML(Document doc) {
        FormI18nString formI18nString = (FormI18nString) getValue();
        Element element;
        element = doc.createElement (XML_RESOURCESTRING);

        String bundleName;
        if (formI18nString.getSupport().getResourceHolder().getResource() == null) {
            bundleName = formI18nString.bundleName;
        } else {
            bundleName = org.netbeans.modules.i18n.Util.
                getResourceName(formI18nString.getSupport().getSourceDataObject().getPrimaryFile(),
                                formI18nString.getSupport().getResourceHolder().getResource().getPrimaryFile(),'/', true);
            if (bundleName == null) bundleName = ""; // NOI18N
        }

        // Set bundle and key property.    
        element.setAttribute(ATTR_BUNDLE, bundleName);
        element.setAttribute(ATTR_KEY, (formI18nString.getKey() == null) ? "" : formI18nString.getKey()); // NOI18N
        // Don't save identifier, replace the identifier argument with actual value in format.
        JavaI18nSupport support = (JavaI18nSupport)formI18nString.getSupport();
        if(support.getIdentifier() == null)
            support.createIdentifier();
        Map map = new HashMap(1);
        map.put("identifier", support.getIdentifier()); // NOI18N
        element.setAttribute(ATTR_REPLACE_FORMAT, formI18nString.getReplaceFormat() == null ? "" : MapFormat.format(formI18nString.getReplaceFormat(), map) ); // NOI18N

        // Append subelements corresponding to parameters.
        String[] arguments = formI18nString.getArguments();
        for (int i = 0; i < arguments.length; i++) {
            Element childElement = doc.createElement (XML_ARGUMENT);
            childElement.setAttribute (ATTR_INDEX, "" + i); // NOI18N
            childElement.setAttribute (ATTR_JAVACODE, arguments[i]);
            try {
                element.appendChild(childElement);
            } catch (DOMException de) {}
        }

        return element;
    }

    /** Custom editor for this property editor. */
    private static class CustomEditor extends JPanel implements EnhancedCustomPropertyEditor {
        private I18nPanel i18nPanel;
        
        /** Constructor. */
        public CustomEditor(I18nString i18nString, Project project, FileObject file) { //, boolean noI18n
            i18nPanel = new I18nPanel(i18nString.getSupport().getPropertyPanel(), false, project, file);
            setLayout(new java.awt.BorderLayout());
            add(i18nPanel);

            i18nPanel.setI18nString(i18nString);

            HelpCtx.setHelpIDString(this, I18nUtil.HELP_ID_FORMED);
        }

        /** Implements <code>EnhancedCustomPropertyEditor</code> interface. */
        public Object getPropertyValue() throws IllegalStateException {
            I18nString i18nString = i18nPanel.getI18nString();

            if(i18nString == null 
                || !(i18nString instanceof FormI18nString)
                || i18nString.getSupport().getResourceHolder().getResource() == null 
                || i18nString.getKey() == null) {

                // Notify user that invalid value set.
                IllegalStateException ise = new IllegalStateException();
                String message = NbBundle.getMessage(FormI18nStringEditor.class, "MSG_InvalidValue"); // NOI18N
                ErrorManager.getDefault().annotate(
                     ise, ErrorManager.WARNING, message,
                     message, null, null);
                throw ise;
            }

            return i18nString;
        }
    }

    /**
     * Panel containing a text area for editing plain text. Used for editing
     * not internationalized strings - replacing the I18nPanel.
     */
    private static class StringPanel extends JPanel {
        private JTextArea textArea;

        private StringPanel() {
            textArea = new JTextArea();
            JScrollPane scroll = new JScrollPane(textArea);
            GroupLayout layout = new GroupLayout(this);
            layout.setAutocreateContainerGaps(true);
            setLayout(layout);
            layout.setHorizontalGroup(layout.createSequentialGroup().add(scroll));
            layout.setVerticalGroup(layout.createSequentialGroup().add(scroll));
        }

        String getString() {
            return textArea.getText();
        }

        void setString(String str) {
            textArea.setText(str);
        }
    }

}
