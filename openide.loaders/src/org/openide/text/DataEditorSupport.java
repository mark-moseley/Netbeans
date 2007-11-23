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

package org.openide.text;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.Reference;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.openide.loaders.DataObjectAccessor;
import org.netbeans.modules.openide.loaders.UIException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.OpenSupport;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.CloneableOpenSupport;

/**
 * Support for associating an editor and a Swing {@link Document} to a data object.
 * @author Jaroslav Tulach
 */
public class DataEditorSupport extends CloneableEditorSupport {
    /** error manager for CloneableEditorSupport logging and error reporting */
    static final Logger ERR = Logger.getLogger("org.openide.text.DataEditorSupport"); // NOI18N

    /** Which data object we are associated with */
    private final DataObject obj;
    /** listener to asociated node's events */
    private NodeListener nodeL;
    
    /** Editor support for a given data object. The file is taken from the
    * data object and is updated if the object moves or renames itself.
    * @param obj object to work with
    * @param env environment to pass to 
    */
    public DataEditorSupport (DataObject obj, CloneableEditorSupport.Env env) {
        super (env, new DOEnvLookup (obj));
        this.obj = obj;
    }
    
    /** Getter for the environment that was provided in the constructor.
    * @return the environment
    */
    final CloneableEditorSupport.Env desEnv() {
        return (CloneableEditorSupport.Env) env;
    }
    
    /** Factory method to create simple CloneableEditorSupport for a given
     * entry of a given DataObject. The common use inside DataObject looks like
     * this:
     * <pre>
     *  getCookieSet().add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), getCookieSet()));
     * </pre>
     *
     * @param obj the data object
     * @param entry the entry to read and write from
     * @param set cookie set to add remove additional cookies (currently only {@link org.openide.cookies.SaveCookie})
     * @return a subclass of DataEditorSupport that implements at least
     *   {@link org.openide.cookies.OpenCookie}, 
     *   {@link org.openide.cookies.EditCookie}, 
     *   {@link org.openide.cookies.EditorCookie.Observable}, 
     *   {@link org.openide.cookies.PrintCookie}, 
     *   {@link org.openide.cookies.CloseCookie}
     * @since 5.2
     */
    public static CloneableEditorSupport create (DataObject obj, MultiDataObject.Entry entry, org.openide.nodes.CookieSet set) {
        return new SimpleES (obj, entry, set);
    }
    
    /** Getter of the data object that this support is associated with.
    * @return data object passed in constructor
    */
    public final DataObject getDataObject () {
        return obj;
    }

    /** Message to display when an object is being opened.
    * @return the message or null if nothing should be displayed
    */
    protected String messageOpening () {
        return NbBundle.getMessage (DataObject.class , "CTL_ObjectOpen", // NOI18N
            obj.getPrimaryFile().getNameExt(),
            FileUtil.getFileDisplayName(obj.getPrimaryFile())
        );
    }
    

    /** Message to display when an object has been opened.
    * @return the message or null if nothing should be displayed
    */
    protected String messageOpened () {
        return null;
    }

    /** Constructs message that should be displayed when the data object
    * is modified and is being closed.
    *
    * @return text to show to the user
    */
    protected String messageSave () {
        return NbBundle.getMessage (
            DataObject.class,
            "MSG_SaveFile", // NOI18N
            obj.getPrimaryFile().getNameExt()
        );
    }
    
    /** Constructs message that should be used to name the editor component.
    *
    * @return name of the editor
    */
    protected String messageName () {
        if (! obj.isValid()) {
            return ""; // NOI18N
        }

        return addFlagsToName(obj.getNodeDelegate().getDisplayName());
    }
    
    @Override
    protected String messageHtmlName() {
        if (! obj.isValid()) {
            return null;
        }

        String name = obj.getNodeDelegate().getHtmlDisplayName();
        if (name != null) {
            if (!name.startsWith("<html>")) {
                name = "<html>" + name;
            }
            name = addFlagsToName(name);
        }
        return name;
    }
        
    /** Helper only. */
    private String addFlagsToName(String name) {
        int version = 3;
        if (isModified ()) {
            if (!obj.getPrimaryFile().canWrite()) {
                version = 2;
            } else {
                version = 1;
            }
        } else {
            if (!obj.getPrimaryFile().canWrite()) {
                version = 0;
            }
        }

        return NbBundle.getMessage (DataObject.class, "LAB_EditorName",
		new Integer (version), name );
    }
    
    @Override
    protected String documentID() {
        if (! obj.isValid()) {
            return ""; // NOI18N
        }
        return obj.getPrimaryFile().getName();
    }

    /** Text to use as tooltip for component.
    *
    * @return text to show to the user
    */
    @Override
    protected String messageToolTip () {
        // update tooltip
        return FileUtil.getFileDisplayName(obj.getPrimaryFile());
    }
    
    /** Computes display name for a line based on the 
     * name of the associated DataObject and the line number.
     *
     * @param line the line object to compute display name for
     * @return display name for the line like "MyFile.java:243"
     *
     * @since 4.3
     */
    @Override
    protected String messageLine (Line line) {
        return NbBundle.getMessage(DataObject.class, "FMT_LineDisplayName2",
            obj.getPrimaryFile().getNameExt(),
            FileUtil.getFileDisplayName(obj.getPrimaryFile()),
            new Integer(line.getLineNumber() + 1));
    }
    
    
    /** Annotates the editor with icon from the data object and also sets 
     * appropriate selected node. But only in the case the data object is valid.
     * This implementation also listen to display name and icon chamges of the
     * node and keeps editor top component up-to-date. If you override this
     * method and not call super, please note that you will have to keep things
     * synchronized yourself. 
     *
     * @param editor the editor that has been created and should be annotated
     */
    @Override
    protected void initializeCloneableEditor (CloneableEditor editor) {
        // Prevention to bug similar to #17134. Don't call getNodeDelegate
        // on invalid data object. Top component should be discarded later.
        if(obj.isValid()) {
            Node ourNode = obj.getNodeDelegate();
            editor.setActivatedNodes (new Node[] { ourNode });
            editor.setIcon(ourNode.getIcon (java.beans.BeanInfo.ICON_COLOR_16x16));
            NodeListener nl = new DataNodeListener(editor);
            ourNode.addNodeListener(org.openide.nodes.NodeOp.weakNodeListener (nl, ourNode));
            nodeL = nl;
        }
    }

    /** Called when closed all components. Overrides superclass method,
     * also unregisters listening on node delegate. */
    @Override
    protected void notifyClosed() {
        // #27645 All components were closed, unregister weak listener on node.
        nodeL = null;
        
        super.notifyClosed();
    }
    
    /** Let's the super method create the document and also annotates it
    * with Title and StreamDescription properities.
    *
    * @param kit kit to user to create the document
    * @return the document annotated by the properties
    */
    @Override
    protected StyledDocument createStyledDocument (EditorKit kit) {
        StyledDocument doc = super.createStyledDocument (kit);
            
        // set document name property
        doc.putProperty(javax.swing.text.Document.TitleProperty,
            FileUtil.getFileDisplayName(obj.getPrimaryFile())
        );
        // set dataobject to stream desc property
        doc.putProperty(javax.swing.text.Document.StreamDescriptionProperty,
            obj
        );
        
        //Report the document into the Timers&Counters window:
        Logger.getLogger("TIMER").log(Level.FINE, "Document", new Object[] {obj.getPrimaryFile(), doc});
        
        return doc;
    }

    /** Checks whether is possible to close support components.
     * Overrides superclass method, adds checking
     * for read-only property of saving file and warns user in that case. */
    @Override
    protected boolean canClose() {
        if(desEnv().isModified() && isEnvReadOnly()) {
            Object result = DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(DataObject.class,
                        "MSG_FileReadOnlyClosing", 
                        new Object[] {((Env)env).getFileImpl().getNameExt()}),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE
            ));

            return result == NotifyDescriptor.OK_OPTION;
        }
        
        return super.canClose();
    }
    
    /**
     * @inheritDoc
     */
    @Override
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        Charset c = this.getDataObject() == charsetForObject ? charsetForSaveAndLoad : null;
        if (c == null) {
            c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
        }
        final Reader r = new InputStreamReader (stream, c);
        kit.read(r, doc, 0);
    }

    /** can hold the right charset to be used during save, needed for communication
     * between saveFromKitToStream and saveDocument
     */
    private static Charset charsetForSaveAndLoad;
    private static DataObject charsetForObject;
    /**
     * @inheritDoc
     */
    @Override
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        if (doc == null) {
            throw new NullPointerException("Document is null"); // NOI18N
        }
        if (kit == null) {
            throw new NullPointerException("Kit is null"); // NOI18N
        }
        
        Charset c = this.getDataObject() == charsetForObject ? charsetForSaveAndLoad : null;
        if (c == null) {
            c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
        }
        FilterOutputStream fos = new FilterOutputStream(stream) {
            @Override
            public void close() throws IOException {
                flush();
            }
        };
        Writer w = new OutputStreamWriter (fos, c);
        try {
            kit.write(w, doc, 0, doc.getLength());
        } finally {
            w.close();
        }
    }

    @Override
    public StyledDocument openDocument() throws IOException {
        Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
        try {
            charsetForSaveAndLoad = c;
            charsetForObject = getDataObject();
            return super.openDocument();
        } finally {
            charsetForSaveAndLoad = null;
            charsetForObject = null;
        }
    }

    /** Saves document. Overrides superclass method, adds checking
     * for read-only property of saving file and warns user in that case. */
    @Override
    public void saveDocument() throws IOException {
        if(desEnv().isModified() && isEnvReadOnly()) {
            IOException e = new IOException("File is read-only: " + ((Env)env).getFileImpl()); // NOI18N
            UIException.annotateUser(e, null,
                                     org.openide.util.NbBundle.getMessage(org.openide.loaders.DataObject.class,
                                                                          "MSG_FileReadOnlySaving",
                                                                          new java.lang.Object[]{((org.openide.text.DataEditorSupport.Env) env).getFileImpl().getNameExt()}),
                                     null, null);
            throw e;
        }
        
        Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
        try {
            charsetForSaveAndLoad = c;
            charsetForObject = getDataObject();
            super.saveDocument();
        } finally {
            charsetForSaveAndLoad = null;
            charsetForObject = null;
        }
    }

    /** Indicates whether the <code>Env</code> is read only. */
    @Override
    boolean isEnvReadOnly() {
        CloneableEditorSupport.Env myEnv = desEnv();
        return myEnv instanceof Env && !((Env) myEnv).getFileImpl().canWrite();
    }
    
    /** Needed for EditorSupport */
    final DataObject getDataObjectHack2 () {
        return obj;
    }
    
    /** Accessor for updateTitles.
     */
    final void callUpdateTitles () {
        updateTitles ();
    }
    
    /** Support method that extracts a DataObject from a Line. If the 
     * line is created by a DataEditorSupport then associated DataObject
     * can be accessed by this method.
     *
     * @param l line object 
     * @return data object or null
     *
     * @since 4.3
     */
    public static DataObject findDataObject (Line l) {
        if (l == null) {
            throw new NullPointerException();
        }
        return l.getLookup().lookup(DataObject.class);
    }
    
    /**
     * Save the document under a new file name and/or extension.
     * @param folder New folder to save the DataObject to.
     * @param fileName New file name to save the DataObject to.
     * @throws java.io.IOException If the operation failed
     * @since 6.3
     */
    public void saveAs( FileObject folder, String fileName ) throws IOException {
        if( env instanceof Env ) {
            
            //ask the user for a new file name to save to
            String newExtension = FileUtil.getExtension( fileName );
            
            DataObject newDob = null;
            DataObject currentDob = getDataObject();
            if( !currentDob.isModified() || null == getDocument() ) {
                //the document is not modified on disk, we copy/rename the file
                DataFolder df = DataFolder.findFolder( folder );
                
                FileObject newFile = folder.getFileObject(fileName);
                if( null != newFile ) {
                    //remove the target file if it already exists
                    newFile.delete();
                }
                
                newDob = DataObjectAccessor.DEFAULT.copyRename( currentDob, df, getFileNameNoExtension(fileName), newExtension );
            } else {
                //the document is modified in editor, we need to save the editor kit instead
                FileObject newFile = FileUtil.createData( folder, fileName );
                saveDocumentAs( newFile.getOutputStream() );
                currentDob.setModified( false );
                newDob = DataObject.find( newFile );
            }
            
            if( null != newDob ) {
                //TODO open the document at the position of the original document when #94607 is implemented
                OpenCookie c = newDob.getCookie( OpenCookie.class );
                if( null != c ) {
                    //close the original document
                    close( false );
                    //open the new one
                    c.open();
                }
            }
        }
    }
    
    private String getFileNameNoExtension(String fileName) {
        int index = fileName.lastIndexOf("."); // NOI18N

        if (index == -1) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }

    
    /** 
     * Save the document to a new file.
     * @param output 
     * @exception IOException on I/O error
     * @since 6.3
     */
    private void saveDocumentAs( final OutputStream output ) throws IOException {

        final StyledDocument myDoc = getDocument();
        
        // save the document as a reader
        class SaveAsWriter implements Runnable {
            private IOException ex;

            public void run() {
                try {
                    OutputStream os = null;

                    try {
                        os = new BufferedOutputStream( output );
                        saveFromKitToStream( myDoc, os );

                        os.close(); // performs firing
                        os = null;

                    } catch( BadLocationException blex ) {
                        ERR.log( Level.INFO, null, blex );
                    } finally {
                        if (os != null) { // try to close if not yet done
                            os.close();
                        }
                    }
                } catch (IOException e) {
                    this.ex = e;
                }
            }

            public void after() throws IOException {
                if (ex != null) {
                    throw ex;
                }
            }
        }

        SaveAsWriter saveAsWriter = new SaveAsWriter();
        myDoc.render(saveAsWriter);
        saveAsWriter.after();
    }
    
    /**
     * Save the document to given stream
     * @param myDoc
     * @param os
     * @throws IOException
     * @throws BadLocationException
     * @since 6.3
     */
    private void saveFromKitToStream( StyledDocument myDoc, OutputStream os ) throws IOException, BadLocationException {
        // Note: there's no new kit getting created, the method actually caches
        // previously created kit and has just a funny name
        final EditorKit kit = createEditorKit();
        
        saveFromKitToStream( myDoc, kit, os );
    }

    /** Environment that connects the data object and the CloneableEditorSupport.
    */
    public static abstract class Env extends OpenSupport.Env implements CloneableEditorSupport.Env {
        /** generated Serialized Version UID */
        private static final long serialVersionUID = -2945098431098324441L;

        /** The file object this environment is associated to.
        * This file object can be changed by a call to refresh file.
        */
        private transient FileObject fileObject;

        /** Lock acquired after the first modification and used in save.
        * Transient => is not serialized.
        * Not private for tests.
        */
        transient FileLock fileLock;
        /** did we warned about the size of the file?
         */
        private transient boolean warned;

        /** Constructor.
        * @param obj this support should be associated with
        */
        public Env (DataObject obj) {
            super (obj);
        }
        
        /** Getter for the file to work on.
        * @return the file
        */
        private FileObject getFileImpl () {
            // updates the file if there was a change
	    changeFile();
            return fileObject;
        }
        
        /** Getter for file associated with this environment.
        * @return the file input/output operation should be performed on
        */
        protected abstract FileObject getFile ();

        /** Locks the file.
        * @return the lock on the file getFile ()
        * @exception IOException if the file cannot be locked
        */
        protected abstract FileLock takeLock () throws IOException;
                
        /** Method that allows subclasses to notify this environment that
        * the file associated with this support has changed and that 
        * the environment should listen on modifications of different 
        * file object.
        */
        protected final void changeFile () {

            FileObject newFile = getFile ();
            
            if (newFile.equals (fileObject)) {
                // the file has not been updated
                return;
            }
            
            boolean lockAgain;
            if (fileLock != null) {
// <> NB #61818 In case the lock was not active (isValid() == false), the new lock was taken,
// which seems to be incorrect. There is taken a lock on new file, while it there wasn't on the old one.
//                fileLock.releaseLock ();
//                lockAgain = true;
// =====
                if(fileLock.isValid()) {
                    ERR.fine("changeFile releaseLock: " + fileLock + " for " + fileObject); // NOI18N
                    fileLock.releaseLock ();
                    lockAgain = true;
                } else {
                    fileLock = null;
                    lockAgain = false;
                }
// </>
            } else {
                lockAgain = false;
            }

            fileObject = newFile;
            ERR.fine("changeFile: " + newFile + " for " + fileObject); // NOI18N
            fileObject.addFileChangeListener (new EnvListener (this));

            if (lockAgain) { // refresh lock
                try {
                    fileLock = takeLock ();
                    ERR.fine("changeFile takeLock: " + fileLock + " for " + fileObject); // NOI18N
                } catch (IOException e) {
                    Logger.getLogger(DataEditorSupport.class.getName()).log(Level.WARNING, null, e);
                }
            }
            
        }
        
        
        /** Obtains the input stream.
        * @exception IOException if an I/O error occures
        */
        public InputStream inputStream() throws IOException {
            final FileObject fo = getFileImpl ();
            if (!warned && fo.getSize () > 1024 * 1024) {
                class ME extends org.openide.util.UserQuestionException {
                    static final long serialVersionUID = 1L;
                    
                    private long size;
                    
                    public ME (long size) {
                        super ("The file is too big. " + size + " bytes.");
                        this.size = size;
                    }
                    
                    @Override
                    public String getLocalizedMessage () {
                        Object[] arr = {
                            fo.getPath (),
                            fo.getNameExt (),
                            new Long (size), // bytes
                            new Long (size / 1024 + 1), // kilobytes
                            new Long (size / (1024 * 1024)), // megabytes
                            new Long (size / (1024 * 1024 * 1024)), // gigabytes
                        };
                        return NbBundle.getMessage(DataObject.class, "MSG_ObjectIsTooBig", arr);
                    }
                    
                    public void confirmed () {
                        warned = true;
                    }
                }
                throw new ME (fo.getSize ());
            }
            InputStream is = getFileImpl ().getInputStream ();
            return is;
        }
        
        /** Obtains the output stream.
        * @exception IOException if an I/O error occures
        */
        public OutputStream outputStream() throws IOException {
            ERR.fine("outputStream: " + fileLock + " for " + fileObject); // NOI18N
            if (fileLock == null || !fileLock.isValid()) {
                fileLock = takeLock ();
            }
            ERR.fine("outputStream after takeLock: " + fileLock + " for " + fileObject); // NOI18N
            try {
                return getFileImpl ().getOutputStream (fileLock);
            } catch (IOException fse) {
	        // [pnejedly] just retry once.
		// Ugly workaround for #40552
                if (fileLock == null || !fileLock.isValid()) {
                    fileLock = takeLock ();
                }
                ERR.fine("ugly workaround for #40552: " + fileLock + " for " + fileObject); // NOI18N
                return getFileImpl ().getOutputStream (fileLock);
            }	    
        }
        
        /** The time when the data has been modified
        */
        public Date getTime() {
            // #32777 - refresh file object and return always the actual time
            getFileImpl().refresh(false);
            return getFileImpl ().lastModified ();
        }
        
        /** Mime type of the document.
        * @return the mime type to use for the document
        */
        public String getMimeType() {
            return getFileImpl ().getMIMEType ();
        }
        
        /** First of all tries to lock the primary file and
        * if it succeeds it marks the data object modified.
         * <p><b>Note: There is a contract (better saying a curse)
         * that this method has to call {@link #takeLock} method
         * in order to keep working some special filesystem's feature.
         * See <a href="http://www.netbeans.org/issues/show_bug.cgi?id=28212">issue #28212</a></b>.
        *
        * @exception IOException if the environment cannot be marked modified
        *   (for example when the file is readonly), when such exception
        *   is the support should discard all previous changes
         * @see  org.openide.filesystems.FileObject#isReadOnly
        */
        @Override
        public void markModified() throws java.io.IOException {
            // XXX This shouldn't be here. But it is due to the 'contract',
            // see javadoc to this method.
            if (fileLock == null || !fileLock.isValid()) {
                fileLock = takeLock ();
            }
            ERR.fine("markModified: " + fileLock + " for " + fileObject); // NOI18N
            
            if (!getFileImpl().canWrite()) {
                if(fileLock != null && fileLock.isValid()) {
                    fileLock.releaseLock();
                }
                throw new IOException("File " // NOI18N
                    + getFileImpl().getNameExt() + " is read-only!"); // NOI18N
            }

            this.getDataObject ().setModified (true);
        }
        
        /** Reverse method that can be called to make the environment 
        * unmodified.
        */
        @Override
        public void unmarkModified() {
            ERR.fine("unmarkModified: " + fileLock + " for " + fileObject); // NOI18N
            if (fileLock != null && fileLock.isValid()) {
                fileLock.releaseLock();
                ERR.fine("releaseLock: " + fileLock + " for " + fileObject); // NOI18N
            }
            
            this.getDataObject ().setModified (false);
        }
        
        /** Called from the EnvListener
        * @param expected is the change expected
        * @param time of the change
        */
        final void fileChanged (boolean expected, long time) {
            ERR.fine("fileChanged: " + expected + " for " + fileObject); // NOI18N
            if (expected) {
                // newValue = null means do not ask user whether to reload
                firePropertyChange (PROP_TIME, null, null);
            } else {
                firePropertyChange (PROP_TIME, null, new Date (time));
            }
        }

        /** Called from the <code>EnvListener</code>.
         * The components are going to be closed anyway and in case of
         * modified document its asked before if to save the change. */
        final void fileRemoved(boolean canBeVetoed) {
            /* JST: Do not do anything here, as there will be new call from
               the DataObject.markInvalid0
             
            if (canBeVetoed) {
                try {
                    // Causes the 'Save' dialog to show if necessary.
                    fireVetoableChange(Env.PROP_VALID, Boolean.TRUE, Boolean.FALSE);
                } catch(PropertyVetoException pve) {
                    // ok vetoed, keep the window open, but continue to veto for ever
                    // any subsequent veto messages from the data object
                }
            }
            
            // Closes the components.
            firePropertyChange(Env.PROP_VALID, Boolean.TRUE, Boolean.FALSE);            
             */
        }
        
        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            CloneableOpenSupport cos = super.findCloneableOpenSupport ();
            if (cos instanceof DataEditorSupport) {
                Object o = ((DataEditorSupport)cos).env;
                if (o != this && o instanceof Env) {
                   ((Env)o).warned = this.warned;
                }
            }
            return cos;
        }
        
        private void readObject (ObjectInputStream ois) throws ClassNotFoundException, IOException {
            ois.defaultReadObject ();
            warned = true;
        }
        
        private class SaveAsCapableImpl implements SaveAsCapable {
            public void saveAs(FileObject folder, String fileName) throws IOException {
                CloneableOpenSupport cos = Env.super.findCloneableOpenSupport();
                if (cos instanceof DataEditorSupport) {
                    ((DataEditorSupport)cos).saveAs( folder, fileName );
                }
            }
        }
    } // end of Env
    
    /** Listener on file object that notifies the Env object
    * that a file has been modified.
    */
    private static final class EnvListener extends FileChangeAdapter {
        /** Reference (Env) */
        private Reference<Env> env;
        
        /** @param env environement to use
        */
        public EnvListener (Env env) {
            this.env = new java.lang.ref.WeakReference<Env> (env);
        }


        /** Handles <code>FileObject</code> deletion event. */
        @Override
        public void fileDeleted(FileEvent fe) {
            Env myEnv = this.env.get();
            FileObject fo = fe.getFile();
            if(myEnv == null || myEnv.getFileImpl() != fo) {
                // the Env change its file and we are not used
                // listener anymore => remove itself from the list of listeners
                fo.removeFileChangeListener(this);
                return;
            }
            
            fo.removeFileChangeListener(this);
            
            myEnv.fileRemoved(true);
            fo.addFileChangeListener(this);
        }
        
        /** Fired when a file is changed.
        * @param fe the event describing context where action has taken place
        */
        @Override
        public void fileChanged(FileEvent fe) {
            Env myEnv = this.env.get ();
            if (myEnv == null || myEnv.getFileImpl () != fe.getFile ()) {
                // the Env change its file and we are not used
                // listener anymore => remove itself from the list of listeners
                fe.getFile ().removeFileChangeListener (this);
                return;
            }

            // #16403. Added handling for virtual property of the file.
            if(fe.getFile().isVirtual()) {
                // Remove file event coming as consequence of this change.
                fe.getFile().removeFileChangeListener(this);
                // File doesn't exist on disk -> simulate env is invalid,
                // even the fileObject could be valid, see VCS FS.
                myEnv.fileRemoved(true);
                fe.getFile().addFileChangeListener(this);
            } else {
                myEnv.fileChanged (fe.isExpected (), fe.getTime ());
            }
        }
                
    }
    
    /** Listener on node representing asociated data object, listens to the
     * property changes of the node and updates state properly
     */
    private final class DataNodeListener extends NodeAdapter {
        /** Asociated editor */
        CloneableEditor editor;
        
        DataNodeListener (CloneableEditor editor) {
            this.editor = editor;
        }
        
        @Override
        public void propertyChange(final PropertyChangeEvent ev) {
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
            if (Node.PROP_DISPLAY_NAME.equals(ev.getPropertyName())) {
                callUpdateTitles();
            }
            if (Node.PROP_ICON.equals(ev.getPropertyName())) {
                if (obj.isValid()) {
                    editor.setIcon(obj.getNodeDelegate().getIcon (java.beans.BeanInfo.ICON_COLOR_16x16));
                }
            }
                }
            });
        }
        
    } // end of DataNodeListener

    /** Lookup that holds DataObject, its primary file and updates if that
     * changes.
     */
    private static class DOEnvLookup extends AbstractLookup 
    implements PropertyChangeListener {
        static final long serialVersionUID = 333L;
        
        private DataObject dobj;
        private InstanceContent ic;
        
        public DOEnvLookup (DataObject dobj) {
            this (dobj, new InstanceContent ());
        }
        
        private DOEnvLookup (DataObject dobj, InstanceContent ic) {
            super (ic);
            this.ic = ic;
            this.dobj = dobj;
        	dobj.addPropertyChangeListener(WeakListeners.propertyChange(this, dobj));
     
            updateLookup ();
        }
        
        private void updateLookup() {
            ic.set(Arrays.asList(new Object[] { dobj, dobj.getPrimaryFile() }), null);
        }
        
        public void propertyChange(PropertyChangeEvent ev) {
            String propName = ev.getPropertyName();
            if (propName == null || propName.equals(DataObject.PROP_PRIMARY_FILE)) {
                updateLookup();
            }
        }
    }
    
}
