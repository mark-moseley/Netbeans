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
package org.netbeans.modules.xsl.transform;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.OutputStream;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;

import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.openide.*;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.actions.CookieAction;

import org.netbeans.api.xml.cookies.*;

import org.netbeans.modules.xml.core.XMLDataObject;
import org.netbeans.modules.xml.core.actions.CollectXMLAction;
import org.netbeans.modules.xml.core.actions.InputOutputReporter;

import org.netbeans.modules.xml.core.lib.GuiUtil;
import org.netbeans.modules.xml.core.lib.FileUtilities;

import org.netbeans.modules.xsl.XSLDataObject;
import org.netbeans.modules.xsl.ui.TransformPanel;
import org.netbeans.modules.xsl.settings.TransformHistory;
import org.netbeans.modules.xsl.actions.TransformAction;
import org.netbeans.modules.xsl.utils.TransformUtil;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Handle workflow of transformation action, gather UI info and
 * launch the processor.
 * <p>
 * This class has very/needlessly complicated workflow.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TransformPerformer {
    /** Represent transformation output window. */
    private InputOutputReporter cookieObserver = null;
    private Node[] nodes;

    // instance freshness state
    private volatile boolean stalled = false;
    private volatile boolean active = true;

    public TransformPerformer(Node[] nodes) {
        this.nodes = nodes;
    }

    /**
     * Entry point called from transform action.
     * There is a fresh instance per call.
     */
    public void perform() {

        if (stalled) throw new IllegalStateException();

        try {
            if ( nodes.length == 2 ) {

                // automatically detect if one of selected nodes is transformation
                // in such case suppose that user want to it to transform second file

                DataObject do1 = (DataObject) nodes[0].getCookie(DataObject.class);
                boolean xslt1 = TransformUtil.isXSLTransformation(do1);
                DataObject do2 = (DataObject) nodes[1].getCookie(DataObject.class);
                boolean xslt2 = TransformUtil.isXSLTransformation(do2);

                if ( Util.THIS.isLoggable() ) /* then */ {
                    Util.THIS.debug("TransformAction.performAction:");
                    Util.THIS.debug("    do1 [" + xslt1 + "] = " + do1);
                    Util.THIS.debug("    do2 [" + xslt2 + "] = " + do2);
                }

                if ( xslt1 != xslt2 ) {
                    TransformableCookie transformable;
                    DataObject xmlDO;
                    DataObject xslDO;
                    if ( xslt1 ) {
                        transformable = (TransformableCookie) nodes[1].getCookie(TransformableCookie.class);
                        xmlDO = do2;
                        xslDO = do1;
                    } else {
                        transformable = (TransformableCookie) nodes[0].getCookie(TransformableCookie.class);
                        xmlDO = do1;
                        xslDO = do2;
                    }
                    DoublePerformer performer = new DoublePerformer(transformable, xmlDO, xslDO);
                    performer.perform();
                } else {
                    TransformableCookie transformable1 = (TransformableCookie) nodes[0].getCookie(TransformableCookie.class);
                    SinglePerformer performer = new SinglePerformer(transformable1, do1, xslt1);
                    performer.setLastInBatch(false);
                    performer.perform();

                    TransformableCookie transformable2 = (TransformableCookie) nodes[1].getCookie(TransformableCookie.class);
                    performer = new SinglePerformer(transformable2, do2, xslt2);
                    performer.perform();
                }
            } else { // nodes.length != 2
                for ( int i = 0; i < nodes.length; i++ ) {
                    DataObject dataObject = (DataObject) nodes[i].getCookie(DataObject.class);
                    TransformableCookie transformable = null;
                    boolean xslt = TransformUtil.isXSLTransformation(dataObject);
                    if ( xslt == false ) {
                        transformable = (TransformableCookie) nodes[i].getCookie(TransformableCookie.class);
                    }
                    SinglePerformer performer = new SinglePerformer(transformable, dataObject, xslt);
                    performer.setLastInBatch(i == (nodes.length -1));
                    performer.perform();
                }
            }
        } finally {
            stalled = true;
        }
    }

    /**
     * Is still running
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Always return an instance. Shareable by all children nested performers.
     */
    private InputOutputReporter getCookieObserver() {
        if ( cookieObserver == null ) {
            String label = Util.THIS.getString("PROP_transformation_io_name");
            cookieObserver = new InputOutputReporter(label);
        }
        return cookieObserver;
    }
    
    
    //
    // class AbstractPerformer
    //
    
    private abstract class AbstractPerformer implements ActionListener {
        // if called on TransformableCookie node
        private TransformableCookie transformableCookie;
        // input XML source DataObject
        protected DataObject xmlDO;
        // <?xml-stylesheet
        protected Source xmlStylesheetSource;
        // input XSLT script DataObject
        protected DataObject xslDO;
        // used to resolve relative path
        protected FileObject baseFO;
        // URL of base FileObject
        protected URL baseURL;
        // XML Source
        private Source xmlSource;
        // XSLT Source
        private Source xslSource;
        // Result FileObject
        private FileObject resultFO;
        
        private TransformPanel transformPanel;
        private DialogDescriptor dialogDescriptor;
        private Dialog dialog;
        
        private TransformPanel.Data data;
        private boolean last = true;


        public AbstractPerformer(TransformableCookie transformable) {
            this.transformableCookie = transformable;
        }


        /**
         * It shows a dialog and let user selct his options. Then it performs them.
         */
        public final void perform() {
            try {
                init(); // throws IOException
                showDialog(); // throws IOException
            } catch (IOException exc) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug(exc);
                
                NotifyDescriptor nd = new NotifyDescriptor.Message(exc.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);

                if (isLastInBatch()) {
                    active = false;
                }
            }
        }
        
        protected abstract void init() throws IOException;
        
        protected abstract void storeData();
        
        private void showDialog() throws IOException {
            String xmlStylesheetName = null;
            if ( xmlStylesheetSource != null ) {
                xmlStylesheetName = xmlStylesheetSource.getSystemId();
            }
            transformPanel = new TransformPanel(xmlDO, xmlStylesheetName, xslDO);
            
            DialogDescriptor transformDD = new DialogDescriptor
            (transformPanel,
            Util.THIS.getString("NAME_transform_panel_title"), true,
            DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
            DialogDescriptor.BOTTOM_ALIGN,
            new HelpCtx(TransformAction.class), null);
            transformDD.setClosingOptions(new Object[] { DialogDescriptor.CANCEL_OPTION });
            transformDD.setButtonListener(this);
            
            dialog = DialogDisplayer.getDefault().createDialog(transformDD);
            dialog.show();
        }
        
        protected void prepareData() throws IOException, FileStateInvalidException, MalformedURLException, ParserConfigurationException, SAXException {
            data = transformPanel.getData();
            
            if ( Util.THIS.isLoggable() ) /* then */ {
                Util.THIS.debug("TransformPerformer...performTransformation");
                Util.THIS.debug("    transformable = " + transformableCookie);
                Util.THIS.debug("    baseFileObject = " + baseFO);
                Util.THIS.debug("    data = " + data);
            }

            try {
                xmlSource = TransformUtil.createSource(baseURL, data.getInput()); // throws IOException, MalformedURLException, FileStateInvalidException, ParserConfigurationException, SAXException
            } catch (IOException ex) {
                ErrorManager.getDefault().annotate(ex, Util.THIS.getString("MSG_sourceError"));
                throw ex;
            }
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("    xmlSource = " + xmlSource.getSystemId());
            
            if ( data.getXSL() != null ) {
                try {
                    xslSource = TransformUtil.createSource(baseURL, data.getXSL()); // throws IOException, MalformedURLException, FileStateInvalidException, ParserConfigurationException, SAXException
                } catch (IOException ex) {
                    ErrorManager.getDefault().annotate(ex, Util.THIS.getString("MSG_transError"));
                    throw ex;
                }
            } else {
                xslSource = xmlStylesheetSource;
            }
            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("    xslSource = " + xslSource.getSystemId());
            
            if ( data.getOutput() != null ) { // not Preview
                String fileName = data.getOutput().toString().replace('\\', '/');
                try {
                    resultFO = FileUtilities.createFileObject(baseFO.getParent(), fileName, data.isOverwriteOutput()); // throws IOException
                } catch (IOException ex) {
                    ErrorManager.getDefault().annotate(ex, Util.THIS.getString("MSG_resultError"));
                    throw ex;
                }

                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("    resultFO = " + resultFO);
            }
        }
        
        protected void updateHistory(DataObject dataObject, boolean xslt) {
            FileObject fileObject = dataObject.getPrimaryFile();
            TransformHistory history = (TransformHistory) fileObject.getAttribute(TransformHistory.TRANSFORM_HISTORY_ATTRIBUTE);
            if ( history == null ) {
                history = new TransformHistory();
            }
            String outputStr=null;
            if(data.getOutput()!=null) {
                outputStr=data.getOutput().toString();
            }
            if ( xslt ) {
                history.addXML(data.getInput(), outputStr);
            } else {
                history.addXSL(data.getXSL(), outputStr);
            }
            history.setOverwriteOutput(data.isOverwriteOutput());
            history.setProcessOutput(data.getProcessOutput());
            
            try {
                fileObject.setAttribute(TransformHistory.TRANSFORM_HISTORY_ATTRIBUTE, history);
            } catch (IOException exc) {
                // ... will not be persistent!
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            }
        }

        /**
         * Inicializes a servet and then provokes it by opening browser poiting to it.
         * External XSLT processor is called from the servlet.
         */
        private void previewOutput() throws MalformedURLException, UnknownHostException {
            TransformServlet.prepare(transformableCookie, xmlSource, xslSource);
            showURL(TransformServlet.getServletURL());
        }

        /**
         * External XSLT processor is called from this method.
         */
        private void fileOutput() throws IOException, FileStateInvalidException, TransformerException {
            OutputStream outputStream = null;
            FileLock fileLock = null;
            try {
                fileLock = resultFO.lock();               
                outputStream = resultFO.getOutputStream(fileLock);
                
                Result outputResult = new StreamResult(outputStream); // throws IOException, FileStateInvalidException
                
                if ( Util.THIS.isLoggable() ) /* then */ {
                    Util.THIS.debug("    resultFO = " + resultFO);
                    Util.THIS.debug("    outputResult = " + outputResult);
                }
                
                String xmlName = data.getInput();
                String xslName = data.getXSL();
                TransformPerformer.this.getCookieObserver().message(Util.THIS.getString("MSG_transformation_1", xmlName, xslName));
                TransformUtil.transform(xmlSource, transformableCookie, xslSource, outputResult, TransformPerformer.this.getCookieObserver()); // throws TransformerException
                
                // revalidate DataObject associated with possibly partially written file #28079
                try {
                    DataObject dataObject = DataObject.find(resultFO);
                    dataObject.setValid(false);
                } catch (DataObjectNotFoundException dnf) {
                    throw new IllegalStateException();
                } catch (PropertyVetoException pve) {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot invalidate " + resultFO);
                }
                
                if ( data.getProcessOutput() == TransformHistory.APPLY_DEFAULT_ACTION ) {
                    GuiUtil.performDefaultAction(resultFO);
                    GuiUtil.performDefaultAction(resultFO);
                } else if ( data.getProcessOutput() == TransformHistory.OPEN_IN_BROWSER ) {
                    showURL(resultFO.getURL());
                }
            } catch (FileAlreadyLockedException exc) {
                throw (FileAlreadyLockedException) ErrorManager.getDefault().annotate(exc, Util.THIS.getString("ERR_FileAlreadyLockedException_output"));
            } finally {
                if ( outputStream != null ) {
                    outputStream.close();
                }
                if ( fileLock != null ) {
                    fileLock.releaseLock();
                }
            }
        }
        
        private void showURL(URL url) {
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            GuiUtil.setStatusText(Util.THIS.getString("MSG_opening_browser"));
        }
        
        
        //
        // from ActionListener
        //
        
        public final void actionPerformed(ActionEvent e) {
            if ( Util.THIS.isLoggable() ) /* then */ {
                Util.THIS.debug("[TransformPerformer::AbstractPerformer] actionPerformed: " + e);
                Util.THIS.debug("    ActionEvent.getSource(): " + e.getSource());
            }
            
            if ( DialogDescriptor.OK_OPTION.equals(e.getSource()) ) {
                try {
                    prepareData(); // throws IOException(, FileStateInvalidException, MalformedURLException), ParserConfigurationException, SAXException
                    
                    if ( ( data.getOutput() != null ) &&
                    ( resultFO == null ) ) {
                        return;
                    }
                    
                    dialog.dispose();
                    storeData();
                    async();

                } catch (Exception exc) { // IOException, ParserConfigurationException, SAXException
                    // during prepareData(), previewOutput() and fileOutput()
                    if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug(exc);
                    
                    //                     NotifyDescriptor nd = new NotifyDescriptor.Message (exc.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
                    //                     TopManager.getDefault().notify (nd);
                    
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, exc);
                    if (isLastInBatch()) {
                        active = false;
                    }
                }
            }
        }

        /**
         * Perform the transformatin itself asynchronously ... (#29614)
         */
        private void async() {
            RequestProcessor rp = RequestProcessor.getDefault();
            rp.post(new Runnable() {
                public void run() {
                    try {
                        if ( data.getOutput() == null ) { // Preview
                            previewOutput(); // throws IOException (MalformedURLException, UnknownHostException)
                        } else {
                            fileOutput(); // throws IOException(, FileStateInvalidException), TransformerException
                        }
                    } catch (TransformerException exc) { // during fileOutput();
                        // ignore it -> it should be displayed by CookieObserver!
                    } catch (Exception exc) { // IOException, ParserConfigurationException, SAXException
                        // during prepareData(), previewOutput() and fileOutput()
                        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug(exc);

                        //                     NotifyDescriptor nd = new NotifyDescriptor.Message (exc.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
                        //                     TopManager.getDefault().notify (nd);

                        ErrorManager.getDefault().notify(ErrorManager.WARNING, exc);
                    } finally {
                        if (isLastInBatch()) {
                            InputOutputReporter cookieObserver = getCookieObserver();
                            if ( cookieObserver != null ) {
                                cookieObserver.message(Util.THIS.getString("MSG_transformation_2"));
                                cookieObserver.moveToFront();
                            }
                            active = false;
                        }
                    }
                }
            });
        }


        /**
         * If possible it finds "file:" URL if <code>fileObject</code> is on LocalFileSystem.
         * @return URL of <code>fileObject</code>.
         */
        protected URL preferFileURL(FileObject fileObject) throws MalformedURLException, FileStateInvalidException {
            URL fileURL = null;
            File file = FileUtil.toFile(fileObject);
            
            if ( file != null ) {
                fileURL = file.toURL();
            } else {
                fileURL = fileObject.getURL();
            }
            return fileURL;
        }

        public final void setLastInBatch(boolean last) {
            this.last = last;
        }

        /**
         * Return if caller uses more perfomers and this one is the last one.
         */
        public final boolean isLastInBatch() {
            return last;
        }
    } // class AbstractPerformer
    
    
    //
    // class SinglePerformer
    //
    
    private class SinglePerformer extends AbstractPerformer {
        private DataObject dataObject;
        private boolean xslt;
        
        public SinglePerformer(TransformableCookie transformable, DataObject dataObject, boolean xslt) {
            super(transformable);
            
            this.dataObject = dataObject;
            this.xslt = xslt;
        }
        
        /**
         * @throws FileStateInvalidException from baseFO.getURL();
         */
        protected void init() throws IOException {
            baseFO = dataObject.getPrimaryFile();
            baseURL = preferFileURL(baseFO);
            
            if ( xslt ) {
                xmlDO = null;
                xmlStylesheetSource = null;
                xslDO = dataObject;
            } else {
                xmlDO = dataObject;
                xmlStylesheetSource = TransformUtil.getAssociatedStylesheet(baseURL);
                xslDO = null;
            }
        }
        
        protected void storeData() {
            updateHistory(dataObject, xslt);
        }
        
    } // class SinglePerformer
    
    
    //
    // class DoublePerformer
    //
    
    private class DoublePerformer extends AbstractPerformer {
        
        public DoublePerformer(TransformableCookie transformable, DataObject xmlDO, DataObject xslDO) {
            super(transformable);
            
            this.xmlDO = xmlDO;
            this.xslDO = xslDO;
        }
        
        /**
         * @throws FileStateInvalidException from baseFO.getURL();
         */
        protected void init() throws IOException {
            baseFO = xmlDO.getPrimaryFile();
            baseURL = preferFileURL(baseFO);
        }
        
        protected void storeData() {
            updateHistory(xmlDO, false);
            updateHistory(xslDO, true);
        }
        
        
    } // class DoublePerformer
    
}
