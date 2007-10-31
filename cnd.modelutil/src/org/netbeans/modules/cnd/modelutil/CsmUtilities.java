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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelutil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmProject;
import java.awt.Container;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
        

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmUtilities {

    /* ------------------ MODIFIERS ---------------------- */
     
    /**
     * The <code>int</code> value representing the <code>public</code> 
     * modifier.
     */   
    public static final int PUBLIC           = 0x00000001;

    /**
     * The <code>int</code> value representing the <code>private</code> 
     * modifier.
     */    
    public static final int PRIVATE          = 0x00000002;

    /**
     * The <code>int</code> value representing the <code>protected</code> 
     * modifier.
     */    
    public static final int PROTECTED        = 0x00000004;    

    /**
     * The <code>int</code> value representing the <code>static</code> 
     * modifier.
     */    
    public static final int STATIC           = 0x00000008;
    
    // the bit for local member. the modificator is not saved within this bit.
    public static final int LOCAL_MEMBER_BIT = 0x00000100;

    // the bit for local member. the modificator is not saved within this bit.
    public static final int CONST_MEMBER_BIT = 0x00000200;
    
    public static final int PUBLIC_LEVEL = 2;
    public static final int PROTECTED_LEVEL = 1;
    public static final int PRIVATE_LEVEL = 0;

    public static final int GLOBAL           = 0x00001000;
    public static final int LOCAL            = 0x00002000;
    public static final int FILE_LOCAL       = 0x00004000;
    public static final int MEMBER           = 0x00008000;
    public static final int ENUMERATOR       = 0x00000400;
    public static final int CONSTRUCTOR      = 0x00000800;
    public static final int DESTRUCTOR       = 0x00020000;
    public static final int OPERATOR         = 0x00040000;
    public static final int MACRO            = 0x00010000;
    public static final int EXTERN           = 0x00080000;

    public static final boolean DEBUG = Boolean.getBoolean("csm.utilities.trace.summary") || 
                                        Boolean.getBoolean("csm.utilities.trace");
    
    public static int getModifiers(CsmObject obj) {
	int mod = 0;
        if (CsmKindUtilities.isClassMember(obj)) {
            mod |= CsmUtilities.getMemberModifiers((CsmMember)obj);
        } else if (CsmKindUtilities.isFunctionDefinition(obj)) {
            CsmFunctionDefinition fun = (CsmFunctionDefinition)obj;
            if (CsmKindUtilities.isClassMember(fun.getDeclaration())) {
                mod |= CsmUtilities.getMemberModifiers((CsmMember)fun.getDeclaration());
            }            
        } else {
            if (CsmKindUtilities.isGlobalVariable(obj)||CsmKindUtilities.isGlobalVariable(obj)){
                mod |= GLOBAL;
            }
            if (CsmKindUtilities.isFileLocalVariable(obj)){
                mod |= FILE_LOCAL;
            }
            if (CsmKindUtilities.isEnumerator(obj)){
                mod |= ENUMERATOR;
            }
        } 
        if (CsmKindUtilities.isOperator(obj)) {
            mod |= OPERATOR;
        }
        // add contst info for variables
        if (CsmKindUtilities.isVariable(obj)) {
            CsmVariable var = (CsmVariable)obj;
            // parameters could be with null type if it's varagrs "..."
            mod |= (var.getType() != null && var.getType().isConst()) ? CONST_MEMBER_BIT : 0;
            if (var.isExtern()){
                mod |= EXTERN;
            }
        }
        return mod;
    }
    
    public static int getMemberModifiers(CsmMember member) {
	int mod = 0;
	CsmVisibility visibility = member.getVisibility();
	if (CsmVisibility.PRIVATE == visibility) {
	    mod = PRIVATE;
	} else if (CsmVisibility.PROTECTED == visibility) {
	    mod = PROTECTED;
	} else if (CsmVisibility.PUBLIC == visibility) {
	    mod = PUBLIC;
	}
	
	if (member.isStatic()) {
	    mod |= STATIC;
	}
        mod |= MEMBER;
        if (CsmKindUtilities.isConstructor(member)){
            mod |= CONSTRUCTOR;
        } else if (CsmKindUtilities.isDestructor(member)){
            mod |= DESTRUCTOR;
        }
	return mod;
    }
    
    /** Get level from modifiers. 
     * @param modifiers 
     * @return one of correspond constant (PUBLIC_LEVEL, PROTECTED_LEVEL, PRIVATE_LEVEL)
     */
    public static int getLevel(int modifiers) {
        if ((modifiers & PUBLIC) != 0) {
            return PUBLIC_LEVEL;
        } else if ((modifiers & PROTECTED) != 0) {
            return PROTECTED_LEVEL;
        } else {
            return PRIVATE_LEVEL;
        }
    }   
    
    public static boolean isPrimitiveClassName(String s) {
	return CCTokenContext.isTypeOrVoid(s);
    }
    
    public static boolean isPrimitiveClass(CsmClassifier c) {
	return c.getKind() == CsmDeclaration.Kind.BUILT_IN;
    }
    
    //====================

    public static CsmFile getCsmFile(Node node, boolean waitParsing) {
        return getCsmFile(node.getLookup().lookup(DataObject.class), waitParsing);
    }

    public static JEditorPane[] getOpenedPanesInEQ(final EditorCookie ec) {
        assert ec != null;
        final JEditorPane[][] panes = {null};
        if (SwingUtilities.isEventDispatchThread()) {
            panes[0] = ec.getOpenedPanes();
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        panes[0] = ec.getOpenedPanes();
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return panes[0];
    }
    
    public static File getFile(Document bDoc) {
        DataObject dobj = NbEditorUtilities.getDataObject(bDoc);
        if (dobj != null && dobj.isValid()) {
            FileObject fo = dobj.getPrimaryFile();
            if (fo != null) {
                File file = FileUtil.toFile(fo);
                return file;
            }
        }
        return null;
    }
    
    public static CsmFile getCsmFile(JTextComponent comp, boolean waitParsing) {
        return comp == null ? null : getCsmFile(comp.getDocument(), waitParsing);
    }
    
    public static CsmFile getCsmFile(Document bDoc, boolean waitParsing) {
	CsmFile csmFile = null;
	try {
	    csmFile = getCsmFile(NbEditorUtilities.getDataObject(bDoc), waitParsing);
	} catch (NullPointerException exc) {
	    exc.printStackTrace();
	}
	return csmFile;
    }
    
    public static CsmProject getCsmProject(Document bDoc) {
	CsmProject csmProject = null;
	try {
	    csmProject = getCsmFile(bDoc, false).getProject();
	} catch (NullPointerException exc) {
	    exc.printStackTrace();
	}
	return csmProject;
    }
    
    public static CsmFile[] getCsmFiles(DataObject dobj) {
	if( dobj != null && dobj.isValid()) {
            try {
                NativeFileItemSet set = dobj.getLookup().lookup(NativeFileItemSet.class);
                if (set == null) {
                    FileObject fo = dobj.getPrimaryFile();
                    if (fo != null) {
                        File file = FileUtil.toFile(fo);
                        // the file can null, for example, when we edit templates
                        if (file != null) {
                            file = FileUtil.normalizeFile(file);
                            CsmFile csmFile = CsmModelAccessor.getModel().findFile(file.getAbsolutePath());
                            if (csmFile != null) {
                                return new CsmFile[]{csmFile};
                            }
                        }
                    }
                } else {
                    List<CsmFile> l = new ArrayList<CsmFile>(set.size());
                    for (NativeFileItem item : set) {
                        CsmProject csmProject = CsmModelAccessor.getModel().getProject(item.getNativeProject());
                        if (csmProject != null) {
                            CsmFile file = csmProject.findFile(item.getFile().getAbsolutePath());
                            if (file != null) {
                                l.add(file);
                            }
                        }
                    }
                    return l.toArray(new CsmFile[l.size()]);
                }
            } catch (IllegalStateException ex){
                // dobj can be invalid
            }
	}
	return new CsmFile[0];
    }
    
    public static CsmFile[] getCsmFiles(FileObject fo) {
	try {
	    return getCsmFiles(DataObject.find(fo));
	} catch (DataObjectNotFoundException ex) {
	    return new CsmFile[0];
	}
    }
    
    
    public static CsmFile getCsmFile(DataObject dobj, boolean waitParsing) {
	CsmFile[] files = getCsmFiles(dobj);
	if( files == null || files.length == 0 ) {
	    return null;
	}
	else {
	    if( waitParsing ) {
		try {
		    files[0].scheduleParsing(true);
		} catch (InterruptedException ex) {		    
		    // ignore
		}
	    }
	    return files[0];
	}
    }
    
    public static CsmFile getCsmFile(FileObject fo, boolean waitParsing) {
	if( fo == null ) {
	    return null;
	}
	else {
	    try {
		return getCsmFile(DataObject.find(fo), waitParsing);
	    } catch (DataObjectNotFoundException ex) {
		return null;
	    }
	}
    }
    

    public static FileObject getFileObject(CsmFile csmFile) {
        FileObject fo = null;
        if (csmFile != null) {
            try {
                try {
                    fo = FileUtil.toFileObject(new File(csmFile.getAbsolutePath()).getCanonicalFile());
                } catch (IOException e) {
                    fo = FileUtil.toFileObject(FileUtil.normalizeFile(new File(csmFile.getAbsolutePath())));
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
        return fo;
    }

    public static DataObject getDataObject(CsmFile csmFile) {
        return getDataObject(getFileObject(csmFile));
    }

    public static DataObject getDataObject(FileObject fo) {
        DataObject dob = null;
        if (fo != null) {
            try {
                dob = DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
            }
        }
        return dob;
    }
    
    public static PositionBounds createPositionBounds(CsmOffsetable csmObj) {
        if (csmObj == null) {
            return null;
        }
        CloneableEditorSupport ces = findCloneableEditorSupport(csmObj.getContainingFile());
        if (ces != null) {
            PositionRef beg = ces.createPositionRef(csmObj.getStartOffset(), Position.Bias.Forward);
            PositionRef end = ces.createPositionRef(csmObj.getEndOffset(), Position.Bias.Backward);
            return new PositionBounds(beg, end);
        }
        return null;
    }
    
    public static CloneableEditorSupport findCloneableEditorSupport(CsmFile csmFile) {
        DataObject dob = getDataObject(csmFile);
        return findCloneableEditorSupport(dob);
    }
    
    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Object obj = dob.getCookie(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        return null;
    }    
    //====================================

    public static CsmFile findHeaderFile(CsmFile file) {
        return findCsmFile(file, true);
    }

    public static CsmFile findSourceFile(CsmFile file) {
        return findCsmFile(file, false);
    }
    
    public static CsmFile toggleFileKind(CsmFile file) {
        assert (file != null) : "can't be null file";
        boolean header = isHeaderFile(file);
        CsmFile toggledFile = findCsmFile(file, !header);
        return toggledFile == null ? file : toggledFile;
    }
    
    public static boolean isHeaderFile(CsmFile file) {
        assert (file != null) : "can't be null file";
        String path = file.getAbsolutePath();
        String ext = FileUtil.getExtension(path);
        return isHeaderExt(ext);
    }
    
    public static boolean isSourceFile(CsmFile file) {
        assert (file != null) : "can't be null file";
        return !isHeaderFile(file);
    }
    
    //====================================
    
    //====================================
    
    private static String headerExts[] = {"h", "hh", "hpp"}; //NOI18N
    private static String sourceExts[] = {"c", "cc", "cpp"}; //NOI18N
   /** default extension separator */
    private static final char EXT_SEP = '.';
    
    protected static CsmFile findCsmFile(CsmFile file, boolean header) {
        String path = file.getAbsolutePath();
        String ext = FileUtil.getExtension(path);
        CsmFile outFile = null;
        if (ext.length() > 0) {
            String pathWoExt = path.substring(0, path.length() - ext.length() - 1);
            String toggledFileName;
            if (header) {
                toggledFileName = findHeaderFileName(pathWoExt);
            } else {
                toggledFileName = findSourceFileName(pathWoExt);
            }
            if (toggledFileName != null && toggledFileName.length() > 0) {
                outFile = file.getProject().findFile(toggledFileName);
            }
        }
        return outFile;
    }
    
    protected static String findHeaderFileName(String pathWoExt) {
        for (int i = 0; i < headerExts.length; i++) {
            String path = new StringBuilder(pathWoExt).append(EXT_SEP).append(headerExts[i]).toString();
            File test = new File(path);
            if (test.exists()) {
                return path;
            }
        }
        return "";
    }
    
    protected static String findSourceFileName(String pathWoExt) {
        for (int i = 0; i < sourceExts.length; i++) {
            String path = new StringBuilder(pathWoExt).append(EXT_SEP).append(sourceExts[i]).toString();
            File test = new File(path);
            if (test.exists()) {
                return path;
            }
        }
        return "";
    }   
    
    protected static boolean isHeaderExt(String ext) {
        for (int i = 0; i < headerExts.length; i++) {
            if (ext.compareToIgnoreCase(headerExts[i]) == 0) {
                return true;
            }
        }
        return false;
    }
    
    protected static boolean isSourcesExt(String ext) {
        for (int i = 0; i < sourceExts.length; i++) {
            if (ext.compareToIgnoreCase(sourceExts[i]) == 0) {
                return true;
            }
        }
        return false;
    }    

    //==================== open elemen's definition/declaration ================
    
    /* 
     * opens source file correspond to input object and set caret on
     * start offset position
     */
    public static boolean openSource(CsmObject element) {
        if (CsmKindUtilities.isOffsetable(element)) {
            return openAtElement((CsmOffsetable)element, false);
        } else if (CsmKindUtilities.isFile(element)) {
            final CsmFile file = (CsmFile)element;
            CsmOffsetable fileTarget = new FileTarget(file);
            return openAtElement(fileTarget, false);
        }
        return false;
    }    
    
//    private static boolean openAtElement(CsmOffsetable element) {
//        return openAtElement(element, true);
//    }
    
    private static boolean openAtElement(final CsmOffsetable element, final boolean jumpLineStart) {
        final DataObject dob = getDataObject(element.getContainingFile());
        if (dob != null) {
            final EditorCookie.Observable ec = dob.getCookie(EditorCookie.Observable.class);
            if (ec != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        NbEditorUtilities.addJumpListEntry(dob);
                        JEditorPane[] panes = ec.getOpenedPanes();
                        boolean opened = true;
                        if (panes != null && panes.length >= 0) {
                            //editor already opened, so just select
                            opened = true;
                        } else {
                            // editor not yet opened
                            
                            // XXX: vv159170 commented out the ollowing code, because on the time
                            // of firing even no chance to get opened panes yet...
//                            ec.addPropertyChangeListener(new PropertyChangeListener() {
//                                public void propertyChange(PropertyChangeEvent evt) {
//                                    if (EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
//                                        final JEditorPane[] panes = ec.getOpenedPanes();
//                                        if (panes != null && panes.length > 0) {
//                                            selectElementInPane(panes[0], element, true);
//                                        }
//                                        ec.removePropertyChangeListener(this);
//                                    }
//                                }
//                            });
                            opened = false;
                            ec.open();
                            // XXX: get panes here instead of in listener
                            panes = ec.getOpenedPanes();
                        }
                        if (panes != null && panes.length > 0) {
                            selectElementInPane(panes[0], element, !opened, jumpLineStart);
                        }                        
                    }
                    
                });
            }
            return true;
        }
        return false;
    }
    
    /** Jumps to element in given editor pane. When delayProcessing is
     * specified, waits for real visible open before jump
     */
    private static void selectElementInPane(final JEditorPane pane, final CsmOffsetable element, 
                                            boolean delayProcessing, final boolean jumpLineStart) {
        //final Cursor editCursor = pane.getCursor();
        //pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (delayProcessing) {
            // [dafe] I don't know why, but editor guys are waiting for focus
            // in delay processing, so I will do the same
            pane.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            jumpToElement(pane, element, jumpLineStart);
                        }
                    });
                    pane.removeFocusListener(this);
                }
            });
        } else {
            // immediate processing
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    jumpToElement(pane, element, jumpLineStart);
                }
            });
            // try to activate outer TopComponent
            Container temp = pane;
            while (!(temp instanceof TopComponent)) {
                temp = temp.getParent();
            }
            ((TopComponent) temp).requestActive();
       }
    }
    
    
//    /** Jumps to element on given editor pane. Call only outside AWT thread!
//     */
//    private static void jumpToElement(JEditorPane pane, CsmOffsetable element) {
//        jumpToElement(pane, element, false);
//    }
    
    private static void jumpToElement(JEditorPane pane, CsmOffsetable element, boolean jumpLineStart) {
        int start = jumpLineStart ? lineToPosition(pane, element.getStartPosition().getLine()-1) : element.getStartOffset();
        if(pane.getDocument() != null && start >= 0 && start < pane.getDocument().getLength()) {
            pane.setCaretPosition(start);
            if (DEBUG) System.err.println("I'm going to "+start+" for element"+getElementJumpName(element));
        }
        StatusDisplayer.getDefault().setStatusText(""); // NOI18N
    }
    
    // NB document independent method
    private static int lineToPosition(JEditorPane pane, int docLine) {
        Document doc = pane.getDocument();
        int lineSt = 0;
        if (doc instanceof BaseDocument) {
            // use NB utilities for NB documents
            lineSt = Utilities.getRowStartFromLineOffset((BaseDocument)doc, docLine);
        } else {
            // not NB document, count lines
            int len = doc.getLength();
            try {
                String text = doc.getText(0, len);
                boolean afterEOL = false;
                for( int i = 0; i < len; i++ ) {
                    char c = text.charAt(i);
                    if( c == '\n') {
                        docLine--;
                        if( docLine == 0 ) {
                            return lineSt;
                        }
                        afterEOL = true;
                    } else if (afterEOL) {
                        lineSt = i;
                        afterEOL = false;
                    }
                }
            }
            catch( BadLocationException e ) {
            }
        }
        return lineSt;
    } 
    
    public static String getElementJumpName(CsmObject element) {
        String text = "";
        if (element != null) {
            if (CsmKindUtilities.isNamedElement(element)) {
                text = ((CsmNamedElement) element).getName();
            } else if (CsmKindUtilities.isStatement((CsmObject)element)) {
                text = ((CsmStatement)element).getText();
            } else if (CsmKindUtilities.isOffsetable(element) ) {
                text = ((CsmOffsetable)element).getText();
            }
            if (text.length() > 0) {
                text = "\"" + text + "\""; // NOI18N
            }
        }
        return text;
    }    
    

    public static <T> Collection<T> merge(Collection<T> orig, Collection<T> newList) {
        orig = orig != null ? orig : new ArrayList<T>();
        if (newList != null && newList.size() > 0) {
            orig.addAll(newList);
        }
        return orig;
    }
    
    public static <T> boolean removeAll(Collection<T> dest, Collection<T> removeItems) {
        if (dest != null && removeItems != null) {
            return dest.removeAll(removeItems);
        }
        return false;
    }
    
    public static String getCsmName(CsmObject obj) {
        StringBuilder buf = new StringBuilder();
        if (CsmKindUtilities.isNamedElement(obj)) {
            CsmNamedElement named = (CsmNamedElement)obj;
            buf.append(" [name] ").append(named.getName()); // NOI18N
        } else {
            String simpleName = obj.getClass().getName();
            simpleName = simpleName.substring(simpleName.lastIndexOf(".")+1); // NOI18N
            buf.append(" [class] ").append(simpleName); // NOI18N
        }
        if (CsmKindUtilities.isDeclaration(obj)) {
            CsmDeclaration decl = (CsmDeclaration)obj;
            buf.append(" [kind] ").append(decl.getKind()); // NOI18N
        }  
        return buf.toString();
    }    
    
    //-------------------------------------------------------------------------
    
    /**
     * Gets function signature in the form that is shown to client
     * @param fun function, which signature should be returned
     * @return signature of the function
     */
    public static String getSignature(CsmFunction fun) {
	return getSignature(fun, true);
    }
    
    /**
     * Gets function signature in the form that is shown to client
     * @param fun function, which signature should be returned
     * @param showParamNames determines whether to include parameter names in signature
     * @return signature of the function
     */
    public static String getSignature(CsmFunction fun, boolean showParamNames) {
        StringBuilder sb = new StringBuilder(fun.isTemplate() ? ((CsmTemplate)fun).getDisplayName() : fun.getName());
        sb.append('(');
        boolean addComma = false;
        for( Iterator iter = fun.getParameters().iterator(); iter.hasNext(); ) {
            CsmParameter par = (CsmParameter) iter.next();
            if( addComma ) {
                sb.append(", "); // NOI18N
            } else {
                addComma = true;
            }
	    if (showParamNames) {
		sb.append(par.getDisplayText());
	    }
	    else {
		CsmType type = par.getType();
		if( type != null ) {
		    sb.append(type.getText());
		    //sb.append(' ');
		} else if (par.isVarArgs()){
		    sb.append("..."); // NOI18N
		}
	    }
        }        
        sb.append(')');
        if (CsmKindUtilities.isMethodDeclaration(fun)){
            if( ((CsmMethod) fun).isConst() ) {
                sb.append(" const"); // NOI18N
            }
        }
	// TODO: as soon as we extract APTStringManager into a separate module,
	// use string manager here.
	// For now it's client responsibility to do this
        //return NameCache.getString(sb.toString());
	return sb.toString();
    }
    
    //-----------------------------------------------------------------

    private static final class FileTarget implements CsmOffsetable {
        private CsmFile file;
        
        public FileTarget(CsmFile file) {
            this.file = file;
        }
        
        public CsmFile getContainingFile() {
            return file;
        }
        
        public int getStartOffset() {
            // start of the file
            return DUMMY_POSITION.getOffset();
        }
        
        public int getEndOffset() {
            // start of the file
            return DUMMY_POSITION.getOffset();
        }
        
        public CsmOffsetable.Position getStartPosition() {
            return DUMMY_POSITION;
        }
        
        public CsmOffsetable.Position getEndPosition() {
            return DUMMY_POSITION;
        }
        
        public String getText() {
            return "";
        }
        
    }
    
    private static final CsmOffsetable.Position DUMMY_POSITION = new CsmOffsetable.Position() {
        public int getOffset() {
            return -1;
        }

        public int getLine() {
            return -1;
        }

        public int getColumn() {
            return -1;
        }
    };    
}
