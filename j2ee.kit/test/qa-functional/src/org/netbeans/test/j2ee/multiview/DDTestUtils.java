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

package org.netbeans.test.j2ee.multiview;

import java.awt.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import junit.framework.AssertionFailedError;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.DDBeanTableModel;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.FilterParamsPanel;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.InitParamsPanel;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Children;
import org.openide.nodes.Node;


/**
 *
 * @author jp159440
 */
public class DDTestUtils /*extends JellyTestCase*/{
    
    
    private DDDataObject ddObj;
    private WebApp webapp;
    private JellyTestCase testCase;
    
    /** Creates a new instance of DDTestUtils */
    public DDTestUtils(DDDataObject ddObj,JellyTestCase testCase) {
        this.ddObj = ddObj;
        webapp = ddObj.getWebApp();
        this.testCase = testCase;
    }
    
    public void testTable(DDBeanTableModel model,String[][] values) {
        testCase.assertEquals("Wrong count of table rows",values.length,model.getRowCount());
        for(int i = 0; i<model.getRowCount();i++) {
            this.testTableRow(model,i,values[i]);
        }
    }
    
    public void testTableRow(DDBeanTableModel model, int row, String[] values) {
        testCase.assertTrue("No such row in table model",model.getRowCount()>=row);
        int i;
        
        for(i=0;i<values.length;i++ ) {
            testCase.assertEquals("Value at "+row+","+i+" does not match.",values[i],model.getValueAt(row,i));
        }
    }
                
    public void setTableRow(DDBeanTableModel model, int row, Object[] values) throws Exception {
        for (int i = 0; i < values.length; i++) {
            model.setValueAt(values[i],row,i);
        }
    }
    
    public CommonDDBean getBeanByProp(CommonDDBean[] beans,String prop, Object value) {        
        for (int i = 0; i < beans.length; i++) {
            if(beans[i].getValue(prop).equals(value)) return beans[i];            
        }
        return null;        
    }
    
    public int getRowIndexByProp(DDBeanTableModel model, int col, Object value) {
        for(int i=0; i < model.getRowCount() ; i++ ) {
            if(model.getValueAt(i,col).equals(value)) return i;
        }
        return -1;
    }
    
    public DDBeanTableModel getModelByBean(Object bean) {
        JPanel panel = getInnerSectionPanel(bean);
        Component[] comp = panel.getComponents();
        DDBeanTableModel model = (DDBeanTableModel) ((DefaultTablePanel) comp[1]).getModel();
        return model;                
    }
    
    public void checkProperyGrpup(JspPropertyGroup prop,String name, String desc, String enc, String[] headers, String[] footers, String[] urls, boolean[] switches) {
        testCase.assertEquals("Display name doesn't match",name,prop.getDefaultDisplayName());
        testCase.assertEquals("Description doesn't match",desc,prop.getDefaultDescription());
        testCase.assertEquals("Encoding doesn't match",enc,prop.getPageEncoding());
        testStringArrayEquals(prop.getIncludeCoda(),footers);
        testStringArrayEquals(prop.getIncludePrelude(),headers);
        testStringArrayEquals(prop.getUrlPattern(),urls);
        testCase.assertEquals("ELIgnored: ",switches[0],prop.isElIgnored());
        testCase.assertEquals("XML syntax: ",switches[1],prop.isIsXml());
        testCase.assertEquals("Scripting invalid: ",switches[2],prop.isScriptingInvalid());
        JPanel panel = getInnerSectionPanel(prop);
        Component[] comp = panel.getComponents();
        testCase.assertEquals("Display name doesn't match.",name,((JTextField)comp[1]).getText());
        testCase.assertEquals("Description doesn't match.",desc,((JTextArea)comp[3]).getText());
        String tmp="";
        for (int i = 0; i < urls.length; i++) {
            if(i>0) tmp = tmp+", ";
            tmp = tmp+urls[i];
        }
        testCase.assertEquals("Url patterns doesn't match.",tmp,((JTextField)comp[5]).getText());
        testCase.assertEquals("Encoding doesn't match.",enc,((JTextField)comp[9]).getText());
        testCase.assertEquals("EL ignore doesn't match.",switches[0],((JCheckBox)comp[10]).isSelected());
        testCase.assertEquals("XML syntax doesn't match.",switches[1],((JCheckBox)comp[11]).isSelected());
        testCase.assertEquals("Script invalid doesn't match.",switches[2],((JCheckBox)comp[12]).isSelected());
        tmp="";
        for (int i = 0; i < headers.length; i++) {
            if(i>0) tmp = tmp+", ";
            tmp = tmp+headers[i];
        }
        testCase.assertEquals("Preludes doesn't match.",tmp,((JTextField)comp[14]).getText());
        tmp="";
        for (int i = 0; i < footers.length; i++) {
            if(i>0) tmp = tmp+", ";
            tmp = tmp+footers[i];
        }
        testCase.assertEquals("Codas doesn't match.",tmp,((JTextField)comp[17]).getText());
    }
    
    public DDBeanTableModel getServletInitParamsTableModel() {
        Servlet[] servlets = webapp.getServlet();
        testCase.assertEquals("Wrong count of servlets",1,servlets.length);
        JPanel panel = getInnerSectionPanel(servlets[0]);
        Component[] comp = panel.getComponents();
        InitParamsPanel tablePanel = ((InitParamsPanel)comp[17]);
        return (DDBeanTableModel) tablePanel.getTable().getModel();
    }
    
    public DDBeanTableModel getFilterInitParamsTableModel() {
        Filter[] filters = webapp.getFilter();
        testCase.assertEquals("Wrong count of filters",1,filters.length);
        JPanel panel = getInnerSectionPanel(filters[0]);
        Component[] comp = panel.getComponents();
        FilterParamsPanel tablePanel = ((FilterParamsPanel)comp[9]);
        return (DDBeanTableModel) tablePanel.getTable().getModel();
    }
    
    public void testProperties(CommonDDBean bean, String[] properties, Object[] values) throws Exception {
        for(int i=0;i<properties.length;i++) {
            testCase.assertEquals("Property "+properties[i]+" has wrong value.",values[i],bean.getValue(properties[i]));
        }
    }
    
    public void save() throws Exception {                
        new StepIterator() {  
            SaveCookie saveCookie;
            public boolean step() throws Exception {
                saveCookie = (SaveCookie) ddObj.getCookie(SaveCookie.class);
                return saveCookie!=null;
            }                     
        };
        SaveCookie saveCookie = (SaveCookie) ddObj.getCookie(SaveCookie.class);
        testCase.assertNotNull("Document was not modified",saveCookie);
        saveCookie.save();
    }
    
    public void traverse(Node n,String pref) {
        System.out.println(pref+n.getName()+" "+n.getClass().getName());
        Children ch = n.getChildren();
        Node[] nodes = ch.getNodes();
        for(int i = 0;i<nodes.length;i++ ) {
            traverse(nodes[i],pref+"  ");
        }
    }
    public static void waitForDispatchThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            return;
        }
        final boolean[] finished = new boolean[]{false};
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                finished[0] = true;
            }
        });
        new StepIterator() {
            public boolean step() throws Exception {
                return finished[0];
            }
        };
    }
    
    public JPanel getInnerSectionPanel(Object sectionPanel) {
        ToolBarMultiViewElement multi = ddObj.getActiveMVElement();
        SectionView section = multi.getSectionView();
        section.openPanel(sectionPanel);
        SectionPanel sp = section.findSectionPanel(sectionPanel);
        testCase.assertNotNull("Section panel "+sectionPanel+" not found",sp);
        JPanel p = sp.getInnerPanel();
        testCase.assertNotNull("Section panel has no inner panel",p);
        return p;
    }
    
    public void checkInDDXML(String findText) {
        checkInDDXML(findText,true);
    }
    
    public void checkInDDXML(String findText,boolean present) {        
        boolean matches = contains(findText);
        if(present) {            
            testCase.assertTrue("Cannot find correct element in XML view (editor document)",matches);
        } else {        
            testCase.assertFalse("Unexpected element found in XML view (editor document)",matches);
        }
    }   
    
    public String document() {
        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport)ddObj.getCookie(EditorCookie.class);
        javax.swing.text.Document document = editor.getDocument();
        try {
            String text = document.getText(0,document.getLength());
            return text;         
        } catch (javax.swing.text.BadLocationException ex) {
            throw new AssertionFailedErrorException("Failed to read the document: ",ex);
        }
    }
    
    public boolean contains(String findText) {
        waitForDispatchThread();
        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport)ddObj.getCookie(EditorCookie.class);
        javax.swing.text.Document document = editor.getDocument();
        try {
            String text = document.getText(0,document.getLength());
            Pattern p = Pattern.compile(findText,Pattern.DOTALL);
            Matcher m  = p.matcher(text.subSequence(0,text.length()-1));
            return m.matches();
        } catch (javax.swing.text.BadLocationException ex) {
            throw new AssertionFailedErrorException("Failed to read the document: ",ex);
        }
    }
    
    public void checkNotInDDXML(String findText) {
        checkInDDXML(findText,false);
    }
    
    public void testStringArrayEquals(String[] expected,String[] actual) {
        testCase.assertEquals("Wrong array size. ",expected.length,actual.length);
        for (int i = 0; i < expected.length; i++) {
            testCase.assertEquals("The "+i+" element has wrong value ",expected[i],actual[i]);            
        }
    }
    
    public void setText(JTextComponent component,String text) {
        component.requestFocus();
        waitForDispatchThread();
        Document doc = component.getDocument();
        try {
            doc.remove(0,doc.getLength());
            doc.insertString(0,text,null);
        } catch (BadLocationException ex) {
            testCase.fail(ex);
        }                
        ddObj.modelUpdatedFromUI();                                
        waitForDispatchThread();
    }
}
