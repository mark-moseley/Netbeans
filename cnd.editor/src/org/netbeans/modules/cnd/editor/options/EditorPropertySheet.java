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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.options;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.reformat.Reformatter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  as204739
 */
public class EditorPropertySheet extends javax.swing.JPanel implements ActionListener, PropertyChangeListener, PreferenceChangeListener {
    
    private static final boolean USE_NEW_FORMATTER = true;
    private static final boolean TRACE = false;
    
    private EditorOptionsPanelController topControler;
    private boolean loaded = false;
    private CodeStyle.Language currentLanguage;
    private String lastChangedproperty;
    private Map<CodeStyle.Language, String> defaultStyles = new HashMap<CodeStyle.Language, String>();
    private Map<CodeStyle.Language, Map<String,PreviewPreferences>> allPreferences = new HashMap<CodeStyle.Language, Map<String, PreviewPreferences>>();
    private PropertySheet holder;


    EditorPropertySheet(EditorOptionsPanelController topControler) {
        this.topControler = topControler;
        initComponents();

        holder = new PropertySheet();
        holder.setOpaque(false);
        holder.setDescriptionAreaVisible(false);
        GridBagConstraints fillConstraints = new GridBagConstraints();
        fillConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        fillConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        fillConstraints.fill = java.awt.GridBagConstraints.BOTH;
        fillConstraints.weightx = 1.0;
        fillConstraints.weighty = 1.0;
        categoryPanel.add(holder, fillConstraints);
        
        manageStyles.setMinimumSize(new Dimension(126,26));
        setName("Tab_Name"); // NOI18N (used as a bundle key)
        if( "Windows".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            setOpaque( false );
        }
        previewPane.setContentType("text/x-c++"); // NOI18N
        // Don't highlight caret row 
        previewPane.putClientProperty(
            "HighlightsLayerExcludes", // NOI18N
            "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" // NOI18N
        );
        previewPane.setText("1234567890123456789012345678901234567890"); // NOI18N
        previewPane.setDoubleBuffered(true);
        initLanguageComboBox();
    }

    private void initLanguageComboBox(){
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(CodeStyle.Language.C);
        model.addElement(CodeStyle.Language.CPP);
        languagesComboBox.setModel(model);
        currentLanguage = CodeStyle.Language.C;
        languagesComboBox.setSelectedIndex(0);
        languagesComboBox.addActionListener(this);
    }

    private void initLanguageMap(){
        for(String style:EditorOptions.getAllStyles(CodeStyle.Language.C)){
            initLanguageStylePreferences(CodeStyle.Language.C, style);
        }
        defaultStyles.put(CodeStyle.Language.C, EditorOptions.getCurrentProfileId(CodeStyle.Language.C));

        for(String style:EditorOptions.getAllStyles(CodeStyle.Language.CPP)){
            initLanguageStylePreferences(CodeStyle.Language.CPP, style);
        }
        defaultStyles.put(CodeStyle.Language.CPP, EditorOptions.getCurrentProfileId(CodeStyle.Language.CPP));
    }
    
    private void initLanguageStylePreferences(CodeStyle.Language language, String styleId){
        Map<String, PreviewPreferences> map = allPreferences.get(language);
        if (map == null){
            map = new HashMap<String, PreviewPreferences>();
            allPreferences.put(language, map);
        }
        PreviewPreferences clone = new PreviewPreferences(
                                   EditorOptions.getPreferences(language, styleId), language, styleId);
        map.put(styleId, clone);
    }
    

    private void initLanguageCategory(){
        styleComboBox.removeActionListener(this);
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        Map<String, PreviewPreferences> map = allPreferences.get(currentLanguage);
        String currentProfile = defaultStyles.get(currentLanguage);
        List<EntryWrapper> list = new ArrayList<EntryWrapper>();
        for(Map.Entry<String, PreviewPreferences> entry : map.entrySet()) {
            list.add(new EntryWrapper(entry));
        }
        Collections.sort(list);
        int index = 0;
        int i = 0;
        for(EntryWrapper entry : list) {
            if (entry.name.equals(currentProfile)) {
                index = i;
            }
            model.addElement(entry);
            i++;
        }
        styleComboBox.setModel(model);
        styleComboBox.setSelectedIndex(index);
        EntryWrapper entry = (EntryWrapper)styleComboBox.getSelectedItem();
        initSheets(entry.preferences);
        currentProfile = entry.name;
        defaultStyles.put(currentLanguage, currentProfile);
        styleComboBox.addActionListener(this);
        repaintPreview();
    }
    
    private PreviewPreferences lastSheetPreferences = null;
    
    private void initSheets(PreviewPreferences preferences){
        if (TRACE) {
            System.out.println("Set properties for "+preferences.getLanguage()+" "+preferences.getStyleId()); // NOI18N
        }
        if (lastSheetPreferences != null){
            lastSheetPreferences.removePreferenceChangeListener(this);
        }
	Sheet sheet = new Sheet();
	Sheet.Set set = new Sheet.Set();
	set.setName("Indents"); // NOI18N
	set.setDisplayName(getString("LBL_TabsAndIndents")); // NOI18N
        set.setShortDescription(getString("HINT_TabsAndIndents")); // NOI18N
	set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.indentSize));
	set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.statementContinuationIndent));
	set.put(new PreprocessorIndentProperty(currentLanguage, preferences, EditorOptions.indentPreprocessorDirectives));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.sharpAtStartLine));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.indentNamespace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.indentCasesFromSwitch));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.absoluteLabelIndent));
        sheet.put(set);
        
	set = new Sheet.Set();
	set.setName("BracesPlacement"); // NOI18N
	set.setDisplayName(getString("LBL_BracesPlacement")); // NOI18N
	set.setShortDescription(getString("HINT_BracesPlacement")); // NOI18N
	set.put(new BracePlacementProperty(currentLanguage, preferences, EditorOptions.newLineBeforeBraceNamespace));
	set.put(new BracePlacementProperty(currentLanguage, preferences, EditorOptions.newLineBeforeBraceClass));
	set.put(new BracePlacementProperty(currentLanguage, preferences, EditorOptions.newLineBeforeBraceDeclaration));
	set.put(new BracePlacementProperty(currentLanguage, preferences, EditorOptions.newLineBeforeBraceSwitch));
	set.put(new BracePlacementProperty(currentLanguage, preferences, EditorOptions.newLineBeforeBrace));
        sheet.put(set);
        
	set = new Sheet.Set();
	set.setName("MultilineAlignment"); // NOI18N
	set.setDisplayName(getString("LBL_MultilineAlignment")); // NOI18N
	set.setShortDescription(getString("HINT_MultilineAlignment")); // NOI18N
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.alignMultilineMethodParams));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.alignMultilineCallArgs));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.alignMultilineArrayInit));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.alignMultilineFor));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.alignMultilineIfCondition));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.alignMultilineWhileCondition));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.alignMultilineParen));
        sheet.put(set);

        set = new Sheet.Set();
	set.setName("NewLine"); // NOI18N
	set.setDisplayName(getString("LBL_NewLine")); // NOI18N
	set.setShortDescription(getString("HINT_NewLine")); // NOI18N
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.newLineFunctionDefinitionName));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.newLineCatch));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.newLineElse));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.newLineWhile));
        sheet.put(set);
        
        set = new Sheet.Set();
	set.setName("SpacesBeforeKeywords"); // NOI18N
	set.setDisplayName(getString("LBL_BeforeKeywords")); // NOI18N
	set.setShortDescription(getString("HINT_BeforeKeywords")); // NOI18N
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeCatch));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeElse));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeWhile));
        sheet.put(set);
        
        set = new Sheet.Set();
	set.setName("SpacesBeforeParentheses"); // NOI18N
	set.setDisplayName(getString("LBL_BeforeParentheses")); // NOI18N
	set.setShortDescription(getString("HINT_BeforeParentheses")); // NOI18N
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeMethodDeclParen));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeMethodCallParen));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeCatchParen));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeForParen));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeIfParen));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeSwitchParen));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeWhileParen));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeKeywordParen));
        sheet.put(set);
        
        set = new Sheet.Set();
	set.setName("SpacesAroundOperators"); // NOI18N
	set.setDisplayName(getString("LBL_AroundOperators")); // NOI18N
	set.setShortDescription(getString("HINT_AroundOperators")); // NOI18N
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAroundAssignOps));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAroundBinaryOps));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAroundTernaryOps));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAroundUnaryOps));
        sheet.put(set);
    
        set = new Sheet.Set();
	set.setName("SpacesBeforeLeftBracess"); // NOI18N
	set.setDisplayName(getString("LBL_BeforeLeftBraces")); // NOI18N
	set.setShortDescription(getString("HINT_BeforeLeftBraces")); // NOI18N
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeClassDeclLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeMethodDeclLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeArrayInitLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeCatchLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeDoLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeElseLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeForLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeIfLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeSwitchLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeTryLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeWhileLeftBrace));
        sheet.put(set);

        set = new Sheet.Set();
	set.setName("SpacesWithinParentheses"); // NOI18N
	set.setDisplayName(getString("LBL_WithinParentheses")); // NOI18N
	set.setShortDescription(getString("HINT_WithinParentheses")); // NOI18N
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinMethodDeclParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinMethodCallParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinBraces));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinCatchParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinForParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinIfParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinSwitchParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinTypeCastParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinWhileParens));
        sheet.put(set);
                
        set = new Sheet.Set();
	set.setName("SpacesOther"); // NOI18N
	set.setDisplayName(getString("LBL_Other_Spaces")); // NOI18N
	set.setShortDescription(getString("HINT_Other_Spaces")); // NOI18N
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeComma));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAfterComma));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeSemi));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAfterSemi));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeColon));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAfterColon));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAfterTypeCast));
        sheet.put(set);
        
        set = new Sheet.Set();
	set.setName("BlankLines"); // NOI18N
	set.setDisplayName(getString("LBL_BlankLines")); // NOI18N
	set.setShortDescription(getString("HINT_BlankLines")); // NOI18N
	set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesBeforeClass));
	//set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesAfterClass));
	set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesAfterClassHeader));
	//set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesBeforeFields));
	//set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesAfterFields));
	set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesBeforeMethods));
	//set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesAfterMethods));
        sheet.put(set);
        
        set = new Sheet.Set();
	set.setName("Other"); // NOI18N
	set.setDisplayName(getString("LBL_Other")); // NOI18N
	set.setShortDescription(getString("HINT_Other")); // NOI18N
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.addLeadingStarInComment));
        sheet.put(set);

        if (TRACE && lastSheetPreferences != null) {
            //Because NPE in PropertySheet in 586 line: Arrays.asList(nodes)
            Logger.getLogger(PropertySheet.class.getName()).setLevel(Level.FINE);
        }
        DummyNode[] dummyNodes = new DummyNode[1];
        dummyNodes[0] = new DummyNode(sheet, "Sheet"); // NOI18N
        holder.setNodes(dummyNodes);
    
        preferences.addPreferenceChangeListener(this);
        lastSheetPreferences = preferences;
    }

    void load() {
        loaded = false;
        initLanguageMap();
        initLanguageCategory();
        loaded = true;
        repaintPreview();        
    }
    
    void store() {
        for(Map.Entry<CodeStyle.Language, Map<String,PreviewPreferences>> entry : allPreferences.entrySet()){
            CodeStyle.Language language = entry.getKey();
            Map<String,PreviewPreferences> map = entry.getValue();
            EditorOptions.setCurrentProfileId(language, defaultStyles.get(language));
            StringBuilder buf = new StringBuilder();
            for(Map.Entry<String,PreviewPreferences> prefEntry : map.entrySet()){
                String style = prefEntry.getKey();
                if (buf.length()>0){
                    buf.append(',');
                }
                buf.append(style);
                PreviewPreferences preferences = prefEntry.getValue();
                Preferences toSave = EditorOptions.getPreferences(language, style);
                if (style.equals(defaultStyles.get(language))){
                    EditorOptions.setPreferences(CodeStyle.getDefault(language), toSave);
                }
                for(String key : EditorOptions.keys()){
                    Object o = EditorOptions.getDefault(language, style, key);
                    if (o instanceof Boolean) {
                        Boolean v = preferences.getBoolean(key, (Boolean) o);
                        if (!o.equals(v)) {
                            toSave.putBoolean(key, v);
                        } else {
                            toSave.remove(key);
                        }
                    } else if (o instanceof Integer) {
                        Integer v = preferences.getInt(key, (Integer) o);
                        if (!o.equals(v)) {
                            toSave.putInt(key, v);
                        } else {
                            toSave.remove(key);
                        }
                    } else {
                        String v = preferences.get(key, o.toString());
                        if (!o.equals(v)) {
                            toSave.put(key, v);
                        } else {
                            toSave.remove(key);
                        }
                    }
                }
            }
            EditorOptions.setAllStyles(language, buf.toString());
        }
        defaultStyles.clear();
        allPreferences.clear();
        holder.setNodes(null);
    }
    
    void cancel() {
        defaultStyles.clear();
        allPreferences.clear();
        holder.setNodes(null);
    }

    // Change in the combo
    public void actionPerformed(ActionEvent e) {
        lastChangedproperty = null;
        if (styleComboBox.equals(e.getSource())){
            EntryWrapper category = (EntryWrapper)styleComboBox.getSelectedItem();
            if (category != null) {
                defaultStyles.put(currentLanguage,category.name);
                initSheets(category.preferences);
                repaintPreview();
            }
        } else if (languagesComboBox.equals(e.getSource())){
            currentLanguage = (CodeStyle.Language)languagesComboBox.getSelectedItem();
            initLanguageCategory();
        }
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        lastChangedproperty = evt.getKey();
        change();
    }
    

    // Change in some of the subpanels
    public void propertyChange(PropertyChangeEvent evt) {
        change();
    }

    private void change(){
        if ( !loaded ) {
            return;
        }
        Runnable run = new Runnable() {
            public void run() {
                // Notify the main controler that the page has changed
                topControler.changed();
                // Repaint the preview
                repaintPreview();
            }
        };
        if(SwingUtilities.isEventDispatchThread()){
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }
    
    private void repaintPreview() { 
        EntryWrapper category = (EntryWrapper)styleComboBox.getSelectedItem();
        if (category != null) {
            if (CodeStyle.Language.C.equals(currentLanguage)){
                previewPane.setContentType("text/x-c"); // NOI18N
            } else {
                previewPane.setContentType("text/x-c++"); // NOI18N
            }
            if (loaded) {
                PreviewPreferences p = new PreviewPreferences(category.preferences,
                                category.preferences.getLanguage(), category.preferences.getStyleId());
                p.makeAllKeys(category.preferences);
                jScrollPane1.setIgnoreRepaint(true);
                refreshPreview(previewPane, p);
                previewPane.setIgnoreRepaint(false);
                previewPane.scrollRectToVisible(new Rectangle(0,0,10,10) );
                previewPane.repaint(100);
            }
        }
    }
    
    private String getPreviwText(){
        String suffix;
        if (CodeStyle.Language.C.equals(currentLanguage)){
            suffix = ".c"; // NOI18N
        } else {
            suffix = ".cpp"; // NOI18N
        }
        if (lastChangedproperty != null) {
            if (lastChangedproperty.startsWith("space")) { // NOI18N
                return loadPreviewExample("SAMPLE_Spaces"+suffix); // NOI18N
            } else if (lastChangedproperty.startsWith("blank")) { // NOI18N
                return loadPreviewExample("SAMPLE_BlankLines"+suffix); // NOI18N
            }  else if (lastChangedproperty.startsWith("align") || // NOI18N
                        lastChangedproperty.startsWith("new")) { // NOI18N
                return loadPreviewExample("SAMPLE_AlignBraces"+suffix); // NOI18N
            }
        }
        return loadPreviewExample("SAMPLE_TabsIndents"+suffix); // NOI18N
    }
    
    private String loadPreviewExample(String example) {
        //return getString(example);
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject exampleFile = fs.findResource("OptionsDialog/CPlusPlus/FormatterPreviewExamples/" + example); //NOI18N
        if (exampleFile != null && exampleFile.getSize() > 0) {
            StringBuilder sb = new StringBuilder((int) exampleFile.getSize());
            try {
                InputStreamReader is = new InputStreamReader(exampleFile.getInputStream());
                char[] buffer = new char[1024];
                int size;
                try {
                    while (0 < (size = is.read(buffer, 0, buffer.length))) {
                        sb.append(buffer, 0, size);
                    }
                } finally {
                    is.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return sb.toString();
        } else {
            return ""; //NOI18N
        }
    }
    
    public void refreshPreview(JEditorPane pane, Preferences p) {
        pane.setText(getPreviwText());
        BaseDocument bd = (BaseDocument) pane.getDocument();
        if (USE_NEW_FORMATTER) {
            CodeStyle codeStyle = EditorOptions.createCodeStyle(currentLanguage, p);
            try {
                new Reformatter(bd, codeStyle).reformat();
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            CodeStyle codeStyle = CodeStyle.getDefault(currentLanguage);
            Preferences oldPreferences = EditorOptions.getPreferences(codeStyle);
            EditorOptions.setPreferences(codeStyle, p);
            Formatter f = bd.getFormatter();
            try {
                f.reformatLock();
                f.reformat(bd, 0, bd.getLength());
                String x = bd.getText(0, bd.getLength());
                pane.setText(x);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                EditorOptions.setPreferences(codeStyle, oldPreferences);
                f.reformatUnlock();
            }
        }
    }

    private static String getString(String key) {
        return NbBundle.getMessage(EditorPropertySheet.class, key);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked") // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel2 = new javax.swing.JLabel();
        languagesComboBox = new javax.swing.JComboBox();
        jSplitPane1 = new javax.swing.JSplitPane();
        oprionsPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        styleComboBox = new javax.swing.JComboBox();
        categoryPanel = new javax.swing.JPanel();
        manageStyles = new javax.swing.JButton();
        previewPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        previewPane = new javax.swing.JEditorPane();
        jSeparator1 = new javax.swing.JSeparator();

        setLayout(new java.awt.GridBagLayout());

        jLabel2.setLabelFor(languagesComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(EditorPropertySheet.class, "EditorPropertySheet.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(jLabel2, gridBagConstraints);

        languagesComboBox.setMinimumSize(new java.awt.Dimension(100, 18));
        languagesComboBox.setPreferredSize(new java.awt.Dimension(100, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(languagesComboBox, gridBagConstraints);

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setOpaque(false);

        oprionsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        oprionsPanel.setOpaque(false);
        oprionsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(styleComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EditorPropertySheet.class, "LBL_Style_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 6);
        oprionsPanel.add(jLabel1, gridBagConstraints);

        styleComboBox.setMaximumSize(new java.awt.Dimension(100, 25));
        styleComboBox.setPreferredSize(new java.awt.Dimension(100, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        oprionsPanel.add(styleComboBox, gridBagConstraints);

        categoryPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        categoryPanel.setOpaque(false);
        categoryPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        oprionsPanel.add(categoryPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(manageStyles, org.openide.util.NbBundle.getMessage(EditorPropertySheet.class, "EditorPropertySheet.manageStyles.text")); // NOI18N
        manageStyles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageStylesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 6);
        oprionsPanel.add(manageStyles, gridBagConstraints);

        jSplitPane1.setLeftComponent(oprionsPanel);

        previewPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        previewPanel.setOpaque(false);
        previewPanel.setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setViewportView(previewPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        previewPanel.add(jScrollPane1, gridBagConstraints);

        jSplitPane1.setRightComponent(previewPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jSplitPane1, gridBagConstraints);

        jSeparator1.setForeground(java.awt.SystemColor.activeCaptionBorder);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(jSeparator1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void manageStylesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageStylesActionPerformed
    Map<CodeStyle.Language, Map<String,PreviewPreferences>> clone =  clonePreferences();
    ManageStylesPanel stylesPanel = new ManageStylesPanel(currentLanguage, clone);
    DialogDescriptor dd = new DialogDescriptor(stylesPanel, getString("MANAGE_STYLES_DIALOG_TITLE")); // NOI18N
    DialogDisplayer.getDefault().notify(dd);
    if (dd.getValue() == DialogDescriptor.OK_OPTION) {
        allPreferences = clone;
        initLanguageCategory();
    }
}//GEN-LAST:event_manageStylesActionPerformed

private Map<CodeStyle.Language, Map<String,PreviewPreferences>> clonePreferences(){
        Map<CodeStyle.Language, Map<String,PreviewPreferences>> newAllPreferences = new HashMap<CodeStyle.Language, Map<String, PreviewPreferences>>();
        for(Map.Entry<CodeStyle.Language, Map<String,PreviewPreferences>> entry : allPreferences.entrySet()){
            CodeStyle.Language lang = entry.getKey();
            Map<String, PreviewPreferences> map = entry.getValue();
            Map<String, PreviewPreferences> newMap = new HashMap<String, PreviewPreferences>();
            newAllPreferences.put(lang, newMap);
            for(Map.Entry<String, PreviewPreferences> entry2: map.entrySet()){
                PreviewPreferences pref = entry2.getValue();
                PreviewPreferences newPref = new PreviewPreferences(pref, lang, entry2.getKey());
                newMap.put(entry2.getKey(), newPref);
            }
        }
        return newAllPreferences;
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel categoryPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JComboBox languagesComboBox;
    private javax.swing.JButton manageStyles;
    private javax.swing.JPanel oprionsPanel;
    private javax.swing.JEditorPane previewPane;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JComboBox styleComboBox;
    // End of variables declaration//GEN-END:variables

    private static class EntryWrapper implements Comparable<EntryWrapper> {
        private final String name;
        private String displayName;
        private final PreviewPreferences preferences;
        private EntryWrapper(Map.Entry<String, PreviewPreferences> enrty){
            this.name = enrty.getKey();
            this.preferences = enrty.getValue();
            displayName = EditorOptions.getStyleDisplayName(preferences.getLanguage(),name);
        }

        @Override
        public String toString() {
            return displayName;
        }

        public int compareTo(EntryWrapper o) {
            return this.displayName.compareTo(o.displayName);
        }
    }

    private class DummyNode extends AbstractNode {
        public DummyNode(Sheet sheet, String name) {
            super(Children.LEAF);
            if (sheet != null) {
                setSheet(sheet);
            }
            setName(name);
        }
    }
}
