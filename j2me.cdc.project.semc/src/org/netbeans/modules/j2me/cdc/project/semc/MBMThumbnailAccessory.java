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

package org.netbeans.modules.j2me.cdc.project.semc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.openide.util.NbBundle;

/**
 *
 * @author  Manowar, suchys
 */
public class MBMThumbnailAccessory extends JPanel implements PropertyChangeListener {
    
    private static final int ICON_MAX_WIDTH = 64;
    
    private Icon[] thumbnails;
    private JLabel[] labels;
    private File file;
    private String tmpDir;
    private JFileChooser fc;
    private String bmconvLocation;

    private int iconCount;
    
    public MBMThumbnailAccessory(JFileChooser fc, File sdkLocation) {
        this.fc = fc;
        
        initComponents();
        
        labels = new JLabel[6];
        labels[0] = label1;
        labels[1] = label2;
        labels[2] = label3;
        labels[3] = label4;
        labels[4] = label5;
        labels[5] = label6;
        int width = 5;//TODO SwingUtilities.computeStringWidth(Toolkit.getDefaultToolkit().getFontMetrics(UIManager.getFont(label1.getFont())), "2");
        Dimension d = new Dimension(64 + width, 64);
        for (int i = 0; i < labels.length; i++){
            labels[i].setPreferredSize(d);
        }
        tmpDir = System.getProperty("java.io.tmpdir"); //NOI18N
        
        iconNumLabel.setText(NbBundle.getMessage(MBMThumbnailAccessory.class, "LBL_ThumbnailsInfo", "0")); //NOI18N
        
        StringBuffer str = new StringBuffer();
        if (sdkLocation != null && sdkLocation.exists()) {
            str.append(sdkLocation.getAbsolutePath());
            str.append(File.separatorChar);
            str.append("epoc32"); //NOI18N
            str.append(File.separatorChar);
            str.append("tools"); //NOI18N
            str.append(File.separatorChar);
        }
        str.append("bmconv"); //NOI18N
        bmconvLocation = str.toString();
    }
    
    
    private void loadImages() {
        if (file == null || thumbnails == null || !file.getPath().toLowerCase().endsWith(".mbm")) { //NOI18N
            thumbnails = null;
            return;
        }
        
        boolean error = false;
        try {
            List thumbs = new ArrayList();
            thumbs.add(bmconvLocation);
            thumbs.add("/u"); //NOI18N
            thumbs.add(file.getPath());
            for (int i = 0; i < thumbnails.length; i++) {
                StringBuffer sb = new StringBuffer();
                sb.append(tmpDir);
                sb.append(File.separatorChar);
                sb.append(i);
                sb.append(".bmp"); //NOI18N
                thumbs.add(sb.toString());
            }
            Process p = Runtime.getRuntime().exec((String[]) thumbs.toArray(new String[0])); //NOI18N
            StringBuffer sout = new StringBuffer();
            StringBuffer serr = new StringBuffer();
            IOThread out = new IOThread(p.getInputStream(), sout);
            out.join();
            IOThread err = new IOThread(p.getInputStream(), serr);
            err.join();
            if (p.waitFor() == 0) {
                ImageIcon tmpIcon = null;
                for (int i = 0; i < thumbnails.length; i++) {
                    File bmpFile = new File(tmpDir + File.separatorChar + i + ".bmp"); //NOI18N
                    bmpFile.deleteOnExit();

                    Iterator itImageReaders = ImageIO.getImageReadersByFormatName("bmp"); //NOI18N
                    ImageReader reader = null;
                    if (itImageReaders.hasNext()){
                        reader = (ImageReader) itImageReaders.next();                                  
                        ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(bmpFile)));
                        reader.setInput(iis);
                        BufferedImage bi = reader.read(0);
                        Iterator itImageWriters = ImageIO.getImageWritersByFormatName("png"); //NOI18N
                        ImageWriter writer = null;
                        if (itImageWriters.hasNext())
                            writer = (ImageWriter) itImageWriters.next();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        writer.setOutput(ImageIO.createImageOutputStream(baos));
                        writer.write(bi);
                        reader.dispose();
                        writer.dispose();
                        iis.close();
                        
                        tmpIcon = new ImageIcon(baos.toByteArray());
                        
                        if (tmpIcon != null) {
                            if (tmpIcon.getIconWidth() > ICON_MAX_WIDTH) {
                                thumbnails[i] = new ImageIcon(tmpIcon.getImage().getScaledInstance(ICON_MAX_WIDTH, -1, Image.SCALE_DEFAULT));
                            } else {
                                thumbnails[i] = tmpIcon;
                            }
                            labels[i].setText(String.valueOf(i+1));
                            labels[i].setIcon(thumbnails[i]);
                        }                        
                    } else {
                        iconNumLabel.setText(NbBundle.getMessage(MBMThumbnailAccessory.class, "ERR_NotAvailable14")); //NOI18N                        
                    }
                }
            } else {
                error = true;
            }
        } catch (IOException ex) {
            error = true;;
        } catch (InterruptedException ex) {
            error = true;
        } finally {
            if (error){
                Color nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
                if (nbErrorForeground == null) {
                    nbErrorForeground = new Color(255, 0, 0); 
                }        
                textArea.setForeground(nbErrorForeground);                
                textArea.setText(NbBundle.getMessage(MBMThumbnailAccessory.class, "ERR_BmpconvNotExists")); //NOI18N
                textArea.setCaretPosition(0);
            }
        }
    }
    
    private void getImageInfo() {
        if (file == null || !file.getPath().toLowerCase().endsWith(".mbm")) { //NOI18N
            thumbnails = null;
            return;
        }
        boolean error = false;
        try {
            Process p = Runtime.getRuntime().exec(new String[] {bmconvLocation, "/v", file.getPath()}); //NOI18N
            StringBuffer sout = new StringBuffer();
            StringBuffer serr = new StringBuffer();
            IOThread out = new IOThread(p.getInputStream(), sout);
            out.join();
            IOThread err = new IOThread(p.getInputStream(), serr);
            err.join();
            p.waitFor();
            int exitValue = p.exitValue();
            if (exitValue == 0) {
                String text = sout.toString();
                int i = text.indexOf("Bitmap"); //NOI18N
                if (i > 0) {
                    Color foreground = UIManager.getColor("TextArea.foreground"); //NOI18N
                    if (foreground == null) {
                        foreground = Color.BLACK; 
                    }        
                    textArea.setForeground(foreground);
                    textArea.setText(text.substring(i).trim());
                    textArea.setCaretPosition(0);
                }
                
                StringTokenizer st = new StringTokenizer(sout.toString(), "\n", false); //NOI18N
                int c = 0;
                while(st.hasMoreTokens()){
                    String s = st.nextToken();
                    if (s.startsWith("Bitmap")){ //NOI18N
                        c++;
                    }
                }
                iconNumLabel.setText(NbBundle.getMessage(MBMThumbnailAccessory.class, "LBL_ThumbnailsInfo", String.valueOf(c)));
                iconCount = c;
                if (c > 0) {
                    if (c > 6) {
                        c = 6;
                    }
                    thumbnails = new Icon[c];
                }
            } else {
            }
        } catch (IOException ex) {
            error = true;;
        } catch (InterruptedException ex) {
            error = true;
        } finally {
            if (error){
                Color nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
                if (nbErrorForeground == null) {
                    nbErrorForeground = new Color(255, 0, 0); 
                }        
                textArea.setForeground(nbErrorForeground);                
                textArea.setText(NbBundle.getMessage(MBMThumbnailAccessory.class, "ERR_BmpconvNotExists")); //NOI18N
                textArea.setCaretPosition(0);
            }
        }
    }
    
    public void propertyChange(PropertyChangeEvent e) {
        boolean update = false;
        String prop = e.getPropertyName();
        
        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
            file = null;
            update = true;
        } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
            file = (File) e.getNewValue();
            update = true;
        }
        
        if (update) {
            thumbnails = null;
            clean();
            if (isShowing()) {
                getImageInfo();
                loadImages();
            }
        }
    }
    
    private void clean() {
        iconCount = 0;
        for (int i = 0; i < labels.length; i++) {
            labels[i].setText(" "); //NOI18N
            labels[i].setIcon(null);
        }
        
        iconNumLabel.setText(NbBundle.getMessage(MBMThumbnailAccessory.class, "LBL_ThumbnailsInfo", "0")); //NOI18N
        textArea.setText(""); //NOI18N
    }

    /**
     * Notifies this component that it no longer has a parent component.
     * When this method is invoked, any <code>KeyboardAction</code>s
     * set up in the the chain of parent components are removed.
     * 
     * 
     * @see #registerKeyboardAction
     */
    public void removeNotify() {
        fc.removePropertyChangeListener(this);
        super.removeNotify();
    }

    /**
     * Notifies this component that it now has a parent component.
     * When this method is invoked, the chain of parent components is
     * set up with <code>KeyboardAction</code> event listeners.
     * 
     * 
     * @see #registerKeyboardAction
     */
    public void addNotify() {
        super.addNotify();
        fc.addPropertyChangeListener(this);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                thumbnails = null;
                file = fc.getSelectedFile();
                clean();
                getImageInfo();
                loadImages();
            }
        });
    }
    
    
    public int getIconCount(){
        return iconCount;
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        label1 = new javax.swing.JLabel();
        label2 = new javax.swing.JLabel();
        label3 = new javax.swing.JLabel();
        label4 = new javax.swing.JLabel();
        label5 = new javax.swing.JLabel();
        label6 = new javax.swing.JLabel();
        iconNumLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(400, 250));

        textArea.setColumns(20);
        textArea.setEditable(false);
        textArea.setRows(5);
        jScrollPane1.setViewportView(textArea);

        label1.setText(" ");

        label2.setText(" ");

        label3.setText(" ");

        label4.setText(" ");

        label5.setText(" ");

        label6.setText(" ");

        iconNumLabel.setText(" ");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(MBMThumbnailAccessory.class, "LBL_MBMDetails")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .addContainerGap(332, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(label1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(label2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(label3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(label4)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(label5)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(label6))
                            .add(iconNumLabel))
                        .add(344, 344, 344))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(iconNumLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(label1)
                    .add(label2)
                    .add(label3)
                    .add(label4)
                    .add(label5)
                    .add(label6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel iconNumLabel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel label1;
    private javax.swing.JLabel label2;
    private javax.swing.JLabel label3;
    private javax.swing.JLabel label4;
    private javax.swing.JLabel label5;
    private javax.swing.JLabel label6;
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables

    static class IOThread extends Thread {
        private BufferedInputStream bis;
        private StringBuffer sb;
        
        IOThread(InputStream is, StringBuffer sb){
            bis = new BufferedInputStream(is, 2048);
            this.sb = sb;
            start();
        }
        
        public void run(){
            try{
                int i;
                while((i = bis.read()) != 1){
                    if (i == -1) break;
                    if (sb != null){
                        sb.append((char)i);
                    }
                }
            } catch (IOException ioEx){
                ///ioEx.printStackTrace();
            }
            try {
                bis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }    
}
