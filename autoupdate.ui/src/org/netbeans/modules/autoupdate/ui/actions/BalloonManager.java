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

package org.netbeans.modules.autoupdate.ui.actions;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.openide.util.Utilities;

/**
 * Shows, hides balloon-like tooltip windows.
 * 
 * @author S. Aubrecht
 */
public class BalloonManager {

    private static Balloon currentBalloon;
    private static JLayeredPane currentPane;
    private static ComponentListener listener;
    
    /**
     * Show balloon-like tooltip pointing to the given component. The balloon stays
     * visible until dismissed by clicking its 'close' button or by invoking its default action.
     * @param owner The component which the balloon will point to
     * @param content Content to be displayed in the balloon.
     * @param defaultAction Action to invoked when the balloon is clicked, can be null.
     */
    public static synchronized void show( final JComponent owner, JComponent content, Action defaultAction ) {
        assert null != owner;
        assert null != content;
        
        //hide current balloon (if any)
        dismiss();
        
        currentBalloon = new Balloon( content, defaultAction );
        currentPane = JLayeredPane.getLayeredPaneAbove( owner );
        
        listener = new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                dismiss();
            }

            public void componentMoved(ComponentEvent e) {
                dismiss();
            }

            public void componentShown(ComponentEvent e) {
            }

            public void componentHidden(ComponentEvent e) {
                dismiss();
            }
        };
        currentPane.addComponentListener( listener );
        configureBalloon( currentBalloon, currentPane, owner );
        currentPane.add( currentBalloon, new Integer(JLayeredPane.POPUP_LAYER-1) );
    }
    
    /**
     * Dismiss currently showing balloon tooltip (if any)
     */
    public static synchronized void dismiss() {
        if( null != currentBalloon ) {
            currentBalloon.setVisible( false );
            currentPane.remove( currentBalloon );
            currentPane.repaint();
            currentPane.removeComponentListener( listener );
            currentBalloon = null;
            currentPane = null;
            listener = null;
        }
    }
    
    private static void configureBalloon( Balloon balloon, JLayeredPane pane, JComponent ownerComp ) {
        Rectangle ownerCompBounds = ownerComp.getBounds();
        ownerCompBounds = SwingUtilities.convertRectangle( ownerComp.getParent(), ownerCompBounds, pane );
        
        int paneWidth = pane.getWidth();
        int paneHeight = pane.getHeight();
        
        Dimension balloonSize = balloon.getPreferredSize();
        balloonSize.height += Balloon.ARC;
        
        //first try lower right corner
        if( ownerCompBounds.x + ownerCompBounds.width + balloonSize.width < paneWidth
            && 
            ownerCompBounds.y + ownerCompBounds.height + balloonSize.height + Balloon.ARC < paneHeight ) {
            
            balloon.setArrowLocation( GridBagConstraints.SOUTHEAST );
            balloon.setBounds( ownerCompBounds.x+ownerCompBounds.width-Balloon.ARC/2, 
                    ownerCompBounds.y+ownerCompBounds.height, balloonSize.width+Balloon.ARC, balloonSize.height );
        
        //upper right corner
        } else  if( ownerCompBounds.x + ownerCompBounds.width + balloonSize.width < paneWidth
                    && 
                    ownerCompBounds.y - balloonSize.height - Balloon.ARC > 0 ) {
            
            balloon.setArrowLocation( GridBagConstraints.NORTHEAST );
            balloon.setBounds( ownerCompBounds.x+ownerCompBounds.width-Balloon.ARC/2, 
                    ownerCompBounds.y-balloonSize.height, balloonSize.width+Balloon.ARC, balloonSize.height );
        
        //lower left corner
        } else  if( ownerCompBounds.x - balloonSize.width > 0
                    && 
                    ownerCompBounds.y + ownerCompBounds.height + balloonSize.height + Balloon.ARC < paneHeight ) {
            
            balloon.setArrowLocation( GridBagConstraints.SOUTHWEST );
            balloon.setBounds( ownerCompBounds.x-balloonSize.width+Balloon.ARC/2, 
                    ownerCompBounds.y+ownerCompBounds.height, balloonSize.width+Balloon.ARC, balloonSize.height );
        //upper left corent
        } else {
            balloon.setArrowLocation( GridBagConstraints.NORTHWEST );
            balloon.setBounds( ownerCompBounds.x-balloonSize.width+Balloon.ARC/2, 
                    ownerCompBounds.y-balloonSize.height, balloonSize.width+Balloon.ARC, balloonSize.height );
        }
    }

    private static class Balloon extends JPanel {

        private static final int Y_OFFSET = 16;
        private static final int ARC = 15;
        private static final int SHADOW_SIZE = 10;


        private JComponent content;
        private Action defaultAction;
        private JButton btnDismiss;
        private int arrowLocation = GridBagConstraints.SOUTHEAST;

        public Balloon( final JComponent content, final Action defaultAction ) {
            super( new GridBagLayout() );
            this.content = content;
            this.defaultAction = defaultAction;
            content.setOpaque( false );

            btnDismiss = new DismissButton();
            btnDismiss.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    BalloonManager.dismiss();
                }
            });

            add( content, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH,new Insets(4,4,4,4),0,0)); 
            add( new JLabel(), new GridBagConstraints(0,1,1,1,0.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0)); 
            add( btnDismiss, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.NORTHEAST,GridBagConstraints.NONE,new Insets(4,0,4,4),0,0)); 

            setOpaque( false );

            if( null != defaultAction ) {
                content.addMouseListener( new MouseListener() {

                    public void mouseClicked(MouseEvent e) {
                        BalloonManager.dismiss();
                        defaultAction.actionPerformed( new ActionEvent( Balloon.this, 0, "", e.getWhen(), e.getModifiers() ) );
                    }

                    public void mousePressed(MouseEvent e) {
                    }

                    public void mouseReleased(MouseEvent e) {
                    }

                    public void mouseEntered(MouseEvent e) {
                        content.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
                    }

                    public void mouseExited(MouseEvent e) {
                        content.setCursor( Cursor.getDefaultCursor() );
                    }

                });
            }
            setBorder( BorderFactory.createEmptyBorder(SHADOW_SIZE, 0, 0, SHADOW_SIZE));
        }
        
        void setArrowLocation( int arrowLocation) {
            this.arrowLocation = arrowLocation;
            if( arrowLocation == GridBagConstraints.NORTHEAST || arrowLocation == GridBagConstraints.NORTHWEST ) {
                setBorder( BorderFactory.createEmptyBorder(SHADOW_SIZE, 0, 0, SHADOW_SIZE));
            } else {
                setBorder( BorderFactory.createEmptyBorder(Y_OFFSET, 0, 0, SHADOW_SIZE));
            }
        }
        
        private Shape getMask( int w, int h ) {
            w--;
            w -= SHADOW_SIZE;
            GeneralPath path = new GeneralPath();
            Area area = null;
            switch( arrowLocation ) {
            case GridBagConstraints.SOUTHEAST: 
                area = new Area(new RoundRectangle2D.Float(0, Y_OFFSET, w, h-Y_OFFSET-SHADOW_SIZE, ARC, ARC));
                path.moveTo(ARC/2, 0);
                path.lineTo(ARC/2+Y_OFFSET/2, Y_OFFSET);
                path.lineTo(ARC/2+Y_OFFSET/2+Y_OFFSET, Y_OFFSET);
                break;
            case GridBagConstraints.NORTHEAST: 
                area = new Area(new RoundRectangle2D.Float(0, SHADOW_SIZE, w, h-Y_OFFSET-SHADOW_SIZE, ARC, ARC));
                path.moveTo(ARC/2, h-1);
                path.lineTo(ARC/2+Y_OFFSET/2, h-1-Y_OFFSET);
                path.lineTo(ARC/2+Y_OFFSET/2+Y_OFFSET, h-1-Y_OFFSET);
                break;
            case GridBagConstraints.SOUTHWEST: 
                area = new Area(new RoundRectangle2D.Float(0, Y_OFFSET, w, h-Y_OFFSET-SHADOW_SIZE, ARC, ARC));
                path.moveTo(w-ARC/2, 0);
                path.lineTo(w-ARC/2-Y_OFFSET/2, Y_OFFSET);
                path.lineTo(w-ARC/2-Y_OFFSET/2-Y_OFFSET, Y_OFFSET);
                break;
            case GridBagConstraints.NORTHWEST: 
                area = new Area(new RoundRectangle2D.Float(0, SHADOW_SIZE, w, h-Y_OFFSET-SHADOW_SIZE, ARC, ARC));
                path.moveTo(w-ARC/2, h-1);
                path.lineTo(w-ARC/2-Y_OFFSET/2, h-1-Y_OFFSET);
                path.lineTo(w-ARC/2-Y_OFFSET/2-Y_OFFSET, h-1-Y_OFFSET);
                break;
            }
                
            path.closePath();
            area.add(new Area(path));
            return area;
        }
        
        private Shape getShadowMask( Shape parentMask ) {
            Area area = new Area(parentMask);

            AffineTransform tx = new AffineTransform();
            if( arrowLocation == GridBagConstraints.NORTHEAST || arrowLocation == GridBagConstraints.NORTHWEST ) {
                tx.translate(SHADOW_SIZE, -SHADOW_SIZE );//Math.sin(ANGLE)*(getHeight()+SHADOW_SIZE), 0);
            } else {
                tx.translate(SHADOW_SIZE, SHADOW_SIZE );//Math.sin(ANGLE)*(getHeight()+SHADOW_SIZE), 0);
            }
            area.transform(tx);
            return area;
        }


        @Override
        protected void paintBorder(Graphics g) {
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;
            Composite oldC = g2d.getComposite();
            Shape s = getMask( getWidth(), getHeight() );

            g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.25f ) );
            g2d.setColor( Color.black );
            g2d.fill( getShadowMask(s) );
            
            g2d.setColor( UIManager.getColor( "ToolTip.background" ) ); //NOI18N
//            g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.5f ) );
            g2d.setComposite( oldC );
            g2d.fill(s);
            g2d.setColor( Color.black );
            g2d.draw(s);
        }
    }
    
    private static class DismissButton extends JButton {

        public DismissButton() {
            Image img = Utilities.loadImage( "org/netbeans/modules/autoupdate/ui/resources/dismiss_enabled.png" );
            setIcon( new ImageIcon( img ) );
            img = Utilities.loadImage( "org/netbeans/modules/autoupdate/ui/resources/dismiss_rollover.png" );
            setRolloverIcon(new ImageIcon( img ));
            img = Utilities.loadImage( "org/netbeans/modules/autoupdate/ui/resources/dismiss_pressed.png" );
            setPressedIcon(new ImageIcon( img ));

            setBorder( BorderFactory.createEmptyBorder() );
            setBorderPainted( false );
            setFocusable( false );
            setOpaque( false );
            setRolloverEnabled( true );
        }
        
        @Override
        public void paint(Graphics g) {
            Icon icon = null;
            if( getModel().isArmed() && getModel().isPressed() ) {
                icon = getPressedIcon();
            } else if( getModel().isRollover() ) {
                icon = getRolloverIcon();
            } else {
                icon = getIcon();
            }
            icon.paintIcon( this, g, 0, 0 );
        }
        
    }
}
