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
/*
 * Created on August 8, 2004, 1:47 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.EventObject;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Appclient;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Application;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Connector;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Ejb;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.StaticVerification;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning;
import org.netbeans.modules.j2ee.sun.dd.impl.verifier.Web;
import org.openide.ErrorManager;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.Mode;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.sun.ide.j2ee.db.ExecSupport;

/**
 * Main TopComponent to display the output of the Sun J2EE Verifier Tool from an archive file.
 * @author ludo
 */
public class VerifierSupport extends TopComponent{
    String _archiveName;
    
    final static int FAIL = 0;
    final static int WARN = 1;
    final static int ALL  = 2;
    
    static String allString = NbBundle.getMessage(VerifierSupport.class,"All_Results");     // NOI18N
    static String failString = NbBundle.getMessage(VerifierSupport.class,"Failures_Only");      //NOI18N
    static String warnString = NbBundle.getMessage(VerifierSupport.class,"Failures_and_Warnings_only");     //NOI18N
    
    // Strings used for 508 compliance
    static String radioButtonName =NbBundle.getMessage(VerifierSupport.class,"Radio_Button");       // NOI18N
    static String radioButtonDesc = NbBundle.getMessage(VerifierSupport.class,"RadioButtonToSelect");       // NOI18N
    static String panelName =NbBundle.getMessage(VerifierSupport.class,"Panel");        // NOI18N
    static String panelDesc =NbBundle.getMessage(VerifierSupport.class,"VerifierPanel");        //NOI18N
    
    JRadioButton allButton ;
    JRadioButton failButton ;
    JRadioButton warnButton ;
    RadioListener myListener ;
    //what shoudl be displayed:ALL, FAIL, WARN
    int statusLeveltoDisplay = ALL;//by default
    boolean verifierIsStillRunning = true; //needed for the ui to know if a status message has to be printed or not...
    JPanel controlPanel;
    JPanel resultPanel;
    JTable table ;
    DefaultTableModel tableModel;
    ListSelectionListener tableSelectionListener;
    JScrollPane tableScrollPane;
    JScrollPane textScrollPane;
    JTextArea detailText;
    
    private static String STATUS_LIT = "Status"; // NOI18N
    
    final String[] columnNames = {
        NbBundle.getMessage(VerifierSupport.class,STATUS_LIT),
        NbBundle.getMessage(VerifierSupport.class,"Test_Description"),      // NOI18N
        NbBundle.getMessage(VerifierSupport.class,"Result")};       // NOI18N
    private Vector passResults = new Vector();
    private Vector failResults = new Vector();
    private Vector errorResults = new Vector();
    private Vector warnResults = new Vector();
    private Vector naResults = new Vector();
    private Vector notImplementedResults = new Vector();
    private Vector notRunResults = new Vector();
    private Vector defaultResults = new Vector();
    
    public static  void launchVerifier(final String fileName, OutputStream outs){
        final File f = new File(fileName);
        final File dir = f.getParentFile();
        final VerifierSupport verifierSupport=new VerifierSupport(fileName);
        
        File irf = org.netbeans.modules.j2ee.sun.api.ServerLocationManager.getLatestPlatformLocation();
        if (null == irf || !irf.exists()) {
            org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util.showWarning(NbBundle.getMessage(VerifierSupport.class, "ERR_CannotFind"));// NOI18N
            return;
        }
        String installRoot = irf.getAbsolutePath();
        SwingUtilities.invokeLater( new Runnable(){
            public void run() {
                verifierSupport.initUI();
                verifierSupport.showInMode();
            }
        });
        
        try{
            String cmd = installRoot+File.separator+"bin"+File.separator+"verifier";//NOI18N
            if (File.separatorChar != '/') {
                cmd =cmd + ".bat";      // NOI18N
            }
            Runtime rt = Runtime.getRuntime();
            String arr[] = {cmd, "-ra", "-d" , dir.getAbsolutePath(), fileName};//NOI18N
            
            String cmdName="";      // NOI18N
            for (int j=0;j<arr.length;j++){
                cmdName= cmdName+arr[j]+" ";        // NOI18N
            }
            System.out.println(NbBundle.getMessage(VerifierSupport.class,"running_", cmdName));     // NOI18N
            final Process child = rt.exec(arr);
            
            //
            // Attach to the process's stdout, and ignore what comes back.
            //
            final Thread[] copyMakers = new Thread[2];
            OutputStreamWriter oss=null;
            if (outs!=null) {
                oss=new OutputStreamWriter(outs);
            }
            (copyMakers[0] = new ExecSupport.OutputCopier(new InputStreamReader(child.getInputStream()), oss, true)).start();
            (copyMakers[1] = new ExecSupport.OutputCopier(new InputStreamReader(child.getErrorStream()), oss, true)).start();
            try {
                child.waitFor();
                Thread.sleep(1000);  // time for copymakers
            } catch (InterruptedException e) {
            } finally {
                try {
                    copyMakers[0].interrupt();
                    copyMakers[1].interrupt();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String onlyJarFile   = f.getName();
        File ff = new File(dir, onlyJarFile+".xml");        // NOI18N
        FileInputStream in = null;
        StaticVerification sv = null;
        org.netbeans.modules.j2ee.sun.dd.impl.verifier.Error err = null;
        try {
            in = new FileInputStream(ff);
            
            sv = StaticVerification.createGraph(in);  // this can throw a RT exception
            err = sv.getError();
            if (err!=null){
                verifierSupport.saveErrorResultsForDisplay( err);
                
            }
            Ejb e = sv.getEjb();
            if (e!=null){
                Failed fail= e.getFailed();
                if (fail!=null){
                    Test t[] =fail.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveFailResultsForDisplay( t[i]);
                    }
                }
                Warning w= e.getWarning();
                if (w!=null){
                    Test t[] =w.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveWarnResultsForDisplay( t[i]);
                    }
                }
                Passed p= e.getPassed();
                if (p!=null){
                    Test t[] =p.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.savePassResultsForDisplay(t[i]);
                    }
                }
                NotApplicable na= e.getNotApplicable();
                if (na!=null){
                    Test t[] =na.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveNaResultsForDisplay( t[i]);
                    }
                }
            }
            Web we = sv.getWeb();
            if (we!=null){
                Failed fail= we.getFailed();
                if (fail!=null){
                    Test t[] =fail.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveFailResultsForDisplay(t[i]);
                    }
                }
                Warning w= we.getWarning();
                if (w!=null){
                    Test t[] =w.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveWarnResultsForDisplay(t[i]);
                    }
                }
                Passed p= we.getPassed();
                if (p!=null){
                    Test t[] =p.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.savePassResultsForDisplay(t[i]);
                    }
                }
                NotApplicable na= we.getNotApplicable();
                if (na!=null){
                    Test t[] =na.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveNaResultsForDisplay(t[i]);
                    }
                }
            }
            Appclient ac = sv.getAppclient();
            if (ac!=null){
                Failed fail= ac.getFailed();
                if (fail!=null){
                    Test t[] =fail.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveFailResultsForDisplay(t[i]);
                    }
                }
                Warning w= ac.getWarning();
                if (w!=null){
                    Test t[] =w.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveWarnResultsForDisplay(t[i]);
                    }
                }
                Passed p= ac.getPassed();
                if (p!=null){
                    Test t[] =p.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.savePassResultsForDisplay(t[i]);
                    }
                }
                NotApplicable na= ac.getNotApplicable();
                if (na!=null){
                    Test t[] =na.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveNaResultsForDisplay(t[i]);
                    }
                }
            }
            Application  app = sv.getApplication();
            if (app!=null){
                Failed fail= app.getFailed();
                if (fail!=null){
                    Test t[] =fail.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveFailResultsForDisplay(t[i]);
                    }
                }
                Warning w= app.getWarning();
                if (w!=null){
                    Test t[] =w.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveWarnResultsForDisplay(t[i]);
                    }
                }
                Passed p= app.getPassed();
                if (p!=null){
                    Test t[] =p.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.savePassResultsForDisplay(t[i]);
                    }
                }
                NotApplicable na= app.getNotApplicable();
                if (na!=null){
                    Test t[] =na.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveNaResultsForDisplay(t[i]);
                    }
                }
            }
            Connector rar = sv.getConnector();
            if (rar!=null){
                Failed fail= rar.getFailed();
                if (fail!=null){
                    Test t[] =fail.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveFailResultsForDisplay(t[i]);
                    }
                }
                Warning w= rar.getWarning();
                if (w!=null){
                    Test t[] =w.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveWarnResultsForDisplay(t[i]);
                    }
                }
                Passed p= rar.getPassed();
                if (p!=null){
                    Test t[] =p.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.savePassResultsForDisplay(t[i]);
                    }
                }
                NotApplicable na= rar.getNotApplicable();
                if (na!=null){
                    Test t[] =na.getTest();
                    for (int i=0;i<t.length ;i++){
                        verifierSupport.saveNaResultsForDisplay(t[i]);
                    }
                }
            }
        } catch (RuntimeException rte) {
            err = StaticVerification.createGraph().newError();
            err.setErrorName(NbBundle.getMessage(VerifierSupport.class,"ERR_PARSING_OUTPUT"));  // NOI18N
            err.setErrorDescription(rte.getMessage());
            if (rte.getMessage().indexOf("error-name") > -1) {
                // TODO do the reparse, correct error-name and error-description
                // currently, tell user to look in the output window
                err.setErrorDescription(NbBundle.getMessage(VerifierSupport.class,"READ_OUTPUT_WINDOW"));   // NOI18N
            }
            verifierSupport.saveErrorResultsForDisplay( err);
        } catch (IOException ioe){
            ioe.printStackTrace();
            err = StaticVerification.createGraph().newError();
            err.setErrorName(NbBundle.getMessage(VerifierSupport.class,"ERR_PARSING_OUTPUT"));  // NOI18N
            err.setErrorDescription(ioe.getMessage());
            verifierSupport.saveErrorResultsForDisplay( err);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    // I cannot do anything here...
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                }
            }
        }
        verifierSupport.verifierIsStillRunning = false;// we are done
        verifierSupport.updateDisplay();
    }
    
    /** Creates a new instance of VerifierOuput */
    public VerifierSupport(String archiveName) {
        _archiveName = archiveName;
    }
    
    public void initUI(){
        setLayout(new BorderLayout());
        setName(NbBundle.getMessage(VerifierSupport.class,"LBL_Verifier",       //NOI18N
                new File(_archiveName).getName()));
        createResultsPanel();
        add(resultPanel);
        
    }
    
    /**
     * Called when the object is opened. Add the GUI.
     * @todo Trigger source listening on window getting VISIBLE instead
     * of getting opened.
     */
    @Override
    protected void componentOpened() {
    }
    
    /** Called when the window is closed. Cleans up. */
    @Override
    protected void componentClosed() {
        clearResults();
        table.getSelectionModel().removeListSelectionListener(tableSelectionListener);
        
        allButton.removeActionListener(myListener);
        failButton.removeActionListener(myListener);
        warnButton.removeActionListener(myListener);
        remove(resultPanel);
        resultPanel =null;
        table=null;
        allButton = null;
        failButton = null;
        warnButton=null;
        myListener =null;
        tableSelectionListener = null;
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    /**
     * Shows the TC in the output mode and activates it.
     */
    public void showInMode() {
        if (!isOpened()) {
            Mode mode = WindowManager.getDefault().findMode("output"); // NOI18N
            if (mode != null) {
                mode.dockInto(this);
            }
        }
        open();
        requestVisible();
        requestActive();
    }
    
    @Override
    protected String preferredID() {
        return NbBundle.getMessage(VerifierSupport.class,"verifierID");//NOI18N
    }
    
    private void createResultsPanel() {
        resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
                _archiveName));
        
        // 508 compliance
        resultPanel.getAccessibleContext().setAccessibleName( NbBundle.getMessage(VerifierSupport.class,"Panel"));  // NOI18N
        resultPanel.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage(VerifierSupport.class,"This_is_a_panel")); // NOI18N
        
        // set up result table
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer,
                    int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent)c;
                    jc.setToolTipText((String)getValueAt(rowIndex, vColIndex));
                }
                return c;
            }
        };

        // 508 for JTable
        table.getAccessibleContext().setAccessibleName( NbBundle.getMessage(VerifierSupport.class,"Table"));    // NOI18N
        table.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage(VerifierSupport.class,"This_is_a_table_of_items"));//NOI18N
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableScrollPane = new JScrollPane(table);
        Object [] row = {NbBundle.getMessage(VerifierSupport.class,"Wait"),NbBundle.getMessage(VerifierSupport.class,"Running_Verifier_Tool..."),NbBundle.getMessage(VerifierSupport.class,"Running...") };  // NOI18N
        tableModel.addRow(row);
        //table.sizeColumnsToFit(0);
        // 508 for JScrollPane
        tableScrollPane.getAccessibleContext().setAccessibleName( NbBundle.getMessage(VerifierSupport.class,"Scroll_Pane"));    // NOI18N
        tableScrollPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(VerifierSupport.class,"ScrollArea"));   // NOI18N
        sizeTableColumns();
        // make the cells uneditable
        JTextField field = new JTextField();
        // 508 for JTextField
        field.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(VerifierSupport.class,"Text_Field"));   // NOI18N
        field.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(VerifierSupport.class,"This_is_a_text_field")); // NOI18N
        table.setDefaultEditor(Object.class, new DefaultCellEditor(field) {
            @Override
            public boolean isCellEditable(EventObject anEvent) {
                return false;
            }
        });
        // add action listener to table to show details
        tableSelectionListener =  new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e){
                if (!e.getValueIsAdjusting()){
                    if(table.getSelectionModel().isSelectedIndex(e.getLastIndex())){
                        setDetailText( table.getModel().getValueAt(e.getLastIndex(),1)+
                                "\n"+table.getModel().getValueAt(e.getLastIndex(),2));//NOI18N
                    }else if(table.getSelectionModel().isSelectedIndex(e.getFirstIndex())){
                        setDetailText(table.getModel().getValueAt(e.getFirstIndex(),1)+
                                "\n"+table.getModel().getValueAt(e.getFirstIndex(),2));//NOI18N
                    }
                }
            }
        };
        table.getSelectionModel().addListSelectionListener(tableSelectionListener);
        
        // create detail text area
        detailText = new JTextArea(4,50);
        // 508 for JTextArea
        detailText.getAccessibleContext().setAccessibleName( NbBundle.getMessage(VerifierSupport.class,"Text_Area"));   // NOI18N
        detailText.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage(VerifierSupport.class,"This_is_a_text_area"));//NOI18N
        detailText.setEditable(false);
        textScrollPane = new JScrollPane(detailText);
        // 508 for JScrollPane
        textScrollPane.getAccessibleContext().setAccessibleName(  NbBundle.getMessage(VerifierSupport.class,"Scroll_Pane"));    // NOI18N
        textScrollPane.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage(VerifierSupport.class,"ScrollListPane"));//NOI18N
        textScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), NbBundle.getMessage(VerifierSupport.class,"Detail:")));// NOI18N
        
        //add the components to the panel
        createControlPanel();
        
        //Create a split pane with the two scroll panes in it.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                tableScrollPane, textScrollPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);
        
        //Provide minimum sizes for the two components in the split pane
        Dimension minimumSize = new Dimension(100, 50);
        tableScrollPane.setMinimumSize(minimumSize);
        textScrollPane.setMinimumSize(minimumSize);
        
        resultPanel.add("North", controlPanel); //NOI18N
        resultPanel.add("Center", splitPane);   // NOI18N
    }
    
    class RadioListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (verifierIsStillRunning){
                if(e.getSource() == allButton){
                    statusLeveltoDisplay = ALL;
                }
                if(e.getSource() == failButton){
                    statusLeveltoDisplay = FAIL;
                }
                if(e.getSource() == warnButton){
                    statusLeveltoDisplay = WARN;
                }
                
                return; // we need to wait!
            }
            if(e.getSource() == allButton){
                statusLeveltoDisplay = ALL;
                if ((getPassResultsForDisplay().size() > 0) ||
                        (getFailResultsForDisplay().size() > 0) ||
                        (getErrorResultsForDisplay().size() > 0) ||
                        (getWarnResultsForDisplay().size() > 0) ||
                        (getNaResultsForDisplay().size() > 0) ||
                        (getNotImplementedResultsForDisplay().size() > 0) ||
                        (getNotRunResultsForDisplay().size() > 0) ||
                        (getDefaultResultsForDisplay().size() > 0)) {
                    updateDisplay();
                } else {
                    clearResults();
                }
            }
            if(e.getSource() == failButton){
                statusLeveltoDisplay = FAIL;
                if (getFailResultsForDisplay().size() > 0 || getErrorResultsForDisplay().size() > 0) {
                    updateDisplay();
                } else {
                    clearResults();
                }
            }
            if(e.getSource() == warnButton){
                statusLeveltoDisplay = WARN;
                if ((getFailResultsForDisplay().size() > 0) ||
                        (getErrorResultsForDisplay().size() > 0) ||
                        (getWarnResultsForDisplay().size() > 0)) {
                    updateDisplay();
                } else {
                    clearResults();
                }
            }
        }
    }
    
    public void setDetailText(String details) {
        detailText.setText(details);
        JScrollBar scrollBar = textScrollPane.getVerticalScrollBar();
        if (scrollBar != null){
            scrollBar.setValue(0);
        }
    }
    
    public void clearResults() {
        //clear the table
        tableModel = new DefaultTableModel(columnNames, 0);
        table.setModel(tableModel);
        sizeTableColumns();
        //clear the detail text
        setDetailText("");
    }
    
    void sizeTableColumns() {
        table.getColumn(NbBundle.getMessage(VerifierSupport.class,STATUS_LIT)).setPreferredWidth(40);
        table.getColumn(NbBundle.getMessage(VerifierSupport.class,"Test_Description")).setPreferredWidth(300);// NOI18N
        table.getColumn(NbBundle.getMessage(VerifierSupport.class,"Result")).setPreferredWidth(300);//NOI18N
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }
    
    /**
     * This is the control panel of the Verifier GUI
     */
    private void createControlPanel() {
        allButton = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(allButton, allString); // NOI18N
        failButton = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(failButton, failString); // NOI18N
        warnButton = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(warnButton, warnString); // NOI18N
        controlPanel = new JPanel();
        
        // 508 for this panel
        controlPanel.getAccessibleContext().setAccessibleName(panelName);
        controlPanel.getAccessibleContext().setAccessibleDescription(panelDesc);
        allButton.getAccessibleContext().setAccessibleName(radioButtonName);
        allButton.getAccessibleContext().setAccessibleDescription(radioButtonDesc);
        failButton.getAccessibleContext().setAccessibleName(radioButtonName);
        failButton.getAccessibleContext().setAccessibleDescription(radioButtonDesc);
        warnButton.getAccessibleContext().setAccessibleName(radioButtonName);
        warnButton.getAccessibleContext().setAccessibleDescription(radioButtonDesc);
        
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        
        // set-up the radio buttons.
        allButton.setActionCommand(allString);
        allButton.setSelected(true);
        failButton.setActionCommand(failString);
        warnButton.setActionCommand(warnString);
        
        // Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(allButton);
        group.add(failButton);
        group.add(warnButton);
        
        // Put the radio buttons in a column in a panel
        JPanel radioPanel = new JPanel();
        // 508 for this panel
        radioPanel.getAccessibleContext().setAccessibleName(panelName);
        radioPanel.getAccessibleContext().setAccessibleDescription(panelDesc);
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));
        JLabel d = new JLabel(
                NbBundle.getMessage(VerifierSupport.class,"DisplayLabel")); // NOI18N
        d.setVerticalAlignment(SwingConstants.BOTTOM);
        // 508 compliance for the JLabel
        d.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(VerifierSupport.class,"Label"));    // NOI18N
        d.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(VerifierSupport.class,"This_is_a_label"));  // NOI18N
        radioPanel.add(d);
        radioPanel.add(allButton);
        radioPanel.add(failButton);
        radioPanel.add(warnButton);
        
        // Add the controls to the Panel
        controlPanel.add(radioPanel);
        
        // Register a listener for the report level radio buttons.
        myListener = new RadioListener();
        allButton.addActionListener(myListener);
        failButton.addActionListener(myListener);
        warnButton.addActionListener(myListener);
    }
    
    private void updateTableRows(String type, Vector results) {
        // update display approriately
        for (int i = 0; i < results.size(); i++) {
            Test t = ((Test)results.elementAt(i));
            Object [] row = {type, t.getTestAssertion(), t.getTestDescription()};
            tableModel.addRow(row);
        }
        //table.sizeColumnsToFit(0);
    }
    
    private void updateDisplayAll(){
        updateDisplayFail();
        updateDisplayWarn();
        updateDisplayPass();
        updateDisplayNa();
        updateDisplayNotImplemented();
        updateDisplayNotRun();
        updateDisplayDefault();
        updateDisplayError();
    }
    
    private void updateDisplayPass(){
        updateTableRows(NbBundle.getMessage(VerifierSupport.class,"Pass"),getPassResultsForDisplay());  // NOI18N
    }
    
    private void updateDisplayFail(){
        updateTableRows(NbBundle.getMessage(VerifierSupport.class,"Fail"),getFailResultsForDisplay());  // NOI18N
    }
    
    private void updateDisplayError(){
        Vector errors = getErrorResultsForDisplay();
        for (int i = 0; i < errors.size(); i++) {
            org.netbeans.modules.j2ee.sun.dd.impl.verifier.Error t = ((org.netbeans.modules.j2ee.sun.dd.impl.verifier.Error)errors.elementAt(i));
            Object [] row = {NbBundle.getMessage(VerifierSupport.class,"Error"),    // NOI18N
            t.getErrorName(),t.getErrorDescription() };
            tableModel.addRow(row);
            
        }
        //table.sizeColumnsToFit(0);
    }
    
    private void updateDisplayWarn(){
        updateTableRows(NbBundle.getMessage(VerifierSupport.class,"Warning"),getWarnResultsForDisplay());   // NOI18N
    }
    
    private void updateDisplayNa(){
        updateTableRows(NbBundle.getMessage(VerifierSupport.class,"Not_Applicable"),getNaResultsForDisplay());  // NOI18N
    }
    
    private void updateDisplayNotImplemented(){
        updateTableRows(NbBundle.getMessage(VerifierSupport.class,"Not_Implemented"),getNotImplementedResultsForDisplay()); // NOI18N
    }
    
    private void updateDisplayNotRun(){
        updateTableRows(NbBundle.getMessage(VerifierSupport.class,"Not_Run"),getNotRunResultsForDisplay()); // NOI18N
    }
    
    private void updateDisplayDefault(){
        updateTableRows("???",getDefaultResultsForDisplay());   // NOI18N
    }
    
    public void updateDisplay(){
        // update display approriately
        clearResults();
        if (statusLeveltoDisplay == ALL){
            updateDisplayAll();
        }
        if (statusLeveltoDisplay == FAIL){
            updateDisplayError();
            updateDisplayFail();
        }
        if (statusLeveltoDisplay == WARN){
            updateDisplayError();
            updateDisplayFail();
            updateDisplayWarn();
        }
    }
    
    private void savePassResultsForDisplay(Test r){
        passResults.addElement(r);
    }
    
    private void saveWarnResultsForDisplay(Test r){
        warnResults.addElement(r);
    }
    
    private void saveFailResultsForDisplay(Test r){
        failResults.addElement(r);
    }
    
    public void saveErrorResultsForDisplay(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Error r){
        errorResults.addElement(r);
    }
    
    private void saveNaResultsForDisplay(Test r){
        naResults.addElement(r);
    }
        
    private Vector getPassResultsForDisplay(){
        return passResults;
    }
    
    private Vector getWarnResultsForDisplay(){
        return warnResults;
    }
    
    private Vector getFailResultsForDisplay(){
        return failResults;
    }
    
    private Vector getErrorResultsForDisplay(){
        return errorResults;
    }
    
    private Vector getNaResultsForDisplay(){
        return naResults;
    }
    
    private Vector getNotImplementedResultsForDisplay(){
        return notImplementedResults;
    }
    
    private Vector getNotRunResultsForDisplay(){
        return notRunResults;
    }
    
    private Vector getDefaultResultsForDisplay(){
        return defaultResults;
    }
    
}
