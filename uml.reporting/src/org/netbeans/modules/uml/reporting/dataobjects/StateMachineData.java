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


package org.netbeans.modules.uml.reporting.dataobjects;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateMachine;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.reporting.ReportTask;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class StateMachineData extends ClassData
{
    private IStateMachine element;
    
    /** Creates a new instance of StateMachineData */
    public StateMachineData()
    {
    }
    
    
    public void setElement(IElement e)
    {
        if (e instanceof IStateMachine)
            this.element = (IStateMachine)e;
    }
    
    public IStateMachine getElement()
    {
        return this.element;
    }
    
    
    private String getSummaryTable(ETList list)
    {
        StringBuilder buff = new StringBuilder();
        
        for (int i=0; i<list.size(); i++)
        {
            INamedElement node = (INamedElement)list.get(i);
            buff.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
            
            String doc = getBriefDocumentation(node.getDocumentation());
            
            if (doc == null || doc.trim().equals("")) // NOI18N
                doc = "&nbsp;"; // NOI18N
            buff.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
            buff.append("<TD WIDTH=\"15%\"><B><A HREF=\"" + getLinkTo(node) + // NOI18N
                    "\" title=\"" + node.getElementType() + " in " + node.getName() + // NOI18N
                    "\">" + node.getName() + "</A></B></TD>\r\n"); // NOI18N
            buff.append("<TD>" + doc + "</TD>\r\n"); // NOI18N
            buff.append("</TR>\r\n"); // NOI18N
        }
        
        buff.append("</TABLE>\r\n&nbsp;\r\n"); // NOI18N
        return buff.toString();
    }
    
    protected String[] getPropertyNames()
    {
        return new String[] {
            Property_Alias,
            Property_Visibility,
            Property_Final,
            Property_Transient,
            Property_Abstract,
            Property_Leaf,
            Property_Reentrant,
        };
    }
    
    protected Object[] getPropertyValues()
    {
        Boolean isFinal = new Boolean(getElement().getIsFinal());
        Boolean isTransient = new Boolean(getElement().getIsTransient());
        Boolean isAbstract = new Boolean(getElement().getIsAbstract());
        Boolean isLeaf = new Boolean(getElement().getIsLeaf());
        Boolean isReentrant = new Boolean(getElement().getIsReentrant());
        
        return new Object[] {getElement().getAlias(),
        getVisibility(getElement()), isFinal,
        isTransient, isAbstract, isLeaf, isReentrant};
    }
    
    public boolean toReport(File file)
    {
        if (getElement()==null)
            return false;
        
        ETList<IState> states = getElement().getSubmachinesStates();
        ETList<INamedElement> elements = getElement().getContainedElements();
        String doc = ""; // NOI18N
        boolean result = false;
        
        try
        {
            FileOutputStream fo = new FileOutputStream(file);
            OutputStreamWriter out = new OutputStreamWriter(fo, ENCODING);
            
            out.write(getHTMLHeader());
            out.write("<BODY BGCOLOR=\"white\">\r\n"); // NOI18N
            out.write(getNavBar());
            out.write("<HR>\r\n"); // NOI18N
            out.write("<H2>\r\n"); // NOI18N
            out.write("<FONT SIZE=\"-1\">" + getOwningPackageName() + "</FONT>\r\n"); // NOI18N
            out.write("<BR>\r\n"); // NOI18N
            out.write(getElementType() + " " + getElement().getName() + "</H2>\r\n"); // NOI18N
            
            out.write(getDependencies());
            out.write(getEnclosingDiagrams());
            out.write(getDocumentation());

            // property summary
            out.write(getProperties());
            
            // diagram summary
            out.write(getDiagramSummary());
            // stereotype summary
            out.write(getStereoTypesSummary());
            
            // tagged value summary
            out.write(getTaggedValueSummary());
            
            // constraint summary
            out.write(getConstraintsSummary());
            
            // sub machine state summary
            if (states.size()>0)
            {
                out.write("<!-- =========== SUBMACHINE STATE SUMMARY =========== -->\r\n\r\n"); // NOI18N
                
                out.write(getSummaryHeader("sub_machine_state_summary", // NOI18N
                    NbBundle.getMessage(StateMachineData.class, "Sub_Machine_State_Summary"))); // NOI18N
                
                for (int i=0; i<states.size(); i++)
                {
                    IState state = (IState)states.get(i);
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
                    
                    doc = getBriefDocumentation(state.getDocumentation());
                    
                    if (doc == null || doc.trim().equals("")) // NOI18N
                        doc = "&nbsp;"; // NOI18N
                    
                    String type = state.getElementType().replaceAll(" ", ""); // NOI18N
                    String name = state.getName();
                    
                    if (name==null || name.equals("")) // NOI18N
                        name = state.getElementType();
                
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
                    
                    String imageName = CommonResourceManager.instance()
                        .getIconDetailsForElementType(state.getElementType());

                    if (imageName.lastIndexOf("/") > -1)
                        imageName = imageName.substring(imageName.lastIndexOf("/")+1); // NOI18N
                    
                    ReportTask.addToImageList(imageName);

                    out.write("<TD WIDTH=\"15%\"><img src=\"" + // NOI18N
                        ReportTask.getPathToReportRoot(getElement()) + 
                        "images/" + imageName + // NOI18N
                        "\" border=n>&nbsp<B><A HREF=\"" + getLinkTo(state) + // NOI18N
                        "\" title=\"" + state.getElementType() + " in " + name + // NOI18N
                        "\">" + name + "</A></B></TD>\r\n"); // NOI18N

                    out.write("<TD>" + doc + "</TD>\r\n"); // NOI18N
                    out.write("</TR>\r\n"); // NOI18N
                }

                out.write("</TABLE>\r\n&nbsp;\r\n"); // NOI18N
            }
            
            
            // contained elements summary
            if (elements!=null && elements.size()>0)
            {
                out.write("<!-- =========== CONTAINED ELEMENTS SUMMARY =========== -->\r\n\r\n"); // NOI18N
                
                out.write(getSummaryHeader("contained_element_summary", // NOI18N
                    NbBundle.getMessage(StateMachineData.class, "Contained_Element_Summary"))); // NOI18N
                
                out.write(getSummaryTable(elements));
            }
            
            out.write("<HR>\r\n"); // NOI18N
            out.write(getNavBar());
            out.write("</BODY>\r\n</HTML>"); // NOI18N
            out.close();
            result = true;
        }

        catch (Exception e)
        {
            Logger.getLogger(ElementDataObject.class.getName()).log(
                    Level.SEVERE, getElement().getElementType() + " - " +  getElement().getNameWithAlias(), e);
            result = false;
        }
        return result;
    }
}
