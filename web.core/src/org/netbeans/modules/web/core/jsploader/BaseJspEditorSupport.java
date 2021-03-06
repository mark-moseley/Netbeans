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

package org.netbeans.modules.web.core.jsploader;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.EditorKit;
import org.netbeans.modules.web.core.palette.JSPPaletteFactory;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.openide.filesystems.FileUtil;

import org.openide.text.DataEditorSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.loaders.MultiDataObject;
import org.openide.cookies.*;
import org.openide.text.CloneableEditor;
import org.openide.util.Lookup;
import org.openide.util.TaskListener;
import org.openide.util.Task;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.CloneableOpenSupport;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
//import org.openide.debugger.Debugger;
//import org.openide.debugger.Breakpoint;

import org.openide.loaders.DataObject;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.palette.PaletteController;
import org.openide.util.UserCancelException;

class BaseJspEditorSupport extends DataEditorSupport implements EditCookie, EditorCookie.Observable, OpenCookie, LineCookie, CloseCookie, PrintCookie {
    
    private static final int AUTO_PARSING_DELAY = 2000;//ms
    
    /** Timer which countdowns the auto-reparsing time. */
    private Timer timer;
    
    /** Cash of encoding of the file */
    private String encoding;
    
    /** When unsupported encoding is set for a jsp file, then defaulEncoding is used for loading
     * and saving
     */
    private static String defaulEncoding = "UTF-8"; // NOI18N
    
    public BaseJspEditorSupport(JspDataObject obj) {
        super(obj, new BaseJspEnv(obj));
        DataObject data = getDataObject();
        if ((data!=null) && (data instanceof JspDataObject)) {
            setMIMEType(JspLoader.getMimeType((JspDataObject)data));
        }
        initialize();
    }
    
    public boolean close() {
        //cancel waiting parsing task if there is any
        //this is largely a workaround for issue #50926
        TagLibParseSupport sup = (TagLibParseSupport)getDataObject().getCookie(TagLibParseSupport.class);
        if(sup != null) {
            sup.cancelParsingTask();
        }
        
        return super.close();
    }
    
    private void initialize() {
        // initialize timer
        timer = new Timer(0, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                final TagLibParseSupport sup = (TagLibParseSupport)getDataObject().getCookie(TagLibParseSupport.class);
                if (sup != null && WebModule.getWebModule(getDataObject().getPrimaryFile())!= null) {
                    sup.autoParse().addTaskListener(new TaskListener() {
                        public void taskFinished(Task t) {
                            notifyParsingDone(sup);
                        }
                    });
                }
            }
        });
        timer.setInitialDelay(AUTO_PARSING_DELAY);
        timer.setRepeats(false);
        
        // create document listener
        final DocumentListener docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { change(e); }
            public void changedUpdate(DocumentEvent e) { }
            public void removeUpdate(DocumentEvent e) { change(e); }
            
            private void change(DocumentEvent e) {
                restartTimer(false);
                TagLibParseSupport sup = (TagLibParseSupport)getDataObject().getCookie(TagLibParseSupport.class);
                if (sup != null) {
                    sup.setDocumentDirty(true);
                }
            }
        };
        
        // add change listener
        addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                if (isDocumentLoaded()) {
                    if (getDocument() != null) {
                        getDocument().addDocumentListener(docListener);
                    }
                }
            }
        });
    
        encoding = null;
        
        JspParserAccess
                .getJspParserWM (getWebModule (getDataObject().getPrimaryFile()))
                .addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public void propertyChange(java.beans.PropertyChangeEvent evt) {
                            String propName = evt.getPropertyName();
                            if (JspParserAPI.WebModule.PROP_LIBRARIES.equals(propName) 
                                || JspParserAPI.WebModule.PROP_PACKAGE_ROOTS.equals(propName)) {
                                // the classpath was changed, need to reparsed
                                restartTimer(false);
                            }
                       }    
                    });
                    
        
    }
    
    private WebModule getWebModule(FileObject fo){
        WebModule wm = WebModule.getWebModule(fo);
        if (wm != null){
            FileObject wmRoot = wm.getDocumentBase();
            if (wmRoot != null && (fo == wmRoot || FileUtil.isParentOf(wmRoot, fo))) {
                return wm;
            }
        }
        return null;
    }
    
    /** Restart the timer which starts the parser after the specified delay.
     * @param onlyIfRunning Restarts the timer only if it is already running
     */
    private void restartTimer(boolean onlyIfRunning) {
        if (onlyIfRunning && !timer.isRunning()){
            return;
        }
            
        
        int delay = AUTO_PARSING_DELAY;
        if (delay > 0) {
            timer.setInitialDelay(delay);
            timer.restart();
        }
    }
    
    private boolean isSupportedEncoding(String encoding){
        boolean supported;
        try{
            supported = java.nio.charset.Charset.isSupported(encoding);
        }
        catch (java.nio.charset.IllegalCharsetNameException e){
            supported = false;
        }
        
        return supported;
    }

    @Override
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        ((JspDataObject)getDataObject()).updateFileEncoding(false);
        super.loadFromStreamToKit(doc, stream, kit);
    }
    
    public void open(){
        ((JspDataObject)getDataObject()).updateFileEncoding(false);
        encoding = ((JspDataObject)getDataObject()).getFileEncoding(); //use encoding from fileobject
        
        if (!isSupportedEncoding(encoding)) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage (BaseJspEditorSupport.class, "MSG_BadEncodingDuringLoad", //NOI18N
                    new Object [] { getDataObject().getPrimaryFile().getNameExt(),
                                    encoding, 
                                    defaulEncoding} ), 
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            if(nd.getValue() != NotifyDescriptor.YES_OPTION) return;
        }
        super.open();
        
        // #120530 - make sure parsing task is started
        restartTimer(false);
    }
    
//    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
//
//        Reader reader = null;
//        encoding = getObjectEncoding(false, true);//use encoding from fileobject & cache it
//        
//        if (!isSupportedEncoding(encoding)){
//            encoding = defaulEncoding;
//        }
//        try {
//            reader = new InputStreamReader(stream, encoding);
//            kit.read(reader, doc, 0);
//        }
//        finally {
//            if (reader != null)
//                reader.close();
//        }
//    }
    
    /** Notify about the editor closing.
     */
    protected void notifyClose() {}
    
    /** Notify that parsing task has been finished; some dependent data may now
     * be refreshed from up-to-date parsing info */
    protected void notifyParsingDone(TagLibParseSupport sup) {
        if (sup.isDocumentDirty()) {
            restartTimer(false);
        }
    }
    
    protected boolean notifyModified() {
        boolean notify = super.notifyModified();
        if (!notify) {
            return false;
        }
        JspDataObject obj = (JspDataObject)getDataObject();
        if (obj.getCookie(SaveCookie.class) == null) {
            obj.addSaveCookie(new SaveCookie() {
                public void save() throws java.io.IOException {
                    try {
                        saveDocument();
                    } catch(UserCancelException e) {
                        //just ignore
                    }
                }
            });
        }
        return true;
    }
    
    /** Called when the document becomes unmodified.
     * Here, removing the save cookie from the object and marking it unmodified.
     */
    protected void notifyUnmodified() {
        super.notifyUnmodified();
        JspDataObject obj = (JspDataObject)getDataObject();
        obj.removeSaveCookie();
    }
    
//    protected String getObjectEncoding(boolean useEditor) {
//        return getObjectEncoding(useEditor, false);
//    }
    
//    /** Returns encoding of the JSP file. 
//     * @param useEditor if <code>true</code> then the encoding is got from the editor 
//     *        otherwise the encoding is obtained from webmodule parser.
//     * @param useCache if <code>true</code> then the encoding parsed from the webmodule and JSP is 
//     *        cached. So the next call of this method wont parse the wm and the JSP again until the JSP file is changed.
//     * @return JSP page encoding.
//     */
//    protected String getObjectEncoding(boolean useEditor, boolean useCache) {
//            return ((JspDataObject)getDataObject()).getFileEncoding(!useCache, useEditor).trim();
//    }
    
    /** Save the document in this thread and start reparsing it.
     * @exception IOException on I/O error
     */
    public void saveDocument() throws IOException {
        saveDocument(true, true);
    }
    
    /** Save the document in this thread.
     * @param parse true if the parser should be started, otherwise false
     * @exception IOException on I/O error
     */
    protected void saveDocumentIfNecessary(boolean parse) throws IOException {
        saveDocument(parse, false);
    }
    
    /** Save the document in this thread.
     * @param parse true if the parser should be started, otherwise false
     * @param forceSave if true save always, otherwise only when is modified
     * @exception IOException on I/O error
     */
    private void saveDocument(boolean parse, boolean forceSave) throws IOException {
        if (forceSave || isModified()) {
            ((JspDataObject)getDataObject()).updateFileEncoding(true);
            
            encoding = ((JspDataObject)getDataObject()).getFileEncoding();
//            encoding = getObjectEncoding(true); //use encoding from editor 
            if (!isSupportedEncoding(encoding)){
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage (BaseJspEditorSupport.class, "MSG_BadEncodingDuringSave", //NOI18N
                    new Object [] { getDataObject().getPrimaryFile().getNameExt(),
                                    encoding, 
                                    defaulEncoding} ), 
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.WARNING_MESSAGE);
                nd.setValue(NotifyDescriptor.NO_OPTION);       
                DialogDisplayer.getDefault().notify(nd);
                if(nd.getValue() != NotifyDescriptor.YES_OPTION) {
                    throw new UserCancelException();
                }
            }
            else {
                try {
                    java.nio.charset.CharsetEncoder coder = java.nio.charset.Charset.forName(encoding).newEncoder();
                    if (!coder.canEncode(getDocument().getText(0, getDocument().getLength()))){
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage (BaseJspEditorSupport.class, "MSG_BadCharConversion", //NOI18N
                        new Object [] { getDataObject().getPrimaryFile().getNameExt(),
                                        encoding}),
                            NotifyDescriptor.YES_NO_OPTION,
                            NotifyDescriptor.WARNING_MESSAGE);
                            nd.setValue(NotifyDescriptor.NO_OPTION);
                            DialogDisplayer.getDefault().notify(nd);
                            if(nd.getValue() != NotifyDescriptor.YES_OPTION) {
                                throw new UserCancelException();
                            }
                    }
                }
                catch (javax.swing.text.BadLocationException e){
                    Logger.getLogger("global").log(Level.INFO, null, e);            
                }
            }
            super.saveDocument();
            if (parse) {
                TagLibParseSupport sup = (TagLibParseSupport)getDataObject().getCookie(TagLibParseSupport.class);
                if (sup != null) {
                    sup.prepare();
                }
            }
        }
    }
    
    /** A method to create a new component. Overridden in subclasses.
     * @return the {@link BaseJspEditor} for this support
     */
    protected CloneableEditor createCloneableEditor() {
        return new BaseJspEditor(this);
    }
    
    public static class BaseJspEnv extends DataEditorSupport.Env {
        
        private static final long serialVersionUID = -800036748848958489L;
        
        public BaseJspEnv(JspDataObject obj) {
            super(obj);
        }
        
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }
        
        protected FileLock takeLock() throws IOException {
            return ((MultiDataObject)getDataObject()).getPrimaryEntry().takeLock();
        }
        
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (BaseJspEditorSupport)getDataObject().getCookie(BaseJspEditorSupport.class);
        }
    }
    
    public static class BaseJspEditor extends CloneableEditor {
        
        public static final String JSP_MIME_TYPE = "text/x-jsp"; // NOI18N
        public static final String TAG_MIME_TYPE = "text/x-tag"; // NOI18N

        private TagLibParseSupport taglibParseSupport;
        private InstanceContent instanceContent;
        
        /** Listener on caret movements */
        CaretListener caretListener;
        //BaseJspEditorSupport support;
        
        public BaseJspEditor() {
            super();
        }
        
        public boolean isXmlSyntax(DataObject dataObject) {
            
            FileObject fileObject = (dataObject != null) ? dataObject.getPrimaryFile() : null;
            if (fileObject == null)
                return false;
            
            return taglibParseSupport.getCachedOpenInfo(false, false).isXmlSyntax();
        }
        
        void associatePalette(BaseJspEditorSupport s) {
        
            DataObject dataObject = s.getDataObject();
            String mimeType = dataObject.getPrimaryFile().getMIMEType();
            instanceContent.add(getActionMap());
            
            if (dataObject instanceof JspDataObject && 
               (mimeType.equals(JSP_MIME_TYPE) || mimeType.equals(TAG_MIME_TYPE)) && 
                !isXmlSyntax(dataObject)) 
            {
                try {
                    PaletteController pc = JSPPaletteFactory.getPalette();
                    instanceContent.add(pc);
                } 
                catch (IOException ioe) {
                    //TODO exception handling
                    ioe.printStackTrace();
                }
            }
        }
        
        /** Creates new editor */
        public BaseJspEditor(BaseJspEditorSupport s) {
            super(s);
            initialize();
        }

        protected void notifyParsingDone() {
        }
        
        private void initialize() {
            Node nodes[] = {((DataEditorSupport)cloneableEditorSupport()).getDataObject().getNodeDelegate()};

            //init lookup
            instanceContent = new InstanceContent();
            associateLookup(new ProxyLookup(new Lookup[] { new AbstractLookup(instanceContent), nodes[0].getLookup()}));
            
            setActivatedNodes(nodes);
            caretListener = new CaretListener() {
                public void caretUpdate(CaretEvent e) {
                    ((BaseJspEditorSupport)cloneableEditorSupport()).restartTimer(true);
                }
            };
            
            taglibParseSupport = (TagLibParseSupport)((BaseJspEditorSupport)cloneableEditorSupport()).getDataObject().getCookie(TagLibParseSupport.class);

        }
        
        /* This method is called when parent window of this component has focus,
         * and this component is preferred one in it.
         */
        protected void componentActivated() {
            // Workaround for bug #37188. If the pane is null, don't activate the component.
            if (getEditorPane() != null){
                getEditorPane().addCaretListener(caretListener);
                super.componentActivated();
            }
            ((BaseJspEditorSupport)cloneableEditorSupport()).restartTimer(false);
            
            //allow resumed parser to perform parsing of the webproject
            taglibParseSupport.setEditorOpened(true);
            //show up the component palette
            associatePalette((BaseJspEditorSupport)cloneableEditorSupport());
            
        }
        
        /*
         * This method is called when parent window of this component losts focus,
         * or when this component losts preferrence in the parent window.
         */
        protected void componentDeactivated() {
             if (getEditorPane() != null){
                getEditorPane().removeCaretListener(caretListener);
                super.componentDeactivated();
            }
            taglibParseSupport.setEditorOpened(false);
        }
        
        /* When closing last view, also close the document.
         * @return <code>true</code> if close succeeded
         */
        protected boolean closeLast() {
            if (!super.closeLast()) return false;
            ((BaseJspEditorSupport)cloneableEditorSupport()).notifyClose();
            return true;
        }
        
        /** Deserialize this top component.
         * @param in the stream to deserialize from
         */
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            initialize();
            associatePalette((BaseJspEditorSupport)cloneableEditorSupport());
        }
        
    } // end of JavaEditorComponent inner class
    
}
