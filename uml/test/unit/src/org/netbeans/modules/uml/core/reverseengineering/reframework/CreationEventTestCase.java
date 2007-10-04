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


package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.Document;
import org.dom4j.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * Test cases for CreationEvent.
 */
public class CreationEventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(CreationEventTestCase.class);
    }

    private ICreationEvent ce;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        ce = new CreationEvent();
        Document doc = XMLManip.getDOMDocument();
        Element el = XMLManip.createElement(doc, "UML:Event");
        Element op = XMLManip.createElement(el, "UML:OutputPin");
        op.addAttribute("value", "Thurman");
        el.addAttribute("classifier", "Zelazny");
        
        XMLManip.createElement(el, "UML:Enumeration");
        
        Element toks = XMLManip.createElement(el, "TokenDescriptors");
        Element td   = XMLManip.createElement(toks, "TDescriptor");
        td.addAttribute("value", "Cthulhu");
        td.addAttribute("type", "InstantiatedTypeName");

        Element tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "true");
        tdp.addAttribute("type", "IsPrimitive");

        ce.setEventData(el);
    }

    public void testGetInstanceName()
    {
        assertEquals("Thurman", ce.getInstanceName());
    }

    public void testGetInstanceTypeName()
    {
        assertEquals("Zelazny", ce.getInstanceTypeName());
    }

    public void testGetInstantiatedTypeName()
    {
        assertEquals("Cthulhu", ce.getInstantiatedTypeName());
    }

    public void testGetIsPrimitive()
    {
        assertTrue(ce.getIsPrimitive());
    }

    public void testGetIsStatic()
    {
        assertFalse(ce.getIsStatic());
    }

    public void testGetREClass()
    {
        assertNotNull(ce.getREClass());
    }
}