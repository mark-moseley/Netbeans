/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.jsploader;

import java.io.*;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;

import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.actions.OpenAction;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.web.core.WebExecSupport;

import org.netbeans.modules.java.Util;

/** The node representation of <code>JspDataObject</code> for internet files.
*
* @author Petr Jiricka
*/
public class JspNode extends DataNode {

    private static final String EXECUTION_SET_NAME = "Execution"; // NOI18N
    private static final String SHEETNAME_TEXT_PROPERTIES = "textProperties"; // NOI18N
   

    private static final String ICON_JSP = "org/netbeans/modules/web/core/resources/jsp16"; // NOI18N
    private static final String ICON_TAG = "org/netbeans/modules/web/core/resources/tag16"; // NOI18N
    private static final String ICON_JSP_XML = "org/netbeans/modules/web/core/resources/jsp-xml16"; // NOI18N
    private static final String ICON_JSP_FRAGMENT = "org/netbeans/modules/web/core/resources/jsp-fragment16"; // NOI18N
    
    public static final String PROP_FILE_ENCODING = "encoding"; //NOI18N
    public static final String PROP_REQUEST_PARAMS   = "requestparams"; // NOI18N

    /** Create a node for the internet data object using the default children.
    * @param jdo the data object to represent
    */
    public JspNode (JspDataObject jdo) {
        super(jdo, Children.LEAF);
        initialize();
    }

    private void initialize () {
        setIconBase(getIconBase());
        setDefaultAction (SystemAction.get (OpenAction.class));
        
        String ext = getExtension();
        
        if (ext.equals(JspLoader.TAGF_FILE_EXTENSION) 
            || ext.equals(JspLoader.TAGX_FILE_EXTENSION)
            || ext.equals(JspLoader.TAG_FILE_EXTENSION))
                setShortDescription (NbBundle.getMessage(JspNode.class, "LBL_tagNodeShortDesc")); //NOI18N
        else
                setShortDescription (NbBundle.getMessage(JspNode.class, "LBL_jspNodeShortDesc")); //NOI18N
        
    }

    private String getExtension(){
        return getDataObject().getPrimaryFile().getExt();
    }
    
    public DataObject getDataObject() {
        return super.getDataObject();
    }
    
    /** Create the property sheet.
    * Subclasses may want to override this and add additional properties.
    * @return the sheet
    */
    protected Sheet createSheet () {
        Sheet.Set ps;

        Sheet sheet = super.createSheet();

        ps = new Sheet.Set ();
        ps.setName(EXECUTION_SET_NAME);
        ps.setDisplayName(NbBundle.getBundle(JspNode.class).getString("PROP_executionSetName")); //NOI18N
        ps.setShortDescription(NbBundle.getBundle(JspNode.class).getString("HINT_executionSetName")); //NOI18N
        
        ps.put(new PropertySupport.ReadWrite (
                   PROP_REQUEST_PARAMS,
                   String.class,
                   NbBundle.getBundle(JspNode.class).getString("PROP_requestParams"), //NOI18N
                   NbBundle.getBundle(JspNode.class).getString("HINT_requestParams") //NOI18N
               ) {
                   public Object getValue() {
                       return getRequestParams(((MultiDataObject)getDataObject()).getPrimaryEntry());
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       if (val instanceof String) {
                           try {
                               setRequestParams(((MultiDataObject)getDataObject()).getPrimaryEntry(), (String)val);
                           } catch(IOException e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                       else {
                           throw new IllegalArgumentException();
                       }
                   }
               }
              );
              
        // remove the params property
        //ps.remove(ExecSupport.PROP_FILE_PARAMS);
        // remove the debugger type property
        //ps.remove(ExecSupport.PROP_DEBUGGER_TYPE);

        sheet.put(ps);

        // text sheet
        ps = new Sheet.Set();
        ps.setName(SHEETNAME_TEXT_PROPERTIES);
        ps.setDisplayName(NbBundle.getBundle(JspNode.class).getString("PROP_textfileSetName")); // NOI18N
        ps.setShortDescription(NbBundle.getBundle(JspNode.class).getString("HINT_textfileSetName")); // NOI18N
        sheet.put(ps);
        
        ps.put(new PropertySupport.ReadOnly (
               PROP_FILE_ENCODING,
               String.class,
               NbBundle.getBundle(JspNode.class).getString("PROP_fileEncoding"), //NOI18N
               NbBundle.getBundle(JspNode.class).getString("HINT_fileEncoding") //NOI18N
           ) {
               public Object getValue() {
                   return ((JspDataObject)getDataObject()).getFileEncoding(true); 
               }

           }
          );     

        Node.PropertySet[] tmp = sheet.toArray();
        String ext = getExtension();
        if (ext.equals(JspLoader.TAGF_FILE_EXTENSION) 
            || ext.equals(JspLoader.TAGX_FILE_EXTENSION)
            || ext.equals(JspLoader.TAG_FILE_EXTENSION))
            for(int i = 0; i < tmp.length; i++){
                tmp[i].setValue("helpID", JspNode.class.getName() + ".Tag.PropertySheet"); // NOI18N
            }
        else    
            for(int i = 0; i < tmp.length; i++){
                tmp[i].setValue("helpID", JspNode.class.getName() + ".PropertySheet"); // NOI18N
            }
        
        
        return sheet;
    }

    static final void wrapThrowable(Throwable outer, Throwable inner, String message) {
        ErrorManager.getDefault().annotate(
            outer, ErrorManager.USER, null, message, inner, null);
    }

    /** Set request parameters for a given entry.
    * @param entry the entry
    * @param args array of arguments
    * @exception IOException if arguments cannot be set
    */
    static void setRequestParams(MultiDataObject.Entry entry, String params) throws IOException {
        StringBuffer newParams=new StringBuffer();
        String s=null;
        if (params!=null){
            for (int i=0;i<params.length();i++) {
                char ch = params.charAt(i);
                if ((int)ch!=13 && (int)ch!=10) newParams.append(ch);
            }
            s=newParams.toString();
            if (s.length()==0) s=null;
        } 
        WebExecSupport.setQueryString(entry.getFile (), s);
    }

    /** Get the request parameters associated with a given entry.
    * @param entry the entry
    * @return the arguments, or an empty string if no arguments are specified
    */
    static String getRequestParams(MultiDataObject.Entry entry) {
        return WebExecSupport.getQueryString(entry.getFile ());
    }

    /** Get the icon base.
    * This should be a resource path, e.g. <code>/some/path/</code>,
    * where icons are held. Subclasses may override this.
    * @return the icon base
    * @see #getIcons
    */
    protected String getIconBase() {
        String ext = getDataObject().getPrimaryFile().getExt();
        
        if (ext.equals(JspLoader.TAGF_FILE_EXTENSION) 
            || ext.equals(JspLoader.TAGX_FILE_EXTENSION)
            || ext.equals(JspLoader.TAG_FILE_EXTENSION))
                return ICON_TAG;
        if (ext.equals(JspLoader.JSF_EXTENSION )
            || ext.equals(JspLoader.JSPF_EXTENSION))
                return ICON_JSP_FRAGMENT;
        if (ext.equals(JspLoader.JSPX_EXTENSION))
                return ICON_JSP_XML;
        return ICON_JSP;

    }

    public HelpCtx getHelpCtx() {
        String ext = getExtension();
        if (ext.equals(JspLoader.TAGF_FILE_EXTENSION) 
            || ext.equals(JspLoader.TAGX_FILE_EXTENSION)
            || ext.equals(JspLoader.TAG_FILE_EXTENSION))
            return new HelpCtx(JspNode.class.getName() + ".Tag");//NOI18N
        else    
            return new HelpCtx(JspNode.class.getName());//NOI18N
    }
}

