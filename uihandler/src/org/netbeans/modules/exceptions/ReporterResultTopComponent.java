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
package org.netbeans.modules.exceptions;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.modules.uihandler.Installer;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays Exception reporter results.
 */
public final class ReporterResultTopComponent extends TopComponent implements HyperlinkListener {

    private static ReporterResultTopComponent instance;
    private static final Logger LOG = Logger.getLogger(ReporterResultTopComponent.class.getName());
    private static boolean showUpload = false;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "ReporterResultTopComponent";

    private ReporterResultTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ReporterResultTopComponent.class, "CTL_ReporterResultTopComponent"));
        setToolTipText(NbBundle.getMessage(ReporterResultTopComponent.class, "HINT_ReporterResultTopComponent"));
        dataDisplayer.addHyperlinkListener(this);
        dataDisplayer.setContentType("text/html");
//        setIcon(Utilities.loadImage(ICON_PATH, true));

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        dataDisplayer = new javax.swing.JEditorPane();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        dataDisplayer.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        dataDisplayer.setEditable(false);
        jScrollPane1.setViewportView(dataDisplayer);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setPreferredSize(new java.awt.Dimension(320, 35));

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(ReporterResultTopComponent.class, "ReporterResultTopComponent.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jButton1)
                .addContainerGap(208, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(jPanel1, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        showMyIssues();
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane dataDisplayer;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized ReporterResultTopComponent getDefault() {
        if (instance == null) {
            instance = new ReporterResultTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ReporterResultTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized ReporterResultTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ReporterResultTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");   //NOI18N
            return getDefault();
        }
        if (win instanceof ReporterResultTopComponent) {
            return (ReporterResultTopComponent) win;
        }
        Logger.getLogger(ReporterResultTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID + //NOI18N
                "' ID. That is a potential source of errors and unexpected behavior.");         //NOI18N
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        if (showUpload) {
            return;
        }
        showMyIssues();
    }

    @Override
    public void componentClosed() {
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public static void showUploadDone(final URL url){
        if (EventQueue.isDispatchThread()) {
            try {
                findInstance().showUploadDoneImpl(url);
            } catch (IOException ex) {
                handleIOException(url, ex);
            }
        } else {
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    try {
                        findInstance().showUploadDoneImpl(url);
                    } catch (IOException ex) {
                        handleIOException(url, ex);
                    }
                }
            });
        }
    }

    private void showUploadDoneImpl(final URL url) throws IOException {
        assert (EventQueue.isDispatchThread());
        showUpload = true;
        open();
        showUpload = false;
        loadPage(url);
    }

    private void showMyIssues() {
        String urlStr = null;
        String userName = new ExceptionsSettings().getUserName();
        if (userName != null && !"".equals(userName)) {             //NOI18N
            urlStr = NbBundle.getMessage(ReporterResultTopComponent.class, "userNameURL") + userName;
        } else {
            String userId = Installer.findIdentity();
            if (userId != null) {
                urlStr = NbBundle.getMessage(ReporterResultTopComponent.class, "userIdURL") + userId;
            }
        }
        try {
            loadPage(new URL(urlStr));
        } catch (MalformedURLException ex) {
            handleIOException(urlStr, ex);
        }
    }

    private void loadPage(URL url) {
        assert (EventQueue.isDispatchThread());
        try {
            dataDisplayer.setPage(getLoadingPageURL(url));
        } catch (IOException ex) {
            handleIOException(url, ex);
        }
        RequestProcessor.getDefault().post(new PageUploader(url));
    }

    private class PageUploader implements Runnable{

        private URL localData = null;
        private final URL url;

        private PageUploader(URL url) {
            this.url = url;
        }

            public void run() {
                try {
                    if (EventQueue.isDispatchThread()) {
                        ReporterResultTopComponent.this.requestVisible();
                        dataDisplayer.setPage(localData);
                    } else {
                        LOG.fine("Loading: " + url);        //NOI18N
                        localData = uploadURL(url);
                        EventQueue.invokeLater(this);
                    }
                } catch (IOException ex) {
                    handleIOException(url, ex);
                }
            }
        
    }

    private static URL uploadURL(URL url) throws IOException {
        assert(!EventQueue.isDispatchThread());
        File tmpFile = File.createTempFile("loading", ".html");        //NOI18N
        tmpFile.deleteOnExit();
        FileOutputStream fw = new FileOutputStream(tmpFile);
        try{
            URLConnection conn = url.openConnection();
            conn.setReadTimeout(200000);
            conn.setDoOutput(false);
            conn.setDoInput(true);
            conn.setRequestProperty("User-Agent", "NetBeans");      //NOI18N
            InputStream is = conn.getInputStream();
            try{
                while(true) {
                    int ch = is.read();
                    if (ch == -1) {
                        break;
                    }
                    fw.write(ch);
                }
            }finally{
                is.close();
            }
        }finally{
            fw.close();
        }
        return tmpFile.toURI().toURL();
    }

    private static void handleIOException(URL url, IOException ex) {
        handleIOException(url.toString(), ex);
    }

    private static void handleIOException(final String url, IOException ex) {
        LOG.log(Level.INFO, "URL Loading failed", ex);        //NOI18N
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                instance.dataDisplayer.setText(NbBundle.getMessage(ReporterResultTopComponent.class,"no_data_found") + url);
            }
        });
    }

    private static URL getLoadingPageURL(URL url) throws IOException {
        File tmpFile = File.createTempFile("loading", ".html");        //NOI18N
        tmpFile.deleteOnExit();
        FileWriter fw = new FileWriter(tmpFile);
        try{
            fw.write("<html><head><title></title></head><body>");
            fw.write(NbBundle.getMessage(ReporterResultTopComponent.class, "LoadingMessage"));
            fw.write("<a href=\"" + url.toString() + "\">" + url + "</a>");
            fw.write("</body></html>");
        }finally{
            fw.close();
        }
        return tmpFile.toURI().toURL();
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
            HtmlBrowser.URLDisplayer.getDefault().showURL(e.getURL());
        }
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return ReporterResultTopComponent.getDefault();
        }
    }
}
