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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.test.php;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.JemmyException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.jemmy.operators.JListOperator;
import junit.framework.Test;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import java.util.List;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

/*

  Commit validation test for PHP.
  Some code just copied from other tests to avoid any affects from
  current changes in working test set.

*/

public class Commit extends GeneralPHP
{
  static private final String TEST_PHP_NAME_1 = "PhpProject_commit_0001";

  static private final String INDEX_PHP_INITIAL_CONTENT =
    "<!DOCTYPEHTMLPUBLIC\"-//W3C//DTDHTML4.01Transitional//EN\"><html><head><metahttp-equiv=\"Content-Type\"content=\"text/html;charset=UTF-8\"><title></title></head><body><?php//putyourcodehere?></body></html>";

  static private final String EMPTY_PHP_INITIAL_CONTENT =
    "<?php/**Tochangethistemplate,chooseTools|Templates*andopenthetemplateintheeditor.*/?>";

  static private final String CLASS_PHP_INITIAL_CONTENT =
    "<?php/**Tochangethistemplate,chooseTools|Templates*andopenthetemplateintheeditor.*//***DescriptionofPHPClass**@author" + System.getProperty( "user.name" ) + "*/classPHPClass{//putyourcodehere}?>";

  static private final int COMPLETION_LIST_THRESHOLD = 5000;
  static private final int COMPLETION_LIST_INCLASS = 22;

  public Commit( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( Commit.class ).addTest(
          "CreatePHPApplication",
          "ManipulateIndexPHP",
          "CreateEmptyPHP",
          "ManipulateEmptyPHP",
          "CreateTemplatePHP",
          "ManipulateTemplatePHP"/*,
          "OpenStandalonePHP",
          "ManipulateStandalonePHP",
          "CretaeCustomPHPApplication",
          "CreatePHPWithExistingSources"*/
        )
        .enableModules( ".*" )
        .clusters( ".*" )
        //.gui( true )
      );
  }

  public void CreatePHPApplication( )
  {
    startTest( );

    CreatePHPApplicationInternal( TEST_PHP_NAME_1 );

    endTest( );
  }

  protected CompletionJListOperator GetCompletion( )
  {
    CompletionJListOperator comp = null;
    int iRedo = 10;
    while( true )
    {
      try
      {
        comp = new CompletionJListOperator( );
        try
        {
          Object o = comp.getCompletionItems( ).get( 0 );
          if(
              !o.toString( ).contains( "No suggestions" )
              && !o.toString( ).contains( "Scanning in progress..." )
            )
          {
            return comp;
          }
          Sleep( 1000 );
        }
        catch( java.lang.Exception ex )
        {
          return null;
        }
      }
      catch( JemmyException ex )
      {
        System.out.println( "Wait completion timeout." );
        if( 0 == --iRedo )
          return null;
      }
      Sleep( 100 );
    }
  }

  protected void Backit( EditorOperator eoPHP, int iCount )
  {
    for( int i = 0; i < iCount; i++ )
      eoPHP.pressKey( KeyEvent.VK_BACK_SPACE );
  }

  protected void EnsureEmptyLine( EditorOperator eoPHP )
  {
    CheckResultRegex( eoPHP, "[ \t\r\n]*" );
  }

  protected void CompletePairCheck( EditorOperator eoPHP, String sCode, String sCheck )
  {
    TypeCodeCheckResult( eoPHP, sCode, sCheck );
    Backit( eoPHP, sCode.length( ) );
    EnsureEmptyLine( eoPHP );
  }

  protected String CreatePair( String sCode )
  {
    String sSuffix = "";
    boolean bQuote = true;
    for( int i = 0; i < sCode.length( ); i++ )
    {
      switch( sCode.charAt( i ) )
      {
        case '[':
          if( bQuote )
            sSuffix = "]" + sSuffix;
          break;
        case '(':
          if( bQuote )
            sSuffix = ")" + sSuffix;
          break;
        case ']':
          if( bQuote )
            sSuffix = sSuffix.substring( 1 );
          break;
        case ')':
          if( bQuote )
            sSuffix = sSuffix.substring( 1 );
          break;
        case '"':
          if( bQuote )
            sSuffix = "\"" + sSuffix;
          else
            sSuffix = sSuffix.substring( 1 );
          bQuote = !bQuote;
          break;
      }
    }
    return sCode + sSuffix;
  }

  protected void TestPHPFile(
      String sProjectName,
      String sFileName,
      String sInitialContent,
      boolean bInitialWait,
      String sCodeLocator,
      boolean bInclass,
      boolean bFormat
    )
  {
    // Check file in tree
    ProjectsTabOperator pto = new ProjectsTabOperator( );
    ProjectRootNode prn = pto.getProjectRootNode(
        sProjectName + "|Source Files|" + sFileName
      );
    prn.select( );

    // Check file opened
    EditorOperator eoPHP = new EditorOperator( sFileName );

    // Check file content
    String sText = eoPHP.getText( ).replaceAll( "[ \t\r\n]", "" );
    if( !sText.equals( sInitialContent ) )
      fail( "Invalid initial file content. Found: \"" + sText + "\". Expected: \"" + sInitialContent + "\"" );

    // Work with content

    // Locate
    eoPHP.setCaretPosition( sCodeLocator, false );
    // Insert new line
    eoPHP.insert( "\n" );
    if( bInitialWait )
      Sleep( 20000 );
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 1000 );

    // Check code completion list
    try
    {
      CompletionJListOperator jCompl = GetCompletion( );
      List list = jCompl.getCompletionItems( );
      // Magic CC number for complete list
      if(
          ( bInclass ? COMPLETION_LIST_INCLASS : COMPLETION_LIST_THRESHOLD )
          > list.size( )
        )
      {
        fail( "CC list looks to small, there are only: " + list.size( ) + " items in." );
      }

      jCompl.hideAll( );
    }
    catch( Exception ex )
    {
      fail( "Completion check failed: \"" + ex.getMessage( ) + "\"" );
    }

    // Brackets
    // Predefined
    String[] asCheckers =
    {
      "[(\"",
      "[(\"\")]",
      "name(",
      "name()",
      "name[",
      "name[]",
      "hello(a[\"1"
    };

    for( String sChecker : asCheckers )
    {
      String[] asChecker = sChecker.split( "[|]" );
      CompletePairCheck(
          eoPHP,
          asChecker[ 0 ],
          ( 1 == asChecker.length ) ? CreatePair( asChecker[ 0 ] ) : asChecker[ 1 ]
        );
    }

    // Check something random
    // Yes I know about StringBuffer :)
    String sRandom = "";
    String sCharset = "abc123[[[[[((((((\"";
    for( int i = 0; i < 50; i++ )
      sRandom = sRandom + sCharset.charAt( ( int )( Math.random( ) * sCharset.length( ) ) );
    CompletePairCheck( eoPHP, sRandom, CreatePair( sRandom ) );

    // Formatting
    if( bFormat )
    {
      TypeCode( eoPHP, "class a{function aa(){return;}}" );
      ClickForTextPopup( eoPHP, "format" );
      // Check return here
      // CheckResult( eoPHP, "some staff"
      // TODO
    }

    // Completion
    if( bInclass )
    {
      // start constructor
      TypeCode( eoPHP, "function __con" );
      eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
      Sleep( 500 );
      CheckResult( eoPHP, "function  __construct() {", -1 );
      int i = eoPHP.getLineNumber( ) - 1;
      eoPHP.deleteLine( i );
      eoPHP.deleteLine( i );
      eoPHP.deleteLine( i );
    }
    else
    {
      // start class declaration
      TypeCode( eoPHP, "class a ext" );
      eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
      Sleep( 500 );
      CheckResult( eoPHP, "class a extends" );
    }

    // Insertion
    if( !bInclass )
    {
      // Complete class declaration before code insertion
      TypeCode( eoPHP, "\n{\n" );
    }
    TypeCode( eoPHP, "public $a, $b;\nprotected $c, $d;\nprivate $e, $f;\n" );

    // Insert constructor
    eoPHP.pressKey( KeyEvent.VK_INSERT, InputEvent.ALT_MASK );

    JDialogOperator jdInsetter = new JDialogOperator( );
    JListOperator jlList = new JListOperator( jdInsetter );

    ClickListItemNoBlock( jlList, 0, 1 );

    JDialogOperator jdGenerator = new JDialogOperator( "Generate Constructor" );

    // Select all but $c
    JTreeOperator jtTree = new JTreeOperator( jdGenerator, 0 );
    jtTree.clickOnPath( jtTree.findPath( "a" ) );
    jtTree.clickOnPath( jtTree.findPath( "d" ) );
    jtTree.clickOnPath( jtTree.findPath( "e" ) );

    JButtonOperator jbOk = new JButtonOperator( jdGenerator, "OK" );
    jbOk.pushNoBlock( );
    jdGenerator.waitClosed( );

    // Check result
    String[] asResult =
    {
      "function __construct($a, $d, $e) {",
      "$this->a = $a;",
      "$this->d = $d;",
      "$this->e = $e;",
      "}"
    };
    CheckResult( eoPHP, asResult, -3 );
    // Remove added
    int il = eoPHP.getLineNumber( ) - 3;
    eoPHP.deleteLine( il );
    eoPHP.deleteLine( il );
    eoPHP.deleteLine( il );
    eoPHP.deleteLine( il );
    eoPHP.deleteLine( il );

    // Insert get
    eoPHP.pressKey( KeyEvent.VK_INSERT, InputEvent.ALT_MASK );

    jdInsetter = new JDialogOperator( );
    jlList = new JListOperator( jdInsetter );

    ClickListItemNoBlock( jlList, 3, 1 );

    jdGenerator = new JDialogOperator( "Generate Getters and Setters" );

    // Select all but $c
    jtTree = new JTreeOperator( jdGenerator, 0 );
    jtTree.clickOnPath( jtTree.findPath( "b" ) );
    jtTree.clickOnPath( jtTree.findPath( "c" ) );
    jtTree.clickOnPath( jtTree.findPath( "f" ) );

    jbOk = new JButtonOperator( jdGenerator, "OK" );
    jbOk.pushNoBlock( );
    jdGenerator.waitClosed( );

    // Check result
    String[] asResult2 =
    {
      "public function getB() {",
      "return $this->b;",
      "}",
      "",  
      "public function setB($b) {",
      "$this->b = $b;",
      "}",
      "",  
      "public function getC() {",
      "return $this->c;",
      "}",
      "",  
      "public function setC($c) {",
      "$this->c = $c;",
      "}",
      "",  
      "public function getF() {",
      "return $this->f;",
      "}",
      "",  
      "public function setF($f) {",
      "$this->f = $f;",
      "}"
    };
    CheckResult( eoPHP, asResult2, -24 );
    // Remove added
    il = eoPHP.getLineNumber( ) - 24;
    for( int i = 0; i < asResult.length; i++ )
      eoPHP.deleteLine( il );

    // Close to prevent affect on next tests
    eoPHP.close( false );
  }

  public void ManipulateIndexPHP( )
  {
    startTest( );

    TestPHPFile(
        TEST_PHP_NAME_1,
        "index.php",
        INDEX_PHP_INITIAL_CONTENT,
        true,
        "// put your code here",
        false,
        true
      );

    endTest( );
  }

  protected void CreatePHPFile(
      String sProject,
      String sItem,
      String sName
    )
  {
    ProjectsTabOperator pto = new ProjectsTabOperator( );
    ProjectRootNode prn = pto.getProjectRootNode( sProject );
    prn.select( );

    // Workaround for MacOS platform
    NewFileWizardOperator.invoke().cancel( );

    NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke( );
    opNewFileWizard.selectCategory( "PHP" );
    opNewFileWizard.selectFileType( sItem );
    opNewFileWizard.next( );

    JDialogOperator jdNew = new JDialogOperator( "New " + sItem );
    JTextComponentOperator jt = new JTextComponentOperator( jdNew, 0 );
    if( null != sName )
      jt.setText( sName );
    else
      sName = jt.getText( );

    opNewFileWizard.finish( );

    // Check created schema in project tree
    String sPath = sProject + "|Source Files|" + sName;
    prn = pto.getProjectRootNode( sPath );
    prn.select( );
  }

  public void CreateEmptyPHP( )
  {
    startTest( );

    CreatePHPFile( TEST_PHP_NAME_1, "PHP File", null );

    endTest( );
  }

  public void ManipulateEmptyPHP( )
  {
    startTest( );

    TestPHPFile(
        TEST_PHP_NAME_1,
        "EmptyPHP.php",
        EMPTY_PHP_INITIAL_CONTENT,
        false,
        "*/",
        false,
        true
      );

    endTest( );
  }

  public void CreateTemplatePHP( )
  {
    startTest( );

    CreatePHPFile( TEST_PHP_NAME_1, "PHP Class", null );

    endTest( );
  }

  public void ManipulateTemplatePHP( )
  {
    startTest( );

    TestPHPFile(
        TEST_PHP_NAME_1,
        "PHPClass.php",
        CLASS_PHP_INITIAL_CONTENT,
        false,
        "//put your code here",
        true,
        false
      );

    endTest( );
  }

  public void OpenStandalonePHP( )
  {
    startTest( );

    endTest( );
  }

  public void ManipulateStandalonePHP( )
  {
    startTest( );

    endTest( );
  }

  public void CretaeCustomPHPApplication( )
  {
    startTest( );

    endTest( );
  }

  public void CreatePHPWithExistingSources( )
  {
    startTest( );

    endTest( );
  }

}
