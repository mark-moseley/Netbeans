package org.netbeans.modules.mashup.db.wizard;

import java.awt.Component;
import java.awt.Dialog;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import org.axiondb.ExternalConnectionProvider;
import org.netbeans.modules.mashup.db.common.FlatfileDBConnectionFactory;
import org.netbeans.modules.mashup.db.ui.wizard.SelectDatabasePanel;
import org.netbeans.modules.mashup.tables.wizard.JDBCTablePanel;
import org.netbeans.modules.sql.framework.ui.utils.AxionExternalConnectionProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.
public final class NewJDBCTableAction extends CallableSystemAction {
    
    private WizardDescriptor.Panel[] panels;
    
    public static final String DEFAULT_FLATFILE_JDBC_URL_PREFIX = "jdbc:axiondb:";
    
    public void performAction() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("Add JDBC Table(s)");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            boolean status = false;
            try {
                Thread.currentThread().getContextClassLoader().loadClass(AxionExternalConnectionProvider.class.getName());
                System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME,
                        AxionExternalConnectionProvider.class.getName());
            } catch (ClassNotFoundException ex) {
                //ignore
            }
            String jdbcUrl = (String) wizardDescriptor.getProperty("url");
            List<String> dblinks = (List<String>)wizardDescriptor.getProperty("dblinks");
            List<String> statements = (List<String>)wizardDescriptor.getProperty("statements");
            Connection conn = null;
            Statement stmt = null;
            try {
                conn = FlatfileDBConnectionFactory.getInstance().getConnection(jdbcUrl);
                if(conn != null) {
                    conn.setAutoCommit(true);
                    stmt = conn.createStatement();
                }
                
                Iterator it = dblinks.iterator();
                while(it.hasNext()) {
                    String sql = (String)it.next();
                    stmt.execute(sql);
                }
                
                it = statements.iterator();
                while(it.hasNext()) {
                    String sql = (String)it.next();
                    stmt.execute(sql);
                }   
                status = true;
            } catch(Exception ex) {
                
            } finally {
                if(conn != null) {
                    try {
                        if(stmt != null) {
                            stmt.execute("shutdown");
                        }
                        conn.close();
                    } catch (SQLException ex) {
                        conn = null;
                    }
                }
            }
            if(status) {
                NotifyDescriptor d =
                    new NotifyDescriptor.Message("Tables successfully created.", NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            } else {
                NotifyDescriptor d =
                    new NotifyDescriptor.Message("Tables creation failed.", NotifyDescriptor.WARNING_MESSAGE);
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
            panels = new WizardDescriptor.Panel[] {
                new SelectDatabasePanel(),
                new JDBCTablePanel()
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
        return "Add JDBC Table(s)";
    }
    
    public String iconResource() {
        return null;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
}