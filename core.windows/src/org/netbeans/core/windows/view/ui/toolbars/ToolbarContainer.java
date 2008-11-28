/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.core.windows.view.ui.toolbars;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;
import org.openide.awt.Actions;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;
import org.openide.loaders.DataObject;

/**
 * A wrapper panel for a single Toolbar. Also contains 'dragger' according to
 * current l&f and is responsible for painting of button dnd feedback.
 *
 * @author S. Aubrecht
 */
final class ToolbarContainer extends JPanel {

    private static final Logger LOG = Logger.getLogger(Toolbar.class.getName());

    private final Toolbar toolbar;
    private JComponent dragger;
    private final DnDSupport dnd;
    private final boolean dragable;
    private DropTarget dropTarget;


    private int dropIndex = -1;
    private boolean dropBefore;

    public ToolbarContainer( Toolbar toolbar, final DnDSupport dnd, boolean dragable ) {
        super( new BorderLayout() );
        setOpaque(false);
        this.toolbar = toolbar;
        this.dnd = dnd;
        this.dragable = dragable;
        add( toolbar, BorderLayout.CENTER );
        toolbar.addContainerListener( new ContainerListener() {

            public void componentAdded(ContainerEvent e) {
                dnd.register(e.getChild());
            }

            public void componentRemoved(ContainerEvent e) {
                dnd.unregister(e.getChild());
            }
        });
//        add( new JLabel("test"), BorderLayout.EAST );
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if( null == dragger && isDragable() ) {
            dragger = createDragger();
            dragger.setToolTipText(Actions.cutAmpersand(toolbar.getDisplayName()));
            add( dragger, BorderLayout.WEST );
        }
        registerDnd();
        if( null == dropTarget ) {
            dropTarget = new DropTarget(toolbar, dnd);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension d = new Dimension(0,0);
        if( null != dragger ) {
            d.height = dragger.getMinimumSize().height;
            d.width = dragger.getMinimumSize().width;
        }

        d.height = Math.max(d.height, toolbar.getMinimumSize().height);
        if( toolbar.getComponentCount() == 0 )
            d.width += ToolbarPool.getDefault().getPreferredIconSize();
        else
            d.width += toolbar.getComponent(0).getMinimumSize().width;
        return d;
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        unregisterDnd();
        if( null != dropTarget ) {
            dropTarget.removeDropTargetListener(dnd);
            dropTarget = null;
        }
    }

    Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public String getName() {
        return toolbar.getName();
    }

    /**
     * Register toolbar content for drag and drop.
     */
    private void registerDnd() {
        for( Component c : toolbar.getComponents() ) {
            if( !(c instanceof JComponent) )
                continue;
            JComponent jc = (JComponent) c;
            Object o = jc.getClientProperty("file");
            //is the component to register a draggable toolbar button?
            if( !(o instanceof DataObject) )
                continue;
            dnd.register(c);
        }
        if( isDragable() && null != dragger )
            dnd.register(dragger);
    }

    /**
     * Unregister toolbar content for drag and drop to avoid memory leaks.
     */
    private void unregisterDnd() {
        for( Component c : toolbar.getComponents() ) {
            dnd.unregister(c);
        }
        if( null != dragger )
            dnd.unregister(dragger);
    }

    private boolean isDragable() {
        return dragable;
    }

    private JComponent createDragger() {
        /** Uses L&F's grip **/
        String lfID = UIManager.getLookAndFeel().getID();
        JPanel dragarea = null;
        // #98888: recognize JGoodies L&F properly
        if (lfID.endsWith("Windows")) { //NOI18N
            if (isXPTheme()) {
                dragarea = (JPanel) new ToolbarXP();
            } else {
                dragarea = (JPanel) new ToolbarGrip();
            }
        } else if (lfID.equals("Aqua")) { //NOI18N
            dragarea = (JPanel) new ToolbarAqua();
        } else if (lfID.equals("GTK")) { //NOI18N
            dragarea = (JPanel) new ToolbarGtk();
            //setFloatable(true);
        } else {
            //Default for Metal and uknown L&F
            dragarea = (JPanel)new ToolbarBump();
        }
        dragarea.setCursor( Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR) );
        return dragarea;
    }

    void setDropGesture(int dropIndex, boolean dropBefore) {
        this.dropIndex = dropIndex;
        this.dropBefore = dropBefore;
        repaint();
    }

    @Override
    public void paint( Graphics g ) {
        super.paint(g);
        //paint drop gesture
        if( dropIndex >= 0 ) {
            paintDropGesture(g);
        }
    }

    private void paintDropGesture( Graphics g ) {
        Component c = toolbar.getComponentAtIndex( dropIndex );
        if( null == c )
            return;

        Point location = c.getLocation();
        int cursorLocation = location.x;
        if( !dropBefore ) {
            cursorLocation += c.getWidth();
            if( dropIndex == toolbar.getComponentCount()-1 )
                cursorLocation -= 3;
        }
        if( null != dragger )
            cursorLocation += dragger.getWidth();
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

    // ------------------------------------------------------------------------
    // Toolbar draggers for various look and feels
    // ------------------------------------------------------------------------

    /** Bumps for floatable toolbar */
    private final class ToolbarBump extends JPanel {
        /** Top gap. */
        static final int TOPGAP = 2;
        /** Bottom gap. */
        static final int BOTGAP = 2;
        /** Width of bump element. */
        private static final int GRIP_WIDTH = 6;

        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;

        /** Create new ToolbarBump. */
        public ToolbarBump () {
            super();
            int width = GRIP_WIDTH;
            dim = new Dimension (width, width);
            max = new Dimension (width, Integer.MAX_VALUE);
        }

        /** Paint bumps to specific Graphics. */
        @Override
        public void paint (Graphics g) {
            Dimension size = this.getSize ();
            int height = size.height - BOTGAP;
            g.setColor (this.getBackground ());

            for (int x = 0; x+1 < size.width; x+=4) {
                for (int y = TOPGAP; y+1 < height; y+=4) {
                    g.setColor (this.getBackground ().brighter ());
                    g.drawLine (x, y, x, y);
                    if (x+5 < size.width && y+5 < height) {
                        g.drawLine (x+2, y+2, x+2, y+2);
                    }
                    g.setColor (this.getBackground ().darker ().darker ());
                    g.drawLine (x+1, y+1, x+1, y+1);
                    if (x+5 < size.width && y+5 < height) {
                        g.drawLine (x+3, y+3, x+3, y+3);
                    }
                }
            }
        }

        /** @return minimum size */
        @Override
        public Dimension getMinimumSize () {
            return dim;
        }

        /** @return preferred size */
        @Override
        public Dimension getPreferredSize () {
            return this.getMinimumSize ();
        }

        @Override
        public Dimension getMaximumSize () {
            return max;
        }
    } // end of inner class ToolbarBump


    private static Class synthIconClass = null;
    private static boolean testExecuted = false;
    /**
     * Test if SynthIcon is available and can be used for painting native Toolbar
     * D&D handle. If not use our own handle. Reflection is used here as it is Sun
     * proprietary API.
     */
    private static boolean useSynthIcon () {
        if (!testExecuted) {
            testExecuted = true;
            try {
                synthIconClass = Class.forName("sun.swing.plaf.synth.SynthIcon");
            } catch (ClassNotFoundException exc) {
                LOG.log(Level.INFO, null, exc);
            }
        }
        return (synthIconClass != null);
    }

    /** Bumps for floatable toolbar GTK L&F */
    private final class ToolbarGtk extends JPanel {
        /** Top gap. */
        int TOPGAP;
        /** Bottom gap. */
        int BOTGAP;
        /** Width of bump element. */
        private static final int GRIP_WIDTH = 6;

        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;

        /** Create new ToolbarBump. */
        public ToolbarGtk () {
            super();
            int width = GRIP_WIDTH;
            if (useSynthIcon()) {
                TOPGAP = 0;
                BOTGAP = 0;
            } else {
                TOPGAP = 2;
                BOTGAP = 2;
            }
            dim = new Dimension (width, width);
            max = new Dimension (width, Integer.MAX_VALUE);
        }

        /** Paint bumps to specific Graphics. */
        @Override
        public void paint (Graphics g) {
            if (useSynthIcon()) {
                int height = toolbar.getHeight() - BOTGAP;
                Icon icon = UIManager.getIcon("ToolBar.handleIcon");
                Region region = Region.TOOL_BAR;
                SynthStyleFactory sf = SynthLookAndFeel.getStyleFactory();
                SynthStyle style = sf.getStyle(toolbar, region);
                SynthContext context = new SynthContext(toolbar, region, style, SynthConstants.DEFAULT);

                // for vertical toolbar, you'll need to ask for getIconHeight() instead
                Method m = null;
                try {
                    m = synthIconClass.getMethod("getIconWidth",Icon.class, SynthContext.class);
                } catch (NoSuchMethodException exc) {
                    LOG.log(Level.WARNING, null, exc);
                }
                int width = 0;
                //width = SynthIcon.getIconWidth(icon, context);
                try {
                    width = (Integer) m.invoke(null, new Object [] {icon, context});
                } catch (IllegalAccessException exc) {
                    LOG.log(Level.WARNING, null, exc);
                } catch (InvocationTargetException exc) {
                    LOG.log(Level.WARNING, null, exc);
                }
                try {
                    m = synthIconClass.getMethod("paintIcon",Icon.class,SynthContext.class,
                    Graphics.class,Integer.TYPE,Integer.TYPE,Integer.TYPE,Integer.TYPE);
                } catch (NoSuchMethodException exc) {
                    LOG.log(Level.WARNING, null, exc);
                }
                //SynthIcon.paintIcon(icon, context, g, 0, 0, width, height);
                try {
                    m.invoke(null, new Object [] {icon,context,g,new Integer(0),new Integer(-1),
                    new Integer(width),new Integer(height)});
                } catch (IllegalAccessException exc) {
                    LOG.log(Level.WARNING, null, exc);
                } catch (InvocationTargetException exc) {
                    LOG.log(Level.WARNING, null, exc);
                }
            } else {
                Dimension size = this.getSize();
                int height = size.height - BOTGAP;
                g.setColor (this.getBackground ());

                for (int x = 0; x+1 < size.width; x+=4) {
                    for (int y = TOPGAP; y+1 < height; y+=4) {
                        g.setColor (this.getBackground ().brighter ());
                        g.drawLine (x, y, x, y);
                        if (x+5 < size.width && y+5 < height) {
                            g.drawLine (x+2, y+2, x+2, y+2);
                        }
                        g.setColor (this.getBackground ().darker ().darker ());
                        g.drawLine (x+1, y+1, x+1, y+1);
                        if (x+5 < size.width && y+5 < height) {
                            g.drawLine (x+3, y+3, x+3, y+3);
                        }
                    }
                }
            }
        }

        /** @return minimum size */
        @Override
        public Dimension getMinimumSize () {
            return dim;
        }

        /** @return preferred size */
        @Override
        public Dimension getPreferredSize () {
            return new Dimension(GRIP_WIDTH,toolbar.getHeight() - BOTGAP - TOPGAP);
        }

        @Override
        public Dimension getMaximumSize () {
            return max;
        }
    } // end of inner class ToolbarGtk

    /**
     * Recognizes if XP theme is set.
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
        private static final int GRIP_WIDTH = 8;
        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;

        public ToolbarAqua() {
            dim = new Dimension (GRIP_WIDTH, GRIP_WIDTH);
            max = new Dimension (GRIP_WIDTH, Integer.MAX_VALUE);
        }

        @Override
        public void paintComponent (Graphics g) {
            super.paintComponent(g);
            java.awt.Graphics2D g2d = (Graphics2D) g;
            g2d.addRenderingHints(getHints());

            int sz = 5;

            int y = ((getHeight() / 2) - (sz / 2)) - 2;
            int x = ((getWidth() / 2) - (sz / 2)) - 2;

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
        @Override
        public Dimension getMinimumSize () {
            return dim;
        }

        /** @return preferred size */
        @Override
        public Dimension getPreferredSize () {
            return this.getMinimumSize ();
        }

        @Override
        public Dimension getMaximumSize () {
            return max;
        }
    }

    private static java.util.Map<RenderingHints.Key, Object> hintsMap = null;
    @SuppressWarnings("unchecked")
    static final Map getHints() {
        //XXX We REALLY need to put this in a graphics utils lib
        if (hintsMap == null) {
            //Thanks to Phil Race for making this possible
            hintsMap = (Map<RenderingHints.Key, Object>)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
            if (hintsMap == null) {
                hintsMap = new HashMap<RenderingHints.Key, Object>();
                hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
        }
        return hintsMap;
    }

    private final class ToolbarXP extends JPanel {
        /** Width of grip */
        private static final int GRIP_WIDTH = 7;
        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;

        public ToolbarXP() {
            dim = new Dimension (GRIP_WIDTH, GRIP_WIDTH);
            max = new Dimension (GRIP_WIDTH, Integer.MAX_VALUE);
        }

        @Override
        public void paintComponent (Graphics g) {
            super.paintComponent(g);
            int x = 3;
            for (int i=4; i < getHeight()-4; i+=4) {
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
        @Override
        public Dimension getMinimumSize() {
            return dim;
        }

        /** @return preferred size */
        @Override
        public Dimension getPreferredSize () {
            return this.getMinimumSize ();
        }

        @Override
        public Dimension getMaximumSize () {
            return max;
        }
    }

    /** Grip for floatable toolbar, used for Windows Classic L&F */
    private final class ToolbarGrip extends JPanel {
        /** Horizontal gaps. */
        static final int HGAP = 1;
        /** Vertical gaps. */
        static final int VGAP = 2;
        /** Step between two grip elements. */
        static final int STEP = 1;
        /** Width of grip element. */
        private static final int GRIP_WIDTH = 2;

        /** Number of grip elements. */
        int columns;
        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;

        /** Create new ToolbarGrip for default number of grip elements. */
        public ToolbarGrip () {
            this(1);
        }

        /** Create new ToolbarGrip for specific number of grip elements.
         * @param col number of grip elements
         */
        public ToolbarGrip (int col) {
            super ();
            columns = col;
            int width = (col - 1) * STEP + col * GRIP_WIDTH + 2 * HGAP;
            dim = new Dimension (width, width);
            max = new Dimension (width, Integer.MAX_VALUE);
            this.setBorder (new EmptyBorder (VGAP, HGAP, VGAP, HGAP));
        }

        /** Paint grip to specific Graphics. */
        @Override
        public void paint (Graphics g) {
            Dimension size = this.getSize();
            int top = VGAP;
            int bottom = size.height - 1 - VGAP;
            int height = bottom - top;
            g.setColor ( this.getBackground() );

            for (int i = 0, x = HGAP; i < columns; i++, x += GRIP_WIDTH + STEP) {
                g.draw3DRect (x, top, GRIP_WIDTH, height, true); // grip element is 3D rectangle now
            }

        }

        /** @return minimum size */
        @Override
        public Dimension getMinimumSize () {
            return dim;
        }

        /** @return preferred size */
        @Override
        public Dimension getPreferredSize () {
            return this.getMinimumSize();
        }

        @Override
        public Dimension getMaximumSize () {
            return max;
        }

    } // end of inner class ToolbarGrip
}
