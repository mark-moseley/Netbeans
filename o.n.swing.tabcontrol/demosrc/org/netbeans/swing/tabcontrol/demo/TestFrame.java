/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * TestFrame.java
 *
 * Created on May 28, 2003, 2:50 AM
 */

package org.netbeans.swing.tabcontrol.demo;

import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.DefaultTabDataModel;
import org.netbeans.swing.tabcontrol.TabbedContainer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

/**
 * A frame to test the tab component
 *
 * @author Tim Boudreau
 */
public class TestFrame extends javax.swing.JFrame {


    /**
     * Creates new form TestFrame
     */
    public TestFrame() {
        setDefaultCloseOperation (javax.swing.WindowConstants.EXIT_ON_CLOSE);
        initComponents();
        /*
        try {
            
            System.setProperty("os.version", new Float(3.5).toString());
            Method m = Toolkit.class.getDeclaredMethod("setDesktopProperty",
            new Class[] {String.class, Object.class});
            m.setAccessible(true);
            m.invoke(java.awt.Toolkit.getDefaultToolkit(),
            new Object[]  {"win.xpstyle.themeActive", Boolean.FALSE });
 
            
            UIManager.setLookAndFeel(new PseudoWin());
//            UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
         */
        
        try {
            //UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        } catch (Exception e) {
        }

        JLabel jb1 = new JLabel("Label 1");
        JButton jb2 = new JButton("Button 2");
        JButton jb3 = new JButton("Click me to remove this tab");
        JLabel jb4 = new JLabel("Label 4");
        JLabel jb5 = new JLabel("Label 5");
        JButton jb6 = new JButton(
                "Click me to programmatically set the selected index to 3 atomically change some tab text and icons");
        JTree jtr = new JTree();

        jb1.setBackground(Color.ORANGE);
        jb2.setBackground(Color.YELLOW);
        jb3.setBackground(Color.CYAN);
        jb4.setBackground(Color.GREEN);
        jb2.setOpaque(true);
        jb3.setOpaque(true);
        jb4.setOpaque(true);
        jb1.setOpaque(true);

        JTextArea JTA = new JTextArea (TEXT);
        JTA.setWrapStyleWord(true);
        JTA.setColumns(80);
        JTA.setLineWrap(true);
        

        TabData tab0 = new TabData(jtr, myIcon, "0 JTree", "0");
        TabData tab1 = new TabData(jb1, myIcon, "1 Tab 1", "1");
        TabData tab2 = new TabData(jb2, myIcon2, "2 Different icon", "2");
        TabData tab3 = new TabData(jb3, myIcon, "3 Tab 3", "3");
        TabData tab4 = new TabData(jb4, myIcon,
                                   "<html>A <b>tab</b> with <font color='#3333DD'><i><u>html</u></i></font> <s>text</s> and stuff</html>",
                                   null);
//        TabData tab4 = new TabData(jb4, myIcon, "4 A bunch of stuff", "4");
        TabData tab5 = new TabData(jb5, myIcon, "5 Tab 5", "5");
        TabData tab6 = new TabData(jb6, myIcon2, "6 Click me!", "6");

//        TabData tab7 = new TabData(new JButton("foo"), myIcon, "A tab which has, shall we say, an extremely long name, one so long that it might cause some problems should the layout model not do all of its mathematics perfectly, I daresay", null);
        TabData tab7 = new TabData(JTA, null, "7 some text",
                                   "7");
        TabData tab8 = new TabData(new JLabel("gioo"), myIcon,
                                   "8 something", "8");
        TabData tab9 = new TabData(new JButton("foo"), myIcon, "9 foob",
                                   "9");
        TabData tab10 = new TabData(new JLabel("gioo"), myIcon, "10 wiggle",
                                    "10");
        TabData tab11 = new TabData(new JButton("foo"), myIcon, "11 bumble",
                                    "11");
        TabData tab12 = new TabData(new JLabel("mooble"), myIcon,
                                    "12 poodle", "12");
        TabData tab13 = new TabData(new JButton("fooble"), myIcon,
                                    "13 hoover", "13");
        TabData tab14 = new TabData(new JLabel("gooble"), myIcon, "14 snip",
                                    "14");

        TabDataModel mdl = new DefaultTabDataModel(new TabData[]{
            tab0, tab1, tab2, tab3, tab4, tab5, tab6, tab7, tab8, tab9, tab10,
            tab11, tab12, tab13, tab14});
 
        /*
        TabDataModel mdl = new DefaultTabDataModel(
            new TabData[] { tab0, tab1, tab2, tab3, tab4, tab5}
        );
         */
        
        final TabbedContainer tab = new TabbedContainer(mdl, TabbedContainer.TYPE_EDITOR);
        tab.setActive(true);    
        
        jb6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                tab.getSelectionModel().setSelectedIndex(3);
                int[] indices = new int[]{0, 3, 5};
                String[] newText = new String[]{
                    "I am still a JTree",
                    "<html>Changed <b><font color='#FFFF00'>3</font></b></html>",
                    "<html><s>changed</s></html> 5"};
                Icon[] icons = new Icon[]{myIcon3, myIcon3, myIcon3};
                tab.getModel().setIconsAndText(indices, newText, icons);
            }
        });

        jb3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                tab.getModel().removeTab(3);
            }
        });

        tab.setActive(true);
        
//        jb2.addMouseListener (new MouseAdapter() {
//            public void mouseClicked(MouseEvent e) {
//                Point point = e.getPoint();
//                System.out.println("Constraint for location is: " + tab.getConstraintForLocation(point, false));
//                System.out.println("Tab index is: " + tab.tabForCoordinate(point.x, point.y));
//                System.out.println(tab.getIndicationForLocation(point, false, new Point(0, 0), false));
//            }
//        }); // PENDING
//        tab.setActive(true);
        getContentPane().add(tab);
        setSize(700, 300);
        setLocation(12, 12);
    }


    public class PseudoWin
            extends com.sun.java.swing.plaf.windows.WindowsLookAndFeel {

        public boolean isSupportedLookAndFeel() {
            return true;
        }
    }

    TabDataModel mdl = null;
    Icon myIcon = new Icon() {
        public int getIconWidth() {
            return 16;
        }

        public int getIconHeight() {
            return 14;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor (Color.BLUE);
            g.fillRect (x, y, getIconWidth()-4, getIconHeight());
            
            g.setColor (Color.YELLOW);
            g.drawLine(x+2, y+2, x+3, y+3);
            g.drawLine(x+2, y+3, x+3, y+4);

            g.drawLine (x+8, y+2, x+7, y+3);
            g.drawLine (x+8, y+3, x+7, y+4);
            
            g.drawLine (x+4, y+6, x+6, y+6);
            
            g.drawRoundRect(x+3, y+9, 5, 3, 8, 5);
        }
    };

    Icon myIcon2 = new Icon() {
        public int getIconWidth() {
            return 19;
        }

        public int getIconHeight() {
            return 14;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.ORANGE);
            g.fillRect(x, y, getIconWidth() - 4, getIconHeight());
            g.setColor(Color.RED);
            g.drawRect(x, y, getIconWidth() - 4, getIconHeight());


            g.setColor(Color.BLUE);
            g.drawLine(x + 2, y + 2, x + 3, y + 3);
            g.drawLine(x + 2, y + 3, x + 3, y + 4);

            g.drawLine(x + 7, y + 2, x + 6, y + 3);
            g.drawLine(x + 7, y + 3, x + 6, y + 4);

            g.drawLine(x + 5, y + 6, x + 6, y + 6);

            g.drawRoundRect(x + 4, y + 9, 3, 3, 8, 5);
        }
    };

    Icon myIcon3 = new Icon() {
        public int getIconWidth() {
            return 19;
        }

        public int getIconHeight() {
            return 14;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.CYAN);
            g.fillRect(x, y, getIconWidth() - 4, getIconHeight());
            g.setColor(Color.WHITE);
            g.drawRect(x, y, getIconWidth() - 4, getIconHeight());


            g.setColor(Color.BLACK);
            g.drawLine(x + 2, y + 2, x + 3, y + 3);
            g.drawLine(x + 2, y + 3, x + 3, y + 4);

            g.drawLine(x + 7, y + 2, x + 6, y + 3);
            g.drawLine(x + 7, y + 3, x + 6, y + 4);

            g.drawLine(x + 5, y + 6, x + 6, y + 6);

            g.drawRoundRect(x + 4, y + 9, 3, 3, 8, 5);
        }
    };

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        pack();
    }//GEN-END:initComponents

    /**
     * Exit the Application
     */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        parseArgs (args);
//        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
//        System.setProperty("apple.awt.brushMetalLook", "true");

        //     RepaintManager nue = new MyRepaintManager(RepaintManager.currentManager(new javax.swing.JFrame()));
        //     RepaintManager.setCurrentManager(nue);
//        System.setProperty ("nb.forceUI", "WindowsXPLFCustoms");
        try {
//            UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        } catch (Exception e) {
        }        
       
     //   System.setProperty ("nb.tabcontrol.fx.gratuitous","true");
        
        try {
            Class c = Class.forName("org.netbeans.swing.plaf.Startup");
            Method m = c.getDeclaredMethod("run", new Class[] {Class.class, Integer.TYPE, java.net.URL.class});
            System.err.println("Installing customs");
            m.invoke(null, new Object[] {null, new Integer(0), null});
        } catch (Exception e) {
            System.err.println("Could not find plaf library");
        }
        
        new TestFrame().show();
    }

    private static void parseArgs (String[] s) {
        try {
        for (int i=0; i < s.length; i++) {
            System.err.println ("Arg: " + s[i]);
            if (s[i].indexOf ('=') != -1) {
                StringTokenizer st = new StringTokenizer (s[i], "=");
                String key = st.nextToken();
                String val = st.nextToken();
                System.setProperty (key, val);
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static class MyRepaintManager extends RepaintManager {
        private RepaintManager r;

        public MyRepaintManager(RepaintManager r) {
            this.r = r;
        }

        public synchronized void addInvalidComponent(
                JComponent invalidComponent) {
            System.err.println("AddInvalidComponent " + invalidComponent);
            super.addInvalidComponent(invalidComponent);
        }

        public synchronized void addDirtyRegion(JComponent c, int x, int y,
                                                int w, int h) {
            System.err.println("addDirtyRegion " + x + "," + y + "," + w + ","
                               + h
                               + " c=" + c);
            super.addDirtyRegion(c, x, y, w, h);
//            Thread.dumpStack();
        }

        public void markCompletelyDirty(JComponent aComponent) {
            System.err.println("MarkCompletelyDirty " + aComponent);
            super.markCompletelyDirty(aComponent);
        }

        public void markCompletelyClean(JComponent aComponent) {
            super.markCompletelyClean(aComponent);
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    //Credit goes to Rick Ross of Javalobby for this - I needed something
    //to cut and paste
    private static final String TEXT = "Tools for Java developers are like " +
    "restaurants in New  York City. Even if you visited a different restaurant " +
    "in NYC for every  meal, every day, you would never get to all the eateries " +
    "in the Big Apple.  Likewise with Java tools, you could probably try a new " +
    "Java development  tool every single day and never get through all of them. " +
    "There is simply  an amazing adundance of Java tools available today, and " +
    "a great many  of them are absolutely free. Why do we still hear industry " +
    "pundits complaining that  Java lacks tools? I don't know what the heck they " +
    "are talking about?  This seems to be one of those criticisms that lives long " +
    "past the time  when it is no longer valid. A few years ago it may have been " +
    "true that  there weren't as many Java tools available as most developers " +
    "would have  liked, but today there are so many that no-one among us could " +
    "possibly  hope to keep up with the flow. Here's a sample list from A to Z, " +
    "and none of these will cost you a penny.";
    
}
