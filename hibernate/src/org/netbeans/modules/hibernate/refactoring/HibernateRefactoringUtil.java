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
 * "Portions Copyrighted [year] [searchingForName of copyright owner]"
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
package org.netbeans.modules.hibernate.refactoring;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.hibernate.editor.HibernateEditorUtil;
import org.netbeans.modules.hibernate.mapping.HibernateMappingXmlConstants;
import org.netbeans.modules.hibernate.service.HibernateEnvironment;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.dom.EmptyTag;
import org.netbeans.modules.xml.text.syntax.dom.StartTag;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author Andrei Badea, Dongmei Cao
 */
public class HibernateRefactoringUtil {

    private static final Logger LOGGER = Logger.getLogger(HibernateRefactoringUtil.class.getName());
    private static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N

    public static boolean isJavaFile(FileObject fo) {
        return JAVA_MIME_TYPE.equals(fo.getMIMEType());
    }

    public static RenamedClassName getRenamedClassName(final TreePathHandle oldHandle, final JavaSource javaSource, final String newName) throws IOException {
        final RenamedClassName[] result = {null};
        javaSource.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) throws IOException {
                cc.toPhase(Phase.ELEMENTS_RESOLVED);
                Element element = oldHandle.resolveElement(cc);
                if (element == null || element.getKind() != ElementKind.CLASS) {
                    return;
                }
                String oldBinaryName = ElementUtilities.getBinaryName((TypeElement) element);
                String oldSimpleName = element.getSimpleName().toString();
                String newBinaryName = null;
                element = element.getEnclosingElement();
                if (element.getKind() == ElementKind.CLASS) {
                    newBinaryName = ElementUtilities.getBinaryName((TypeElement) element) + '$' + newName;
                } else if (element.getKind() == ElementKind.PACKAGE) {
                    String packageName = ((PackageElement) element).getQualifiedName().toString();
                    newBinaryName = createQualifiedName(packageName, newName);
                } else {
                    LOGGER.log(Level.WARNING, "Enclosing element of {0} was neither class nor package", oldHandle);
                }
                result[0] = new RenamedClassName(oldSimpleName, oldBinaryName, newBinaryName);
            }
        }, true);
        return result[0];
    }

    public static List<String> getTopLevelClassNames(FileObject fo) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fo);
        if (javaSource == null) {
            return Collections.emptyList();
        }
        final List<String> result = new ArrayList<String>(1);
        javaSource.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) throws IOException {
                cc.toPhase(Phase.ELEMENTS_RESOLVED);
                for (TypeElement typeElement : cc.getTopLevelElements()) {
                    result.add(ElementUtilities.getBinaryName(typeElement));
                }
            }
        }, true);
        return result;
    }

    public static String getPackageName(FileObject folder) {
        ClassPath cp = ClassPath.getClassPath(folder, ClassPath.SOURCE);
        if (cp != null) {
            return cp.getResourceName(folder, '.', false);
        }
        return null;
    }

    public static String getRenamedPackageName(FileObject folder, String newName) {
        FileObject parent = folder.getParent();
        if (parent == null) {
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(parent, ClassPath.SOURCE);
        if (cp == null) {
            return null;
        }
        String parentName = cp.getResourceName(parent, '.', false);
        if (parentName == null) {
            return null;
        }
        if (parentName.length() > 0) {
            return parentName + '.' + newName;
        } else {
            return newName;
        }
    }

    public static String getPackageName(URL url) {
        File f = null;
        try {
            String path = URLDecoder.decode(url.getPath(), "UTF-8"); // NOI18N
            f = FileUtil.normalizeFile(new File(path));
        } catch (UnsupportedEncodingException u) {
            throw new IllegalArgumentException("Cannot create package name for URL " + url); // NOI18N
        }
        String suffix = "";
        do {
            FileObject fo = FileUtil.toFileObject(f);
            if (fo != null) {
                if ("".equals(suffix)) {
                    return getPackageName(fo);
                }
                String prefix = getPackageName(fo);
                return prefix + ("".equals(prefix) ? "" : ".") + suffix; // NOI18N
            }
            if (!"".equals(suffix)) {
                suffix = "." + suffix; // NOI18N
            }
            try {
                suffix = URLDecoder.decode(f.getPath().substring(f.getPath().lastIndexOf(File.separatorChar) + 1), "UTF-8") + suffix; // NOI18N
            } catch (UnsupportedEncodingException u) {
                throw new IllegalArgumentException("Cannot create package name for URL " + url); // NOI18N
            }
            f = f.getParentFile();
        } while (f != null);
        throw new IllegalArgumentException("Cannot create package name for URL " + url); // NOI18N
    }

    public static String getSimpleElementName(String elementName) {
        for (;;) {
            if (elementName.length() == 0) {
                return elementName;
            }
            int lastDot = elementName.lastIndexOf('.');
            if (lastDot == -1) {
                return elementName;
            }
            if (lastDot == elementName.length() - 1) {
                elementName = elementName.substring(0, lastDot);
                continue;
            }
            return elementName.substring(lastDot + 1);
        }
    }

    public static String createQualifiedName(String packageName, String simpleName) {
        if (packageName.length() == 0) {
            return simpleName;
        } else {
            if (simpleName.length() == 0) {
                return packageName;
            } else {
                return packageName + '.' + simpleName;
            }
        }
    }

    public static final class RenamedClassName {

        private final String oldSimpleName;
        private final String oldBinaryName;
        private final String newBinaryName;

        public RenamedClassName(String oldSimpleName, String oldBinaryName, String newBinaryName) {
            this.oldSimpleName = oldSimpleName;
            this.oldBinaryName = oldBinaryName;
            this.newBinaryName = newBinaryName;
        }

        public String getOldSimpleName() {
            return oldSimpleName;
        }

        public String getOldBinaryName() {
            return oldBinaryName;
        }

        public String getNewBinaryName() {
            return newBinaryName;
        }
    }

    public static Map<FileObject, List<OccurrenceItem>> getJavaClassOccurrences(List<FileObject> allMappingFiles, String origBinaryName) {
        Map<FileObject, List<OccurrenceItem>> occurrences = new HashMap<FileObject, List<OccurrenceItem>>();
        for (FileObject mFileObj : allMappingFiles) {
            occurrences.put(mFileObj, getOccurPlaces(mFileObj, origBinaryName, false));
        }
        return occurrences;
    }
    
    public static Map<FileObject, List<OccurrenceItem>> getJavaPackageOccurrences(List<FileObject> allMappingFiles, String origPkgName) {
        Map<FileObject, List<OccurrenceItem>> occurrences = new HashMap<FileObject, List<OccurrenceItem>>();
        for (FileObject mFileObj : allMappingFiles) {
            occurrences.put(mFileObj, getOccurPlaces(mFileObj, origPkgName, true));
        }

        return occurrences;
    }
    
    public static Map<FileObject, List<OccurrenceItem>> getJavaFieldOccurrences(List<FileObject> allMappingFiles, String className, String fieldName) {
        Map<FileObject, List<OccurrenceItem>> occurrences = new HashMap<FileObject, List<OccurrenceItem>>();
        for (FileObject mFileObj : allMappingFiles) {
            occurrences.put( mFileObj, getJavaFieldOccurPlaces(mFileObj, className, fieldName));
        }
        return occurrences;
    }
    
    private static List<OccurrenceItem> getOccurPlaces(FileObject mappingFile, String searchingForName, boolean searchingPackageName) {
        List<OccurrenceItem> foundPlaces = new ArrayList<OccurrenceItem>();
        try {
            // Get the document for this file
            DataObject dataObject = DataObject.find(mappingFile);
            EditorCookie result = dataObject.getCookie(EditorCookie.class);
            if (result == null) {
                throw new IllegalStateException("File " + mappingFile + " does not have an EditorCookie.");
            }
            
            CloneableEditorSupport editor = (CloneableEditorSupport) result;
            BaseDocument document = (BaseDocument) editor.openDocument();
            XMLSyntaxSupport syntaxSupport = (XMLSyntaxSupport) document.getSyntaxSupport();

            int start = document.getStartPosition().getOffset();
            TokenItem item = syntaxSupport.getTokenChain(start, Math.min(start + 1, document.getLength()));
            if (item == null) {
                return null;
            }

            while (item != null) {
                TokenID tokenId = item.getTokenID();

                if (tokenId == XMLDefaultTokenContext.TAG) {

                    SyntaxElement element = syntaxSupport.getElementChain(item.getOffset() + 1);
                    String[] attributeValues = null; // Multiple attributes can have class name as values
                    boolean pkgValue = false; // To indicate the attributeValues are Java package, not full class name
                    if (element instanceof StartTag || element instanceof EmptyTag) {

                        Node theNode = (Node) element;
                        String nodeName = theNode.getNodeName();
                        String itemImage = item.getImage();
                        
                        if (searchingPackageName && nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.MAPPING_TAG) &&
                                    itemImage.contains(HibernateMappingXmlConstants.MAPPING_TAG)) {
                                // <class> element
                                attributeValues = new String[1];
                                attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.PACKAGE_ATTRIB);
                                pkgValue = true;
                        }
                        // Search the element/attrubutes that take class names
                        else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.CLASS_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.CLASS_TAG)) {
                            // <class> element
                            attributeValues = new String[1];
                            attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.NAME_ATTRIB); 
                        } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.ONE_TO_MANY_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.ONE_TO_MANY_TAG)) {
                            // <one-to-many> element
                            attributeValues = new String[1];
                            attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB); 
                        } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.COMPOSITE_ID_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.COMPOSITE_ID_TAG)) {
                            // <composite-id> element
                            attributeValues = new String[1];
                            attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB); 
                        } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.KEY_MANY_TO_ONE_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.KEY_MANY_TO_ONE_TAG)) {
                            // <key-many-to-one> element
                            attributeValues = new String[1];
                            attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB); 
                        } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.MANY_TO_ONE_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.MANY_TO_ONE_TAG)) {
                            // <many-to-one> element
                            attributeValues = new String[1];
                            attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB);
                        } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.ONE_TO_ONE_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.ONE_TO_ONE_TAG)) {
                            // <one-to-one> element
                            attributeValues = new String[1];
                            attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB); 
                        } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.COMPONENT_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.COMPONENT_TAG)) {
                            // <component> element
                            attributeValues = new String[1];
                            attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB); 
                        } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.SUBCLASS_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.SUBCLASS_TAG)) {
                            // <subclass> element
                            attributeValues = new String[2];
                            attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.NAME_ATTRIB); 
                            attributeValues[1] = getAttributeValue(theNode, HibernateMappingXmlConstants.EXTENDS_ATTRIB);
                        } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.JOINED_SUBCLASS_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.JOINED_SUBCLASS_TAG)) {
                            // <joined-subclass> element
                            attributeValues = new String[3];
                            attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.NAME_ATTRIB); 
                            attributeValues[1] = getAttributeValue(theNode, HibernateMappingXmlConstants.EXTENDS_ATTRIB);
                            attributeValues[2] = getAttributeValue(theNode, HibernateMappingXmlConstants.PERSISTER_ATTRIB);
                        } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.UNION_SUBCLASS_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.UNION_SUBCLASS_TAG)) {
                            // <union-subclass> element
                            attributeValues = new String[3];
                            attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.NAME_ATTRIB); 
                            attributeValues[1] = getAttributeValue(theNode, HibernateMappingXmlConstants.EXTENDS_ATTRIB);
                            attributeValues[2] = getAttributeValue(theNode, HibernateMappingXmlConstants.PERSISTER_ATTRIB);
                        } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.IMPORT_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.IMPORT_TAG)) {
                            // <import> element
                            attributeValues = new String[1];
                            attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB);
                        } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.MANY_TO_MANY_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.MANY_TO_MANY_TAG)) {
                            // <many-to-many> element
                            attributeValues = new String[1];
                            attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB);
                        } 
                        
                        if (attributeValues != null ) {
                            for( int i = 0; i < attributeValues.length; i ++ ) {

                                String text = document.getText(item.getOffset(), element.getElementLength());
                                
                                String value = attributeValues[i];
                                if(searchingPackageName && !pkgValue) {
                                    value = getPackageName(value);
                                }

                                if (value != null && value.equals(searchingForName)){
                                    
                                    // TODO: can not just do indexof. It does not work correctly if there are multiple
                                    // attributes have the same class searchingForName. Though, it does not make sense to have such case.
                            
                                    if (text.indexOf(searchingForName) != -1) {
                                        int startOffset = item.getOffset() + text.indexOf(searchingForName);
                                        int endOffset = startOffset + searchingForName.length();

                                        PositionBounds loc = new PositionBounds(editor.createPositionRef(startOffset, Bias.Forward),
                                                editor.createPositionRef(endOffset, Bias.Forward));

                                        foundPlaces.add(new OccurrenceItem(loc, text));
                                    }
                                }
                            }
                        }
                    }
                }
                item = item.getNext();
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }

        return foundPlaces;
    }
    
    private static List<OccurrenceItem> getJavaFieldOccurPlaces(FileObject mappingFile, String className, String fieldName) {
        List<OccurrenceItem> foundPlaces = new ArrayList<OccurrenceItem>();
        try {
            // Get the document for this file
            DataObject dataObject = DataObject.find(mappingFile);
            EditorCookie result = dataObject.getCookie(EditorCookie.class);
            if (result == null) {
                throw new IllegalStateException("File " + mappingFile + " does not have an EditorCookie.");
            }

            CloneableEditorSupport editor = (CloneableEditorSupport) result;
            BaseDocument document = (BaseDocument) editor.openDocument();
            XMLSyntaxSupport syntaxSupport = (XMLSyntaxSupport) document.getSyntaxSupport();

            int start = document.getStartPosition().getOffset();
            TokenItem item = syntaxSupport.getTokenChain(start, Math.min(start + 1, document.getLength()));
            if (item == null) {
                return null;
            }

            String text = null;
            while (item != null) {
                TokenID tokenId = item.getTokenID();

                if (tokenId == XMLDefaultTokenContext.TAG) {
                    // Did we find the <class> element

                    SyntaxElement element = syntaxSupport.getElementChain(item.getOffset() + 1);
                    String nameAttribValue = null;
                    if (element instanceof StartTag || element instanceof EmptyTag) {

                        Node theNode = (Node) element;
                        String nodeName = theNode.getNodeName();
                        String itemImage = item.getImage();

                        if ((nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.PROPERTY_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.PROPERTY_TAG)) ||
                                
                                (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.ID_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.ID_TAG)) ||
                                
                                (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.SET_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.SET_TAG)) ||
                                
                                (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.COMPOSITE_ID_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.COMPOSITE_ID_TAG)) ||
                                
                                (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.KEY_PROPERTY_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.KEY_PROPERTY_TAG)) ||
                                
                                (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.KEY_MANY_TO_ONE_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.KEY_MANY_TO_ONE_TAG)) ||
                                
                                (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.VERSION_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.VERSION_TAG)) ||
                                
                                (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.TIMESTAMP_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.TIMESTAMP_TAG)) ||
                                
                                (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.MANY_TO_ONE_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.MANY_TO_ONE_TAG)) ||
                                
                                (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.ONE_TO_ONE_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.ONE_TO_ONE_TAG)) ||
                                
                                (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.COMPONENT_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.COMPONENT_TAG)) ||
                                
                                (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.ANY_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.ANY_TAG)) ||
                                
                                (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.MAP_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.MAP_TAG)) ||
                                
                                (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.LIST_TAG) &&
                                itemImage.contains(HibernateMappingXmlConstants.LIST_TAG))) {
                            
                            nameAttribValue = getAttributeValue(theNode, HibernateMappingXmlConstants.NAME_ATTRIB);
                            
                            if (nameAttribValue != null && nameAttribValue.equals(fieldName)) {
                                
                                // Check class name
                                if (HibernateEditorUtil.getClassName(theNode).equals(className)) {
                                    text = document.getText(item.getOffset(), element.getElementLength());

                                    // find the offset for the field name
                                    int index = text.indexOf(fieldName);
                                    int startOffset = item.getOffset() + index;
                                    int endOffset = startOffset + fieldName.length();
                                    PositionBounds loc = new PositionBounds(editor.createPositionRef(startOffset, Bias.Forward),
                                            editor.createPositionRef(endOffset, Bias.Forward));

                                    foundPlaces.add( new OccurrenceItem(loc, text) );
                                }
                            }
                        }
                    }
                }
                item = item.getNext();
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }

        return foundPlaces;
    }

    public static final class OccurrenceItem {

        private String text;
        private PositionBounds location;

        public OccurrenceItem(PositionBounds location, String text) {
            this.location = location;
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        public PositionBounds getLocation() {
            return this.location;
        }
    }

    public static boolean anyHibernateMappingFiles(FileObject fo) {
        Project proj = org.netbeans.api.project.FileOwnerQuery.getOwner(fo);
        HibernateEnvironment env = new HibernateEnvironment(proj);
        List<FileObject> mFileObjs = env.getAllHibernateMappingFileObjects();
        if (mFileObjs == null || mFileObjs.size() == 0) {
            // OK, no mapping files at all. 
            return false;
        } else {
            return true;
        }
    }

    private static String getAttributeValue(Node node, String attributeName) {
        if (node == null) {
            return null;
        }

        NamedNodeMap attribs = node.getAttributes();
        if (attribs != null && attribs.getNamedItem(attributeName) != null) { // NOI18N
            return attribs.getNamedItem(attributeName).getNodeValue(); // NOI18N
        }

        return null;
    }
    
    private static String getPackageName(String binaryClassName) {
        if(binaryClassName == null)
            return null;
        
        int lastDot = binaryClassName.lastIndexOf(".");
        return binaryClassName.substring(0, lastDot);
    }
}
