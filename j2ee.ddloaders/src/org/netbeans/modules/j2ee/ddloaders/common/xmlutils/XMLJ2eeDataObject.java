/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.common.xmlutils;

import org.openide.cookies.*;
import org.openide.nodes.CookieSet;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.XMLDataObject;
import org.openide.ErrorManager;
import org.openide.text.Line;
import org.openide.windows.*;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

import java.util.ResourceBundle;
import java.text.MessageFormat;
import java.io.*;
import org.xml.sax.*;
import org.openide.xml.*;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.spi.xml.cookies.*;
import org.openide.windows.IOProvider;

/** Represents a XMLJ2eeDataObject in the Repository.
 *
 * @author  mkuchtiak
 */
public abstract class XMLJ2eeDataObject extends XMLDataObject implements CookieSet.Factory {

    protected boolean nodeDirty = false;
    private boolean documentDirty = true;
    private boolean savingDocument;
    private InputStream inputStream;
    private InputOutput inOut;
    protected XMLJ2eeEditorSupport editor;
    private boolean documentValid=true;
    private SAXParseError error;
    private org.openide.text.Annotation errorAnnotation;

    private static final long serialVersionUID = -515751072013886985L;   
    
    /** Property name for property documentValid */
    public static final String PROP_DOC_VALID = "documentValid"; // NOI18N
    
    public XMLJ2eeDataObject(FileObject pf, MultiFileLoader loader)
        throws org.openide.loaders.DataObjectExistsException {
        super(pf,loader);
        
        getCookieSet().add(XMLJ2eeEditorSupport.class, this);
        getCookieSet().add(EditCookie.class, this);
        getCookieSet().add(EditorCookie.class, this);
        getCookieSet().add(LineCookie.class, this);
        getCookieSet().add(PrintCookie.class, this);
        getCookieSet().add(CloseCookie.class, this);
        // added CheckXMLCookie
        InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
    }
    // Issuezilla 23493 - this is the way how to disable the OpenCoookie from this data object
    protected EditorCookie createEditorCookie () {
        return null;
    }    
    /** Update the node from document. This method is called after document is changed.
    * @param is Input source for the document
    * @return number of the line with error (document is invalid), 0 (xml document is valid)
    */    
    protected abstract SAXParseError updateNode(org.xml.sax.InputSource is) throws java.io.IOException;    
    /** gets the Icon Base for node delegate when parser accepts the xml document as valid
    * @return Icon Base for node delegate
    */   
    protected abstract String getIconBaseForValidDocument();
    
    /** gets the Icon Base for node delegate when parser finds error(s) in xml document
    * @return Icon Base for node delegate
    */
    protected abstract String getIconBaseForInvalidDocument();
    
    /** Implements <code>CookieSet.Factory</code> interface. */
    public org.openide.nodes.Node.Cookie createCookie(Class clazz) {
        if(clazz.isAssignableFrom(XMLJ2eeEditorSupport.class))
            return getEditorSupport();
        else
            return null;
    }
    
    /** Gets editor support for this data object. */
    protected synchronized XMLJ2eeEditorSupport getEditorSupport() {
        if(editor == null) {
            editor = new XMLJ2eeEditorSupport(this);
        }
        return editor;
    }
    /** gets the String for status line when parser finds error(s) in xml document
    * @param error info about an error
    * @return String for status line
    */    
    public String getOutputStringForInvalidDocument(SAXParseError error){
        //return error.getErrorText()+" ["+error.getErrorLine()+","+error.getErrorColumn()+"]";
        String mes = MessageFormat.format (NbBundle.getMessage (XMLJ2eeDataObject.class, "TXT_errorMessage"),
                                new Object [] { error.getErrorText(),
                                                new Integer(error.getErrorLine()),
                                                new Integer(error.getErrorColumn()) });
        return mes;        
    }
    /** Getter for property nodeDirty.
    * @return Value of property nodeDirty.
    */
    public boolean isNodeDirty(){
        return nodeDirty;
    }

    /** setter for property documentDirty. Method updateDocument() usually setsDocumentDirty to false
    */    
    public void setDocumentDirty(boolean dirty){
        documentDirty=dirty;
    }
    
    /** Getter for property documentDirty.
    * @return Value of property documentDirty.
    */
    public boolean isDocumentDirty(){
        return documentDirty;
    }
    
    /** Setter for property nodeDirty.
     * @param dirty New value of property nodeDirty.
     */
    public void setNodeDirty(boolean dirty){
        nodeDirty=dirty;
    }
    
    /** This method repairs Node Delegate (usually after changing document by property editor)
    */  
    protected void repairNode(){
        // PENDING: set the icon in Node
        // ((DataNode)getNodeDelegate()).setIconBase (getIconBaseForValidDocument());
        org.openide.awt.StatusDisplayer.getDefault().setStatusText("");  // NOI18N
        if (inOut!=null) {
            inOut.closeInputOutput();
            errorAnnotation.detach();
        }
    }
    
    /** This method parses XML document and calls abstract updateNode method which
    * updates corresponding Node.
    */    
    public void parsingDocument(){
        //System.out.println("background parsing "); // NOI18N
        //Thread.dumpStack();
        SAXParseError err=null;
        try {
            err=updateNode(prepareInputSource());
        }
        catch (Exception e) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
            setDocumentValid(false);
            return;
        }
        finally {
            closeInputSource();
            documentDirty=false;
        }
        if (err==null){
            setDocumentValid(true);
        }else {
            setDocumentValid(false);
        }
    }

    /** This method is used for obtaining the current source of xml document.
    * First try if document is in the memory. If not, provide the input from
    * underlayed file object.
    * @return The input source from memory or from file
    * @exception IOException if some problem occurs
    */
    protected org.xml.sax.InputSource prepareInputSource() throws java.io.IOException {
        if ((editor != null) && (editor.isDocumentLoaded())) {
            // loading from the memory (Document)
            final javax.swing.text.Document doc = editor.getDocument();
            final String[] str = new String[1];
            // safely take the text from the document
            Runnable run = new Runnable() {
                public void run() {
                    try {
                        str[0] = doc.getText(0, doc.getLength());
                    }
                    catch (javax.swing.text.BadLocationException e) {
                        // impossible
                    }
                }
            };
            
            doc.render(run);
            // TODO: this StringReader should be also closed.
            StringReader reader = new StringReader(str[0]);
            return new org.xml.sax.InputSource(reader);
        } 
        else {
            // loading from the file
            inputStream = new BufferedInputStream(getPrimaryFile().getInputStream());
            return new org.xml.sax.InputSource(inputStream);
        }
    }
    
    /** This method has to be called everytime after prepareInputSource calling.
     * It is used for closing the stream, because it is not possible to access the
     * underlayed stream hidden in InputSource.
     * It is save to call this method without opening.
     */
    protected void closeInputSource() {
        InputStream is = inputStream;
        if (is != null) {
            try {
                is.close();
            }
            catch (IOException e) {
                // nothing to do, if exception occurs during saving.
            }
            if (is == inputStream) {
                inputStream = null;
            }
        }
    }     
    public boolean isDocumentValid(){
        return documentValid;
    }
    public void setDocumentValid (boolean valid){
        if (documentValid!=valid) {
            if (valid) 
                repairNode();
            documentValid=valid;
            firePropertyChange (PROP_DOC_VALID, !documentValid ? Boolean.TRUE : Boolean.FALSE, documentValid ? Boolean.TRUE : Boolean.FALSE);
        }
    }
    public void addSaveCookie(SaveCookie cookie){
        getCookieSet().add(cookie);
    }
    public void removeSaveCookie(){
        org.openide.nodes.Node.Cookie cookie = getCookie(SaveCookie.class);
        if (cookie!=null) getCookieSet().remove(cookie);
    }
    
    public void setSavingDocument(boolean saving){
        savingDocument=saving;
    }
    public boolean isSavingDocument(){
        return savingDocument;
    }    
    public void displayErrorMessage() {
            if (error==null) return;
            if (errorAnnotation==null)
                 errorAnnotation = new org.openide.text.Annotation() {
                 public String getAnnotationType() {
                   return "xml-j2ee-annotation";    // NOI18N
                 }
                 String desc = NbBundle.getMessage(XMLJ2eeDataObject.class, "HINT_XMLErrorDescription");
                 public String getShortDescription() {
                   return desc;
                 }
            };
            if (inOut==null)
                inOut=org.openide.windows.IOProvider.getDefault().getIO(NbBundle.getMessage(XMLJ2eeDataObject.class, "TXT_parser"), false);
            inOut.setFocusTaken (false);
            OutputWriter outputWriter = inOut.getOut();
            int line   = Math.max(0,error.getErrorLine());
            int column = Math.max(0,error.getErrorColumn());
            
            LineCookie cookie = (LineCookie)getCookie(LineCookie.class);
            // getting Line object
            Line xline = cookie.getLineSet ().getCurrent(line==0?0:line-1);
            // attaching Annotation
            errorAnnotation.attach(xline);
            
            try {
                outputWriter.reset();
                // defining of new OutputListener
                IOCtl outList= new IOCtl(xline);
                outputWriter.println(this.getOutputStringForInvalidDocument(error),outList);
            }catch(IOException e){}        
    }
    
    public void setValid(boolean valid) throws java.beans.PropertyVetoException {
        if (!valid && inOut!=null) inOut.closeInputOutput();
        super.setValid(valid);
    }    

    final class IOCtl implements OutputListener {
        /** line we check */
        Line xline;

        public IOCtl (Line xline) {
            this.xline=xline;
        }

        public void outputLineSelected (OutputEvent ev) {
            errorAnnotation.attach(xline);
            xline.show(Line.SHOW_TRY_SHOW);
        }

        public void outputLineAction (OutputEvent ev) {
            errorAnnotation.attach(xline);
            xline.show(Line.SHOW_TRY_SHOW);
        }
        
        public void outputLineCleared (OutputEvent ev) {
            errorAnnotation.detach();
        }
    }
     
    public static class J2eeErrorHandler implements ErrorHandler {
        
        private XMLJ2eeDataObject xmlJ2eeDataObject;
        
        public J2eeErrorHandler(XMLJ2eeDataObject obj) {
             xmlJ2eeDataObject=obj;
        }
        
        public void error(SAXParseException exception) throws SAXException {
            xmlJ2eeDataObject.createSAXParseError(exception);
            throw exception;
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            xmlJ2eeDataObject.createSAXParseError(exception);
            throw exception;
        }

        public void warning(SAXParseException exception) throws SAXException {
            xmlJ2eeDataObject.createSAXParseError(exception);
            throw exception;
        }
    }
    
    private void createSAXParseError(SAXParseException error){
        this.error = new SAXParseError(error);
    }    
    
}

