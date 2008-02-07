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
package org.netbeans.modules.cnd.refactoring.actions;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * perform instant rename action
 * 
 * @author Jan Lahoda
 * @author Vladimir Voskresensky
 */
public class InstantRenamePerformer implements DocumentListener, KeyListener {

    private SyncDocumentRegion region;
    private Document doc;
    private JTextComponent target;
    private final PositionsBag bag;
    
    /** Creates a new instance of InstantRenamePerformer */
    private InstantRenamePerformer(JTextComponent target,  Collection<CsmReference> highlights, int caretOffset) throws BadLocationException {
	this.target = target;
	this.doc = target.getDocument();
	
	MutablePositionRegion mainRegion = null;
	List<MutablePositionRegion> regions = new ArrayList<MutablePositionRegion>();
        bag = new PositionsBag(doc);
        
	for (CsmReference h : highlights) {
	    Position start = NbDocument.createPosition(doc, h.getStartOffset(), Bias.Backward);
	    Position end = NbDocument.createPosition(doc, h.getEndOffset(), Bias.Forward);
	    MutablePositionRegion current = new MutablePositionRegion(start, end);
	    
	    if (isIn(current, caretOffset)) {
		mainRegion = current;
	    } else {
		regions.add(current);
	    }
	    
            bag.addHighlight(start, end, COLORING);
	}
	
	if (mainRegion == null) {
	    throw new IllegalArgumentException("No highlight contains the caret."); // NOI18N
	}
	
	regions.add(0, mainRegion);
	
	this.region = new SyncDocumentRegion(doc, regions);
	
        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).setPostModificationDocumentListener(this);
        }
        
	target.addKeyListener(this);
	
	target.putClientProperty(InstantRenamePerformer.class, this);
	
        getHighlightsBag(doc).setHighlights(bag);
        
        target.select(mainRegion.getStartOffset(), mainRegion.getEndOffset());
    }
    
//    private FileObject getFileObject() {
//	DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
//	
//	if (od == null)
//	    return null;
//	
//	return od.getPrimaryFile();
//    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(InstantRenamePerformer.class, key);
    }
    
    public static void invokeInstantRename(JTextComponent target) {
        try {
            final int caret = target.getCaretPosition();   
            Document doc = target.getDocument();
            DataObject dobj = NbEditorUtilities.getDataObject(doc);
            CsmFile file = CsmUtilities.getCsmFile(dobj, false);
            if (file == null) {
                Utilities.setStatusBoldText(target, getString("no-instant-rename")); // NOI18N
                return;
            }
            CsmReference ref = CsmReferenceResolver.getDefault().findReference(file, caret);
            if (ref == null) {
                Utilities.setStatusBoldText(target, getString("no-instant-rename")); // NOI18N
                return;
            }
            
            if (allowInstantRename(ref)) {
                Collection<CsmReference> changePoints = computeChangePoints(ref);
                //String ident = ref.getText();
                doInstantRename(changePoints, target, caret);
            } else {
                doFullRename(dobj, target);
            }
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    private static boolean allowInstantRename(CsmReference ref) {
        CsmReferenceResolver.Scope scope = CsmReferenceResolver.getDefault().fastCheckScope(ref);
        if (scope == CsmReferenceResolver.Scope.LOCAL) {
            return true;
        } else {
            return false;
        }
    }
    
    private static void doFullRename(DataObject dobj, JTextComponent target) {
        EditorCookie ec = dobj.getCookie(EditorCookie.class);
        Node n = dobj.getNodeDelegate();
        if (n == null) {
             Utilities.setStatusBoldText(target, getString("no-instant-rename")); // NOI18N
             return;
        }
        InstanceContent ic = new InstanceContent();
        if (ec != null) {
            ic.add(ec);
        }
        ic.add(n);
        Lookup actionContext = new AbstractLookup(ic);
        
        Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(actionContext);
        a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
    }
    
    private static void doInstantRename(Collection<CsmReference> changePoints, JTextComponent target, int caret) throws BadLocationException {
        performInstantRename(target, changePoints, caret);
    }
    
    static Collection<CsmReference> computeChangePoints(CsmReference ref) {
        CsmObject resolved = ref.getReferencedObject();
        if (resolved == null) {
            return Collections.<CsmReference>emptyList();
        }
        CsmFile file = ref.getContainingFile();
        Collection<CsmReference> out = CsmReferenceRepository.getDefault().getReferences(resolved, file, true);
        return out;
    }
    
//    private static boolean allowInstantRename(Element e) {
//        if (org.netbeans.modules.java.editor.semantic.Utilities.isPrivateElement(e)) {
//            return true;
//        }
//        
//        //#92160: check for local classes:
//        if (e.getKind() == ElementKind.CLASS) {//only classes can be local
//            Element enclosing = e.getEnclosingElement();
//            
//            return LOCAL_CLASS_PARENTS.contains(enclosing.getKind());
//        }
//        
//        return false;
//    }
//    
//    private static boolean overlapsWithGuardedBlocks(Document doc, Set<Token<JavaTokenId>> highlights) {
//        if (!(doc instanceof GuardedDocument))
//            return false;
//        
//        GuardedDocument gd = (GuardedDocument) doc;
//        MarkBlock current = gd.getGuardedBlockChain().getChain();
//        
//        while (current != null) {
//            for (Token<JavaTokenId> h : highlights) {
//                if ((current.compare(h.offset(null), h.offset(null) + h.length()) & MarkBlock.OVERLAP) != 0) {
//                    return true;
//                }
//            }
//            
//            current = current.getNext();
//        }
//        
//        return false;
//    }
//    
//    private static final Set<ElementKind> LOCAL_CLASS_PARENTS = EnumSet.of(ElementKind.CONSTRUCTOR, ElementKind.INSTANCE_INIT, ElementKind.METHOD, ElementKind.STATIC_INIT);
//    
//    
    public static void performInstantRename(JTextComponent target, Collection<CsmReference> highlights, int caretOffset) throws BadLocationException {
	new InstantRenamePerformer(target, highlights, caretOffset);
    }

    private boolean isIn(MutablePositionRegion region, int caretOffset) {
	return region.getStartOffset() <= caretOffset && caretOffset <= region.getEndOffset();
    }
    
    private boolean inSync;
    
    public synchronized void insertUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
	inSync = true;
	region.sync(0);
        getHighlightsBag(doc).setHighlights(bag);
	inSync = false;
	target.repaint();
    }

    public synchronized void removeUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
        //#89997: do not sync the regions for the "remove" part of replace selection,
        //as the consequent insert may use incorrect offset, and the regions will be synced
        //after the insert anyway.
        if (doc.getProperty(BaseKit.DOC_REPLACE_SELECTION_PROPERTY) != null) {
            return ;
        }
        
	inSync = true;
	region.sync(0);
        getHighlightsBag(doc).setHighlights(bag);
	inSync = false;
	target.repaint();
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void caretUpdate(CaretEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
	if (   (e.getKeyCode() == KeyEvent.VK_ESCAPE && e.getModifiers() == 0) 
            || (e.getKeyCode() == KeyEvent.VK_ENTER  && e.getModifiers() == 0)) {
	    release();
	    e.consume();
	}
    }

    public void keyReleased(KeyEvent e) {
    }

    private void release() {
	target.putClientProperty(InstantRenamePerformer.class, null);
        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).setPostModificationDocumentListener(null);
        }
	target.removeKeyListener(this);
        getHighlightsBag(doc).clear();

	region = null;
	doc = null;
	target = null;
    }

    private static final AttributeSet COLORING = AttributesUtilities.createImmutable(StyleConstants.Background, new Color(138, 191, 236));
    
    public static PositionsBag getHighlightsBag(Document doc) {
        PositionsBag bag = (PositionsBag) doc.getProperty(InstantRenamePerformer.class);
        if (bag == null) {
            doc.putProperty(InstantRenamePerformer.class, bag = new PositionsBag(doc));
        }
        return bag;
    }
    
}
