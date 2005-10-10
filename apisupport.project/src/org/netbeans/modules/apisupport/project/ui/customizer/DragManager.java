/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;



/**
 *
 * @author Radek Matous
 */
final class DragManager implements DragGestureListener, DragSourceListener,
        DropTargetListener, MouseMotionListener {
    
    private JComponent component;
    
    private DragGestureRecognizer  dRecognizer;
    private DragSource dSource;
    private DropTarget dTarget;
    
    private int translateX;
    private int translateY;
    
    private final Cursor oCursor;
    
    List allItems = new ArrayList();
    private DragItem activeDragItem = null;
    
    /** Creates a new instance of SplashDnDSupport */
    DragManager(JComponent component) {
        this.component = component;
        dSource =  new DragSource();
        dRecognizer = dSource.createDefaultDragGestureRecognizer(this.component,DnDConstants.ACTION_MOVE,this);
        dTarget = new DropTarget(this.component,DnDConstants.ACTION_MOVE,this);
        component.addMouseMotionListener(this);
        oCursor = component.getCursor();
    }
    
    DragItem createNewItem() {
        DragItem retval = new DragItem();
        allItems.add(retval);
        return retval;
    }
    
    void setTranslate(int translateX, int translateY) {
        this.translateX = -translateX;
        this.translateY = -translateY;
        
        SplashComponentPreview scomp = (SplashComponentPreview)component;        
        Rectangle bounds = new Rectangle(new Dimension(scomp.image.getWidth(null),scomp.image.getHeight(null)));
        for (Iterator it = allItems.iterator(); it.hasNext();) {
            DragItem elem = (DragItem) it.next();
            elem.setBounds(bounds);
        }                            
    }
    
    public void paint(Graphics g) {
        g.setXORMode(Color.white); //Color of line varies
        
        for (Iterator it = allItems.iterator(); it.hasNext();) {
            DragItem elem = (DragItem) it.next();
            elem.paint(g);
        }
    }
    
    public void dragGestureRecognized(DragGestureEvent dge) {
        Point gesturePoint = dge.getDragOrigin();
        DragItem item = getDragItem(transformMousePoint(gesturePoint));
        if (item != null) {
            activeDragItem = item;
            activeDragItem.setGesturePoint(gesturePoint);
            Cursor curs = activeDragItem.getCursor();
            assert curs != null;
            dge.startDrag(curs,new StringSelection(""),this);
            component.repaint();
        }
        
    }
    
    private Point transformMousePoint(Point mousePoint) {
        return new Point(mousePoint.x+translateX, mousePoint.y + translateY);
    }
    
    public void dragEnter(DragSourceDragEvent dsde) {
    }
    
    public void dragOver(DragSourceDragEvent dsde) {
    }
    
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }
    
    public void dragExit(DragSourceEvent dse) {
    }
    
    public void dragDropEnd(DragSourceDropEvent dsde) {
    }
    
    public void dragEnter(DropTargetDragEvent dtde) {
    }
    
    public void dragOver(DropTargetDragEvent dtde) {
        if (activeDragItem != null) {
            activeDragItem.recalculateSize(dtde.getLocation());
            activeDragItem.updateSize();
            activeDragItem.scroll(component);                    
            component.repaint();
        }
    }
    
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }
    
    public void dragExit(DropTargetEvent dte) {
    }
    
    public void drop(DropTargetDropEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.dropComplete(true);
            if (activeDragItem != null) {
                activeDragItem.dragAccepted();
            }
        } else {
            dtde.rejectDrop();
        }
        component.setCursor(oCursor);
        activeDragItem = null;
        component.repaint();
    }
    
    public void mouseDragged(MouseEvent e) {}
    
    public void mouseMoved(MouseEvent e) {
        activeDragItem = null;
        DragItem item = null;
        for (Iterator it = allItems.iterator(); it.hasNext();) {
            DragItem elem = (DragItem) it.next();
            if (elem.contains(transformMousePoint(e.getPoint()))) {
                item = elem;
                break;
            }
        }
        if (item != null) {
            Cursor c = item.getCursor();
            component.setCursor(c);
        } else  {
            component.setCursor(oCursor);
        }
    }
    
    DragItem getDragItem(Point p2Compare) {
        DragItem retval = null;
        for (Iterator it = allItems.iterator(); it.hasNext();) {
            DragItem elem = (DragItem) it.next();
            if (elem.contains(p2Compare)) {
                retval = elem;
                break;
            }
        }
        return retval;
    }
    
    interface DropHandler {
        void dragAccepted(Rectangle original, Rectangle afterDrag);
    }
    
    static class DragItem implements Mode{
        private Point gesturePoint = new Point();
        private Rectangle rectangle = new Rectangle();
        private Rectangle currentDragRect = new Rectangle();
        private Mode dragMode;//DnDController should have instead of Mode -> RectangleEntity
        private DropHandler dHandler;
        private Rectangle bounds;
        private Mode[] allmodes = new Mode[9];
        private boolean enabled = true;
        DragItem(){
            allmodes[0] = new OneSideScaleMode(OneSideScaleMode.N_RESIZE_MODE);
            allmodes[1] = new OneSideScaleMode(OneSideScaleMode.S_RESIZE_MODE);
            allmodes[2] = new OneSideScaleMode(OneSideScaleMode.W_RESIZE_MODE);
            allmodes[3] = new OneSideScaleMode(OneSideScaleMode.E_RESIZE_MODE);
            allmodes[4] = new ScaleMode(ScaleMode.NW_RESIZE_MODE);
            allmodes[5] = new ScaleMode(ScaleMode.NE_RESIZE_MODE);
            allmodes[6] = new ScaleMode(ScaleMode.SW_RESIZE_MODE);
            allmodes[7] = new ScaleMode(ScaleMode.SE_RESIZE_MODE);
            allmodes[8] = new MoveMode();
        }
        
        void setDropHandler(DropHandler dHandler) {
            this.dHandler = dHandler;
        }
        
        public void dragAccepted() {
            if (dHandler != null) {
                dHandler.dragAccepted(rectangle,currentDragRect);
            }
        }
        
        void setRectangle(Rectangle rectangle) {
            this.rectangle.setBounds(rectangle);
            this.currentDragRect.setBounds(rectangle);
            updateSize(rectangle);
        }
        void setGesturePoint(Point gesturePoint) {
            this.gesturePoint = gesturePoint;
        }
        
        public boolean contains(Point point) {
            Mode mode = null;
            if (isEnabled()) {
                for (int i = 0; i < allmodes.length; i++) {
                    if (allmodes[i].contains(point)) {
                        mode = allmodes[i];
                        break;
                    }
                }
            }
            dragMode = mode;
            return mode != null;
        }
        
        public void updateSize() {
            updateSize(currentDragRect);
        }
        
        
        public void updateSize(Rectangle rec) {
            for (int i = 0; i < allmodes.length; i++) {
                allmodes[i].updateSize(rec);
            }
        }
        
        public void recalculateSize(Point p) {
            if (dragMode != null) {
                Rectangle oldtDragRect = new Rectangle();
                oldtDragRect.setBounds(currentDragRect);
                dragMode.recalculateSize(p);
                if (bounds != null && !bounds.contains(currentDragRect)) {
                    int x = currentDragRect.x;
                    int y = currentDragRect.y;
                    int w = currentDragRect.width;
                    int h = currentDragRect.height;
                    
                    if (x < bounds.x) {
                        x = bounds.x;
                    }
                    
                    if (y < bounds.y) {
                        y = bounds.y;
                    }

                    if (h  + y > bounds.height + bounds.y) {
                        y = (bounds.height + bounds.y) - h;
                    }

                    if (w + x > bounds.width + bounds.x ) {
                        x = (bounds.width + bounds.x) - w;
                    }
                    
                    //currentDragRect.setBounds(currentDragRect.intersection(bounds));
                    currentDragRect.setBounds(x,y,w,h);
                }
                
                if (currentDragRect.width <=0 || currentDragRect.height <=0) {                    
                    currentDragRect.setBounds(oldtDragRect);
                } 
            }
            
        }
        
        public void scroll(JComponent component) {
            component.scrollRectToVisible(currentDragRect);            
        }
        
        void setBounds(Rectangle bounds) {
            this.bounds = bounds;
        }
        
        public void paint(Graphics g) {
            if (isEnabled()) {
                for (int i = 0; i < allmodes.length; i++) {
                    allmodes[i].paint(g);
                }
            }
        }
        
        public Cursor getCursor() {
            return (dragMode != null) ? dragMode.getCursor() : null;
        }
        
        private class MoveMode implements Mode {
            public boolean contains(Point point) {
                return (currentDragRect != null && currentDragRect.contains(point));
            }
            
            public void updateSize(Rectangle rec) {}
            
            public void paint(Graphics g) {}
            
            public Cursor getCursor() {
                return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
            }
            
            public void recalculateSize(Point p) {
                currentDragRect.setBounds((p.x-gesturePoint.x)+rectangle.x,(p.y-gesturePoint.y)+rectangle.y,rectangle.width, rectangle.height);
            }
        }
        
        private class ScaleMode implements Mode {
            static final int NW_RESIZE_MODE = 0;
            static final int NE_RESIZE_MODE = 1;
            static final int SW_RESIZE_MODE = 2;
            static final int SE_RESIZE_MODE = 3;
            private static final int MOVE_MODE = 4;
            
            private int resizeMode = -1;
            private Rectangle rec;
            
            ScaleMode(int resizeMode) {
                this.resizeMode = resizeMode;
            }
            
            public boolean contains(Point point) {
                assert resizeMode != -1;
                assert rec != null;
                return rec.contains(point);
            }
            
            public void updateSize(Rectangle currentDragRect) {
                assert resizeMode != -1;
                //assert rec != null;
                
                int inset = Math.min(Math.min(5, currentDragRect.width/5), Math.min(5, currentDragRect.height/5));
                Dimension d = new Dimension(inset*2,inset*2);
                Point origin = new Point(currentDragRect.x-inset, currentDragRect.y-inset);
                switch(resizeMode) {
                    case ScaleMode.NW_RESIZE_MODE:
                        rec = new Rectangle(origin,d);
                        break;
                    case ScaleMode.NE_RESIZE_MODE:
                        rec = new Rectangle(new Point(origin.x+currentDragRect.width, origin.y),d);
                        break;
                    case ScaleMode.SW_RESIZE_MODE:
                        rec = new Rectangle(new Point(origin.x, origin.y+currentDragRect.height),d);
                        break;
                    case ScaleMode.SE_RESIZE_MODE:
                        rec = new Rectangle(new Point(origin.x+currentDragRect.width, origin.y+currentDragRect.height),d);
                        break;
                }
            }
            
            public void paint(Graphics g) {
                assert resizeMode != -1;
                assert rec != null;
                
                g.fillRect(rec.x, rec.y, rec.width, rec.height);
            }
            
            public Cursor getCursor() {
                assert resizeMode != -1;
                assert rec != null;
                
                Cursor retval = null;
                switch(resizeMode) {
                    case ScaleMode.NW_RESIZE_MODE:
                        retval = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                        break;
                    case ScaleMode.NE_RESIZE_MODE:
                        retval = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                        break;
                    case ScaleMode.SW_RESIZE_MODE:
                        retval = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                        break;
                    case ScaleMode.SE_RESIZE_MODE:
                        retval = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                        break;
                }
                assert retval != null;
                return retval;
            }
            
            public void recalculateSize(Point p) {
                int xDelta = (p.x-gesturePoint.x);
                int yDelta = (p.y-gesturePoint.y);
                switch(resizeMode) {
                    case ScaleMode.NW_RESIZE_MODE:
                        currentDragRect.setBounds(rectangle.x+xDelta,rectangle.y+yDelta,rectangle.width-xDelta, rectangle.height-yDelta);
                        break;
                    case ScaleMode.NE_RESIZE_MODE:
                        currentDragRect.setBounds(rectangle.x,rectangle.y+yDelta,rectangle.width+xDelta, rectangle.height-yDelta);
                        break;
                    case ScaleMode.SW_RESIZE_MODE:
                        currentDragRect.setBounds(rectangle.x+xDelta,rectangle.y,rectangle.width-xDelta, rectangle.height+yDelta);
                        break;
                    case ScaleMode.SE_RESIZE_MODE:
                        currentDragRect.setBounds(rectangle.x,rectangle.y,rectangle.width+xDelta, rectangle.height+yDelta);
                        break;
                }
            }
        }
        
        private class OneSideScaleMode implements Mode {
            static final int N_RESIZE_MODE = 5;
            static final int E_RESIZE_MODE = 6;
            static final int S_RESIZE_MODE = 7;
            static final int W_RESIZE_MODE = 8;
            
            private int resizeMode = -1;
            private Rectangle rec;
            
            OneSideScaleMode(int resizeMode) {
                this.resizeMode = resizeMode;
            }
            
            public boolean contains(Point point) {
                assert resizeMode != -1;
                assert rec != null;
                return rec.contains(point);
            }
            
            public void updateSize(Rectangle currentDragRect) {
                assert resizeMode != -1;
                //assert rec != null;
                int inset = 5;//Math.min(Math.min(5, currentDragRect.width/5), Math.min(5, currentDragRect.height/5));
                Dimension d = new Dimension(inset*2,inset*2);
                switch(resizeMode) {
                    case OneSideScaleMode.N_RESIZE_MODE:
                        rec = new Rectangle(currentDragRect.x+inset,currentDragRect.y-inset,currentDragRect.width-inset,2*inset);
                        break;
                    case OneSideScaleMode.E_RESIZE_MODE:
                        rec = new Rectangle(currentDragRect.x+currentDragRect.width-inset,currentDragRect.y+inset,2*inset,currentDragRect.height-inset);
                        break;
                    case OneSideScaleMode.S_RESIZE_MODE:
                        rec = new Rectangle(currentDragRect.x+inset,currentDragRect.y+currentDragRect.height-inset,currentDragRect.width-inset,2*inset);
                        break;
                    case OneSideScaleMode.W_RESIZE_MODE:
                        rec = new Rectangle(currentDragRect.x-inset,currentDragRect.y+inset,2*inset,currentDragRect.height-inset);
                        break;
                }
                
            }
            
            public void paint(Graphics g) {
                assert resizeMode != -1;
                assert rec != null;
                
                Graphics2D g2d = (Graphics2D)g;
                Stroke oStroke = g2d.getStroke();
                g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{3.0f}, 0.0f));
                Rectangle2D rec2d = rec.getBounds2D();
                Line2D line = null;
                switch(resizeMode) {
                    case OneSideScaleMode.N_RESIZE_MODE:
                        line = new Line2D.Double(rec2d.getMinX(),rec2d.getCenterY(),rec2d.getMaxX(),rec2d.getCenterY());
                        break;
                    case OneSideScaleMode.E_RESIZE_MODE:
                        line = new Line2D.Double(rec2d.getCenterX(),rec2d.getMinY(),rec2d.getCenterX(),rec2d.getMaxY());
                        break;
                    case OneSideScaleMode.S_RESIZE_MODE:
                        line = new Line2D.Double(rec2d.getMinX(),rec2d.getCenterY(),rec2d.getMaxX(),rec2d.getCenterY());
                        break;
                    case OneSideScaleMode.W_RESIZE_MODE:
                        line = new Line2D.Double(rec2d.getCenterX(),rec2d.getMinY(),rec2d.getCenterX(),rec2d.getMaxY());
                        break;
                }
                g2d.draw(line);
                g2d.setStroke(oStroke);
            }
            
            public Cursor getCursor() {
                assert resizeMode != -1;
                assert rec != null;
                Cursor retval = null;
                switch(resizeMode) {
                    case OneSideScaleMode.N_RESIZE_MODE:
                        retval = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                        break;
                    case OneSideScaleMode.E_RESIZE_MODE:
                        retval = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                        break;
                    case OneSideScaleMode.S_RESIZE_MODE:
                        retval = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                        break;
                    case OneSideScaleMode.W_RESIZE_MODE:
                        retval = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                        break;
                }
                assert retval != null;
                return retval;
            }
            
            public void recalculateSize(Point p) {
                int xDelta = (p.x-gesturePoint.x);
                int yDelta = (p.y-gesturePoint.y);
                switch(resizeMode) {
                    case OneSideScaleMode.N_RESIZE_MODE:
                        currentDragRect.setBounds(rectangle.x,rectangle.y+yDelta,rectangle.width, rectangle.height-yDelta);
                        break;
                    case OneSideScaleMode.E_RESIZE_MODE:
                        currentDragRect.setBounds(rectangle.x,rectangle.y,rectangle.width+xDelta, rectangle.height);
                        break;
                    case OneSideScaleMode.S_RESIZE_MODE:
                        currentDragRect.setBounds(rectangle.x,rectangle.y,rectangle.width, rectangle.height+yDelta);
                        break;
                    case OneSideScaleMode.W_RESIZE_MODE:
                        currentDragRect.setBounds(rectangle.x+xDelta,rectangle.y,rectangle.width-xDelta, rectangle.height);
                        break;
                }
            }
        }

        boolean isEnabled() {
            return enabled;
        }

        void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

    }
    
    private interface   Mode {
        boolean contains(Point point);
        void updateSize(Rectangle rec);
        void recalculateSize(Point p);
        void paint(Graphics g);
        Cursor getCursor();
    }
}