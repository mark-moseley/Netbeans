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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.welcome.content;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.UIManager;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
public class Utils {
    
    /** Creates a new instance of Utils */
    private Utils() {
    }

    public static Graphics2D prepareGraphics(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Map rhints = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
        if( rhints == null && Boolean.getBoolean("swing.aatext") ) { //NOI18N
             g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        } else if( rhints != null ) {
            g2.addRenderingHints( rhints );
        }
        return g2;
    }

    public static void showURL(String href) {
        try {
            HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault();
            if (displayer != null) {
                displayer.showURL(new URL(href));
            }
        } catch (Exception e) {}
    }

    static int getDefaultFontSize() {
        Integer customFontSize = (Integer)UIManager.get("customFontSize"); // NOI18N
        if (customFontSize != null) {
            return customFontSize.intValue();
        } else {
            Font systemDefaultFont = UIManager.getFont("TextField.font"); // NOI18N
            return (systemDefaultFont != null)
                ? systemDefaultFont.getSize()
                : 12;
        }
    }

    public static Action findAction( String key ) {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(key);
        
        if (fo != null && fo.isValid()) {
            try {
                DataObject dob = DataObject.find(fo);
                InstanceCookie ic = dob.getCookie(InstanceCookie.class);
                
                if (ic != null) {
                    Object instance = ic.instanceCreate();
                    if (instance instanceof Action) {
                        Action a = (Action) instance;
                        return a;
                    }
                }
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                return null;
            }
        }
        return null;
    }

    public static Action createSampleProjectAction() {
        ClassLoader loader = Lookup.getDefault().lookup( ClassLoader.class );
        if( null == loader )
            loader = ClassLoader.getSystemClassLoader();
        try {
            Class clazz = Class.forName( "org.netbeans.modules.project.ui.actions.NewProject", true, loader ); // NOI18N
            Method getDefault = clazz.getMethod( "newSample"); // NOI18N
            Object newSample = getDefault.invoke( null );
            if( newSample instanceof Action )
                return (Action)newSample;
        } catch( Exception e ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
        }
        return null;
    }
    
    public static Color getColor( String resId ) {
        ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.welcome.resources.Bundle"); // NOI18N
        try {
            Integer rgb = Integer.decode(bundle.getString(resId));
            return new Color(rgb.intValue());
        } catch( NumberFormatException nfE ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, nfE );
            return Color.BLACK;
        }
    }
    
    public static File getCacheStore() throws IOException {
        File cacheStore;
        String userDir = System.getProperty("netbeans.user"); // NOI18N
        if (userDir != null) {
            cacheStore = new File(new File(new File (userDir, "var"), "cache"), "welcome"); // NOI18N
        } else {
            File cachedir = FileUtil.toFile(Repository.getDefault().getDefaultFileSystem().getRoot());
            cacheStore = new File(cachedir, "welcome"); // NOI18N
        }
        return cacheStore;
    }

    /**
     * Try to extract the URL from the given DataObject using reflection.
     * (The DataObject should be URLDataObject in most cases)
     */
    public static String getUrlString(DataObject dob) {
        try {
            Method m = dob.getClass().getDeclaredMethod( "getURLString", new Class[] {} ); //NOI18N
            m.setAccessible( true );
            Object res = m.invoke( dob );
            if( null != res ) {
                return res.toString();
            }
        } catch (Exception ex) {
            //ignore
        }
        return null;
    }
}
