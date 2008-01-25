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
 * EditControlOperator.java
 *
 */

package org.netbeans.test.umllib;
import java.awt.Frame;
import javax.swing.JComponent;
import javax.swing.JDialog;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.modules.uml.ui.controls.editcontrol.EditControlImpl;

/**
 * class handles current EditControl
 * @author psb
 */
public class EditControlOperator extends JDialogOperator {
    
    static{
        DriverManager.setKeyDriver(new KeyRobotDriver(new Timeout("autoDelay",50)));     
    }
    
    private JDialogOperator ecFrame=null;
    
    /** 
     * Creates a new instance of first visible EditControlOperator 
     */
    public EditControlOperator() {
       super(new checkForEditControlDialog());
       ecFrame=new JDialogOperator((JDialog)(this.getSource()));
    }
    /**
     * Creates a new instance of EditControlOperator
     * use case insensitive an
     * @param text in dialog
     */
     public EditControlOperator(String text) {
       super(new checkForEditControlDialogByText(text));
       ecFrame=new JDialogOperator((JDialog)(this.getSource()));
    }
    /**
     * Creates a new instance of EditControlOperator 
     * @param text in dialog
     * @param ce -compare exactly
     * @param cs - case sensitive
     */
     public EditControlOperator(String text, boolean ce, boolean cs) {
       super(new checkForEditControlDialogByText(text,ce,cs));
       ecFrame=new JDialogOperator((JDialog)(this.getSource()));
    }
   /**
     * Find textfield in edit control dialog
     * we assume only one textfield in dialog for now
     * @return JTextFieldOperator for first textfield in edit control dialog
     */
    public JTextFieldOperator getTextFieldOperator() {
        return new JTextFieldOperator(this);
    }
   /**
     * Find textfield in edit control dialog
     * we assume only one textfield in dialog for now
     * @return JTextFieldOperator for first textfield in edit control dialog
     */
    public JTextAreaOperator getTextAreaOperator() {
        return new JTextAreaOperator(this);
    }
    
    /**
     * TBD - check if there is TextAre instead of textfield
     * @param text 
     */
    public void typeText(String text){
        getTextFieldOperator().typeText(text);
        getTextFieldOperator().typeKey('\n');
    }
 
};


/**
 * Findder for Edit Control dialog (based on current realization)
 */
class checkForEditControlDialog implements ComponentChooser
{
    private String frameTitle=MainWindowOperator.getDefault().getTitle();
     /**
      * chooser with all default values
      */
     checkForEditControlDialog(){}
     //
    /**
     * 
     * @param comp 
     * @return 
     */
     public boolean checkComponent(java.awt.Component comp)
     {
         javax.swing.JDialog cmp=(javax.swing.JDialog)comp;
         JComponent ec=JComponentOperator.findJComponent(cmp,new checkETEditControlObject());
        return ec!=null && cmp.isShowing() && ((Frame)(comp.getParent())).getTitle().equals(frameTitle);
     }
    /**
     * 
     * @return 
     */
     public String	getDescription()
     {
         return "Try to find UML Edit Control Dialog.";
     }
}
class checkForEditControlDialogByText implements ComponentChooser
{
    private String text=null;
    private boolean cs=false;
    private boolean ce=false;
    private String frameTitle=MainWindowOperator.getDefault().getTitle();
   /**
     * find specific dialog with param name
     * @param txt 
     */
     checkForEditControlDialogByText(String txt)
     {
        text=txt;
     }
    /**
     * 
     * @param txt 
     * @param e 
     * @param s 
     */
     checkForEditControlDialogByText(String txt, boolean e, boolean s)
     {
         ce=e;
         cs=s;
        text=txt;
     }
     //
    /**
     * 
     * @param comp 
     * @return 
     */
     public boolean checkComponent(java.awt.Component comp)
     {
        javax.swing.JDialog cmp=(javax.swing.JDialog)comp;
        JComponent ec=JComponentOperator.findJComponent(cmp,new checkETEditControlObject());
         return ec!=null  && ((Frame)(comp.getParent())).getTitle().equals(frameTitle) && JTextFieldOperator.findJTextField(cmp,text,ce,cs)!=null;
     }
    /**
     * 
     * @return 
     */
     public String	getDescription()
     {
         return "Try to find UML Edit Control Dialog.";
     }
}

//-------------------
class checkETEditControlObject implements ComponentChooser
{
    /**
     * 
     * @param comp 
     * @return 
     */
    public boolean checkComponent(java.awt.Component comp)
    {
        return comp.isShowing() && (comp instanceof EditControlImpl);
    }
    /**
     * 
     * @return 
     */
     public String	getDescription()
     {
         return "Try to find showing ET EditControl Object.";
     }
}
