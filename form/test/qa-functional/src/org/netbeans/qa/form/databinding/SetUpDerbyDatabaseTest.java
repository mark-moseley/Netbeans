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

package org.netbeans.qa.form.databinding;

import java.io.File;
import java.io.FileNotFoundException;
import org.netbeans.qa.form.*;
import org.netbeans.qa.form.visualDevelopment.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.*;
import org.netbeans.qa.form.ExtJellyTestCase;
import java.util.*;
import org.netbeans.jellytools.modules.db.nodes.DatabasesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Set up Derby DB for following tests
 *
 * @author Jiri Vagner
 */
public class SetUpDerbyDatabaseTest  extends ExtJellyTestCase {
    public static String TMP_DB_DIR_NAME = "TempDerbyDatabaseDir" + String.valueOf(new Date().getTime());;
    public static String DEFAULT_DERBY_DIR_NAME = "db";    

    public static String JAVA_DB_MENU = "Tools|Java DB Database|";

    public static String DB_START_SERVER_MENU = JAVA_DB_MENU + "Start Server";
    public static String DB_STOP_SERVER_MENU = JAVA_DB_MENU + "Stop Server";
    public static String DB_CREATE_DB_MENU = JAVA_DB_MENU + "Create Database...";
    public static String DB_SETTINGS_MENU = JAVA_DB_MENU + "Settings";    
    
    public static String DB_NAME = "testdatabase";
    public static String DB_USER_NAME = "testuser";
    public static String DB_PASSWORD = "testpassword";
    
    public static String JDBC_URL = String.format("jdbc:derby://localhost:1527/%s [%s on %s]",DB_NAME,DB_USER_NAME,DB_USER_NAME.toUpperCase());
    public static String DB_EXPLORER_NODE_PATH = "Databases|" + JDBC_URL;
    
    public static String DB_SETTINGS_DIALOG_TITLE = "Java DB Settings";
    public static String DB_CREATE_DB_DIALOG_TITLE = "Create Java DB Database";
    
    /**
     * Constructor required by JUnit
     */
    public SetUpDerbyDatabaseTest(String testName) {
        super(testName);
    }
    
    /**
     * Method allowing to execute test directly from IDE.
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /**
     * Creates suite from particular test cases.
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new SetUpDerbyDatabaseTest("testSetupDbPaths")); // NOI18N
        suite.addTest(new SetUpDerbyDatabaseTest("testStartDbServer")); // NOI18N
        suite.addTest(new SetUpDerbyDatabaseTest("testCreateDb")); // NOI18N        
        suite.addTest(new SetUpDerbyDatabaseTest("testConnectDbAndCreateTables")); // NOI18N
//        suite.addTest(new SetUpDerbyDatabaseTest("testStopDbServer")); // NOI18N        
        
        return suite;
    }
    
    /** Sets db server path and database folder path */
    public void testSetupDbPaths() {
        String derbyHomeDirPath = null;
        String dbDirPath = null;
        
        try {
            derbyHomeDirPath = getDerbyHomeDir();
            dbDirPath = getTempDerbyDbDir();
        } catch (Exception e){
            fail(e.getMessage());
        }
        
        if (derbyHomeDirPath != null && dbDirPath != null) {
            // invoke and catch db settings dialog
            new ActionNoBlock(DB_SETTINGS_MENU, null).perform(); // NOI18N
            DialogOperator op = new DialogOperator(DB_SETTINGS_DIALOG_TITLE);
            
            // setup derby db home 
            JTextFieldOperator derbyHomeOp = new JTextFieldOperator(op,1);
            derbyHomeOp.clearText();
            derbyHomeOp.typeText(derbyHomeDirPath);
            
            // setup database folder
            JTextFieldOperator dbPathOp = new JTextFieldOperator(op,0);
            dbPathOp.clearText();
            dbPathOp.typeText(dbDirPath);
            
            // close settings dialog
            new JButtonOperator(op,"OK").clickMouse(); // NOI18N
        }
    }
    
    /** Starts db server */
    public void testStartDbServer() {
        new Action(DB_START_SERVER_MENU, null).perform(); // NOI18N
        waitAMoment();
    }

    /** Stops db server */
    public void testStopDbServer() {
        new Action(DB_STOP_SERVER_MENU, null).perform(); // NOI18N
        waitAMoment();
    }
    
    /** Creates sample db */
    public void testCreateDb() {
        new ActionNoBlock(DB_CREATE_DB_MENU, null).perform(); // NOI18N

        DialogOperator op = new DialogOperator(DB_CREATE_DB_DIALOG_TITLE);
        new JTextFieldOperator(op,0).typeText(DB_PASSWORD);
        new JTextFieldOperator(op,1).typeText(DB_USER_NAME);
        new JTextFieldOperator(op,3).typeText(DB_NAME);
        
        JButtonOperator okOp = new JButtonOperator(op,"OK"); // NOI18N
        
        if (okOp.isEnabled()) {
            okOp.clickMouse();
        } else {
            // db already exists
            new JButtonOperator(op,"Cancel").clickMouse(); // NOI18N
        }
    }

    /** Connects to database and creates test db tables */
    public void testConnectDbAndCreateTables() throws ClassNotFoundException {
        DatabasesNode dbs = DatabasesNode.invoke();
        
        if (dbs.isCollapsed())
            dbs.expand();
        
        Node node = new Node(dbs.tree(), DB_EXPLORER_NODE_PATH);
        node.expand();
        new ActionNoBlock(null,"Connect...").perform(node); // NOI18N

        waitAMoment();
        
        /*
        // write password into connect dialog
        NbDialogOperator dialogOp = new NbDialogOperator("Connect");
        new JTextFieldOperator(dialogOp, 0).typeText(DB_PASSWORD);
        new JCheckBoxOperator(dialogOp).setSelected(true);
        new JButtonOperator(dialogOp,"OK").clickMouse();
        */

        Node tableNode = new Node(dbs.tree(), DB_EXPLORER_NODE_PATH + "|Tables"); // NOI18N
        tableNode.expand();
        waitAMoment();
        String[] tableNames = tableNode.getChildren();
        
        if (tableNames.length == 0) {
            // create tables
            runPopupOverNode("Execute Command...", node); // NOI18N
            
            waitAMoment();

            EditorOperator editOp = new EditorOperator("SQL Command"); // NOI18N
            editOp.insert(getSqlCommands());
            editOp.getToolbarButton(0).clickMouse();
        }
    }
    
    /** Finds DB server folder */
    public static String getDerbyHomeDir() throws Exception  {
        File derbyDir;
        
        String derbyHomeEnv = System.getenv("DERBY_HOME"); // NOI18N
        
        if (derbyHomeEnv != null && (derbyHomeEnv.length() > 0)) {
            // environmental variable DERBY_HOME is set
            derbyDir = new File(derbyHomeEnv);
            
            if (derbyDir.exists())
                return derbyDir.getPath();
            
        } else {
            // try to find derby in JDK home
            String jdkHomePath = System.getProperty("java.home"); // NOI18N
            File jdkHomeDir = new File(jdkHomePath);
            
            if (jdkHomeDir.exists()) {
                String upperJdkPath = jdkHomePath.substring(0, jdkHomePath.lastIndexOf(File.separator)); // cut last dir
                derbyDir = new File(upperJdkPath + File.separator + DEFAULT_DERBY_DIR_NAME);

                if (derbyDir.exists())
                    return derbyDir.getPath();
            }
        }
        throw new FileNotFoundException("Java DB (Derby) home dir was not found."); // NOI18N
    }

    /** Find temp folder for database files */
    public static String getTempDerbyDbDir() throws Exception  {
        String tmpPath = System.getProperty("java.io.tmpdir"); // NOI18N
        
        File tmpDir = new File(tmpPath);
        
        if (tmpDir.exists() && tmpDir.isDirectory()) {
            File tmpDbDir = new File(tmpDir.getPath() + File.separator + TMP_DB_DIR_NAME + File.separator);
            
            if (!tmpDbDir.exists()) {
                tmpDbDir.mkdir();
            }
            
            return tmpDbDir.getPath();
            
        } else {
            throw new FileNotFoundException("Invalid \"java.io.tmpdir\" path " + tmpPath ); // NOI18N
        }
    }
    
    /** returns SQL commands for creating content of test database  */
    public static String getSqlCommands() {
        StringBuilder build = new StringBuilder();
        build.append("create table BOOK (\"ID\" INTEGER not null primary key,\"AUTHOR\" VARCHAR(150),\"TITLE\" VARCHAR(150),\"PRICE\" INTEGER);"); // NOI18N
        build.append("create table CAR (\"ID\" INTEGER not null primary key,\"MAKE\" VARCHAR(50),\"MODEL\" VARCHAR(50),\"PRICE\" INTEGER,\"BODY_STYLE\" VARCHAR(30));"); // NOI18N
        build.append("insert into CAR values (854, 'Lancia', 'Lybra', 15590, 'combi');"); // NOI18N
        build.append("insert into CAR values (852, 'Daewoo', 'Leganza', 10590, 'sedan');"); // NOI18N
        build.append("insert into CAR values (456, 'MG Rover', '75', 47075, 'sedan');"); // NOI18N
        build.append("insert into CAR values (789, 'Fiat', 'Stillo', 30590, 'combi');"); // NOI18N
        build.append("insert into CAR values (321, 'Jaguar', 'S-Type', 80590, 'sedan');"); // NOI18N
        build.append("insert into BOOK values (1,'Christopher Paolini', 'Eragon', 12);"); // NOI18N
        build.append("insert into BOOK values (2,'Christopher Paolini', 'Eldest', 10);"); // NOI18N
        build.append("insert into BOOK values (3,'J.R.R. Tolkien', 'The Hobit', 5);"); // NOI18N
        build.append("insert into BOOK values (4,'J.R.R. Tolkien', 'The Fellowship of The Ring', 5);"); // NOI18N
        build.append("insert into BOOK values (5,'J.R.R. Tolkien', 'The Two Towers', 5);"); // NOI18N
        build.append("insert into BOOK values (6,'J.R.R. Tolkien', 'The Return of The King', 5);"); // NOI18N
        return build.toString();
    }
}
