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

package org.netbeans.modules.apisupport.project.layers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.tax.InvalidArgumentException;
import org.netbeans.tax.ReadOnlyException;
import org.netbeans.tax.TreeAttribute;
import org.netbeans.tax.TreeCDATASection;
import org.netbeans.tax.TreeChild;
import org.netbeans.tax.TreeDocumentRoot;
import org.netbeans.tax.TreeElement;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeObjectList;
import org.netbeans.tax.TreeParentNode;
import org.netbeans.tax.TreeText;
import org.openide.ErrorManager;
import org.openide.filesystems.AbstractFileSystem;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Enumerations;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

// XXX use doc.getRootEventManager().setFirePolicy(TreeEventManager.FIRE_{LATER,NOW})

/**
 * A filesystem which is based on a TAX document and implements
 * the same syntax as XMLFileSystem, from which inspiration is taken.
 * Not implemented similarly to XMLFileSystem because this is writable
 * and designed specifically to write human-readable XML and work nicely
 * as an authoring tool. The filesystem expects to get an XML document
 * according to DTD "-//NetBeans//DTD Filesystem 1.0//EN" (or 1.1 is OK).
 * When it is changed via FileSystems API, it will fire TAX
 * events. Not intended to be efficient or terribly robust, since it
 * is development-time only. Also displays annotations just like SystemFileSystem
 * does, more or less, to help test localized file names.
 * @author Jesse Glick
 */
final class WritableXMLFileSystem extends AbstractFileSystem
        implements AbstractFileSystem.Attr,
        AbstractFileSystem.Change,
        AbstractFileSystem.Info,
        AbstractFileSystem.List,
        AbstractFileSystem.Transfer,
        FileChangeListener,
        PropertyChangeListener {
    
    private final TreeEditorCookie cookie;
    private TreeDocumentRoot doc; // may be null if malformed
    private URL location;
    private String suffix; // for branding/localization like "_f4j_ce_ja"; never null, at worst ""
    private final FileChangeListener fileChangeListener;
    private ClassPath classpath; // OK to be null
    private final BadgingSupport status;
    
    public WritableXMLFileSystem(URL location, TreeEditorCookie cookie, boolean badging) {
        this.attr = this;
        this.change = this;
        this.info = this;
        this.list = this;
        this.transfer = this;
        this.cookie = cookie;
        try {
            doc = cookie.openDocumentRoot();
        } catch (TreeException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        fileChangeListener = FileUtil.weakFileChangeListener(this, null);
        if (badging) {
            status = new BadgingSupport(this);
            status.addFileStatusListener(new FileStatusListener() {
                public void annotationChanged(FileStatusEvent ev) {
                    fireFileStatusChanged(ev);
                }
            });
        } else {
            status = null;
        }
        cookie.addPropertyChangeListener(WeakListeners.propertyChange(this, cookie));
        setLocation(location);
    }
    
    public FileSystem.Status getStatus() {
        return status != null ? status : super.getStatus();
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException("WritableXMLFileSystem is not persistent");
    }
    
    public void setLocation(URL location) {
        String u = location.toExternalForm();
        if (u.endsWith("/")) {
            throw new IllegalArgumentException(u);
        }
        this.location = location;
        if (status != null) {
            Matcher m = Pattern.compile("(.*/)?[^_/.]+(_[^/.]+)?(\\.[^/]+)?").matcher(u);
            assert m.matches() : u;
            suffix = m.group(2);
            if (suffix == null) {
                suffix = "";
            }
            status.setSuffix(suffix);
        }
    }
    
    public void setClasspath(ClassPath classpath) {
        this.classpath = classpath;
        if (status != null) {
            status.setClasspath(classpath);
        }
    }
    
    public String getDisplayName() {
        FileObject fo = URLMapper.findFileObject(location);
        if (fo != null) {
            return FileUtil.getFileDisplayName(fo);
        } else {
            return location.toExternalForm();
        }
    }
    
    public boolean isReadOnly() {
        return false;
    }
    
    private TreeElement getRootElement() {
        if (doc == null) {
            return null;
        }
        Iterator it;
        it = doc.getChildNodes().iterator();
        while (it.hasNext()) {
            Object next = it.next();
            if (next instanceof TreeElement) {
                return (TreeElement) next;
            }
        }
        return null;
    }
    
    /** Given a resource name, find the matching DOM element.
     * @return a <folder> or <file> or <filesystem> element, or null if file does not exist
     */
    private TreeElement findElement(String name) {
        return findElementIn(getRootElement(), name);
    }
    /** helper method only */
    private static TreeElement findElementIn(TreeElement el, String name) {
        if (el == null) return null;
        if (name.equals("")) { // NOI18N
            return el;
        } else {
            int idx = name.indexOf((char) '/');
            String nextName, remainder;
            if (idx == -1) {
                nextName = name;
                remainder = ""; // NOI18N
            } else {
                nextName = name.substring(0, idx);
                remainder = name.substring(idx + 1);
            }
            TreeElement subel = null;
            Iterator it = el.getChildNodes(TreeElement.class).iterator();
            while (it.hasNext()) {
                TreeElement e = (TreeElement) it.next();
                if (e.getLocalName().equals("file") || // NOI18N
                        e.getLocalName().equals("folder")) { // NOI18N
                    TreeAttribute attr = e.getAttribute("name");
                    if (attr != null && attr.getValue().equals(nextName)) {
                        subel = e;
                        break;
                    }
                }
            }
            return findElementIn(subel, remainder);
        }
    }
    
    public boolean folder(String name) {
        TreeElement el = findElement(name);
        if (el == null) {
            //System.err.println("folder <" + name + ">: false, no such element");
            return false;
        }
        boolean res = el.getLocalName().equals("folder"); // NOI18N
        //System.err.println("folder <" + name + ">: " + res);
        return res;
    }
    
    private static final Set warnedAboutDupeKids = new HashSet(1); // Set<String>
    public String[] children(String f) {
        TreeElement el = findElement(f);
        if (el == null) {
            //System.err.println("children <" + f + ">: none, no such element");
            return new String[] {};
        }
        Iterator it = el.getChildNodes(TreeElement.class).iterator();
        ArrayList kids = new ArrayList(); // List<String>
        Set allNames = new HashSet(); // Set<String>
        while (it.hasNext()) {
            TreeElement sub = (TreeElement) it.next();
            if (sub.getLocalName().equals("file") || // NOI18N
                    sub.getLocalName().equals("folder")) { // NOI18N
                TreeAttribute attr = sub.getAttribute("name");
                if (attr == null) {
                    continue;
                }
                String name = attr.getValue(); // NOI18N
                if (allNames.add(name)) {
                    kids.add(name);
                } else {
                    if (warnedAboutDupeKids.add(location + ":" + f + "/" + name)) { // NOI18N
                        // #18699: will deadlock if you try to change anything.
                        if (f.equals("")) { // NOI18N
                            //LayerDataNode.getErr().println("WARNING: in " + xmlfile + " the root folder contains the child " + name + " more than once.");
                        } else {
                            //LayerDataNode.getErr().println("WARNING: in " + xmlfile + " the folder " + f + " contains the child " + name + " more than once.");
                        }
                        //LayerDataNode.getErr().println("The Open APIs Support module will not work properly with such a layer.");
                        //LayerDataNode.getErr().println("Please edit the XML text and merge together all children of a <folder> with the same name.");
                    }
                }
            }
        }
        //System.err.println("children <" + f + ">: " + kids);
        return (String[]) kids.toArray(new String[kids.size()]);
    }
    
    /** retrieve byte contents of a named resource */
    private byte[] getContentsOf(final String name) throws FileNotFoundException {
        TreeElement el = findElement(name);
        if (el == null) throw new FileNotFoundException(name);
        TreeAttribute attr = el.getAttribute("url"); // NOI18N
        if (attr != null) {
            try {
                URL u = LayerUtils.currentify(new URL(location, attr.getValue()), suffix, classpath);
                URLConnection conn = u.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                byte[] buf = new byte[conn.getContentLength()];
                if (is.read(buf) != buf.length) throw new IOException("wrong content length");
                // Also listen to changes in it.
                FileObject fo = URLMapper.findFileObject(u);
                if (fo != null) {
                    fo.removeFileChangeListener(fileChangeListener);
                    fo.addFileChangeListener(fileChangeListener);
                }
                return buf;
            } catch (IOException ioe) {
                throw new FileNotFoundException(ioe.getMessage());
            }
        } else {
            StringBuffer buf = new StringBuffer();
            Iterator it = el.getChildNodes().iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof TreeCDATASection) {
                    buf.append(((TreeCDATASection) o).getData());
                } else if (o instanceof TreeText) {
                    buf.append(((TreeText) o).getData().trim());
                }
            }
            try {
                // This encoding is intentional...
                return buf.toString().getBytes("UTF-8"); // NOI18N
            } catch (UnsupportedEncodingException uee) {
                throw new FileNotFoundException(uee.getMessage());
            }
        }
    }
    
    // [PENDING] should I/O from/to external text files be done via EditorCookie?
    // Not clear if this is safe (call from FS -> DS) even tho in separate FSs...
    
    public InputStream inputStream(String name) throws FileNotFoundException {
        return new ByteArrayInputStream(getContentsOf(name));
    }
    
    public OutputStream outputStream(final String name) throws IOException {
        final TreeElement el = findElement(name);
        if (el == null) {
            throw new FileNotFoundException(name);
        }
        TreeAttribute attr = el.getAttribute("url"); // NOI18N
        if (attr != null) {
            String u = attr.getValue();
            if (URI.create(u).isAbsolute()) {
                // What to do? Can't overwrite it, obviously.
                throw new IOException(name);
            }
            // We have an existing external file. Try to write to that instead.
            FileObject external = URLMapper.findFileObject(new URL(location, u));
            if (external == null) {
                throw new FileNotFoundException(name);
            }
            final FileLock lock = external.lock();
            return new FilterOutputStream(external.getOutputStream(lock)) {
                public void close() throws IOException {
                    super.close();
                    lock.releaseLock();
                }
            };
        }
        // We will change the layer file.
        return new ByteArrayOutputStream() {
            public void close() throws IOException {
                super.close();
                byte[] contents = toByteArray();
                /* If desired to kill any existing inline content:
                Iterator it = el.getChildNodes().iterator();
                ArrayList/ *<TreeCDATASection>* / allCdata = new ArrayList();
                while (it.hasNext()) {
                    Object o = it.next();
                    if (o instanceof TreeCDATASection) {
                        allCdata.add(o);
                    } else if (o instanceof TreeText &&
                            ((TreeText) o).getData().trim().length() > 0) {
                        el.removeChild((TreeText) o);
                    }
                }
                Iterator it = allCdata.iterator();
                while (it.hasNext()) {
                    el.removeChild((CDATASection) it.next());
                }
                 */
                FileObject parent = findLayerParent();
                String externalName = LayerUtils.findGeneratedName(parent, name);
                assert externalName.indexOf('/') == -1 : externalName;
                FileObject externalFile = parent.createData(externalName);
                FileLock lock = externalFile.lock();
                try {
                    OutputStream os = externalFile.getOutputStream(lock);
                    try {
                        os.write(contents);
                    } finally {
                        os.close();
                    }
                } finally {
                    lock.releaseLock();
                }
                externalFile.addFileChangeListener(fileChangeListener);
                try {
                    el.addAttribute("url", externalName); // NOI18N
                } catch (ReadOnlyException e) {
                    throw (IOException) new IOException(e.toString()).initCause(e);
                } catch (InvalidArgumentException e) {
                    assert false : e;
                }
            }
        };
    }
    private FileObject findLayerParent() throws IOException {
        String loc = location.toExternalForm();
        int slash = loc.lastIndexOf('/');
        assert slash != -1 : loc;
        FileObject parent = URLMapper.findFileObject(new URL(loc.substring(0, slash + 1)));
        if (parent == null) {
            throw new IOException(loc);
        }
        return parent;
    }
    
    private void createFileOrFolder(String name, boolean folder) throws IOException {
        String parentName, baseName;
        int idx = name.lastIndexOf('/');
        if (idx == -1) {
            parentName = ""; // NOI18N
            baseName = name;
        } else {
            parentName = name.substring(0, idx);
            baseName = name.substring(idx + 1);
        }
        TreeElement el = findElement(parentName);
        if (el == null) {
            throw new FileNotFoundException(parentName);
        }
        try {
            TreeElement nue = new TreeElement(folder ? "folder" : "file", true); // NOI18N
            nue.addAttribute("name", baseName); // NOI18N
            appendWithIndent(el, nue);
        } catch (InvalidArgumentException e) {
            assert false : e;
        } catch (ReadOnlyException e) {
            throw (IOException) new IOException(e.toString()).initCause(e);
        }
    }
    
    public void createFolder(String name) throws IOException {
        createFileOrFolder(name, true);
    }
    
    public void createData(String name) throws IOException {
        createFileOrFolder(name, false);
    }
    
    public void delete(String name) throws IOException {
        TreeElement el = findElement(name);
        if (el == null) {
            throw new FileNotFoundException(name);
        }
        TreeAttribute externalName = el.getAttribute("url");
        if (externalName != null && !URI.create(externalName.getValue()).isAbsolute()) {
            // Delete the external file if it can be found.
            FileObject externalFile = URLMapper.findFileObject(new URL(location, externalName.getValue()));
            if (externalFile != null) {
                externalFile.removeFileChangeListener(fileChangeListener);
                externalFile.delete();
            }
        }
        try {
            deleteWithIndent((TreeChild) el);
        } catch (ReadOnlyException e) {
            throw (IOException) new IOException(e.toString()).initCause(e);
        }
    }
    
    public void rename(String oldName, String newName) throws IOException {
        /*
        Element el = findElement(oldName);
        if (el == null) throw new FileNotFoundException(oldName);
        int idx = newName.lastIndexOf('/');
        if (idx != -1) newName = newName.substring(idx + 1);
        el.setAttribute("name", newName); // NOI18N
         */
        throw new UnsupportedOperationException("XXX");
    }
    
    public boolean copy(String name, Transfer target, String targetName) throws IOException {
        /*
        if (! (target instanceof WritableXMLFileSystem)) return false;
        WritableXMLFileSystem otherfs = (WritableXMLFileSystem) target;
        Element el = findElement(name);
        if (el == null) throw new FileNotFoundException(name);
        Element el2;
        if (otherfs == this) {
            el2 = (Element) el.cloneNode(true);
        } else {
            el2 = (Element) otherfs.doc.importNode(el, true);
        }
        String path, base;
        int idx = targetName.lastIndexOf('/');
        if (idx == -1) {
            path = ""; // NOI18N
            base = targetName;
        } else {
            path = targetName.substring(0, idx);
            base = targetName.substring(idx + 1);
        }
        Element parent = otherfs.findElement(path);
        if (parent == null) throw new FileNotFoundException(path);
        el2.setAttribute("name", base); // NOI18N
        Element old = otherfs.findElement(targetName);
        if (old != null) {
            parent.replaceChild(el2, old);
        } else {
            appendWithIndent(parent, el2);
        }
        return true;
         */
        throw new UnsupportedOperationException("XXX");
    }
    
    public boolean move(String name, Transfer target, String targetName) throws IOException {
        /*
        if (! (target instanceof WritableXMLFileSystem)) return false;
        WritableXMLFileSystem otherfs = (WritableXMLFileSystem) target;
        Element el = findElement(name);
        if (el == null) throw new FileNotFoundException(name);
        Element el2;
        if (otherfs == this) {
            // Just move it, no need to clone.
            el2 = el;
        } else {
            el2 = (Element) otherfs.doc.importNode(el, true);
        }
        String path, base;
        int idx = targetName.lastIndexOf('/');
        if (idx == -1) {
            path = ""; // NOI18N
            base = targetName;
        } else {
            path = targetName.substring(0, idx);
            base = targetName.substring(idx + 1);
        }
        Element parent = otherfs.findElement(path);
        if (parent == null) throw new FileNotFoundException(path);
        el2.setAttribute("name", base); // NOI18N
        Element old = otherfs.findElement(targetName);
        if (el != el2) {
            // Cross-document import, so need to remove old one.
            el.getParentNode().removeChild(el);
        }
        if (old != null) {
            parent.replaceChild(el2, old);
        } else {
            appendWithIndent(parent, el2);
        }
        return true;
         */
        throw new UnsupportedOperationException("XXX");
    }
    
    public Enumeration attributes(String name) {
        TreeElement el = findElement(name);
        if (el == null) {
            return Enumerations.empty();
        }
        java.util.List/*<String>*/ l = new ArrayList(10);
        Iterator it = el.getChildNodes(TreeElement.class).iterator();
        while (it.hasNext()) {
            TreeElement sub = (TreeElement) it.next();
            if (sub.getLocalName().equals("attr")) { // NOI18N
                TreeAttribute attr = sub.getAttribute("name"); // NOI18N
                if (attr == null) {
                    // Malformed.
                    continue;
                }
                l.add(attr.getValue());
            }
        }
        return Collections.enumeration(l);
    }
    
    public Object readAttribute(String name, String attrName) {
        if (attrName.equals("WritableXMLFileSystem.cp")) { // NOI18N
            // Special access for MiscPropEds, so it knows what classpath to work with.
            // XXX should probably use some extension interface?
            return classpath;
        }
        TreeElement el = findElement(name);
        if (el == null) {
            return null;
        }
        Iterator it = el.getChildNodes(TreeElement.class).iterator();
        while (it.hasNext()) {
            TreeElement sub = (TreeElement) it.next();
            if (!sub.getLocalName().equals("attr")) { // NOI18N
                continue;
            }
            TreeAttribute attr = sub.getAttribute("name"); // NOI18N
            if (attr == null) {
                // Malformed.
                continue;
            }
            if (!attrName.equals(attr.getValue())) {
                continue;
            }
            try {
                if ((attr = sub.getAttribute("stringvalue")) != null) { // NOI18N
                    // Stolen from XMLMapAttr, with tweaks:
                    String inStr = attr.getValue();
                    StringBuffer outStr = new StringBuffer(inStr.length());
                    for (int j = 0; j < inStr.length(); j++) {
                        char ch = inStr.charAt(j);
                        if (ch == '\\' && inStr.charAt(j + 1) == 'u' && j + 5 < inStr.length()) {
                            String hex = inStr.substring(j + 2, j + 6);
                            try {
                                outStr.append((char) Integer.parseInt(hex, 16));
                                j += 5;
                            } catch (NumberFormatException e) {
                                // OK, just treat as literal text.
                                outStr.append(ch);
                            }
                        } else {
                            outStr.append(ch);
                        }
                    }
                    return outStr.toString();
                } else if ((attr = sub.getAttribute("boolvalue")) != null) { // NOI18N
                    return Boolean.valueOf(attr.getValue());
                } else if ((attr = sub.getAttribute("urlvalue")) != null) { // NOI18N
                    return new URL(attr.getValue());
                }
            } catch (Exception e) {
                // MalformedURLException, etc.
                // XXX notify?
                return null;
            }
            // XXX warn that this attr had no recognized *value?
        }
        return null;
        /*
                        if ((v = sub.getAttributeNode("bytevalue")) != null) { // NOI18N
                            return new Byte(v.getValue());
                        } else if ((v = sub.getAttributeNode("shortvalue")) != null) { // NOI18N
                            return new Short(v.getValue());
                        } else if ((v = sub.getAttributeNode("intvalue")) != null) { // NOI18N
                            return new Integer(v.getValue());
                        } else if ((v = sub.getAttributeNode("longvalue")) != null) { // NOI18N
                            return new Long(v.getValue());
                        } else if ((v = sub.getAttributeNode("floatvalue")) != null) { // NOI18N
                            return new Float(v.getValue());
                        } else if ((v = sub.getAttributeNode("doublevalue")) != null) { // NOI18N
                            // When was the last time you set a file attribute to a double?!
                            // Useless list of primitives...
                            return new Double(v.getValue());
                        } else if ((v = sub.getAttributeNode("charvalue")) != null) { // NOI18N
                            return new Character(v.getValue().charAt(0));
                        } else if ((v = sub.getAttributeNode("methodvalue")) != null) { // NOI18N
                            String value = v.getValue();
                            Object[] params = new Object[] { findResource(name), attrName };
                            // Stolen from XMLMapAttr:
                            String className,methodName;
                            int j = value.lastIndexOf('.');
                            if (j != -1) {
         
                                methodName = value.substring(j+1);
         
                                className = value.substring(0,j);
                                ClassLoader cl = cp.getClassLoader(true);
                                Class cls = Class.forName(className, true, cl);
                                // Note that unlike XMLMapAttr, we want to use currentClassLoader.
         
                                Object objArray[][] = {null,null,null};
                                Method methArray[] = {null,null,null};
         
         
                                Class fParam = null, sParam = null;
         
                                if (params != null) {
                                    if (params.length > 0) fParam = params[0].getClass();
                                    if (params.length > 1) sParam = params[1].getClass();
                                }
         
                                Method[] allMethods = cls.getDeclaredMethods();
                                Class[] paramClss;
         
                                for (int k=0; k < allMethods.length; k++) {
         
                                    if (!allMethods[k].getName().equals(methodName))  continue;
         
         
                                    paramClss = allMethods[k].getParameterTypes();
         
                                    if (params == null  || params.length == 0 || paramClss.length == 0) {
                                        if (paramClss.length == 0 && methArray[0] == null && objArray[0] == null) {
                                            methArray[paramClss.length] = allMethods[k];
                                            objArray[paramClss.length] = new Object[] {};
                                            continue;
                                        }
         
                                        continue;
                                    }
         
         
                                    if (paramClss.length == 2 && params.length >= 2  && methArray[2] == null && objArray[2] == null)  {
                                        if (paramClss[0].isAssignableFrom(fParam) && paramClss[1].isAssignableFrom(sParam)) {
                                            methArray[paramClss.length] = allMethods[k];
                                            objArray[paramClss.length] = new Object[] {params[0],params[1]};
                                            break;
                                        }
         
                                        if (paramClss[0].isAssignableFrom(sParam) && paramClss[1].isAssignableFrom(fParam)) {
                                            methArray[paramClss.length] = allMethods[k];
                                            objArray[paramClss.length] = new Object[] {params[1],params[0]};
                                            break;
                                        }
         
                                        continue;
                                    }
         
                                    if (paramClss.length == 1 && params.length >= 1 && methArray[1] == null && objArray[1] == null)  {
                                        if (paramClss[0].isAssignableFrom(fParam)) {
                                            methArray[paramClss.length] = allMethods[k];
                                            objArray[paramClss.length] = new Object[] {params[0]};
                                            continue;
                                        }
         
                                        if (paramClss[0].isAssignableFrom(sParam)) {
                                            methArray[paramClss.length] = allMethods[k];
                                            objArray[paramClss.length] = new Object[] {params[1]};
                                            continue;
                                        }
         
                                        continue;
                                    }
         
                                }
         
                                for (int l = methArray.length-1; l >= 0; l-- ) {//clsArray.length
                                    if (methArray[l] != null && objArray[l] != null)  {
                                        //Method meth = cls.getDeclaredMethod(methodName,clsArray[l]);
                                        methArray[l].setAccessible(true); //otherwise cannot invoke private
                                        return methArray[l].invoke(null,objArray[l]);
                                    }
                                }
                            }
                            // Some message to logFile
                            throw new InstantiationException(value);
                        } else if ((v = sub.getAttributeNode("serialvalue")) != null) { // NOI18N
                            // Copied from XMLMapAttr:
                            String value = v.getValue();
                            if (value.length() == 0) return null;
         
                            byte[] bytes = new byte[value.length()/2];
                            int tempJ;
                            int count = 0;
                            for (int j = 0; j < value.length(); j += 2) {
                                tempJ = Integer.parseInt(value.substring(j,j+2),16);
                                if (tempJ > 127) tempJ -=256;
                                bytes[count++] = (byte) tempJ;
                            }
         
                            ByteArrayInputStream bis = new ByteArrayInputStream(bytes, 0, count);
                            // XXX this should be using a subclass that specifies the right class loader:
                            ObjectInputStream ois = new NbObjectInputStream(bis);
                            return ois.readObject();
                        } else if ((v = sub.getAttributeNode("newvalue")) != null) { // NOI18N
                            ClassLoader cl = cp.getClassLoader(true);
                            return Class.forName(v.getValue(), true, cl).newInstance();
                        }
         */
    }
    
    private final Set orderAbsorbers = new HashSet(); // Set<String>
    public void writeAttribute(String name, String attrName, Object v) throws IOException {
        if (v == null) {
            String mebbeOrder = name + '/' + attrName; // NOI18N
            if (orderAbsorbers.remove(mebbeOrder)) {
                return; // see below
            }
        }
        if (attrName.equals("OpenIDE-Folder-Order") && (v instanceof String)) { // NOI18N
            // This is a special case. We do not want to store a fully fixed order in a layer.
            // Rather, compute some reasonable orderings from it.
            StringTokenizer tok = new StringTokenizer((String) v, "/"); // NOI18N
            if (tok.hasMoreTokens()) {
                String prev = tok.nextToken();
                while (tok.hasMoreTokens()) {
                    String next = tok.nextToken();
                    writeAttribute(name, prev + '/' + next, Boolean.TRUE); // NOI18N
                    // DataFolder tries to cancel these orders immediately after writing!
                    // Don't let it.
                    orderAbsorbers.add(name + '/' + prev + '/' + next); // NOI18N
                    prev = next;
                }
            }
            return;
        }
        TreeElement el = findElement(name);
        if (el == null) {
            throw new FileNotFoundException(name);
        }
        // Find any existing <attr>.
        TreeChild existingAttr = null;
        Iterator it = el.getChildNodes(TreeElement.class).iterator();
        while (it.hasNext()) {
            TreeElement sub = (TreeElement) it.next();
            if (sub.getLocalName().equals("attr")) { // NOI18N
                TreeAttribute attr = sub.getAttribute("name"); // NOI18N
                if (attr == null) {
                    // Malformed.
                    continue;
                }
                if (attr.getValue().equals(attrName)) {
                    existingAttr = sub;
                    break;
                }
            }
        }
        TreeElement attr;
        try {
            attr = new TreeElement("attr", true); // NOI18N
            attr.addAttribute("name", attrName); // NOI18N
            if (v instanceof String) {
                String inStr = (String) v;
                String newValueMagic = "newvalue:"; // NOI18N
                String methodValueMagic = "methodvalue:"; // NOI18N
                if (inStr.startsWith(newValueMagic)) {
                    // Impossible to set this (reliably) as a real value, so use this magic technique instead:
                    attr.addAttribute("newvalue", inStr.substring(newValueMagic.length()));
                } else if (inStr.startsWith(methodValueMagic)) {
                    // Same here:
                    attr.addAttribute("methodvalue", inStr.substring(methodValueMagic.length()));
                } else {
                    // Regular string value.
                    // Stolen from XMLMapAttr w/ mods:
                    StringBuffer outStr = new StringBuffer();
                    for (int i = 0; i < inStr.length(); i++) {
                        char c = inStr.charAt(i);
                        if (Character.isISOControl(c) || c == '&' || c == '<' || c == '>' || c == '"' || c == '\'') {
                            outStr.append(encodeChar(c));
                        } else {
                            outStr.append(c);
                        }
                    }
                    attr.addAttribute("stringvalue", outStr.toString()); // NOI18N
                }
            } else if (v instanceof URL) {
                attr.addAttribute("urlvalue", ((URL) v).toExternalForm()); // NOI18N
            } else if (v instanceof Boolean) {
                attr.addAttribute("boolvalue", v.toString());
            } else if (v != null) {
                throw new UnsupportedOperationException("XXX");
            }
            if (v != null && existingAttr == null) {
                appendWithIndent(el, attr);
            } else if (v != null) {
                ((TreeParentNode) el).replaceChild(existingAttr, attr);
            } else if (existingAttr != null) {
                deleteWithIndent(existingAttr);
            }
        } catch (InvalidArgumentException e) {
            throw new AssertionError(e);
        } catch (ReadOnlyException e) {
            throw (IOException) new IOException(e.toString()).initCause(e);
        }
        /*
        if (v instanceof Byte) {
            attr.setAttribute("bytevalue", v.toString()); // NOI18N
        } else if (v instanceof Short) {
            attr.setAttribute("shortvalue", v.toString()); // NOI18N
        } else if (v instanceof Integer) {
            attr.setAttribute("intvalue", v.toString()); // NOI18N
        } else if (v instanceof Long) {
            attr.setAttribute("longvalue", v.toString()); // NOI18N
        } else if (v instanceof Float) {
            attr.setAttribute("floatvalue", v.toString()); // NOI18N
        } else if (v instanceof Double) {
            attr.setAttribute("doublevalue", v.toString()); // NOI18N
        } else if (v instanceof Character) {
            attr.setAttribute("charvalue", v.toString()); // NOI18N
        } else {
            // Stolen from XMLMapAttr, mostly.
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(v);
            oos.close();
            byte bArray[] = bos.toByteArray();
            // Check to see if this is the same as a default instance.
            Class clazz = v.getClass();
            boolean usenewinstance = false;
            try {
                Object v2 = clazz.newInstance();
                bos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(bos);
                oos.writeObject(v2);
                oos.close();
                byte[] bArray2 = bos.toByteArray();
                usenewinstance = Utilities.compareObjects(bArray, bArray2);
            } catch (Exception e) {
                // quite expectable - ignore
            }
            if (usenewinstance) {
                attr.setAttribute("newvalue", clazz.getName()); // NOI18N
            } else {
                StringBuffer strBuff = new StringBuffer(bArray.length*2);
                for(int i = 0; i < bArray.length;i++) {
                    if (bArray[i] < 16 && bArray[i] >= 0) strBuff.append("0");// NOI18N
                    strBuff.append(Integer.toHexString(bArray[i] < 0?bArray[i]+256:bArray[i]));
                }
                attr.setAttribute("serialvalue", strBuff.toString()); // NOI18N
                // Also mention what the original value was, for reference.
                // Do it after adding element since otherwise we cannot indent correctly.
                String asString;
                if (clazz.isArray()) {
                    // Default toString sucks for arrays. Pretty common so worth special-casing.
                    asString = Arrays.asList((Object[]) v).toString();
                } else {
                    asString = v.toString();
                }
                serialComment = " (" + attrName + "=" + asString + ") ";
            }
        }
        if (adding) {
            // XXX if an ordering attr, try to put it next to file; else for folders, put it before any file
            appendWithIndent(el, attr);
        }
        // Deal with serial comments now.
        Comment comment = findSerialComment(el, attrName);
        if (serialComment != null) {
            if (comment != null) {
                comment.setData(serialComment);
            } else {
                appendWithIndent(el, doc.createComment(serialComment));
            }
        } else if (comment != null) {
            // Changed from some serialvalue to simple value; kill comment.
            el.removeChild(comment);
        }
        if (attrName.startsWith("SystemFileSystem")) { // NOI18N
            fireFileStatusChanged(new FileStatusEvent(this, findResource(name), true, true));
        }
    }
    private Comment findSerialComment(Element el, String attrName) {
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.COMMENT_NODE) {
                String comm = nl.item(i).getNodeValue();
                if (comm.startsWith(" (" + attrName + "=") && comm.endsWith(") ")) {
                    return (Comment) nl.item(i);
                }
            }
        }
        return null;
         */
    }
    
    /** stolen from XMLMapAttr */
    private static String encodeChar(char ch) {
        String encChar = Integer.toString((int) ch, 16);
        return "\\u" + "0000".substring(0, "0000".length() - encChar.length()).concat(encChar); // NOI18N
    }
    
    public void renameAttributes(String oldName, String newName) {
        // do nothing
    }
    
    public void deleteAttributes(String name) {
        // do nothing
    }
    
    public boolean readOnly(String name) {
        return false;
    }
    
    public String mimeType(String name) {
        return null; // i.e. use default resolvers
    }
    
    public long size(String name) {
        try {
            return getContentsOf(name).length;
        } catch (FileNotFoundException fnfe) {
            return 0;
        }
    }
    
    public void markUnimportant(String name) {
        // do nothing
    }
    
    public Date lastModified(String name) {
        final TreeElement el = findElement(name);
        if (el == null) {
            return new Date(0L);
        }
        TreeAttribute attr = el.getAttribute("url"); // NOI18N
        if (attr == null) {
            return new Date(0L);
        }
        String u = attr.getValue();
        if (URI.create(u).isAbsolute()) {
            return new Date(0L);
        }
        FileObject external;
        try {
            external = URLMapper.findFileObject(new URL(location, u));
        } catch (MalformedURLException e) {
            assert false : e;
            return new Date(0L);
        }
        if (external == null) {
            return new Date(0L);
        }
        return external.lastModified();
    }
    
    // These are not important for us:
    
    public void lock(String name) throws IOException {
        // [PENDING] should this try to lock the XML document??
        // (not clear if it is safe to do so from FS call, even tho
        // on a different FS)
    }
    
    public void unlock(String name) {
        // do nothing
    }
    
    // don't bother making configurable; or could use an indentation engine
    private static final int INDENT_STEP = 4;
    /**
     * Add a new element to a parent in the correct position.
     * Inserts whitespace to retain indentation rules.
     */
    private static void appendWithIndent(TreeElement parent, TreeChild child) throws ReadOnlyException {
        TreeParentNode doc = parent;
        int depth = -2; // will get <filesystem> then TreeDocument then null
        while (doc != null) {
            doc = ((TreeChild) doc).getParentNode();
            depth++;
        }
        TreeChild position = insertBefore(parent, child);
        try {
            if (position != null) {
                parent.insertBefore(child, position);
                parent.insertBefore(new TreeText("\n" + spaces((depth + 1) * INDENT_STEP)), position);
            } else {
                if (/*XXX this is clumsy*/ parent.hasChildNodes()) {
                    parent.appendChild(new TreeText(spaces(INDENT_STEP)));
                } else {
                    parent.appendChild(new TreeText("\n" + spaces((depth + 1) * INDENT_STEP)));
                }
                parent.appendChild(child);
                parent.appendChild(new TreeText("\n" + spaces(depth * INDENT_STEP)));
            }
            parent.normalize();
            if (((TreeElement) child).getQName().equals("attr") && ((TreeElement) child).getAttribute("name").getValue().indexOf('/') != -1) { // NOI18N
                // Check for ordering attributes, which we have to handle specially.
                resort(parent);
            }
        } catch (InvalidArgumentException e) {
            assert false : e;
        }
    }
    /**
     * Find the proper location for a newly inserted element.
     * Rules:
     * 1. <file> or <folder> must be added in alphabetical order w.r.t. existing ones.
     * 2. <attr> w/o '/' in name must be added to top of element in alpha order w.r.t. existing ones.
     * 3. <attr> w/ '/' should be added wherever - will rearrange everything later...
     * Returns a position to insert before (null for end).
     */
    private static TreeChild insertBefore(TreeElement parent, TreeChild child) throws ReadOnlyException {
        if (!(child instanceof TreeElement)) {
            return null; // TBD for now
        }
        TreeElement childe = (TreeElement) child;
        if (childe.getQName().equals("file") || childe.getQName().equals("folder")) { // NOI18N
            String name = childe.getAttribute("name").getValue(); // NOI18N
            Iterator it = parent.getChildNodes(TreeElement.class).iterator();
            while (it.hasNext()) {
                TreeElement kid = (TreeElement) it.next();
                if (kid.getQName().equals("file") || kid.getQName().equals("folder")) { // NOI18N
                    String kidname = kid.getAttribute("name").getValue(); // NOI18N
                    if (kidname.compareTo(name) > 0) {
                        return kid;
                    }
                }
            }
            return null;
        } else if (childe.getQName().equals("attr")) { // NOI18N
            String name = childe.getAttribute("name").getValue(); // NOI18N
            int slash = name.indexOf('/');
            if (slash == -1) {
                // Regular attribute.
                Iterator it = parent.getChildNodes(TreeElement.class).iterator();
                while (it.hasNext()) {
                    TreeElement kid = (TreeElement) it.next();
                    if (kid.getQName().equals("file") || kid.getQName().equals("folder")) { // NOI18N
                        return kid;
                    } else if (kid.getQName().equals("attr")) { // NOI18N
                        String kidname = kid.getAttribute("name").getValue(); // NOI18N
                        if (kidname.compareTo(name) > 0) {
                            return kid;
                        }
                    } else {
                        throw new AssertionError("Weird child: " + kid.getQName());
                    }
                }
                return null;
            } else {
                // Ordering attribute. Will be ordered later, so skip it now.
                return null;
                /*
                String former = name.substring(0, slash);
                String latter = name.substring(slash + 1);
                TreeElement formerMatch = findElementIn(parent, former);
                TreeElement latterMatch = findElementIn(parent, latter);
                if (formerMatch == null) {
                    if (latterMatch == null) {
                        // OK, just stick it somewhere.
                        return positionForRegularAttr(parent, name);
                    } else {
                        return latterMatch;
                    }
                } else { // formerMatch != null
                    if (latterMatch == null) {
                        return null; // XXX OK?
                    } else {
                        int formerIdx = parent.indexOf(formerMatch);
                        int latterIdx = parent.indexOf(latterMatch);
                        if (formerIdx > latterIdx) {
                            // Out of order. Swap them.
                            // XXX does this need to be smarter?
                            parent.removeChild(formerMatch);
                            parent.removeChild(latterMatch);
                            parent.insertChildAt(formerMatch, latterIdx);
                            parent.insertChildAt(latterMatch, formerIdx);
                        }
                        return latterMatch;
                    }
                }
                 */
            }
        } else {
            throw new AssertionError("Weird child: " + childe.getQName());
        }
    }
    /**
     * Resort all files and folders and attributes in a folder context. The order is:
     * 1. Files and folders are alpha-sorted if they have no relative attrs.
     * 2. But existing relative attrs change that order. Unordered files/folders go at the end.
     * 3. Most attributes are alpha-sorted at the top.
     * 4. Relative attrs are sorted between files or folders they order, if possible.
     * 5. Relative attrs with one missing referent are ordered right after the existing referent.
     * 6. Relative attrs with both missing referents are ordered after other attrs.
     */
    private static void resort(TreeElement parent) throws ReadOnlyException {
        class Item {
            public TreeElement child;
            public int originalIndex;
            boolean isAttr() {
                return child.getQName().equals("attr"); // NOI18N
            }
            String getName() {
                return child.getAttribute("name").getValue(); // NOI18N
            }
            boolean isOrderingAttr() {
                return isAttr() && getName().indexOf('/') != -1;
            }
            String getFormer() {
                String n = getName();
                return n.substring(0, n.indexOf('/'));
            }
            String getLatter() {
                String n = getName();
                return n.substring(n.indexOf('/') + 1);
            }
        }
        Set/*<Item>*/ items = new LinkedHashSet();
        SortedSet/*<Integer>*/ indices = new TreeSet();
        for (int i = 0; i < parent.getChildrenNumber(); i++) {
            TreeChild child = (TreeChild) parent.getChildNodes().get(i);
            if (child instanceof TreeElement) {
                Item item = new Item();
                item.child = (TreeElement) child;
                item.originalIndex = i;
                items.add(item);
                indices.add(new Integer(i));
            }
        }
        Map/*<Item,Collection<Item>>*/ edges = new LinkedHashMap();
        Map/*<String,Item>*/ filesAndFolders = new LinkedHashMap();
        Map/*<String,Item>*/ attrs = new LinkedHashMap();
        Set/*<String>*/ orderedFilesAndFolders = new LinkedHashSet();
        Iterator it = items.iterator();
        while (it.hasNext()) {
            Item item = (Item) it.next();
            String name = item.getName();
            if (item.isAttr()) {
                attrs.put(name, item);
                if (item.isOrderingAttr()) {
                    orderedFilesAndFolders.add(item.getFormer());
                    orderedFilesAndFolders.add(item.getLatter());
                }
            } else {
                filesAndFolders.put(name, item);
            }
        }
        class NameComparator implements Comparator {
            public int compare(Object o1, Object o2) {
                Item i1 = (Item) o1;
                Item i2 = (Item) o2;
                return i1.getName().compareTo(i2.getName());
            }
        }
        Set/*<Item>*/ sortedAttrs = new TreeSet(new NameComparator());
        Set/*<Item>*/ sortedFilesAndFolders = new TreeSet(new NameComparator());
        Set/*<Item>*/ orderableItems = new LinkedHashSet();
        it = items.iterator();
        while (it.hasNext()) {
            Item item = (Item) it.next();
            String name = item.getName();
            if (item.isAttr()) {
                if (item.isOrderingAttr()) {
                    Item former = (Item) filesAndFolders.get(item.getFormer());
                    if (former != null) {
                        Set/*<Item>*/ formerConstraints = (Set) edges.get(former);
                        if (formerConstraints == null) {
                            formerConstraints = new LinkedHashSet();
                            edges.put(former, formerConstraints);
                        }
                        formerConstraints.add(item);
                    }
                    Item latter = (Item) filesAndFolders.get(item.getLatter());
                    if (latter != null) {
                        Set/*<Item>*/ constraints = new LinkedHashSet();
                        constraints.add(latter);
                        edges.put(item, constraints);
                    }
                    orderableItems.add(item);
                } else {
                    sortedAttrs.add(item);
                }
            } else {
                if (orderedFilesAndFolders.contains(name)) {
                    orderableItems.add(item);
                } else {
                    sortedFilesAndFolders.add(item);
                }
            }
        }
        java.util.List/*<Item>*/ orderedItems;
        try {
            orderedItems = Utilities.topologicalSort(orderableItems, edges);
        } catch (TopologicalSortException e) {
            // OK, ignore.
            return;
        }
        it = items.iterator();
        while (it.hasNext()) {
            Item item = (Item) it.next();
            parent.removeChild(item.child);
        }
        java.util.List/*<Item>*/ allOrderedItems = new ArrayList(sortedAttrs);
        allOrderedItems.addAll(orderedItems);
        allOrderedItems.addAll(sortedFilesAndFolders);
        assert new HashSet(allOrderedItems).equals(items);
        it = allOrderedItems.iterator();
        Iterator indexIt = indices.iterator();
        while (it.hasNext()) {
            Item item = (Item) it.next();
            int index = ((Integer) indexIt.next()).intValue();
            parent.insertChildAt(item.child, index);
        }
    }
    private static String spaces(int size) {
        char[] chars = new char[size];
        for (int i = 0; i < size; i++) {
            chars[i] = ' ';
        }
        return new String(chars);
    }
    /**
     * Remove an element (and its children), deleting any surrounding newlines and indentation too.
     */
    private static void deleteWithIndent(TreeChild child) throws ReadOnlyException {
        TreeChild next = child.getNextSibling();
        // XXX better might be to delete any maximal [ \t]+ previous plus \n next (means splitting up TreeText's)
        if (next instanceof TreeText && ((TreeText) next).getData().matches("(\r|\n|\r\n)[ \t]+")) { // NOI18N
            next.removeFromContext();
        } else {
            TreeChild previous = child.getPreviousSibling();
            if (previous instanceof TreeText && ((TreeText) previous).getData().matches("(\r|\n|\r\n)[ \t]+")) { // NOI18N
                previous.removeFromContext();
            } else {
                // Well, not sure what is here, so skip it.
            }
        }
        TreeElement parent = (TreeElement) child.getParentNode();
        TreeObjectList list = parent.getChildNodes();
        boolean kill = true;
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o == child) {
                continue;
            }
            if (!(o instanceof TreeText)) {
                kill = false;
                break;
            }
            if (((TreeText) o).getData().trim().length() > 0) {
                kill = false;
                break;
            }
        }
        if (kill) {
            try {
                // Special case for root of filesystem.
                if (((TreeChild) parent).getParentNode() instanceof TreeDocumentRoot) {
                    it = list.iterator();
                    while (it.hasNext()) {
                        ((TreeChild) it.next()).removeFromContext();
                    }
                    parent.appendChild(new TreeText("\n")); // NOI18N
                } else {
                    // Make sure we convert it to an empty tag (seems to only affect elements
                    // which were originally parsed?):
                    TreeElement parent2 = new TreeElement(parent.getQName(), true);
                    parent2.addAttribute("name", parent.getAttribute("name").getValue()); // NOI18N
                    TreeParentNode grandparent = ((TreeChild) parent).getParentNode();
                    // TreeElement.empty is sticky - cannot be changed retroactively (argh!).
                    grandparent.replaceChild(parent, parent2);
                    parent = parent2; // for normalize() below
                }
            } catch (InvalidArgumentException e) {
                assert false : e;
            }
        }
        child.removeFromContext();
        parent.normalize();
    }
    
    // Listen to changes in files used as url= external contents. If these change,
    // the filesystem needs to show something else. Properly we would
    // keep track of *which* file changed and thus which of our resources
    // is affected. Practically this would be a lot of work and gain
    // very little.
    public void fileDeleted(FileEvent fe) {
        someFileChange(fe);
    }
    public void fileFolderCreated(FileEvent fe) {
        // does not apply to us
    }
    public void fileDataCreated(FileEvent fe) {
        // not interesting here
    }
    public void fileAttributeChanged(FileAttributeEvent fe) {
        // don't care about attributes on included files...
    }
    public void fileRenamed(FileRenameEvent fe) {
        someFileChange(fe);
    }
    public void fileChanged(FileEvent fe) {
        someFileChange(fe);
    }
    private void someFileChange(FileEvent fe) {
        // If used as url=, refresh contents, timestamp, ...
        refreshResource("", true);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (!evt.getPropertyName().equals(TreeEditorCookie.PROP_DOCUMENT_ROOT)) {
            return;
        }
        if (cookie.getStatus() == TreeEditorCookie.STATUS_OK || cookie.getStatus() == TreeEditorCookie.STATUS_NOT) {
            // Document was modified, and reparsed OK. See what changed.
            try {
                doc = cookie.openDocumentRoot();
                /* Neither of the following work:
                refreshResource("", true); // only works on root folder
                refreshRoot();             // seems to do nothing at all
                 */
                Enumeration/*<FileObject>*/ e = existingFileObjects(getRoot());
                while (e.hasMoreElements()) {
                    FileObject fo = (FileObject) e.nextElement();
                    // fo.refresh() does not work
                    refreshResource(fo.getPath(), true);
                }
                //System.err.println("got changes; new files: " + Collections.list(getRoot().getChildren(true)));
                //Thread.dumpStack();
            } catch (TreeException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
    
}
