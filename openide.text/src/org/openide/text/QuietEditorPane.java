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

package org.openide.text;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.InputEvent;
import java.util.TooManyListenersException;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.plaf.UIResource;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.openide.util.Lookup;
import org.openide.windows.ExternalDropHandler;
import org.openide.windows.TopComponent;

/** performance trick - 18% of time saved during open of an editor
*
* @author Ales Novak
*/
final class QuietEditorPane extends JEditorPane {
    
    static DataFlavor constructActiveEditorDropFlavor() {
        try {
            return new DataFlavor("text/active_editor_flavor;class=org.openide.text.ActiveEditorDrop", // NOI18N
                    "Active Editor Flavor", // XXX missing I18N!
                    QuietEditorPane.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }
    
    final static int FIRE = 0x1;
    final static int PAINT = 0x2;
    final static int ALL = FIRE | PAINT;

    // #21120. Caret was null while serializing CloneableEditor.

    /** Saves last position of caret when, doing it's UI reinstallation. */
    private int lastPosition = -1;

    /** is firing of events enabled? */
    int working = FIRE; // [Mila] firing since begining, otherwise doesn't work well
    
    /** determines scroll unit */
    private int fontHeight;
    private int charWidth;

    /**
     * consturctor sets the initial values for horizontal
     * and vertical scroll units.
     * also listenes for font changes.
     */
    public QuietEditorPane() {
        setFontHeightWidth(getFont());
    }
    
    public void setFont(Font font) {
        super.setFont(font);
        setFontHeightWidth(getFont());
    }


    private void setFontHeightWidth(Font font) {
        FontMetrics metrics=getFontMetrics(font);
        fontHeight=metrics.getHeight();
        charWidth=metrics.charWidth('m');
    }

    /**
     * fix for #38139. 
     * returns height of a line for vertical scroll unit
     * or width of a widest char for a horizontal scroll unit
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        switch (orientation) {
            case SwingConstants.VERTICAL:
                return fontHeight;
            case SwingConstants.HORIZONTAL:
                return charWidth;
            default:
                throw new IllegalArgumentException("Invalid orientation: " +orientation);
        }
    }
    
    public void setDocument(Document doc) {
        super.setDocument(doc);
        
        // Setting DelegatingTransferHandler, where CallbackTransferable will
        // be handled in importData method. 
        // For more details, please refer issue #53439        
        if (doc != null){
            TransferHandler thn = getTransferHandler();
            if( !(thn instanceof DelegatingTransferHandler) ) {
                DelegatingTransferHandler dth = new DelegatingTransferHandler(thn);
                setTransferHandler(dth);
            }

            DropTarget currDt = getDropTarget();
            if( !(currDt instanceof DelegatingDropTarget ) ) {
                DropTarget dt = new DelegatingDropTarget( currDt );
                setDropTarget( dt );
            }
        }
    }
    
    public void setWorking(int x) {
        working = x;
    }

    public void firePropertyChange(String s, Object val1, Object val2) {
        if ((working & FIRE) != 0) {
            super.firePropertyChange(s, val1, val2);
        }
    }

    /** Overrides superclass method, to keep old caret position.
     * While is reinstallation of UI in progress, there
     * is a gap between the uninstallUI
     * and intstallUI when caret set to <code>null</code>. */
    public void setCaret(Caret caret) {
        if (caret == null) {
            Caret oldCaret = getCaret();

            if (oldCaret != null) {
                lastPosition = oldCaret.getDot();
            }
        }

        super.setCaret(caret);
    }

    /** Gets the last caret position, for the case the serialization
     * is done during the time of pane UI reinstallation. */
    int getLastPosition() {
        return lastPosition;
    }

    /*
    public void setDocument(Document doc) {
      if (working) {
        super.setDocument(doc);
      }
    }

    public void setUI(javax.swing.plaf.TextUI ui) {
      if (working) {
        super.setUI(ui);
      }
    }*/
    public void revalidate() {
        if ((working & PAINT) != 0) {
            super.revalidate();
        }
    }

    public void repaint() {
        if ((working & PAINT) != 0) {
            super.repaint();
        }
    }

    /**
     * Delegating TransferHandler.
     * The main purpose is hooking on importData method where CallbackTransferable
     * is handled. For more details, please refer issue #53439
     */    
    private class DelegatingTransferHandler extends TransferHandler{
        
        TransferHandler delegator;
        
        public DelegatingTransferHandler(TransferHandler delegator){
            this.delegator = delegator;
        }
        
        public void exportAsDrag(JComponent comp, InputEvent e, int action) {
            delegator.exportAsDrag(comp, e, action);
        }

        public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
            delegator.exportToClipboard(comp, clip, action);
        }

        public boolean importData(JComponent comp, Transferable t) {
            try {
                if (t.isDataFlavorSupported(ActiveEditorDrop.FLAVOR)){
                    Object obj = t.getTransferData(ActiveEditorDrop.FLAVOR);
                    if (obj instanceof ActiveEditorDrop && comp instanceof JTextComponent){
                        boolean success = false;
                        try {
                            success = ((ActiveEditorDrop)obj).handleTransfer((JTextComponent)comp);
                        }
                        finally {
                            requestFocus(comp);
                        }
                        return success;
                    }
                }
            } catch (Exception exc){
                exc.printStackTrace();
            }
            return delegator.importData(comp, t);
        }

        private void requestFocus(JComponent comp) {
            Container container = SwingUtilities.getAncestorOfClass(TopComponent.class, comp);
            if (container != null) {
                ((TopComponent)container).requestActive();
            }
            else {
                Component f = comp;
                do {
                    f = f.getParent();
                    if (f instanceof Frame) {
                        break;
                    }
                } while (f != null);
                if (f != null) {
                    f.requestFocus();
                }
                comp.requestFocus();
            }
        }
        
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            for (int i=0; i<transferFlavors.length; i++){
                if (transferFlavors[i] == ActiveEditorDrop.FLAVOR){
                    return true;
                }
            }
            return delegator.canImport(comp, transferFlavors);
        }

        public int getSourceActions(JComponent c) {
            return delegator.getSourceActions(c);
        }

        public Icon getVisualRepresentation(Transferable t) {
            return delegator.getVisualRepresentation(t);
        }

        protected void exportDone(JComponent source, Transferable data, int action) {
            try {
                java.lang.reflect.Method method = delegator.getClass().getDeclaredMethod(
                    "exportDone",  // NOI18N
                    new Class[] {javax.swing.JComponent.class, Transferable.class, int.class});
                method.setAccessible(true);
                method.invoke(delegator, new Object[] {source, data, new Integer(action)});
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (java.lang.reflect.InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
        
        protected Transferable createTransferable(JComponent comp) {
            try {
                java.lang.reflect.Method method = delegator.getClass().getDeclaredMethod(
                    "createTransferable", // NOI18N
                    new Class[] {javax.swing.JComponent.class});
                method.setAccessible(true);
                return (Transferable)method.invoke(delegator, new Object[] {comp});
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (java.lang.reflect.InvocationTargetException ex) {
                ex.printStackTrace();
            }
            return null;
	}
    }
    
    private class DelegatingDropTarget extends DropTarget implements UIResource {
        private DropTarget orig;
        private boolean isDragging = false;

        public DelegatingDropTarget( DropTarget orig ) {
            this.orig = orig;
        }
        public void addDropTargetListener(DropTargetListener dtl) throws TooManyListenersException {
            orig.addDropTargetListener( dtl );
        }

        public void removeDropTargetListener(DropTargetListener dtl) {
            orig.removeDropTargetListener( dtl );
        }

        public void dragEnter(DropTargetDragEvent dtde) {
            ExternalDropHandler handler = (ExternalDropHandler)Lookup.getDefault().lookup( ExternalDropHandler.class );
            if( null != handler && handler.canDrop( dtde ) ) {
                dtde.acceptDrag( DnDConstants.ACTION_COPY );
                isDragging = false;
            } else {
                orig.dragEnter( dtde );
                isDragging = true;
            }
        }

        public void dragExit(DropTargetEvent dte) {
            if( isDragging ) {
                orig.dragExit( dte );
            }
            isDragging = false;
        }

        public void dragOver(DropTargetDragEvent dtde) {
            ExternalDropHandler handler = (ExternalDropHandler)Lookup.getDefault().lookup( ExternalDropHandler.class );
            if( null != handler && handler.canDrop( dtde ) ) {
                dtde.acceptDrag( DnDConstants.ACTION_COPY );
                isDragging = false;
            } else {
                orig.dragOver( dtde );
                isDragging = true;
            }
        }

        public void drop(DropTargetDropEvent e) {
            ExternalDropHandler handler = (ExternalDropHandler)Lookup.getDefault().lookup( ExternalDropHandler.class );
            if( null != handler && handler.canDrop( e ) ) {
                e.acceptDrop( DnDConstants.ACTION_COPY );

                e.dropComplete( handler.handleDrop( e ) );
            } else {
                orig.drop( e );
            }
            isDragging = false;
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
            if( isDragging )
                orig.dropActionChanged( dtde );
        }
    }
}
