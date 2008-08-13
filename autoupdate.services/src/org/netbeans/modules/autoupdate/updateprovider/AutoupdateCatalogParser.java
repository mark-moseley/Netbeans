/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.autoupdate.services.Trampoline;
import org.netbeans.modules.autoupdate.services.UpdateLicenseImpl;
import org.netbeans.modules.autoupdate.services.Utilities;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateLicense;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateCatalogParser extends DefaultHandler {
    private final Map<String, UpdateItem> items;
    private final AutoupdateCatalogProvider provider;
    private final EntityResolver entityResolver;
    private final URI baseUri;
    
    private AutoupdateCatalogParser (Map<String, UpdateItem> items, AutoupdateCatalogProvider provider, URI base) {
        this.items = items;
        this.provider = provider;
        this.entityResolver = org.netbeans.updater.XMLUtil.createAUResolver ();
        this.baseUri = base;
    }
    
    private static final Logger ERR = Logger.getLogger (AutoupdateCatalogParser.class.getName ());
    
    private static enum ELEMENTS {
        module_updates, module_group, notification, module, description,
        module_notification, external_package, manifest, l10n, license
    }
    
    private static final String MODULE_UPDATES_ATTR_TIMESTAMP = "timestamp"; // NOI18N
    
    private static final String MODULE_GROUP_ATTR_NAME = "name"; // NOI18N
    
    private static final String NOTIFICATION_ATTR_URL = "url"; // NOI18N
    
    private static final String LICENSE_ATTR_NAME = "name"; // NOI18N
    
    private static final String MODULE_ATTR_CODE_NAME_BASE = "codenamebase"; // NOI18N
    private static final String MODULE_ATTR_HOMEPAGE = "homepage"; // NOI18N
    private static final String MODULE_ATTR_DISTRIBUTION = "distribution"; // NOI18N
    private static final String MODULE_ATTR_DOWNLOAD_SIZE = "downloadsize"; // NOI18N
    private static final String MODULE_ATTR_NEEDS_RESTART = "needsrestart"; // NOI18N
    private static final String MODULE_ATTR_MODULE_AUTHOR = "moduleauthor"; // NOI18N
    private static final String MODULE_ATTR_RELEASE_DATE = "releasedate"; // NOI18N
    private static final String MODULE_ATTR_IS_GLOBAL = "global"; // NOI18N
    private static final String MODULE_ATTR_TARGET_CLUSTER = "targetcluster"; // NOI18N
    private static final String MODULE_ATTR_EAGER = "eager"; // NOI18N
    private static final String MODULE_ATTR_AUTOLOAD = "autoload"; // NOI18N
    private static final String MODULE_ATTR_LICENSE = "license"; // NOI18N
    
    private static final String MANIFEST_ATTR_SPECIFICATION_VERSION = "OpenIDE-Module-Specification-Version"; // NOI18N
    
    private static final String TIME_STAMP_FORMAT = "ss/mm/hh/dd/MM/yyyy"; // NOI18N
    
    private static final String L10N_ATTR_LOCALE = "langcode"; // NOI18N
    private static final String L10N_ATTR_BRANDING = "brandingcode"; // NOI18N
    private static final String L10N_ATTR_MODULE_SPECIFICATION = "module_spec_version"; // NOI18N
    private static final String L10N_ATTR_MODULE_MAJOR_VERSION = "module_major_version"; // NOI18N
    private static final String L10N_ATTR_LOCALIZED_MODULE_NAME = "OpenIDE-Module-Name"; // NOI18N
    private static final String L10N_ATTR_LOCALIZED_MODULE_DESCRIPTION = "OpenIDE-Module-Long-Description"; // NOI18N
    
    private static String GZIP_EXTENSION = ".gz"; // NOI18N
    
    public synchronized static Map<String, UpdateItem> getUpdateItems (URL url, AutoupdateCatalogProvider provider) {
        Map<String, UpdateItem> items = new HashMap<String, UpdateItem> ();
        try {
            URI base = null;
            if (provider != null) {
                try {
                    base = provider.getUpdateCenterURL ().toURI ();
                } catch (URISyntaxException ex) {
                    ERR.log (Level.INFO, null, ex);
                }
            } else {
                base = url.toURI ();
            }
            SAXParser saxParser = SAXParserFactory.newInstance ().newSAXParser ();
            saxParser.parse (getInputSource (url, provider), new AutoupdateCatalogParser (items, provider, base));
        } catch (URISyntaxException ex) {
            ERR.log (Level.INFO, "Problems while parsing " + url, ex);
        } catch (SAXException ex) {
            ERR.log (Level.INFO, "Problems while parsing " + url, ex);
        } catch (IOException ex) {
            ERR.log (Level.INFO, "Problems while parsing " + url, ex);
        } catch (ParserConfigurationException ex) {
            ERR.log (Level.INFO, "Problems while parsing " + url, ex);
        }
        return items;
    }
    
    private static boolean isGzip (AutoupdateCatalogProvider p) {
        boolean res = false;
        if (p != null) {
            URL url = p.getUpdateCenterURL ();
            if (url != null) {
                res = url.getPath ().toLowerCase ().endsWith (GZIP_EXTENSION);
                ERR.log (Level.FINER, "Is GZIP " + url + " ? " + res);
            } else {
                ERR.log (Level.WARNING, "AutoupdateCatalogProvider has not URL.");
            }
        }
        return res;
    }
    
    private static InputStream getInputSource (URL toParse, AutoupdateCatalogProvider p) {
        InputStream is = null;
        try {
            if (isGzip (p)) {
                is = new BufferedInputStream (new GZIPInputStream (toParse.openStream ()));
            } else {
                is = new BufferedInputStream (toParse.openStream ());
            }
        } catch (IOException ex) {
            ERR.log (Level.SEVERE, null, ex);
        }
        return is;
    }
    
    private Stack<String> currentGroup = new Stack<String> ();
    private String catalogDate;
    private Stack<ModuleDescriptor> currentModule = new Stack<ModuleDescriptor> ();
    private Stack<String> currentLicense = new Stack<String> ();
    private Stack<String> currentNotificationUrl = new Stack<String> ();
    private Map<String, UpdateLicenseImpl> name2license = new HashMap<String, UpdateLicenseImpl> ();
    private List<String> lines = new ArrayList<String> ();
    private int bufferInitSize = 0;

    @Override
    public void characters (char[] ch, int start, int length) throws SAXException {
        lines.add (new String(ch, start, length));
        bufferInitSize += length;
    }

    @Override
    public void endElement (String uri, String localName, String qName) throws SAXException {
        switch (ELEMENTS.valueOf (qName)) {
            case module_updates :
                break;
            case module_group :
                assert ! currentGroup.empty () : "Premature end of module_group " + qName;
                currentGroup.pop ();
                break;
            case module :
                assert ! currentModule.empty () : "Premature end of module " + qName;
                currentModule.pop ();
                break;
            case l10n :
                break;
            case manifest :
                break;
            case description :
                ERR.info ("Not supported yet.");
                break;
            case notification :
                // write catalog notification
                if (this.provider != null && ! lines.isEmpty ()) {
                    StringBuffer sb = new StringBuffer (bufferInitSize);
                    for (String line : lines) {
                        sb.append (line);
                    }
                    String notification = sb.toString ();
                    String notificationUrl = currentNotificationUrl.peek ();
                    if (notificationUrl != null && notificationUrl.length () > 0) {
                        notification += (notification.length () > 0 ? "<br>" : "") + // NOI18N
                                "<a href=\"" + notificationUrl + "\">" + notificationUrl + "</a>"; // NOI18N
                    }
                    provider.setNotification (notification);
                }
                lines.clear ();
                bufferInitSize = 0;
                currentNotificationUrl.pop ();
                break;
            case module_notification :
                // write module notification
                if (! lines.isEmpty ()) {
                    ModuleDescriptor md = currentModule.peek ();
                    assert md != null : "ModuleDescriptor found for " + provider;
                    StringBuffer buf = new StringBuffer (bufferInitSize);
                    for (String line : lines) {
                        buf.append (line);
                    }
                    md.appendNotification (buf.toString ());
                    lines.clear ();
                    bufferInitSize = 0;
                }
                break;
            case external_package :
                ERR.info ("Not supported yet.");
                break;
            case license :
                assert ! currentLicense.empty () : "Premature end of license " + qName;
                
                if (! lines.isEmpty ()) {

                    // find and fill UpdateLicenseImpl
                    StringBuffer sb = new StringBuffer (bufferInitSize);
                    for (String line : lines) {
                        sb.append (line);
                    }
                    UpdateLicenseImpl updateLicenseImpl = this.name2license.get (currentLicense.peek ());
                    // in invalid catalog might be a un-paired license
                    // assert updateLicenseImpl != null : "UpdateLicenseImpl found in map for key " + currentLicense.peek ();
                    if (updateLicenseImpl != null) {
                        updateLicenseImpl.setAgreement (sb.toString ());
                    } else {
                        ERR.info ("Unpaired license " + currentLicense.peek () + " without any module.");
                    }

                    lines.clear ();
                    bufferInitSize = 0;
                }
                
                currentLicense.pop ();
                break;
            default:
                ERR.warning ("Unknown element " + qName);
        }
    }

    @Override
    public void endDocument () throws SAXException {
        ERR.fine ("End parsing " + (provider == null ? "" : provider.getUpdateCenterURL ()) + " at " + System.currentTimeMillis ());
    }

    @Override
    public void startDocument () throws SAXException {
        ERR.fine ("Start parsing " + (provider == null ? "" : provider.getUpdateCenterURL ()) + " at " + System.currentTimeMillis ());
    }

    @Override
    public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (ELEMENTS.valueOf (qName)) {
            case module_updates :
                try {
                    catalogDate = "";
                    DateFormat format = new SimpleDateFormat (TIME_STAMP_FORMAT);
                    String timeStamp = attributes.getValue (MODULE_UPDATES_ATTR_TIMESTAMP);
                    if (timeStamp == null) {
                        ERR.info ("No timestamp is presented in " + (this.provider == null ? "" : this.provider.getUpdateCenterURL ()));
                    } else {
                        catalogDate = Utilities.formatDate (format.parse (timeStamp));
                        ERR.finer ("Successfully read time " + timeStamp); // NOI18N
                    }
                } catch (ParseException pe) {
                    ERR.log (Level.INFO, null, pe);
                }
                break;
            case module_group :
                currentGroup.push (attributes.getValue (MODULE_GROUP_ATTR_NAME));
                break;
            case module :
                ModuleDescriptor md = ModuleDescriptor.getModuleDescriptor (
                        currentGroup.size () > 0 ? currentGroup.peek () : null, /* group */
                        baseUri, /* base URI */
                        this.catalogDate); /* catalog date */
                md.appendModuleAttributes (attributes);
                currentModule.push (md);
                break;
            case l10n :
                // construct l10n
                // XXX
                break;
            case manifest :
                
                // construct module
                ModuleDescriptor desc = currentModule.peek ();
                desc.appendManifest (attributes);
                UpdateItem m = desc.createUpdateItem ();
                
                // put license impl to map for future refilling
                UpdateItemImpl impl = Trampoline.SPI.impl (m);
                String licName = impl.getUpdateLicenseImpl ().getName ();
                if (this.name2license.keySet ().contains (licName)) {
                    impl.setUpdateLicenseImpl (this.name2license.get (licName));
                } else {
                    this.name2license.put (impl.getUpdateLicenseImpl ().getName (), impl.getUpdateLicenseImpl ());
                }
                
                // put module into UpdateItems
                items.put (desc.getId (), m);
                
                break;
            case description :
                ERR.info ("Not supported yet.");
                break;
            case module_notification :
                break;
            case notification :
                currentNotificationUrl.push (attributes.getValue (NOTIFICATION_ATTR_URL));
                break;
            case external_package :
                ERR.info ("Not supported yet.");
                break;
            case license :
                assert lines.isEmpty (): "No other license is processing at start of new license.";
                currentLicense.push (attributes.getValue (LICENSE_ATTR_NAME));
                break;
            default:
                ERR.warning ("Unknown element " + qName);
        }
    }
    
    @Override
    public InputSource resolveEntity (String publicId, String systemId) throws IOException, SAXException {
        return entityResolver.resolveEntity (publicId, systemId);
    }
    
    private static class ModuleDescriptor {
        private String moduleCodeName;
        private URL distributionURL;
        private String targetcluster;
        private String homepage;
        private String downloadSize;
        private String author;
        private String publishDate;
        private String notification;

        private Boolean needsRestart;
        private Boolean isGlobal;
        private Boolean isEager;
        private Boolean isAutoload;

        private String specVersion;
        private Manifest mf;
        
        private String id;

        private UpdateLicense lic;
        
        private String group;
        private URI base;
        private String catalogDate;
        
        private static ModuleDescriptor md = null;
        
        private ModuleDescriptor () {}
        
        public static ModuleDescriptor getModuleDescriptor (String group, URI base, String catalogDate) {
            if (md == null) {
                md = new ModuleDescriptor ();
            }
            
            md.group = group;
            md.base = base;
            md.catalogDate = catalogDate;
            
            return md;
        }
        
        public void appendModuleAttributes (Attributes module) {
            moduleCodeName = module.getValue (MODULE_ATTR_CODE_NAME_BASE);
            distributionURL = getDistribution (module.getValue (MODULE_ATTR_DISTRIBUTION), base);
            targetcluster = module.getValue (MODULE_ATTR_TARGET_CLUSTER);
            homepage = module.getValue (MODULE_ATTR_HOMEPAGE);
            downloadSize = module.getValue (MODULE_ATTR_DOWNLOAD_SIZE);
            author = module.getValue (MODULE_ATTR_MODULE_AUTHOR);
            publishDate = module.getValue (MODULE_ATTR_RELEASE_DATE);
            if (publishDate == null || publishDate.length () == 0) {
                publishDate = catalogDate;
            }
            String needsrestart = module.getValue (MODULE_ATTR_NEEDS_RESTART);
            String global = module.getValue (MODULE_ATTR_IS_GLOBAL);
            String eager = module.getValue (MODULE_ATTR_EAGER);
            String autoload = module.getValue (MODULE_ATTR_AUTOLOAD);
                        
            needsRestart = needsrestart == null || needsrestart.trim ().length () == 0 ? null : Boolean.valueOf (needsrestart);
            isGlobal = global == null || global.trim ().length () == 0 ? null : Boolean.valueOf (global);
            isEager = Boolean.parseBoolean (eager);
            isAutoload = Boolean.parseBoolean (autoload);
                        
            String licName = module.getValue (MODULE_ATTR_LICENSE);
            lic = UpdateLicense.createUpdateLicense (licName, null);
        }
        
        public void appendManifest (Attributes manifest) {
            specVersion = manifest.getValue (MANIFEST_ATTR_SPECIFICATION_VERSION);
            mf = getManifest (manifest);
            id = moduleCodeName + '_' + specVersion; // NOI18N
        }
        
        public void appendNotification (String notification) {
            this.notification = notification;
        }
        
        public String getId () {
            return id;
        }
        
        public UpdateItem createUpdateItem () {
            UpdateItem res = UpdateItem.createModule (
                    moduleCodeName,
                    specVersion,
                    distributionURL,
                    author,
                    downloadSize,
                    homepage,
                    publishDate,
                    group,
                    mf,
                    isEager,
                    isAutoload,
                    needsRestart,
                    isGlobal,
                    targetcluster,
                    lic);
            
            // read module notification
            UpdateItemImpl impl = Trampoline.SPI.impl(res);
            ((ModuleItem) impl).setModuleNotification (notification);
            
            // clean-up ModuleDescriptor
            cleanUp ();
            
            return res;
        }
        
        private void cleanUp (){
            this.specVersion = null;
            this.mf = null;
            this.notification = null;
        }
    }
    
    private static URL getDistribution (String distribution, URI base) {
        URL retval = null;
        if (distribution != null && distribution.length () > 0) {
            try {
                URI distributionURI = new URI (distribution);
                if (! distributionURI.isAbsolute ()) {
                    if (base != null) {
                        distributionURI = base.resolve (distributionURI);
                    }
                }
                retval = distributionURI.toURL ();
            } catch (MalformedURLException ex) {
                ERR.log (Level.INFO, null, ex);
            } catch (URISyntaxException ex) {
                ERR.log (Level.INFO, null, ex);
            }
        }
        return retval;
    }

    private static Manifest getManifest (Attributes attrList) {
        Manifest mf = new Manifest ();
        java.util.jar.Attributes mfAttrs = mf.getMainAttributes ();

        for (int i = 0; i < attrList.getLength (); i++) {
            mfAttrs.put (new java.util.jar.Attributes.Name (attrList.getQName (i)), attrList.getValue (i));
        }
        return mf;
    }

}
