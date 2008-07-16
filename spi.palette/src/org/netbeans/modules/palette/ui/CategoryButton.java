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

package org.netbeans.modules.palette.ui;

import java.awt.dnd.Autoscroll;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Utils;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.Utilities;


/**
 * @author David Kaspar, Jan Stola
 */
class CategoryButton extends JCheckBox implements Autoscroll {

    private static final Color AQUA_BK_COLOR = new Color(225, 235, 240);
    
    static final boolean isGTK = "GTK".equals( UIManager.getLookAndFeel().getID() );
    static final boolean isAqua = "Aqua".equals( UIManager.getLookAndFeel().getID() );

    private CategoryDescriptor descriptor;
    private Category category;
    
    private AutoscrollSupport support;
    
    // Workaround for JDK bug in GTK #6527149 - use Metal UI class
    static {
        if (isGTK) {
            UIManager.put("MetalCheckBoxUI_4_GTK", "javax.swing.plaf.metal.MetalCheckBoxUI");
        }
    }

    @Override
    public String getUIClassID() {
        String classID = super.getUIClassID();
        if (isGTK) {
            classID = "MetalCheckBoxUI_4_GTK";
        }
        return classID;
    }

    

    CategoryButton( CategoryDescriptor descriptor, Category category ) {
        this.descriptor = descriptor;
        this.category = category;

        //force initialization of PropSheet look'n'feel values 
        UIManager.get( "nb.propertysheet" );
            
        setFont( getFont().deriveFont( Font.BOLD ) );
        setMargin(new Insets(0, 3, 0, 3));
        setFocusPainted( false );

        setSelected( false );

        setHorizontalAlignment( SwingConstants.LEFT );
        setHorizontalTextPosition( SwingConstants.RIGHT );
        setVerticalTextPosition( SwingConstants.CENTER );

        updateProperties();
        
        if( getBorder() instanceof CompoundBorder ) { // from BasicLookAndFeel
            Dimension pref = getPreferredSize();
            pref.height -= 3;
            setPreferredSize( pref );
        }

        addActionListener( new ActionListener () {
            public void actionPerformed( ActionEvent e ) {
                boolean opened = !CategoryButton.this.descriptor.isOpened();
                setExpanded( opened );
            }
        });
        
        addFocusListener( new FocusListener() {
            public void focusGained(FocusEvent e) {
                scrollRectToVisible( getBounds() );
            }
            public void focusLost(FocusEvent e) {
            }
        });
        
        initActions();
    }
    
    private void initActions() {
        InputMap inputMap = getInputMap( WHEN_FOCUSED );
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, 0, false ), "moveFocusDown" ); //NOI18N
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_UP, 0, false ), "moveFocusUp" ); //NOI18N
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, 0, false ), "collapse" ); //NOI18N
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, 0, false ), "expand" ); //NOI18N
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK, false ), "popup" ); //NOI18N
        inputMap.put( KeyStroke.getKeyStroke( "ctrl V" ), "paste" ); //NOI18N //NOI18N
        inputMap.put( KeyStroke.getKeyStroke( "PASTE" ), "paste" ); //NOI18N //NOI18N
        
        ActionMap actionMap = getActionMap();
        actionMap.put( "moveFocusDown", new MoveFocusAction( true ) ); //NOI18N
        actionMap.put( "moveFocusUp", new MoveFocusAction( false ) ); //NOI18N
        actionMap.put( "collapse", new ExpandAction( false ) ); //NOI18N
        actionMap.put( "expand", new ExpandAction( true ) ); //NOI18N
        actionMap.put( "popup", new PopupAction() ); //NOI18N
        Node categoryNode = (Node)category.getLookup().lookup( Node.class );
        if( null != categoryNode )
            actionMap.put( "paste", new Utils.PasteItemAction( categoryNode ) ); //NOI18N
    }
    
    void updateProperties() {
        setIcon( (Icon)UIManager.get("Tree.collapsedIcon") );
        setSelectedIcon( (Icon)UIManager.get("Tree.expandedIcon") );
        Mnemonics.setLocalizedText( this, category.getDisplayName() );
        setToolTipText( category.getShortDescription() );
        getAccessibleContext().setAccessibleName( category.getDisplayName() );
        getAccessibleContext().setAccessibleDescription( category.getShortDescription() );
        if( isAqua ) {
            setContentAreaFilled(true);
            setOpaque(true);
            setBackground( new Color(0,0,0) );
            setForeground( new Color(255,255,255) );
        }
    }
    
    Category getCategory() {
        return category;
    }

    
    /** notify the Component to autoscroll */
    public void autoscroll( Point cursorLoc ) {
        Point p = SwingUtilities.convertPoint( this, cursorLoc, getParent().getParent() );
        getSupport().autoscroll( p );
    }

    /** @return the Insets describing the autoscrolling
     * region or border relative to the geometry of the
     * implementing Component.
     */
    public Insets getAutoscrollInsets() {
        return getSupport().getAutoscrollInsets();
    }
    
    boolean isExpanded() {
        return isSelected();
    }
    
    void setExpanded( boolean expand ) {
        setSelected( expand );
        if( descriptor.isOpened() == expand )
            return;
        descriptor.setOpened( expand );
        descriptor.getPalettePanel().computeHeights( expand ? CategoryButton.this.category : null );
        requestFocus ();
    }

    /** Safe getter for autoscroll support. */
    AutoscrollSupport getSupport() {
        if( null == support ) {
            support = new AutoscrollSupport( PalettePanel.getDefault() );
        }

        return support;
    }

    @Override
    public Color getBackground() {
        if( isFocusOwner() ) {
            if( isAqua )
                return UIManager.getColor("Table.selectionBackground"); //NOI18N
            return UIManager.getColor( "PropSheet.selectedSetBackground" ); //NOI18N
        } else {
            if( isAqua ) {
                return AQUA_BK_COLOR;
            } else {
                return UIManager.getColor( "PropSheet.setBackground" ); //NOI18N
            }
        }
    }

    @Override
    public Color getForeground() {
        if( isFocusOwner() ) {
            if( isAqua )
                return UIManager.getColor( "Table.selectionForeground" ); //NOI18N
            return UIManager.getColor( "PropSheet.selectedSetForeground" ); //NOI18N
        } else {
            if( isAqua ) {
                Color res = UIManager.getColor("PropSheet.setForeground"); //NOI18N

                if (res == null) {
                    res = UIManager.getColor("Table.foreground"); //NOI18N

                    if (res == null) {
                        res = UIManager.getColor("textText");

                        if (res == null) {
                            res = Color.BLACK;
                        }
                    }
                }
                return res;
            }
            return super.getForeground();
        }
    }
    
    private class MoveFocusAction extends AbstractAction {
        private boolean moveDown;
        
        public MoveFocusAction( boolean moveDown ) {
            this.moveDown = moveDown;
        }
        
        public void actionPerformed(ActionEvent e) {
            KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            Container container = kfm.getCurrentFocusCycleRoot();
            FocusTraversalPolicy policy = container.getFocusTraversalPolicy();
            if( null == policy )
                policy = kfm.getDefaultFocusTraversalPolicy();
            Component next = moveDown ? policy.getComponentAfter( container, CategoryButton.this )
                                      : policy.getComponentBefore( container, CategoryButton.this );
            if( null != next && next instanceof CategoryList ) {
                if( ((CategoryList)next).getModel().getSize() != 0 ) {
                    ((CategoryList)next).takeFocusFrom( CategoryButton.this );
                    return;
                } else {
                    next = moveDown ? policy.getComponentAfter( container, next )
                                    : policy.getComponentBefore( container, next );
                }
            }
            if( null != next && next instanceof CategoryButton ) {
                next.requestFocus();
            }
        }
    }
    
    private class ExpandAction extends AbstractAction {
        private boolean expand;
        
        public ExpandAction( boolean expand ) {
            this.expand = expand;
        }
        
        public void actionPerformed(ActionEvent e) {
            if( expand == isExpanded() )
                return;
            setExpanded( expand );
        }
    }

    private class PopupAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            Action[] actions = category.getActions();
            JPopupMenu popup = Utilities.actionsToPopup( actions, CategoryButton.this );
            Utils.addCustomizationMenuItems( popup, descriptor.getPalettePanel().getController(), descriptor.getPalettePanel().getSettings() );
            popup.show( getParent(), 0, getHeight() );
        }
    }
}
