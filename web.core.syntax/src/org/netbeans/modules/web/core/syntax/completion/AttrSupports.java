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

package org.netbeans.modules.web.core.syntax.completion;

import java.util.*;
import java.beans.*;
import javax.swing.text.JTextComponent;
import javax.swing.ImageIcon;

import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.java.JavaCompletionQuery;
import org.netbeans.jmi.javamodel.JavaPackage;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.java.JMIUtils;
import org.netbeans.modules.editor.java.NbJMIResultItem;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.netbeans.modules.web.core.syntax.*;



/** Support for code completion of default JSP tags.
 *
 * @author  pjiricka
 */
public class AttrSupports extends Object {

    public static class ScopeSupport extends AttributeValueSupport.Default {
        
        public ScopeSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            list.add("application");    // NOI18N
            list.add("page");           // NOI18N
            list.add("request");        // NOI18N
            list.add("session");        // NOI18N
            return list;
        }
        
    }
    
    public static class RootVersionSupport extends AttributeValueSupport.Default {
        
        public RootVersionSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            list.add("1.2");    // NOI18N
            list.add("2.0");           // NOI18N
            return list;
        }
        
    }
    
    public static class VariableScopeSupport extends AttributeValueSupport.Default {
        
        public VariableScopeSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            list.add("AT_BEGIN");    // NOI18N
            list.add("AT_END");           // NOI18N
            list.add("NESTED");        // NOI18N
            return list;
        }
        
    }
    
    public static class YesNoTrueFalseSupport extends AttributeValueSupport.Default {
        
        public YesNoTrueFalseSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            list.add("false");    // NOI18N
            list.add("no");           // NOI18N
            list.add("true");        // NOI18N
            list.add("yes");        // NOI18N
            return list;
        }
        
    }

    /** Support for code completing of package and class. */
    public static class PackageClassSupport extends AttributeValueSupport.Default {
        /** Index where to start substitution */
        private int itemOffset;
        /** Length of currently substituted text */
        private int itemLength;
        
        public PackageClassSupport (boolean tag, String longName, String attrName) {
            super (tag, longName, attrName);
        }
        
        protected List possibleValues (JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            return new ArrayList ();
        }
        
        /** Returns the complete result that contains elements from getCompletionItems.   */
        public CompletionQuery.Result getResult (JTextComponent component, int offset, 
            JspSyntaxSupport sup, SyntaxElement.TagDirective item, String valuePart) {
            if(valuePart.lastIndexOf(',') > 0)
                valuePart = valuePart.substring(valuePart.lastIndexOf(',')+1).trim();
            List res = completionResults (offset, sup, item, valuePart);
            return new JavaCompletionQuery.JavaResult (component, res, completionTitle (), null, 
                itemOffset, itemLength, 0);
        }
        
        /** Returns generated List of items for completion.  
         *  It sets itemLength and itemOffset variables as a side effect
         */
        private List completionResults (int offset, JspSyntaxSupport sup, SyntaxElement.TagDirective item, String valuePart) {
            JMIUtils jmiutils = JMIUtils.get(sup.getDocument());
            String pkgName = "";    // NOI18N
            JavaPackage pkg = null;
            String clsNamePart = valuePart;
            int lastDot = valuePart.lastIndexOf ('.');
            if (lastDot >= 0) {
                pkgName = valuePart.substring (0, lastDot);
                pkg = jmiutils.getExactPackage(pkgName);                
                clsNamePart = (lastDot == valuePart.length ())? "": valuePart.substring (lastDot+1);    // NOI18N
            }
            itemOffset = offset - valuePart.length () + lastDot + 1;  // works even with -1
            itemLength = clsNamePart.length ();
            
            List res = new ArrayList();
            res.addAll(jmiutils.findPackages (valuePart, false, false, true)); // Find all possible packages // NOI18N
            if (pkg != null)
                res.addAll(jmiutils.findClasses(pkg, clsNamePart, false, true, true, /*JavaClass context*/ null, true, false));
            
            //set substitute offset
            Iterator i = res.iterator();
            while(i.hasNext()) {
                NbJMIResultItem jmiResultItem = (NbJMIResultItem)i.next();
                jmiResultItem.setSubstituteOffset(itemOffset);
            }
            
            return res;
        }
             
    }

    public static class GetSetPropertyName extends AttributeValueSupport.Default {
        
        public GetSetPropertyName(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            PageInfo.BeanData[] beanData = sup.getBeanData();
            for (int i = 0; i < beanData.length; i++) {
                list.add(beanData[i].getId());
            }
            return list;
        }
        
    }

    
    public static abstract class GetSetPropertyProperty extends AttributeValueSupport.Default {
        
        public GetSetPropertyProperty(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item, boolean setter) {
            ArrayList list = new ArrayList();
            String namePropertyValue = (String)item.getAttributes().get("name");    // NOI18N
            if (namePropertyValue != null) {
                String className = null;
                PageInfo.BeanData[] beanData = sup.getBeanData();
                for (int i = 0; i < beanData.length; i++) {
                    if (beanData[i] == null || beanData[i].getId() == null)
                        continue;
                    
                    if (beanData[i].getId().equals(namePropertyValue)) {
                        className = beanData[i].getClassName();
                        break;
                    }
                }

                if (className != null) {
                    try {
                        FileObject fo = NbEditorUtilities.getDataObject( sup.getDocument()).getPrimaryFile();
                        ClassLoader cld = JspUtils.getModuleClassLoader( sup.getDocument(), fo);
                        Class beanClass = Class.forName(className, false, cld);
                        Introspector.flushFromCaches(beanClass);
                        BeanInfo benInfo = Introspector.getBeanInfo(beanClass);
                        PropertyDescriptor[] properties = benInfo.getPropertyDescriptors();
                        for (int j = 0; j < properties.length; j++) {
                            if (setter && (properties[j].getWriteMethod() != null))
                                list.add(properties[j].getName());
                            if (!setter && (properties[j].getReadMethod() != null) && !properties[j].getName().equals("class")) //NOI18N
                                    list.add(properties[j].getName());
                        }
                    }
                    catch (ClassNotFoundException e) {
                        // do nothing
                    }
                    catch (IntrospectionException e) {
                        // do nothing
                    }
                }
            }    
            return list;
        }
    }
    
    public static class GetPropertyProperty extends GetSetPropertyProperty {
        
        public GetPropertyProperty() {
            super(true, "jsp:getProperty", "property");     // NOI18N
        }
        
        public List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            return possibleValues(sup, item, false);
        }
        
    }
    
    public static class SetPropertyProperty extends GetSetPropertyProperty {
        
        public SetPropertyProperty() {
            super(true, "jsp:setProperty", "property"); // NOI18N
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            List l = possibleValues(sup, item, true);
            l.add(0, "*");  // NOI18N
            return l;
        }
        
    }
    
    public static class TaglibURI extends AttributeValueSupport.Default {
        
        public TaglibURI() {
            super(false, "taglib", "uri");      // NOI18N
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            List l = new ArrayList();
            Map m = sup.getTagLibraryMappings();
            if (m != null) {
                Iterator it = m.keySet().iterator();
                for (; it.hasNext(); ) {
                    String s = (String)it.next();
                    l.add(s);
                }
            }
            // sort alphabetically
            Collections.sort(l);
            return l;
        }
        
    }

    public static class TaglibTagdir extends AttributeValueSupport.Default {
        
        public TaglibTagdir() {
            super(false, "taglib", "tagdir");      // NOI18N
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            List l = new ArrayList();
            FileObject orig = sup.getFileObject();
            FileObject documentBase = JspUtils.guessWebModuleRoot(sup.getDocument(), orig);
            if (documentBase != null) {
                FileObject webInfTags = JspUtils.findRelativeFileObject(documentBase, "WEB-INF/tags");
                if (webInfTags != null) {
                    // WEB-INF/tags itself
                    if (isValidTagDir(webInfTags)) {
                        l.add(JspUtils.findRelativeContextPath(documentBase, webInfTags));
                    }
                    // subfolders of WEB-INF/tags
                    Enumeration en = webInfTags.getFolders(true);
                    while (en.hasMoreElements()) {
                        FileObject subF = (FileObject)en.nextElement();
                        if (isValidTagDir(subF)) {
                            l.add(JspUtils.findRelativeContextPath(documentBase, subF));
                        }
                    }
                }
            }
            // sort alphabetically
            Collections.sort(l);
            return l;
        }
        
        private boolean isValidTagDir(FileObject subF) {
            // must contain at least one file
            return subF.getChildren(false).hasMoreElements();
        }
        
    }

    
    /** Support for code completing of package and class. */
    public static class FilenameSupport extends AttributeValueSupport.Default {
        static final ImageIcon PACKAGE_ICON =         
            new ImageIcon(org.openide.util.Utilities.loadImage("org/openide/loaders/defaultFolder.gif")); // NOI18N
        
        /** Index where to start substitution */
        private int itemOffset;
        /** Length of currently substituted text */
        private int itemLength;
        
        public FilenameSupport (boolean tag, String longName, String attrName) {
            super (tag, longName, attrName);
        }
        
        protected List possibleValues (JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            return new ArrayList ();
        }
        
        /** Returns the complete result that contains elements from getCompletionItems.   */
        public CompletionQuery.Result getResult (JTextComponent component, int offset, 
            JspSyntaxSupport sup, SyntaxElement.TagDirective item, String valuePart) {
            List res = completionResults (offset, sup, item, valuePart);
            return new CompletionQuery.DefaultResult(component, 
                        completionTitle (), res, 
                        itemOffset, itemLength);
        }
        
        /** Returns generated List of items for completion.  
         *  It sets itemLength and itemOffset variables as a side effect
         */
        private List completionResults (int offset, JspSyntaxSupport sup, SyntaxElement.TagDirective item, String valuePart) {
            List res = new ArrayList ();
            String path = "";   // NOI18N
            String fileNamePart = valuePart;
            int lastSlash = valuePart.lastIndexOf ('/');
            if (lastSlash == 0) {
                path = "/"; // NOI18N
                fileNamePart = valuePart.substring (1);
            }
            else if (lastSlash > 0) { // not a leading slash?
                path = valuePart.substring (0, lastSlash);
                fileNamePart = (lastSlash == valuePart.length ())? "": valuePart.substring (lastSlash+1);    // NOI18N
            }
            
            try {
                FileObject orig = sup.getFileObject();
                FileObject documentBase = JspUtils.guessWebModuleRoot(sup.getDocument(), orig);
                // need to normalize fileNamePart with respect to orig
                String ctxPath = JspUtils.resolveRelativeURL ("/"+orig.getPath (), path);  // NOI18N
                //is this absolute path?
                if (path.startsWith("/"))
                    ctxPath = documentBase.getPath() + path;
                else 
                    ctxPath = ctxPath.substring (1);
                
                
                FileSystem fs = orig.getFileSystem ();
                
                FileObject folder = fs.findResource (ctxPath);
                if (folder != null) {
                    res = files (folder, fileNamePart, sup);
                    if (!folder.equals(documentBase) && !path.startsWith("/") // NOI18N
                        && (path.length() == 0 || (path.lastIndexOf("../")+3 == path.length()))){ // NOI18N
                        res.add(0,  new JspCompletionItem.FileAttributeValue ("../", java.awt.Color.BLUE, PACKAGE_ICON)); // NOI18N
                    }
                }
            }
            catch (FileStateInvalidException ex) {
                // unreachable FS - disable completion
            }
            catch (IllegalArgumentException ex) {
                // resolving failed
            }
            itemOffset = offset - valuePart.length () + lastSlash + 1;  // works even with -1
            itemLength = fileNamePart.length ();
            
            
            //set substitute offset
            Iterator i = res.iterator();
            while(i.hasNext()) {
                JspCompletionItem.JspResultItem resultItem = (JspCompletionItem.JspResultItem)i.next();
                resultItem.setSubstituteOffset(itemOffset);
            }            
            
            return res;
        }
        
        private List files (FileObject folder, String prefix, JspSyntaxSupport sup) {
            ArrayList res = new ArrayList ();
            TreeMap resFolders = new TreeMap();
            TreeMap resFiles = new TreeMap();
            
            Enumeration files = folder.getChildren (false);
            while (files.hasMoreElements ()) {
                FileObject file = (FileObject)files.nextElement ();
                String fname = file.getNameExt ();
                if (fname.startsWith (prefix)) {
                    
                    if (file.isFolder())
                        resFolders.put(file.getNameExt (), new JspCompletionItem.FileAttributeValue (file.getNameExt () + "/", java.awt.Color.BLUE, PACKAGE_ICON));
                    else{
                        java.awt.Image icon = JspUtils.getIcon(sup.getDocument(), file);
                        if (icon != null)
                            resFiles.put (file.getNameExt (), new JspCompletionItem.FileAttributeValue (file.getNameExt (), java.awt.Color.BLACK, new javax.swing.ImageIcon(icon)));
                        else 
                            resFiles.put (file.getNameExt (), new JspCompletionItem.FileAttributeValue (file.getNameExt (), java.awt.Color.BLACK));
                    }
                }
            }
            res.addAll(resFolders.values());
            res.addAll(resFiles.values());
            
            return res;
        }
            
    }
    
    public static class TrueFalseSupport extends AttributeValueSupport.Default {
        
        public TrueFalseSupport (boolean tag, String longName, String attrName) {
            super (tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            list.add("false");   // NOI18N
            list.add("true");    // NOI18N
            return list;
        }
        
    }

    public static class PageLanguage extends AttributeValueSupport.Default {
        
        public PageLanguage() {
            super(false, "page", "language");    // NOI18N
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            list.add("java");    // NOI18N
            return list;
        }
        
    }
    
    public static class EncodingSupport extends AttributeValueSupport.Default {
        
        public EncodingSupport (boolean tag, String longName, String attrName) {
            super (tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            Iterator iter = java.nio.charset.Charset.availableCharsets().keySet().iterator();
            
            while (iter.hasNext())
                list.add(iter.next()); 
            
            return list;
        }
        
    }

}
