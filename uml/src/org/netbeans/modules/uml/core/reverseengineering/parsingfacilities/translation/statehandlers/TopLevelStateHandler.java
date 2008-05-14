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


/*
 * File       : TopLevelStateHandler.java
 * Created on : Dec 8, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dom4j.*;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Aztec
 */
public class TopLevelStateHandler extends StateHandler
{
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.uml.core");
    String m_Language = null;

    public TopLevelStateHandler(String language)
    {
        m_Language = language;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ITopLevelStateHandler#createTopLevelNode(java.lang.String)
     */
    public void createTopLevelNode(String nodeName)
    {
        if(nodeName == null) return;
        
        Document pDoc = XMLManip.getDOMDocument();
        if(pDoc != null)
        {

            Node pNewNode = createNode(pDoc, nodeName);

            if(pNewNode != null)
            {
                Element element = (pNewNode instanceof Element)?
                                    (Element)pNewNode : null;
                if(element != null)
                {
                    XMLManip.setAttributeValue(element, 
                                                "language", 
                                                getLanguage());
                }

                setDOMNode(pNewNode);
            }
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ITopLevelStateHandler#writeDocument(java.lang.String)
     */
    public void writeDocument(String filename)
    {
        BufferedWriter buffWriter = null;
        try
        {
            String xml = getDOMNode().getDocument().asXML();
            File f = new File(filename);
            FileObject fo = FileUtil.createData(f);
            if(fo != null)
            {
                OutputStream fos = fo.getOutputStream();
                buffWriter = new BufferedWriter(new OutputStreamWriter(fos));
                buffWriter.write(xml);
                buffWriter.flush();
            }
        }
        catch (IOException e)
        {
            String mesg = e.getMessage();
            logger.log(Level.WARNING, mesg != null ? mesg : "", e);
        }
        finally 
        {
            if (buffWriter != null) 
            { 
                try {
                    buffWriter.close();
                } catch (IOException ex) {
                    logger.log(Level.INFO, ex.getMessage());
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ITopLevelStateHandler#getLanguage()
     */
    public String getLanguage()
    {
        return m_Language;
    }

}
