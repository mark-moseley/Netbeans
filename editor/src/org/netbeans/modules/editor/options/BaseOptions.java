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

package org.netbeans.modules.editor.options;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Enumeration;
import javax.swing.text.JTextComponent;
import java.awt.Toolkit;

import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.ExtKit;

import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.FormatterIndentEngine;
import org.netbeans.modules.editor.IndentEngineFormatter;
import org.netbeans.modules.editor.SimpleIndentEngine;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.text.IndentEngine;
import org.openide.ServiceType;
import org.openide.TopManager;
import java.beans.IntrospectionException;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import java.io.ObjectOutput;
import org.openide.nodes.FilterNode;
import org.openide.util.RequestProcessor;
import org.netbeans.editor.ext.java.JavaSettingsDefaults;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.editor.NbEditorSettingsInitializer;
import javax.swing.KeyStroke;
import org.netbeans.modules.editor.java.JavaKit;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Template;
import org.openide.util.Lookup.Item;
import java.util.StringTokenizer;
import org.netbeans.modules.editor.NbEditorUtilities;
import java.util.Set;
import java.util.HashSet;
import org.openide.execution.NbClassPath;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileChangeAdapter;
import java.io.File;
import java.awt.Dimension;



/**
 * Options for the base editor kit
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
public class BaseOptions extends OptionSupport {
    
    /** Latest version of the options. It must be increased
     * manually when new patch is added to the options.
     */
    protected static final int LATEST_OPTIONS_VERSION = UpgradeOptions.LATEST_VERSION;
    
    protected static final String OPTIONS_VERSION_PROP = "optionsVersion";
    
    public static final String ABBREV_MAP_PROP = "abbrevMap"; // NOI18N
    public static final String BASE = "base"; // NOI18N
    public static final String CARET_BLINK_RATE_PROP = "caretBlinkRate"; // NOI18N
    public static final String CARET_COLOR_INSERT_MODE_PROP = "caretColorInsertMode"; // NOI18N
    public static final String CARET_COLOR_OVERWRITE_MODE_PROP = "caretColorOverwriteMode"; // NOI18N
    public static final String CARET_ITALIC_INSERT_MODE_PROP = "caretItalicInsertMode"; // NOI18N
    public static final String CARET_ITALIC_OVERWRITE_MODE_PROP = "caretItalicOverwriteMode"; // NOI18N
    public static final String CARET_TYPE_INSERT_MODE_PROP = "caretTypeInsertMode"; // NOI18N
    public static final String CARET_TYPE_OVERWRITE_MODE_PROP = "caretTypeOverwriteMode"; // NOI18N
    public static final String COLORING_MAP_PROP = "coloringMap"; // NOI18N
    public static final String EXPAND_TABS_PROP = "expandTabs"; // NOI18N
    public static final String FIND_HIGHLIGHT_SEARCH_PROP = "findHighlightSearch"; // NOI18N
    public static final String FIND_HISTORY_PROP = "findHistory"; // NOI18N
    public static final String FIND_HISTORY_SIZE_PROP = "findHistorySize"; // NOI18N
    public static final String FIND_INC_SEARCH_DELAY_PROP = "findIncSearchDelay"; // NOI18N
    public static final String FIND_INC_SEARCH_PROP = "findIncSearch"; // NOI18N
    public static final String FIND_MATCH_CASE_PROP = "findMatchCase"; // NOI18N
    public static final String FIND_REG_EXP_PROP = "findRegExp"; // NOI18N
    public static final String FIND_SMART_CASE_PROP = "findSmartCase"; // NOI18N
    public static final String FIND_WHOLE_WORDS_PROP = "findWholeWords"; // NOI18N
    public static final String FIND_WRAP_SEARCH_PROP = "findWrapSearch"; // NOI18N
    public static final String FONT_SIZE_PROP = "fontSize"; // NOI18N
    public static final String HIGHLIGHT_CARET_ROW_PROP = "highlightCaretRow"; // NOI18N
    public static final String HIGHLIGHT_MATCHING_BRACKET_PROP = "highlightMatchingBracket"; // NOI18N
    public static final String INDENT_ENGINE_PROP = "indentEngine"; // NOI18N
    public static final String KEY_BINDING_LIST_PROP = "keyBindingList"; // NOI18N
    public static final String LINE_HEIGHT_CORRECTION_PROP = "lineHeightCorrection"; // NOI18N
    public static final String LINE_NUMBER_MARGIN_PROP = "lineNumberMargin"; // NOI18N
    public static final String LINE_NUMBER_MARGIN_PROP_2 = "lineNumberMargin2"; // NOI18N    
    public static final String LINE_NUMBER_VISIBLE_PROP = "lineNumberVisible"; // NOI18N
    public static final String MACRO_MAP_PROP = "macroMap"; // NOI18N
    public static final String MARGIN_PROP = "margin"; // NOI18N
    public static final String SCROLL_FIND_INSETS_PROP = "scrollFindInsets"; // NOI18N
    public static final String SCROLL_JUMP_INSETS_PROP = "scrollJumpInsets"; // NOI18N
    public static final String SPACES_PER_TAB_PROP = "spacesPerTab"; // NOI18N
    public static final String STATUS_BAR_CARET_DELAY_PROP = "statusBarCaretDelay"; // NOI18N
    public static final String STATUS_BAR_VISIBLE_PROP = "statusBarVisible"; // NOI18N
    public static final String TAB_SIZE_PROP = "tabSize"; // NOI18N
    public static final String TEXT_LIMIT_LINE_COLOR_PROP = "textLimitLineColor"; // NOI18N
    public static final String TEXT_LIMIT_LINE_VISIBLE_PROP = "textLimitLineVisible"; // NOI18N
    public static final String TEXT_LIMIT_WIDTH_PROP = "textLimitWidth"; // NOI18N
    public static final String TOOLBAR_VISIBLE_PROP = "toolbarVisible"; // NOI18N
    
    static final String[] BASE_PROP_NAMES = {
        ABBREV_MAP_PROP,
        CARET_BLINK_RATE_PROP,
        CARET_COLOR_INSERT_MODE_PROP,
        CARET_COLOR_OVERWRITE_MODE_PROP,
        CARET_ITALIC_INSERT_MODE_PROP,
        CARET_ITALIC_OVERWRITE_MODE_PROP,
        CARET_TYPE_INSERT_MODE_PROP,
        CARET_TYPE_OVERWRITE_MODE_PROP,
        COLORING_MAP_PROP,
        EXPAND_TABS_PROP,
        FONT_SIZE_PROP,
        HIGHLIGHT_CARET_ROW_PROP,
        HIGHLIGHT_MATCHING_BRACKET_PROP,
        INDENT_ENGINE_PROP,
        KEY_BINDING_LIST_PROP,
        LINE_HEIGHT_CORRECTION_PROP,
        LINE_NUMBER_MARGIN_PROP_2,
        LINE_NUMBER_VISIBLE_PROP,
        MACRO_MAP_PROP,
        MARGIN_PROP,
        SCROLL_FIND_INSETS_PROP,
        SCROLL_JUMP_INSETS_PROP,
        SPACES_PER_TAB_PROP,
        STATUS_BAR_CARET_DELAY_PROP,
        STATUS_BAR_VISIBLE_PROP,
        TAB_SIZE_PROP,
        TEXT_LIMIT_LINE_COLOR_PROP,
        TEXT_LIMIT_LINE_VISIBLE_PROP,
        TEXT_LIMIT_WIDTH_PROP,
        OPTIONS_VERSION_PROP
    };
    
    static final long serialVersionUID =-5469192431366914841L;
    
    private static final String HELP_ID = "editing.global"; // !!! NOI18N
    private static final String NO_INDENT_ENGINE = "NO_INDENT_ENGINE"; // NOI18N
    
    /** Whether formatting debug messages should be displayed */
    private static final boolean debugFormat
    = Boolean.getBoolean("netbeans.debug.editor.format"); // NOI18N
    
    private transient Settings.Initializer coloringMapInitializer;
    
    /** Version of the options. It's used for patching the options. */
    private transient int optionsVersion;
    
    /* Indent engine available during readExternal() */
    private transient IndentEngine readExternalIndentEngine;
    private transient boolean inReadExternal;
    
    private transient MIMEOptionNode mimeNode;
    private transient Map defaultAbbrevsMap;
    private transient Map defaultMacrosMap;
    private transient Map defaultKeyBindingsMap;
    private transient MIMEOptionFolder settingsFolder;
    
    /** Map of Kit to Options */
    private static final HashMap kitClass2Options = new HashMap();

    public BaseOptions() {
        this(BaseKit.class, BASE);
        optionsVersion = LATEST_OPTIONS_VERSION;
    }
    
    public BaseOptions(Class kitClass, String typeName) {
        super(kitClass, typeName);
        kitClass2Options.put(kitClass, this);
    }

    public static BaseOptions getOptions(Class kitClass) {
        BaseOptions option = (BaseOptions)kitClass2Options.get(kitClass);
        
        if (option == null) {
            AllOptionsFolder.getDefault().loadMIMEOption(kitClass, false);
            
            option = (BaseOptions)kitClass2Options.get(kitClass);
        }
        
        return option;
    }

    /** Listening for Settings.settings creation.*/
    private void attachSettingsFileListener(FileObject folderFO){
        final String contentType = BaseKit.getKit(getKitClass()).getContentType();
        if (contentType == null) return;
        
        FileObject optionFO = TopManager.getDefault().getRepository().getDefaultFileSystem().
        findResource(AllOptionsFolder.FOLDER+"/"+contentType+"/"+AllOptionsFolder.OPTION_FILE_NAME); //NOI18N
        if (optionFO!=null && NbClassPath.toFile(optionFO)!=null){
            try{
                NbClassPath.toFile(optionFO).delete();
            }catch (SecurityException se){
            }
        }
        
        folderFO.addFileChangeListener(new FileChangeAdapter(){
            private void delete(FileObject fo){
                if (fo.getNameExt().equals(AllOptionsFolder.OPTION_FILE_NAME)){
                    File settingsFile = NbClassPath.toFile(fo);
                    if (settingsFile != null) {
                        settingsFile.delete();
                    }
                }
            }
            public void fileDataCreated(FileEvent fe){
                if (fe==null) return;
                delete(fe.getFile());
            }
            public void fileChanged(FileEvent fe){
                if (fe==null) return;
                delete(fe.getFile());
            }
        }
        );
    }

    
    /** Lazy initialization of the MIME specific settings folder. The folder should be created
     *  via XML layers, if not, it will be created.
     *  Instances of all XML file in this folder will be created.
     */
    protected synchronized MIMEOptionFolder getMIMEFolder(){
        // return already initialized folder
        if (settingsFolder!=null) return settingsFolder;
        
        if (BaseKit.getKit(getKitClass()).getContentType() == null) return null;

        String name = BaseKit.getKit(getKitClass()).getContentType();
        
        FileObject f = TopManager.getDefault().getRepository().getDefaultFileSystem().
        findResource(AllOptionsFolder.FOLDER+"/"+name); //NOI18N
        
        // MIME folder doesn't exist, let's create it
        if (f==null){
            FileObject fo = TopManager.getDefault().getRepository().getDefaultFileSystem().
            findResource(AllOptionsFolder.FOLDER);
            
            if (fo != null){
                try{
                    StringTokenizer stok = new StringTokenizer(name,"/");
                    while (stok.hasMoreElements()) {
                        String newFolder = stok.nextToken();
                        if (fo.getFileObject(newFolder) == null){
                            fo = fo.createFolder(newFolder);
                        }
                        else
                            fo = fo.getFileObject(newFolder);
                    }
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }
                
                f = TopManager.getDefault().getRepository().getDefaultFileSystem().
                findResource(AllOptionsFolder.FOLDER+"/"+name);
            }
        }
        
        if (f != null) {
            try {
                DataObject d = DataObject.find(f);
                DataFolder df = (DataFolder)d.getCookie(DataFolder.class);
                if (df != null) {
                    
                    /* hack for listenning on mime folder
                       for creation of Settings.settings file. Only file on 
                       default layer is valid, so if Settings.settings is created, we need to delete it.
                     */
                    attachSettingsFileListener(f);
                    
                    settingsFolder = new MIMEOptionFolder(df, this);
                    return settingsFolder;
                }
            } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        
        return null;
    }
    
    /** Gets MIMEOptionNode that belongs to this bean */
    public synchronized MIMEOptionNode getMimeNode(){
        if (mimeNode == null) createMIMENode(getTypeName());
        return mimeNode;
    }
    
    /** Creates Node in global options for appropriate MIME type */
    private void createMIMENode(String typeName){
        if (typeName.equals(BASE)){
            return;
        }
        try{
            mimeNode = new MIMEOptionNode(this);
        }catch(IntrospectionException ie){
            ie.printStackTrace();
        }
    }
    
    protected void updateSettingsMap(Class kitClass, Map settingsMap) {
        super.updateSettingsMap(kitClass, settingsMap);

        if (kitClass == getKitClass()) {
            // Create evaluators for indentEngine and formatter
            settingsMap.put(NbEditorDocument.INDENT_ENGINE,
                new Settings.Evaluator() {
                    public Object getValue(Class kitClass2, String settingName) {
                        return getIndentEngine();
                    }
                }
            );

            settingsMap.put(NbEditorDocument.FORMATTER,
                new Settings.Evaluator() {
                    public Object getValue(Class kitClass2, String settingName) {
                        IndentEngine eng = getIndentEngine();
                        return (eng != null)
                            ? ((eng instanceof FormatterIndentEngine)
                                ? ((FormatterIndentEngine)eng).getFormatter()
                                : ((Formatter)new IndentEngineFormatter(getKitClass(), eng)))
                            : null;
                    }
                }
            );

            if (coloringMapInitializer != null) {
                coloringMapInitializer.updateSettingsMap(kitClass, settingsMap);
            }
        }
        
        if (kitClass == BaseKit.class && coloringMapInitializer != null) {
            coloringMapInitializer.updateSettingsMap(BaseKit.class, settingsMap);
        }
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
    
    public int getTabSize() {
        return getSettingInteger(SettingsNames.TAB_SIZE);
    }
    public void setTabSize(int tabSize) {
        if (tabSize < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        setSettingInteger(SettingsNames.TAB_SIZE, tabSize, TAB_SIZE_PROP);
    }
    
/*    public boolean getExpandTabs() {
        return getSettingBoolean(SettingsNames.EXPAND_TABS);
    }
    [Mila] Moved to IndentEngine; Setter must stay here
 */
    
    public void setExpandTabs(boolean expandTabs) {
        setSettingBoolean(SettingsNames.EXPAND_TABS, expandTabs, EXPAND_TABS_PROP);
    }
    
/*    public int getSpacesPerTab() {
        return getSettingInteger(SettingsNames.SPACES_PER_TAB);
    }
    [Mila] Moved to IndentEngine; Setter must stay here
 */
    public void setSpacesPerTab(int i){
        if (i > 0)
            setSettingInteger(SettingsNames.SPACES_PER_TAB, i, SPACES_PER_TAB_PROP);
        else
            Toolkit.getDefaultToolkit().beep();
    }
    
    /** Gets Map of default Abbreviations as they are stored in
     *  MIMEFolder/Defaults/abbreviations.xml */
    public Map getDefaultAbbrevMap(){
        loadDefaultAbbreviations();
        return defaultAbbrevsMap;
    }
    
    /** Loads default abbreviations from MIMEFolder/Defaults/abbreviations.xml and
     * stores them to defaultAbbrevsMap */
    private void loadDefaultAbbreviations(){
        if (defaultAbbrevsMap!=null) return;
        MIMEOptionFolder mimeFolder = getMIMEFolder();
        if (mimeFolder == null) return;
        MIMEOptionFolder mof = mimeFolder.getFolder(OptionUtilities.DEFAULT_FOLDER);
        if (mof == null) {
            return;
        }
        
        MIMEOptionFile file = mof.getFile(AbbrevsMIMEProcessor.class, false);
        if ((file!=null) && (!file.isLoaded())) {
            file.loadSettings(false);
            defaultAbbrevsMap = new HashMap(file.getAllProperties());
        }
    }
    
    public Map getAbbrevMap() {
        loadDefaultAbbreviations();
        loadSettings(AbbrevsMIMEProcessor.class);
        return new HashMap((Map)super.getSettingValue(SettingsNames.ABBREV_MAP) );
    }
    
    /** Sets new abbreviations map to initializer map and if saveToXML is true,
     *  then new settings will be saved to XML file. */
    public void setAbbrevMap(Map map, boolean saveToXML) {
        Map diffMap = null;
        if (saveToXML){
            // we are going to save the diff-ed changes to XML, all default
            // properties have to be available
            loadDefaultAbbreviations();
            diffMap = OptionUtilities.getMapDiff(getAbbrevMap(),map,true);
            if (diffMap.size()>0){
                // settings has changed, write changed settings to XML file
                updateSettings(AbbrevsMIMEProcessor.class, diffMap);
            }
        }
        super.setSettingValue(SettingsNames.ABBREV_MAP, map, ABBREV_MAP_PROP);
    }
    
    /** Sets new abbreviations map and save the diff-ed changes to XML file*/
    public void setAbbrevMap(Map map) {
        setAbbrevMap(map, true);
    }
    
    public String getCaretTypeInsertMode() {
        return (String)getSettingValue(SettingsNames.CARET_TYPE_INSERT_MODE);
    }
    public void setCaretTypeInsertMode(String type) {
        setSettingValue(SettingsNames.CARET_TYPE_INSERT_MODE, type,
        CARET_TYPE_INSERT_MODE_PROP);
    }
    
    public String getCaretTypeOverwriteMode() {
        return (String)getSettingValue(SettingsNames.CARET_TYPE_OVERWRITE_MODE);
    }
    public void setCaretTypeOverwriteMode(String type) {
        setSettingValue(SettingsNames.CARET_TYPE_OVERWRITE_MODE, type,
        CARET_TYPE_OVERWRITE_MODE_PROP);
    }
    
    public boolean getCaretItalicInsertMode() {
        return getSettingBoolean(SettingsNames.CARET_ITALIC_INSERT_MODE);
    }
    public void setCaretItalicInsertMode(boolean b) {
        setSettingBoolean(SettingsNames.CARET_ITALIC_INSERT_MODE, b,
        CARET_ITALIC_INSERT_MODE_PROP);
    }
    
    public boolean getCaretItalicOverwriteMode() {
        return getSettingBoolean(SettingsNames.CARET_ITALIC_OVERWRITE_MODE);
    }
    public void setCaretItalicOverwriteMode(boolean b) {
        setSettingBoolean(SettingsNames.CARET_ITALIC_OVERWRITE_MODE, b,
        CARET_ITALIC_OVERWRITE_MODE_PROP);
    }
    
    public Color getCaretColorInsertMode() {
        loadSettings(FontsColorsMIMEProcessor.class);
        return (Color)super.getSettingValue(SettingsNames.CARET_COLOR_INSERT_MODE);
    }
    
    /** Sets new CaretColorInsertMode property to initializer map and save the
     *  changes to XML file */
    public void setCaretColorInsertMode(Color color) {
        setCaretColorInsertMode(color, true);
    }
    
    /** Sets new CaretColorInsertMode property to initializer map and if saveToXML is true,
     *  then new settings will be saved to XML file (fontsColors.xml). */
    public void setCaretColorInsertMode(Color color, boolean saveToXML) {
        if (saveToXML){
            if (!getCaretColorInsertMode().equals(color) && (color!=null)){
                // settings has changed, write changed settings to XML file
                Map map = new HashMap();
                map.put(SettingsNames.CARET_COLOR_INSERT_MODE,color);
                if (map!=null){
                    updateSettings(FontsColorsMIMEProcessor.class,
                    map);
                }
            }
        }
        
        super.setSettingValue(SettingsNames.CARET_COLOR_INSERT_MODE, color,
        CARET_COLOR_INSERT_MODE_PROP);
    }
    
    public Color getCaretColorOverwriteMode() {
        loadSettings(FontsColorsMIMEProcessor.class);
        return (Color)super.getSettingValue(SettingsNames.CARET_COLOR_OVERWRITE_MODE);
    }
    
    /** Sets new CaretColorOverwriteMode property to initializer map and save the
     *  changes to XML file */
    public void setCaretColorOverwriteMode(Color color) {
        setCaretColorOverwriteMode(color, true);
    }
    
    /** Sets new CaretColorOverwriteMode property to initializer map and if saveToXML is true,
     *  then new settings will be saved to XML file (fontsColors.xml). */
    public void setCaretColorOverwriteMode(Color color, boolean saveToXML) {
        if (saveToXML){
            if (!getCaretColorOverwriteMode().equals(color) && (color!=null)){
                // settings has changed, write changed settings to XML file
                Map map = new HashMap();
                map.put(SettingsNames.CARET_COLOR_OVERWRITE_MODE,color);
                if (map!=null){
                    updateSettings(FontsColorsMIMEProcessor.class,
                    map);
                }
            }
        }
        
        super.setSettingValue(SettingsNames.CARET_COLOR_OVERWRITE_MODE, color,
        CARET_COLOR_OVERWRITE_MODE_PROP);
    }
    
    public int getCaretBlinkRate() {
        return getSettingInteger(SettingsNames.CARET_BLINK_RATE);
    }
    public void setCaretBlinkRate(int rate) {
        if (rate < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        setSettingInteger(SettingsNames.CARET_BLINK_RATE, rate,
        CARET_BLINK_RATE_PROP);
    }
    
    public boolean getLineNumberVisible() {
        return getSettingBoolean(SettingsNames.LINE_NUMBER_VISIBLE);
    }
    public void setLineNumberVisible(boolean b) {
        setSettingBoolean(SettingsNames.LINE_NUMBER_VISIBLE, b,
        LINE_NUMBER_VISIBLE_PROP);
    }
    
    public Insets getScrollJumpInsets() {
        return (Insets)getSettingValue(SettingsNames.SCROLL_JUMP_INSETS);
    }
    public void setScrollJumpInsets(Insets i) {
        setSettingValue(SettingsNames.SCROLL_JUMP_INSETS, i,
        SCROLL_JUMP_INSETS_PROP);
    }
    
    public Insets getScrollFindInsets() {
        return (Insets)getSettingValue(SettingsNames.SCROLL_FIND_INSETS);
    }
    public void setScrollFindInsets(Insets i) {
        setSettingValue(SettingsNames.SCROLL_FIND_INSETS, i,
        SCROLL_FIND_INSETS_PROP);
    }
    
    /** Gets Map of default KeyBindings as they are stored in
     *  MIMEFolder/Defaults/keybindings.xml */
    public Map getDefaultKeyBindingsMap(){
        loadDefaultKeyBindings();
        return defaultKeyBindingsMap;
    }
    
    /** Loads default abbreviations from MIMEFolder/Defaults/keybindings.xml and
     *  stores them to defaultKeyBindingsMap */
    private void loadDefaultKeyBindings(){
        if (defaultKeyBindingsMap!=null) return;
        MIMEOptionFolder mof;
        if (BASE.equals(getTypeName())){
            MIMEOptionFolder mimeFolder = AllOptionsFolder.getDefault().getMIMEFolder();
            if (mimeFolder == null) return;
            mof = mimeFolder.getFolder(OptionUtilities.DEFAULT_FOLDER);
        }else{
            AllOptionsFolder.getDefault().loadDefaultKeyBindings();
            MIMEOptionFolder mimeFolder = getMIMEFolder();
            if (mimeFolder == null) return;
            mof = mimeFolder.getFolder(OptionUtilities.DEFAULT_FOLDER);
        }
        if (mof == null) {
            return;
        }
        
        MIMEOptionFile file = mof.getFile(KeyBindingsMIMEProcessor.class, false);
        if ((file!=null) && (!file.isLoaded())) {
            file.loadSettings(false);
            defaultKeyBindingsMap = new HashMap(file.getAllProperties());
        }
    }
    
    private List getKBList(){
        loadDefaultKeyBindings();
        loadSettings(KeyBindingsMIMEProcessor.class);
        
        Class kitClass = getKitClass();
        Settings.KitAndValue[] kav = getSettingValueHierarchy(SettingsNames.KEY_BINDING_LIST);
        List kbList = null;
        for (int i = 0; i < kav.length; i++) {
            if (kav[i].kitClass == kitClass) {
                kbList = (List)kav[i].value;
            }
        }
        if (kbList == null) {
            kbList = new ArrayList();
        }
        
        // must convert all members to serializable MultiKeyBinding
        int cnt = kbList.size();
        for (int i = 0; i < cnt; i++) {
            Object o = kbList.get(i);
            if (!(o instanceof MultiKeyBinding) && o != null) {
                JTextComponent.KeyBinding b = (JTextComponent.KeyBinding)o;
                kbList.set(i, new MultiKeyBinding(b.key, b.actionName));
            }
        }
        return new ArrayList( kbList );
    }
    
    public List getKeyBindingList() {
        List kb2 = new ArrayList( getKBList() );
        kb2.add( 0, getKitClass().getName() ); //insert kit class name
        return kb2;
    }
    
    /** Sets new keybindings map and save the diff-ed changes to XML file*/
    public void setKeyBindingList(List list) {
        setKeyBindingList(list, true);
    }
    
    
    /** Saves keybindings settings to XML file. 
     *  (This is used especially for record macro action.)*/
    public void setKeyBindingsDiffMap(Map diffMap){
        if ((diffMap != null) && (diffMap.size()>0)){
            updateSettings(KeyBindingsMIMEProcessor.class, diffMap);
        }
    }
    
    
    /** Sets new keybindings list to initializer map and if saveToXML is true,
     *  then new settings will be saved to XML file. */
    public void setKeyBindingList(List list, boolean saveToXML) {
        if( list.size() > 0 &&
        ( list.get( 0 ) instanceof Class || list.get( 0 ) instanceof String )
        ) {
            list.remove( 0 ); //remove kit class name
        }
        
        Map diffMap = null;
        if (saveToXML){
            // we are going to save the diff-ed changes to XML, all default
            // properties have to be available
            loadDefaultKeyBindings();
            List kbMap = getKeyBindingList();
            if( kbMap.size() > 0 &&
            ( kbMap.get( 0 ) instanceof Class || kbMap.get( 0 ) instanceof String )
            ) {
                kbMap.remove( 0 ); //remove kit class name
            }
            
            diffMap = OptionUtilities.getMapDiff(OptionUtilities.makeKeyBindingsMap(kbMap),
            OptionUtilities.makeKeyBindingsMap(list),true);
            if (diffMap.size()>0){
                // settings has changed, write changed settings to XML file
                updateSettings(KeyBindingsMIMEProcessor.class, diffMap);
            }
        }
        
        super.setSettingValue(SettingsNames.KEY_BINDING_LIST, list, KEY_BINDING_LIST_PROP);
    }
    
    public Map getColoringMap() {
        loadSettings(FontsColorsMIMEProcessor.class);
        Map cm = new HashMap( SettingsUtil.getColoringMap(getKitClass(), false, true) ); // !!! !evaluateEvaluators
        cm.put(null, getKitClass().getName() ); // add kit class
        return cm;
    }
    
    /** Sets new coloring map and save the diff-ed changes to XML file*/
    public void setColoringMap(Map coloringMap) {
        setColoringMap(coloringMap, true);
    }
    
    /** Sets new coloring map to initializer map and if saveToXML is true,
     *  then new settings will be saved to XML file. */
    public void setColoringMap(Map coloringMap, boolean saveToXML){
        Map diffMap = null;
        if (coloringMap != null) {
            if (inReadExternal) {
                /* Fix of #11115
                 * The better place would be in upgradeOptions()
                 * which was attempted originally. However the normal
                 * behavior of setColoringMap() destroys the colorings
                 * if they are not upgraded immediately. Therefore
                 * the readExternalColoringMap approach was attempted.
                 * However there was an NPE in
                 * properties.syntax.EditorSettingsCopy.updateColors
                 * at line 235 the keyColoring was null.
                 * Therefore the patch appears here.
                 */
                coloringMap = UpgradeOptions.patchColorings(getKitClass(), coloringMap);
            }
            
            if (saveToXML){
                diffMap = OptionUtilities.getMapDiff(getColoringMap(),coloringMap,false);
                if (diffMap.size()>0){
                    // settings has changed, write changed settings to XML file
                    //System.out.println("SETTING COLORING MAP:"+diffMap);
                    updateSettings(FontsColorsMIMEProcessor.class, diffMap);
                }
            }
            
            coloringMap.remove(null); // remove kit class
            SettingsUtil.setColoringMap( getKitClass(), coloringMap, false );
            
            coloringMapInitializer = SettingsUtil.getColoringMapInitializer(
            getKitClass(), coloringMap, false,
            getTypeName() + "-coloring-map-initializer" //NOI18N
            );
            
            firePropertyChange(COLORING_MAP_PROP, null, null);
        }
    }
    
    public int getFontSize() {
        Coloring dc = SettingsUtil.getColoring(getKitClass(), SettingsNames.DEFAULT_COLORING, false);
        return (dc != null) ? dc.getFont().getSize() : SettingsDefaults.defaultFont.getSize();
    }
    
    public void setFontSize(final int size) {
        if (size < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        final int oldSize = getFontSize();
        Map cm = SettingsUtil.getColoringMap(getKitClass(), false, true); // !!! !evaluateEvaluators
        if (cm != null) {
            Iterator it = cm.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                Object value = entry.getValue();
                if (value instanceof Coloring) {
                    Coloring c = (Coloring)value;
                    Font font = c.getFont();
                    if (font != null && font.getSize() != size) {
                        font = font.deriveFont((float)size);
                        Coloring newColoring = new Coloring(font, c.getFontMode(),
                        c.getForeColor(), c.getBackColor()); // this way due to bug in Coloring
                        entry.setValue(newColoring);
                    }
                }
            }
            setColoringMap(cm);
            
            firePropertyChange(FONT_SIZE_PROP, null, null);
        }
    }
    
    public float getLineHeightCorrection() {
        return ((Float) getSettingValue(SettingsNames.LINE_HEIGHT_CORRECTION)).floatValue();
    }
    public void setLineHeightCorrection(float f) {
        if (f <= 0) {
            NbEditorUtilities.invalidArgument("MSG_OutOfRange"); // NOI18N
            return;
        }
        setSettingValue(SettingsNames.LINE_HEIGHT_CORRECTION, new Float(f),
        LINE_HEIGHT_CORRECTION_PROP);
    }
    
    /** Gets Map of default Macros as they are stored in
     *  MIMEFolder/Defaults/macros.xml */
    public Map getDefaultMacrosMap(){
        loadDefaultMacros();
        return defaultMacrosMap;
    }
    
    /** Loads default abbreviations from MIMEFolder/Defaults/macros.xml and
     *  stores them to defaultMacrosMap */
    private void loadDefaultMacros(){
        if (defaultMacrosMap!=null) return;
        MIMEOptionFolder mimeFolder = getMIMEFolder();
        if (mimeFolder == null) return;

        MIMEOptionFolder mof = mimeFolder.getFolder(OptionUtilities.DEFAULT_FOLDER);
        if (mof == null) return;
        
        MIMEOptionFile file = mof.getFile(MacrosMIMEProcessor.class, false);
        if ((file!=null) && (!file.isLoaded())) {
            file.loadSettings(false);
            defaultMacrosMap = new HashMap(file.getAllProperties());
        }
    }

    /** removes keybindings from deleted macros, or if macro deletion was cancelled
     *  it restores old keybinding value */
    private void processMacroKeyBindings(Map diff, List oldKB){
        List deletedKB = new ArrayList();
        List addedKB = new ArrayList();
        List newKB = getKBList();        

        for( Iterator i = diff.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            if (!(diff.get(key) instanceof String)) continue;
            String action = (String) diff.get(key);
            String kbActionName = new String(BaseKit.macroActionPrefix+key);

            if (action.length()!=0){
                // process restored macros
                for (int j = 0; j < oldKB.size(); j++){
                    if(oldKB.get(j) instanceof MultiKeyBinding){
                        MultiKeyBinding mkb = (MultiKeyBinding) oldKB.get(j);
                        if (!kbActionName.equals(mkb.actionName)) continue;
                        addedKB.add(mkb);
                        break;
                    }
                }
                continue;
            }
            
            for (int j = 0; j < newKB.size(); j++){
                // process deleted macros
                if(newKB.get(j) instanceof MultiKeyBinding){
                    MultiKeyBinding mkb = (MultiKeyBinding) newKB.get(j);
                    if (!kbActionName.equals(mkb.actionName)) continue;
                    deletedKB.add(mkb);
                    break;
                }
            }
        }
        
        if ((deletedKB.size()>0) || (addedKB.size()>0)){
            newKB.removeAll(deletedKB);
            newKB.addAll(addedKB);
            // save changed keybindings to XML file
            setKeyBindingsDiffMap(OptionUtilities.getMapDiff(OptionUtilities.makeKeyBindingsMap(getKBList()), 
                OptionUtilities.makeKeyBindingsMap(newKB), true));
            // set new keybindings
            Settings.setValue( getKitClass(), SettingsNames.KEY_BINDING_LIST, newKB);
        }
    }
    
    /** Gets Macro Map */
    public Map getMacroMap() {
        loadDefaultMacros();
        loadSettings(MacrosMIMEProcessor.class);
        Map ret = new HashMap( (Map)super.getSettingValue(SettingsNames.MACRO_MAP) );
        ret.put(null, getKBList());
        return ret;
    }
    
    /** Saves macro settings to XML file. 
     *  (This is used especially for record macro action.)*/
    public void setMacroDiffMap(Map diffMap){
        if ((diffMap != null) && (diffMap.size()>0)){
            updateSettings(MacrosMIMEProcessor.class, diffMap);
        }
    }
    
    /** Sets new macro map to initializer map and if saveToXML is true,
     *  then new settings will be saved to XML file. */
    public void setMacroMap(Map map, boolean saveToXML) {
        Map diffMap = null;
        List kb = new ArrayList();
        if (map.containsKey(null)){
            kb.addAll((List)map.get(null));
            map.remove(null);
        }
        if (saveToXML){
            // we are going to save the diff-ed changes to XML, all default
            // properties have to be available
            loadDefaultMacros();
            diffMap = OptionUtilities.getMapDiff(getMacroMap(),map,true);
            if (diffMap.containsKey(null)) diffMap.remove(null);
            if (diffMap.size()>0){
                // settings has changed, write changed settings to XML file
                processMacroKeyBindings(diffMap,kb);
                updateSettings(MacrosMIMEProcessor.class, diffMap);
            }
        }
        
        super.setSettingValue(SettingsNames.MACRO_MAP, map);
    }
    
    /** Sets new macros map and save the diff-ed changes to XML file*/
    public void setMacroMap(Map map) {
        setMacroMap(map, true);
    }
    
    public Insets getMargin() {
        return (Insets)getSettingValue(SettingsNames.MARGIN);
    }
    public void setMargin(Insets i) {
        setSettingValue(SettingsNames.MARGIN, i, MARGIN_PROP);
    }
    
    public Insets getLineNumberMargin() {
        return (Insets)getSettingValue(SettingsNames.LINE_NUMBER_MARGIN);
    }
    public void setLineNumberMargin(Insets i) {
        setSettingValue(SettingsNames.LINE_NUMBER_MARGIN, i, LINE_NUMBER_MARGIN_PROP);
    }


    //-------- used only for better UI repres=entation as a fix of the bug #17950 --
    public Dimension getLineNumberMargin2() {
        Insets ins =  (Insets)getSettingValue(SettingsNames.LINE_NUMBER_MARGIN);
        return new Dimension(ins.left, ins.right);
    }
    public void setLineNumberMargin2(Dimension d) {
        setLineNumberMargin(new Insets(0, d.width, 0, d.height));
    }
    //--------------------------------------------------------------------
    
    public boolean getStatusBarVisible() {
        return getSettingBoolean(SettingsNames.STATUS_BAR_VISIBLE);
    }
    public void setStatusBarVisible(boolean v) {
        setSettingBoolean(SettingsNames.STATUS_BAR_VISIBLE, v, STATUS_BAR_VISIBLE_PROP);
    }
    
    public int getStatusBarCaretDelay() {
        return getSettingInteger(SettingsNames.STATUS_BAR_CARET_DELAY);
    }
    public void setStatusBarCaretDelay(int delay) {
        if (delay < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        setSettingInteger(SettingsNames.STATUS_BAR_CARET_DELAY, delay,
        STATUS_BAR_CARET_DELAY_PROP);
    }
    
    public boolean getFindHighlightSearch() {
        return getSettingBoolean(SettingsNames.FIND_HIGHLIGHT_SEARCH);
    }
    
    public void setFindHighlightSearch(boolean b) {
        setSettingBoolean(SettingsNames.FIND_HIGHLIGHT_SEARCH, b,
        FIND_HIGHLIGHT_SEARCH_PROP);
    }
    
    public boolean getFindIncSearch() {
        return getSettingBoolean(SettingsNames.FIND_INC_SEARCH);
    }
    
    public void setFindIncSearch(boolean b) {
        setSettingBoolean(SettingsNames.FIND_INC_SEARCH, b, FIND_INC_SEARCH_PROP);
    }
    
    public int getFindIncSearchDelay() {
        return getSettingInteger(SettingsNames.FIND_INC_SEARCH_DELAY);
    }
    
    public void setFindIncSearchDelay(int delay) {
        if (delay < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        setSettingInteger(SettingsNames.FIND_INC_SEARCH_DELAY, delay,
        FIND_INC_SEARCH_DELAY_PROP);
    }
    
    public boolean getFindWrapSearch() {
        return getSettingBoolean(SettingsNames.FIND_WRAP_SEARCH);
    }
    
    public void setFindWrapSearch(boolean b) {
        setSettingBoolean(SettingsNames.FIND_WRAP_SEARCH, b,
        FIND_WRAP_SEARCH_PROP);
    }
    
    public boolean getFindSmartCase() {
        return getSettingBoolean(SettingsNames.FIND_SMART_CASE);
    }
    
    public void setFindSmartCase(boolean b) {
        setSettingBoolean(SettingsNames.FIND_SMART_CASE, b, FIND_SMART_CASE_PROP);
    }
    
    public Map getFindHistory() {
        return new HashMap( (Map)getSettingValue(SettingsNames.FIND_HISTORY) );
    }
    
    public void setFindHistory(Map m) {
        setSettingValue(SettingsNames.FIND_HISTORY, m, FIND_HISTORY_PROP);
    }
    
    public int getFindHistorySize() {
        return getSettingInteger(SettingsNames.FIND_HISTORY_SIZE);
    }
    
    public void setFindHistorySize(int size) {
        setSettingInteger(SettingsNames.FIND_HISTORY_SIZE, size,
        FIND_HISTORY_SIZE_PROP);
    }
    
    public Color getTextLimitLineColor() {
        loadSettings(FontsColorsMIMEProcessor.class);
        return (Color)super.getSettingValue(SettingsNames.TEXT_LIMIT_LINE_COLOR);
    }
    
    /** Sets new TextLimitLineColor property to initializer map and save the
     *  changes to XML file */
    public void setTextLimitLineColor(Color color) {
        setTextLimitLineColor(color, true);
    }
    
    /** Sets new TextLimitLineColor property to initializer map and if saveToXML is true,
     *  then new settings will be saved to XML file (fontsColors.xml). */
    public void setTextLimitLineColor(Color color , boolean saveToXML) {
        if (saveToXML){
            if (!getTextLimitLineColor().equals(color) && (color!=null)){
                // settings has changed, write changed settings to XML file
                Map map = new HashMap();
                map.put(SettingsNames.TEXT_LIMIT_LINE_COLOR,color);
                if (map!=null){
                    updateSettings(FontsColorsMIMEProcessor.class,
                    map);
                }
            }
        }
        
        super.setSettingValue(SettingsNames.TEXT_LIMIT_LINE_COLOR, color,
        TEXT_LIMIT_LINE_COLOR_PROP);
    }
    
    public int getTextLimitWidth() {
        return getSettingInteger(SettingsNames.TEXT_LIMIT_WIDTH);
    }
    
    public void setTextLimitWidth(int width) {
        if (width <= 0) {
            NbEditorUtilities.invalidArgument("MSG_OutOfRange"); // NOI18N
            return;
        }
        setSettingInteger(SettingsNames.TEXT_LIMIT_WIDTH, width,
        TEXT_LIMIT_WIDTH_PROP);
    }
    
    public boolean getTextLimitLineVisible() {
        return getSettingBoolean(SettingsNames.TEXT_LIMIT_LINE_VISIBLE);
    }
    
    public void setTextLimitLineVisible(boolean visible) {
        setSettingBoolean(SettingsNames.TEXT_LIMIT_LINE_VISIBLE, visible,
        TEXT_LIMIT_LINE_VISIBLE_PROP);
    }
    
    public boolean getHighlightMatchingBracket() {
        return getSettingBoolean(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE);
    }
    
    public void setHighlightMatchingBracket(boolean highlight) {
        setSettingBoolean(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE, highlight,
        HIGHLIGHT_MATCHING_BRACKET_PROP);
    }
    
    public boolean getHighlightCaretRow() {
        return getSettingBoolean(ExtSettingsNames.HIGHLIGHT_CARET_ROW);
    }
    
    public void setHighlightCaretRow(boolean highlight) {
        setSettingBoolean(ExtSettingsNames.HIGHLIGHT_CARET_ROW, highlight,
        HIGHLIGHT_CARET_ROW_PROP);
    }

    public boolean isToolbarVisible() {
        return getSettingBoolean(TOOLBAR_VISIBLE_PROP);
    }
    
    public void setToolbarVisible(boolean toolbarVisible) {
        setSettingBoolean(TOOLBAR_VISIBLE_PROP, toolbarVisible, TOOLBAR_VISIBLE_PROP);
    }
        
    /** Retrieves the actions from XML file */
    public void initPopupMenuItems(){
        if (!BASE.equals(getTypeName())){
            MIMEOptionFolder mimeFolder = getMIMEFolder();
            if (mimeFolder != null){
                MultiPropertyFolder mpf = mimeFolder.getMPFolder("Popup",false); //NOI18N
                if (mpf!=null){
                    DataFolder df = mpf.getDataFolder();
                    List mimeFolderAttribs = new ArrayList();
                    for (Enumeration e = df.getPrimaryFile().getAttributes() ; e.hasMoreElements() ;) {
                        mimeFolderAttribs.add(e.nextElement());
                    }

                    // merge folders only if mime folder has some relevant info
                    if ( (mpf.getProperties().size()!=0) || (mimeFolderAttribs.size() != 0)) {
                        
                        // We are going to merge global popup items and mime popup items ...
                        // Firstly merge popup items
                        Set mergedPopupItems = new HashSet(OptionUtilities.getGlobalPopupMenuItems());
                        mergedPopupItems.addAll(mpf.getProperties());
                        
                        // Then merge attribs
                        Set mergedPopupAttribs = new HashSet(OptionUtilities.getGlobalPopupAttribs());
                        mergedPopupAttribs.addAll(mimeFolderAttribs);
                        
                        // Sort it in accordance with merged Popup Attribs
                        List orderedPopupItems = OptionUtilities.arrangeMergedPopup(mergedPopupItems, mergedPopupAttribs);
                        super.setSettingValue(ExtSettingsNames.POPUP_MENU_ACTION_NAME_LIST, orderedPopupItems);
                        return;
                    }
                }
            }
        }
    }
    
    
    public IndentEngine getIndentEngine() {
        // Due to #11212
        if (inReadExternal) {
            return readExternalIndentEngine;
        }

        if (!BASE.equals(getTypeName())){
            loadSettings(PropertiesMIMEProcessor.class);        

            MIMEOptionFile file; 
            MIMEOptionFolder mimeFolder = getMIMEFolder();
            if (mimeFolder != null){
                file= mimeFolder.getFile(PropertiesMIMEProcessor.class, false);
                if (file != null) {
                    Map setMap = file.getAllProperties();
                    Object handle = setMap.get(INDENT_ENGINE_PROP);
                    if (handle instanceof String){
                        Object instance = null;
                        String handleString = (String) handle;
                        
                        if (handleString.equals(NO_INDENT_ENGINE)){
                            return IndentEngine.getDefault();
                        }
                        
                        Lookup.Template tmp = new Lookup.Template(null, handleString, null);
                        Lookup.Item item = Lookup.getDefault().lookupItem(tmp);
                        if (item != null) {
                            instance = item.getInstance();
                            if(instance instanceof IndentEngine){
                                return (IndentEngine) instance;
                            }
                        }

                    }
                }
            }
        
        }

        // [BACKWARD-COMPATIBILITY-START]
        /* Either handle or real indent-egine is attempted
         * to be obtained from property.
         */
        Object o = getProperty(INDENT_ENGINE_PROP);
        if (o instanceof IndentEngine.Handle) {
            IndentEngine eng = (IndentEngine)((IndentEngine.Handle)o).getServiceType();
            if (eng != null) {
                setIndentEngine(eng);
                return eng;
            }

        } else if (o instanceof IndentEngine) {
            setIndentEngine((IndentEngine)o);
            return (IndentEngine)o;
        }
        // [BACKWARD-COMPATIBILITY-END]
                                       
        
        // Try to find the default indent engine in Services registry
        IndentEngine eng = findDefaultIndentEngine();
        if (eng != null) { // found
            setIndentEngine(eng);
        }
        
        return eng;
    }
    
    public void setIndentEngine(IndentEngine eng) {
        /* Disabled direct setting of the engine
         * during project deserialization to avoid doubled
         * indent engine as described in #9687
         */
        if (!inReadExternal) {
            String id = null;
            if (eng != null) {
                Lookup.Template tmp = new Lookup.Template(null, null, eng);
                Lookup.Item item = Lookup.getDefault().lookupItem(tmp);
                if (item != null) id = item.getId();
                
            }

            if (!BASE.equals(getTypeName())){
                Map map = new HashMap();
                if (id == null) id = NO_INDENT_ENGINE; 
                map.put(INDENT_ENGINE_PROP, id);
                updateSettings(PropertiesMIMEProcessor.class, map);
            }

            refreshIndentEngineSettings();
        }
    }
    
    private void refreshIndentEngineSettings() {
        // Touches the settings
        RequestProcessor.postRequest(new Runnable(){
            public void run(){
                Settings.touchValue(getKitClass(), NbEditorDocument.INDENT_ENGINE);
                Settings.touchValue(getKitClass(), NbEditorDocument.FORMATTER);
            }
        });
    }
    
    /** Return class of the default indentation engine. */
    protected Class getDefaultIndentEngineClass() {
        return SimpleIndentEngine.class;
    }
    
    private IndentEngine findDefaultIndentEngine() {
        if (getDefaultIndentEngineClass() != null) {
            ServiceType.Registry sr = TopManager.getDefault().getServices();
            Enumeration en = sr.services(getDefaultIndentEngineClass());
            if (en.hasMoreElements()) {
                return (IndentEngine)en.nextElement();
            }
        }
        
        return null;
    }
    
    public void setOptionsVersion(int optionsVersion) {
        int oldOptionsVersion = this.optionsVersion;
        this.optionsVersion = optionsVersion;

        if (optionsVersion != oldOptionsVersion) {
            firePropertyChange(OPTIONS_VERSION_PROP,
            new Integer(oldOptionsVersion), new Integer(optionsVersion));
        }

    }
    
    public int getOptionsVersion() {
        return optionsVersion;
    }
    
    public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
        
        /** Hold the current indent engine due to #11212 */
        readExternalIndentEngine = getIndentEngine();
        inReadExternal = true;
        
        /* Make the current options version to be zero
         * temporarily to distinguish whether the options
         * imported were old and the setOptionsVersion()
         * was not called or whether the options
         * were new so the options version was set
         * to the LATEST_OPTIONS_VERSION value.
         */
        optionsVersion = 0;
        
        try {
            // Read the serialized options
            super.readExternal(in);
        }catch(java.io.OptionalDataException ode){
            // #17385. It occurs during reading Settings.settings, that is unimportant
        } finally {

            // Make sure the indent engine settings are propagated
            // (SharedClassObject.putProperty() is final)
            refreshIndentEngineSettings();

            // Possibly upgrade the options
            if (optionsVersion < LATEST_OPTIONS_VERSION) {
                upgradeOptions(optionsVersion, LATEST_OPTIONS_VERSION);
            }

            optionsVersion = LATEST_OPTIONS_VERSION;

            /** Release temp indent engine -  #11212 */
            inReadExternal = false;
            readExternalIndentEngine = null;
        }
    }
    
    /** Upgrade the deserialized options.
     * @param version deserialized version of the options
     * @param latestVersion latest version of the options
     *   that will be set to them after they are upgraded
     */
    protected void upgradeOptions(int version, int latestVersion) {
        // Upgrade in separate class to avoid messing up BaseOptions
        UpgradeOptions.upgradeOptions(this, version, latestVersion);
    }
    
    /** Load settings from XML files and initialize changes */
    private void loadSettings(Class processor){
        MIMEOptionFile file;
        if (BASE.equals(getTypeName())){
            MIMEOptionFolder mimeFolder = AllOptionsFolder.getDefault().getMIMEFolder();
            if (mimeFolder == null) return;
            file= mimeFolder.getFile(processor, false);
        }else{
            MIMEOptionFolder mimeFolder = getMIMEFolder();
            if (mimeFolder == null) return;
            file= mimeFolder.getFile(processor, false);
        }
        if ((file!=null) && (!file.isLoaded())) {
            file.loadSettings();
        }
    }
    
    /** Save changes to XML files */
    private void updateSettings(Class processor, Map settings){
        MIMEOptionFile fileX;
        MIMEOptionFolder mimeFolder;
        if (BASE.equals(getTypeName())){
            mimeFolder = AllOptionsFolder.getDefault().getMIMEFolder();
            if (mimeFolder == null) return;
            fileX = mimeFolder.getFile(processor, true);
        }else{
            mimeFolder = getMIMEFolder();
            if (mimeFolder == null) return;
            fileX = mimeFolder.getFile(processor, true);
        }
        final Map finalSettings = settings;
        final MIMEOptionFile file = fileX;
        if (file!=null){
            RequestProcessor.postRequest(new Runnable(){
                public void run(){
                    file.updateSettings(finalSettings);
                }
            });
            
        } else {
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!"+processor.toString()+" type file haven't been found in folder:"+mimeFolder.getDataFolder()); //TEMP
        }
    }
    
    public void setSettingValue(String settingName, Object newValue) {
        setSettingValue(settingName, newValue, settingName);
    }
    
    private boolean isTheSame(String settingName, Object newValue){
        if (settingName == null ||
        settingName.equals(NbEditorDocument.INDENT_ENGINE) ||
        settingName.equals(NbEditorDocument.FORMATTER) ){
            return true;
        }
        Object oldValue = getSettingValue(settingName);
        if ((oldValue == null && newValue == null)
        || (oldValue != null && oldValue.equals(newValue))
        ) {
            return true; // the same object
        }
        return false;
    }
    
    /** Sets setting value to initializer Map and save the changes to XML file
     *  (properties.xml) */
    public void setSettingValue(String settingName, Object newValue,
    String propertyName) {
        if (!isTheSame(settingName, newValue)){
            Map map = new HashMap();
            map.put(settingName, newValue);
            updateSettings(PropertiesMIMEProcessor.class, map);
        }
        super.setSettingValue(settingName,newValue,propertyName);
    }
    
    public Object getSettingValue(String settingName) {
        loadSettings(PropertiesMIMEProcessor.class);
        return super.getSettingValue(settingName);
    }
    
    protected final void setSettingBoolean(String settingName, boolean newValue, String propertyName) {
        setSettingValue(settingName, newValue ? Boolean.TRUE : Boolean.FALSE);
    }
    
    protected final void setSettingInteger(String settingName, int newValue, String propertyName) {
        setSettingValue(settingName, new Integer(newValue));
    }
    
    /** Load all available settings from XML files and initialize them */
    protected void loadXMLSettings(){
        getKeyBindingList();
        getAbbrevMap();
        getMacroMap();
        loadSettings(FontsColorsMIMEProcessor.class);
        loadSettings(PropertiesMIMEProcessor.class);
    }

    /** Overriden writeExternal method. BaseOptions are no longer serialized. */
    public void writeExternal() throws IOException{
    }
    
    /** Overriden writeExternal method. BaseOptions are no longer serialized. */
    public void writeExternal(ObjectOutput out) throws IOException{
    }
    
}
