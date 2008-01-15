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

package org.netbeans.modules.sql.framework.ui.editor.property.impl;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.logger.LogUtil;
import org.netbeans.modules.sql.framework.ui.editor.property.IResource;
import org.netbeans.modules.sql.framework.ui.editor.property.ITemplate;
import org.netbeans.modules.sql.framework.ui.editor.property.ITemplateGroup;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class TemplateManager {

    private static final String LOG_CATEGORY = TemplateManager.class.getName();
    private static transient final Logger mLogger = LogUtil.getLogger(TemplateManager.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private IResource rManager;
    private ITemplateGroup tg;

    /**
     * Creates a new instance of TemplateManager using the contents supplied by the
     * InputStream and IResource instance.
     * 
     * @param in InputStream containing template configuration
     * @param resource IResource instance
     */
    public TemplateManager(InputStream in, IResource resource) {
        rManager = resource;
        TemplateFactory fac = new TemplateFactory(rManager);
        init(new TemplateParser(in, fac), fac);
    }

    /**
     * Gets PropertyNode, if any, associated with the given name.
     * 
     * @param templateName name of template whose PropertyNode is sought
     * @return associated PropertyNode instance, or null if none exists
     */
    public PropertyNode getNodeForTemplateName(String templateName) {
        Map map = tg.getTemplates();
        Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            if(name.equals(templateName)){
                ITemplate template = (ITemplate) map.get(name);
                PropertyNode node = new PropertyNode(template);
                return node;
            }
        }
        return null;
    }

    /*
     * Performs common initialization tasks for overloaded constructors. @param parser
     * TemplateParser to use in parsing template configuration info @param fac
     * TemplateFactory to generate ITemplateGroup instance.
     */
    private void init(TemplateParser parser, TemplateFactory fac) {
        if (parser == null) {
            mLogger.infoNoloc(mLoc.t("PRSR137: TemplateParser is null{0}",LOG_CATEGORY));
           // Logger.print(Logger.DEBUG, LOG_CATEGORY, "init(TemplateParser)", "TemplateParser is null");
        }
        tg = fac.getTemplateGroup();
    }

}

