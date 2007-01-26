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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.unit;

import java.awt.CardLayout;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.text.JTextComponent;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Properties;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Property;
import org.netbeans.modules.j2ee.persistence.provider.DefaultProvider;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.util.PersistenceProviderComboboxHelper;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.unit.JdbcListCellRenderer;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourcePopulator;
import org.netbeans.modules.j2ee.persistence.util.PersistenceProviderComboboxHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.Error;
import org.openide.util.NbBundle;

/**
 *
 * @author  Martin Adamek
 */
public class PersistenceUnitPanel extends SectionInnerPanel {
    
    private final PersistenceUnit persistenceUnit;
    private PUDataObject dObj;
    private Project project;
    private boolean isContainerManaged;
    
    public PersistenceUnitPanel(SectionView view, final PUDataObject dObj,  final PersistenceUnit persistenceUnit) {
        super(view);
        this.dObj=dObj;
        this.persistenceUnit=persistenceUnit;
        this.project = FileOwnerQuery.getOwner(this.dObj.getPrimaryFile());
        
        assert project != null : "Could not resolve project for " + dObj.getPrimaryFile(); //NOI18N
        
        if (ProviderUtil.getLibrary(persistenceUnit) != null && ProviderUtil.getConnection(persistenceUnit) != null) {
            isContainerManaged = false;
        } else if (persistenceUnit.getJtaDataSource() != null || persistenceUnit.getNonJtaDataSource() != null) {
            isContainerManaged = true;
        } else {
            isContainerManaged = Util.isSupportedJavaEEVersion(project);
        }
        
        initComponents();
        
        PersistenceProviderComboboxHelper comboHelper = new PersistenceProviderComboboxHelper(project);
        if (isContainerManaged){
            comboHelper.connect(providerCombo);
            Provider provider = ProviderUtil.getProvider(persistenceUnit);
            providerCombo.setSelectedItem(provider);
        } else {
            comboHelper.connect(libraryComboBox);
            setSelectedLibrary();
        }
        
        setVisiblePanel();
        initIncludeAllEntities();
        initEntityList();
        
        initDataSource();
        
        initJdbcComboBox();
        nameTextField.setText(persistenceUnit.getName());
        setTableGeneration();
        handleCmAmSelection();
        
        registerModifiers();
        
    }
    
    /**
     * Registers the components that modify the model. Should be called after
     * these components have been initialized (otherwise the underlying file will
     * be marked  as modified immediately upon opening it).
     */
    private void registerModifiers(){
        if (isContainerManaged){
            addImmediateModifier(dsCombo);
            addImmediateModifier(providerCombo);
            addImmediateModifier(jtaCheckBox);
            
        } else {
            addImmediateModifier(jdbcComboBox);
            addImmediateModifier(libraryComboBox);
        }
        addImmediateModifier(nameTextField);
        addImmediateModifier(ddDropCreate);
        addImmediateModifier(ddCreate);
        addImmediateModifier(ddUnknown);
        addImmediateModifier(includeAllEntities);
        
    }
    
    
    /**
     * Sets which panel (container/application) is visible.
     */
    private void setVisiblePanel(){
        String panelName = isContainerManaged ? "container" : "application";//NOI18N
        ((CardLayout)jPanel3.getLayout()).show(jPanel3, panelName);
    }
    
    private void initDataSource(){
        jtaCheckBox.setEnabled(!Util.isJavaSE(project));
        if (isContainerManaged && ProviderUtil.isValidServerInstanceOrNone(project)) {
            String jtaDataSource = persistenceUnit.getJtaDataSource();
            String nonJtaDataSource = persistenceUnit.getNonJtaDataSource();
            
            JPADataSourcePopulator dsPopulator = project.getLookup().lookup(JPADataSourcePopulator.class);
            if (dsPopulator != null){
                dsPopulator.connect(dsCombo);
                addModifier((JTextComponent)dsCombo.getEditor().getEditorComponent(), false);
            }
            
            String jndiName = (jtaDataSource != null ? jtaDataSource : nonJtaDataSource);
            selectDatasource(jndiName);
            
            jtaCheckBox.setSelected(jtaDataSource != null);
            
            String provider = persistenceUnit.getProvider();
            for (int i = 0; i < providerCombo.getItemCount(); i++) {
                Object item = providerCombo.getItemAt(i);
                if (item instanceof Provider){
                    if (((Provider) item).getProviderClass().equals(provider)) {
                        providerCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } else if (!isContainerManaged){
            setSelectedConnection();
            setSelectedLibrary();
            jtaCheckBox.setSelected(false);
        }
    }
    
    private void initJdbcComboBox(){
        DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();
        for (int i = 0; i < connections.length; i++) {
            jdbcComboBox.addItem(connections[i]);
        }
        setSelectedConnection();
    }
    
    private void initIncludeAllEntities(){
        boolean javaSE = Util.isJavaSE(project);
        includeAllEntities.setEnabled(!javaSE);
        includeAllEntities.setSelected(!javaSE && !persistenceUnit.isExcludeUnlistedClasses());
        includeAllEntities.setText(NbBundle.getMessage(PersistenceUnitPanel.class,
                "LBL_IncludeAllEntities",
                new Object[]{ProjectUtils.getInformation(project).getDisplayName()}));
    }
    
    private void initEntityList(){
        DefaultListModel listedClassesModel = new DefaultListModel();
        for (String elem : persistenceUnit.getClass2()) {
            listedClassesModel.addElement(elem);
        }
        entityList.setModel(listedClassesModel);
        
    }
    
    /**
     * Sets selected item in library combo box.
     */
    private void setSelectedLibrary(){
        Provider selected = ProviderUtil.getProvider(persistenceUnit);
        if (selected == null){
            return;
        }
        for(int i = 0; i < libraryComboBox.getItemCount(); i++){
            Object item = libraryComboBox.getItemAt(i);
            Provider provider = (Provider) (item instanceof Provider ? item : null);
            if (provider!= null && provider.equals(selected)){
                libraryComboBox.setSelectedIndex(i);
                break;
            }
        }
    }
    
    private Provider getSelectedProvider(){
        if (isContainerManaged){
            return (Provider) providerCombo.getSelectedItem();
        }
        Object selectedItem = libraryComboBox.getSelectedItem();
        Provider provider = (Provider) (selectedItem instanceof Provider ? selectedItem : null);
        if (provider != null) {
            return  provider;
        }
        return ProviderUtil.getProvider(persistenceUnit.getProvider(), project);
    }
    
    /**
     * Selects appropriate table generation radio button.
     */
    private void setTableGeneration(){
        Provider provider = getSelectedProvider();
        Property tableGeneration = ProviderUtil.getProperty(persistenceUnit, provider.getTableGenerationPropertyName());
        if (tableGeneration != null){
            if (provider.getTableGenerationCreateValue().equals(tableGeneration.getValue())){
                ddCreate.setSelected(true);
            } else if (provider.getTableGenerationDropCreateValue().equals(tableGeneration.getValue())){
                ddDropCreate.setSelected(true);
            }
        } else {
            ddUnknown.setSelected(true);
        }
        boolean toggle = provider.supportsTableGeneration();
        
        ddCreate.setEnabled(toggle);
        ddDropCreate.setEnabled(toggle);
        ddUnknown.setEnabled(toggle);
    }
    
    /**
     * Sets the selected item in connection combo box.
     */
    private void setSelectedConnection(){
        DatabaseConnection connection = ProviderUtil.getConnection(persistenceUnit);
        if (connection != null){
            jdbcComboBox.setSelectedItem(connection);
        } else {
            // custom connection (i.e. connection not registered in netbeans)
            Properties props = persistenceUnit.getProperties();
            if (props != null){
                Property[] properties = props.getProperty2();
                String url = "custom";
                Provider provider = ProviderUtil.getProvider(persistenceUnit);
                for (int i = 0; i < properties.length; i++) {
                    String key = properties[i].getName();
                    if (key.equals(provider.getJdbcUrl())) {
                        url = properties[i].getValue();
                        break;
                    }
                }
                jdbcComboBox.addItem(url);
                jdbcComboBox.setSelectedItem(url);
            }
        }
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (source == nameTextField) {
            persistenceUnit.setName((String) value);
        } else if (source == dsCombo){
            setDataSource();
        } else if (source == dsCombo.getEditor().getEditorComponent()) {
            setDataSource((String)value);
        } else if (source == jdbcComboBox){
            if (value instanceof DatabaseConnection){
                ProviderUtil.setDatabaseConnection(persistenceUnit, (DatabaseConnection) value);
            }
        } else if (source == libraryComboBox){
            setProvider();
            setTableGeneration();
        } else if (providerCombo == source){
            setProvider();
            setDataSource();
        } else if (source == ddCreate || source == ddDropCreate || source == ddUnknown){
            ProviderUtil.setTableGeneration(persistenceUnit, getTableGeneration(), ProviderUtil.getProvider(persistenceUnit.getProvider(), project));
        } else if (source == includeAllEntities){
            persistenceUnit.setExcludeUnlistedClasses(!includeAllEntities.isSelected());
        } else if (source == jtaCheckBox){
            setDataSource();
        }
        performValidation();
    }
    
    private void performValidation(){
        PersistenceValidator validator = new PersistenceValidator(dObj);
        List<Error> result = validator.validate();
        if (!result.isEmpty()){
            getSectionView().getErrorPanel().setError(result.get(0));
        } else {
            getSectionView().getErrorPanel().clearError();
        }
        
    }
    
    private void setDataSource() {
        setDataSource(null);
    }
    
    private void setProvider(){
        
        if (isContainerManaged && providerCombo.getSelectedItem() instanceof Provider) {
            Provider provider = (Provider) providerCombo.getSelectedItem();
            ProviderUtil.removeProviderProperties(persistenceUnit);
            
            if (!(provider instanceof DefaultProvider)) {
                persistenceUnit.setProvider(provider.getProviderClass());
                setTableGeneration();
            }
            ProviderUtil.makePortableIfPossible(project, persistenceUnit);
            
        } else if (libraryComboBox.getSelectedItem() instanceof Provider){
            Provider provider = (Provider) libraryComboBox.getSelectedItem();
            ProviderUtil.removeProviderProperties(persistenceUnit);
            if (!(provider instanceof DefaultProvider)) {
                ProviderUtil.setProvider(persistenceUnit, provider, getSelectedConnection(), getTableGeneration());
            }
        }
    }
    
    private void setDataSource(String name) {
        
        String jndiName = name;
        
        if (jndiName == null) {
            int itemIndex = dsCombo.getSelectedIndex();
            Object item = dsCombo.getSelectedItem();
            if (item instanceof JPADataSource)
                jndiName = ((JPADataSource)item).getJndiName();
            else if (itemIndex == -1 && item != null){ // user input
                jndiName = item.toString();
            }
        }
        
        if (jndiName == null){
            return;
        }
        
        if (isJta()){
            persistenceUnit.setJtaDataSource(jndiName);
            persistenceUnit.setNonJtaDataSource(null);
        } else {
            persistenceUnit.setJtaDataSource(null);
            persistenceUnit.setNonJtaDataSource(jndiName);
        }
    }
    
    private boolean isJta(){
        return jtaCheckBox.isEnabled() && jtaCheckBox.isSelected();
    }
    
    /**
     *@return selected table generation strategy.
     */
    private String getTableGeneration(){
        if (ddCreate.isSelected()){
            return Provider.TABLE_GENERATION_CREATE;
        } else if (ddDropCreate.isSelected()){
            return Provider.TABLE_GENERATION_DROPCREATE;
        } else {
            return Provider.TABLE_GENERATTION_UNKOWN;
        }
    }
    
    private DatabaseConnection getSelectedConnection(){
        DatabaseConnection connection = null;
        if (jdbcComboBox.getSelectedItem() instanceof DatabaseConnection){
            connection = (DatabaseConnection) jdbcComboBox.getSelectedItem();
        }
        return connection;
        
    }
    
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (nameTextField == source) {
            nameTextField.setText(persistenceUnit.getName());
        } else if (dsCombo.getEditor().getEditorComponent() == source){
            String jndiName = (isJta() ? persistenceUnit.getJtaDataSource() : persistenceUnit.getNonJtaDataSource());
            selectDatasource(jndiName);
        }
    }
    
    private void selectDatasource(String jndiName) {
        
        Object item = findDatasource(jndiName);
        dsCombo.setSelectedItem(item);
        if (dsCombo.getEditor() != null) { // item must be set in the editor
            dsCombo.configureEditor(dsCombo.getEditor(), item);
        }
    }
    
    private Object findDatasource(String jndiName) {
        
        if (jndiName != null) {
            int nItems = dsCombo.getItemCount();
            for (int i = 0; i < nItems; i++) {
                Object item = dsCombo.getItemAt(i);
                if (item instanceof JPADataSource && jndiName.equals(((JPADataSource)item).getJndiName())) {
                    return (JPADataSource)item;
                }
            }
        }
        
        return jndiName;
    }
    
    protected void endUIChange() {
        dObj.modelUpdatedFromUI();
    }
    
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("name".equals(errorId)) {
            return nameTextField;
        }
        return null;
    }
    
    private void handleCmAmSelection() {
        boolean isCm = isContainerManaged;
        datasourceLabel.setEnabled(isCm);
        dsCombo.setEnabled(isCm);
        jtaCheckBox.setEnabled(isCm && !Util.isJavaSE(project));
        libraryLabel.setEnabled(!isCm);
        libraryComboBox.setEnabled(!isCm);
        jdbcLabel.setEnabled(!isCm);
        jdbcComboBox.setEnabled(!isCm);
        setTableGeneration();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
        includeAllEntities = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        entityList = new javax.swing.JList();
        addClassButton = new javax.swing.JButton();
        removeClassButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        datasourceLabel = new javax.swing.JLabel();
        jtaCheckBox = new javax.swing.JCheckBox();
        dsCombo = new javax.swing.JComboBox();
        providerCombo = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        libraryLabel = new javax.swing.JLabel();
        jdbcLabel = new javax.swing.JLabel();
        libraryComboBox = new javax.swing.JComboBox();
        jdbcComboBox = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        tableGenerationLabel = new javax.swing.JLabel();
        ddCreate = new javax.swing.JRadioButton();
        ddDropCreate = new javax.swing.JRadioButton();
        ddUnknown = new javax.swing.JRadioButton();

        jPanel2.setOpaque(false);
        includeAllEntities.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/unit/Bundle").getString("LBL_IncludeAllEntities"));
        includeAllEntities.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        includeAllEntities.setMargin(new java.awt.Insets(0, 0, 0, 0));
        includeAllEntities.setOpaque(false);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(includeAllEntities)
                .addContainerGap(395, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(includeAllEntities)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setOpaque(false);
        jScrollPane2.setViewportView(entityList);

        addClassButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/unit/Bundle").getString("LBL_AddClasses"));
        addClassButton.setOpaque(false);
        addClassButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addClassButtonActionPerformed(evt);
            }
        });

        removeClassButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/unit/Bundle").getString("LBL_RemoveClass"));
        removeClassButton.setOpaque(false);
        removeClassButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeClassButtonActionPerformed(evt);
            }
        });

        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/unit/Bundle").getString("LBL_IncludeEntityClasses"));

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 449, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(addClassButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(removeClassButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 20, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(addClassButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeClassButton))
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 96, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setOpaque(false);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitPanel.class, "LBL_UnitName"));

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .add(32, 32, 32)
                .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 440, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(27, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel3.setLayout(new java.awt.CardLayout());

        jPanel3.setOpaque(false);
        jPanel6.setOpaque(false);
        datasourceLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitPanel.class, "LBL_DatasourceName"));

        jtaCheckBox.setSelected(true);
        jtaCheckBox.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitPanel.class, "LBL_JTA"));
        jtaCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jtaCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jtaCheckBox.setOpaque(false);

        dsCombo.setEditable(true);

        providerCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel3.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("LBL_Provider"));

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(datasourceLabel))
                        .add(43, 43, 43)
                        .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(providerCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(dsCombo, 0, 436, Short.MAX_VALUE)))
                    .add(jtaCheckBox))
                .add(79, 79, 79))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(providerCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(datasourceLabel)
                    .add(dsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jtaCheckBox)
                .add(48, 48, 48))
        );
        jPanel3.add(jPanel6, "container");

        jPanel5.setOpaque(false);
        libraryLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitPanel.class, "LBL_PersistenceLibrary"));

        jdbcLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitPanel.class, "LBL_JdbcConnection"));

        libraryComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                libraryComboBoxActionPerformed(evt);
            }
        });

        jdbcComboBox.setRenderer(new JdbcListCellRenderer());

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(libraryLabel)
                    .add(jdbcLabel))
                .add(47, 47, 47)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jdbcComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(libraryComboBox, 0, 442, Short.MAX_VALUE))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(libraryLabel)
                    .add(libraryComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jdbcLabel)
                    .add(jdbcComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        jPanel3.add(jPanel5, "application");

        jPanel1.setOpaque(false);
        tableGenerationLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/unit/Bundle").getString("LBL_TableGeneration"));

        buttonGroup2.add(ddCreate);
        ddCreate.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/unit/Bundle").getString("LBL_Create"));
        ddCreate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ddCreate.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ddCreate.setOpaque(false);

        buttonGroup2.add(ddDropCreate);
        ddDropCreate.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/unit/Bundle").getString("LBL_DropCreate"));
        ddDropCreate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ddDropCreate.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ddDropCreate.setOpaque(false);

        buttonGroup2.add(ddUnknown);
        ddUnknown.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/unit/Bundle").getString("LBL_None"));
        ddUnknown.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ddUnknown.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ddUnknown.setOpaque(false);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(tableGenerationLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ddCreate)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ddDropCreate)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ddUnknown)
                .addContainerGap(272, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ddCreate)
                    .add(ddDropCreate)
                    .add(ddUnknown)
                    .add(tableGenerationLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 98, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void removeClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeClassButtonActionPerformed
        Object[] values = entityList.getSelectedValues();
        for (Object value : values) {
            dObj.removeClass(persistenceUnit, (String)value);
            ((DefaultListModel)entityList.getModel()).removeElement(value);
        }
    }//GEN-LAST:event_removeClassButtonActionPerformed
    
    private void addClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClassButtonActionPerformed
        String[] existingClasses = persistenceUnit.getClass2();
        AddEntityDialog dialog = new AddEntityDialog(project, existingClasses);
        for (String entityClass : dialog.open()) {
            if (dObj.addClass(persistenceUnit, entityClass)){
                ((DefaultListModel)entityList.getModel()).addElement(entityClass);
            }
        }
    }//GEN-LAST:event_addClassButtonActionPerformed
    
    private void libraryComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_libraryComboBoxActionPerformed
        //        setProvider();
        ////        setSelectedLibrary();
        //        setTableGeneration();
    }//GEN-LAST:event_libraryComboBoxActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addClassButton;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JLabel datasourceLabel;
    private javax.swing.JRadioButton ddCreate;
    private javax.swing.JRadioButton ddDropCreate;
    private javax.swing.JRadioButton ddUnknown;
    private javax.swing.JComboBox dsCombo;
    private javax.swing.JList entityList;
    private javax.swing.JCheckBox includeAllEntities;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JComboBox jdbcComboBox;
    private javax.swing.JLabel jdbcLabel;
    private javax.swing.JCheckBox jtaCheckBox;
    private javax.swing.JComboBox libraryComboBox;
    private javax.swing.JLabel libraryLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JComboBox providerCombo;
    private javax.swing.JButton removeClassButton;
    private javax.swing.JLabel tableGenerationLabel;
    // End of variables declaration//GEN-END:variables
    
}

