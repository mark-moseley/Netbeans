/*
 * ImageCacheTest.java
 *
 * Created on February 17, 2004, 12:25 AM
 */

package org.netbeans.imagecache;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author  tim
 */
public class ImageCacheTest extends javax.swing.JFrame {
    private CacheReader reader=null;
    private CacheWriter writer = new CacheWriter();
    private HashSet imgs = new HashSet();
    private String CACHE = "/tmp/cachetest/";
    /** Creates new form ImageCacheTest */
    public ImageCacheTest() {
        initComponents();
        jSplitPane1.setRightComponent(jTabbedPane2);
        jSplitPane1.setDividerLocation(0.5d);
        setBounds (0,0, 600,600);
    }
    
    private CacheReader getReader() {
        if (reader != null) {
            return reader;
        }
        try {
            reader = new CacheReader (new File(CACHE));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }
        return reader;
    }
    
    private void recreateCacheDir() throws IOException {
        File f = new File(CACHE);
        if (f.exists()) {
            File[] files = f.listFiles();
            for (int i=0; i < files.length; i++) {
                files[i].delete();
            }
            f.delete();
        }
        f.mkdir();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jSplitPane1.setLeftComponent(jTabbedPane1);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jButton2.setText("Build and load cache");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        getContentPane().add(jButton2, java.awt.BorderLayout.SOUTH);

        jButton1.setText("Load image");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel1.add(jButton1);

        jButton3.setText("Load cache dir");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jPanel1.add(jButton3);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        pack();
    }//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        JFileChooser jfc = new JFileChooser ();
        jfc.setFileSelectionMode(jfc.DIRECTORIES_ONLY);
        jfc.showDialog(this, "Choose a directory with cache files");
       

        File fi = jfc.getSelectedFile();
        
        try {
            if (fi==null || (!fi.exists() && !fi.isDirectory())) {
                System.err.println("I can't do that: " + fi);
                return;
            }
            CacheReader r = new CacheReader (fi);
            String[] s = r.getIDs();
            for (int i=0; i < s.length; i++) {
                File f = new File(s[i]);
                BufferedImage img = (BufferedImage) r.find(s[i]);
                ImagePanel ip = new ImagePanel(img);
                ip.setName ("c-" + s[i]);
                jTabbedPane2.insertTab(f.getName(), null, new JScrollPane(ip), f.toString(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        try {
            jTabbedPane2.removeAll();
            recreateCacheDir();

            writer.setDir(CACHE, true);
            String[] s = new String[imgs.size()];
            s = (String[]) imgs.toArray(s);
            for (int i=0; i < s.length; i++) {
                writer.write(s[i], true);
            }
            
            CacheReader r = getReader();
            for (int i=0; i < s.length; i++) {
                File f = new File(s[i]);
                BufferedImage img = (BufferedImage) r.find(s[i]);
                ImagePanel ip = new ImagePanel(img);
                ip.setName ("c-" + s[i]);
                jTabbedPane2.insertTab(f.getName(), null, new JScrollPane(ip), f.toString(), 0);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private FileDialog fd = new FileDialog(this, "Add image");
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        
        fd.show(true);
        if (fd.getDirectory() != null && fd.getFile() != null) {
            String filename = fd.getDirectory() + fd.getFile();
            try {
                File f = new File(filename);
                BufferedImage img = ImageIO.read(f);
                imgs.add (filename);
                ImagePanel ip = new ImagePanel(img);
                ip.setName (filename);
                System.err.println("Created " + ip);
                jTabbedPane1.insertTab(f.getName(), null, new JScrollPane(ip), filename, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new ImageCacheTest().show();
    }
    
    private class ImagePanel extends JPanel {
        private BufferedImage img;
        public ImagePanel (BufferedImage img) {
            this.img = img;
        }
        
        public Dimension getPreferredSize() {
            if (img != null) {
                return new Dimension (img.getWidth(), img.getHeight());
            } else {
                return super.getPreferredSize();
            }
        }
        
        public void paintComponent(Graphics g) {
            long time = perf.highResCounter();
            ((Graphics2D) g).drawRenderedImage(img, AffineTransform.getTranslateInstance(0,0));
            long dur = perf.highResCounter() - time;
            System.err.println(getName() + " " + dur);
        }
    }
    private static final sun.misc.Perf perf = sun.misc.Perf.getPerf();

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    // End of variables declaration//GEN-END:variables
    
}
