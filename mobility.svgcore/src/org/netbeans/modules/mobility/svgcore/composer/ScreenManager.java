/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.mobility.svgcore.composer;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.Stack;
import javax.microedition.m2g.SVGImage;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import org.netbeans.modules.mobility.svgcore.view.svg.SVGImagePanel;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;

/**
 *
 * @author Pavel Benes
 */
public class ScreenManager {
    private static final Image LOCK_ICON           = org.openide.util.Utilities.loadImage ("org/netbeans/modules/mobility/svgcore/resources/lock.png"); // NOI18N        
    private static final Color VIEWBOXBORDER_COLOR = Color.DARK_GRAY;

    private static final float MINIMUM_ZOOM = 0.01f;
    private static final float MAXIMUM_ZOOM = 100f;
    
    private final SceneManager        m_sceneMgr;
    private       JScrollPane         m_topComponent;
    private       JComponent          m_animatorView;
    private       SVGImagePanel       m_imageContainer;
    private       SVGLocatableElement m_popupElement = null;
    private       Cursor              m_cursor = null;    
    private       SVGRect             m_bBox;
    private       boolean             m_showAllArea;
    private       boolean             m_showTooltip     = true;
    private       boolean             m_highlightObject = true;
    private       float               m_zoomRatio = 1;
    private       short               m_changeTicker = 0;
    
    ScreenManager(SceneManager sceneMgr) {
        m_sceneMgr = sceneMgr;
    }

    void initialize() {
        PerseusController perseus = m_sceneMgr.getPerseusController();
        m_animatorView = perseus.getAnimatorGUI();
        m_bBox         = perseus.getSVGRootElement().getScreenBBox();
                       
        m_imageContainer = new SVGImagePanel(m_animatorView) {
            protected void paintPanel(Graphics g, int x, int y, int w, int h) {
                PerseusController perseus = m_sceneMgr.getPerseusController();
                if (m_showAllArea) {
                    SVGLocatableElement elem = perseus.getViewBoxMarker();
                    if (elem != null) {
                        SVGRect rect = elem.getScreenBBox();
                        g.setColor( VIEWBOXBORDER_COLOR);
                        g.drawRect((int)(x + rect.getX()), (int)(y + rect.getY()),
                                   (int)(rect.getWidth()), (int)(rect.getHeight()));
                    }
                }
                
                if (perseus.isAnimationStopped()) {
                    Stack<ComposerAction> actions = m_sceneMgr.getActiveActions();
                    if (actions != null) {
                        for (int i = actions.size()-1; i >= 0; i--) {
                            actions.get(i).paint(g, x, y);
                        }
                    }
                } else {
                    x += 1;
                    y += h - LOCK_ICON.getHeight(null) - 1;
                    g.drawImage(LOCK_ICON, x, y, null);
                }
            }
        };
        
        m_topComponent = new JScrollPane(m_imageContainer);
    }
    
    public void registerMouseController( InputControlManager.MouseController mouseListener) {
        m_animatorView.addMouseListener(mouseListener);
        m_animatorView.addMouseMotionListener(mouseListener);
    }

    public void registerPopupMenu( final JPopupMenu popup) {
        m_topComponent.addMouseListener(new org.openide.awt.MouseUtils.PopupMouseAdapter() {
            protected void showPopup(java.awt.event.MouseEvent e) {
                popup.show(m_topComponent, e.getX(), e.getY());
            }
        });

        m_animatorView.addMouseListener(new org.openide.awt.MouseUtils.PopupMouseAdapter() {
            protected void showPopup(java.awt.event.MouseEvent e) {
                popupAt(e.getX(), e.getY());
                popup.show(m_animatorView, e.getX(), e.getY());
            }
        });        
    }
    
    /**
     * Sets the position of the cursor. The position is in the component's coordinate
     * system.
     *
     * @param x the new position of the cursor along the x-axis.
     * @param y the new position of the cursor along the y-axis.
     */
    private void popupAt(final int x, final int y) {
        SVGLocatableElement elem = m_sceneMgr.getPerseusController()._findElementAt(x, y);
        if (m_popupElement != null) {
            m_sceneMgr.getLoookupContent().remove(m_popupElement);
        }
        
        m_popupElement = elem;
        if (m_popupElement != null){
            m_sceneMgr.getLoookupContent().add(m_popupElement);
        }
        m_animatorView.repaint();
    }
    
    public JComponent getComponent() {
        return m_topComponent;
    }

    public JComponent getAnimatorView() {
        return m_animatorView;
    }
    
    public Rectangle getImageBounds() {
        return m_animatorView.getBounds();
    }
    
    public void repaint(int x, int y, int w, int h) {
        m_animatorView.repaint(x, y, w, h);
    }
    
    public void repaint(Rectangle rect) {
        m_animatorView.repaint(rect);
    }

    public void repaint(Rectangle rect, int overlap) {
        m_animatorView.repaint(rect.x - overlap, rect.y - overlap,
                               rect.width + 2 * overlap, rect.height + 2 * overlap);
    }
    
    public void setCursor(Cursor cursor) {
        if (m_cursor != cursor) {
            m_animatorView.setCursor( cursor);
            m_cursor = cursor;
        }
    }
        
    public void setShowAllArea(boolean showAllArea) {
        if (showAllArea != m_showAllArea) {
            m_showAllArea = showAllArea;
            refresh();
        }
    }

    public boolean getShowAllArea() {
        return m_showAllArea;
    }
    
    public void setShowTooltip(boolean showTooltip) {
        m_showTooltip = showTooltip;
    }
    
    public boolean getShowTooltip() {
        return m_showTooltip;
    }
    
    public void setHighlightObject(boolean highlightObject) {
        m_highlightObject = highlightObject;
    }
    
    public boolean getHighlightObject() {
        return m_highlightObject;
    }
    
    public float getZoomRatio() {
        return m_zoomRatio;
    }
    
    public void setZoomRatio(float zoomRatio) {
        if (zoomRatio < MINIMUM_ZOOM) {
            zoomRatio = MINIMUM_ZOOM;
        } else if (zoomRatio > MAXIMUM_ZOOM) {
            zoomRatio = MAXIMUM_ZOOM;
        }
        
        if ( zoomRatio != m_zoomRatio) {
            m_zoomRatio = zoomRatio;
            refresh();
        }
    } 
    
    public void repaint() {
        m_animatorView.invalidate();
        m_topComponent.validate(); 
        m_animatorView.repaint();
        m_topComponent.repaint();
    }
    
    public void refresh() {
        SVGSVGElement svg        = m_sceneMgr.getPerseusController().getSVGRootElement();                
        SVGRect   viewBoxRect    = svg.getRectTrait("viewBox");
        SVGPoint  translatePoint = svg.getCurrentTranslate();
        SVGRect   rect           = null;
        Dimension size;

        if (!m_showAllArea) {
            translatePoint.setX(0);
            translatePoint.setY(0);
            rect = viewBoxRect;
        } else {
            translatePoint.setX( m_zoomRatio * ((m_bBox.getWidth() - m_bBox.getWidth()) / 2 - m_bBox.getX() ));
            translatePoint.setY( m_zoomRatio * ((m_bBox.getHeight() - m_bBox.getHeight()) / 2 - m_bBox.getY() ));
            rect = m_bBox;
        }
        size = new Dimension((int) (rect.getWidth() * m_zoomRatio),
                             (int) (rect.getHeight() * m_zoomRatio));
        SVGImage svgImage = m_sceneMgr.getSVGImage();
        svgImage.setViewportWidth(size.width); 
        svgImage.setViewportHeight(size.height); 

        m_animatorView.setSize(size);
        if (++m_changeTicker < 0) {
            m_changeTicker = 0;
        }
        //imageHolderPanel.setPreferredSize(size);
        repaint();
    }
    
    public short getChangeTicker() {
        return m_changeTicker;
    }
}
