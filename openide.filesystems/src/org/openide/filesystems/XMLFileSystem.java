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

package org.openide.filesystems;

import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.openide.util.Enumerations;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/** XML-based filesystem.
 * <PRE>
 *  Description of format of XML file (which can be parsed by XMLFileSystem)
 * ==================================================================
 * Allowed Elements:        filesystem,file,folder,attr
 *
 * Mandatory attributes:
 *         -for filesystem    version=... (e.g. "1.0")
 *         -for file,folder,attr name=....  (e.g.: &lt;folder name="Config"&gt;)
 *         -for attr is mandatory one of bytevalue,shortvalue,intvalue,longvalue,floatvalue,doublevalue,boolvalue,charvalue,stringvalue,methodvalue,serialvalue,urlvalue
 *
 * Allowed atributes:
 *         -for file:        url=.... (e.g.: &lt;file name="sample.xml" url="file:/c:/sample.xml"&gt;)
 *         -for folder,filesystem        nothing allowed
 *
 *
 *
 * Note: file can contain         content e.g.:
 *  &lt; file name="sample.java"&gt;
 * &lt; ![CDATA[
 * package org.sample;
 * import java.io;
 * ]]&gt;
 * &lt; /file&gt;
 * But using url="..." is preferred.
 *
 *
 * This class implements virtual FileSystem. It is special case of FileSystem in XML format.
 *
 * Description of this format best ilustrate DTD file that is showed in next lines:
 * &lt; !ELEMENT filesystem (file | folder)*&gt;
 * &lt; !ATTLIST filesystem version CDATA #REQUIRED&gt; //version not checkked yet
 * &lt; !ELEMENT folder (file |folder | attr)*&gt;
 * &lt; !ATTLIST folder name CDATA #REQUIRED&gt; //name of folder
 * &lt; !ELEMENT file (#PCDATA | attr)*&gt;
 * &lt; !ATTLIST file name CDATA #REQUIRED&gt; //name of file
 * &lt; !ATTLIST file url CDATA #IMPLIED&gt; //content of the file can be find at url
 * &lt; !ELEMENT attr EMPTY&gt;
 * &lt; !ATTLIST attr name CDATA #REQUIRED&gt; //name of attribute
 * &lt; !ATTLIST attr bytevalue CDATA #IMPLIED&gt;//the rest - types of attributes
 * &lt; !ATTLIST attr shortvalue CDATA #IMPLIED&gt;
 * &lt; !ATTLIST attr intvalue CDATA #IMPLIED&gt;
 * &lt; !ATTLIST attribute longvalue CDATA #IMPLIED&gt;
 * &lt; !ATTLIST attr floatvalue CDATA #IMPLIED&gt;
 * &lt; !ATTLIST attr doublevalue CDATA #IMPLIED&gt;
 * &lt; !ATTLIST attr boolvalue CDATA #IMPLIED&gt;
 * &lt; !ATTLIST attr charvalue CDATA #IMPLIED&gt;
 * &lt; !ATTLIST attr stringvalue CDATA #IMPLIED&gt;
 * &lt; !ATTLIST attr methodvalue CDATA #IMPLIED&gt;
 * &lt; !ATTLIST attr serialvalue CDATA #IMPLIED&gt;
 * &lt; !ATTLIST attr urlvalue CDATA #IMPLIED&gt;
 * </PRE>
 *
 * <p>
 * The methodvalue attribute can be in form of <code>pgk1.pkg2.ClassName.methodName</code>
 * which should point to existing class with static method usually having no, one
 * or two arguments. This method does not need to be public or in public class, if 
 * the filesystem has permissions to call the method. The method can take one
 * of the following signatures:
 * <pre>
 * static Value methodName();
 * static Value methodName(FileObject fo);
 * static Value methodName(FileObject fo, String attrName);
 * static Value methodName(Map<String,Object> attrs); // since 7.0
 * static Value methodName(Map<String,Object> attrs, String attrName); // since 7.0
 * </pre>
 * where <code>Value</code> can be any java type.
 *
 *
 * @author Radek Matous
 */
public final class XMLFileSystem extends AbstractFileSystem {
    private static final long serialVersionUID = 28974107313702326L;

    // <?xml version="1.0"?>
    // <!DOCTYPE filesystem PUBLIC "-//NetBeans//DTD Filesystem 1.0//EN" "http://www.netbeans.org/dtds/filesystem-1_0.dtd">
    // <filesystem>...</filesystem>
    private static final Map<String, String> DTD_MAP = new HashMap<String, String>();

    static {
        DTD_MAP.put("-//NetBeans//DTD Filesystem 1.0//EN", "org/openide/filesystems/filesystem.dtd"); //NOI18N
        DTD_MAP.put("-//NetBeans//DTD Filesystem 1.1//EN", "org/openide/filesystems/filesystem1_1.dtd"); //NOI18N        
    }

    /**  Url location of XML document    */
    private URL[] urlsToXml = new URL[] {  };
    private transient FileObjRef<? extends FileObject> rootRef;

    /** Constructor. Creates new XMLFileSystem */
    public XMLFileSystem() {
        Impl impl = new Impl(this);
        this.list = impl;
        this.info = impl;
        this.change = impl;
        this.attr = impl;
    }

    /** Constructor. Creates new XMLFileSystem.
     * @param uri to file with definition of XMLFileSystem
     * @throws SAXException if parsing is not succesful
     */
    public XMLFileSystem(String uri) throws SAXException {
        this();

        if (uri == null) {
            throw new NullPointerException("Null uri"); // NOI18N
        }

        try {
            setXmlUrl(new URL(uri));
        } catch (Exception e) {
            throw (SAXException) ExternalUtil.copyAnnotation(new SAXException(e.getMessage()), e);
        }
    }

    /** Constructor. Creates new XMLFileSystem.
     * @param url to definition of XMLFileSystem
     * @throws SAXException if parsing not succesful
     */
    public XMLFileSystem(URL url) throws SAXException {
        this();

        if (url == null) {
            throw new NullPointerException("Null url"); // NOI18N
        }

        try {
            setXmlUrl(url);
        } catch (Exception e) {
            throw (SAXException) ExternalUtil.copyAnnotation(new SAXException(e.getMessage()), e);
        }
    }

    /** Constructor. Allows user to provide own capabilities
    * for this filesystem.
    * @param cap capabilities for this filesystem
     * @deprecated Useless.
    */
    @Deprecated
    public XMLFileSystem(FileSystemCapability cap) {
        this();
        setCapability(cap);
    }

    /** Getter of url field.
     * @return URL associated with XMLFileSystem or null if no URL was set.
     * In case that definition of XMLFileSystem
     * is merged from more URLs than the first is returned.
     */
    public URL getXmlUrl() {
        return (urlsToXml.length > 0) ? urlsToXml[0] : null;
    }

    /**
     * Setter of url field. Set name of the XML file.
     * @param url with definition of XMLFileSystem
     * @throws PropertyVetoException if the change is not allowed by a listener
     * @throws IOException if the file is not valid
     */
    public synchronized void setXmlUrl(URL url) throws IOException, PropertyVetoException {
        setXmlUrl(url, false);
    }

    /**
     * Setter of url field. Set name of the XML file.
     * @param url with definition of XMLFileSystem
     * @param validate sets validating of SAXParser
     * @throws PropertyVetoException if the change is not allowed by a listener
     * @throws IOException if the file is not valid
     */
    public void setXmlUrl(URL url, boolean validate) throws IOException, PropertyVetoException {
        try {
            beginAtomicAction();

            synchronized (this) {
                setXmlUrls(new URL[] { url }, validate);
            }
        } finally {
            finishAtomicAction();
        }
    }

    /** Getter of url fields.
     * @return URLs associated with XMLFileSystem.
     * @since 1.14
     */
    public URL[] getXmlUrls() {
        return urlsToXml;
    }

    /** Setter of url fields. First URL in array sets name of XMLFileSystem.
     * If more then one url in array of URLs defines the same FileObject, then
     * url with lower index in array overrides (means content and attributes) the other.
     * @param urls array of definitions (in xml form) of XMLFileSystem
     * @throws IOException if the file is not valid
     * @throws PropertyVetoException if the change is not allowed by a listener
     * @since 1.14
     */
    public void setXmlUrls(URL[] urls) throws IOException, PropertyVetoException {
        try {
            beginAtomicAction();

            synchronized (this) {
                setXmlUrls(urls, false);
            }
        } finally {
            finishAtomicAction();
        }
    }

    @SuppressWarnings("deprecation") // need to set it for compat
    private void _setSystemName(String s) throws PropertyVetoException {
        setSystemName(s);
    }

    private synchronized void setXmlUrls(URL[] urls, boolean validate)
    throws IOException, PropertyVetoException {
        if ((urls == null) || Arrays.asList(urls).contains(null)) {
            throw new NullPointerException("Null URL list or member"); // NOI18N
        }

        ResourceElem rootElem;
        String oldDisplayName = getDisplayName();

        if (urls.length == 0) {
            urlsToXml = new URL[] {  };
            refreshChildrenInAtomicAction((AbstractFolder) getRoot(), rootElem = new ResourceElem(true, urls, null)); // NOI18N
            rootElem = null;

            return;
        }

        Handler handler = new Handler(DTD_MAP, rootElem = new ResourceElem(true, urls, null), validate); // NOI18N        

        URL[] origUrls = urlsToXml;
        urlsToXml = new URL[urls.length];

        try {
            _setSystemName("XML_" + urls[0].toExternalForm().replace('/','-')); // NOI18N
        } catch (PropertyVetoException pvx) {
            urlsToXml = origUrls;
            rootElem = null;
            throw pvx;
        }

        URL act = null;

        try {
            XMLReader xp = XMLUtil.createXMLReader(validate, false);
            xp.setEntityResolver(handler);
            xp.setContentHandler(handler);
            xp.setErrorHandler(handler);

            for (int index = 0; index < urls.length; index++) {
                act = urls[index];
                urlsToXml[index] = act;
                handler.urlContext = act;

                String systemId = act.toExternalForm();

                xp.parse(systemId);
            }

            refreshChildrenInAtomicAction((AbstractFolder) getRoot(), rootElem);
        } catch (IOException iox) {
            urlsToXml = origUrls;
            throw iox;
        } catch (Exception e) {
            throw (IOException) new IOException(act + ": " + e.toString()).initCause(e); // NOI18N
        } finally {
            rootElem = null;
        }

        firePropertyChange(PROP_DISPLAY_NAME, oldDisplayName, getDisplayName());
    }

    /**
    * @return if value of lastModified should be cached
    */
    boolean isLastModifiedCacheEnabled() {
        return false;
    }

    /**
     * Test if the file is folder or contains data.
     * @param name name of the file
     * @return true if the file is folder, false otherwise
     */
    private boolean isFolder(String name) {
        Reference ref = findReference(name);

        if ((ref != null) && (ref instanceof FileObjRef)) {
            return ((FileObjRef) ref).isFolder();
        }

        return false;
    }

    /**
    * Get input stream.
    *
    * @param name the file to test
    * @return an input stream to read the contents of this file
    * @exception FileNotFoundException if the file does not exists or is invalid
    */
    private InputStream getInputStream(String name) throws java.io.FileNotFoundException {
        Reference ref = findReference(name);

        if ((ref != null) && (ref instanceof FileObjRef)) {
            return (((FileObjRef) ref).getInputStream(name));
        }

        throw new FileNotFoundException(NbBundle.getMessage(XMLFileSystem.class, "EXC_CanntRead", name)); // NOI18N
    }

    /**
    * Get URL.
    *
    * @param name of the file to test
    * @return URL of resource or null
    * @exception FileNotFoundException if the file does not exists or is invalid
    */
    URL getURL(String name) throws java.io.FileNotFoundException {
        Reference ref = findReference(name);

        if ((ref != null) && (ref instanceof FileObjRef)) {
            return ((FileObjRef) ref).createAbsoluteUrl(name);
        }

        throw new FileNotFoundException(NbBundle.getMessage(XMLFileSystem.class, "EXC_CanntRead", name)); // NOI18N
    }

    /** Get size of stream*/
    private long getSize(String name) {
        Reference ref = findReference(name);

        if ((ref != null) && (ref instanceof FileObjRef)) {
            return ((FileObjRef) ref).getSize(name);
        }

        return 0;
    }

    /**returns value of last modification*/
    private java.util.Date lastModified(String name) {
        Reference ref = findReference(name);

        if ((ref != null) && (ref instanceof FileObjRef)) {
            return ((FileObjRef) ref).lastModified(name);
        }

        /**return value for resource that does not exists*/
        return new Date(0);
    }

    /** Provides a name for the system that can be presented to the user.
     * @return user presentable name of the filesystem
     */
    public String getDisplayName() {
        if ((urlsToXml.length == 0) || (urlsToXml[0] == null) || (urlsToXml[0].toExternalForm().length() == 0)) {
            return NbBundle.getMessage(XMLFileSystem.class, "XML_NotValidXMLFileSystem"); // NOI18N
        }

        return "XML:" + urlsToXml[0].toExternalForm().trim(); // NOI18N
    }

    /** Test if the filesystem is read-only or not.
     * @return true if the system is read-only
     */
    public boolean isReadOnly() {
        return true;
    }

    /** Initializes the root of FS.
    */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        //ois.defaultReadObject ();
        ObjectInputStream.GetField fields = ois.readFields();
        URL[] urls = (URL[]) fields.get("urlsToXml", null); // NOI18N

        if (urls == null) {
            urls = new URL[1];
            urls[0] = (URL) fields.get("uriId", null); // NOI18N
        }

        try {
            if (urlsToXml.length != 1) {
                setXmlUrls(urlsToXml);
            } else {
                setXmlUrl(urlsToXml[0]);
            }
        } catch (PropertyVetoException ex) {
            IOException x = new IOException(ex.getMessage());
            ExternalUtil.copyAnnotation(x, ex);
            throw x;
        }
    }

    /** Notifies this filesystem that it has been added to the repository.
     * Various initialization tasks should go here. Default implementation is noop.
     */
    public void addNotify() {
    }

    /** Notifies this filesystem that it has been removed from the repository.
     * Concrete filesystem implementations should perform clean-up here.
     * Default implementation is noop.
     */
    public void removeNotify() {
    }

    protected <T extends FileObject> Reference<T> createReference(T fo) {
        return new FileObjRef<T>(fo);
    }

    private void refreshChildrenInAtomicAction(AbstractFolder fo, ResourceElem resElem) {
        try {
            beginAtomicAction();
            Collection<AbstractFolder> oldChildren = new HashSet<AbstractFolder>(Collections.list(fo.existingSubFiles(true)));
            
            refreshChildren(fo, resElem);
            
            Collection<AbstractFolder> newChildren = Collections.list(fo.existingSubFiles(true));
            oldChildren.removeAll(newChildren);
            for (Iterator<AbstractFolder> it = oldChildren.iterator(); it.hasNext();) {
                AbstractFileObject invalid = (AbstractFileObject)it.next();
                if (invalid.validFlag) {
                    invalid.validFlag = false;
                    invalid.fileDeleted0(new FileEvent(invalid));
                }
            }            
        } finally {
            finishAtomicAction();
        }
    }

    /** refreshes children recursively.*/
    private void refreshChildren(AbstractFolder fo, ResourceElem resElem) {
        if (fo.isRoot()) {
            initializeReference(rootRef = new FileObjRef<AbstractFolder>(fo), resElem);
        }

        java.util.List<String> nameList = resElem.getChildren();
        String[] names = new String[nameList.size()];
        ResourceElem[] children = new ResourceElem[names.length];

        nameList.toArray(names);

        for (int i = 0; i < names.length; i++)
            children[i] = resElem.getChild(names[i]);

        fo.refresh(null, null, true, true, names);

        for (int i = 0; i < children.length; i++) {
            AbstractFolder fo2 = (AbstractFolder) fo.getFileObject(names[i]);
            FileObjRef currentRef = (FileObjRef) findReference(fo2.getPath());
            int diff = initializeReference(currentRef, children[i]);
            fo2.lastModified();

            if (fo2.isFolder()) {
                refreshChildren(fo2, children[i]);
            } else {
                if ((diff & 0x01) != 0) {
                    fo2.fileChanged0(new FileEvent(fo2));
                } else {
                    if ((diff & 0x02) != 0) {
                        fo2.fileAttributeChanged0(new FileAttributeEvent(fo2, null, null, null));
                    }
                }
            }
        }
    }

    /** Initialize a reference with parsed element.
     * @param currentRef the reference
     * @param resElem the new element
     * @return ret&0x01 if content changed, ret&0x02 if attributes changed.
     */
    private int initializeReference(FileObjRef currentRef, ResourceElem resElem) {
        if (!currentRef.isInitialized()) {
            currentRef.initialize(resElem);
            return 0x00;
        } else {
            boolean attrDiff = currentRef.attacheAttrs(resElem.getAttr(false));
            currentRef.setUrlContext(resElem.getUrlContext());

            boolean diff = false;
            if (resElem.getContent() != null) {
                diff = !(currentRef.content instanceof byte[]) || !java.util.Arrays.equals((byte[])currentRef.content, resElem.getContent());
                currentRef.content = resElem.getContent();
            } else if (resElem.getURI() != null) {
                diff = !resElem.getURI().equals(currentRef.content);
                currentRef.content = resElem.getURI();
            }
            
            return (diff ? 0x01 : 0x00) + (attrDiff ? 0x02 : 0x00);
        }
    }
    
    /** Temporary hierarchical structure of resources. Used while parsing.*/
    private static class ResourceElem {
        private java.util.List<ResourceElem> children;
        private java.util.List<String> names;
        private byte[] content;
        private java.util.List<URL> urlContext = new ArrayList<URL>();
        private XMLMapAttr foAttrs;
        private boolean isFolder;
        private String uri;

        public ResourceElem(boolean isFolder, URL[] urlContext, String uri) {
            this.isFolder = isFolder;
            this.uri = uri;
            this.urlContext.addAll(Arrays.asList(urlContext));

            if (isFolder) {
                children = new ArrayList<ResourceElem>();
                names = new ArrayList<String>();
            }
        }

        public ResourceElem(boolean isFolder, URL urlContext, String uri) {
            this.isFolder = isFolder;
            this.uri = uri;
            this.urlContext.add(urlContext);

            if (isFolder) {
                children = new ArrayList<ResourceElem>();
                names = new ArrayList<String>();
            }
        }

        ResourceElem addChild(String name, ResourceElem child) {
            if (!isFolder) {
                throw new IllegalArgumentException("not a folder"); // NOI18N
            }

            ResourceElem retVal = child;
            int idx = names.indexOf(name);

            if (idx == -1) {
                names.add(name);
                children.add(child);
            } else {
                retVal = children.get(idx);
            }

            return retVal;
        }

        java.util.List<String> getChildren() {
            return names;
        }

        ResourceElem getChild(String name) {
            return children.get(names.indexOf(name));
        }

        XMLMapAttr getAttr(boolean create) {
            if (create && (foAttrs == null)) {
                foAttrs = new XMLMapAttr();
            }

            return foAttrs;
        }

        byte[] getContent() {
            return content;
        }

        URL[] getUrlContext() {
            URL[] retVal = new URL[urlContext.size()];
            urlContext.toArray(retVal);

            return retVal;
        }

        String getURI() {
            return uri;
        }

        void setContent(byte[] content) {
            if (this.content == null) {
                byte[] alloc = new byte[content.length];
                System.arraycopy(content, 0, alloc, 0, content.length);
                this.content = alloc;
            }
        }

        boolean isFolder() {
            return isFolder;
        }
    }

    //private void debugInfo(String dbgStr) { System.out.println(dbgStr);}

    /** Implementation of all interfaces List, Change, Info and Attr
     * that delegates to XMLFileSystem
     */
    public static class Impl extends Object implements AbstractFileSystem.List, AbstractFileSystem.Info,
        AbstractFileSystem.Change, AbstractFileSystem.Attr {
        /** generated Serialized Version UID */
        private static final long serialVersionUID = -67233358102597232L;

        /** the pointer to filesystem */
        private XMLFileSystem fs;

        /** Constructor.
         * @param fs the filesystem to delegate to
         */
        public Impl(XMLFileSystem fs) {
            this.fs = fs;
        }

        public String[] children(String name) {
            FileObject fo2name;

            if ((fo2name = fs.findResource(name)) == null) {
                return new String[] {  };
            }

            synchronized (fo2name) {
                return ((AbstractFolder) fo2name).getChildrenArray();
            }
        }

        //
        // Change
        //

        public void createFolder(String name) throws java.io.IOException {
            throw new IOException();
        }

        public void createData(String name) throws IOException {
            throw new IOException();
        }

        public void rename(String oldName, String newName)
        throws IOException {
            throw new IOException();
        }

        public void delete(String name) throws IOException {
            throw new IOException();
        }

        // Info

        public java.util.Date lastModified(String name) {
            return fs.lastModified(name);
        }

        public boolean folder(String name) {
            return fs.isFolder(name);
        }

        public boolean readOnly(String name) {
            return true;
        }

        public String mimeType(String name) {
            return null;
        }

        public long size(String name) {
            if (fs.isFolder(name)) {
                return 0;
            }

            return fs.getSize(name);
        }

        public InputStream inputStream(String name) throws java.io.FileNotFoundException {
            InputStream is = fs.getInputStream(name);

            if (is == null) {
                throw new java.io.FileNotFoundException(name);
            }

            return is;
        }

        public OutputStream outputStream(String name) throws java.io.IOException {
            throw new IOException();
        }

        public void lock(String name) throws IOException {
            FSException.io("EXC_CannotLock", name, fs.getDisplayName(), name); // NOI18N
        }

        public void unlock(String name) {
        }

        public void markUnimportant(String name) {
        }

        public Object readAttribute(String name, String attrName) {
            FileObjRef ref = (FileObjRef) fs.findReference(name);

            if ((ref == null) && (name.length() == 0) && (fs.rootRef != null)) {
                ref = fs.rootRef;
            }

            if (ref == null) {
                return null;
            }

            return ref.readAttribute(attrName);
        }

        public void writeAttribute(String name, String attrName, Object value)
        throws IOException {
            throw new IOException();
        }

        public Enumeration<String> attributes(String name) {
            FileObjRef<? extends FileObject> ref = (FileObjRef<? extends FileObject>) fs.findReference(name);

            if ((ref == null) && (name.length() == 0) && (fs.rootRef != null)) {
                ref = fs.rootRef;
            }

            if (ref == null) {
                return Enumerations.empty();
            }

            return ref.attributes();
        }

        public void renameAttributes(String oldName, String newName) {
        }

        public void deleteAttributes(String name) {
        }
    }

    /** Strong reference to FileObject. To FileObject may be attached attributes (XMLMapAttr)
     *  and info about if it is folder or not.
     */
    private static class FileObjRef<T extends FileObject> extends WeakReference<T> {
        private T fo;
        private Object content;
        private XMLMapAttr foAttrs;
        byte isFolder = -1;
        Object urlContext = null;

        public FileObjRef(T fo) {
            super(fo);
            this.fo = fo;
        }

        public boolean isInitialized() {
            return (isFolder != -1);
        }

        public void initialize(ResourceElem res) {
            content = res.getContent();

            XMLMapAttr tmp = res.getAttr(false);

            if ((tmp != null) && !tmp.isEmpty()) {
                foAttrs = tmp;
            }

            isFolder = (byte) (res.isFolder() ? 1 : 0);

            if (content == null) {
                content = res.getURI();
            }

            setUrlContext(res.getUrlContext());
        }

        public boolean isFolder() {
            return (isFolder == 1);
        }

        /** @return true if at lest one attribute changed */
        public boolean attacheAttrs(XMLMapAttr attrs) {
            if ((attrs == null) || attrs.isEmpty()) {
                return false;
            }

            if (foAttrs == null) {
                foAttrs = new XMLMapAttr();
            }

            Iterator it = attrs.entrySet().iterator();
            boolean ch = false;
            while (it.hasNext()) {
                Map.Entry attrEntry = (Map.Entry) it.next();
                Object prev = foAttrs.put(attrEntry.getKey(), attrEntry.getValue());
                
                ch |= (prev == null && attrEntry.getValue() != null) || !prev.equals(attrEntry.getValue());
            }
            
            return ch;
        }

        public void setUrlContext(URL[] ctx) {
            if (ctx.length > 0) {
                if (ctx.length > 1) {
                    urlContext = ctx;
                } else {
                    urlContext = ctx[0];
                }
            }
        }

        public Enumeration<String> attributes() {
            if (foAttrs == null) {
                return Enumerations.empty();
            } else {
                Set<String> s = new HashSet<String>(foAttrs.keySet());

                return Collections.enumeration(s);
            }
        }

        private URL[] getLayers() {
            if (urlContext == null) {
                return null;
            }

            if (urlContext instanceof URL[]) {
                return (URL[]) urlContext;
            }

            return new URL[] { (URL) urlContext };
        }

        public Object readAttribute(String attrName) {
            if (attrName.equals("layers")) { //NOI18N

                return getLayers();
            }

            if (foAttrs == null) {
                return null;
            }

            FileObject topFO = MultiFileObject.attrAskedFileObject.get();
            FileObject f = (topFO == null) ? fo : topFO;

            MultiFileObject.attrAskedFileObject.set(null);

            try {
                Object[] objs = new Object[] { f, attrName };

                return foAttrs.get(attrName, objs);
            } finally {
                MultiFileObject.attrAskedFileObject.set(topFO);
            }
        }

        /**
         * Get input stream.
         *
         * @return an input stream to read the contents of this file
         * @param context
         * @param name the file to test
         * @exception FileNotFoundException if the file does not exists or is invalid */
        public InputStream getInputStream(String name)
        throws java.io.FileNotFoundException {
            InputStream is = null;

            if (content == null) {
                return new ByteArrayInputStream(new byte[] {  });
            }

            if (content instanceof String) {
                URL absURL = createAbsoluteUrl(name);

                try {
                    is = absURL.openStream();
                } catch (IOException iox) {
                    FileNotFoundException x = new FileNotFoundException(name);
                    ExternalUtil.copyAnnotation(x, iox);
                    throw x;
                }
            }

            if (content instanceof byte[]) {
                is = new ByteArrayInputStream((byte[]) content);
            }

            if (is == null) {
                throw new FileNotFoundException(name);
            }

            return is;
        }

        private URL createAbsoluteUrl(String name) throws java.io.FileNotFoundException {
            if (!(content instanceof String)) {
                return null;
            }

            String uri = (String) content;

            try {
                URL[] uc = getLayers();
                URL retVal = ((uc == null) || (uc.length == 0)) ? new URL(uri) : new URL(uc[0], uri);

                return retVal;
            } catch (IOException ex) { // neni koser osetreni - RM

                FileNotFoundException x = new FileNotFoundException(name);
                ExternalUtil.copyAnnotation(x, ex);
                throw x;
            }
        }

        public long getSize(String name) {
            if (content == null) {
                return 0;
            }

            if (content instanceof byte[]) {
                return ((byte[]) content).length;
            }

            if (content instanceof String) {
                try {
                    URL absURL = createAbsoluteUrl(name);
                    URLConnection urlConnection = absURL.openConnection();

                    try {
                        return urlConnection.getContentLength();
                    } finally {
                        urlConnection.getInputStream().close();
                    }
                } catch (IOException iex) {
                }
            }

            return 0;
        }

        private static final Set<String> NETWORK_PROTOCOLS = new HashSet<String>(Arrays.asList("http", "https", "ftp")); // NOI18N
        public Date lastModified(String name) {
            URL url = null;
            Date retval = null;
            
            if ((content == null) || !(content instanceof String)) {
                URL[] all = getLayers();
                url = (all != null && all.length > 0) ? all[0] : null;
            } else {
                try {
                    url = createAbsoluteUrl(name);
                } catch (IOException iex) {
                    url = null;
                }
            }
            
            if (url != null) {
                String protocol = url.getProtocol();
                if ("jar".equals(protocol)) {//NOI18N
                    URL tmp = FileUtil.getArchiveFile(url);
                    url = (tmp != null) ? tmp : url;
                    protocol = url.getProtocol();
                }
                
                if ("file".equals(protocol)) { //NOI18N
                    File f = new File(URI.create(url.toExternalForm()));
                    retval = (f.exists()) ? new Date(f.lastModified()) : null;
                } else /* #96928 */ if (!NETWORK_PROTOCOLS.contains(protocol)) {
                    retval = timeFromDateHeaderField(url);
                }
            }
            
            if (retval == null) {
                retval = new Date(0);
            }
            
            return retval;
        }

        /** can return null*/
        private static String getLocalResource(URL url) {
            if (url == null) {
                return null;
            }

            if (url.getProtocol().equals("jar")) { // NOI18N

                URL testURL = null;

                try {
                    testURL = new URL(url.getFile());

                    if (testURL.getProtocol().equals("file")) { // NOI18N

                        return testURL.getFile();
                    }
                } catch (MalformedURLException mfx) {
                    return null;
                }
            }

            if (url.getProtocol().equals("file")) { // NOI18N

                return url.getFile();
            }

            return null;
        }

        /** can return null*/
        private static File getFileFromResourceString(String localResource) {
            if (localResource == null) {
                return null;
            }

            int idx = localResource.indexOf('!');
            String fileName = (idx != -1) ? localResource.substring(0, idx) : localResource;
            File f = new File(fileName);

            if (f.exists()) {
                return f;
            }

            return null;
        }

        private java.util.Date timeFromDateHeaderField(URL url) {
            URLConnection urlConn;

            try {
                urlConn = url.openConnection();

                try {
                    return new Date(urlConn.getLastModified());
                } finally {
                    urlConn.getInputStream().close();
                }
            } catch (IOException ie) {
                return new java.util.Date(0);
            }
        }

    }

    /** Class that can be used to parse XML document (Expects array of ElementHandler clasess).  Calls handler methods of ElementHandler clasess.
     */
    static class Handler extends DefaultHandler {
        private static final int FOLDER_CODE = "folder".hashCode(); // NOI18N
        private static final int FILE_CODE = "file".hashCode(); // NOI18N
        private static final int ATTR_CODE = "attr".hashCode(); // NOI18N
        private ResourceElem rootElem;
        private boolean validate = false;
        Stack<ResourceElem> resElemStack = new Stack<ResourceElem>();
        Stack<String> elementStack = new Stack<String>();
        URL urlContext;
        private Map dtdMap;
        private ResourceElem topRE;
        private StringBuffer pcdata = new StringBuffer();

        Handler(Map dtdMap, ResourceElem rootElem, boolean validate) {
            this.dtdMap = dtdMap;
            this.rootElem = rootElem;
            this.validate = validate;
        }

        public void error(SAXParseException exception)
        throws SAXException {
            throw exception;
        }

        public void warning(SAXParseException exception)
        throws SAXException {
            throw exception;
        }

        public void fatalError(SAXParseException exception)
        throws SAXException {
            throw exception;
        }

        public void startElement(String xmluri, String lname, String name, Attributes amap)
        throws SAXException {
            int controlCode = name.hashCode();

            elementStack.push(name);

            String foName = amap.getValue("name"); // NOI18N

            if (controlCode == FOLDER_CODE) {
                if (foName == null) {
                    throw new SAXException(NbBundle.getMessage(XMLFileSystem.class, "XML_MisssingAttr")); // NOI18N 
                }

                ResourceElem newRes = new ResourceElem(true, urlContext, null);

                topRE = topRE.addChild(foName, newRes);
                resElemStack.push(topRE);

                return;
            }

            if (controlCode == FILE_CODE) {
                if (foName == null) {
                    throw new SAXException(NbBundle.getMessage(XMLFileSystem.class, "XML_MisssingAttr")); // NOI18N 
                }

                foName = foName.intern();

                String uri = null;

                if (amap.getLength() > 1) {
                    uri = amap.getValue("url"); // NOI18N
                }

                ResourceElem newRes = new ResourceElem(false, urlContext, uri);

                topRE = topRE.addChild(foName, newRes);
                resElemStack.push(topRE);

                pcdata.setLength(0);

                return;
            }

            if (controlCode == ATTR_CODE) {
                if (foName == null) {
                    throw new SAXException(NbBundle.getMessage(XMLFileSystem.class, "XML_MisssingAttr")); // NOI18N 
                }

                int len = amap.getLength();

                for (int i = 0; i < len; i++) {
                    String key = amap.getQName(i);
                    String value = amap.getValue(i);

                    if (XMLMapAttr.Attr.isValid(key) != -1) {
                        XMLMapAttr.Attr attr = XMLMapAttr.createAttributeAndDecode(key, value);
                        XMLMapAttr attrMap = topRE.getAttr(true);
                        Object retVal = attrMap.put(foName, attr);

                        if (retVal != null) {
                            attrMap.put(foName, retVal);
                        }
                    }
                }

                return;
            }
        }

        public void endElement(String uri, String lname, String name) throws SAXException {
            if ((elementStack.peek().hashCode() == FILE_CODE) && !topRE.isFolder()) {
                String string = pcdata.toString().trim();

                if (string.length() > 0) {
                    topRE.setContent(string.getBytes());
                }

                pcdata.setLength(0);
            }

            int controlCode = name.hashCode();

            elementStack.pop();

            if ((controlCode == FOLDER_CODE) || (controlCode == FILE_CODE)) {
                resElemStack.pop();
                topRE = resElemStack.peek();

                return;
            }
        }

        public void characters(char[] ch, int start, int length)
        throws SAXException {
            if (elementStack.peek().hashCode() != FILE_CODE) {
                return;
            }

            if (topRE.isFolder()) {
                return;
            }

            pcdata.append(new String(ch, start, length));
        }

        public InputSource resolveEntity(String pid, String sid)
        throws SAXException {
            String publicURL = (String) dtdMap.get(pid);

            if (publicURL != null) {
                if (validate) {
                    publicURL = getClass().getClassLoader().getResource(publicURL).toExternalForm();

                    return new InputSource(publicURL);
                } else {
                    return new InputSource(new ByteArrayInputStream(new byte[0]));
                }
            }

            return new InputSource(sid);
        }

        public void startDocument() throws SAXException {
            super.startDocument();
            resElemStack = new Stack<ResourceElem>();
            resElemStack.push(rootElem);
            topRE = rootElem;

            elementStack = new Stack<String>();
            elementStack.push("<root>"); // NOI18N
        }

        public void endDocument() throws SAXException {
            super.endDocument();
            resElemStack.pop();
            elementStack.pop();
        }
    }
}
