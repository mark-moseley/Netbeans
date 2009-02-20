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

package org.netbeans.test.php.cc;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import java.util.List;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.WindowOperator;
import org.netbeans.jemmy.Timeouts;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class cc_0001 extends cc
{
  static final String TEST_PHP_NAME = "PhpProject_cc_0001";

  public cc_0001( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( cc_0001.class ).addTest(
          "CreateApplication",
          "Create_a_PHP_source_file",
          "Verify_automatic_code_completion_invocation",
          "Verify_local_variable_code_completion",
          "Verify_global_variable_code_completion",
          "Verify_variable_from_included_file_code_completion",
          "Verify_variable_from_required_file_code_completion",
          "Verify_code_completion_inside_the_identifier",
          "Verify_documentation_hints_for_built_in_identifiers",
          "Verify_documentation_hints_for_keywords",
          "Verify_keywords_code_completion",
          "Verify_code_completion_with_a_single_option",
          "Verify_JavaDoc_window",
          "Verify_code_completion_after_EXTENDS",
          //"Verify_that_require_directive_is_automatically_added",
          "Verify_code_completion_in_slash_slash_comments",
          "Verify_code_completion_in_slash_star_comments",
          "Verify_code_completion_in_slash_star_star_comments"
        )
        .enableModules( ".*" )
        .clusters( ".*" )
        //.gui( true )
      );
  }

  public void CreateApplication( )
  {
    startTest( );

    CreatePHPApplicationInternal( TEST_PHP_NAME );

    endTest( );
  }

  public void Create_a_PHP_source_file( )
  {
    startTest( );

    CreatePHPFile( TEST_PHP_NAME, "PHP File", null );

    endTest( );
  }

  public void Verify_automatic_code_completion_invocation( )
  {
    startTest( );

    EditorOperator eoPHP = new EditorOperator( "EmptyPHP.php" );

    //Sleep( 2000 );
    eoPHP.setCaretPosition( "*/\n", false );
    eoPHP.typeKey( '$' );
    Sleep( 1000 );

    // Check code completion list
    try
    {
      CompletionInfo completionInfo = GetCompletion( );
      if( null == completionInfo )
        fail( "NPE instead of competion info." );
      // Magic CC number for complete list
      if(
          DOLLAR_COMPLETION_LIST != completionInfo.listItems.size( )
        )
      {
        fail( "CC list looks to small, there are only: " + completionInfo.listItems.size( ) + " items in." );
      }

      // Check some completions
      String[] asCompletions =
      {
        "$GLOBALS",
        "$HTTP_RAW_POST_DATA",
        "$_COOKIE",
        "$_ENV",
        "$_FILES",
        "$_GET",
        "$_POST",
        "$_REQUEST",
        "$_SERVER",
        "$_SESSION",
        "$argc",
        "$argv",
        "$http_response_header",
        "$php_errormsg"
        //"$dirh",
        //"$new_obj"
      };
      CheckCompletionItems( completionInfo.listItself, asCompletions );
      completionInfo.listItself.hideAll( );
    }
    catch( Exception ex )
    {
      ex.printStackTrace( System.out );
      fail( "Completion check failed: \"" + ex.getMessage( ) + "\"" );
    }

    // Clean up
    eoPHP.pressKey( KeyEvent.VK_BACK_SPACE );

    endTest( );
  }

  public void Verify_local_variable_code_completion( )
  {
    startTest( );

    EditorOperator eoPHP = new EditorOperator( "EmptyPHP.php" );
    TypeCode( eoPHP, "function function_0001( )\n{\n$variable_0001 = 1;\n$va" );
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    CheckResult( eoPHP, "$variable_0001" );

    // Cleanup
    eoPHP.deleteLine( eoPHP.getLineNumber( ) );
    eoPHP.deleteLine( eoPHP.getLineNumber( ) - 1 );

    endTest( );
  }

  public void Verify_global_variable_code_completion( )
  {
    startTest( );

    EditorOperator eoPHP = new EditorOperator( "EmptyPHP.php" );
    eoPHP.setCaretPosition( "*/\n", false );
    TypeCode( eoPHP, "$variable_0002 = 2;\n" );
    eoPHP.setCaretPosition( "{", false );
    TypeCode( eoPHP, "\nglobal $va" );

    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    // THIS IS ISSUE
    //CheckResult( eoPHP, "global $variable_0002" );

    // Cleanup
    eoPHP.deleteLine( eoPHP.getLineNumber( ) );
    //eoPHP.setCaretPosition( "$variable_0002", false );
    //eoPHP.deleteLine( eoPHP.getLineNumber( ) );

    endTest( );
  }

  public void Verify_variable_from_included_file_code_completion( )
  {
    startTest( );

    // Create new file
    CreatePHPFile( TEST_PHP_NAME, "PHP File", null );

    // Include first file
    EditorOperator eoPHP = new EditorOperator( "EmptyPHP_1.php" );
    eoPHP.setCaretPosition( "*/\n", false );
    TypeCode( eoPHP, "include 'EmptyPHP.php';\n\n$va" );

    // Use global variable from first file
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 1000 );
    CheckResult( eoPHP, "$variable_0002" );

    // Cleanup
    eoPHP.deleteLine( eoPHP.getLineNumber( ) );

    endTest( );
  }

  public void Verify_variable_from_required_file_code_completion( )
  {
    startTest( );

    // Add required third into first file
    EditorOperator eoPHP = new EditorOperator( "EmptyPHP.php" );
    eoPHP.setCaretPosition( "*/\n", false );
    TypeCode( eoPHP, "require 'EmptyPHP_2.php';\n" );

    // Add third file
    CreatePHPFile( TEST_PHP_NAME, "PHP File", null );

    // Add variable into third file
    EditorOperator eoPHP_2 = new EditorOperator( "EmptyPHP_2.php" );
    eoPHP_2.setCaretPosition( "*/\n", false );
    TypeCode( eoPHP_2, "$variable_0003 = 3;\n" );

    // Check completion within first file
    eoPHP = new EditorOperator( "EmptyPHP.php" );
    eoPHP.setCaretPosition( "$variable_0002", false );
    eoPHP.deleteLine( eoPHP.getLineNumber( ) );
    eoPHP.setCaretPosition( "}", false );
    TypeCode( eoPHP, "\n$va" );
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 1000 );
    CheckResult( eoPHP, "$variable_0003" );

    endTest( );
  }

  public void Verify_code_completion_inside_the_identifier( )
  {
    startTest( );

    // Locate existing variable
    EditorOperator eoPHP = new EditorOperator( "EmptyPHP.php" );
    eoPHP.setCaretPosition( "varia", false );
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 1000 );
    String[] asIdeals = { "$variable_0003" };

    CompletionInfo jCompl = GetCompletion( );
    if( asIdeals.length != jCompl.size( ) )
      fail( "Invalid CC list size: " + jCompl.size( ) + ", expected: " + asIdeals.length );
    // Check each
    CheckCompletionItems( jCompl, asIdeals );
    jCompl.hideAll( );

    endTest( );
  }

  public void Verify_documentation_hints_for_built_in_identifiers( )
  {
    startTest( );

    EditorOperator eoPHP = new EditorOperator( "EmptyPHP.php" );
    eoPHP.setCaretPosition( "*/", false );
    //TypeCode( eoPHP, "$" );
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 5000 );
    CompletionInfo jCompl = GetCompletion( );
    Sleep( 5000 );

    Timeouts t =  jCompl.listItself.getTimeouts( );
    //t.print( System.out );
    long lBack1 = t.getTimeout( "JScrollBarOperator.OneScrollClickTimeout" );
    long lBack2 = t.getTimeout( "JScrollBarOperator.WholeScrollTimeout" );
    t.setTimeout( "JScrollBarOperator.OneScrollClickTimeout", 6000000 );
    t.setTimeout( "JScrollBarOperator.WholeScrollTimeout", 6000000 );
    jCompl.listItself.setTimeouts( t );

    System.out.println( "==== go to click on item ====" );
    jCompl.listItself.clickOnItem( "$GLOBALS", new CFulltextStringComparator( ) );
    //jCompl.listItself.pressKey( KeyEvent.VK_DOWN );
    System.out.println( "=== check done ===" );

    t.setTimeout( "JScrollBarOperator.OneScrollClickTimeout", lBack1 );
    t.setTimeout( "JScrollBarOperator.WholeScrollTimeout", lBack2 );
    jCompl.listItself.setTimeouts( t );


    //try{ Dumper.dumpAll( "c:\\dump.txt" ); } catch( IOException ex ) { }
    WindowOperator jdDoc = new WindowOperator( 1 );
    JEditorPaneOperator jeEdit = new JEditorPaneOperator( jdDoc );
    String sCompleteContent = jeEdit.getText( );
    //System.out.println( ">>>" + st + "<<<" );
    //Sleep( 5000 );
    // Check content
    String[] asContents =
    {
      "$GLOBALS",
      "Contains a reference to every variable which is currently available within",
      "the global scope of the script. The keys of this array are the names of",
       "the global variables. $GLOBALS has existed since PHP 3.",
       "<a href=\"http://www.php.net/manual/en/reserved.variables.php\">http://us2.php.net/manual/en/reserved.variables.php</a>"
    };
    for( String sContentPart : asContents )
    {
      if( -1 == sCompleteContent.indexOf( sContentPart ) )
      {
        System.out.println( ">>>" + sCompleteContent + "<<<" );
        fail( "Unable to find part of required content: \"" + sContentPart + "\"" );
      }
    }
    //jCompl.hideAll( );
    //Backit( eoPHP, 1 );

    endTest( );
  }

  public void Verify_documentation_hints_for_keywords( )
  {
    startTest( );

    EditorOperator eoPHP = new EditorOperator( "EmptyPHP.php" );
    //eoPHP.setCaretPosition( "*/", false );
    //TypeCode( eoPHP, "ext" );
    //eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    //Sleep( 5000 );
    //CompletionInfo jCompl = GetCompletion( );
    //System.out.println( "==== go to click on item ====" );
    //Sleep( 5000 );

    CompletionInfo jCompl = GetCompletion( );

    Timeouts t =  jCompl.listItself.getTimeouts( );
    //t.print( System.out );
    long lBack1 = t.getTimeout( "JScrollBarOperator.OneScrollClickTimeout" );
    long lBack2 = t.getTimeout( "JScrollBarOperator.WholeScrollTimeout" );
    t.setTimeout( "JScrollBarOperator.OneScrollClickTimeout", 60000 );
    t.setTimeout( "JScrollBarOperator.WholeScrollTimeout", 60000 );
    jCompl.listItself.setTimeouts( t );

    jCompl.listItself.clickOnItem( "extends", new CFulltextStringComparator( ) );

    t.setTimeout( "JScrollBarOperator.OneScrollClickTimeout", lBack1 );
    t.setTimeout( "JScrollBarOperator.WholeScrollTimeout", lBack2 );
    jCompl.listItself.setTimeouts( t );

    WindowOperator jdDoc = new WindowOperator( 1 );
    JEditorPaneOperator jeEdit = new JEditorPaneOperator( jdDoc );
    String sCompleteContent = jeEdit.getText( );
    // Check content
    String[] asContents =
    {
      "extends!!!"
    };
    for( String sContentPart : asContents )
    {
      if( -1 == sCompleteContent.indexOf( sContentPart ) )
      {
        System.out.println( ">>>" + sCompleteContent + "<<<" );
        // THIS IS ISSUE
        // fail( "Unable to find part of required content: \"" + sContentPart + "\"" );
      }
    }
    jCompl.hideAll( );
    //Backit( eoPHP, 3 );

    endTest( );
  }

  public void Verify_keywords_code_completion( )
  {
    startTest( );

    EditorOperator eoPHP = new EditorOperator( "EmptyPHP.php" );
    eoPHP.setCaretPosition( "?>", true );
    TypeCode( eoPHP, "class a ext" );
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 1000 );
    TypeCode( eoPHP, "\n" );
    CheckResult( eoPHP, "class a extends ", -1 );

    // Clean up
    eoPHP.deleteLine( eoPHP.getLineNumber( ) - 1 );

    endTest( );
  }

  public void Verify_code_completion_with_a_single_option( )
  {
    startTest( );

    EditorOperator eoPHP = new EditorOperator( "EmptyPHP.php" );
    eoPHP.setCaretPosition( "?>", true );
    TypeCode( eoPHP, "odbc_ge" );
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 1000 );
    TypeCode( eoPHP, "\n" );
    CheckResult( eoPHP, "odbc_gettypeinfo()", -1 );

    // Clean up
    eoPHP.deleteLine( eoPHP.getLineNumber( ) - 1 );

    endTest( );
  }

  public void Verify_JavaDoc_window( )
  {
    startTest( );

    String sJavaDoc = "This is function 1234567890...";

    EditorOperator eoPHP = new EditorOperator( "EmptyPHP.php" );
    eoPHP.setCaretPosition( "\nfunction", true );
    TypeCode( eoPHP, "\n/**\n" + sJavaDoc );
    eoPHP.setCaretPosition( "}", false );
    TypeCode( eoPHP, "\nfunction" );
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 1000 );

    CompletionInfo jCompl = GetCompletion( );
    jCompl.listItself.clickOnItem( "function_0001" );

    WindowOperator jdDoc = new WindowOperator( 1 );
    JEditorPaneOperator jeEdit = new JEditorPaneOperator( jdDoc );
    String sCompleteContent = jeEdit.getText( );
    // Check content
    if( -1 == sCompleteContent.replaceAll( "[\t\r\n ]", "" ).indexOf( sJavaDoc.replaceAll( "[\t\r\n ]", "" ) ) )
    {
      System.out.println( ">>>" + sCompleteContent + "<<<" );
      fail( "Unable to find part of required content: \"" + sJavaDoc + "\"" );
    }
    jCompl.hideAll( );
    // Clean up
    eoPHP.deleteLine( eoPHP.getLineNumber( ) );

    endTest( );
  }

  public void Verify_code_completion_after_EXTENDS( )
  {
    startTest( );

    EditorOperator eoPHP = new EditorOperator( "EmptyPHP.php" );
    eoPHP.setCaretPosition( "}", false );
    TypeCode( eoPHP, "\nclass Foo\n{\n" );
    eoPHP.setCaretPosition( "\n?>", true );
    TypeCode( eoPHP, "\nclass MyClass extends F" );
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 1000 );

    CompletionInfo jCompl = GetCompletion( );
    jCompl.listItself.clickOnItem( "Foo" );

    endTest( );
  }

  public void Verify_that_require_directive_is_automatically_added( )
  {
    startTest( );

    EditorOperator eoPHP = new EditorOperator( "EmptyPHP.php" );
    eoPHP.setCaretPosition( "require", true );
    eoPHP.deleteLine( eoPHP.getLineNumber( ) );

    EditorOperator eoPHP_2 = new EditorOperator( "EmptyPHP_2.php" );
    eoPHP_2.setCaretPosition( "\n?>", true );
    TypeCode( eoPHP_2, "function" );
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 1000 );

    CompletionInfo jCompl = GetCompletion( );
    jCompl.listItself.clickOnItem( "function_0001", 2 );
    Sleep( 1000 );

    String sText = eoPHP_2.getText( );
    if( -1 == sText.indexOf( "required" ) )
    {
      // THIS IS ISSUE
      // fail( "Require directive was not added into source file." );
    }

    endTest( );
  }

  public void Verify_code_completion_in_slash_slash_comments( )
  {
    startTest( );

    EditorOperator eoPHP_2 = new EditorOperator( "EmptyPHP_2.php" );
    TypeCode( eoPHP_2, "//" );
    eoPHP_2.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 1000 );

    CompletionJListOperator jList = new CompletionJListOperator( );
    List lm = null;
    try
    {
      lm = jList.getCompletionItems( );
    }
    catch( Exception ex )
    {
      fail( "Somehting is wrong completely, unable to get List for completion." );
    }
    Object o = lm.get( 0 );
    if( !o.toString( ).contains( "No suggestions" ) )
      fail( "Completion should not work for // comments." );

    // Cleanup
    //eoPHP_2.pressKey( KeyEvent.VK_BACK_SPACE );
    jList.hideAll( );

    endTest( );
  }

  public void Verify_code_completion_in_slash_star_comments( )
  {
    startTest( );

    EditorOperator eoPHP_2 = new EditorOperator( "EmptyPHP_2.php" );
    TypeCode( eoPHP_2, "\n/* comment    */" );
    eoPHP_2.setCaretPosition( "/* comment  ", false );
    eoPHP_2.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 1000 );


    // Check code completion list
    try
    {
      CompletionInfo completionInfo = GetCompletion( );
      if( null == completionInfo )
        fail( "NPE instead of competion info." );
      // Magic CC number for complete list
      if(
          SLASHSTAR_COMPLETION_LIST != completionInfo.listItems.size( )
        )
      {
        fail( "CC list looks to small, there are only: " + completionInfo.listItems.size( ) + " items in." );
      }

      // Check some completions
      String[] asCompletions =
      {
        "AppendIterator",
        "Countable",
        "DOMAttr",
        "PDORow",
        "ZipArchive",
        "tidy"
      };
      CheckCompletionItems( completionInfo.listItself, asCompletions );
      completionInfo.listItself.hideAll( );
    }
    catch( Exception ex )
    {
      ex.printStackTrace( System.out );
      fail( "Completion check failed: \"" + ex.getMessage( ) + "\"" );
    }

    /*
    CompletionJListOperator jList = new CompletionJListOperator( );
    List lm = null;
    try
    {
      lm = jList.getCompletionItems( );
    }
    catch( Exception ex )
    {
      fail( "Somehting is wrong completely, unable to get List for completion." );
    }
    Object o = lm.get( 0 );
    if( !o.toString( ).contains( "No suggestions" ) )
      fail( "Completion should not work for /* comments." );
    */

    // Cleanup
    //jList.hideAll( );

    endTest( );
  }

  public void Verify_code_completion_in_slash_star_star_comments( )
  {
    startTest( );

    EditorOperator eoPHP_2 = new EditorOperator( "EmptyPHP_2.php" );
    eoPHP_2.setCaretPosition( "/* comment    */", false );
    TypeCode( eoPHP_2, "\n/** comment    */" );
    eoPHP_2.setCaretPosition( "/** comment  ", false );
    eoPHP_2.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 1000 );

      CompletionInfo completionInfo = GetCompletion( );
      if( null == completionInfo )
        fail( "NPE instead of competion info." );
      // Magic CC number for complete list
      if(
          JAVADOC_COMPLETION_LIST != completionInfo.listItems.size( )
        )
      {
        fail( "CC list looks to small, there are only: " + completionInfo.listItems.size( ) + " items in." );
      }

      // Check some completions
      String[] asCompletions =
      {
        "@abstract",
        "@access",
        "@author",
        "@category",
        "@copyright",
        "@deprecated",
        "@example",
        "@filesource",
        "@final",
        "@global",
        "@ignore",
        "@internal",
        "@license",
        "@link",
        "@method",
        "@name",
        "@package",
        "@param",
        "@property",
        "@property-read",
        "@property-write",
        "@return",
        "@see",
        "@since",
        "@static",
        "@staticvar",
        "@subpackage",
        "@todo",
        "@tutorial",
        "@uses",
        "@var",
        "@version"
      };
      CheckCompletionItems( completionInfo.listItself, asCompletions );
      completionInfo.listItself.hideAll( );

    endTest( );
  }
}
