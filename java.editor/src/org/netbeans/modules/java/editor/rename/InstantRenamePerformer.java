/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.java.editor.rename;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
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
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.modules.editor.java.JavaKit.JavaDeleteCharAction;
import org.netbeans.modules.java.editor.semantic.FindLocalUsagesQuery;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jan Lahoda
 */
public class InstantRenamePerformer implements DocumentListener, KeyListener {
    
    private SyncDocumentRegion region;
    private int span;
    private Document doc;
    private JTextComponent target;
    
    /** Creates a new instance of InstantRenamePerformer */
    private InstantRenamePerformer(JTextComponent target, Set<Token<JavaTokenId>> highlights, int caretOffset) throws BadLocationException {
	this.target = target;
	doc = target.getDocument();
	
	MutablePositionRegion mainRegion = null;
	List<MutablePositionRegion> regions = new ArrayList<MutablePositionRegion>();
        PositionsBag bag = new PositionsBag(doc);
        
	for (Token<JavaTokenId> h : highlights) {
	    Position start = NbDocument.createPosition(doc, h.offset(null), Bias.Backward);
	    Position end = NbDocument.createPosition(doc, h.offset(null) + h.length(), Bias.Forward);
	    MutablePositionRegion current = new MutablePositionRegion(start, end);
	    
	    if (isIn(current, caretOffset)) {
		mainRegion = current;
	    } else {
		regions.add(current);
	    }
	    
            bag.addHighlight(start, end, getSyncedTextBlocksHighlight());
	}
	
	if (mainRegion == null) {
	    throw new IllegalArgumentException("No highlight contains the caret.");
	}
	
	regions.add(0, mainRegion);
	
	region = new SyncDocumentRegion(doc, regions);
	
        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).setPostModificationDocumentListener(this);
        }
        
	target.addKeyListener(this);
	
	target.putClientProperty(InstantRenamePerformer.class, this);
	
        getHighlightsBag(doc).setHighlights(bag);
        
        target.select(mainRegion.getStartOffset(), mainRegion.getEndOffset());
        
        span = region.getFirstRegionLength();
    }
    
    public static void invokeInstantRename(JTextComponent target) {
        try {
            final int caret = target.getCaretPosition();
            String ident = Utilities.getIdentifier(Utilities.getDocument(target), caret);
            
            if (ident == null) {
                Utilities.setStatusBoldText(target, "Cannot perform instant rename here.");
                return;
            }
            
            DataObject od = (DataObject) target.getDocument().getProperty(Document.StreamDescriptionProperty);
            JavaSource js = JavaSource.forFileObject(od.getPrimaryFile());
            final boolean[] wasResolved = new boolean[1];
            @SuppressWarnings("unchecked")
            final Set<Token<JavaTokenId>>[] changePoints = new Set[1];
            
            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController controller) throws Exception {
                    if (controller.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0)
                        return;
                    
                    changePoints[0] = computeChangePoints(controller, caret, wasResolved);
                }
            }, true);
            
            if (wasResolved[0]) {
                if (changePoints[0] != null) {
                    doInstantRename(changePoints[0], target, caret, ident);
                } else {
                    doFullRename(od.getCookie(EditorCookie.class), od.getNodeDelegate());
                }
            } else {
                Utilities.setStatusBoldText(target, "Cannot perform instant rename here.");
            }
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    private static void doFullRename(EditorCookie ec, Node n) {
        
        InstanceContent ic = new InstanceContent();
        ic.add(ec);
        ic.add(n);
        Lookup actionContext = new AbstractLookup(ic);
        
        Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(actionContext);
        a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
    }
    
    private static void doInstantRename(Set<Token<JavaTokenId>> changePoints, JTextComponent target, int caret, String ident) throws BadLocationException {
        InstantRenamePerformer.performInstantRename(target, changePoints, caret);
    }
    
    static Set<Token<JavaTokenId>> computeChangePoints(final CompilationInfo info, final int caret, final boolean[] wasResolved) throws IOException {
        final Document doc = info.getDocument();
        
        if (doc == null)
            return null;
        
        final int[] adjustedCaret = new int[] {caret};
        
        doc.render(new Runnable() {
            public void run() {
                TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(info.getTokenHierarchy(), caret);
                
                ts.move(caret);
                
                if (ts.moveNext() && ts.token()!=null && ts.token().id() == JavaTokenId.IDENTIFIER) {
                    adjustedCaret[0] = ts.offset() + ts.token().length() / 2 + 1;
                }
            }
        });
        
        TreePath path = info.getTreeUtilities().pathFor(adjustedCaret[0]);
        
        //correction for int something[]:
        if (path != null && path.getParentPath() != null) {
            Kind leafKind = path.getLeaf().getKind();
            Kind parentKind = path.getParentPath().getLeaf().getKind();
            
            if (leafKind == Kind.ARRAY_TYPE && parentKind == Kind.VARIABLE) {
                long typeEnd = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), path.getLeaf());
                long variableEnd = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), path.getLeaf());
                
                if (typeEnd == variableEnd) {
                    path = path.getParentPath();
                }
            }
        }
        
        Element el = info.getTrees().getElement(path);
        
        if (el == null) {
            wasResolved[0] = false;
            return null;
        }
        
        //#89736: if the caret is not in the resolved element's name, no rename:
        final Token<JavaTokenId> name = org.netbeans.modules.java.editor.semantic.Utilities.getToken(info, doc, path);
        
        if (name == null)
            return null;
        
        doc.render(new Runnable() {
            public void run() {
                wasResolved[0] = name.offset(null) <= caret && caret <= (name.offset(null) + name.length());
            }
        });
        
        if (!wasResolved[0])
            return null;
        
        if (el.getKind() == ElementKind.CONSTRUCTOR) {
            //for constructor, work over the enclosing class:
            el = el.getEnclosingElement();
        }
        
        if (allowInstantRename(el)) {
            final Set<Token<JavaTokenId>> points = new HashSet<Token<JavaTokenId>>(new FindLocalUsagesQuery().findUsages(el, info, doc));
            
            if (el.getKind().isClass()) {
                //rename also the constructors:
                for (ExecutableElement c : ElementFilter.constructorsIn(el.getEnclosedElements())) {
                    TreePath t = info.getTrees().getPath(c);
                    
                    if (t != null) {
                        Token<JavaTokenId> token = org.netbeans.modules.java.editor.semantic.Utilities.getToken(info, doc, t);
                        
                        if (token != null) {
                            points.add(token);
                        }
                    }
                }
            }
            
            final boolean[] overlapsWithGuardedBlocks = new boolean[1];
            
            doc.render(new Runnable() {
                public void run() {
                    overlapsWithGuardedBlocks[0] = overlapsWithGuardedBlocks(doc, points);
                }
            });
            
            if (overlapsWithGuardedBlocks[0]) {
                return null;
            }
            
            return points;
        }
        
        return null;
    }
    
    private static boolean allowInstantRename(Element e) {
        if (org.netbeans.modules.java.editor.semantic.Utilities.isPrivateElement(e)) {
            return true;
        }
        
        //#92160: check for local classes:
        if (e.getKind() == ElementKind.CLASS) {//only classes can be local
            Element enclosing = e.getEnclosingElement();
            
            return LOCAL_CLASS_PARENTS.contains(enclosing.getKind());
        }
        
        return false;
    }
    
    private static boolean overlapsWithGuardedBlocks(Document doc, Set<Token<JavaTokenId>> highlights) {
        if (!(doc instanceof GuardedDocument))
            return false;
        
        GuardedDocument gd = (GuardedDocument) doc;
        MarkBlock current = gd.getGuardedBlockChain().getChain();
        
        while (current != null) {
            for (Token<JavaTokenId> h : highlights) {
                if ((current.compare(h.offset(null), h.offset(null) + h.length()) & MarkBlock.OVERLAP) != 0) {
                    return true;
                }
            }
            
            current = current.getNext();
        }
        
        return false;
    }
    
    private static final Set<ElementKind> LOCAL_CLASS_PARENTS = EnumSet.of(ElementKind.CONSTRUCTOR, ElementKind.INSTANCE_INIT, ElementKind.METHOD, ElementKind.STATIC_INIT);
    
    
    public static void performInstantRename(JTextComponent target, Set<Token<JavaTokenId>> highlights, int caretOffset) throws BadLocationException {
	new InstantRenamePerformer(target, highlights, caretOffset);
    }

    private boolean isIn(MutablePositionRegion region, int caretOffset) {
	return region.getStartOffset() <= caretOffset && caretOffset <= region.getEndOffset();
    }
    
    private boolean inSync;
    
    public synchronized void insertUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
	
        //check for modifications outside the first region:
        if (e.getOffset() < region.getFirstRegionStartOffset() || (e.getOffset() + e.getLength()) > region.getFirstRegionEndOffset()) {
            release();
            return;
        }
        
	inSync = true;
	region.sync(0);
        span = region.getFirstRegionLength();
	inSync = false;
	target.repaint();
    }

    public synchronized void removeUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
	
        if (e.getLength() == 1) {
            if ((e.getOffset() < region.getFirstRegionStartOffset() || e.getOffset() > region.getFirstRegionEndOffset())) {
                release();
                return;
            }

            if (e.getOffset() == region.getFirstRegionStartOffset() && region.getFirstRegionLength() > 0) {
                JavaDeleteCharAction jdca = (JavaDeleteCharAction) target.getClientProperty(JavaDeleteCharAction.class);
                
                if (jdca != null && !jdca.getNextChar()) {
                    undo();
                } else {
                    release();
                }
                
                return;
            }
            
            if (e.getOffset() == region.getFirstRegionEndOffset() && region.getFirstRegionLength() > 0) {
            //XXX: moves the caret anyway:
//                JavaDeleteCharAction jdca = (JavaDeleteCharAction) target.getClientProperty(JavaDeleteCharAction.class);
//
//                if (jdca != null && jdca.getNextChar()) {
//                    undo();
//                } else {
                    release();
//                }

                return;
            }
        } else {
            //selection/multiple characters removed:
            int removeSpan = e.getLength() + region.getFirstRegionLength();
            
            if (span < removeSpan) {
                release();
                return;
            }
        }
        
        //#89997: do not sync the regions for the "remove" part of replace selection,
        //as the consequent insert may use incorrect offset, and the regions will be synced
        //after the insert anyway.
        if (doc.getProperty(BaseKit.DOC_REPLACE_SELECTION_PROPERTY) != null) {
            return ;
        }
        
	inSync = true;
	region.sync(0);
        span = region.getFirstRegionLength();
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

    private void undo() {
        if (doc instanceof BaseDocument && ((BaseDocument) doc).isAtomicLock()) {
            ((BaseDocument) doc).atomicUndo();
        } else {
            UndoableEdit undoMgr = (UndoableEdit) doc.getProperty(BaseDocument.UNDO_MANAGER_PROP);
            if (target != null && undoMgr != null) {
                try {
                    undoMgr.undo();
                } catch (CannotUndoException e) {
                    Logger.getLogger(InstantRenamePerformer.class.getName()).log(Level.WARNING, null, e);
                }
            }
        }
    }
            
    private static final AttributeSet defaultSyncedTextBlocksHighlight = AttributesUtilities.createImmutable(StyleConstants.Background, new Color(138, 191, 236));
    
    private static AttributeSet getSyncedTextBlocksHighlight() {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
        AttributeSet as = fcs != null ? fcs.getFontColors("synchronized-text-blocks") : null; //NOI18N
        return as == null ? defaultSyncedTextBlocksHighlight : as;
    }
    
    public static PositionsBag getHighlightsBag(Document doc) {
        PositionsBag bag = (PositionsBag) doc.getProperty(InstantRenamePerformer.class);
        
        if (bag == null) {
            doc.putProperty(InstantRenamePerformer.class, bag = new PositionsBag(doc));
            
            Object stream = doc.getProperty(Document.StreamDescriptionProperty);
            
            if (stream instanceof DataObject) {
                Logger.getLogger("TIMER").log(Level.FINE, "Instant Rename Highlights Bag", new Object[] {((DataObject) stream).getPrimaryFile(), bag}); //NOI18N
            }
        }
        
        return bag;
    }
    
}
