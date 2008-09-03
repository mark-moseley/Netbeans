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

package org.netbeans.modules.xslt.core.text.completion.support;

import org.xml.sax.InputSource;
import javax.xml.transform.Source;
import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.netbeans.spi.xml.cookies.*;
import org.netbeans.modules.xml.XMLDataObjectLook;
import org.netbeans.modules.xml.text.TextEditorSupport;
import org.netbeans.modules.xml.sync.*;
import org.netbeans.modules.xml.cookies.*;
import org.netbeans.modules.xml.api.XmlFileEncodingQueryImpl;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.modules.xslt.core.text.completion.support.cookies.ValidateXSLSupport;
import org.openide.util.NbBundle;

/**
 * XSL owner.
 *
 * @author Libor Kramolis
 * @author asgeir@dimonsoftware.com
 */
public final class XSLDataObject extends MultiDataObject implements XMLDataObjectLook {
    /** Serial Version UID */
    private static final long serialVersionUID = -3523066651187749549L;
    /** XSLT Mime Type. */
    public static final String MIME_TYPE = "application/xslt+xml"; // NOI18N    
    private static final String XSL_ICON_BASE =
        "org/netbeans/modules/xslt/core/resources/xslObject"; // NOI18N    
    private transient final DataObjectCookieManager cookieManager;
    private transient Synchronizator synchronizator;    
    
    public XSLDataObject(final FileObject obj, final UniFileLoader loader) throws DataObjectExistsException {
        super (obj, loader);

        CookieSet set = getCookieSet();
        cookieManager = new DataObjectCookieManager (this, set);
        set.add (cookieManager);
    
        // add check and validate cookies
        InputSource is = DataObjectAdapters.inputSource (this);
        set.add(new CheckXMLSupport (is));
        set.add(new ValidateXSLSupport (is));

        // add TransformableCookie
        Source source = DataObjectAdapters.source (this);
        set.add (new TransformableSupport (source));

        // editor support defines MIME type understood by EditorKits registry         
        TextEditorSupport.TextEditorSupportFactory editorFactory =
            new TextEditorSupport.TextEditorSupportFactory (this, XMLKit.MIME_TYPE);
        editorFactory.registerCookies (set);
		
    }

    @Override
    public final Lookup getLookup() {
        return Lookups.fixed(new Object[] {
            super.getLookup(), this, XmlFileEncodingQueryImpl.singleton()});
    }

    @Override
    protected Node createNodeDelegate () {
        return new XSLDataNode (this);
    }

    @Override
    public HelpCtx getHelpCtx() {
        //return new HelpCtx (XSLDataObject.class);
        return HelpCtx.DEFAULT_HELP;
    }
    
    // XMLDataObjectLook to be deprecated ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public DataObjectCookieManager getCookieManager() {
        return cookieManager;
    }

    public synchronized Synchronizator getSyncInterface() {
        if (synchronizator == null) {
            synchronizator = new DataObjectSyncSupport (XSLDataObject.this);
        }
        return synchronizator;
    }
    
    /**
     * Redefine icon and help.
     */
    private static class XSLDataNode extends DataNode {

        /** Create new XSLDataNode. */
        public XSLDataNode (XSLDataObject obj) {
            super (obj, Children.LEAF);
            setIconBase (XSL_ICON_BASE);
            setShortDescription(NbBundle.getMessage(XSLDataObject.class, "PROP_XSLDataNode_desc"));
        }

        @Override
        public HelpCtx getHelpCtx() {
            //return new HelpCtx (XSLDataObject.class);
            return HelpCtx.DEFAULT_HELP;
        }
        
    }

}
