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

package org.netbeans.modules.spring.beans.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.beans.BeanInfo;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.util.SpringBeansUIs;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * A completion item shown in a valid code completion request 
 * in a Spring XML Configuration file
 * 
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public abstract class SpringXMLConfigCompletionItem implements CompletionItem {

    public static SpringXMLConfigCompletionItem createBeanRefItem(int substitutionOffset, String displayName, 
            SpringBean bean, FileObject containerFO) {
        return new BeanRefItem(substitutionOffset, displayName, bean, containerFO);
    }
    
    public static SpringXMLConfigCompletionItem createPackageItem(int substitutionOffset, String packageName, 
            boolean deprecated) {
        return new PackageItem(substitutionOffset, packageName, deprecated);
    }
    
    public static SpringXMLConfigCompletionItem createTypeItem(int substitutionOffset, TypeElement elem, ElementHandle<TypeElement> elemHandle, 
                boolean deprecated, boolean smartItem) {
        return new ClassItem(substitutionOffset, elem, elemHandle, deprecated, smartItem);
    }
    
    public static SpringXMLConfigCompletionItem createAttribValueItem(int substitutionOffset, String displayText, String docText) {
        return new AttribValueItem(substitutionOffset, displayText, docText);
    }
    
    public static SpringXMLConfigCompletionItem createFolderItem(int substitutionOffset, FileObject folder) {
        return new FolderItem(substitutionOffset, folder);
    }
    
    public static SpringXMLConfigCompletionItem createSpringXMLFileItem(int substitutionOffset, FileObject file) {
        return new FileItem(substitutionOffset, file);
    }
    
    protected int substitutionOffset;
    
    protected SpringXMLConfigCompletionItem(int substitutionOffset) {
        this.substitutionOffset = substitutionOffset;
    }
    
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            int caretOffset = component.getSelectionEnd();
            substituteText(component, substitutionOffset, caretOffset - substitutionOffset, null);
        }
    }
    
    protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
        BaseDocument doc = (BaseDocument) c.getDocument();
        CharSequence prefix = getInsertPrefix();
        String text = prefix.toString();
        if(toAdd != null) {
            text += toAdd;
        }
        
        doc.atomicLock();
        try {
            Position position = doc.createPosition(offset);
            doc.remove(offset, len);
            doc.insertString(position.getOffset(), text.toString(), null);
        } catch (BadLocationException ble) {
            // nothing can be done to update
        } finally {
            doc.atomicUnlock();
        }
    }

    public void processKeyEvent(KeyEvent evt) {
        
    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), 
                getRightHtmlText(), g, defaultFont);
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor, 
            Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), 
                getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    public CompletionTask createDocumentationTask() {
        return null;
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    public boolean instantSubstitution(JTextComponent component) {
        defaultAction(component);
        return true;
    }
    
    protected String getLeftHtmlText() {
        return null;
    }
    
    protected String getRightHtmlText() {
        return null;
    }
    
    protected ImageIcon getIcon() {
        return null;
    }
    
    private static class BeanRefItem extends SpringXMLConfigCompletionItem {

        private static final String CLASS_COLOR = "<font color=#808080>"; //NOI18N
        
        private String beanId;
        private String beanClass;
        private List<String> beanNames;
        private String displayName;
        private String beanLocFile;
        private Action goToBeanAction;
        private String leftText;
        
        public BeanRefItem(int substitutionOffset, String displayName, SpringBean bean, FileObject containerFO) {
            super(substitutionOffset);
            this.beanId = bean.getId();
            this.beanClass = bean.getClassName();
            this.beanNames = bean.getNames();
            if (bean.getLocation() != null) {
                File file = bean.getLocation().getFile();
                FileObject fo = FileUtil.toFileObject(file);
                if (fo != null) {
                    this.beanLocFile = FileUtil.getRelativePath(containerFO.getParent(), fo);
                }
            }
            goToBeanAction = SpringBeansUIs.createGoToBeanAction(bean);
            this.displayName = displayName;
        }
        
        public int getSortPriority() {
            return 100;
        }

        public CharSequence getSortText() {
            return displayName;
        }

        public CharSequence getInsertPrefix() {
            return displayName;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(displayName);
                sb.append(CLASS_COLOR);
                sb.append(" ("); // NOI18N
                if(this.beanClass != null) {
                    sb.append(beanClass);
                }
                sb.append(")"); // NOI18N
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            return beanLocFile;
        }
        
        @Override
        protected ImageIcon getIcon() {
            return new ImageIcon(Utilities.loadImage("org/netbeans/modules/spring/beans/resources/bean.gif")); // NOI18N
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {
                @Override
                protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                    CompletionDocumentation docItem = SpringXMLConfigCompletionDoc.createBeanRefDoc(beanId, 
                            beanNames, beanClass, beanLocFile, goToBeanAction);
                    resultSet.setDocumentation(docItem);
                    resultSet.finish();
                }
            });
        }        
    }
    
    public static final String COLOR_END = "</font>"; //NOI18N
    public static final String STRIKE = "<s>"; //NOI18N
    public static final String STRIKE_END = "</s>"; //NOI18N
    public static final String BOLD = "<b>"; //NOI18N
    public static final String BOLD_END = "</b>"; //NOI18N
    
    /**
     * Represents a class in the completion popup. 
     * 
     * Heavily derived from Java Editor module's JavaCompletionItem class
     * 
     */
    private static class ClassItem extends SpringXMLConfigCompletionItem {
        private static final String CLASS = "org/netbeans/modules/editor/resources/completion/class_16.png"; //NOI18N
        private static final String CLASS_COLOR = "<font color=#560000>"; //NOI18N
        private static final String PKG_COLOR = "<font color=#808080>"; //NOI18N
        
        private ElementHandle<TypeElement> elemHandle;
        private boolean deprecated;
        private String displayName;
        private String enclName;
        private String sortText;
        private String leftText;
        private boolean displayPackage;

        public ClassItem(int substitutionOffset, TypeElement elem, ElementHandle<TypeElement> elemHandle, 
                boolean deprecated, boolean smartItem) {
            super(substitutionOffset);
            this.elemHandle = elemHandle;
            this.deprecated = deprecated;
            this.displayName = smartItem ? elem.getSimpleName().toString() : getRelativeName(elem);
            this.enclName = getElementName(elem.getEnclosingElement(), true).toString();
            this.sortText = this.displayName + getImportanceLevel(this.enclName) + "#" + this.enclName; //NOI18N
            this.displayPackage = smartItem;
        }
        
        private String getRelativeName(TypeElement elem) {
            StringBuilder sb = new StringBuilder();
            sb.append(elem.getSimpleName().toString());
            Element parent = elem.getEnclosingElement();
            while(parent.getKind() != ElementKind.PACKAGE) {
                sb.insert(0, parent.getSimpleName().toString() + "."); // NOI18N
                parent = parent.getEnclosingElement();
            }
            
            return sb.toString();
        }
        
        public int getSortPriority() {
            return 200;
        }

        public CharSequence getSortText() {
            return sortText;
        }

        public CharSequence getInsertPrefix() {
            return elemHandle.getBinaryName();
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }
        
        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(getColor());
                if (deprecated)
                    sb.append(STRIKE);
                sb.append(displayName);
                if (deprecated)
                    sb.append(STRIKE_END);
                if (displayPackage && enclName != null && enclName.length() > 0) {
                    sb.append(COLOR_END);
                    sb.append(PKG_COLOR);
                    sb.append(" ("); //NOI18N
                    sb.append(enclName);
                    sb.append(")"); //NOI18N
                }
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }
        
        protected String getColor() {
            return CLASS_COLOR;
        }

        @Override
        protected ImageIcon getIcon() {
            return new ImageIcon(Utilities.loadImage(CLASS));
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {
                @Override
                protected void query(final CompletionResultSet resultSet, Document doc, int caretOffset) {
                    try {
                        JavaSource js = SpringXMLConfigEditorUtils.getJavaSource(doc);
                        if (js == null) {
                            return;
                        }

                        js.runUserActionTask(new Task<CompilationController>() {
                            public void run(CompilationController cc) throws Exception {
                                cc.toPhase(Phase.RESOLVED);
                                Element element = elemHandle.resolve(cc);
                                if (element == null) {
                                    return;
                                }
                                SpringXMLConfigCompletionDoc doc = SpringXMLConfigCompletionDoc.createJavaDoc(cc, element);
                                resultSet.setDocumentation(doc);
                            }
                        }, false);
                        resultSet.finish();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }, EditorRegistry.lastFocusedComponent());
        }
    }
    
    private static class PackageItem extends SpringXMLConfigCompletionItem {

        private static final String PACKAGE = "org/netbeans/modules/java/editor/resources/package.gif"; // NOI18N
        private static final String PACKAGE_COLOR = "<font color=#005600>"; //NOI18N
        private static ImageIcon icon;
        
        private boolean deprecated;
        private String simpleName;
        private String sortText;
        private String leftText;
        
        public PackageItem(int substitutionOffset, String packageFQN, boolean deprecated) {
            super(substitutionOffset);
            int idx = packageFQN.lastIndexOf('.'); // NOI18N
            this.simpleName = idx < 0 ? packageFQN : packageFQN.substring(idx + 1);
            this.deprecated = deprecated;
            this.sortText = this.simpleName + "#" + packageFQN; //NOI18N
        }

        public int getSortPriority() {
            return 50;
        }

        public CharSequence getSortText() {
            return sortText;
        }

        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
            if (evt.getID() == KeyEvent.KEY_TYPED) {
                if(evt.getKeyChar() == '.') { // NOI18N
                    Completion.get().hideDocumentation();
                    JTextComponent component = (JTextComponent)evt.getSource();
                    int caretOffset = component.getSelectionEnd();
                    substituteText(component, substitutionOffset, caretOffset - substitutionOffset, Character.toString(evt.getKeyChar()));
                    Completion.get().showCompletion();
                    evt.consume();
                }
            }
        }
        
        @Override
        protected ImageIcon getIcon(){
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(PACKAGE));
            return icon;            
        }
        
        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(PACKAGE_COLOR);
                if (deprecated)
                    sb.append(STRIKE);
                sb.append(simpleName);
                if (deprecated)
                    sb.append(STRIKE_END);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }
    }
    
    private static class AttribValueItem extends SpringXMLConfigCompletionItem {

        private String displayText;
        private String docText;
        
        public AttribValueItem(int substitutionOffset, String displayText, String docText) {
            super(substitutionOffset);
            this.displayText = displayText;
            this.docText = docText;
        }

        public int getSortPriority() {
            return 50;
        }

        public CharSequence getSortText() {
            return displayText;
        }

        public CharSequence getInsertPrefix() {
            return displayText;
        }

        @Override
        protected String getLeftHtmlText() {
            return displayText;
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {
                @Override
                protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                    if(docText != null) {
                        CompletionDocumentation documentation = SpringXMLConfigCompletionDoc.getAttribValueDoc(docText);
                        resultSet.setDocumentation(documentation);
                    }
                    resultSet.finish();
                }
            });
        }        
    }
    
    private static class FolderItem extends SpringXMLConfigCompletionItem {

        private FileObject folder;
        
        public FolderItem(int substitutionOffset, FileObject folder) {
            super(substitutionOffset);
            this.folder = folder;
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
            if (evt.getID() == KeyEvent.KEY_TYPED) {
                if(evt.getKeyChar() == '/') {
                    Completion.get().hideDocumentation();
                    JTextComponent component = (JTextComponent)evt.getSource();
                    int caretOffset = component.getSelectionEnd();
                    substituteText(component, substitutionOffset, caretOffset - substitutionOffset, Character.toString(evt.getKeyChar()));
                    Completion.get().showCompletion();
                    evt.consume();
                }
            }
        }
        
        public int getSortPriority() {
            return 300;
        }

        public CharSequence getSortText() {
            return folder.getName();
        }

        public CharSequence getInsertPrefix() {
            return folder.getName();
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }
        
        @Override
        protected ImageIcon getIcon() {
            return new ImageIcon(getTreeFolderIcon());
        }

        @Override
        protected String getLeftHtmlText() {
            return folder.getName();
        }
        
        private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; // NOI18N
        private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; // NOI18N
        
        /**
         * Returns default folder icon as {@link java.awt.Image}. Never returns
         * <code>null</code>.Adapted from J2SELogicalViewProvider
         */
        private static Image getTreeFolderIcon() {
            Image base = null;
            Icon baseIcon = UIManager.getIcon(ICON_KEY_UIMANAGER); // #70263
            if (baseIcon != null) {
                base = Utilities.icon2Image(baseIcon);
            } else {
                base = (Image) UIManager.get(ICON_KEY_UIMANAGER_NB); // #70263
                if (base == null) { // fallback to our owns                
                    final Node n = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
                    base = n.getIcon(BeanInfo.ICON_COLOR_16x16);                                 
                }
            }
            assert base != null;
            return base;
        }
    }
    
    private static class FileItem extends SpringXMLConfigCompletionItem {

        private FileObject file;

        public FileItem(int substitutionOffset, FileObject file) {
            super(substitutionOffset);
            this.file = file;
        }
        
        public int getSortPriority() {
            return 100;
        }

        public CharSequence getSortText() {
            return file.getNameExt();
        }

        public CharSequence getInsertPrefix() {
            return file.getNameExt();
        }

        @Override
        protected ImageIcon getIcon() {
            return new ImageIcon(Utilities.loadImage(
                    "org/netbeans/modules/spring/beans/resources/spring.png")); // NOI18N
        }

        @Override
        protected String getLeftHtmlText() {
            return file.getNameExt();
        }
    }
    
    public static CharSequence getElementName(Element el, boolean fqn) {
        if (el == null || el.asType().getKind() == TypeKind.NONE)
            return ""; //NOI18N
        return new ElementNameVisitor().visit(el, fqn);
    }
    
    private static class ElementNameVisitor extends SimpleElementVisitor6<StringBuilder,Boolean> {
        
        private ElementNameVisitor() {
            super(new StringBuilder());
        }

        @Override
        public StringBuilder visitPackage(PackageElement e, Boolean p) {
            return DEFAULT_VALUE.append((p ? e.getQualifiedName() : e.getSimpleName()).toString());
        }

	@Override
        public StringBuilder visitType(TypeElement e, Boolean p) {
            return DEFAULT_VALUE.append((p ? e.getQualifiedName() : e.getSimpleName()).toString());
        }        
    }
    
    public static int getImportanceLevel(String fqn) {
        int weight = 50;
        if (fqn.startsWith("java.lang") || fqn.startsWith("java.util")) // NOI18N
            weight -= 10;
        else if (fqn.startsWith("org.omg") || fqn.startsWith("org.apache")) // NOI18N
            weight += 10;
        else if (fqn.startsWith("com.sun") || fqn.startsWith("com.ibm") || fqn.startsWith("com.apple")) // NOI18N
            weight += 20;
        else if (fqn.startsWith("sun") || fqn.startsWith("sunw") || fqn.startsWith("netscape")) // NOI18N
            weight += 30;
        return weight;
    }
}
