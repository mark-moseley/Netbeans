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

package org.netbeans.test.web;

import java.io.File;
import java.util.Arrays;
import javax.swing.JComboBox;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.modules.debugger.BreakpointsWindowOperator;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;

/**
 *
 * @author  lm97939
 */
public class Util {
    
    /** Creates a new instance of Util */
    public Util() {
    }
    
    public static String dumpProjectView(String project) {
        // TODO replace sleep()
        try { Thread.currentThread().sleep(3000); }
        catch (InterruptedException e) {}
        StringBuffer buff = new StringBuffer();
        Node node = new ProjectsTabOperator().getProjectRootNode(project);
        dumpNode(node, buff, 0);
        return buff.toString();
    }
    
    
    private static void dumpNode(Node node, StringBuffer buff, int level) {
        for (int i=0; i<level; i++)
            buff.append(".");
        buff.append("+ ");
//        if (!node.isLeaf()) buff.append("+ ");
//        else buff.append("- ");
        buff.append(node.getText());
        if (!node.isLeaf() && node.getText().indexOf('.') < 0) {
            buff.append(" ");
            boolean wasCollapsed = node.isCollapsed();
            buff.append("\n");
            String nodes[] = node.getChildren();
            for (int i=0; i<nodes.length; i++) {
                //XXX System.out.println("Parent:: " + node.getText() + " - subPath:: " + nodes[i]);
                Node child = new Node(node, nodes[i]);
                // prevents infinite loop in case the nodes[i].equals("");
                if (child.getPath().equals(node.getPath())) {
                    //XXX System.out.println("===Continue===");
                    continue;
                }
                dumpNode(child, buff,  level+1);
            }
            if (wasCollapsed) node.collapse();
        } else {
            buff.append("\n");
        }
    }
    
    public static String dumpFiles(File file) {
//        try { Thread.currentThread().sleep(3000); }
//        catch (InterruptedException e) {}
        StringBuffer buff = new StringBuffer();
        dumpFiles(file, buff, 0);
        return buff.toString();
    }
    
    private static void dumpFiles(File file, StringBuffer buff, int level) {
         for (int i=0; i<level; i++)
            buff.append(".");
         buff.append(file.getName());
         buff.append("\n");
         if (file.isDirectory()) {
             String files[] = file.list();
             Arrays.sort(files);
             for (int i=0; i<files.length; i++) {
                 dumpFiles(new File(file,files[i]), buff, level+1);
             }
         }
    }
    
    public static void setSwingBrowser() {
        // Set Swing HTML Browser as default browser
        OptionsOperator optionsOper = OptionsOperator.invoke();
        optionsOper.selectGeneral();
        // "Web Browser:"
        String webBrowserLabel = Bundle.getStringTrimmed(
                "org.netbeans.modules.options.general.Bundle",
                "CTL_Web_Browser");
        JLabelOperator jloWebBrowser = new JLabelOperator(optionsOper, webBrowserLabel);
        // "Swing HTML Browser"
        String swingBrowserLabel = Bundle.getString(
                "org.netbeans.core.ui.Bundle",
                "Services/Browsers/SwingBrowser.ser");
        new JComboBoxOperator((JComboBox)jloWebBrowser.getLabelFor()).
                selectItem(swingBrowserLabel);
        optionsOper.ok();
    }
    
    public static void deleteAllBreakpoints() {
        BreakpointsWindowOperator bwo = BreakpointsWindowOperator.invoke();
        bwo.deleteAll();
        bwo.close();
    }
    
    public static void stopTomcat() {
        getServerNode().stop();;
    }
    
    public static void startTomcat() {
        getServerNode().start();

    }
    
    private static final J2eeServerNode getServerNode() {
        RuntimeTabOperator.invoke();
        return new J2eeServerNode("Bundled Tomcat");
    }
    
    public static final void cleanStatusBar() {
        MainWindowOperator.getDefault().setStatusText("STATUS CLEANED");
    }
}
