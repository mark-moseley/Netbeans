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
 */
package org.netbeans.modules.soa.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.openide.filesystems.FileObject;
import org.openide.awt.UndoRedo;
import org.netbeans.modules.soa.ui.form.InitialFocusProvider;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.EditorCookie;
import org.netbeans.modules.xml.api.EncodingUtil;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.DataEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * @author nk160297
 */
public class SoaUiUtil {
    
    public static Color MISTAKE_RED = new Color(204, 0, 0);
    public static Color INACTIVE_BLUE = new Color(0, 102, 153);
    public static Color HTML_GRAY = new Color(153, 153, 153);
    
    private static String GRAY_COLOR = "#999999";
    
    private SoaUiUtil() {}

    public static String getGrayString(String message) {
        return getGrayString("", message);
    }
    
    public static String getGrayString(String nonGrayPrefix, String message) {
        return message == null ? nonGrayPrefix : "<html>"+getCorrectedHtmlRenderedString(nonGrayPrefix) // NOI18N
        +"<font color='"+GRAY_COLOR+"'>"+getCorrectedHtmlRenderedString(message)+"</font></html>";// NOI18N
    }
    
    public static String getGrayString(String nonGrayPrefix, String message
            , String nonGraySuffix) {
        return getGrayString(nonGrayPrefix,message,nonGraySuffix, true);
    }
    
    public static String getGrayString(String nonGrayPrefix, String message
            , String nonGraySuffix, boolean isSetHtmlHeader) {
        String htmlHeader = isSetHtmlHeader ? "<html>" : ""; // NOI18N
        String htmlFooter = isSetHtmlHeader ? "</html>" : ""; // NOI18N
        return message == null ? nonGrayPrefix : htmlHeader
                +getCorrectedHtmlRenderedString(nonGrayPrefix)
                +"<font color='"+GRAY_COLOR+"'>" // NOI18N
                +getCorrectedHtmlRenderedString(message)
                +"</font>" // NOI18N
                +(nonGraySuffix == null ? ""
                : getCorrectedHtmlRenderedString(nonGraySuffix))
                +htmlFooter;// NOI18N
    }
    
    public static String getFormattedHtmlString(
            boolean isSetHtmlHeader, TextChunk... chunkArr) {
        String htmlHeader = isSetHtmlHeader ? "<html>" : ""; // NOI18N
        String htmlFooter = isSetHtmlHeader ? "</html>" : ""; // NOI18N
        //
        StringBuffer sb = new StringBuffer();
        boolean isFirst = true;
        TextChunk prevTextChunk = null;
        boolean isPrevChunkStyled = false;
        for (TextChunk chunk : chunkArr) {
            if (chunk.myText == null || chunk.myText.length() == 0) {
                continue;
            }
            //
            boolean hasSameTextAttributes = prevTextChunk == null ? false :
                prevTextChunk.hasSameTextAttributes(chunk);
            if (isPrevChunkStyled && !hasSameTextAttributes) {
                sb.append("</font>");  // NOI18N
            }
            //
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(" "); // NOI18N
            }
            //
            if (chunk.myColor == null) {
                sb.append(getCorrectedHtmlRenderedString(chunk.myText));
                isPrevChunkStyled = false;
            } else {
                if (!isPrevChunkStyled || 
                        (isPrevChunkStyled && !hasSameTextAttributes)) {
                    sb.append("<font color='" + getHtmlColor(chunk.myColor) + "'>"); // NOI18N
                }
                sb.append(getCorrectedHtmlRenderedString(chunk.myText));
                isPrevChunkStyled = true;
            }
            //
            prevTextChunk = chunk;
        }
        //
        if (isPrevChunkStyled) {
            sb.append("</font>");  // NOI18N
        }
        //
        return htmlHeader + sb.toString() + htmlFooter;
    }
    
    public static class TextChunk {
        String myText = null;
        Color myColor = null;
        
        public TextChunk(String text) {
            myText = text;
        }
        
        public TextChunk(String text, Color color) {
            myText = text;
            myColor = color;
        }
        
        public boolean hasSameTextAttributes(TextChunk anotherChunk) {
            if (anotherChunk == null) {
                return false;
            }
            //
            return anotherChunk.myColor == this.myColor;
        }
    }
    
    public static String getHtmlColor(Color color) {
        int redValue = color.getRed();
        String red = redValue == 0 ? "00" : Integer.toHexString(redValue);
        //
        int greenValue = color.getGreen();
        String green = greenValue == 0 ? "00" : Integer.toHexString(greenValue);
        //
        int blueValue = color.getBlue();
        String blue = blueValue == 0 ? "00" : Integer.toHexString(blueValue);
        //
        return "#" + red + green + blue; // NOI18N
    }
    
    public static Image getBadgedIcon(Image originalImage, Image badgeImage) {
        return getBadgedIcon(originalImage, badgeImage, 9, 0);
    }
    
    public static Image getBadgedIcon(Image originalImage, Image badgeImage, int x, int y) {
        if (originalImage == null) {
            return null;
        }
        if (badgeImage == null) {
            return originalImage;
        }
        Image image = org.openide.util.Utilities.mergeImages(originalImage, badgeImage, x, y );
        return image;
    }
    
    public static final String getCorrectedHtmlRenderedString(String htmlString) {
        if (htmlString == null) {
            return null;
        }
        htmlString = htmlString.replaceAll("&amp;","&"); // NOI18n
        htmlString = htmlString.replaceAll("&gt;",">;"); // NOI18n
        htmlString = htmlString.replaceAll("&lt;","<"); // NOI18n
        
        htmlString = htmlString.replaceAll("&","&amp;"); // NOI18n
        htmlString = htmlString.replaceAll(">","&gt;"); // NOI18n
        htmlString = htmlString.replaceAll("<","&lt;"); // NOI18n
        return htmlString;
    }
    
    public static <T> T lookForChildByClass(Container parent, Class<T> clazz) {
        for (Component child : parent.getComponents()) {
            if (clazz.isInstance(child)) {
                return clazz.cast(child);
            }
            if (child instanceof Container) {
                return lookForChildByClass((Container)child, clazz);
            }
        }
        return null;
    }
    
    public static <T> Collection<T> lookForChildrenByClass(
            Container parent, Class<T> clazz) {
        ArrayList<T> result = new ArrayList<T>();
        lookForChildrenByClass(parent, clazz, result);
        return result;
    }
    
    private static <T> void lookForChildrenByClass(
            Container parent, Class<T> clazz, Collection<T> candidates) {
        for (Component child : parent.getComponents()) {
            if (clazz.isInstance(child)) {
                T candidate = clazz.cast(child);
                candidates.add(candidate);
            }
            if (child instanceof Container) {
                lookForChildrenByClass((Container)child, clazz, candidates);
            }
        }
    }
    
    /**
     * Looks for the component to which the focus should be set initially.
     * @param container 
     * @return 
     */
    public static Component getInitialFocusComponent(Container container) {
        Collection<InitialFocusProvider> providers = lookForChildrenByClass(
                container, InitialFocusProvider.class);
        //
        int maxPriority = Integer.MIN_VALUE;
        Component resultComp = null;
        //
        for (InitialFocusProvider provider : providers) {
            int priority = provider.getProviderPriority();
            if (priority > maxPriority) {
                maxPriority = priority;
                resultComp = provider.getInitialFocusComponent();
            }
        }
        //
        return resultComp;
    }
    
    /**
     * This method has to be called after the pack() and before the setVisible() 
     * for the dialog or window.
     */ 
    public static boolean setInitialFocusComponentFor(Container container) {
        Component comp = SoaUiUtil.getInitialFocusComponent(container);
        if (comp != null) {
            return comp.requestFocusInWindow();
        }
        return false;
    }
    
    // vlv
    public static void fixEncoding(DataObject data, FileObject dir) throws IOException {
      DataEditorSupport support = data.getLookup().lookup(DataEditorSupport.class);

      if ( !(support instanceof UndoRedoManagerProvider)) {
        return;
      }
      UndoRedo.Manager manager = ((UndoRedoManagerProvider) support).getUndoRedoManager();
      String encoding = EncodingUtil.getProjectEncoding(DataFolder.findFolder(dir).getPrimaryFile());

      // # 115502
      if (encoding == null || !EncodingUtil.isValidEncoding(encoding)) {
        encoding = "UTF-8"; // NOI18N
      }
      EditorCookie editor = data.getCookie(EditorCookie.class);
      Document document = (Document) editor.openDocument();
      
      try {
        document.insertString(19, " encoding=\"" + encoding + "\"", null);
      }
      catch (BadLocationException e) {
        ErrorManager.getDefault().notify(e);
      }

      SaveCookie save = data.getCookie(SaveCookie.class);
      
      if (save != null) {
        save.save();
      }
      // # 119057 after changes for # 115502
      if (manager == null) {
        return;
      }
      manager.discardAllEdits();
    }
    
    public static TopComponent safeFindTopComponent(final String tcId) {
        if (tcId == null || "".equals(tcId)) { // NOI18N
            return null;
        }
        TopComponent tc = null;
        if (SwingUtilities.isEventDispatchThread()) {
            tc = WindowManager.getDefault().findTopComponent(tcId);
        } else {
            class SafeFindTopComponent implements Runnable {
                private TopComponent myTopComponent;
                public void run() {
                    myTopComponent = WindowManager.getDefault().findTopComponent(tcId);
                }
                
                public TopComponent getTopComponent() {
                    return myTopComponent;
                }
            }
            SafeFindTopComponent findTc = new SafeFindTopComponent();
            try {
                SwingUtilities.invokeAndWait(findTc);
                tc = findTc.getTopComponent();
            } catch(InterruptedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                return null;
            } catch (InvocationTargetException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                return null;
            }
        }
        
        return tc;
    }

    public static void activateInlineMnemonics(Container owner) {
        for (Component comp : owner.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel)comp;
                Mnemonics.setLocalizedText(label, label.getText());
            } else if (comp instanceof AbstractButton) {
                AbstractButton button = (AbstractButton)comp;
                Mnemonics.setLocalizedText(button, button.getText());
            } else if (comp instanceof Container) {
                activateInlineMnemonics((Container)comp);
            }
        }
    }
    
    public static void fireHelpContextChange(Component comp, HelpCtx newHelpCtx) {
        Container parent = comp.getParent();
        if (parent != null) {
            PropertyChangeEvent event = new PropertyChangeEvent(
                    comp, DialogDescriptor.PROP_HELP_CTX,
                    null, newHelpCtx);
            //
            // notify all parents that the help context is changed
            while (true) {
                if (parent instanceof PropertyChangeListener) {
                    ((PropertyChangeListener) parent).propertyChange(event);
                }
                //
                Container newParent = parent.getParent();
                if (newParent == null || newParent == parent) {
                    break;
                }
                parent = newParent;
            }
        }
    }
}
