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

import java.util.Iterator;
import java.util.Collection;
import java.util.Vector;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Date;
import java.util.Locale;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.File;
import java.nio.charset.Charset;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.openide.*;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.CompilerCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.execution.NbClassPath;
import org.openide.execution.NbProcessDescriptor;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.util.enum.*;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.CompilerType;
import org.openide.compiler.ExternalCompilerGroup;

import org.netbeans.modules.web.core.ServletSettings;
import org.netbeans.modules.web.core.QueryStringCookie;
import org.netbeans.modules.j2ee.server.ServerInstance;
import org.netbeans.modules.j2ee.server.web.FfjJspCompileContext;
import org.netbeans.modules.j2ee.server.datamodel.WebStandardData;
import org.netbeans.modules.j2ee.impl.ServerExecSupport;
import org.netbeans.modules.j2ee.impl.ServerRegistryImpl;
import org.netbeans.modules.web.context.WebContextObject;

import org.netbeans.modules.java.JavaCompilerType;
import org.netbeans.modules.java.JavaExternalCompilerType;
import org.netbeans.modules.java.JExternalCompilerGroup;
import org.netbeans.modules.java.environment.Utilities;
import org.netbeans.modules.java.Util;
import org.openidex.nodes.looks.Look;
import org.openidex.nodes.looks.DefaultLook;
import org.openidex.nodes.looks.CompositeLook;
import org.netbeans.modules.web.core.WebExecSupport;

import org.netbeans.modules.web.jsps.parserapi.*;
/** Object that provides main functionality for internet data loader.
*
* @author Petr Jiricka
*/
public class JspDataObject extends MultiDataObject implements QueryStringCookie {

    public static final String EA_CONTENT_LANGUAGE = "AttrJSPContentLanguage"; // NOI18N
    public static final String EA_SCRIPTING_LANGUAGE = "AttrJSPScriptingLanguage"; // NOI18N
    public static final String EA_JSP_ERRORPAGE = "jsp_errorpage"; // NOI18N
    // property for the servlet dataobject corresponding to this page
    public static final String PROP_SERVLET_DATAOBJECT = "servlet_do"; // NOI18N
    public static final String PROP_CONTENT_LANGUAGE   = "contentLanguage"; // NOI18N
    public static final String PROP_SCRIPTING_LANGUAGE = "scriptingLanguage"; // NOI18N
//    public static final String PROP_ENCODING = "encoding"; // NOI18N
    public static final String PROP_SERVER_CHANGE = "PROP_SERVER_CHANGE";// NOI18N

    transient private EditorCookie servletEdit;
    transient protected JspServletDataObject servletDataObject;
    // it is guaranteed that if servletDataObject != null, then this is its 
    // last modified date at the time of last refresh
    transient private Date servletDataObjectDate;
    transient private CompileData compileData;
    transient private boolean firstStart;
    transient private Listener listener;
    
    transient final private static boolean debug = false;
    
    public JspDataObject (FileObject pf, final UniFileLoader l) throws DataObjectExistsException {
        super (pf, l);
        CookieSet cookies = getCookieSet();
        cookies.add (createJspEditorSupport());
        initialize();
    }

    // Public accessibility for e.g. JakartaServerPlugin.
    // [PENDING] Handle this more nicely.
    public org.openide.nodes.CookieSet getCookieSet0 () {
        return super.getCookieSet ();
    }

    protected org.openide.nodes.Node createNodeDelegate () {
        Lookup.Template template = new Lookup.Template( org.openidex.nodes.looks.Look.class ); 
        Lookup.Result result = Lookup.getDefault().lookup( template );
        Collection cls = result.allInstances(); 
        Look defaultLook = null;
        Look wellKnown = null;
        Object o = new JspNode (this);

        for( Iterator it = cls.iterator(); it.hasNext();  ) {
            Look look = (Look)it.next();
            // System.out.println ("Inspecting look " + look); // NOI18N

            // ignore it now - CNFE when jspie is missing
            // if (look.isLookStandalone (o) == false) continue;
            
            // System.out.println ("\tpassed");
            
            // skip some well known looks
            if (DefaultLook.class.equals(look.getClass())) {
                if (wellKnown == null) {
                    wellKnown = look;
                }
            // } else if (JspServletDefaultLook.class.equals(look.getClass())) {
            } else if (CompositeLook.class.equals(look.getClass())
                   &&  "Web-Look".equals (look.getName ())) {   // NOI18N
                wellKnown = look;                    
                break;
            } else {
                // System.out.println ("\tand using as default"); // NOI18N
                defaultLook = look;
            }
        }

        if (defaultLook == null) {
            defaultLook = wellKnown;
        }

        // System.out.println ("Default look for " + this + " = " + defaultLook); // NOI18N

        Node ret = new WebLookNode (o, wellKnown == null? defaultLook: wellKnown);
        // System.out.println ("Testing " + ret.getLook()); // NOI18N
        return ret;
    }

    /** Creates a EditorSupport for this page. May return null. */
    protected BaseJspEditorSupport createJspEditorSupport() {
        return new BaseJspEditorSupport(this);
    }

    protected EditorCookie createServletEditor() {
        return new ServletEditor(this);
    }
    
    /** Creates an execution (and debugging) support for this page. May return null. */
    protected Node.Cookie createExecSupport() {
        return new ServerExecSupport(getPrimaryEntry());
    }
    
    public synchronized CompileData getPlugin() {
        if (compileData == null) {
            if ( firstStart ) {
                addWebContextListener();
		firstStart=false;
            }
   	    compileData = new CompileData(this);
       	    checkRefreshServlet();
        }
        return compileData;
    }
    
    /** Invalidates the current copy of server plugin for this JSP.
    * @param reload true if the new version of the plugin should be loaded.
    */
    public synchronized void refreshPlugin(boolean reload) {
//System.out.println("REFRESHING PLUGIN " + reload);
        compileData = null;
        if (reload)
            getPlugin();
    }

    public void refreshPlugin() {
        refreshPlugin(true);
    }

    public JspServletDataObject getServletDataObject() {
        // force registering the servlet
        getPlugin();
        return servletDataObject;
    }

    /** Returns the MIME type of the content language for this page set in this file's attributes. 
     * If nothing is set, defaults to 'text/html'.
     */
    public String getContentLanguage() {
        try {
            String contentLanguage = (String)getPrimaryFile ().getAttribute (EA_CONTENT_LANGUAGE);
            if (contentLanguage != null) {
                return contentLanguage;
            }
        } catch (Exception ex) {
            // null pointer or IOException
        }
        return "text/html"; // NOI18N
    }

    /** Sets the MIME type of the content language for this page. The language is stored 
     * in this page's filesystem attributes set in this file's attributes. 
     */
    public void setContentLanguage(String contentLanguage) throws IOException {
        getPrimaryFile ().setAttribute (EA_CONTENT_LANGUAGE, contentLanguage);
        firePropertyChange(PROP_CONTENT_LANGUAGE, null, contentLanguage);
    }

    /** Returns the MIME type of the scripting language for this page set in this file's attributes. 
     * If nothing is set, defaults to 'text/x-java'.
     */
    public String getScriptingLanguage() {
        try {
            String scriptingLanguage = (String)getPrimaryFile ().getAttribute (EA_SCRIPTING_LANGUAGE);
            if (scriptingLanguage != null) {
                return scriptingLanguage;
            }
        } catch (Exception ex) {
            // null pointer or IOException
        }
        return "text/x-java"; // NOI18N
    }

    /** Sets the MIME type of the scripting language for this page. The language is stored 
     * in this page's filesystem attributes set in this file's attributes. 
     */
    public void setScriptingLanguage(String scriptingLanguage) throws IOException {
        getPrimaryFile ().setAttribute (EA_SCRIPTING_LANGUAGE, scriptingLanguage);
        firePropertyChange(PROP_SCRIPTING_LANGUAGE, null, scriptingLanguage);
    }
    
    /** Gets the raw encoding from the fileobject, 
     * ensures beckward compatibility.
     **/
    static String getFileEncoding0(FileObject someFile) {
        String enc = Util.getFileEncoding0(someFile);
        // backward compatibility - read the old attribute
        if (enc == null) {
            enc = (String)someFile.getAttribute ("AttrEncoding"); // NOI18N
        }
        return enc;
    }
    
    static String getFileEncoding(FileObject someFile) {
        String enc = getFileEncoding0(someFile);
        if (enc == null) {
            enc = getDefaultEncoding();
        }
        return enc;
    }
    
    
    public static String getDefaultEncoding() {
        String language = Locale.getDefault().getLanguage();
        if (language.startsWith("en")) {
            // we are English
            return "ISO-8859-1"; // NOI18N
            // per JSP 1.2 specification, the default encoding is always ISO-8859-1,
            // regardless of the setting of the file.encoding property
            //return System.getProperty("file.encoding", "ISO-8859-1");
        }
        return canonizeEncoding(System.getProperty("file.encoding", "ISO-8859-1"));

/*        if ("ja".equals(language)) { // NOI18N
            // we are Japanese
            if (org.openide.util.Utilities.isUnix())
                return "EUC-JP"; // NOI18N
            else
                return "Shift_JIS"; // NOI18N
        }
        else
            // we are English
            return "ISO-8859-1"; // NOI18N*/
    }
    
    private static final String CORRECT_WINDOWS_31J = "windows-31j";
    private static final String CORRECT_EUC_JP = "EUC-JP";
    private static final String CORRECT_GB2312 = "GB2312";
    private static final String CORRECT_BIG5 = "BIG5";
    
    private static String canonizeEncoding(String encodingAlias) {
        
        // canonic name first
        if (Charset.isSupported(encodingAlias)) {
            Charset cs = Charset.forName(encodingAlias);
            encodingAlias = cs.name();
        }
        
        // this is not supported on JDK 1.4.1
        if (encodingAlias.equalsIgnoreCase("MS932")) {
            return CORRECT_WINDOWS_31J;
        }
        // this is not a correct charset by http://www.iana.org/assignments/character-sets
        if (encodingAlias.equalsIgnoreCase("euc-jp-linux")) {
            return CORRECT_EUC_JP;
        }
        // chinese encodings that must be adjusted
        if (encodingAlias.equalsIgnoreCase("EUC-CN")) {
            return CORRECT_GB2312;
        }
        if (encodingAlias.equalsIgnoreCase("GBK")) {
            return CORRECT_GB2312;
        }
        if (encodingAlias.equalsIgnoreCase("GB18030")) {
            return CORRECT_GB2312;
        }
        if (encodingAlias.equalsIgnoreCase("EUC-TW")) {
            return CORRECT_BIG5;
        }

        return encodingAlias;
    }

    private void printJob(CompilerJob job) {
/*        System.out.println("-- compilers --"); // NOI18N
        java.util.Iterator compilers = job.compilers().iterator();
        for (; compilers.hasNext();) {
            org.openide.compiler.Compiler comp = (org.openide.compiler.Compiler)compilers.next();
            CompilerJob newJob = new CompilerJob(Compiler.DEPTH_ZERO);
            newJob.add(comp);
            System.out.println(comp.toString() + " upToDate=" + newJob.isUpToDate());
        }
        System.out.println("-- x --");*/ // NOI18N
    }

    private void initialize() {
    	firstStart = true;
        listener = new Listener();
        getPrimaryFile().addFileChangeListener(listener);
        addPropertyChangeListener(listener);
        refreshPlugin(false);
    }
    
    private void addWebContextListener() {
        //server changes
        try {
            FileObject root = JspCompileUtil.getContextRoot(getPrimaryFile());
            DataObject wco = DataObject.find(root);
            if (!(wco instanceof WebContextObject)) {
                ServerRegistryImpl source = ServerRegistryImpl.getRegistry();
                source.addServerRegistryListener(
                    ServerRegistryImpl.weakServerRegistryListener(listener, source));
            }
            else {
                wco.addPropertyChangeListener(WeakListener.propertyChange(listener, wco));
            }
        } 
        catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
    }
    
    /** Updates classFileData, servletDataObject, servletEdit 
     * This does not need to be synchronized, because the calling method
     * getPlugin() is synchronized.
     */
    private void checkRefreshServlet() {

        final DataObject oldServlet = servletDataObject;
	if(debug) System.out.println("refreshing servlet, old = " + oldServlet); // NOI18N

        // dataobject
        try {
            FileObject servletFileObject = updateServletFileObject();
            if(debug) System.out.println("refreshing servlet, new servletFile = " + servletFileObject); // NOI18N
            if (servletFileObject != null) {
                // if the file has not changed, just return
                if ((oldServlet != null) && 
                    (oldServlet.getPrimaryFile() == servletFileObject) &&
                    (servletFileObject.lastModified().equals(servletDataObjectDate)))
                    return; // performance
                
                // set the origin JSP page
                JspServletDataObject.setSourceJspPage(servletFileObject, this);
                // now the loader should recognize that this servlet was generated from a JSP
                DataObject dObj= DataObject.find(servletFileObject);
                if (debug) {
                    System.out.println("checkRefr::servletDObj=" +  // NOI18N
                        ((dObj == null) ? "null" : dObj.getClass().getName()) + // NOI18N
                        "/" + dObj); // NOI18N
                }
                if (!(dObj instanceof JspServletDataObject)) {
                    // need to re-recognize
                    dObj = rerecognize(dObj);
                }
                if (dObj instanceof JspServletDataObject) {
                    servletDataObject = (JspServletDataObject)dObj;
                    servletDataObjectDate = dObj.getPrimaryFile().lastModified();
                }
                // set the encoding of the generated servlet
                String encoding = compileData.getServletEncoding();
                if (encoding != null) {
                    if (!"".equals(encoding)) {
                        try {
                            sun.io.CharToByteConverter.getConverter(encoding);
                        } catch (IOException ex) {
                            IOException t = new IOException(
                                NbBundle.getMessage(JspDataObject.class, "FMT_UnsupportedEncoding", encoding)
                            );
                            ErrorManager.getDefault().annotate(t, ex);
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
                        }
                    } else
                        encoding = null;
                }
                try {
                    // actually set the encoding
                    Util.setFileEncoding(servletFileObject, encoding);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            else
                servletDataObject = null;
        }
        catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            servletDataObject = null;
        }

        // editor
        if ((oldServlet == null)/*&&(servletDataObject != null)*/) {
        } else {
            RequestProcessor.postRequest(
                new Runnable() {
                    public void run() {
                        updateServletEditor();
                        // Bugfix 31143: oldValue must be null, since if oldValue == newValue, no change will be fired
                        JspDataObject.this.firePropertyChange0(PROP_SERVLET_DATAOBJECT, null, getServletDataObject());
                        // the state of some CookieActions may need to be updated
                        JspDataObject.this.firePropertyChange0(PROP_COOKIE, null, null);
                    }
                }
            );
        }
    }
    
    /** This method causes a DataObject to be re-recognized by the loader system.
    *  This is a poor practice and should not be normally used, as it uses reflection 
    *  to call a protected method DataObject.dispose().
    */
    private DataObject rerecognize(DataObject dObj) {
        // invalidate the object so it can be rerecognized
        FileObject prim = dObj.getPrimaryFile();
        try {
            dObj.setValid(false);
            return DataObject.find(prim);
        }
        catch (java.beans.PropertyVetoException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return dObj;
    }
    
    /** JDK 1.2 compiler hack. */
    public void firePropertyChange0(String propertyName, Object oldValue, Object newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
    }
    
    /** Returns an editor for the servlet. Architecturally, a better solution would be to attach a cookie for 
     * editing the servlet, but we choose this approach for performance reasons - this allows lazy initialization of
     * the editor (unlike the cookie). */
    public EditorCookie getServletEditor() {
        DataObject obj = getServletDataObject();
        if ((obj == null) != (servletEdit == null))
            updateServletEditor();
        return servletEdit;
    }
    
    private void updateServletEditor() {
        if (servletDataObject == null) {
            if (servletEdit != null) {
                servletEdit.close();
                servletEdit = null;
            }
        }
        else {
            if (servletEdit == null) {
                servletEdit = createServletEditor();
            }
        }
    }


    /** Gets the current fileobject of the servlet corresponding to this JSP or null if may not exist.
    * Note that the file still doesn't need to exist, even if it's not null. 
    * This does not need to be synchronized, because the calling method
    * getPlugin() is synchronized.
    */
    private FileObject updateServletFileObject() throws IOException {
        return compileData.getServletFileObject();
    }
    
    /* Determines whether the FileObject representing the generated servlet
     * needs to be refresh()-ed to synchronize itself with the underlying file.
     * @param servlet FileObject representing the generated servlet (may be null)
     * @return returns true if the servlet FileObject does not exist (is null),
     *  the underlying file does not exist (is null) or the last modified date 
     *  of the fileoblect differs from the last modif. date of the file.
     */
    private boolean needsRefresh(FileObject servlet) {
        if (servlet == null)
            return true;
        File servletFile = NbClassPath.toFile(servlet);
        if (servletFile == null)
            return true;
        if (debug) {
            System.out.println("needsR::file modified       :" + servletFile.lastModified()); // NOI18N
            System.out.println("needsR::fileobject modified :" + servlet.lastModified().getTime()); // NOI18N
        }
        if (servlet.lastModified().getTime() != servletFile.lastModified())
            return true;
        return false;
    }
    
    /////// -------- FIELDS AND METHODS FOR MANIPULATING THE PARSED INFORMATION -------- ////////
    
    /** Updates the information about statically included pages for these pages.
     * E.g. tells the included pages that they are included in this page. */
/*    private void updateIncludedPagesInfo(JspCompilationInfo compInfo) throws IOException {
        FileObject included[] = compInfo.getIncludedFileObjects();
        for (int i = 0; i < included.length; i++) {
            IncludedPagesSupport.setIncludedIn(getPrimaryFile(), included[i]);
        }
    }*/
    
    public void setQueryString (String params) throws java.io.IOException {
        WebExecSupport.setQueryString(getPrimaryEntry ().getFile (), params);
        firePropertyChange (ServletDataNode.PROP_REQUEST_PARAMS, null, null);
    }
    
    protected org.openide.filesystems.FileObject handleRename (String str) throws java.io.IOException {
        if ("".equals(str)) // NOI18N
            throw new IOException(NbBundle.getMessage(JspDataObject.class, "FMT_Not_Valid_FileName"));

        org.openide.filesystems.FileObject retValue;
        
        retValue = super.handleRename (str);
        return retValue;
    }
    
    public void addSaveCookie(SaveCookie cookie){
        getCookieSet().add(cookie);
    }

    public void removeSaveCookie(){
        Node.Cookie cookie = getCookie(SaveCookie.class);
        if (cookie!=null) getCookieSet().remove(cookie);
    }

    /* Creates new object from template. Inserts the correct ";charset=..." clause to the object
    * @exception IOException
    */
    protected DataObject handleCreateFromTemplate (
        DataFolder df, String name
    ) throws IOException {
        
        DataObject dobj = super.handleCreateFromTemplate(df, name);
        if (dobj instanceof JspDataObject) {
            JspDataObject jspDO = (JspDataObject)dobj;
            FileObject prim = jspDO.getPrimaryFile();
            String encoding = jspDO.getFileEncoding(prim);
            if (!"ISO-8859-1".equals(encoding)) {
                // write the encoding to file
                sun.io.CharToByteConverter.getConverter(encoding);
                Util.setFileEncoding(prim, encoding);
                
                // change in the file
                // warning: the following approach will only work if the page does not 
                // contain any strange characters. That's the case right after creation 
                // from template, so we are safe here
                EditorCookie edit = (EditorCookie)jspDO.getCookie(EditorCookie.class);
                if (edit != null) {
                    try {
                        StyledDocument doc = edit.openDocument();
                        int offset = findEncodingOffset(doc);
                        if (offset != -1) {
                            doc.insertString(offset, ";charset=" + encoding, null);
                            SaveCookie sc = (SaveCookie)jspDO.getCookie(SaveCookie.class);
                            if (sc != null) {
                                sc.save();
                            }
                        }
                    }
                    catch (BadLocationException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        }
        return dobj;
    }
    
    private int findEncodingOffset(StyledDocument doc) throws BadLocationException {
        String text = doc.getText(0, doc.getLength());
        String toFind = "contentType=\"";
        int index1 = text.indexOf(toFind);
        if (index1 == -1) return -1;
        int index2 = text.indexOf("\"", index1 + toFind.length());
        return index2;
    }

    ////// -------- INNER CLASSES ---------

    private class Listener extends FileChangeAdapter implements PropertyChangeListener, ServerRegistryImpl.ServerRegistryListener {
        
        Listener() {
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            // listening on properties which could affect the server plugin
            // saving the file
            if (PROP_MODIFIED.equals(evt.getPropertyName())) {
                if ((Boolean.FALSE).equals(evt.getNewValue())) {
                    refreshPlugin(false);
                }
            }
            // primary file changed or files changed
            if (PROP_PRIMARY_FILE.equals(evt.getPropertyName()) || 
                PROP_FILES.equals(evt.getPropertyName())) {
                if (evt.getOldValue() instanceof FileObject)
                    ((FileObject)evt.getOldValue()).removeFileChangeListener(this);
                if (evt.getNewValue() instanceof FileObject)
                    ((FileObject)evt.getNewValue()).addFileChangeListener(this);
                refreshPlugin(true);
            }
            // primary file changed or files changed
            if (WebContextObject.PROP_NEW_SERVER_INSTANCE.equals(evt.getPropertyName())) {
                serverChange();
            }
            // the context object has changed
            if (DataObject.PROP_VALID.equals(evt.getPropertyName())) {
                if (evt.getSource() instanceof DataObject) {
                    DataObject dobj = (DataObject)evt.getSource();
                    if (dobj.getPrimaryFile().getPackageNameExt('/','.').equals("")) { // NOI18N
                        dobj.removePropertyChangeListener(this);
                        ServerRegistryImpl.getRegistry().removeServerRegistryListener(this);
                        JspDataObject.this.addWebContextListener();
                    }
                }
            }

        }
        
        public void fileRenamed(FileRenameEvent fe) {
            refreshPlugin(true);
        }
        
        // implementation of ServerRegistryImpl.ServerRegistryListener
        public void added(ServerRegistryImpl.ServerEvent added) {
            serverChange();
        }

        public void setAppDefault(ServerRegistryImpl.InstanceEvent inst) {
            serverChange();
        }

        public void setWebDefault(ServerRegistryImpl.InstanceEvent inst) {
            serverChange();
        }

        public void removed(ServerRegistryImpl.ServerEvent removed) {
            serverChange();
        }
        
        private void serverChange() {
            refreshPlugin(true);
            firePropertyChange0(PROP_SERVER_CHANGE, null, null);
        }
        
    }
}

