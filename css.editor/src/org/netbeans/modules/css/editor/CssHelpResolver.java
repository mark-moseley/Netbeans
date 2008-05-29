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
package org.netbeans.modules.css.editor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 *
 * @author marek.fukala@sun.com
 */
public class CssHelpResolver {

    private static CssHelpResolver instance;
    private static final String HELP_LOCATION = "docs/css21-spec.zip";
    public static final String HELP_URL = getHelpZIPURL();
    private WeakHashMap<String, String> pages_cache = new WeakHashMap<String, String>();

    public static synchronized CssHelpResolver instance() {
        if (instance == null) {
            instance = new CssHelpResolver("org/netbeans/modules/css/resources/css_property_help"); //NOI18N
        }
        return instance;
    }

    private CssHelpResolver(String sourcePath) {
        parseSource(sourcePath);
    }
    private Map<String, PropertyDescriptor> properties;

    public String getPropertyHelp(String propertyName) {
        return getHelpText(getPropertyHelpURL(propertyName));
    }

    private String getHelpText(URL url) {
        if (url == null) {
            return null;
        }

        //strip off the anchor url part
        String path = url.getPath();

        //try to load from cache
        String file_content = pages_cache.get(path);
        if (file_content == null) {
            try {
                InputStream is = url.openStream();
                byte buffer[] = new byte[1000];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int count = 0;
                do {
                    count = is.read(buffer);
                    if (count > 0) {
                        baos.write(buffer, 0, count);
                    }
                } while (count > 0);

                is.close();
                file_content = baos.toString();
                baos.close();
            } catch (java.io.IOException e) {
                Logger.global.log(Level.WARNING, "Cannot read css help file.", e); //NOI18N
            }

            pages_cache.put(path, file_content);
        }

        //strip off the "anchor part"
        String anchor = url.getRef();

        Pattern start_search = Pattern.compile("^.*<a name=\"" + anchor + "\".*$", Pattern.MULTILINE);
        Matcher matcher = start_search.matcher(file_content);
//        int anchor_index = file_content.indexOf("<a name=\"" + anchor + "\"");
        //find line beginning
        int start_index = 0;
        int begin_end_search_from = 0;
        if(matcher.find()) {
            start_index = matcher.start();
            begin_end_search_from = matcher.end();
        }
        
        
//        for (start_index = anchor_index; start_index > 0; start_index--) {
//            char ch = file_content.charAt(start_index);
//            if (ch == '\n') {
//                break;
//            }
//        }

        //find end of the "anchor part"
        int end_index = file_content.length();
        
        Pattern end_search = Pattern.compile("^.*<[hH][1-5]>|<a name=\"propdef-", Pattern.MULTILINE);
        matcher = end_search.matcher(file_content.subSequence(begin_end_search_from, file_content.length()));
        if(matcher.find()) {
            end_index = matcher.start() + begin_end_search_from + 1;
        }

        String anchor_part = file_content.substring(start_index, end_index);

        return anchor_part;

    }

    public URL getPropertyHelpURL(String propertyName) {
        PropertyDescriptor pd = getPD(propertyName);
        if (pd == null) {
            return null;
        } else {
            try {
                return new URL(HELP_URL + pd.helpLink);
            } catch (MalformedURLException ex) {
                Logger.global.log(Level.WARNING, "Error creating URL for property " + propertyName, ex);
                return null;
            }
        }
    }

//    public URL getPropertyValueHelp(String propertyName, String propertyValueName) {
//        PropertyDescriptor pd = getPD(propertyName);
//        if (pd != null) {
//            String valueHelpLink = pd.values.get(propertyValueName);
//            if (valueHelpLink == null) {
//                Logger.global.warning("No such value " + propertyValueName + " for property " + propertyName);
//            } else {
//                try {
//                    return new URL(valueHelpLink);
//                } catch (MalformedURLException ex) {
//                    Logger.global.log(Level.WARNING, "Error creating URL for property value " + propertyValueName + " (property " + propertyName + ")", ex);
//                }
//            }
//        }
//        return null;
//    }
    private PropertyDescriptor getPD(String propertyName) {
        PropertyDescriptor pd = properties.get(propertyName.toLowerCase());
        if (pd == null) {
            Logger.global.warning("No such property: " + propertyName);
            return null;
        } else {
            return pd;
        }
    }

    private void parseSource(String sourcePath) {
        ResourceBundle bundle = NbBundle.getBundle(sourcePath);

        properties = new HashMap<String, PropertyDescriptor>();

        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            //the bundle key is the property link; the property name is extracted from the link
            String helpLink = keys.nextElement();

            int propertyNameIdx = helpLink.indexOf('-');
            String propertyName = helpLink.substring(propertyNameIdx + 1);

            String value = bundle.getString(helpLink);

            //parse the value - delimiter is semicolon
            StringTokenizer st = new StringTokenizer(value, ";"); //NOI18N
            Map<String, String> valueToLink = new HashMap<String, String>();
            while (st.hasMoreTokens()) {
                String val = st.nextToken();
                int propertyValueIdx = helpLink.indexOf('-');
                String valueName = helpLink.substring(propertyValueIdx + 1);
                valueToLink.put(valueName, val);
            }

            PropertyDescriptor pd = new PropertyDescriptor(propertyName, helpLink, valueToLink);
            properties.put(propertyName, pd);

        }

    }

    private static String getHelpZIPURL() {
        File f = InstalledFileLocator.getDefault().locate(HELP_LOCATION, null, false); //NoI18N
        if (f != null) {
            try {
                URL urll = f.toURL();
                urll = FileUtil.getArchiveRoot(urll);
                return urll.toString();
            } catch (java.net.MalformedURLException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return null;
    }

    private static class PropertyDescriptor {

        String propertyName;
        String helpLink;
        Map<String, String> values;

        private PropertyDescriptor(String propertyName, String helpLink, Map<String, String> values) {
            this.propertyName = propertyName;
            this.helpLink = helpLink;
            this.values = values;
        }
    }
}
