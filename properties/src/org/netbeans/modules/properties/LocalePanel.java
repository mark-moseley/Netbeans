/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.properties;


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Locale;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * Panel to allow user convenient to choose locale.
 *
 * @author  Peter Zavadsky
 * @see java.util.Locale
 */
public class LocalePanel extends JPanel {

    /** Variable holding actually chosen <code>Locale</code> object. */
    private Locale locale;

    /** Property name of <code>locale</code> property. */
    public static final String PROP_CUSTOMIZED_LOCALE = "customized_locale"; // NOI18N
    
    /** Array representing supported locales by Java platform.
     * You can find them at <a href="http://java.sun.com/j2se/1.3/docs/guide/intl/locale.doc.html">Supported Locales.</a>
     * This should be taken as temporary solution. And revised after announced changes in API dealing with supported
     * locales.
     */
    private static final Locale[] supportedLocales = new Locale[] {
        new Locale("ar", "AE", ""), // NOI18N // Arabic United Arab Emirates 
        new Locale("ar", "BH", ""), // NOI18N // Arabic Bahrain
        new Locale("ar", "DZ", ""), // NOI18N // Arabic Algeria 
        new Locale("ar", "EG", ""), // NOI18N // Arabic Egypt
        new Locale("ar", "IQ", ""), // NOI18N // Arabic Iraq 
        new Locale("ar", "JO", ""), // NOI18N // Arabic Jordan 
        new Locale("ar", "KW", ""), // NOI18N // Arabic Kuwait 
        new Locale("ar", "LB", ""), // NOI18N // Arabic Lebanon 
        new Locale("ar", "LY", ""), // NOI18N // Arabic Libya 
        new Locale("ar", "MA", ""), // NOI18N // Arabic Morocco 
        new Locale("ar", "OM", ""), // NOI18N // Arabic Oman 
        new Locale("ar", "QA", ""), // NOI18N // Arabic Qatar 
        new Locale("ar", "SA", ""), // NOI18N // Arabic Saudi Arabia 
        new Locale("ar", "SD", ""), // NOI18N // Arabic Sudan 
        new Locale("ar", "SY", ""), // NOI18N // Arabic Syria 
        new Locale("ar", "TN", ""), // NOI18N // Arabic Tunisia 
        new Locale("ar", "YE", ""), // NOI18N // Arabic Yemen 
        new Locale("be", "BY", ""), // NOI18N // Byelorussian Byelorussia 
        new Locale("bg", "BG", ""), // NOI18N // Bulgarian Bulgaria 
        new Locale("ca", "ES", ""), // NOI18N // Catalan Spain 
        new Locale("cs", "CZ", ""), // NOI18N // Czech Czech Republic 
        new Locale("da", "DK", ""), // NOI18N // Danish Denmark 
        new Locale("de", "AT", ""), // NOI18N // German Austria 
        new Locale("de", "AT", "EURO"), // NOI18N // German Austria 
        new Locale("de", "CH", ""), // NOI18N // German Switzerland 
        new Locale("de", "DE", ""), // NOI18N // German Germany 
        new Locale("de", "DE", "EURO"), // NOI18N // German Germany 
        new Locale("de", "LU", ""), // NOI18N // German Luxembourg 
        new Locale("de", "LU", "EURO"), // NOI18N // German Luxembourg 
        new Locale("el", "GR", ""), // NOI18N // Greek Greece 
        new Locale("en", "AU", ""), // NOI18N // English Australia 
        new Locale("en", "CA", ""), // NOI18N // English Canada 
        new Locale("en", "GB", ""), // NOI18N // English United Kingdom 
        new Locale("en", "IE", ""), // NOI18N // English Ireland 
        new Locale("en", "IE", "EURO"), // NOI18N // English Ireland 
        new Locale("en", "NZ", ""), // NOI18N // English New Zealand 
        new Locale("en", "US", ""), // NOI18N // English United States 
        new Locale("en", "ZA", ""), // NOI18N // English South Africa 
        new Locale("es", "AR", ""), // NOI18N // Spanish Argentina 
        new Locale("es", "BO", ""), // NOI18N // Spanish Bolivia 
        new Locale("es", "CL", ""), // NOI18N // Spanish Chile 
        new Locale("es", "CO", ""), // NOI18N // Spanish Colombia 
        new Locale("es", "CR", ""), // NOI18N // Spanish Costa Rica 
        new Locale("es", "DO", ""), // NOI18N // Spanish Dominican Republic 
        new Locale("es", "EC", ""), // NOI18N // Spanish Ecuador 
        new Locale("es", "ES", ""), // NOI18N // Spanish Spain 
        new Locale("es", "ES", "EURO"), // NOI18N // Spanish Spain 
        new Locale("es", "GT", ""), // NOI18N // Spanish Guatemala 
        new Locale("es", "HN", ""), // NOI18N // Spanish Honduras 
        new Locale("es", "MX", ""), // NOI18N // Spanish Mexico 
        new Locale("es", "NI", ""), // NOI18N // Spanish Nicaragua 
        new Locale("es", "PA", ""), // NOI18N // Spanish Panama 
        new Locale("es", "PE", ""), // NOI18N // Spanish Peru 
        new Locale("es", "PR", ""), // NOI18N // Spanish Puerto Rico 
        new Locale("es", "PY", ""), // NOI18N // Spanish Paraguay 
        new Locale("es", "SV", ""), // NOI18N // Spanish El Salvador 
        new Locale("es", "UY", ""), // NOI18N // Spanish Uruguay 
        new Locale("es", "VE", ""), // NOI18N // Spanish Venezuela 
        new Locale("et", "EE", ""), // NOI18N // Estonian Estonia 
        new Locale("fi", "FI", ""), // NOI18N // Finnish Finland 
        new Locale("fi", "FI", "EURO"), // NOI18N // Finnish Finland 
        new Locale("fr", "BE", ""), // NOI18N // French Belgium 
        new Locale("fr", "BE", "EURO"), // NOI18N // French Belgium 
        new Locale("fr", "CA", ""), // NOI18N // French Canada 
        new Locale("fr", "CH", ""), // NOI18N // French Switzerland 
        new Locale("fr", "FR", ""), // NOI18N // French France 
        new Locale("fr", "FR", "EURO"), // NOI18N // French France 
        new Locale("fr", "LU", ""), // NOI18N // French Luxembourg 
        new Locale("fr", "LU", "EURO"), // NOI18N // French Luxembourg 
        new Locale("hr", "HR", ""), // NOI18N // Croatian Croatia 
        new Locale("hu", "HU", ""), // NOI18N // Hungarian Hungary 
        new Locale("is", "IS", ""), // NOI18N // Icelandic Iceland 
        new Locale("it", "CH", ""), // NOI18N // Italian Switzerland 
        new Locale("it", "IT", ""), // NOI18N // Italian Italy 
        new Locale("it", "IT", "EURO"), // NOI18N // Italian Italy 
        new Locale("iw", "IL", ""), // NOI18N // Hebrew Israel 
        new Locale("ja", "JP", ""), // NOI18N // Japanese Japan 
        new Locale("ko", "KR", ""), // NOI18N // Korean Korea 
        new Locale("lt", "LT", ""), // NOI18N // Lithuanian Lithuania 
        new Locale("lv", "LV", ""), // NOI18N // Latvian Latvia 
        new Locale("mk", "MK", ""), // NOI18N // Macedonian Macedonia 
        new Locale("nl", "BE", ""), // NOI18N // Dutch Belgium 
        new Locale("nl", "BE", "EURO"), // NOI18N // Dutch Belgium 
        new Locale("nl", "NL", ""), // NOI18N // Dutch Netherlands 
        new Locale("nl", "NL", "EURO"), // NOI18N // Dutch Netherlands 
        new Locale("no", "NO", ""), // NOI18N // Norwegian (Nynorsk) Norway 
        new Locale("no", "NO", "B"), // NOI18N // Norwegian (Bokm�l) Norway 
        new Locale("pl", "PL", ""), // NOI18N // Polish Poland 
        new Locale("pt", "BR", ""), // NOI18N // Portuguese Brazil 
        new Locale("pt", "PT", ""), // NOI18N // Portuguese Portugal 
        new Locale("pt", "PT", "EURO"), // NOI18N // Portuguese Portugal 
        new Locale("ro", "RO", ""), // NOI18N // Romanian Romania 
        new Locale("ru", "RU", ""), // NOI18N // Russian Russia 
        new Locale("sh", "YU", ""), // NOI18N // Serbo-Croatian Yugoslavia 
        new Locale("sk", "SK", ""), // NOI18N // Slovakian Slovakia 
        new Locale("sl", "SL", ""), // NOI18N // Slovenian Slovenia 
        new Locale("sq", "AL", ""), // NOI18N // Albanian Albania 
        new Locale("sr", "YU", ""), // NOI18N // Serbian (Cyrillic) Yugoslavia 
        new Locale("sv", "SE", ""), // NOI18N // Swedish Sweden 
        new Locale("th", "TH", ""), // NOI18N // Thai Thailand 
        new Locale("tr", "TR", ""), // NOI18N // Turkish Turkey 
        new Locale("uk", "UA", ""), // NOI18N // Ukranian Ukraine 
        new Locale("zh", "CN", ""), // NOI18N // Chinese (Simplified) China 
        new Locale("zh", "HK", ""), // NOI18N // Chinese Hong Kong 
        new Locale("zh", "TW", ""), // NOI18N // Chinese (Traditional) Taiwan
    };

    
    /** Creates new <code>LocalePanel</code>. */
    public LocalePanel() {
        this(new Locale("", "", "")); // NOI18N
    }
    
    /** <code>Creates new LocalePanel</code>. */
    public LocalePanel(Locale locale) {
        this.locale = locale;
        
        initComponents();
        
        languageCombo.setSelectedItem(locale.getLanguage());
        countryCombo.setSelectedItem(locale.getCountry());
        variantCombo.setSelectedItem(locale.getVariant());
        
        localeText.setText(locale.toString());
        
        HelpCtx.setHelpIDString(this, Util.HELP_ID_ADDLOCALE);
    }

    
    /** Getter for <code>locale</code> property. */
    public Locale getLocale() {
        return locale;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        languageLabel = new javax.swing.JLabel();
        languageCombo = new JComboBox(Locale.getISOLanguages());
        countryLabel = new javax.swing.JLabel();
        countryCombo = new JComboBox(Locale.getISOCountries());
        variantLabel = new javax.swing.JLabel();
        variantCombo = new JComboBox(new String[] {
            "B", // Bokmal // NOI18N
            "EURO", // EURO // NOI18N
            "NY" // Nynorsk // NOI18N
        });
        
        supportedLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        supportedList = new JList(supportedLocales);
        localeLabel = new javax.swing.JLabel();
        localeText = new javax.swing.JTextField();
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        languageLabel.setText(NbBundle.getBundle(LocalePanel.class).getString("CTL_LanguageCode"));
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.insets = new java.awt.Insets(11, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(languageLabel, gridBagConstraints1);
        
        
        languageCombo.setEditable(true);
        languageCombo.setRenderer(new BasicComboBoxRenderer() {
            public Component getListCellRendererComponent(
            JList list,
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // the list and the cell have the focus
            {
                JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if("".equals(value.toString())) // NOI18N
                    label.setText(""); // NOI18N
                else
                    label.setText(value.toString() + " - " + new Locale((String)value, "", "").getDisplayLanguage()); // NOI18N
                
                return label;
            }
        });
        // Insert empty string at the beginning.
        languageCombo.insertItemAt("", 0); // NOI18N
        languageCombo.setSelectedIndex(0);
        languageCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                languageComboActionPerformed(evt);
            }
        }
        );
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(11, 7, 0, 11);
        gridBagConstraints1.weightx = 1.0;
        add(languageCombo, gridBagConstraints1);
        
        
        countryLabel.setText(NbBundle.getBundle(LocalePanel.class).getString("CTL_CountryCode"));
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.insets = new java.awt.Insets(7, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(countryLabel, gridBagConstraints1);
        
        
        countryCombo.setEditable(true);
        countryCombo.setRenderer(new BasicComboBoxRenderer() {
            public Component getListCellRendererComponent(
            JList list,
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // the list and the cell have the focus
            {
                JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if("".equals(value.toString())) // NOI18N
                    label.setText(""); // NOI18N
                else
                    label.setText(value.toString() + " - " + new Locale("", (String)value, "").getDisplayCountry()); // NOI18N
                
                return label;
            }
        });
        // Insert empty string at the beginning.
        countryCombo.insertItemAt("", 0); // NOI18N
        countryCombo.setSelectedIndex(0);
        countryCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countryComboActionPerformed(evt);
            }
        }
        );
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(7, 7, 0, 11);
        gridBagConstraints1.weightx = 1.0;
        add(countryCombo, gridBagConstraints1);
        
        
        variantLabel.setText(NbBundle.getBundle(LocalePanel.class).getString("CTL_Variant"));
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.insets = new java.awt.Insets(7, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(variantLabel, gridBagConstraints1);
        
        
        variantCombo.setEditable(true);
        variantCombo.setRenderer(new BasicComboBoxRenderer() {
            public Component getListCellRendererComponent(
            JList list,
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // the list and the cell have the focus
            {
                JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if("".equals(value.toString())) // NOI18N
                    label.setText(""); // NOI18N
                else
                    label.setText(value.toString() + " - " + new Locale("", "", (String)value).getDisplayVariant()); // NOI18N
                
                return label;
            }
        });
        // Insert empty string at the beginning.
        variantCombo.insertItemAt("", 0); // NOI18N
        variantCombo.setSelectedIndex(0);
        variantCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                variantComboActionPerformed(evt);
            }
        }
        );
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(7, 7, 0, 11);
        gridBagConstraints1.weightx = 1.0;
        add(variantCombo, gridBagConstraints1);
        
        
        supportedLabel.setText(NbBundle.getBundle(LocalePanel.class).getString("CTL_SupportedLocales"));
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 4;
        gridBagConstraints1.gridwidth = 3;
        gridBagConstraints1.insets = new java.awt.Insets(17, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(supportedLabel, gridBagConstraints1);
        
        
        
        supportedList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(
            JList list,
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // the list and the cell have the focus
            {
                JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                Locale locale = (Locale)value;
                
                label.setText(locale.toString() +
                (locale.getLanguage().equals("") ? "" : " - " + locale.getDisplayLanguage()) + // NOI18N
                (locale.getCountry().equals("") ? "" : " / " + locale.getDisplayCountry()) + // NOI18N
                (locale.getVariant().equals("") ? "" : " / " + locale.getDisplayVariant()) // NOI18N
                );
                
                return label;
            }
        });
        supportedList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        supportedList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                supportedListValueChanged(evt);
            }
        }
        );
        jScrollPane1.setViewportView(supportedList);
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 5;
        gridBagConstraints1.gridwidth = 3;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(7, 7, 11, 11);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints1);
        
        
        localeLabel.setText(NbBundle.getBundle(LocalePanel.class).getString("CTL_Locale"));
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(localeLabel, gridBagConstraints1);
        
        
        localeText.setEditable(false);
        localeText.setText("jTextField1");
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 7, 0, 11);
        gridBagConstraints1.weightx = 1.0;
        add(localeText, gridBagConstraints1);
        
    }//GEN-END:initComponents

    private void supportedListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_supportedListValueChanged
        Locale selectedLocale = (Locale)supportedList.getSelectedValue();
        
        languageCombo.setSelectedItem(selectedLocale.getLanguage());
        countryCombo.setSelectedItem(selectedLocale.getCountry());
        variantCombo.setSelectedItem(selectedLocale.getVariant());
    }//GEN-LAST:event_supportedListValueChanged

    private void variantComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_variantComboActionPerformed
        comboHandler(evt);
    }//GEN-LAST:event_variantComboActionPerformed

    private void countryComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countryComboActionPerformed
        comboHandler(evt);
    }//GEN-LAST:event_countryComboActionPerformed

    private void languageComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_languageComboActionPerformed
        comboHandler(evt);
    }//GEN-LAST:event_languageComboActionPerformed

    /** Combo box event handler. Helper method. */
    private void comboHandler(ActionEvent evt) {
        String str = (String)((JComboBox)evt.getSource()).getSelectedItem();

        Locale oldLocale = locale;
        
        Object source = evt.getSource();
        if(source.equals(languageCombo)) {
            // 99% trick to avoid unneccessary reset of the string (kind of event filter) ,
            // e.g. when loosing the focus and was choosen item from list right before etc.
            if(str.equals(locale.getLanguage()))
                return;
            
            locale = new Locale(str, locale.getCountry(), locale.getVariant());
        } else if(source.equals(countryCombo)) {
            if(str.equals(locale.getCountry()))
                return;
            
            locale = new Locale(locale.getLanguage(), str, locale.getVariant());
        } else if(source.equals(variantCombo)) {
            if(str.equals(locale.getVariant()))
                return;
            
            locale = new Locale(locale.getLanguage(), locale.getCountry(), str);
        }

        localeText.setText(locale.toString());

        firePropertyChange(PROP_CUSTOMIZED_LOCALE, oldLocale, locale);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel languageLabel;
    private javax.swing.JComboBox languageCombo;
    private javax.swing.JLabel countryLabel;
    private javax.swing.JComboBox countryCombo;
    private javax.swing.JLabel variantLabel;
    private javax.swing.JComboBox variantCombo;
    private javax.swing.JLabel supportedLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList supportedList;
    private javax.swing.JLabel localeLabel;
    private javax.swing.JTextField localeText;
    // End of variables declaration//GEN-END:variables

}
