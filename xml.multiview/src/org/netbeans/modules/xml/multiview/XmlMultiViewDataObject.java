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

package org.netbeans.modules.xml.multiview;

import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.windows.CloneableTopComponent;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.io.ReaderInputStream;
import org.openide.util.NbBundle;

import java.io.*;
import java.util.Enumeration;
import java.util.Date;
import java.lang.ref.WeakReference;

/**
 * XmlMultiviewDataObject.java
 *
 * Created on October 5, 2004, 10:49 AM
 * @author  mkuchtiak
 */
public abstract class XmlMultiViewDataObject extends MultiDataObject implements CookieSet.Factory {

    public static final String PROP_DOCUMENT_VALID = "document_valid"; //NOI18N
    public static final String PROP_SAX_ERROR = "sax_error"; //NOI18N
    protected static final String PROPERTY_DATA_MODIFIED = "data modified";  //NOI18N
    protected static final String PROPERTY_DATA_UPDATED = "data changed";  //NOI18N
    private XmlMultiViewEditorSupport editorSupport;
    private org.xml.sax.SAXException saxError;

    private final DataCache dataCache = new DataCache();
    private EncodingHelper encodingHelper = new EncodingHelper();
    private transient long timeStamp = 0;
    private transient WeakReference lockReference;


    private MultiViewElement activeMVElement;

    private final SaveCookie saveCookie = new SaveCookie() {
        /** Implements <code>SaveCookie</code> interface. */
        public void save() throws java.io.IOException {
            boolean save = acceptEncoding();
            if (save) {
                getEditorSupport().saveDocument();
            }
        }
    };

    /** Creates a new instance of XmlMultiViewDataObject */
    public XmlMultiViewDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        getCookieSet().add(XmlMultiViewEditorSupport.class, this);
    }

    protected EditorCookie createEditorCookie() {
        return getEditorSupport();
    }

    public org.openide.nodes.Node.Cookie createCookie(Class clazz) {
        if (clazz.isAssignableFrom(XmlMultiViewEditorSupport.class)) {
            return getEditorSupport();
        } else {
            return null;
        }
    }

    /** Gets editor support for this data object. */
    protected synchronized XmlMultiViewEditorSupport getEditorSupport() {
        if(editorSupport == null) {
            editorSupport = new XmlMultiViewEditorSupport(this);
        }
        return editorSupport;
    }

    /** enables to switch quickly to XML perspective in multi view editor
     */
    public void goToXmlView() {
        getEditorSupport().goToXmlPerspective();
    }

    protected void setSaxError(org.xml.sax.SAXException saxError) {
        org.xml.sax.SAXException oldError = this.saxError;
        this.saxError=saxError;
        if (oldError==null) {
            if (saxError != null) {
                firePropertyChange(PROP_DOCUMENT_VALID, Boolean.TRUE, Boolean.FALSE);
            }
        } else {
            if (saxError == null) {
                firePropertyChange(PROP_DOCUMENT_VALID, Boolean.FALSE, Boolean.TRUE);
            }
        }

        String oldErrorMessage = getErrorMessage(oldError);
        String newErrorMessage = getErrorMessage(saxError);
        if (oldErrorMessage==null) {
            if (newErrorMessage!=null) {
                firePropertyChange(PROP_SAX_ERROR, null, newErrorMessage);
            }
        } else if (!oldErrorMessage.equals(newErrorMessage)) {
            firePropertyChange(PROP_SAX_ERROR, oldErrorMessage, newErrorMessage);
        }
    }

    private static String getErrorMessage(Exception e) {
        return e == null ? null : e.getMessage();
    }

    public org.xml.sax.SAXException getSaxError() {
        return saxError;
    }

    /** Icon for XML View */
    protected java.awt.Image getXmlViewIcon() {
        return org.openide.util.Utilities.loadImage("org/netbeans/modules/xml/multiview/resources/xmlObject.gif"); //NOI18N
    }

    /** MultiViewDesc for MultiView editor
     */
    protected abstract DesignMultiViewDesc[] getMultiViewDesc();

    public void setLastOpenView(int index) {
        getEditorSupport().setLastOpenView(index);
    }

    /** provides renaming of super top component */
    protected FileObject handleRename(String name) throws IOException {
        FileObject retValue = super.handleRename(name);
        getEditorSupport().updateDisplayName();
        return retValue;
    }

    /**
     * Set whether the object is considered modified.
     * Also fires a change event.
     * If the new value is <code>true</code>, the data object is added into a {@link #getRegistry registry} of opened data objects.
     * If the new value is <code>false</code>,
     * the data object is removed from the registry.
     */
    public void setModified(boolean modif) {
        super.setModified(modif);
        //getEditorSupport().updateDisplayName();
        if (modif) {
            // Add save cookie
            if (getCookie(SaveCookie.class) == null) {
                getCookieSet().add(saveCookie);
            }
        } else {
            // Remove save cookie
            if(saveCookie.equals(getCookie(SaveCookie.class))) {
                getCookieSet().remove(saveCookie);
            }

        }
    }

    public boolean canClose() {
        final CloneableTopComponent topComponent = ((CloneableTopComponent) getEditorSupport().getMVTC());
        Enumeration enumeration = topComponent.getReference().getComponents();
        if (enumeration.hasMoreElements()) {
            enumeration.nextElement();
            if (enumeration.hasMoreElements()) {
                return true;
            }
        }
        FileLock lock;
        try {
            lock = waitForLock();
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return !isModified();
        }
        try {
            return !isModified();
        } finally {
            lock.releaseLock();
        }
    }

    public FileLock waitForLock() throws IOException {
        return waitForLock(10000);
    }

    public FileLock waitForLock(long timeout) throws IOException {
        long t = new Date().getTime() + timeout;
        for (;;) {
            try {
                return dataCache.lock();
            } catch (IOException e) {
                if (new Date().getTime() > t) {
                    throw new IOException("Cannot take data lock for more than " + timeout + " ms");
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                    //
                }
            }
        }
    }

    public org.netbeans.core.api.multiview.MultiViewPerspective getSelectedPerspective() {
        return getEditorSupport().getSelectedPerspective();
    }

    /** Enable to focus specific object in Multiview Editor
     *  The default implementation opens the XML View.
     */
    public void showElement(Object element) {
        getEditorSupport().edit();
    }

    /** Enable to get active MultiViewElement object
     */
    protected MultiViewElement getActiveMultiViewElement() {
        return activeMVElement;
    }
    void setActiveMultiViewElement(MultiViewElement element) {
        activeMVElement = element;
    }
    /** Opens the specific view
     * @param index multi-view index
     */
    public void openView(int index) {
        getEditorSupport().openView(index);
    }

    protected abstract String getPrefixMark();

    boolean acceptEncoding() throws IOException {
        encodingHelper.resetEncoding();
        DataCache dataCache = getDataCache();
        String s = dataCache.getStringData();
        String encoding = encodingHelper.detectEncoding(s.getBytes());
        if (!encodingHelper.getEncoding().equals(encoding)) {
            Object result = showChangeEncodingDialog(encoding);
            if (NotifyDescriptor.YES_OPTION.equals(result)) {
                dataCache.setData(encodingHelper.setDefaultEncoding(s));
            } else if (NotifyDescriptor.NO_OPTION.equals(result)) {
                showUsingDifferentEncodingMessage(encoding);
            } else {
                return false;
            }
        }
        return true;
    }

    private void showUsingDifferentEncodingMessage(String encoding) {
        String message = NbBundle.getMessage(XmlMultiViewDataObject.class, "TEXT_TREAT_USING_DIFFERENT_ENCODING", encoding,
                encodingHelper.getEncoding());
        NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(message);
        descriptor.setTitle(getPrimaryFile().getPath());
        DialogDisplayer.getDefault().notify(descriptor);
    }

    private Object showChangeEncodingDialog(String encoding) {
        String message = NbBundle.getMessage(Utils.class, "TEXT_CHANGE_DECLARED_ENCODING", encoding,
                encodingHelper.getEncoding());
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(message, getPrimaryFile().getPath(),
                NotifyDescriptor.YES_NO_CANCEL_OPTION);
        return DialogDisplayer.getDefault().notify(descriptor);
    }

    public EncodingHelper getEncodingHelper() {
        return encodingHelper;
    }

    public DataCache getDataCache() {
        return dataCache;
    }

    public class DataCache {

        private transient String buffer = null;
        private long fileTime = 0;

        public void loadData() {
            FileObject file = getPrimaryFile();
            if (fileTime == file.lastModified().getTime()) {
                return;
            }
            try {
                FileLock dataLock = lock();
                loadData(file, dataLock);
            } catch (IOException e) {
                if (buffer == null) {
                    buffer = ""; //NOI18N
                }
            }
        }

        public void loadData(FileObject file, FileLock dataLock) throws IOException {
            try {
                BufferedInputStream inputStream = new BufferedInputStream(file.getInputStream());
                String encoding = encodingHelper.detectEncoding(inputStream);
                if (!encodingHelper.getEncoding().equals(encoding)) {
                    showUsingDifferentEncodingMessage(encoding);
                }
                Reader reader = new InputStreamReader(inputStream, encodingHelper.getEncoding());
                long time;
                StringBuffer sb = new StringBuffer(5000);
                try {
                    time = file.lastModified().getTime();
                    int i;
                    while ((i = reader.read()) != -1) {
                        sb.append((char) i);
                    }
                } finally {
                    reader.close();
                }
                buffer = null;
                fileTime = time;
                setData(dataLock, sb.toString(), true);
            } finally {
                dataLock.releaseLock();
            }
        }

        public void setData(FileLock lock, String s, boolean modify) throws IOException {
            testLock(lock);
            boolean modified = isModified() || modify;
            long oldTimeStamp = timeStamp;
            if (setData(s)) {
                if (!modified) {
                    saveData(lock);
                    firePropertyChange(PROPERTY_DATA_UPDATED, new Long(oldTimeStamp), new Long(timeStamp));
                } else {
                    firePropertyChange(PROPERTY_DATA_MODIFIED, new Long(oldTimeStamp), new Long(timeStamp));
                }
            }
        }

        private boolean setData(String s) {
            if (s.equals(buffer)) {
                return false;
            }
            buffer = s;
            long newTimeStamp = new Date().getTime();
            if (newTimeStamp <= timeStamp) {
                newTimeStamp = timeStamp + 1;
            }
            timeStamp = newTimeStamp;
            fileTime = 0;
            return true;
        }

        public synchronized void saveData(FileLock dataLock) {
            if (buffer == null || fileTime == getPrimaryFile().lastModified().getTime()) {
                return;
            }

            try {
                XmlMultiViewEditorSupport editorSupport = getEditorSupport();
                if (editorSupport.getDocument() == null) {
                    XmlMultiViewEditorSupport.XmlEnv xmlEnv = editorSupport.getXmlEnv();
                    OutputStream outputStream = getPrimaryFile().getOutputStream(xmlEnv.takeLock());
                    Writer writer = new OutputStreamWriter(outputStream, encodingHelper.getEncoding());
                    try {
                        writer.write(buffer);
                    } finally {
                        writer.close();
                        xmlEnv.unmarkModified();
                        resetFileTime();
                    }
                } else {
                    editorSupport.saveDocument(dataLock);
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }

        public FileLock lock() throws IOException {
            if (getLock() != null) {
                throw new FileAlreadyLockedException();
            }
            FileLock l = new FileLock();
            lockReference = new WeakReference(l);
            return l;
        }

        private FileLock getLock() {
            FileLock l = lockReference == null ? null : (FileLock) lockReference.get();
            if (l != null && !l.isValid()) {
                l = null;
            }
            return l;
        }

        public String getStringData() {
            if (buffer == null) {
                loadData();
            }
            return buffer;
        }

        public byte[] getData() {
            try {
                return getStringData().getBytes(encodingHelper.getEncoding());
            } catch (UnsupportedEncodingException e) {
                return null;  // should not happen
            }
        }

        public void setData(FileLock lock, byte[] data, boolean modify) throws IOException {
            encodingHelper.detectEncoding(data);
            setData(lock, new String(data, encodingHelper.getEncoding()), modify);
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public InputStream createInputStream() {
            try {
                encodingHelper.detectEncoding(getStringData().getBytes());
                return new ReaderInputStream(new StringReader(getStringData()), encodingHelper.getEncoding());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        public Reader createReader() throws IOException {
            return new StringReader(getStringData());
        }

        public OutputStream createOutputStream() throws IOException {
            final FileLock dataLock = lock();
            return new ByteArrayOutputStream() {
                public void close() throws IOException {
                    try {
                        super.close();
                        setData(dataLock, toByteArray(), true);
                    } finally {
                        dataLock.releaseLock();
                    }
                }
            };
        }

        public OutputStream createOutputStream(final FileLock dataLock, final boolean modify) throws IOException {
            testLock(dataLock);
            return new ByteArrayOutputStream() {
                public void close() throws IOException {
                    super.close();
                    setData(dataLock, toByteArray(), modify);
                    if (!modify) {
                        dataCache.saveData(dataLock);
                    }
                }
            };
        }

        public Writer createWriter() throws IOException {
            final FileLock dataLock = lock();
            return new StringWriter() {
                public void close() throws IOException {
                    try {
                        super.close();
                        setData(dataLock, toString(), true);
                    } finally {
                        dataLock.releaseLock();
                    }
                }
            };
        }

        public Writer createWriter(final FileLock dataLock, final boolean modify) throws IOException {
            testLock(dataLock);
            return new StringWriter() {
                public void close() throws IOException {
                    super.close();
                    setData(dataLock, toString(), modify);
                    if (!modify) {
                        dataCache.saveData(dataLock);
                    }
                }
            };
        }

        public void testLock(FileLock lock) throws IOException {
            if (lock == null || lock != getLock()) {
                throw new IOException();
            }
        }

        public void resetFileTime() {
            fileTime = getPrimaryFile().lastModified().getTime();
        }
    }
}
