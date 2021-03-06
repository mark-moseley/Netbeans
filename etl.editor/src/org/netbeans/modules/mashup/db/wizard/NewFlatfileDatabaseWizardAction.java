package org.netbeans.modules.mashup.db.wizard;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JComponent;

import org.netbeans.modules.etl.ui.ETLEditorSupport;
import org.netbeans.modules.mashup.db.ui.AxionDBConfiguration;
import org.netbeans.modules.mashup.tables.wizard.MashupTableWizardIterator;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

public final class NewFlatfileDatabaseWizardAction extends CallableSystemAction {

    private WizardDescriptor.Panel[] panels;
    public static final String DEFAULT_FLATFILE_JDBC_URL_PREFIX = "jdbc:axiondb:";

    public void performAction() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("Create Mashup Database");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setSize(630, 334);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            String dbName = (String) wizardDescriptor.getProperty("dbName");
            boolean status = handle(dbName);
            if (status) {
                NotifyDescriptor d =
                        new NotifyDescriptor.Message("Database '" + dbName + "' successfully created.", NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                new NewFlatfileDatabaseWizardPanel()
            };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    public String getName() {
        return "Create Mashup Database";
    }

    @Override
    public String iconResource() {
        return null;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private String getDefaultWorkingFolder() {
        File conf = AxionDBConfiguration.getConfigFile();
        Properties prop = new Properties();
        try {
            FileInputStream in = new FileInputStream(conf);
            prop.load(in);
        } catch (FileNotFoundException ex) {
        //ignore
        } catch (IOException ex) {
        //ignore
        }
        String loc = prop.getProperty(AxionDBConfiguration.PROP_DB_LOC);
        File db = new File(loc);
        if (!db.exists()) {
            db.mkdir();
        }
        return loc;
    }

    private boolean handle(String name) {
        String location = null;
        if (MashupTableWizardIterator.IS_PROJECT_CALL) {
            location = ETLEditorSupport.PRJ_PATH + "\\nbproject\\private\\databases";
        } else {
            location = getDefaultWorkingFolder();
        }       
        MashupTableWizardIterator.IS_PROJECT_CALL = false;
        boolean status = false;
        String url = DEFAULT_FLATFILE_JDBC_URL_PREFIX + name + ":" + location + "\\" + name;
        File f = new File(location + name);
        char[] ch = name.toCharArray();
        if (ch == null) {
            NotifyDescriptor d =
                    new NotifyDescriptor.Message("No Database name specified.", NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        } else if (f.exists()) {
            NotifyDescriptor d =
                    new NotifyDescriptor.Message("Database '" + name + " already exists.", NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        } else {
            Connection conn = null;
            try {
                conn = DBExplorerUtil.createConnection("org.axiondb.jdbc.AxionDriver", url, "sa", "sa");
                if (conn != null) {
                    status = true;
                }
            } catch (Exception ex) {
                NotifyDescriptor d =
                        new NotifyDescriptor.Message("Axion driver could not be loaded.", NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            } finally {
                try {
                    if (conn != null) {
                        conn.createStatement().execute("shutdown");
                        conn.close();
                    }
                } catch (SQLException ex) {
                    conn = null;
                }
            }
        }
        return status;
    }
}
