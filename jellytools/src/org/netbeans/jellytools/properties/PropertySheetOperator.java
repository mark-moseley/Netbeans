/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.properties;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import javax.swing.JComponent;
import org.netbeans.core.NbSheet;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.HelpAction;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.actions.ShowDescriptionAreaAction;
import org.netbeans.jellytools.actions.SortByCategoryAction;
import org.netbeans.jellytools.actions.SortByNameAction;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.WindowOperator;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.windows.TopComponent;

/**
 * Handles org.openide.explorer.propertysheet.PropertySheet which
 * represents IDE property sheet TopComponent.
 * It includes JTable with properties and optional description area.
 * Use {@link Property} class or its descendants to work with properties.
 * <p>
 * Usage:<br>
 * <pre>
        PropertySheetOperator pso = new PropertySheetOperator("Properties of MyClass");
        new Property(pso, "Arguments").setValue("arg1 arg2");
        pso.sortByName();
        System.out.println("Number of properties="+pso.tblSheet().getRowCount());
        pso.sortByCategory();
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com
 * @see Property
 * @see PropertiesAction
 * @see SortByCategoryAction
 * @see SortByNameAction
 * @see ShowDescriptionAreaAction
 * @see HelpAction
 */
public class PropertySheetOperator extends TopComponentOperator {
    // In IDE PropertySheet extends JPanel (parent org.netbeans.core.NbSheet extends TopComponent).
    /* In new window system global property sheet resides in main window and extends TopComponent.
     * Other property sheets are opened in dialog and they do not behave as TopComponents.
     */
    
    /** JTable representing property sheet. */
    private JTableOperator _tblSheet;
    private JLabelOperator _lblDescriptionHeader;
    private JTextAreaOperator _txtDescription;
    private JButtonOperator _btHelp;
    
    /** "No Properties" property sheet. */
    public static final int MODE_NO_PROPERTIES = 0;
    /** "Properties of" property sheet. */
    public static final int MODE_PROPERTIES_OF_ONE_OBJECT = 1;
    /** "Properties of Multiple Objects" property sheet. */
    public static final int MODE_PROPERTIES_OF_MULTIPLE_OBJECTS = 2;
    
    /** "Properties" */
    private static final String propertiesText = Bundle.getStringTrimmed("org.openide.actions.Bundle",
                                                                         "Properties");
    
    /** Generic constructor
     * @param sheet instance of PropertySheet
     */
    public PropertySheetOperator(JComponent sheet) {
        super(sheet);
    }
    
    /** Waits for property sheet anywhere in IDE. */
    public PropertySheetOperator() {
        this(waitPropertySheet(null, 0));
    }
    
    /** Waits for property sheet with name according to given mode ("No Properties",
     * "Properties of" or "Properties of Multiple Objects").
     * @param mode type of shown properties
     * @see #MODE_NO_PROPERTIES
     * @see #MODE_PROPERTIES_OF_ONE_OBJECT
     * @see #MODE_PROPERTIES_OF_MULTIPLE_OBJECTS
     */
    public PropertySheetOperator(int mode) {
        this(mode, "");
    }
    
    /** Waits for property sheet with name according to given mode ("No Properties",
     * "Properties of" or "Properties of Multiple Objects") plus objectName
     * in case of one object property sheet. In case of usage
     * <code>
     * new PropertySheetOperator(PropertySheetOperator.MODE_PROPERTIES_OF_ONE_OBJECT, "MyClass");
     * </code>
     * will be searched property sheet with name "Properties of MyClass" (on
     * English locale).
     * @param mode type of shown properties
     * @param objectName name of object for that properties are shown (e.g. "MyClass")
     * @see #MODE_NO_PROPERTIES
     * @see #MODE_PROPERTIES_OF_ONE_OBJECT
     * @see #MODE_PROPERTIES_OF_MULTIPLE_OBJECTS
     */
    public PropertySheetOperator(int mode, String objectName) {
        this(Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_GlobalProperties",
                              new Object[]{new Integer(mode), objectName}));
    }
    
    /** Waits for property sheet with given name. Typically sheet
     * name is used as window title.
     * @param sheetName name of sheet to find (e.g. "Properties of MyClass")
     */
    public PropertySheetOperator(String sheetName) {
        this(waitPropertySheet(sheetName, 0));
    }
    
    /** Waits for property sheet with given name in specified
     * container.
     * @param contOper where to find
     * @param sheetName name of sheet to find (e.g. "Properties of MyClass")
     */
    public PropertySheetOperator(ContainerOperator contOper, String sheetName) {
        super((JComponent)contOper.waitSubComponent(new PropertySheetSubchooser(sheetName, contOper.getComparator())));
        copyEnvironment(contOper);
    }
    
    /** Waits for property sheet in specified ContainerOperator.
     * It is for example PropertySheet in Options window.
     * @param contOper where to find
     */
    public PropertySheetOperator(ContainerOperator contOper) {
        this(contOper, 0);
    }
    
    /** Waits for index-th property sheet in specified ContainerOperator.
     * @param contOper where to find
     * @param index int index
     */
    public PropertySheetOperator(ContainerOperator contOper, int index) {
        super((JComponent)contOper.waitSubComponent(new PropertySheetSubchooser(), index));
        copyEnvironment(contOper);
    }
    
    /** Invokes properties by default action on currently selected object.
     * @return instance of PropertySheetOperator
     * @see org.netbeans.jellytools.actions.PropertiesAction
     */
    public static PropertySheetOperator invoke() {
        new PropertiesAction().perform();
        return new PropertySheetOperator();
    }
    
    /** Returns JTableOperator representing SheetTable of this property sheet. 
     * @return instance of JTableOperator
     */
    public JTableOperator tblSheet() {
        if(_tblSheet == null) {
            _tblSheet = new JTableOperator(this);
        }
        return _tblSheet;
    }
    
    /** Returns JLabelOperator representing header of description area.
     * @return instance of JLabelOperator
     */
    public JLabelOperator lblDescriptionHeader() {
        if(_lblDescriptionHeader == null) {
            _lblDescriptionHeader = new JLabelOperator(this);
        }
        return _lblDescriptionHeader;
    }
    
    /** Returns JTextAreaOperator representing text from description area.
     * @return instance of JTextAreaOperator
     */
    public JTextAreaOperator txtDescription() {
        if(_txtDescription == null) {
            _txtDescription = new JTextAreaOperator(this);
        }
        return _txtDescription;
    }
    
    /** Returns JButtonOperator representing help button of description area.
     * @return instance of JButtonOperator
     */
    public JButtonOperator btHelp() {
        if(_btHelp == null) {
            _btHelp = new JButtonOperator(this);
        }
        return _btHelp;
    }
    
    /** Gets JTabbedPaneOperator of this property sheet.
     * @return instance of JTabbedPaneOperator
     * @deprecated JTabbedPane is no more used in property sheet.
     */
    public JTabbedPaneOperator tbpPropertySheetTabbedPane() {
        throw new JemmyException("Don't use this! JTabbedPane no more used in property sheet.");
        /*
        if(_tbpPropertySheetTabPane == null) {
            _tbpPropertySheetTabPane = new JTabbedPaneOperator(this);
        }
        return _tbpPropertySheetTabPane;
         */
    }
    
    /** Gets PropertySheetTabOperator with given name. It selects requested tab
     * if exist and it is not active.
     * @param tabName name of tab to find.
     * @return instance of PropertySheetTabOperator
     * @deprecated will be removed because of property sheet rewrite
     */
    public PropertySheetTabOperator getPropertySheetTabOperator(String tabName) {
        return new PropertySheetTabOperator(this, tabName);
    }
    
    /** Gets PropertySheetToolbarOperator for this property sheet.
     * @return instance of PropertySheetToolbarOperator
     * @deprecated Tool bar no more used in property sheet.
     */
    public PropertySheetToolbarOperator getToolbar() {
        throw new JemmyException("Don't use this! Tool bar no more used in property sheet.");
        //return new PropertySheetToolbarOperator(this);
    }

    /** Gets text of header from description area.
     * @return text of header from description area
     */
    public String getDescriptionHeader() {
        return lblDescriptionHeader().getText();
    }
    
    /** Gest description from description area.
     * @return description from description area.
     */
    public String getDescription() {
        return txtDescription().getText();     
    }
    
    /** Sorts properties by name by calling of popup menu on property sheet. */
    public void sortByName() {
        new SortByNameAction().perform(this);
    }

    /** Sorts properties by category by calling of popup menu on property sheet. */
    public void sortByCategory() {
        new SortByCategoryAction().perform(this);
    }
    
    /** Shows or hides description area depending on whether it is already shown 
     * or not. It just invokes Show description area popup menu item.
     */
    public void showDescriptionArea() {
        new ShowDescriptionAreaAction().perform(this);
    }
    
    /** Shows help by calling popup menu on property sheet. */
    public void help() {
        new HelpAction().performPopup(this);
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tblSheet();
    }
    
    /** Closes this property sheet and waits until 
     * it is not closed. In fact it closes container in which this property 
     * sheet is placed. It can be a TopComponent in the main window or in a separate
     * frame, or a dialog.
     */
    public void close() {
        if(getSource() instanceof TopComponent) {
            if(((TopComponent)getSource()).canClose()) {
                // if it is regular TopComponent and it can be closed
                super.close();
                return;
            }
        } 
        // close window where property sheet is hosted but not main window
        if(getWindow() != MainWindowOperator.getDefault().getSource()) {
            new WindowOperator(getWindow()).close();
        }
    }
    
    /** Finds property sheet anywhere in IDE. First it tries to find TopComponent
     * representing global properties and if not found, it tries to find 
     * property sheet in all dialogs owned by Main Window or other frames in SDI.
     * @param sheetName name of property sheet
     * @param index index of property sheet 
     */
    private static JComponent findPropertySheet(String sheetName, int index) {
        // try to find PS in MainWindow
        JComponent comp = findTopComponent(null, sheetName, index, new PropertySheetSubchooser());
        if(comp != null) {
            return comp;
        }
        // Try to find PS in a dialog which is owned by Main window or by other
        // frame in SDI.
        Frame[] frames;
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        if(mwo.isMDI()) {
            frames = new Frame[] {(Frame)mwo.getSource()};
        } else {
            frames = Frame.getFrames();
        }
        for(int frameIndex=0;frameIndex<frames.length;frameIndex++) {
            Window[] windows = frames[frameIndex].getOwnedWindows();
            for(int i=0;i<windows.length;i++) {
                if(windows[i].isShowing()) {
                    // only showing windows are interesting
                    // create windows operator for found window
                    WindowOperator wo = new WindowOperator(windows[i]);
                    // supress output
                    wo.setOutput(TestOut.getNullOutput());
                    // try to find PropertySheet subcomponent
                    comp = (JComponent)wo.findSubComponent(
                                new PropertySheetSubchooser(sheetName, mwo.getComparator()), 
                                index);
                    if(comp != null) {
                        return comp;
                    }
                }
            }
        }
        return null;
    }
    
    /** Waits for property sheet anywhere in IDE. First it tries to find TopComponent
     * representing global properties and if not found, it tries to find 
     * property sheet in all dialogs owned by Main Window or other frames in SDI.
     * @param sheetName name of property sheet
     * @param index index of property sheet 
     */
    private static JComponent waitPropertySheet(final String sheetName, final int index) {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object obj) {
                    return findPropertySheet(sheetName, index);
                }
                public String getDescription() {
                    return("Wait PropertySheet with name="+sheetName+
                           " index="+index+" loaded");
                }
            });
            Timeouts times = JemmyProperties.getCurrentTimeouts().cloneThis();
            times.setTimeout("Waiter.WaitingTime", times.getTimeout("ComponentOperator.WaitComponentTimeout"));
            waiter.setTimeouts(times);
            waiter.setOutput(JemmyProperties.getCurrentOutput());
            return (JComponent)waiter.waitAction(null);
        } catch(InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
    }

    /** SubChooser to determine find property sheet.
     * Used in constructors.
     */
    private static final class PropertySheetSubchooser implements ComponentChooser {
        
        private String sheetName;
        private StringComparator comparator;
        
        public PropertySheetSubchooser() {
        }

        public PropertySheetSubchooser(String sheetName, StringComparator comparator) {
            this.sheetName = sheetName;
            this.comparator = comparator;
        }
        
        public boolean checkComponent(Component comp) {
            if(comp instanceof PropertySheet || comp instanceof NbSheet) {
                if(sheetName == null) {
                    return true;
                } else {
                    if(comp instanceof TopComponent) {
                        String name = ((TopComponent)comp).getDisplayName();
                        if(name == null) {
                            name = comp.getName();
                        }
                        return comparator.equals(name, sheetName);
                    }
                }
            }
            return false;          
        }
        
        public String getDescription() {
            return "org.openide.explorer.propertysheet.PropertySheet";
        }
    }
}
