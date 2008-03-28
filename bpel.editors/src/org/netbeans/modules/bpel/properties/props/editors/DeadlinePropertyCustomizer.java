/*
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
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.properties.props.editors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.soa.ui.form.ValidablePropertyCustomizer;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.soa.ui.form.RangeIntegerDocument;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager.ValidStateListener;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.bpel.properties.props.PropertyVetoError;
import org.netbeans.modules.bpel.editors.api.utils.DurationUtil;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;

/**
 *
 * @author nk160297
 */
public class DeadlinePropertyCustomizer extends ValidablePropertyCustomizer
        implements PropertyChangeListener {
    
    private static final long serialVersionUID = 1L;
    private Timer inputDelayTimer;
    
    private PropertyEditor myPropertyEditor;
    
    /** Creates new form StringPropertyCustomizer */
    public DeadlinePropertyCustomizer() {
        super();
        //
        initComponents();
        //
        getValidStateManager(true).addValidStateListener(new ValidStateListener() {
            public void stateChanged(ValidStateManager source, boolean isValid) {
                if (source.isValid()) {
                    lblErrorMessage.setText("");
                } else {
                    lblErrorMessage.setText(source.getHtmlReasons());
                }
            }
        });
        //
        fldYear.setDocument(new RangeIntegerDocument(
                Integer.MIN_VALUE, Integer.MAX_VALUE));
        fldMonth.setDocument(new RangeIntegerDocument(1, 12));
        fldDay.setDocument(new RangeIntegerDocument(1, 31));
        fldHour.setDocument(new RangeIntegerDocument(0, 23));
        fldMinute.setDocument(new RangeIntegerDocument(0, 59));
        fldSecond.setDocument(new RangeIntegerDocument(0, 59));
        //
        ActionListener timerListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                revalidate(true);
            }
        };
        inputDelayTimer = new Timer(Constants.INPUT_VALIDATION_DELAY, timerListener);
        inputDelayTimer.setCoalesce(true);
        inputDelayTimer.setRepeats(false);
        //
        DocumentListener docListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                inputDelayTimer.restart();
            }
            public void insertUpdate(DocumentEvent e) {
                inputDelayTimer.restart();
            }
            public void removeUpdate(DocumentEvent e) {
                inputDelayTimer.restart();
            }
        };
        //
        fldYear.getDocument().addDocumentListener(docListener);
        fldMonth.getDocument().addDocumentListener(docListener);
        fldDay.getDocument().addDocumentListener(docListener);
        fldHour.getDocument().addDocumentListener(docListener);
        fldMinute.getDocument().addDocumentListener(docListener);
        fldSecond.getDocument().addDocumentListener(docListener);
        //
        FocusListener fl = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                inputDelayTimer.stop();
                revalidate(true);
            }
        };
        //
        fldYear.addFocusListener(fl);
        fldMonth.addFocusListener(fl);
        fldDay.addFocusListener(fl);
        fldHour.addFocusListener(fl);
        fldMinute.addFocusListener(fl);
        fldSecond.addFocusListener(fl);
        //
        HelpCtx.setHelpIDString(this, this.getClass().getName());
        //
        SoaUiUtil.activateInlineMnemonics(this);
    }
    
    @Override
    public synchronized void init(
            PropertyEnv propertyEnv, PropertyEditor propertyEditor) {
        assert propertyEnv != null && propertyEditor != null : "Wrong params"; // NOI18N
        //
        if (myPropertyEnv == propertyEnv) {
            return; // Prevent repeated initialization
        }
        //
        if (myPropertyEnv != null) {
            myPropertyEnv.removePropertyChangeListener(this);
        }
        //
        myPropertyEditor = propertyEditor;
        //
        super.init(propertyEnv, propertyEditor);
        //
        myPropertyEnv.addPropertyChangeListener(this);
        //
        // The Ok button will not work without the following line!!!
        myPropertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        //
        String value = propertyEditor.getAsText();
        //
        value = DurationUtil.removeQuotes(value);
        //
        parseUntil(value);
        revalidate(true);
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        if (PropertyEnv.PROP_STATE.equals(event.getPropertyName()) &&
                event.getNewValue() == PropertyEnv.STATE_VALID) {
            String currText = DurationUtil.addQuotes(getContent());
            try {
                myPropertyEditor.setAsText(currText);
            } catch (PropertyVetoError ex) {
                myPropertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
                PropertyVetoError.defaultProcessing(ex);
            }
        }
    }
    
    private void parseUntil(String value) {
        Date date = null;
        try {
            date = parseDate(value);
        } catch (ParseException e) {
            // do nothing
        }
        //
        if (date == null) {
            date = new Date(System.currentTimeMillis());
        }
        //
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        //
        fldYear.setText(Integer.toString(calendar.get(Calendar.YEAR)));
        fldMonth.setText(Integer.toString(calendar.get(Calendar.MONTH) + 1));
        fldDay.setText(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
        fldHour.setText(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
        fldMinute.setText(Integer.toString(calendar.get(Calendar.MINUTE)));
        fldSecond.setText(Integer.toString(calendar.get(Calendar.SECOND)));
    }
    
    private Date parseDate(String value) throws ParseException {
        if (value == null || value.length() == 0) {
            return null;
        }
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // NOI18N
        dateFormat.setLenient(false);
        
        return dateFormat.parse(
                value.replace(DurationUtil.T_DELIM.charAt(0), ' '));
    }
    
    private String getContent() {
        return DurationUtil.getContent(false,
                DurationUtil.parseInt(fldYear.getText()),
                DurationUtil.parseInt(fldMonth.getText()),
                DurationUtil.parseInt(fldDay.getText()),
                DurationUtil.parseInt(fldHour.getText()),
                DurationUtil.parseInt(fldMinute.getText()),
                DurationUtil.parseDouble(fldSecond.getText()));
    }
    
    public Validator createValidator() {
        return new MyValidator(this);
    }
    
    private class MyValidator extends DefaultValidator {
        
        public MyValidator(ValidStateManager.Provider vsmProvider) {
            super(vsmProvider, ErrorMessagesBundle.class);
        }
        
        public void doFastValidation() {
            String param = DurationUtil.getParseUntil(
                    fldYear.getText().trim(),
                    fldMonth.getText().trim(),
                    fldDay.getText().trim(),
                    fldHour.getText().trim(),
                    fldMinute.getText().trim(),
                    fldSecond.getText().trim()
                    );
            //
            try {
                parseDate(param);
            } catch (ParseException e) {
                addReasonKey(Severity.ERROR, "ERR_INCORRECT_DATE_TIME", param); // NOI18N
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblYear = new javax.swing.JLabel();
        fldYear = new javax.swing.JTextField();
        lblMonth = new javax.swing.JLabel();
        fldMonth = new javax.swing.JTextField();
        lblDay = new javax.swing.JLabel();
        fldDay = new javax.swing.JTextField();
        lblHour = new javax.swing.JLabel();
        fldHour = new javax.swing.JTextField();
        lblMinute = new javax.swing.JLabel();
        fldMinute = new javax.swing.JTextField();
        lblSecond = new javax.swing.JLabel();
        fldSecond = new javax.swing.JTextField();
        lblErrorMessage = new javax.swing.JLabel();

        lblYear.setLabelFor(fldYear);
        lblYear.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Year"));
        lblYear.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Year"));
        lblYear.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Year"));

        fldYear.setColumns(4);

        lblMonth.setLabelFor(fldMonth);
        lblMonth.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Month"));
        lblMonth.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Month"));
        lblMonth.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Month"));

        fldMonth.setColumns(4);

        lblDay.setLabelFor(fldDay);
        lblDay.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Day"));
        lblDay.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Day"));
        lblDay.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Day"));

        fldDay.setColumns(4);

        lblHour.setLabelFor(fldHour);
        lblHour.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Hour"));
        lblHour.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Hour"));
        lblHour.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Hour"));

        fldHour.setColumns(4);

        lblMinute.setLabelFor(fldMinute);
        lblMinute.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Minute"));
        lblMinute.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Minute"));
        lblMinute.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Minute"));

        fldMinute.setColumns(4);

        lblSecond.setLabelFor(fldSecond);
        lblSecond.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Second"));
        lblSecond.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Second"));
        lblSecond.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Second"));

        fldSecond.setColumns(4);

        lblErrorMessage.setForeground(new java.awt.Color(255, 0, 0));
        lblErrorMessage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_ErrorLabel"));
        lblErrorMessage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_ErrorLabel"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lblYear)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lblHour))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fldYear, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                            .add(fldHour, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lblMonth)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lblMinute))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fldMonth, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                            .add(fldMinute, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lblDay)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lblSecond))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fldDay, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
                            .add(fldSecond, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblYear)
                    .add(fldYear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblMonth)
                    .add(lblDay)
                    .add(fldDay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fldMonth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblHour)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(lblMinute)
                        .add(lblSecond)
                        .add(fldHour, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(fldSecond, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(fldMinute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField fldDay;
    private javax.swing.JTextField fldHour;
    private javax.swing.JTextField fldMinute;
    private javax.swing.JTextField fldMonth;
    private javax.swing.JTextField fldSecond;
    private javax.swing.JTextField fldYear;
    private javax.swing.JLabel lblDay;
    private javax.swing.JLabel lblErrorMessage;
    private javax.swing.JLabel lblHour;
    private javax.swing.JLabel lblMinute;
    private javax.swing.JLabel lblMonth;
    private javax.swing.JLabel lblSecond;
    private javax.swing.JLabel lblYear;
    // End of variables declaration//GEN-END:variables
    
}
