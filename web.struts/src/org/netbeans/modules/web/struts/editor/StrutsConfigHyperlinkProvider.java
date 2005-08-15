/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.struts.editor;

import java.text.MessageFormat;
import java.util.Hashtable;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.java.JMIUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.struts.StrutsConfigDataObject;
import org.netbeans.modules.web.struts.StrutsConfigUtilities;
import org.openide.ErrorManager;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author petr
 */
public class StrutsConfigHyperlinkProvider implements HyperlinkProvider {
    
    static private boolean debug = false;
    private static Hashtable hyperlinkTable;
    
    private final int JAVA_CLASS = 0;
    private final int FORM_NAME = 1;
    private final int RESOURCE_PATH = 2;
    
    {
        hyperlinkTable = new Hashtable();
        hyperlinkTable.put("form-bean#type", new Integer(JAVA_CLASS));      //NOI18N
        hyperlinkTable.put("action#name", new Integer(FORM_NAME));          //NOI18N
        hyperlinkTable.put("action#type", new Integer(JAVA_CLASS));         //NOI18N
        hyperlinkTable.put("action#forward", new Integer(RESOURCE_PATH));   //NOI18N
        hyperlinkTable.put("forward#path", new Integer(RESOURCE_PATH));     //NOI18N
    }
    
    private int valueOffset;
    private String [] eav = null;
    /** Creates a new instance of StrutsHyperlinkProvider */
    public StrutsConfigHyperlinkProvider() {
    }

    public int[] getHyperlinkSpan(javax.swing.text.Document doc, int offset) {
        if (debug) debug (":: getHyperlinkSpan");
        if (eav != null){
            return new int []{valueOffset, valueOffset + eav[2].length() -1};
        }
        return null;
    }

    public boolean isHyperlinkPoint(javax.swing.text.Document doc, int offset) {
        if (debug) debug (":: isHyperlinkSpan - offset: " + offset); //NOI18N
        
        // PENDING - this check should be removed, when 
        // the issue #61704 is solved.
        DataObject dObject = NbEditorUtilities.getDataObject(doc);
        if (! (dObject instanceof StrutsConfigDataObject))
            return false;
        
        eav = getElementAttrValue(doc, offset); 
        if (eav != null){ 
            if (hyperlinkTable.get(eav[0]+"#"+eav[1])!= null)
                return true;
        }
        return false;
    }

    public void performClickAction(javax.swing.text.Document doc, int offset) {
        if (debug) debug (":: performClickAction");
        if (hyperlinkTable.get(eav[0]+"#"+eav[1])!= null){
            int type = ((Integer)hyperlinkTable.get(eav[0]+"#"+eav[1])).intValue();
            switch (type){
                case JAVA_CLASS: findJavaClass(eav[2], doc); break;
                case FORM_NAME: findForm(eav[2], (BaseDocument)doc);break;
                case RESOURCE_PATH: findResourcePath(eav[2], (BaseDocument)doc);break;
            }
        }
    }
    
    static void debug(String message){
        System.out.println("StrutsHyperlinkProvider: " + message); //NoI18N
    }
    /** This method finds the value for an attribute of element of on the offset. 
     * @return Returns null, when the offset is not a value of an attribute. If the there is value
     * of an attribute, then returns String array [element, attribute, value].
     */
    private String[] getElementAttrValue(javax.swing.text.Document doc, int offset){
        String attribute = null;
        String tag = null;
        String value = null;
        
        try {
            BaseDocument bdoc = (BaseDocument) doc;
            JTextComponent target = Utilities.getFocusedComponent();
            
            if (target == null || target.getDocument() != bdoc)
                return null;
            
            ExtSyntaxSupport sup = (ExtSyntaxSupport)bdoc.getSyntaxSupport();
            //TokenID tokenID = sup.getTokenID(offset);
            TokenItem token = sup.getTokenChain(offset, offset+1);
            //if (debug) debug ("token: "  +token.getTokenID().getNumericID() + ":" + token.getTokenID().getName());
            // when it's not a value -> do nothing.
            if (token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ATTRIBUTE_VALUE)
                return null;
            value = token.getImage();
            if (value != null){
               // value = value.substring(0, offset - token.getOffset());
                //if (debug) debug ("value to cursor: " + value);
                value = value.trim();
                valueOffset = token.getOffset();
                if (value.charAt(0) == '"') {
                    value = value.substring(1);
                    valueOffset ++;
                }
                
                if (value.length() > 0  && value.charAt(value.length()-1) == '"') value = value.substring(0, value.length()-1);
                value = value.trim();
                //if (debug) debug ("value: " + value);
            }
            
            //if (debug) debug ("Token: " + token);
            // Find attribute and tag
            // 5 - attribute
            // 4 - tag
            while(token != null && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ATTRIBUTE 
                    && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ELEMENT)
                token = token.getPrevious();
            if (token != null && token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ATTRIBUTE){
                attribute = token.getImage();
                while(token != null && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ELEMENT)
                    token = token.getPrevious();
                if (token != null && token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ELEMENT)
                    tag = token.getImage();
            }
            if (attribute == null || tag == null)
                return null;
            tag = tag.substring(1);
            if (debug) debug ("element: " + tag );   // NOI18N
            if (debug) debug ("attribute: " + attribute ); //NOI18N
            if (debug) debug ("value: " + value );  //NOI18N
            return new String[]{tag, attribute, value};
        } catch (BadLocationException e) {
        }
        return null;
    }
    
    private void findJavaClass(String fqn, javax.swing.text.Document doc){
        OpenJavaClassThread run = new OpenJavaClassThread(fqn, (BaseDocument)doc);
        RequestProcessor.getDefault().post(run);
        //JavaMetamodel.getManager().invokeAfterScanFinished(run, NbBundle.getMessage(BaseDocument.class, "goto-source"));
    }
   
    private class OpenJavaClassThread implements Runnable {
        private String fqn;
        private BaseDocument doc;
        
        public OpenJavaClassThread(String name, BaseDocument doc){
            super();
            this.fqn = name;
            this.doc = doc;
        }
        
        public void run() {
            JMIUtils jmiUtils = JMIUtils.get(doc);
            JavaClass item = null;
            jmiUtils.beginTrans(false);
            try {
                item = jmiUtils.getExactClass(fqn);
                if (item != null) {
                    jmiUtils.openElement(item);
                } 
                else {
                    String key = "goto_source_not_found"; // NOI18N
                    String msg = NbBundle.getBundle(StrutsConfigHyperlinkProvider.class).getString(key);
                    org.openide.awt.StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object [] { fqn } ));
                }
            } finally {
                jmiUtils.endTrans(false);
            }
        }
    }
    
    private void findForm(String name, BaseDocument doc){
        ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
        
        int offset = findDefinitionInSection(sup, "form-beans", "form-bean", "name", name);
        if (offset > 0){
            JTextComponent target = Utilities.getFocusedComponent();
            target.setCaretPosition(offset);
        }
        else {
            String key = "goto_formbean_not_found"; // NOI18N
            String msg = NbBundle.getBundle(StrutsConfigHyperlinkProvider.class).getString(key);
            org.openide.awt.StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object [] { name } ));
        }
    }
    
    private void findResourcePath(String path, BaseDocument doc){
        WebModule wm = WebModule.getWebModule(NbEditorUtilities.getFileObject(doc));
        if (wm != null){
            FileObject docBase= wm.getDocumentBase();
            FileObject fo = docBase.getFileObject(path);
            if (fo == null){
                // maybe an action
                String servletMapping = StrutsConfigUtilities.getActionServletMapping(wm.getDeploymentDescriptor());
                if (servletMapping != null && servletMapping.lastIndexOf('.')>0){
                    String extension = servletMapping.substring(servletMapping.lastIndexOf('.'));
                    String actionPath;
                    if (path.endsWith(extension))
                        actionPath = path.substring(0, path.length()-extension.length());
                    else
                        actionPath = path;
                    ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
                    int offset = findDefinitionInSection(sup, "action-mappings","action","path", actionPath);
                    if (offset > 0){
                        JTextComponent target = Utilities.getFocusedComponent();
                        target.setCaretPosition(offset);
                    }
                }
            }
            else
                openInEditor(fo);
        }
    }
    
    private int findDefinitionInSection(ExtSyntaxSupport sup, String section, String tag, String attribute, String value){
        TokenItem token;
        String startSection = "<"+ section;
        String endSection = "</" + section;
        String element = "<" + tag;
        String attributeValue = "\""+ value + "\"";
        int tagOffset = 0;
        try{
            token  = sup.getTokenChain(0, 1);
            //find  section
            while (token != null
                    && !(token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ELEMENT
                    && token.getImage().equals(startSection))){
                token = token.getNext();
            }
            if (token.getImage().equals(startSection)){
                //find out, whether the section is empty
                token = token.getNext();
                while (token != null
                        && (token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ELEMENT
                        || token.getImage().equals(">")))
                    token = token.getNext();
                if(token.getImage().equals("/>") || token.getImage().equals(endSection))
                    //section is empty
                    return -1;
                while(token != null
                        && !(token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ELEMENT
                        && token.getImage().equals(endSection))){
                   //find tag
                   while (token != null
                           && (token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ELEMENT
                           || (!token.getImage().equals(endSection)
                           && !token.getImage().equals(element))) )
                       token = token.getNext();
                   if (token == null) return -1;
                   tagOffset = token.getOffset();
                   if (token.getImage().equals(element)){
                        //find attribute
                       token = token.getNext();
                       while (token != null 
                               && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ELEMENT
                               && !(token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ATTRIBUTE
                               && token.getImage().equals(attribute)))
                           token = token.getNext();
                       if (token == null) return -1;
                       if (token.getImage().equals(attribute)){
                           //find value
                           token = token.getNext();
                           while (token != null 
                                    && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ATTRIBUTE_VALUE    
                                    && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ELEMENT
                                    && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ATTRIBUTE)
                               token = token.getNext();
                           if (token.getImage().equals(attributeValue))
                               return tagOffset;
                       }
                   }
                   else 
                       token = token.getNext();
                }                
            }
        }
        catch (BadLocationException e){
            e.printStackTrace(System.out);
        }
        return -1;
    }
    
    private void openInEditor(FileObject fObj){
        if (fObj != null){
            DataObject dobj = null;
            try{
                dobj = DataObject.find(fObj);
            }
            catch (DataObjectNotFoundException e){
               ErrorManager.getDefault().notify(e);
               return; 
            }
            if (dobj != null){
                Node.Cookie cookie = dobj.getCookie(OpenCookie.class);
                if (cookie != null)
                    ((OpenCookie)cookie).open();
            }
        }
    }
}
