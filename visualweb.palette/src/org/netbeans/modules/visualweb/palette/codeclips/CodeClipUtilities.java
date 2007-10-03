/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */ /*
 * CodeClipUtilities.java
 *
 * Created on July 27, 2006, 10:16 AM
 *
 * This is a generic codeclip utilities class.
 *
 * @author Joelle Lam <joelle.lam@sun.com>
 * @version %I%, %G%
 */

package org.netbeans.modules.visualweb.palette.codeclips;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;


public class CodeClipUtilities {

    private static final String LOC_MARKER = "~"; // NOI18N
    private static final String PARAM_MARKER = "@"; // NOI18N

    /** Creates a new instance of CodeClipUtilities */
//    public CodeClipUtilities() {
//    }
    /*
     * create a new codeclip in the given category folder.
     * @param categoryFolder DataFolder of the given category.
     */
    public static void createCodeClip(DataFolder categoryFolder) {
        final FileObject categoryFile = categoryFolder.getPrimaryFile();
        final String displayNameString = NbBundle.getMessage(CodeClipUtilities.class, "CLIP");


        CodeClipViewerPanel snippetViewer = new CodeClipViewerPanel(displayNameString, displayNameString, "");
        snippetViewer.setVisible(true);
        if (!snippetViewer.isCancelled()) {
            try {
                CodeClipUtilities.createCodeClipFile(categoryFile, snippetViewer.getContentText(), snippetViewer.getClipName(), null, snippetViewer.getToolTip());
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (MissingResourceException mre) {
                ErrorManager.getDefault().notify(mre);
            }
        }
    }

    public static String parseClipForParams(String title, String str) {
        String clipStr = str;
        Vector editedToken = new Vector();
        // look for marker
        int occ = clipStr.indexOf(PARAM_MARKER);
        if (occ != -1) {
            // we need two or more markers and a way to escape them
            //String[] paramArray = new String[3];
            Vector<String> paramArray = new Vector<String>();
            String chopped = clipStr;
            for (int i = 0; occ != -1; i++) {
                int nextOcc = chopped.indexOf(PARAM_MARKER, occ + 1);

                if (nextOcc < 0) {
                    occ = -1;
                } else if (((String) chopped.substring(occ + 1, nextOcc)).contains(" ")) {
                    //This match is currently not picking up.
                    //It is an annotation
                    chopped = chopped.substring(nextOcc);
                    occ = nextOcc; //the is should equal chopped.indexOf(PARAM_MARKER)
                } else {

                    if (!paramArray.contains(chopped.substring(occ + 1, nextOcc))) {
                        paramArray.add(chopped.substring(occ + 1, nextOcc));
                    }
                    chopped = chopped.substring(nextOcc + 1);
                    occ = chopped.indexOf(PARAM_MARKER);
                }
            }
            if (paramArray.size() == 0) {
                return clipStr;
            }
            final CodeClipsParametersDialog paramEditor = new CodeClipsParametersDialog(title, paramArray);
            paramEditor.setVisible(true);
            if (paramEditor.isCancelled()) {
                return ""; // NOI18N
            }

            if (!paramEditor.isCancelled()) {
                editedToken = paramEditor.getNewParam();
            }

            // insert token back in code clip the edited part
            int j = 0;
            while (j < paramArray.size()) {
                if (paramArray.elementAt(j) != null) {
                    String paramElement = paramArray.elementAt(j);
                    String editedElement = editedToken.elementAt(j).toString();
                    clipStr = clipStr.replaceAll(PARAM_MARKER + paramElement + PARAM_MARKER, editedElement);
                }
                j++;
            }
        }

        return clipStr;
    }

    public static String fillFromBundle(String body, String bundleName) {
        String clipStr = body;
        // look for marker
        int occ = clipStr.indexOf(LOC_MARKER);
        if (occ != -1) {
            Vector<String> paramArray = new Vector<String>();
            String chopped = clipStr;
            for (int i = 0; occ != -1; i++) {
                int nextOcc = chopped.indexOf(LOC_MARKER, occ + 1);
                if (!paramArray.contains(chopped.substring(occ + 1, nextOcc))) {
                    paramArray.add(chopped.substring(occ + 1, nextOcc));
                }
                chopped = chopped.substring(nextOcc + 1);
                occ = chopped.indexOf(LOC_MARKER);
            }
            Vector<String> editedToken = new Vector<String>();
            for (int i = 0; i < paramArray.size(); i++) {
                try {
                    ResourceBundle ccbundle = NbBundle.getBundle(bundleName);
                    editedToken.add(ccbundle.getString(paramArray.elementAt(i)));
                } catch (MissingResourceException mse) {
                    editedToken.add(LOC_MARKER + paramArray.elementAt(i) + LOC_MARKER);
                }
            }
            // insert token back in code clip the edited part
            int j = 0;
            while (j < paramArray.size()) {
                if (paramArray.elementAt(j) != null) {
                    String paramElement = paramArray.elementAt(j);
                    String editedElement = editedToken.elementAt(j);
                    clipStr = clipStr.replaceAll(LOC_MARKER + paramElement + LOC_MARKER, editedElement);
                }
                j++;
            }
        }
        return clipStr;
    }

    /**
     * Creates a code clip and uses the displayNameString as the name without checking the bundle file.
     */
    public static void createCodeClipFile(FileObject folder, String body, String displayNameString, String tooltip) throws IOException {
        String localizingBundle = "org.netbeans.modules.visualweb.palette.codeclips.Bundle";
        createCodeClipFile(folder, body, displayNameString, localizingBundle, tooltip);
    }

    /**
     * Creates a code clip
     *
     * @param  folder the fileObject or category in which you wan the codeClipCreated
     * @param  body the body of the codeclip
     * @param displayNameString the codeclip name (it will be localized if a valid bundle file is given)
     * @param localizingBundle the bundle file for which to find the display name string.
     * @param image the image file associated with this codeclip.
     */
    public static void createCodeClipFile(FileObject folder, String body, String displayNameString, String bundleName, String tooltip) throws IOException {

        String nameExt = "xml";
        String image_16 = "org/netbeans/modules/visualweb/spi/palette/resources/Codesnippet_C16.png";
        String image_32 = "org/netbeans/modules/visualweb/spi/palette/resources/Codesnippet_C32.png";


        String fileName = getFreeFileName(folder, "CLIP", nameExt); //NOI18N
//        String fileName = getFreeFileName(folder, displayNameString, nameExt );
        FileObject itemFile = folder.createData(fileName, "xml");
//        This would be only necessary if I wanted to increment the "CLIP" name, however, if I do that, all edited clips would also take on their name.
//        String displayName = fileName.replace('_',' ');
        StringBuffer buff = new StringBuffer(512);

        buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); // NOI18N
        buff.append("<!DOCTYPE " + CodeClipHandler.XML_ROOT + " PUBLIC \"-//NetBeans//CodeClip Palette Item 1.0//EN\"\n"); // NOI18N
        buff.append("\"http://www.netbeans.org/dtds/codeclip-palette-item-1_0.dtd\">\n\n"); // NOI18N
        buff.append("  <" + CodeClipHandler.XML_ROOT + " " + CodeClipHandler.ATTR_VERSION + "=\"" + CodeClipHandler.LATEST_VERSION + "\">\n"); // NOI18N
        buff.append("    <" + CodeClipHandler.TAG_BODY + ">\n"); // NOI18N
        buff.append("        <![CDATA[\n"); //NOIl8N
        buff.append(body + "\n");
        buff.append("        ]]>\n"); //NOIl8N
        buff.append("    </" + CodeClipHandler.TAG_BODY + ">\n"); // NOI18N
        buff.append("<" + CodeClipHandler.TAG_ICON16 + " urlvalue=\"" + image_16 + "\"/>\n");
        buff.append("<" + CodeClipHandler.TAG_ICON32 + " urlvalue=\"" + image_32 + "\"/>\n");
//        This code line causes names to be fumbled.
//        buff.append("<" + CodeClipHandler.TAG_DESCRIPTION + " display-name-key=\"" + displayName + "\" \n");
        buff.append("<" + CodeClipHandler.TAG_DESCRIPTION + " display-name-key=\"" + displayNameString + "\" \n");
        if (bundleName != null) {
            buff.append("              " + CodeClipHandler.ATTR_BUNDLE + "=\"" + bundleName + "\" \n");
        }
        if (tooltip != null) {
            buff.append("              " + CodeClipHandler.ATTR_TOOLTIP_KEY + "=\"" + tooltip + "\" \n");
        }
        buff.append("/>\n");
        buff.append("</" + CodeClipHandler.XML_ROOT + ">");

        FileLock lock = itemFile.lock();

        //OutputStreamWriter out = new OutputStreamWriter(new ByteArrayOutputStream(),"UTF-32");
        OutputStream os = itemFile.getOutputStream(lock);

        OutputStreamWriter out = new OutputStreamWriter(os, "UTF8");
        out.write(buff.toString());
        out.close();
        //os.write(buff.toString().getBytes());
        os.close();
        lock.releaseLock();
    }

    public static String getFreeFileName(FileObject folder, String filename, String nameExt) {
        String myfilename = filename.replaceAll("\\W", "");
        return FileUtil.findFreeFileName(folder, myfilename, nameExt);
    }
}
