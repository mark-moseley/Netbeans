/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.schema.abe;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingConstants;
import org.netbeans.modules.xml.schema.abe.palette.DnDHelper;

/**
 *
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class TweenerPanel extends ABEBaseDropPanel {
    private static final long serialVersionUID = 7526472295622776147L;
    private TranslucentLabel dropInfoLabel = new TranslucentLabel(new javax.swing.ImageIcon(getClass().
            getResource("/org/netbeans/modules/xml/schema/abe/resources/bulb.png")));
    /**
     *
     *
     */
    public TweenerPanel(int orientation, InstanceUIContext context) {
        super(context);
        this.orientation=orientation;
        initialize();
        //setBorder(new LineBorder(Color.BLACK));
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Class members
    ////////////////////////////////////////////////////////////////////////////
    private static final int HBAR_NOMINAL_WIDTH = 500;
    private static final int HBAR_MAX_HEIGHT=2;
    
    private static final int VBAR_MAX_WIDTH=10;
    private static final int VBAR_NOMINAL_HEIGHT = StartTagPanel.getTagHeight();
    
    private static final int PADDING=3;
    
    private static final int EXPAND_FACTOR = 3;
    
    List<TweenerListener> tweenerListeners = new ArrayList<TweenerListener>();
    
    /**
     *
     *
     */
    private void initialize() {
        setOpaque(false);
        
        if (getOrientation()==SwingConstants.HORIZONTAL)
            _setSize(HBAR_NOMINAL_WIDTH, HBAR_MAX_HEIGHT);
        else
            _setSize(VBAR_MAX_WIDTH, VBAR_NOMINAL_HEIGHT);
    }
    
    
    /**
     *
     *
     */
    public int getOrientation() {
        return orientation;
    }
    
    
    /**
     *
     *
     */
    private void _setSize(int w, int h) {
        setPreferredSize(new Dimension(w,h));
        setMinimumSize(new Dimension(w,h));
        setMaximumSize(new Dimension(w,h));
        revalidate();
    }
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Accessors and mutators
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     *
     *
     */
    protected void handleActive(boolean value) {
        boolean oldValue=active;
        if (oldValue!=value) {
            active=value;
            if(!active)
                removeDropInfoLabel();
            repaint();
        }
    }
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Paint methods
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     *
     *
     */
    public void paint(Graphics g) {
        Graphics2D g2d=(Graphics2D)g;
        super.paint(g2d);
        if (isActive()) {
            if (getOrientation()==SwingConstants.HORIZONTAL) {
                // Horizontal
                int x = 0;
                int y = (getHeight()/2) -1 ; //-2;
                int w = getVisibleRect().width;
                
                g2d.setColor(Color.BLACK);
                
                int left=PADDING;
                int right=w-PADDING;
                
                // Left cap
                g2d.drawLine(left,y-2,left,y+3);
                g2d.drawLine(left+1,y-1,left+1,y+2);
                
                // Right cap
                g2d.drawLine(right,y-2,right,y+3);
                g2d.drawLine(right-1,y-1,right-1,y+2);
                
                // Horizontal line
                g2d.drawLine(left,y,right,y);
                g2d.drawLine(left,y+1,right,y+1);
            } else {
                // Vertical
                int x = (getWidth() / 2) - 1;
                int h = getHeight();
                
                g2d.setColor(Color.BLACK);
                
                int top=PADDING;
                int bottom = h - (PADDING * 4) ;
                
                // Top cap
                g2d.drawLine(x-2,top,x+3,top);
                g2d.drawLine(x-1,top+1,x+2,top+1);
                
                // Bottom cap
                g2d.drawLine(x-2,bottom,x+3,bottom);
                g2d.drawLine(x-1,bottom-1,x+2,bottom-1);
                
                // Vertical line
                g2d.drawLine(x,top,x,bottom);
                g2d.drawLine(x+1,top,x+1,bottom);
            }
        }
    }
    
    public void addTweenerListener(TweenerListener tl){
        tweenerListeners.add(tl);
    }
    
    
    private int orientation;
    private boolean active;
    
    
    private void showExpanded(){
        Dimension dim = new Dimension(HBAR_NOMINAL_WIDTH, HBAR_MAX_HEIGHT * EXPAND_FACTOR);
        setPreferredSize(dim);
        setMinimumSize(dim);
        revalidate();
        //getParent().validate();
    }
    
    private void showCollapsed(){
        Dimension dim = new Dimension(HBAR_NOMINAL_WIDTH, HBAR_MAX_HEIGHT);
        setPreferredSize(dim);
        setMinimumSize(dim);
        revalidate();
        //getParent().validate();
    }
    
    public void drop(DropTargetDropEvent event) {
        context.setUserInducedEventMode(true);
        try{
            for(TweenerListener tl: tweenerListeners){
                if(!tl.dragAccept(DnDHelper.getDraggedPaletteItem(event))){
                    event.rejectDrop();
                    return;
                }
            }
            
            for(TweenerListener tl: tweenerListeners){
                tl.drop(DnDHelper.getDraggedPaletteItem(event));
            }
            if(orientation == SwingConstants.HORIZONTAL){
                showCollapsed();
                for(TweenerListener tl: tweenerListeners){
                    tl.dragExited();
                }
            }
        }finally{
            context.setUserInducedEventMode(false);
        }
    }
    
    public void dragExit(DropTargetEvent event) {
        if(orientation == SwingConstants.HORIZONTAL){
            showCollapsed();
        }
        for(TweenerListener tl: tweenerListeners){
            tl.dragExited();
        }
    }
    
    public void dragOver(DropTargetDragEvent event) {
    }
    
    public void dragEnter(DropTargetDragEvent event) {
        if(orientation == SwingConstants.HORIZONTAL){
            showExpanded();
        }
        for(TweenerListener tl: tweenerListeners){
            if(!tl.dragAccept(DnDHelper.getDraggedPaletteItem(event))){
                //addDropInfoLabel();
                event.rejectDrag();
                return;
            }
        }
        for(TweenerListener tl: tweenerListeners){
            tl.dragEntered(DnDHelper.getDraggedPaletteItem(event));
        }
        addDropInfoLabel();
    }
    
    
    //NBGlassPaneAccessSupport gpSupport;
    
    
    private void addDropInfoLabel() {
        String infoText = getDropInfoText();
        if(infoText != null){
            UIUtilities.showBulbMessageFor(infoText, context, this);
        }
    }
    
    private void removeDropInfoLabel() {
        NBGlassPaneAccessSupport.disposeNBGlassPane();
    }
    
    private String dropInfoText;
    public String getDropInfoText() {
        return dropInfoText;
    }
    
    public void setDropInfoText(String dropInfoText) {
        this.dropInfoText = dropInfoText;
    }

    public void accept(UIVisitor visitor) {
        //does not contribute for UI traversal
    }
    
    
}
