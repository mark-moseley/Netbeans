/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.properties.editors;

import java.io.File;
/*
 * ClasspathCustomEditorOperator.java
 *
 * Created on 6/13/02 4:40 PM
 */

import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.ListModel;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling Classpath Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0 */
public class ClasspathCustomEditorOperator extends NbDialogOperator {

    /** Creates new ClasspathCustomEditorOperator
     * @throws TimeoutExpiredException when NbDialog not found
     * @param title String title of custom editor */
    public ClasspathCustomEditorOperator(String title) {
        super(title);
    }

    /** Creates new ClasspathCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public ClasspathCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }

    private JButtonOperator _btAddDirectory;
    private JButtonOperator _btMoveDown;
    private JListOperator _lstClasspath;
    private JButtonOperator _btAddJARZIP;
    private JButtonOperator _btRemove;
    private JButtonOperator _btMoveUp;


    /** Tries to find "Add Directory..." JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btAddDirectory() {
        if (_btAddDirectory==null) {
            _btAddDirectory = new JButtonOperator( this, "Add Directory...", 0 );
        }
        return _btAddDirectory;
    }

    /** Tries to find "Move Down" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btMoveDown() {
        if (_btMoveDown==null) {
            _btMoveDown = new JButtonOperator( this, "Move Down", 0 );
        }
        return _btMoveDown;
    }

    /** Tries to find null JList in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JListOperator
     */
    public JListOperator lstClasspath() {
        if (_lstClasspath==null) {
            _lstClasspath = new JListOperator( this, 0 );
        }
        return _lstClasspath;
    }

    /** Tries to find "Add JAR/ZIP..." JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btAddJARZIP() {
        if (_btAddJARZIP==null) {
            _btAddJARZIP = new JButtonOperator( this, "Add JAR/ZIP...", 0 );
        }
        return _btAddJARZIP;
    }

    /** Tries to find "Remove" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btRemove() {
        if (_btRemove==null) {
            _btRemove = new JButtonOperator( this, "Remove", 0 );
        }
        return _btRemove;
    }

    /** Tries to find "Move Up" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btMoveUp() {
        if (_btMoveUp==null) {
            _btMoveUp = new JButtonOperator( this, "Move Up", 0 );
        }
        return _btMoveUp;
    }

    /** clicks on "Add Directory..." JButton
     * @throws TimeoutExpiredException when JButton not found
     * @return FileCustomEditorOperator of directory selector */
    public FileCustomEditorOperator addDirectory() {
        btAddDirectory().pushNoBlock();
        return new FileCustomEditorOperator(Bundle.getString("org.netbeans.beaninfo.editors.Bundle", "CTL_FileSystemPanel.Local_Dialog_Title"));
    }

    /** clicks on "Move Down" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void moveDown() {
        btMoveDown().push();
    }

    /** clicks on "Add JAR/ZIP..." JButton
     * @throws TimeoutExpiredException when JButton not found
     * @return FileCustomEditorOperator of JAR or ZIP file selector */
    public FileCustomEditorOperator addJARZIP() {
        btAddJARZIP().pushNoBlock();
        return new FileCustomEditorOperator(Bundle.getString("org.netbeans.beaninfo.editors.Bundle", "CTL_FileSystemPanel.Jar_Dialog_Title"));
    }

    /** adds directory into classpath list
     * @param directoryPath String directory path to be added */    
    public void addDirectory(String directoryPath) {
        FileCustomEditorOperator editor=addDirectory();
        editor.setFileValue(directoryPath);
        editor.ok();
    }

    /** adds JAR or ZIP file into classpath list
     * @param filePath String path of JAR or ZIP file to be added */    
    public void addJARZIP(String filePath) {
        FileCustomEditorOperator editor=addJARZIP();
        editor.setFileValue(filePath);
        editor.ok();
    }

    /** adds directory into classpath list
     * @param directory File directory to be added */    
    public void addDirectory(File directory) {
        FileCustomEditorOperator editor=addDirectory();
        editor.setFileValue(directory);
        editor.ok();
    }

    /** adds JAR or ZIP file into classpath list
     * @param jarZip File JAR or ZIP to be added */    
    public void addJARZIP(File jarZip) {
        FileCustomEditorOperator editor=addJARZIP();
        editor.setFileValue(jarZip);
        editor.ok();
    }
    
    /** sets complete classpath in custom editor
     * @param classPathElements File[] array of directories or JAR or ZIP files to be included in classapth */    
    public void setClasspathValue(File[] classPathElements) {
        removeAll();
        for (int i=0; i<classPathElements.length; i++) {
            if (classPathElements[i].isFile())
                addJARZIP(classPathElements[i]);
            else
                addDirectory(classPathElements[i]);
        }
    }
    
    /** sets complete classpath in custom editor
     * @param classPathElements String[] array of paths of directories or JAR or ZIP files to be included in classapth */    
    public void setClasspathValue(String[] classPathElements) {
        removeAll();
        for (int i=0; i<classPathElements.length; i++) {
            String lower=classPathElements[i].toLowerCase();
            if (lower.endsWith(".jar")||lower.endsWith(".zip"))
                addJARZIP(classPathElements[i]);
            else
                addDirectory(classPathElements[i]);
        }
    }

    /** clicks on "Remove" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void remove() {
        btRemove().push();
    }

    /** clicks on "Move Up" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void moveUp() {
        btMoveUp().push();
    }
    
    /** removes given item from classpath
     * @param value String item to be removed */    
    public void remove(String value) {
        lstClasspath().selectItem(value);
        remove();
    }
    
    /** removes complete classpath */    
    public void removeAll() {
        while (lstClasspath().getModel().getSize()>0) {
            lstClasspath().selectItem(0);
            remove();
        }
    }
    
    /** returns complete class path from editor
     * @return String[] class paths */    
    public String[] getClasspathValue() {
        ArrayList data=new ArrayList();
        ListModel model=lstClasspath().getModel();
        for (int i=0; i<model.getSize(); i++)
            data.add(model.getElementAt(i).toString());
        return (String[])data.toArray(new String[data.size()]);
    }
}

