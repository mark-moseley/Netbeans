/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.query;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Query.Filter;
import org.netbeans.modules.bugtracking.spi.QueryNotifyListener;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.commands.BugzillaCommand;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.bugzilla.query.QueryParameter.CheckBoxParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ComboParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ListParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ParameterValue;
import org.netbeans.modules.bugzilla.query.QueryParameter.TextFieldParameter;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class QueryController extends BugtrackingController implements DocumentListener, ItemListener, ListSelectionListener, ActionListener, FocusListener, KeyListener {
    protected QueryPanel panel;

    private final ComboParameter summaryParameter;
    private final ComboParameter commentsParameter;
    private final ComboParameter keywordsParameter;
    private final ComboParameter peopleParameter;
    private final ListParameter productParameter;
    private final ListParameter componentParameter;
    private final ListParameter versionParameter;
    private final ListParameter statusParameter;
    private final ListParameter resolutionParameter;
    private final ListParameter priorityParameter;
    private final ListParameter changedFieldsParameter;
    private final ListParameter severityParameter;

    private final Map<String, QueryParameter> parameters;

    private static int counter;
    private RequestProcessor rp = new RequestProcessor("Bugzilla query - " + counter++, 1);  // NOI18N
    private Task task;

    private final BugzillaRepository repository;
    private BugzillaQuery query;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss, EEE MMM d yyyy"); // NOI18N
    private NotifyListener notifyListener;

    public QueryController(BugzillaRepository repository, BugzillaQuery query, String urlParameters) {
        this.repository = repository;
        this.query = query;
        
        panel = new QueryPanel(query.getTableComponent(), this);

        panel.productList.addListSelectionListener(this);
        panel.filterComboBox.addItemListener(this);
        panel.searchButton.addActionListener(this);
        panel.keywordsButton.addActionListener(this);
        panel.saveChangesButton.addActionListener(this);
        panel.cancelChangesButton.addActionListener(this);
        panel.gotoIssueButton.addActionListener(this);
        panel.webButton.addActionListener(this);
        panel.saveButton.addActionListener(this);
        panel.urlToggleButton.addActionListener(this);
        panel.refreshButton.addActionListener(this);
        panel.modifyButton.addActionListener(this);
        panel.seenButton.addActionListener(this);
        panel.removeButton.addActionListener(this);
        panel.changedFromTextField.addFocusListener(this);

        panel.idTextField.addActionListener(this);
        panel.productList.addKeyListener(this);
        panel.componentList.addKeyListener(this);
        panel.versionList.addKeyListener(this);
        panel.statusList.addKeyListener(this);
        panel.resolutionList.addKeyListener(this);
        panel.severityList.addKeyListener(this);
        panel.priorityList.addKeyListener(this);
        panel.changedList.addKeyListener(this);

        panel.summaryTextField.addActionListener(this);
        panel.commentTextField.addActionListener(this);
        panel.keywordsTextField.addActionListener(this);
        panel.peopleTextField.addActionListener(this);
        panel.changedFromTextField.addActionListener(this);
        panel.changedToTextField.addActionListener(this);
        panel.changedToTextField.addActionListener(this);

        // setup parameters
        parameters = new LinkedHashMap<String, QueryParameter>();
        summaryParameter = createQueryParameter(ComboParameter.class, panel.summaryComboBox, "short_desc_type");    // NOI18N
        commentsParameter = createQueryParameter(ComboParameter.class, panel.commentComboBox, "long_desc_type");    // NOI18N
        keywordsParameter = createQueryParameter(ComboParameter.class, panel.keywordsComboBox, "keywords_type");    // NOI18N
        peopleParameter = createQueryParameter(ComboParameter.class, panel.peopleComboBox, "emailtype1");           // NOI18N
        productParameter = createQueryParameter(ListParameter.class, panel.productList, "product");                 // NOI18N
        componentParameter = createQueryParameter(ListParameter.class, panel.componentList, "component");           // NOI18N
        versionParameter = createQueryParameter(ListParameter.class, panel.versionList, "version");                 // NOI18N
        statusParameter = createQueryParameter(ListParameter.class, panel.statusList, "bug_status");                // NOI18N
        resolutionParameter = createQueryParameter(ListParameter.class, panel.resolutionList, "resolution");        // NOI18N
        priorityParameter = createQueryParameter(ListParameter.class, panel.priorityList, "priority");              // NOI18N
        changedFieldsParameter = createQueryParameter(ListParameter.class, panel.changedList, "chfield");           // NOI18N
        severityParameter = createQueryParameter(ListParameter.class, panel.severityList, "bug_severity");          // NOI18N
        
        createQueryParameter(TextFieldParameter.class, panel.summaryTextField, "short_desc");                       // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.commentTextField, "long_desc");                        // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.keywordsTextField, "keywords");                        // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.peopleTextField, "email1");                            // NOI18N
        createQueryParameter(CheckBoxParameter.class, panel.bugAssigneeCheckBox, "emailassigned_to1");              // NOI18N
        createQueryParameter(CheckBoxParameter.class, panel.reporterCheckBox, "emailreporter1");                    // NOI18N
        createQueryParameter(CheckBoxParameter.class, panel.ccCheckBox, "emailcc1");                                // NOI18N
        createQueryParameter(CheckBoxParameter.class, panel.commenterCheckBox, "emaillongdesc1");                   // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.changedFromTextField, "chfieldfrom");                  // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.changedToTextField, "chfieldto");                      // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.newValueTextField, "chfieldvalue");                    // NOI18N

        if(query.isSaved()) {
            setAsSaved();
        }
        notifyListener = new NotifyListener();
        query.addNotifyListener(notifyListener);
        postPopulate(urlParameters);
    }

    void addNotify() {
        if(query.isSaved() && !query.wasRun()) {
            onRefresh();
        }
    }

    void removeNotify() {
        onCancelChanges();
        if(task != null) {
            task.cancel();
        }
    }

    private <T extends QueryParameter> T createQueryParameter(Class<T> clazz, Component c, String parameter) {
        try {
            Constructor<T> constructor = clazz.getConstructor(c.getClass(), String.class);
            T t = constructor.newInstance(c, parameter);
            parameters.put(parameter, t);
            return t;
        } catch (Exception ex) {
            Bugzilla.LOG.log(Level.SEVERE, parameter, ex);
        }
        return null;
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public HelpCtx getHelpContext() {
        return new HelpCtx(org.netbeans.modules.bugzilla.query.BugzillaQuery.class);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void applyChanges() {
        
    }

    public String getUrlParameters() {
        StringBuffer sb = new StringBuffer();
        for (QueryParameter p : parameters.values()) {
            sb.append(p.get());
        }
        return sb.toString();
    }

    private void postPopulate(final String urlParameters) {
        enableFields(false);

        final Task[] t = new Task[1];
        Cancellable c = new Cancellable() {
            public boolean cancel() {
                if(t[0] != null) {
                    return t[0].cancel();
                }
                return true;
            }
        };

        final String msgPopulating = NbBundle.getMessage(QueryController.class, "MSG_Populating");    // NOI18N
        final ProgressHandle handle = ProgressHandleFactory.createHandle(msgPopulating, c);
        final JComponent progressBar = ProgressHandleFactory.createProgressComponent(handle);
        panel.showRetrievingProgress(true, progressBar, msgPopulating, !query.isSaved());
        t[0] = rp.post(new Runnable() {
            public void run() {
                handle.start();
                try {
                    populate(urlParameters);
                } finally {
                    enableFields(true);
                    handle.finish();
                    panel.showRetrievingProgress(false, progressBar, null, !query.isSaved());
                }
            }
        });
    }

    public void populate(final String urlParameters) {
        Bugzilla.LOG.fine("Starting populate query controller"); // NOI18N
        try {
            BugzillaCommand cmd = new BugzillaCommand() {
                @Override
                public void execute() throws CoreException, IOException, MalformedURLException {
                    BugzillaConfiguration bc = repository.getConfiguration();
                    if(bc == null) {
                        // XXX nice errro msg?
                        return;
                    }
                    productParameter.setParameterValues(toParameterValues(bc.getProducts()));
                    if (panel.productList.getModel().getSize() > 0) {
                        panel.productList.setSelectedIndex(0);
                        populateProductDetails(((ParameterValue) panel.productList.getSelectedValue()).getValue());
                    }
                    severityParameter.setParameterValues(toParameterValues(bc.getSeverities()));
                    statusParameter.setParameterValues(toParameterValues(bc.getStatusValues()));
                    resolutionParameter.setParameterValues(toParameterValues(bc.getResolutions()));
                    priorityParameter.setParameterValues(toParameterValues(bc.getPriorities()));
                    changedFieldsParameter.setParameterValues(QueryParameter.PV_LAST_CHANGE);
                    summaryParameter.setParameterValues(QueryParameter.PV_TEXT_SEARCH_VALUES);
                    commentsParameter.setParameterValues(QueryParameter.PV_TEXT_SEARCH_VALUES);
                    keywordsParameter.setParameterValues(QueryParameter.PV_KEYWORDS_VALUES);
                    peopleParameter.setParameterValues(QueryParameter.PV_PEOPLE_VALUES);
                    panel.changedToTextField.setText("Now"); // XXX

                    // XXX
                    if (urlParameters != null) {
                        setParameters(urlParameters);
                    }

                    panel.filterComboBox.setModel(new DefaultComboBoxModel(query.getFilters()));
                    panel.jScrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    panel.jScrollPane3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    panel.jScrollPane4.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    panel.jScrollPane5.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    panel.jScrollPane6.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    panel.jScrollPane7.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                }
            };
            repository.getExecutor().execute(cmd);
        } finally {
            Bugzilla.LOG.fine("Finnished populate query controller"); // NOI18N
        }
    }

    protected void enableFields(boolean bl) {
        // set all non parameter fields
        panel.enableFields(bl);
        // set the parameter fields
        for (Map.Entry<String, QueryParameter> e : parameters.entrySet()) {
            QueryParameter pv = parameters.get(e.getKey());
            pv.setEnabled(bl);
        }
    }

    protected void disableProduct(String product) { // XXX whatever field
        productParameter.setAlwaysDisabled(true);
    }

    public void insertUpdate(DocumentEvent e) {
        fireDataChanged();
    }

    public void removeUpdate(DocumentEvent e) {
        fireDataChanged();
    }

    public void changedUpdate(DocumentEvent e) {
        fireDataChanged();
    }

    public void itemStateChanged(ItemEvent e) {
        fireDataChanged();
        if(e.getSource() == panel.filterComboBox) {
            onFilterChange((Query.Filter)e.getItem());
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if(e.getSource() == panel.productList) {
            onProductChanged(e);
        }
        fireDataChanged();            // XXX do we need this ???
    }

    public void focusGained(FocusEvent e) {
        if(panel.changedFromTextField.getText().equals("")) {
            String lastChangeFrom = BugzillaConfig.getInstance().getLastChangeFrom();
            panel.changedFromTextField.setText(lastChangeFrom);
            panel.changedFromTextField.setSelectionStart(0);
            panel.changedFromTextField.setSelectionEnd(lastChangeFrom.length());
        }
    }

    public void focusLost(FocusEvent e) {
        // do nothing
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.searchButton) {
            onSearch();
        } else if (e.getSource() == panel.gotoIssueButton) {
            onGotoIssue();
        } else if (e.getSource() == panel.keywordsButton) {
            onKeywords();
        } else if (e.getSource() == panel.searchButton) {
            onSearch();
        } else if (e.getSource() == panel.saveChangesButton) {
            onSave();
        } else if (e.getSource() == panel.cancelChangesButton) {
            onCancelChanges();
        } else if (e.getSource() == panel.gotoIssueButton) {
            onGotoIssue();
        } else if (e.getSource() == panel.webButton) {
            onWeb();
        } else if (e.getSource() == panel.saveButton) {
            onSave();
        } else if (e.getSource() == panel.urlToggleButton) {
            onDefineAs();
        } else if (e.getSource() == panel.refreshButton) {
            onRefresh();
        } else if (e.getSource() == panel.modifyButton) {
            onModify();
        } else if (e.getSource() == panel.seenButton) {
            onMarkSeen();
        } else if (e.getSource() == panel.removeButton) {
            onRemove();
        } else if (e.getSource() == panel.idTextField) {
            if(!panel.idTextField.getText().trim().equals("")) {
                onGotoIssue();
            }
        } else if (e.getSource() == panel.idTextField ||
                   e.getSource() == panel.summaryTextField ||
                   e.getSource() == panel.commentTextField ||
                   e.getSource() == panel.keywordsTextField ||
                   e.getSource() == panel.peopleTextField ||
                   e.getSource() == panel.changedFromTextField ||
                   e.getSource() == panel.newValueTextField ||
                   e.getSource() == panel.changedToTextField)
        {
            onSearch();
        }
    }

    public void keyTyped(KeyEvent e) {
        // do nothing
    }

    public void keyPressed(KeyEvent e) {
        // do nothing
    }

    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() != KeyEvent.VK_ENTER) {
            return;
        }
        if(e.getSource() == panel.productList ||
           e.getSource() == panel.componentList ||
           e.getSource() == panel.versionList ||
           e.getSource() == panel.statusList ||
           e.getSource() == panel.resolutionList ||
           e.getSource() == panel.priorityList ||
           e.getSource() == panel.changedList)
        {
            onSearch();
        }
    }

    private void onFilterChange(Query.Filter filter) {
        query.setFilter(filter);
    }

    private void onSave() {
       Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                String name = query.getDisplayName();
                boolean firstTime = false;
                if(!query.isSaved()) {
                    firstTime = true;
                    name = getName();
                    if(name == null) {
                        return;
                    }
                    panel.queryNameTextField.setText("");
                }
                assert name != null;
                save(name, firstTime);
            }
       });
    }

    private String getName() {
        String name = null;
        if(BugzillaUtil.show(panel.savePanel, NbBundle.getMessage(QueryController.class, "LBL_SaveQuery"),  NbBundle.getMessage(QueryController.class, "LBL_Save"))) { // NOI18N
            name = panel.queryNameTextField.getText();
            if(name == null || name.trim().equals("")) { // NOI18N
                return null;
            }
            Query[] queries = repository.getQueries();
            for (Query q : queries) {
                if(q.getDisplayName().equals(name)) {
                    panel.saveErrorLabel.setVisible(true);
                    name = getName();                    
                    panel.saveErrorLabel.setVisible(false);
                    break;
                }
            }
        } else {
            return null;
        }
        return name;
    }

    private void save(String name, boolean firstTime) {
        query.setName(name);
        repository.saveQuery(query);
        query.setSaved(true); // XXX
        setAsSaved();
        if (firstTime) {
            onSearch();
        } else {
            onRefresh();
        }
    }

    private void onCancelChanges() {
        if(query.getDisplayName() != null) { // XXX need a better semantic - isSaved?
            String urlParameters = BugzillaConfig.getInstance().getUrlParams(repository, query.getDisplayName());
            if(urlParameters != null) {
                setParameters(urlParameters);
            }
        }
        setAsSaved();
    }

    public void selectFilter(Filter filter) {
        if(filter != null) {
            panel.filterComboBox.setSelectedItem(filter);
        }
    }

    private void setAsSaved() {
        panel.setSaved(query.getDisplayName(), getLastRefresh());
        panel.setModifyVisible(false);
    } 

    private String getLastRefresh() throws MissingResourceException {
        long l = query.getLastRefresh();
        return l > -1 ?
            dateFormat.format(new Date(l)) :
            NbBundle.getMessage(QueryController.class, "LBL_Never"); // NOI18N
    }

    private void onGotoIssue() {
        final String id = panel.idTextField.getText().trim();
        if(id == null || id.trim().equals("") ) {
            return;
        }
        
        final Task[] t = new Task[1];
        Cancellable c = new Cancellable() {
            public boolean cancel() {
                if(t[0] != null) {
                    return t[0].cancel();
                }
                return true;
            }
        };
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(QueryController.class, "MSG_Opening", new Object[] {id}), c); // NOI18N
        t[0] = Bugzilla.getInstance().getRequestProcessor().create(new Runnable() {
            public void run() {
                handle.start();
                try {
                    Issue issue = repository.getIssue(id);
                    if (issue != null) {
                        issue.open();
                    } else {
                        // XXX nice message?
                    }
                } finally {
                    handle.finish();
                }
            }
        });
        t[0].schedule(0);
    }

    private void onWeb() {
        final String repoURL = repository.getTaskRepository().getRepositoryUrl() + "/query.cgi" + "?format=advanced"; // NOI18N //XXX need constants
        Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                URL url;
                try {
                    url = new URL(repoURL);
                } catch (MalformedURLException ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex);
                    return;
                }
                HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
                if (displayer != null) {
                    displayer.showURL (url);
                } else {
                    // XXX nice error message?
                    Bugzilla.LOG.warning("No URLDisplayer found.");             // NOI18N
                }
            }
        });
    }

    private void onProductChanged(ListSelectionEvent e) {
        Object[] values =  panel.productList.getSelectedValues();
        String[] products = null;
        if(values != null) {
            products = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                products[i] = ((ParameterValue) values[i]).getValue();
            }
        }
        populateProductDetails(products);
    }

    private void onDefineAs() {
        panel.switchQueryFields(panel.urlPanel.isVisible());
    }

    private void onKeywords() {
        String keywords = BugzillaUtil.getKeywords(NbBundle.getMessage(QueryController.class, "LBL_SelectKeywords"), panel.keywordsTextField.getText(), repository);
        if(keywords != null) {
            panel.keywordsTextField.setText(keywords);
        }
    }

    private void onSearch() {
        post(new Runnable() {
            public void run() {
                try {
                    String lastChageFrom = panel.changedFromTextField.getText().trim();
                    if(lastChageFrom != null && !lastChageFrom.equals("")) {
                        BugzillaConfig.getInstance().setLastChangeFrom(lastChageFrom);
                    }
                    refresh();
                } finally {
                    panel.setQueryRunning(false);
                    task = null;
                }
            }
        });
    }

    public void onRefresh() {
        post(new Runnable() {
            public void run() {
                panel.setQueryRunning(true);
                try {
                    refresh();
                } finally {
                    panel.setQueryRunning(false);
                    task = null;
                }
            }

        });        
    }

    public void refresh() {
        if (panel.urlPanel.isVisible()) {
            // XXX check url format etc...
            // XXX what if there is a different host in queries repository as in the url?
            query.refresh(panel.urlTextField.getText());
        } else {
            query.refresh(getUrlParameters());
        }
    }

    private void onModify() {
        panel.setModifyVisible(true);
    }

    private void onMarkSeen() {
        Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                Issue[] issues = query.getIssues();
                for (Issue issue : issues) {
                    try {
                        ((BugzillaIssue) issue).setSeen(true);
                    } catch (IOException ex) {
                        Bugzilla.LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    private void onRemove() {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
            NbBundle.getMessage(QueryController.class, "MSG_RemoveQuery", new Object[] { query.getDisplayName() }), // NOI18N
            NbBundle.getMessage(QueryController.class, "CTL_RemoveQuery"),      // NOI18N
            NotifyDescriptor.OK_CANCEL_OPTION);

        if(DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
                public void run() {
                    remove();
                }
            });
        }
    }
    
    private void remove() {
        if (task != null) {
            task.cancel();
        }
        repository.removeQuery(query);
        query.fireQueryRemoved();
    }

    private synchronized void post(Runnable r) {
        if(task != null) {
            task.cancel();
        }
        enableFields(false);        
        task = rp.create(r);

        Cancellable c = new Cancellable() {
            public boolean cancel() {
                task.cancel();
                return true;
            }
        };

        ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(QueryController.class, "MSG_SearchingQuery", new Object[] {query.getDisplayName()}), c);// NOI18N
        JComponent progressBar = ProgressHandleFactory.createProgressComponent(handle);
        panel.showSearchingProgress(true, progressBar, NbBundle.getMessage(QueryController.class, "MSG_Searching")); // NOI18N
        notifyListener.setProgressHandle(handle);
        handle.start();
        
        task.schedule(0);
    }

    private void populateProductDetails(String... products) {
        BugzillaConfiguration bc = repository.getConfiguration();
        if(bc == null) {
            // XXX nice errro msg?
            return;
        }
        if(products == null || products.length == 0) {
            products = new String[] {null};
        }

        List<String> newComponents = new ArrayList<String>();
        List<String> newVersions = new ArrayList<String>();
        for (String p : products) {
            List<String> productComponents = bc.getComponents(p);
            for (String c : productComponents) {
                if(!newComponents.contains(c)) {
                    newComponents.add(c);
                }
            }
            List<String> productVersions = bc.getVersions(p);
            for (String c : productVersions) {
                if(!newVersions.contains(c)) {
                    newVersions.add(c);
                }
            }
        }

        componentParameter.setParameterValues(toParameterValues(newComponents));
        versionParameter.setParameterValues(toParameterValues(newVersions));
    }

    private List<ParameterValue> toParameterValues(List<String> values) {
        List<ParameterValue> ret = new ArrayList<ParameterValue>(values.size());
        for (String v : values) {
            ret.add(new ParameterValue(v, v));
        }
        return ret;
    }

    private void setParameters(String urlParameters) {
        if(urlParameters == null) {
            return;
        }
        String[] params = urlParameters.split("&"); // NOI18N
        if(params == null || params.length == 0) return;
        Map<String, List<ParameterValue>> normalizedParams = new HashMap<String, List<ParameterValue>>();
        for (String p : params) {
            int idx = p.indexOf("="); // NOI18N
            if(idx > -1) {
                String parameter = p.substring(0, idx);
                String value = p.substring(idx + 1, p.length());

                ParameterValue pv = new ParameterValue(value, value);
                List<ParameterValue> values = normalizedParams.get(parameter);
                if(values == null) {
                    values = new ArrayList<ParameterValue>();
                    normalizedParams.put(parameter, values);
                }
                values.add(pv);
            } else {
                // XXX warning!!
            }
        }

        for (Map.Entry<String, List<ParameterValue>> e : normalizedParams.entrySet()) {
            QueryParameter pv = parameters.get(e.getKey());
            if(pv != null) {
                List<ParameterValue> pvs = e.getValue();    
                pv.setValues(pvs.toArray(new ParameterValue[pvs.size()]));
            }
        }
    }

    private class NotifyListener implements QueryNotifyListener {
        private int counter;
        private ProgressHandle handle;

        void setProgressHandle(ProgressHandle handle) {
            this.handle = handle;
        }
        
        public void notifyData(final Issue issue) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    panel.showNoContentPanel(false);
                    panel.tableSummaryLabel.setText(NbBundle.getMessage(QueryController.class, "LBL_MatchingIssues", new Object[] {++counter})); // NOI18N // XXX
                }
            });
        }

        public void started() {
            counter = 0;
        }

        public void finished() {
            if(handle != null) {
                handle.finish();
                handle = null;
            }
            final int size = query.getSize();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    enableFields(true);
                    panel.setLastRefresh(getLastRefresh());
                    panel.showNoContentPanel(false);
                    if(size == 0) {
                        panel.tableSummaryLabel.setText(NbBundle.getMessage(QueryController.class, "LBL_MatchingIssues", new Object[] {0})); // NOI18N // XXX
                    }
                }
            });
        }
    }

}
