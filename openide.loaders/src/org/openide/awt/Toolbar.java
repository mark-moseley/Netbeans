/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.awt;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.cookies.InstanceCookie;
import org.openide.util.actions.Presenter;
import org.openide.util.Task;
import org.openide.util.Utilities;

/**
 * Toolbar provides a component which is useful for displaying commonly used
 * actions.  It can be dragged inside its <code>ToolbarPanel</code> to
 * customize its location.
 *
 * @author  David Peroutka, Libor Kramolis
 */
public class Toolbar extends JToolBar /*implemented by patchsuperclass MouseInputListener*/ {
    /** Basic toolbar height.
     @deprecated Use getBasicHeight instead. */
    public static final int BASIC_HEIGHT = 34;
    
    /** 5 pixels is tolerance of toolbar height so toolbar can be high (BASIC_HEIGHT + HEIGHT_TOLERANCE)
        but it will be set to BASIC_HEIGHT high. */
    static int HEIGHT_TOLERANCE = 5;
    /** TOP of toolbar empty border. */
    static int TOP = 2;
    /** LEFT of toolbar empty border. */
    static int LEFT = 3;
    /** BOTTOM of toolbar empty border. */
    static int BOTTOM = 2;
    /** RIGHT of toolbar empty border. */
    static int RIGHT = 3;
    /** Residual size of the toolbar when dragged far right */
    static int RESIDUAL_WIDTH = 16;
   

    /** is toolbar floatable */
    private boolean floatable;
    /** Toolbar DnDListener */
    private DnDListener listener;
    /** Toolbar mouse listener */
    private ToolbarMouseListener mouseListener;
    /** display name of the toolbar */
    private String displayName;
    
    /** Used for lazy creation of Folder and DisplayName */
    private DataFolder backingFolder;
    /* FolderInstance that will track all the changes in backingFolder */
    private Folder processor;

    static final long serialVersionUID =5011742660516204764L;

    /** Create a new Toolbar with empty name. */
    public Toolbar () {
        this (""); // NOI18N
    }

    /** Create a new not floatable Toolbar with programmatic name.
     * Display name is set to be the same as name */
    public Toolbar (String name) {
        this (name, name, false);
    }

    /** Create a new not floatable Toolbar with specified programmatic name
     * and display name */
    public Toolbar (String name, String displayName) {
        this (name, displayName, false);
    }
    
    /** Create a new <code>Toolbar</code>.
     * @param name a <code>String</code> containing the associated name
     * @param f specified if Toolbar is floatable
     * Display name of the toolbar is set equal to the name.
     */
    public Toolbar (String name, boolean f) {
        this (name, name, f);
    }
        
    Toolbar(DataFolder folder, boolean f) {
        super();
        backingFolder = folder;
        initAll(folder.getName(), f);
        initDnD();
    }
    
    private void initDnD() {
        DropTarget dt = new DropTarget(this, dnd);
    }
    
    DataFolder getFolder() {
        return backingFolder;
    }
    

    public void paint( Graphics g ) {
        super.paint( g );
        if( -1 != dropTargetButtonIndex ) {
            paintDropGesture( g );
        }
    }
    
    private void updateDropGesture( DropTargetDragEvent e ) {
        Point p = e.getLocation();
        Component c = getComponentAt(p);
        int index = Toolbar.this.getComponentIndex(c);
        if( index == 0 ) {
            //dragging over toolbar's grip
            resetDropGesture();
        } else {
            //find out whether we want to drop before or after this component
            boolean b = p.x <= c.getLocation().x + c.getWidth() / 2;
            if( index != dropTargetButtonIndex || b != insertBefore ) {
                dropTargetButtonIndex = index;
                insertBefore = b;
                repaint();
            }
        }
    }
    
    private void resetDropGesture() {
        dropTargetButtonIndex = -1;
        repaint();
    }
    
    private void paintDropGesture( Graphics g ) {
        Component c = getComponentAtIndex( dropTargetButtonIndex );
        if( null == c )
            return;
        
        Point location = c.getLocation();
        int cursorLocation = location.x;
        if( !insertBefore ) {
            cursorLocation += c.getWidth();
            if( dropTargetButtonIndex == getComponentCount()-1 )
                cursorLocation -= 3;
        }
        drawDropLine( g, cursorLocation );
    }
    
    private void drawDropLine( Graphics g, int x ) {
        Color oldColor = g.getColor();
        g.setColor( Color.black );
        int height = getHeight();
        g.drawLine( x, 3, x, height-4 );
        g.drawLine( x-1, 3, x-1, height-4 );

        g.drawLine( x+1, 2, x+1+2, 2 );
        g.drawLine( x+1, height-3, x+1+2, height-3 );

        g.drawLine( x-2, 2, x-2-2, 2 );
        g.drawLine( x-2, height-3, x-2-2, height-3 );
        g.setColor( oldColor );
    }

    /**
     * Component index of the button under the drag cursor, or -1 when the cursor
     * is above the toolbar drag handle
     */
    int dropTargetButtonIndex = -1;
    /**
     * Component index of the button being dragged, only used when dragging a button
     * within the same toolbar.
     */
    int dragSourceButtonIndex = -1;
    /**
     * True if the button being dragged should be dropped BEFORE the button 
     * under the drag cursor.
     */
    boolean insertBefore = true;
    /**
     * True indicates the toolbar instance whose button is being dragged.
     */
    boolean isDragSourceToolbar = false;
    
    private DnDSupport dnd = new DnDSupport();
    private class DnDSupport implements DragSourceListener, DragGestureListener, DropTargetListener {
        private DragSource dragSource = new DragSource();
        
        private Cursor dragMoveCursor = createCustomCursor( Utilities.loadImage( "org/openide/resources/cursorsmovesingle.gif"), "ACTION_MOVE" );
        private Cursor dragNoDropCursor = createCustomCursor( Utilities.loadImage( "org/openide/resources/cursorsnone.gif"), "NO_ACTION_MOVE" );
        
        public DnDSupport() {
            dragSource.addDragSourceListener(this);
        }
        
        public void register(Component c) {
            dgr = dragSource.createDefaultDragGestureRecognizer(c, DnDConstants.ACTION_MOVE, this);
            if (dgr != this.dgr) {
                this.dgr = dgr;
                try {
                    dgr.addDragGestureListener(this);
                } catch (TooManyListenersException e) {
                    //do nothing
                }
            }
        }
        DragGestureRecognizer dgr = null;

        public void dragEnter(DragSourceDragEvent e) {
            DragSourceContext context = e.getDragSourceContext();
            int myaction = e.getDropAction();
            if ((myaction & DnDConstants.ACTION_MOVE) != 0) {
                context.setCursor( dragMoveCursor ); //DragSource.DefaultMoveDrop);
            } else {
                context.setCursor( dragNoDropCursor ); //DragSource.DefaultCopyNoDrop);                
            }
        }

        public void dragOver(DragSourceDragEvent e) {
            DragSourceContext context = e.getDragSourceContext();
            int action = e.getDropAction();
            if ((action & DnDConstants.ACTION_MOVE) != 0) {
                context.setCursor( dragMoveCursor ); //DragSource.DefaultMoveDrop);
            } else {
                context.setCursor( dragNoDropCursor ); //DragSource.DefaultCopyNoDrop);                
            }
        }
        
        public void dragExit(DragSourceEvent e) {
            DragSourceContext context = e.getDragSourceContext();
            context.setCursor( dragNoDropCursor ); //DragSource.DefaultCopyNoDrop);                
            resetDropGesture();
        }

        public void dragDropEnd(DragSourceDropEvent e) {
            isDragSourceToolbar = false;
            if ( e.getDropSuccess() == false ) {
                  return;
                }
            e.getDragSourceContext().getComponent().repaint();
            resetDropGesture();
        }
        
        public void dragGestureRecognized(DragGestureEvent e) {
              try {
                 Component c = e.getComponent();
                 //do not allow to drag toolbar separators
                 if( c instanceof JToolBar.Separator || "grip".equals( c.getName() ) )
                     return;
                 Transferable t = null;
                 if (c instanceof JComponent) {
                     DataObject dob = (DataObject) ((JComponent) c).getClientProperty("file");
                     if (dob != null) {
                         t = new TbTransferable(dob);
                     }
                 }
                 if (t != null) {
                    dragSourceButtonIndex = Toolbar.this.getComponentIndex( c );
                    isDragSourceToolbar = true;
                    dragSource.startDrag(e, dragMoveCursor/*DragSource.DefaultCopyNoDrop*/, t, this);
                 }
                
              } catch ( InvalidDnDOperationException idoe ) {
                    ErrorManager.getDefault().notify(idoe);
              }
        }

        public void dropActionChanged (DragSourceDragEvent e) {
            DragSourceContext context = e.getDragSourceContext();
            int myaction = e.getDropAction();
            if ((myaction & DnDConstants.ACTION_MOVE) != 0) {
                context.setCursor( dragMoveCursor ); //DragSource.DefaultMoveDrop);
            } else {
                context.setCursor( dragNoDropCursor ); //DragSource.DefaultCopyNoDrop);                
            }
        }
        
        public void drop(DropTargetDropEvent dtde) {
            Transferable t = dtde.getTransferable();
            try {
                Object o = t.getTransferData( tiDataFlavor ); //XXX
                if (o instanceof DataObject) {
                    doDrop( (DataObject) o, dropTargetButtonIndex-1, insertBefore );
                    
                    resetDropGesture();
                }
            } catch (UnsupportedFlavorException e) {
                ErrorManager.getDefault().notify (e);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
        
        public void dragExit(DropTargetEvent dte) {
            resetDropGesture();
        }
        
        public void dropActionChanged(DropTargetDragEvent dtde) {
            
        }

        public void dragEnter(DropTargetDragEvent e) {
            if( e.isDataFlavorSupported( tiDataFlavor ) ) {
                e.acceptDrag(DnDConstants.ACTION_MOVE);
            }
        }

        public void dragOver(DropTargetDragEvent e) {
            updateDropGesture( e );
            if( !validateDropPosition() ) {
                e.rejectDrag();
            } else {
                e.acceptDrag( DnDConstants.ACTION_MOVE );
            }
        }
        

        //XXX copy & paste from DragDropUtilities, make it public instead?
        /**
         * Returns cursor created from given icon.
         */
        private Cursor createCustomCursor(Image icon, String name) {
            Toolkit t = Toolkit.getDefaultToolkit();
            Dimension d = t.getBestCursorSize(16, 16);
            Image i = icon;
            if (d.width != icon.getWidth(null)) {
                // need to resize the icon
                Image empty = createBufferedImage(d.width, d.height);
                i = Utilities.mergeImages(icon, empty, 0, 0);
            }
            return t.createCustomCursor(i, new Point(1,1), name);
        }
    
        /** 
         * Creates BufferedImage and Transparency.BITMASK 
         * Note: this method is copied from org.openide.util.IconManager. Should
         * it be exposed in Utilities? I don't know (dstrupl).
         */
        private final BufferedImage createBufferedImage(int width, int height) {
            ColorModel model = GraphicsEnvironment.getLocalGraphicsEnvironment().
                                              getDefaultScreenDevice().getDefaultConfiguration().getColorModel(Transparency.BITMASK);
            BufferedImage buffImage = new BufferedImage(model,
                    model.createCompatibleWritableRaster(width, height), model.isAlphaPremultiplied(), null);
            return buffImage;
        }
        
        //XXX copy&paste end
    }
    
    void doDrop( DataObject ob, int dropIndex, boolean dropBefore ) throws IOException {
        //find out which button is currently under the drag cursor
        DataObject[] buttons = backingFolder.getChildren();
        DataObject objUnderCursor = null;
        boolean appendToEnd = false;
        if( buttons.length > 0 ) {
            if( !dropBefore )
                dropIndex++;
            if( dropIndex >= buttons.length ) {
                objUnderCursor = buttons[buttons.length-1];
                appendToEnd = true;
            } else {
                objUnderCursor = buttons[dropIndex];
            }
        }

        if( Toolbar.this.isDragSourceToolbar ) {
            //do nothing, we're just reordering buttons in this toolbar
        } else {
            //move button to the new toolbar
            ob.move(backingFolder);                 
        }

        if( null != objUnderCursor ) {
            //reorder children
            java.util.List children = new ArrayList( Arrays.asList( backingFolder.getChildren() ) );
            int targetIndex = children.indexOf( objUnderCursor );
            int currentIndex = children.indexOf( ob );
            if( currentIndex < targetIndex )
                targetIndex--;
            children.remove( ob );
            if( appendToEnd )
                children.add( ob );
            else
                children.add( targetIndex, ob );

            backingFolder.setOrder( (DataObject[])children.toArray( new DataObject[children.size()]) );
        }
        //else we're dragging a button to an empty toolbar
    }
    
    private boolean validateDropPosition() {
               //the drag cursor cannot be positioned above toolbar's drag handle
        return dropTargetButtonIndex >= 0
               //when toolbar has buttons '1 2 3 4 5' and we're dragging button 3,
               //do not allow drop between buttons 2 and 3 and also between buttons 3 and 3
               && !(isDragSourceToolbar && (dragSourceButtonIndex == dropTargetButtonIndex  //drop index 3
                                        || (dropTargetButtonIndex == dragSourceButtonIndex-1 && !insertBefore) //drop index 2
                                        || (dropTargetButtonIndex == dragSourceButtonIndex+1 && insertBefore))) //drop index 4
               //dragging a button to an empty toolbar
               || (dropTargetButtonIndex < 0 && getComponentCount() == 1);
    }

    private static DataFlavor tiDataFlavor = new DataFlavor( DataObject.class, "Toolbar Item" );

    private static class TbTransferable implements Transferable, ClipboardOwner {
        final DataObject obj;
        
      public TbTransferable (DataObject obj) {
          this.obj = obj;
      }
        
      public synchronized DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { 
            tiDataFlavor
        };
      }
      
      public boolean isDataFlavorSupported( DataFlavor flavor ) {
        return flavor == tiDataFlavor;
      }
      
      public synchronized Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return obj; //XXX
            
      }
      
      public void lostOwnership (Clipboard clip, Transferable tf) {
          //Hmm...
      }
    }    

    /** Start tracking content of the underlaying folder if not doing so yet */
    final Folder waitFinished() {
        // check for too early call (from constructor and UI.setUp...)
        if (backingFolder == null) return null;
        
        if(processor == null && isVisible()) {
            processor = new Folder(); // It will start tracking immediatelly
        }
        return processor;
    }    
    
    public void addNotify() {
        super.addNotify();
        waitFinished();
    }
    
    public Component[] getComponents () {
        waitFinished ();
        return super.getComponents ();
    }
    
    public void setVisible(boolean b) {
	super.setVisible(b);
	waitFinished();	
    }
    
    /** Overridden to set focusable to false for any AbstractButton
     * subclasses which are added */
    protected void addImpl(Component c, Object constraints, int idx) {
        //issue 39896, after opening dialog from toolbar button, focus
        //remains on toolbar button.  Does not create an accessibility issue - 
        //all standard toolbar buttons are also available via the keyboard
        if (c instanceof AbstractButton) {
            c.setFocusable(false);
            ((JComponent) c).setOpaque(false);
            if (!(c instanceof JToggleButton)) {
                //JDK 1.5 metal/ocean resets borders, so fix it this way
                ((AbstractButton) c).setBorderPainted(false);
                ((AbstractButton) c).setOpaque(false);
            }
        }
        super.addImpl (c, constraints, idx);
        if (!"grip".equals(c.getName())) {
            dnd.register(c);
        }
    }
    
    /**
     * Create a new <code>Toolbar</code>.
     * @param name a <code>String</code> containing the associated name
     * @param f specified if Toolbar is floatable
     */
    public Toolbar (String name, String displayName, boolean f) {
        super();
        setDisplayName (displayName);
        initAll(name, f);
    }
    
    /** Returns basic toolbar height according to preferred icons size. Used by
     * toolbar layout manager.
     * @return basic toolbar height
     * @since 4.15
     */
    public static int getBasicHeight () {
        if (ToolbarPool.getDefault().getPreferredIconSize() == 24) {
            return 44;
        } else {
            return 34;
        }
    }
    
    private void initAll(String name, boolean f) {
        floatable = f;
        mouseListener = null;

        setName (name);
        
        setFloatable (false);
        String lAndF = UIManager.getLookAndFeel().getName();
        
        if (lAndF.equals("Windows")) {
            //Get rid of extra height, also allow for minimalist main
            //window
            setBorder(Boolean.getBoolean("netbeans.small.main.window") ?
                BorderFactory.createEmptyBorder(1,1,1,1) : 
                BorderFactory.createEmptyBorder()); //NOI18N
        } else if (!"Aqua".equals(UIManager.getLookAndFeel().getID()) && !"GTK".equals(UIManager.getLookAndFeel().getID())){
            Border b = UIManager.getBorder ("ToolBar.border"); //NOI18N
            
            if ((b==null) || (b instanceof javax.swing.plaf.metal.MetalBorders.ToolBarBorder))  
                b=BorderFactory.createEtchedBorder (EtchedBorder.LOWERED);
            setBorder (new CompoundBorder ( 
                   b,
                   new EmptyBorder (TOP, LEFT, BOTTOM, RIGHT))
                   );  
             
        } 
        if (!"Aqua".equals(UIManager.getLookAndFeel().getID())) {
            putClientProperty("JToolBar.isRollover", Boolean.TRUE); // NOI18N
        }
        addGrip();

        getAccessibleContext().setAccessibleName(displayName == null ? getName() : displayName);
        getAccessibleContext().setAccessibleDescription(getName());
    }

    public String getUIClassID() {
        if (UIManager.get("Nb.Toolbar.ui") != null) { //NOI18N
            return "Nb.Toolbar.ui"; //NOI18N
        } else {
            return super.getUIClassID();
        }
    }
    
    public Dimension getPreferredSize() {
        String lfid = UIManager.getLookAndFeel().getID();
        int minheight;
        
        if (ToolbarPool.getDefault().getPreferredIconSize() == 24) {
            if ("Aqua".equals(lfid)) {
                minheight = 29 + 8;
            } else if ("Metal".equals(lfid)) {
                minheight = 36 + 8;
            } else if ("Windows".equals(lfid)) {
                minheight = isXPTheme() ? (23 + 8) : (27 + 8);
            } else if ("GTK".equals(lfid)) {
                minheight = 36 + 8;
            } else {
                minheight = 28 + 8;
            }
        } else {
            if ("Aqua".equals(lfid)) {
                minheight = 29;
            } else if ("Metal".equals(lfid)) {
                minheight = 36;
            } else if ("Windows".equals(lfid)) {
                minheight = isXPTheme() ? 23 : 27;
            } else if ("GTK".equals(lfid)) {
                minheight = 36;
            } else {
                minheight = 28;
            }
        }
        Dimension result = super.getPreferredSize();
        result.height = Math.max (result.height, minheight);
        return result;
    }

    /** Removes all ACTION components. */
    public void removeAll () {
        super.removeAll();
        addGrip();
    }

    /**
     * When Toolbar is floatable, ToolbarBump is added as Grip as first toolbar component
     * modified by Michael Wever, to use l&f's grip/bump. */
    void addGrip () {
        if (floatable) {
            /** Uses L&F's grip **/
            String lAndF = UIManager.getLookAndFeel().getName();
            //XXX should use getID() note getName() - Tim
            JPanel dragarea = lAndF.equals("Windows") ? isXPTheme() ?
                                    (JPanel)new ToolbarXP() : 
                                    (JPanel) new ToolbarGrip() :
                                    UIManager.getLookAndFeel().getID().equals("Aqua")
                                    ? (JPanel) new ToolbarAqua() :
                                    (JPanel)new ToolbarBump(); //NOI18N
            if (mouseListener == null)
                mouseListener = new ToolbarMouseListener ();

            dragarea.addMouseListener (mouseListener);
            dragarea.addMouseMotionListener (mouseListener);

            dragarea.setName ("grip");
            add (dragarea);
        }
    }

    /** Compute with HEIGHT_TOLERANCE number of rows for specific toolbar height.
     * @param height of some toolbar
     * @return number of rows
     */
    static public int rowCount (int height) {
        return 1 + height / (getBasicHeight() + HEIGHT_TOLERANCE);
    }

    /** Set DnDListener to Toolbar.
     * @param l DndListener for toolbar
     */
    public void setDnDListener (DnDListener l) {
        listener = l;
    }
    
    /** @return Display name of this toolbar. Display name is localizable,
     * on the contrary to the programmatic name */
    public String getDisplayName () {
        if (displayName == null) {
            if (!backingFolder.isValid()) {
                // #17020
                return backingFolder.getName();
            }
            return backingFolder.getNodeDelegate ().getDisplayName ();
        }
        return displayName;
    }
    
    /** Sets new display name of this toolbar. Display name is localizable,
     * on the contrary to the programmatic name */
    public void setDisplayName (String displayName) {
        this.displayName = displayName;
    }

    /** Fire drag of Toolbar
     * @param dx distance of horizontal dragging
     * @param dy distance of vertical dragging
     * @param type type of toolbar dragging
     */
    protected void fireDragToolbar (int dx, int dy, int type) {
        if (listener != null)
            listener.dragToolbar (new DnDEvent (this, getName(), dx, dy, type));
    }

    /** Fire drop of Toolbar
     * @param dx distance of horizontal dropping
     * @param dy distance of vertical dropping
     * @param type type of toolbar dropping
     */
    protected void fireDropToolbar (int dx, int dy, int type) {
        if (listener != null)
            listener.dropToolbar (new DnDEvent (this, getName(), dx, dy, type));
    }

    synchronized final MouseInputListener mouseDelegate () {
        if (mouseListener == null) mouseListener = new ToolbarMouseListener ();
        return mouseListener;
    }

    /** Toolbar mouse listener. */
    class ToolbarMouseListener extends MouseInputAdapter {
        /** Is toolbar dragging now. */
        private boolean dragging = false;
        /** Start point of dragging. */
        private Point startPoint = null;

        /** Invoked when a mouse button has been pressed on a component. */
        public void mousePressed (MouseEvent e) {
            startPoint = e.getPoint();
        }

        /** Invoked when a mouse button has been released on a component. */
        public void mouseReleased (MouseEvent e) {
            if (dragging) {
                
                int dx = getX() + e.getX() - startPoint.x > getParent().getWidth() - RESIDUAL_WIDTH ?
                0 : e.getX() - startPoint.x;
                
                fireDropToolbar (dx,
                                 e.getY() - startPoint.y,
                                 DnDEvent.DND_ONE);
                dragging = false;
            }
        }

        /** Invoked when a mouse button is pressed on a component and then dragged. */
        public void mouseDragged (MouseEvent e) {
            int m = e.getModifiers();
            int type = DnDEvent.DND_ONE;
            int dx;
            
            if (e.isControlDown())
                type = DnDEvent.DND_LINE;
            else if (((m & InputEvent.BUTTON2_MASK) != 0) ||
                     ((m & InputEvent.BUTTON3_MASK) != 0))
                type = DnDEvent.DND_END;
            if (startPoint == null) {
                startPoint = new Point (e.getX(), e.getY());
            }
            
            if ( getX() + e.getX() + startPoint.x > getParent().getWidth() - RESIDUAL_WIDTH ) {
                if ( getX() >= getParent().getWidth() - RESIDUAL_WIDTH ) {
                    dx = 0;
                }
                else {
                    dx = getParent().getWidth() - RESIDUAL_WIDTH - getX();
                }
            }
            else {
                dx = e.getX() - startPoint.x; 
            }
            
            fireDragToolbar ( dx,
                             e.getY() - startPoint.y,
                             type);
            dragging = true;
        }

    } // end of inner class ToolbarMouseListener

    /**
     * This class can be used to produce a <code>Toolbar</code> instance from
     * the given <code>DataFolder</code>.
     */
    final class Folder extends FolderInstance {

        /**
         * Creates a new folder on the specified <code>DataFolder</code>.
         *
         */
        public Folder () {
            super (backingFolder);
            recreate ();
        }

        /**
         * Full name of the data folder's primary file separated by dots.
         * @return the name
         */
        public String instanceName () {
            return Toolbar.this.getClass().getName();
        }

        /**
         * Returns the root class of all objects.
         * @return Object.class
         */
        public Class instanceClass ()
        throws java.io.IOException, ClassNotFoundException {
            return Toolbar.this.getClass();
        }

        /** If no instance cookie, tries to create execution action on the
         * data object.
         */
        protected InstanceCookie acceptDataObject (DataObject dob) {
            InstanceCookie ic = super.acceptDataObject (dob);
            if (ic == null) {
                JButton button = ExecBridge.createButton (dob);
                if (button != null) {
                    System.err.println("Button is " + button);
                    System.err.println("dob is " + dob);
                    button.putClientProperty ("file", dob);
                }
                return button != null ? new InstanceSupport.Instance (button) : null;
            } else {
                return ic;
            }
        }
        
    private Map cookiesToObjects = new HashMap();
    
    protected Object instanceForCookie (DataObject obj, InstanceCookie cookie)
    throws IOException, ClassNotFoundException {
        Object result = super.instanceForCookie(obj, cookie);
        cookiesToObjects.put (result, obj);
        return result;
    }
        

        /**
         * Accepts only cookies that can provide <code>Toolbar</code>.
         * @param cookie an <code>InstanceCookie</code> to test
         * @return true if the cookie can provide accepted instances
         */
        protected InstanceCookie acceptCookie (InstanceCookie cookie)
        throws java.io.IOException, ClassNotFoundException {
            boolean is;
            
            if (cookie instanceof InstanceCookie.Of) {
                InstanceCookie.Of of = (InstanceCookie.Of)cookie;
                is = of.instanceOf (Component.class) ||
                     of.instanceOf (Presenter.Toolbar.class) ||
                     of.instanceOf (Action.class);
            } else {
                Class c = cookie.instanceClass();
                is = Component.class.isAssignableFrom(c) ||
                     Presenter.Toolbar.class.isAssignableFrom(c) ||
                     Action.class.isAssignableFrom (c);
            }
            return is ? cookie : null;
        }

        /**
         * Returns a <code>Toolbar.Folder</code> cookie for the specified
         * <code>DataFolder</code>.
         * @param df a <code>DataFolder</code> to create the cookie for
         * @return a <code>Toolbar.Folder</code> for the specified folder
         */
        protected InstanceCookie acceptFolder(DataFolder df) {
            return null; // PENDING new Toolbar.Folder(df);
        }

        /**
         * Updates the <code>Toolbar</code> represented by this folder.
         *
         * @param cookies array of instance cookies for the folder
         * @return the updated <code>ToolbarPool</code> representee
         */
        protected Object createInstance(final InstanceCookie[] cookies)
        throws java.io.IOException, ClassNotFoundException {
            // refresh the toolbar's content
            Toolbar.this.removeAll();
            for (int i = 0; i < cookies.length; i++) {
                try {
                    Object obj = cookies[i].instanceCreate();
                    Object file = cookiesToObjects.get(obj);
                    if (obj instanceof Presenter.Toolbar) {
                        obj = ((Presenter.Toolbar)obj).getToolbarPresenter();
                        // go on to get thru next condition
                    }
                    if (obj instanceof Component) {
                        // remove border and grip if requested. "Fixed" toolbar
                        // item has to live alone in toolbar now
                        if ((obj instanceof JComponent) &&
                            "Fixed".equals(((JComponent)obj).getClientProperty("Toolbar"))) { // NOI18N
                            floatable = false;
                            Toolbar.this.removeAll();
                            setBorder(null);
                        }
                        if (obj instanceof JComponent) {
                            if (ToolbarPool.getDefault().getPreferredIconSize() == 24) {
                                ((JComponent) obj).putClientProperty("PreferredIconSize",new Integer(24)); //NOI18N
                            }
                            ((JComponent) obj).putClientProperty("file", file);
                        }
                        Toolbar.this.add ((Component)obj);
                        continue;
                    }
                    if (obj instanceof Action) {
                        Action a = (Action)obj;
                        JButton b = new JButton();
                        if (ToolbarPool.getDefault().getPreferredIconSize() == 24) {
                            b.putClientProperty("PreferredIconSize",new Integer(24)); //NOI18N
                        }
                        Actions.connect (b, a);
                        b.putClientProperty ("file", file);
                        Toolbar.this.add (b);
                        continue;
                    }
                } catch (java.io.IOException ex) {
                    ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ex);
                } catch (ClassNotFoundException ex) {
                    ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ex);
                } finally {
                   cookiesToObjects.clear();
                }
            }

            // invalidate the toolbar, trigger proper relayout
            Toolbar.this.invalidate ();
            return Toolbar.this;
        }

        /** Recreate the instance in AWT thread.
        */
        protected Task postCreationTask (Runnable run) {
            return new AWTTask (run);
        }

    } // end of inner class Folder

    /** Bumps for floatable toolbar */
    private final class ToolbarBump extends JPanel {
        /** Top gap. */
        static final int TOPGAP = 2;
        /** Bottom gap. */
        static final int BOTGAP = 2;
        /** Width of bump element. */
        static final int WIDTH = 6;
        
        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;

        static final long serialVersionUID =-8819972936203315277L;

        /** Create new ToolbarBump. */
        public ToolbarBump () {
            super ();
            int width = WIDTH;
            dim = new Dimension (width, width);
            max = new Dimension (width, Integer.MAX_VALUE);
            this.setToolTipText (Toolbar.this.getDisplayName());
        }

        /** Paint bumps to specific Graphics. */
        public void paint (Graphics g) {
            Dimension size = this.getSize ();
            int height = size.height - BOTGAP;
            g.setColor (this.getBackground ());

            for (int x = 0; x+1 < size.width; x+=4) {
                for (int y = TOPGAP; y+1 < height; y+=4) {
                    g.setColor (this.getBackground ().brighter ());
                    g.drawLine (x, y, x, y);
                    if (x+5 < size.width && y+5 < height)
                        g.drawLine (x+2, y+2, x+2, y+2);
                    g.setColor (this.getBackground ().darker ().darker ());
                    g.drawLine (x+1, y+1, x+1, y+1);
                    if (x+5 < size.width && y+5 < height)
                        g.drawLine (x+3, y+3, x+3, y+3);
                }
            }
        }

        /** @return minimum size */
        public Dimension getMinimumSize () {
            return dim;
        }

        /** @return preferred size */
        public Dimension getPreferredSize () {
            return this.getMinimumSize ();
        }

        public Dimension getMaximumSize () {
            return max;
        }
    } // end of inner class ToolbarBump

    /** Recognizes if XP theme is set.
     * @return true if XP theme is set, false otherwise
     */
    private static Boolean isXP = null;
    private static boolean isXPTheme () {
        if (isXP == null) {
            Boolean xp = (Boolean)Toolkit.getDefaultToolkit().
            getDesktopProperty("win.xpstyle.themeActive"); //NOI18N
            isXP = Boolean.TRUE.equals(xp)? Boolean.TRUE : Boolean.FALSE;
        }
        return isXP.booleanValue();
    }    
    
    private final class ToolbarAqua extends JPanel {
        /** Width of grip */
        static final int WIDTH = 5;
        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;
        static final long serialVersionUID =-8819972972003315277L;

        public ToolbarAqua() {
            dim = new Dimension (WIDTH, WIDTH);
            max = new Dimension (WIDTH, Integer.MAX_VALUE);
            this.setToolTipText (Toolbar.this.getDisplayName());
        }
        
        public void paintComponent (Graphics g) {
            super.paintComponent(g);
            java.awt.Graphics2D g2d = (Graphics2D) g;
            g2d.addRenderingHints(getHints());
            
            int sz = 5;
            
            int y = (getHeight() / 2) - (sz / 2);
            int x = (getWidth() / 2) - (sz / 2);
            
            y-=2;
            
            GradientPaint gradient = new GradientPaint(x+1, y+1, Color.BLACK,
            x+sz-1, y+sz-1, Color.WHITE);
            
            Paint paint = g2d.getPaint();
            
            g2d.setPaint(gradient);
            g2d.drawArc(x,y,sz,sz,0,359);
            
            g.setColor(new Color(240,240,240));
            g.drawLine(x+(sz/2), y + (sz/2),x+(sz/2), y + (sz/2));

            g2d.setPaint(paint);
        }
        
        /** @return minimum size */
        public Dimension getMinimumSize () {
            return dim;
        }
        
        /** @return preferred size */
        public Dimension getPreferredSize () {
            return this.getMinimumSize ();
        }
        
        public Dimension getMaximumSize () {
            return max;
        }
    }    

    private static java.util.HashMap hintsMap = null;
    static final java.util.Map getHints() {
        if (hintsMap == null) {
            hintsMap = new java.util.HashMap();
            hintsMap.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            hintsMap.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        return hintsMap;
    }
    
    private final class ToolbarXP extends JPanel {
        /** Width of grip */
        static final int WIDTH = 6;
        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;
        
        static final long serialVersionUID =-8819972936203315277L;
        public ToolbarXP() {
            dim = new Dimension (WIDTH, WIDTH);
            max = new Dimension (WIDTH, Integer.MAX_VALUE);
            this.setToolTipText (Toolbar.this.getDisplayName());
        }
        
        public void paintComponent (Graphics g) {
            super.paintComponent(g);
            int x = 2;
            for (int i=3; i < getHeight()-3; i+=4) {
                //first draw the rectangular highlight below each dot
                g.setColor(UIManager.getColor("controlLtHighlight")); //NOI18N
                g.fillRect(x + 1, i + 1, 2, 2);
                //Get the shadow color.  We'll paint the darkest dot first,
                //and work our way to the lightest
                Color col = UIManager.getColor("controlShadow"); //NOI18N
                g.setColor(col);
                //draw the darkest dot
                g.drawLine(x+1, i+1, x+1, i+1);
                
                //Get the color components and calculate the amount each component
                //should increase per dot
                int red = col.getRed();
                int green = col.getGreen();
                int blue = col.getBlue();
                
                //Get the default component background - we start with the dark
                //color, and for each dot, add a percentage of the difference
                //between this and the background color
                Color back = getBackground();
                int rb = back.getRed();
                int gb = back.getGreen();
                int bb = back.getBlue();
                
                //Get the amount to increment each component for each dot
                int incr = (rb - red) / 5;
                int incg = (gb - green) / 5;
                int incb = (bb - blue) / 5;
                
                //Increment the colors
                red += incr;
                green += incg;
                blue += incb;
                //Create a slightly lighter color and draw the dot
                col = new Color(red, green, blue);
                g.setColor(col);
                g.drawLine(x+1, i, x+1, i);
                
                //And do it for the next dot, and so on, for all four dots
                red += incr;
                green += incg;
                blue += incb;
                col = new Color(red, green, blue);
                g.setColor(col);
                g.drawLine(x, i+1, x, i+1);
                
                red += incr;
                green += incg;
                blue += incb;
                col = new Color(red, green, blue);
                g.setColor(col);
                g.drawLine(x, i, x, i);
            }
        }
        
        /** @return minimum size */
        public Dimension getMinimumSize () {
            return dim;
        }
        
        /** @return preferred size */
        public Dimension getPreferredSize () {
            return this.getMinimumSize ();
        }
        
        public Dimension getMaximumSize () {
            return max;
        }
    }
    
  /*
    public static void main(String[] args) {
        JFrame jf = new JFrame();
        jf.getContentPane().add (new ToolbarXP());
        jf.setSize(new java.awt.Dimension(200,200));
        jf.setLocation(20,20);
        jf.show();
    }
   */

    
    /** Grip for floatable toolbar, used for Windows L&F */
    private final class ToolbarGrip extends JPanel {
        /** Horizontal gaps. */
        static final int HGAP = 1;
        /** Vertical gaps. */
        static final int VGAP = 1;
        /** Step between two grip elements. */
        static final int STEP = 1;
        /** Width of grip element. */
        static final int WIDTH = 2;

        /** Number of grip elements. */
        int columns;
        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;

        static final long serialVersionUID =-8819972936203315276L;

        /** Create new ToolbarGrip for default number of grip elements. */
        public ToolbarGrip () {
            this (1);
        }

        /** Create new ToolbarGrip for specific number of grip elements.
         * @param col number of grip elements
         */
        public ToolbarGrip (int col) {
            super ();
            columns = col;
            int width = (col - 1) * STEP + col * WIDTH + 2 * HGAP;
            dim = new Dimension (width, width);
            max = new Dimension (width, Integer.MAX_VALUE);
            this.setBorder (new EmptyBorder (VGAP, HGAP, VGAP, HGAP));
            this.setToolTipText (Toolbar.this.getName());
        }

        /** Paint grip to specific Graphics. */
        public void paint (Graphics g) {
            Dimension size = this.getSize();
            int top = VGAP;
            int bottom = size.height - 1 - VGAP;
            int height = bottom - top;
            g.setColor ( this.getBackground() );

            for (int i = 0, x = HGAP; i < columns; i++, x += WIDTH + STEP) {
                g.draw3DRect (x, top, WIDTH, height, true); // grip element is 3D rectangle now
            }

        }

        /** @return minimum size */
        public Dimension getMinimumSize () {
            return dim;
        }

        /** @return preferred size */
        public Dimension getPreferredSize () {
            return this.getMinimumSize();
        }
        
        public Dimension getMaximumSize () {
            return max;
        }
        
    } // end of inner class ToolbarGrip

    /** DnDListener is Drag and Drop listener for Toolbar motion events. */
    public interface DnDListener extends java.util.EventListener {
        /** Invoced when toolbar is dragged. */
        public void dragToolbar (DnDEvent e);

        /** Invoced when toolbar is dropped. */
        public void dropToolbar (DnDEvent e);
    } // end of interface DnDListener


    /** DnDEvent is Toolbar's drag and drop event. */
    public static class DnDEvent extends EventObject {
        /** Type of DnDEvent. Dragging with only one Toolbar. */
        public static final int DND_ONE  = 1;
        /** Type of DnDEvent. Only horizontal dragging with Toolbar and it's followers. */
        public static final int DND_END  = 2;
        /** Type of DnDEvent. Only vertical dragging with whole lines. */
        public static final int DND_LINE = 3;

        /** Name of toolbar where event occured. */
        private String name;
        /** distance of horizontal dragging */
        private int dx;
        /** distance of vertical dragging */
        private int dy;
        /** Type of event. */
        private int type;

        static final long serialVersionUID =4389530973297716699L;
        public DnDEvent (Toolbar toolbar, String name, int dx, int dy, int type) {
            super (toolbar);

            this.name = name;
            this.dx = dx;
            this.dy = dy;
            this.type = type;
        }

        /** @return name of toolbar where event occured. */
        public String getName () {
            return name;
        }

        /** @return distance of horizontal dragging */
        public int getDX () {
            return dx;
        }

        /** @return distance of vertical dragging */
        public int getDY () {
            return dy;
        }

        /** @return type of event. */
        public int getType () {
            return type;
        }
    } // end of class DnDEvent
} // end of class Toolbar
