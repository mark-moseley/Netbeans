/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]".
 *
 * The Original Software is the Jemmy library. The Initial Developer of the
 * Original Software is Alexandre Iline. All Rights Reserved.
 *
 * ---------------------------------------------------------------------------
 *
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 */
package org.netbeans.jemmy.operators;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;


/**
 * A JUnit test for JTextFieldOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JTextFieldOperatorTest extends TestCase {
    /**
     * Stores the frame we use for testing.
     */
    private JFrame frame;
    
    /**
     * Stores the checkBox we use for testing.
     */
    private JTextField textField;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JTextFieldOperatorTest(String testName) {
        super(testName);
    }
    
    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        textField = new JTextField("JTextFieldOperatorTest");
        textField.setName("JTextFieldOperatorTest");
        frame.getContentPane().add(textField);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }
    
    /**
     * Cleanup after testing.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(false);
        frame.dispose();
        frame = null;
    }
    
    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(JTextFieldOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);

        JTextFieldOperator operator2 = new JTextFieldOperator(operator1);
        assertNotNull(operator2);
        
        JTextFieldOperator operator3 = new JTextFieldOperator(operator1, new NameComponentChooser("JTextFieldOperatorTest"));
        assertNotNull(operator3);

        JTextFieldOperator operator4 = new JTextFieldOperator(operator1, "JTextFieldOperatorTest");
        assertNotNull(operator4);
    }
    
    /**
     * Test findJTextField method.
     */
    public void testFindJTextField() {
        frame.setVisible(true);
        
        JTextField textField1 = JTextFieldOperator.findJTextField(frame, new NameComponentChooser("JTextFieldOperatorTest"));
        assertNotNull(textField1);
        
        JTextField textField2 = JTextFieldOperator.findJTextField(frame, "JTextFieldOperatorTest", false, false);
        assertNotNull(textField2);
    }
    
    /**
     * Test waitJTextField method.
     */
    public void testWaitJTextField() {
        frame.setVisible(true);
        
        JTextField textField1 = JTextFieldOperator.waitJTextField(frame, new NameComponentChooser("JTextFieldOperatorTest"));
        assertNotNull(textField1);
        
        JTextField textField2 = JTextFieldOperator.waitJTextField(frame, "JTextFieldOperatorTest", false, false);
        assertNotNull(textField2);
    }
    
    /**
     * Test waitText method.
     */
    public void testWaitText() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JTextFieldOperator operator2 = new JTextFieldOperator(operator1, new NameComponentChooser("JTextFieldOperatorTest"));
        assertNotNull(operator2);
        
        operator2.waitText("JTextFieldOperatorTest");
        assertEquals("JTextFieldOperatorTest", textField.getText());
        
        operator2.waitText("JTextFieldOperatorTest\n");
        assertEquals("JTextFieldOperatorTest", textField.getText());
    }
    
    /**
     * Test addActionListener method.
     */
    public void testAddActionListener() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JTextFieldOperator operator2 = new JTextFieldOperator(operator1, new NameComponentChooser("JTextFieldOperatorTest"));
        assertNotNull(operator2);
        
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            }
        };
        
        operator2.addActionListener(listener);
        assertEquals(listener, textField.getActionListeners()[0]);
        
        operator2.removeActionListener(listener);
        assertEquals(0, textField.getActionListeners().length);
    }
    
    /**
     * Test getColumns method.
     */
    public void testGetColumns() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JTextFieldOperator operator2 = new JTextFieldOperator(operator1, new NameComponentChooser("JTextFieldOperatorTest"));
        assertNotNull(operator2);
        
        operator2.setColumns(10);
        assertEquals(10, operator2.getColumns());
        assertEquals(textField.getColumns(), operator2.getColumns());
    }
    
    /**
     * Test of getHorizontalAlignment method, of class org.netbeans.jemmy.operators.JTextFieldOperator.
     */
    public void testGetHorizontalAlignment() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JTextFieldOperator operator2 = new JTextFieldOperator(operator1, new NameComponentChooser("JTextFieldOperatorTest"));
        assertNotNull(operator2);
        
        operator2.setHorizontalAlignment(SwingConstants.RIGHT);
        assertEquals(SwingConstants.RIGHT, operator2.getHorizontalAlignment());
        assertEquals(textField.getHorizontalAlignment(), operator2.getHorizontalAlignment());
    }
    
    /**
     * Test getScrollOffset method.
     *
     * @todo See why scroll offset is not working properly. What is the real
     *       contract?
     */
    public void testGetScrollOffset() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JTextFieldOperator operator2 = new JTextFieldOperator(operator1, new NameComponentChooser("JTextFieldOperatorTest"));
        assertNotNull(operator2);
        
        operator2.setScrollOffset(operator2.getScrollOffset());
    }
    
    /**
     * Test postActionEvent method.
     */
    public void testPostActionEvent() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JTextFieldOperator operator2 = new JTextFieldOperator(operator1, new NameComponentChooser("JTextFieldOperatorTest"));
        assertNotNull(operator2);
        
        operator2.setActionCommand("ACTION_COMMAND");
        
        ActionListener1 listener = new ActionListener1();
        operator2.addActionListener(listener);
        operator2.postActionEvent();
        assertEquals(listener.actionCommand, "ACTION_COMMAND");
    }
    
    /**
     * Inner class used for testing.
     */
    public class ActionListener1 implements ActionListener {
        public String actionCommand;
        
        public void actionPerformed(ActionEvent event) {
            actionCommand = event.getActionCommand();
        }
    }
    
    /**
     * Test getHorizontalVisiblity method.
     */
    public void testGetHorizontalVisibility() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);

        JTextFieldOperator operator2 = new JTextFieldOperator(operator1);
        assertNotNull(operator2);

        operator2.getHorizontalVisibility();
    }
}
