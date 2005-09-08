/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Marian Petras
 */
final class TestMethodNode extends AbstractNode {
    
    /** */
    private final Report.Testcase testcase;
    
    /**
     * Creates a new instance of TestcaseNode
     */
    TestMethodNode(final Report.Testcase testcase) {
        super(testcase.trouble != null
              ? new TestMethodNodeChildren(testcase)
              : Children.LEAF);

        this.testcase = testcase;

        setDisplayName();
        setIconBaseWithExtension(
                "org/netbeans/modules/junit/output/res/method.gif");    //NOI18N
    }
    
    /**
     */
    private void setDisplayName() {
        final int status = (testcase.trouble == null)
                           ? 0
                           : testcase.trouble.isError() ? 1 : 2;
        
        if ((status == 0) && (testcase.timeMillis < 0)) {
            setDisplayName(testcase.name);
            return;
        }
        
        String[] noTimeKeys = new String[] {
                                      null,
                                      "MSG_TestMethodError",            //NOI18N
                                      "MSG_TestMethodFailed"};          //NOI18N
        String[] timeKeys = new String[] {
                                      "MSG_TestMethodPassed_time",      //NOI18N
                                      "MSG_TestMethodError_time",       //NOI18N
                                      "MSG_TestMethodFailed_time"};     //NOI18N
        setDisplayName(
                testcase.timeMillis < 0
                ? NbBundle.getMessage(getClass(),
                                      noTimeKeys[status],
                                      testcase.name)
                : NbBundle.getMessage(getClass(),
                                      timeKeys[status],
                                      testcase.name,
                                      new Float(testcase.timeMillis/1000f)));
    }
    
    /**
     */
    public String getHtmlDisplayName() {
        final int status = (testcase.trouble == null)
                           ? 0
                           : testcase.trouble.isError() ? 1 : 2;
        String[] noTimeKeys = new String[] {
                                      "MSG_TestMethodPassed_HTML",      //NOI18N
                                      "MSG_TestMethodError_HTML",       //NOI18N
                                      "MSG_TestMethodFailed_HTML"};     //NOI18N
        String[] timeKeys = new String[] {
                                      "MSG_TestMethodPassed_HTML_time", //NOI18N
                                      "MSG_TestMethodError_HTML_time",  //NOI18N
                                      "MSG_TestMethodFailed_HTML_time"};//NOI18N
                                          
        StringBuffer buf = new StringBuffer(60);
        buf.append(testcase.name);
        buf.append("&nbsp;&nbsp;");                                     //NOI18N
        buf.append("<font color='#");                                   //NOI18N
        buf.append(testcase.trouble != null ? "FF0000'>" : "00CC00'>"); //NOI18N
        buf.append(testcase.timeMillis < 0
                   ? NbBundle.getMessage(getClass(),
                                         noTimeKeys[status])
                   : NbBundle.getMessage(getClass(),
                                         timeKeys[status],
                                         new Float(testcase.timeMillis/1000f)));
        buf.append("</font>");                                          //NOI18N
        return buf.toString();
    }
    
    /**
     */
    public Action getPreferredAction() {
        Report.Trouble trouble = testcase.trouble;
        String callstackFrameInfo =
                ((trouble != null)
                        && (trouble.stackTrace != null)
                        && (trouble.stackTrace.length != 0))
                ? trouble.stackTrace[0]
                : null;
        
        return new JumpAction(this, callstackFrameInfo);
    }
    
}
