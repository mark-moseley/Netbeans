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

package org.netbeans.modules.pdf;

import java.io.*;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Permits a PDF file to be opened in an external viewer.
 *
 * @author Jesse Glick
 * @author Marian Petras
 */
class PDFOpenSupport implements OpenCookie {
    
    private static final String[] APP_DIRS = new String[] {
            "/usr/bin", "/usr/local/bin" };                             //NOI18N
    private static final String[] VIEWER_NAMES = new String[] {
            "xpdf", "kghostview", "ggv", "acroread" };                  //NOI18N
    static final String FALLBACK_VIEWER_NAME = "acroread";              //NOI18N

    private File f;
    
    /**
     * @exception  java.lang.IllegalArgumentException
     *             if the specified file does not exist or is not a plain file
     */
    public PDFOpenSupport(File f) {
        if (!f.isFile()) {
            String msg = NbBundle.getMessage(PDFOpenSupport.class,
                                             "EXC_no_such_pdf",         //NOI18N
                                             f.getPath());
            throw new IllegalArgumentException(msg);
        }
        this.f = f;
    }

    public void open() {
        Settings sett = Settings.getDefault();
        File viewer = sett.getPDFViewer();
        final boolean viewerUnset = (viewer == null);
        if (viewerUnset) {
            viewer = findViewer();
        }
        try {
            Process p = Runtime.getRuntime().exec(
                    new String[] {viewer.getPath(),
                                  f.getAbsolutePath()
            });
            if (viewerUnset) {
                sett.setPDFViewer(viewer);
            }
            // [PENDING] redirect p's output
        } catch (IOException ioe) {
            // Try to reconfigure.
            String excmessage = ioe.getLocalizedMessage();
            String exceptionType = ioe.getClass().getName();
            int idx = exceptionType.lastIndexOf('.');
            if (idx != -1) {
                exceptionType = exceptionType.substring(idx + 1);
            }
            /* [PENDING] does not work (no properties show in sheet, though node has them):
            Node n;
            try {
                n = new BeanNode (sett);
            } catch (IntrospectionException ie) {
                TopManager.getDefault ().notifyException (ie);
                return;
            }
            PropertySheet sheet = new PropertySheet ();
            sheet.setNodes (new Node[] { n });
            //TopManager.getDefault ().getNodeOperation ().explore (n);
             */
            DialogDescriptor d = new DialogDescriptor(
                new ReconfigureReaderPanel(viewer, excmessage), // content pane
                NbBundle.getMessage(PDFOpenSupport.class,
                                    "TITLE_pick_a_viewer"));            //NOI18N
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                open ();
            }
        }
    }

    /**
     */
    private static File findViewer() {
        File viewer;
        for (int i = 0; i < APP_DIRS.length; i++) {
            for (int j = 0; j < VIEWER_NAMES.length; j++) {
                String viewerPath;
                viewerPath = APP_DIRS[i] + File.separatorChar + VIEWER_NAMES[j];
                viewer = new File(viewerPath);
                if (viewer.exists()) {
                    return viewer;
                }
            }
        }
        return new File(FALLBACK_VIEWER_NAME);
    }

    
}
