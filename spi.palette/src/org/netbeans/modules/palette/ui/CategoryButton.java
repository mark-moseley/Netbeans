/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.palette.ui;

import java.awt.dnd.Autoscroll;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Utils;
import org.openide.nodes.Node;
import org.openide.util.Utilities;


/**
 * @author David Kaspar, Jan Stola
 */
class CategoryButton extends JCheckBox implements Autoscroll {

    private static final Icon openedIcon = (Icon)UIManager.get("Tree.expandedIcon"); // NOI18N
    private static final Icon closedIcon = (Icon)UIManager.get("Tree.collapsedIcon"); // NOI18N
    private static final Color GTK_BK_COLOR = new Color( 184,207,229 );
    private static final Color AQUA_BK_COLOR = new Color(225, 235, 240);
    
    private static final boolean isGTK = "GTK".equals( UIManager.getLookAndFeel().getID() );
    private static final boolean isAqua = "Aqua".equals( UIManager.getLookAndFeel().getID() );

    private CategoryDescriptor descriptor;
    private Category category;
    
    private AutoscrollSupport support;

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
        setIcon( closedIcon );
        setSelectedIcon( openedIcon );
        setText( category.getDisplayName() );
        setToolTipText( category.getShortDescription() );
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
        descriptor.setOpened( expand );
        descriptor.getPalettePanel().computeHeights( expand ? CategoryButton.this.category : null );
        requestFocus ();
    }

    /** Safe getter for autoscroll support. */
    AutoscrollSupport getSupport() {
        if( null == support ) {
            support = new AutoscrollSupport( getParent().getParent() );
        }

        return support;
    }

    public Color getBackground() {
        if( isFocusOwner() ) {
            if( isAqua )
                return UIManager.getColor("Table.selectionBackground"); //NOI18N
            return UIManager.getColor( "PropSheet.selectedSetBackground" ); //NOI18N
        } else {
            if( isGTK ) {
                return GTK_BK_COLOR;
            } else if( isAqua ) {
                return AQUA_BK_COLOR;
            } else {
                return UIManager.getColor( "PropSheet.setBackground" ); //NOI18N
            }
        }
    }

    public Color getForeground() {
        if( isFocusOwner() ) {
            if( isAqua )
                return UIManager.getColor( "Table.selectionForeground" ); //NOI18N
            return UIManager.getColor( "PropSheet.selectedSetForeground" ); //NOI18N
        } else {
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
