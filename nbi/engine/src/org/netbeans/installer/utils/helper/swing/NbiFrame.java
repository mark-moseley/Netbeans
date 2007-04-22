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
 *
 * $Id$
 */
package org.netbeans.installer.utils.helper.swing;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.helper.ExtendedUri;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiFrame extends JFrame {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private int frameWidth;
    private int frameHeight;
    private File frameIcon;
    
    private NbiFrameContentPane contentPane;
    
    public NbiFrame() {
        super();
        
        frameWidth = DEFAULT_FRAME_WIDTH;
        frameHeight = DEFAULT_FRAME_HEIGHT;
        
        try {
            frameIcon = FileProxy.getInstance().getFile(DEFAULT_FRAME_ICON_URI);
        } catch (DownloadException e) {
            ErrorManager.notifyWarning("Cannot download frame icon", e);
        }
        
        initComponents();
    }
    
    public void setVisible(boolean visible) {
        final GraphicsDevice screen = GraphicsEnvironment.
                getLocalGraphicsEnvironment().
                getScreenDevices()[0];
        final GraphicsConfiguration config = screen.getDefaultConfiguration();
        
        final int screenWidth  = config.getBounds().width;
        final int screenHeight = config.getBounds().height;
        
        setLocation(
                (screenWidth - getSize().width) / 2,
                (screenHeight - getSize().height) / 2);
        
        super.setVisible(visible);
    }
    
    public Image getBackgroundImage() {
        return contentPane.getBackgroundImage();
    }
    
    public void setBackgroundImage(URL url) {
        contentPane.setBackgroundImage(url);
    }
    
    private void initComponents() {
        // the frame itself
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        setSize(frameWidth, frameHeight);
        
        try {
            setIconImage(new ImageIcon(frameIcon.toURI().toURL()).getImage());
        } catch (MalformedURLException e) {
            ErrorManager.notifyWarning("Cannot load frame icon", e);
        }
        
        // content pane
        contentPane = new NbiFrameContentPane();
        setContentPane(contentPane);
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class NbiFrameContentPane extends NbiPanel {
        private Image backgroundImage;
        
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            
            if (backgroundImage != null) {
                graphics.drawImage(backgroundImage, 0, 0, this);
            }
        }
        
        public Image getBackgroundImage() {
            return backgroundImage;
        }
        
        public void setBackgroundImage(URL url) {
            if (url != null) {
                backgroundImage = new ImageIcon(url).getImage();
            } else {
                backgroundImage = null;
            }
        }
        
        public void setBackgroundImage(Image image) {
            if (image != null) {
                backgroundImage = image;
            } else {
                backgroundImage = null;
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final int DEFAULT_FRAME_WIDTH  = 
            650;
    public static final int DEFAULT_FRAME_HEIGHT = 
            600;
    public static final String DEFAULT_FRAME_ICON_URI = 
            ExtendedUri.RESOURCE_SCHEME + 
            ":org/netbeans/installer/utils/helper/swing/frame-icon.png";
    
    public static final String FRAME_WIDTH_PROPERTY = 
            "nbi.ui.swing.frame.width";
    public static final String FRAME_HEIGHT_PROPERTY = 
            "nbi.ui.swing.frame.height";
    public static final String FRAME_ICON_URI_PROPERTY = 
            "nbi.ui.swing.frame.icon.uri";
}
