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

package org.netbeans.core.startup.layers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import org.openide.filesystems.FileSystem;
import org.openide.util.NotImplementedException;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/** A cache manager which parses the layers according to the Filesystems 1.x DTDs.
 * This class just handles the parsing during cache rewrite time; subclasses are
 * responsible for loading from and saving to the storage format.
 * @author Jesse Glick
 */
abstract class ParsingLayerCacheManager extends LayerCacheManager implements ContentHandler, ErrorHandler, EntityResolver {
    
    private final static String[] ATTR_TYPES = {
        "boolvalue",
        "bytevalue",
        "charvalue",
        "doublevalue",
        "floatvalue",
        "intvalue",
        "longvalue",
        "methodvalue",
        "newvalue",
        "serialvalue",
        "shortvalue",
        "stringvalue",
        "urlvalue"
    };
    
    private final static String DTD_1_0 = "-//NetBeans//DTD Filesystem 1.0//EN";
    private final static String DTD_1_1 = "-//NetBeans//DTD Filesystem 1.1//EN";
    
    private Locator locator;
    private MemFolder root;
    private Stack<Object> curr; // Stack<MemFileOrFolder | MemAttr>
    private URL base;
    private StringBuffer buf = new StringBuffer();
    private int fileCount, folderCount, attrCount;
    // Folders, files, and attrs already encountered in this layer.
    // By path; attrs as folder/file path plus "//" plus attr name.
    private Set<String> oneLayerFiles; // Set<String>
    // Related:
    private boolean checkingForDuplicates;
    private String currPath;
    private boolean atLeastOneFileOrFolderInLayer;

    /** Constructor for subclasses.
     */
    protected ParsingLayerCacheManager() {
    }
    
    /** Implements storage by parsing the layers and calling
     * store(FileSystem,ParsingLayerCacheManager.MemFolder).
     */
    @Override
    public final void store(FileSystem fs, List<URL> urls, OutputStream os) throws IOException {
        store(fs, createRoot(urls), os);
    }
    
    /**
     * Do the actual parsing.
     */
    private MemFolder createRoot(List<URL> urls) throws IOException {
        root = new MemFolder(null);
        curr = new Stack<Object>();
        curr.push(root);
        try {
            // XMLReader r = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            XMLReader r = XMLUtil.createXMLReader();
            // Speed enhancements.
            // XXX these are not really necessary; OK to run validation here!
            r.setFeature("http://xml.org/sax/features/validation", false);
            r.setFeature("http://xml.org/sax/features/namespaces", false);
            // XXX this is not standard, should not rely on it:
            r.setFeature("http://xml.org/sax/features/string-interning", true);
            r.setContentHandler(this);
            r.setErrorHandler(this);
            r.setEntityResolver(this);
            Exception carrier = null;
            // #23609: reverse these...
            urls = new ArrayList<URL>(urls);
            Collections.reverse(urls);
            checkingForDuplicates = LayerCacheManager.err.isLoggable(Level.FINE);
            Iterator<URL> it = urls.iterator();
            while (it.hasNext()) {
                base = it.next(); // store base for resolving in parser
                if (checkingForDuplicates) {
                    oneLayerFiles = new HashSet<String>(100);
                    currPath = null;
                }
                LayerCacheManager.err.log(Level.FINE, "Parsing: {0}", base);
                atLeastOneFileOrFolderInLayer = false;
                try {
                    r.parse(base.toExternalForm());
                    if (!atLeastOneFileOrFolderInLayer) {
                        LayerCacheManager.err.log(Level.WARNING, "Inefficient to include an empty layer in a module: {0}", base);
                    }
                } catch (Exception e) {
                    curr.clear();
                    curr.push(root);
                    LayerCacheManager.err.fine("Caught " + e + " while parsing: " + base);
                    if (carrier == null) {
                        carrier = e;
                    } else {
                        Throwable t = carrier;
                        while (t.getCause() != null) {
                            t = t.getCause();
                        }
                        t.initCause(e);
                    }
                }
            }
            if (carrier != null) throw carrier;
            LayerCacheManager.err.fine("Finished layer parsing; " + fileCount + " files, " + folderCount + " folders, " + attrCount + " attributes");
            return root;
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            throw (IOException) new IOException("While parsing " + base + ": " + e).initCause(e);
        } finally {
            fileCount = folderCount = attrCount = 0;
            base = null;
            locator = null;
            curr = null;
            root = null;
            oneLayerFiles = null;
            currPath = null;
        }
    }

    /** Delegated storage method supplied with a merged layer parse.
     * Not called if the manager does not support loading;
     * otherwise must be overridden.
     */
    protected abstract void store(FileSystem fs, MemFolder root, OutputStream os) throws IOException;
    
    /** If true, file content URLs should be opened and the contents extracted,
     * if they are of an appropriate type (locally stored). If false, the original
     * URLs should be left alone.
     */
    protected abstract boolean openURLs();
    
    public void startElement(String ns, String lname, String qname, Attributes attrs) throws SAXException {
        if (qname.equals("filesystem")) {
            return;
        } else if (qname.equals("folder")) {
            fileOrFolder(qname, attrs);
        } else if (qname.equals("file")) {
            MemFile file = (MemFile)fileOrFolder(qname, attrs);
            file.contents = null;
            String u = attrs.getValue("url");
            if (u != null) {
                try {
                    file.ref = new URL(base, u);
                } catch (MalformedURLException mfue) {
                    throw (SAXException) new SAXException(mfue.toString()).initCause(mfue);
                }
            } else {
                file.ref = null;
            }
        } else if (qname.equals("attr")) {
            attrCount++;
            MemAttr attr = new MemAttr();
            int len = attrs.getLength();
            for (int i = 0; i < len; i++) {
                String attrName = attrs.getQName(i);
                if ("name".equals(attrName)) {
                    attr.name = attrs.getValue(i);
                }
                else {
                    int idx = Arrays.binarySearch(ATTR_TYPES, attrName);
                    if (idx >= 0) {
                        attr.type = ATTR_TYPES[idx];
                        attr.data = attrs.getValue(i);
                    }
                }
                if (attr.name != null && attr.data != null) {
                    break;
                }
            }
//            System.out.println("found attr "+attr);
            /*
            attr.name = attrs.getValue("name");
            for (int i = 0; i < ATTR_TYPES.length; i++) {
                String v = attrs.getValue(ATTR_TYPES[i]);
                if (v != null) {
                    attr.type = ATTR_TYPES[i];
                    attr.data = v;
                    break;
                }
            }
             */
            if (attr.type == null) throw new SAXParseException("unknown <attr> value type for " + attr.name, locator);
            MemFileOrFolder parent = (MemFileOrFolder)curr.peek();
            if (parent.attrs == null) parent.attrs = new LinkedList<MemAttr>();
            Iterator it = parent.attrs.iterator();
            while (it.hasNext()) {
                if (((MemAttr)it.next()).name.equals(attr.name)) {
                    attrCount--;
                    it.remove();
                }
            }
            parent.attrs.add(attr);
            if (checkingForDuplicates && !oneLayerFiles.add(currPath + "//" + attr.name)) { // NOI18N
                LayerCacheManager.err.warning("layer " + base + " contains duplicate attributes " + attr.name + " for " + currPath);
            }
        } else {
            throw new SAXException(qname);
        }
    }
    
    private MemFileOrFolder fileOrFolder(String qname, Attributes attrs) {
        atLeastOneFileOrFolderInLayer = true;
        String name = attrs.getValue("name");
        if (name == null) throw new NullPointerException("No name"); // NOI18N
        if (!(curr.peek() instanceof MemFolder)) throw new ClassCastException("Stack: " + curr); // NOI18N
        MemFolder parent = (MemFolder)curr.peek();
        MemFileOrFolder f = null;
        if (parent.children == null) {
            parent.children = new LinkedList<MemFileOrFolder>();
        }
        else {
            Iterator it = parent.children.iterator();
            while (it.hasNext()) {
                MemFileOrFolder f2 = (MemFileOrFolder)it.next();
                if (f2.name.equals(name)) {
                    f = f2;
                    break;
                }
            }
        }
        if (f == null) {
            if (qname.equals("folder")) { // NOI18N
                f = new MemFolder(base);
                folderCount++;
            } else {
                f = new MemFile(base);
                fileCount++;
            }
            f.name = name;
            parent.children.add(f);
        }
        curr.push(f);
        if (checkingForDuplicates) {
            if (currPath == null) {
                currPath = name;
            } else {
                currPath += "/" + name;
            }
            if (!oneLayerFiles.add(currPath)) {
                LayerCacheManager.err.warning("layer " + base + " contains duplicate " + qname + "s named " + currPath);
            }
        }
        return f;
    }
    
    public void endElement(String ns, String lname, String qname) throws SAXException {
        if (qname.equals("file") && buf.length() > 0) {
            String text = buf.toString().trim();
            if (text.length() > 0) {
                MemFile file = (MemFile)curr.peek();
                if (file.ref != null) throw new SAXParseException("CDATA plus url= in <file>", locator);
                LayerCacheManager.err.warning("use of inline CDATA text contents in <file name=\"" + file.name + "\"> deprecated for performance and charset safety at " + locator.getSystemId() + ":" + locator.getLineNumber() + ". Please use the 'url' attribute instead, or the file attribute 'originalFile' on *.shadow files.");
                // Note: platform default encoding used. If you care about the encoding,
                // you had better be using url= instead.
                file.contents = text.getBytes();
            }
            buf.setLength(0);
        }
        if (qname.equals("file") && openURLs()) {
            MemFile file = (MemFile)curr.peek();
            // Only open simple URLs. Assume that JARs are the same JARs with the layers.
            if (file.ref != null && file.ref.toExternalForm().startsWith("jar:file:")) { // NOI18N
                try {
                    URLConnection conn = file.ref.openConnection();
                    conn.connect();
                    byte[] readBuf = new byte[conn.getContentLength()];
                    InputStream is = conn.getInputStream();
                    try {
                        int pos = 0;
                        while (pos < readBuf.length) {
                            int read = is.read(readBuf, pos, readBuf.length - pos);
                            if (read < 1) throw new IOException("Premature EOF on " + file.ref.toExternalForm()); // NOI18N
                            pos += read;
                        }
                        if (is.read() != -1) throw new IOException("Delayed EOF on " + file.ref.toExternalForm()); // NOI18N
                    } finally {
                        is.close();
                    }
                    file.contents = readBuf;
                    file.ref = null;
                } catch (IOException ioe) {
                    throw new SAXException(ioe);
                }
            }
        }
        if (qname.equals("file") || qname.equals("folder")) { // NOI18N
            curr.pop();
            if (checkingForDuplicates) {
                int i = currPath.lastIndexOf('/'); // NOI18N
                if (i == -1) {
                    currPath = null;
                } else {
                    currPath = currPath.substring(0, i);
                }
            }
        }
    }
    
    public void characters(char[] ch, int start, int len) throws SAXException {
        Object currF = curr.peek();
        if (!(currF instanceof MemFile)) {
            return;
        }
        // Usually this will just be whitespace which we will ignore anyway.
        // Do it anyway, so that people who accidentally write:
        // <file name="x" url="y"><![CDATA[z]]></file>
        // will at least get an error.
        buf.append(ch, start, len);
    }
    
    public void warning(SAXParseException e) throws SAXException {
        LayerCacheManager.err.log(Level.WARNING, null, e);
    }
    
    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }
    
    public void error(SAXParseException e) throws SAXException {
        throw e;
    }
    
    public InputSource resolveEntity(String pubid, String sysid) throws SAXException, IOException {
        if (pubid != null && (pubid.equals(DTD_1_0) || pubid.equals(DTD_1_1))) {
            return new InputSource(new ByteArrayInputStream(new byte[0]));
        } else {
            return null;
        }
    }
    
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }
    
    public void endDocument() throws SAXException {
        if (curr.size() != 1) throw new SAXException("Wrong stack: " + curr); // NOI18N
    }
    
    public void startDocument() throws SAXException {}
    
    public void startPrefixMapping(String str, String str1) throws SAXException {}
    
    public void skippedEntity(String str) throws SAXException {}
    
    public void processingInstruction(String str, String str1) throws SAXException {}
    
    public void ignorableWhitespace(char[] values, int param, int param2) throws SAXException {}
    
    public void endPrefixMapping(String str) throws SAXException {}

    /** Struct for <file> or <folder>.
     */
    protected static abstract class MemFileOrFolder {
        public String name;
        public List<MemAttr> attrs = null; // {null | List<MemAttr>}
        public final URL base;
        
        public MemFileOrFolder (URL base) {
            this.base = base;
        }
    }
    
    /** Struct for <folder>.
     */
    protected static final class MemFolder extends MemFileOrFolder {
        public List<MemFileOrFolder> children = null;
        
        public MemFolder (URL base) {
            super (base);
        }
        
        @Override
        public String toString() {
            return "MemFolder[" + name + "]"; // NOI18N
        }
    }
    
    /** Struct for <file>.
     */
    protected static final class MemFile extends MemFileOrFolder {
        public byte[] contents = null; // {null | byte[]}
        public URL ref = null; // {null | URL}
        
        public MemFile (URL base) {
            super (base);
        }
        
        @Override
        public String toString() {
            return "MemFile[" + name + "]"; // NOI18N
        }
    }
    
    /** Struct for &lt;attr&gt;.
     */
    protected static final class MemAttr {
        public String name;
        public String type;
        public String data;
        @Override
        public String toString() {
            return "MemAttr[" + name + "," + type + "," + data + "]"; // NOI18N
        }
    }
    
}
