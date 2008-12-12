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

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.JemmyException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import java.util.List;
import org.netbeans.jemmy.util.Dumper;
import java.io.*;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.WindowOperator;
import org.netbeans.jemmy.Timeouts;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class cc_0002 extends cc
{
  static final String TEST_PHP_NAME = "PhpProject_cc_0002";

  public cc_0002( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( cc_0002.class ).addTest(
          "CreateApplication",
          "CreatePHPFile",
          "DetailedCodeCompletionTesting"
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

  public void CreatePHPFile( )
  {
    startTest( );

    SetAspTags( TEST_PHP_NAME, true );

    CreatePHPFile( TEST_PHP_NAME, "PHP File", null );

    endTest( );
  }

  private class CCompletionCase
  {
    public String sInitialLocation;
    public String sCode;
    public String sCompletionLocation;
    int iCompletionType;
    int iResultOffset;
    public String sResult;

    public int iCleanupOffset;
    public int iCleanupCount;

    public static final int COMPLETION_LIST = 0;
    public static final int COMPLETION_STRING = 1;

    public CCompletionCase(
        String sIL,
        String sC,
        String sCL,
        int iCT,
        int iR,
        String sR,
        int iCO,
        int iCC
      )
    {
      sInitialLocation = sIL;
      sCode = sC;
      sCompletionLocation = sCL;
      iCompletionType = iCT;
      iResultOffset = iR;
      sResult = sR;
      iCleanupOffset = iCO;
      iCleanupCount = iCC;
    }
  };


  private boolean CheckCodeCompletion( EditorOperator eoPHP, CCompletionCase cc )
  {
    // Locate position
    eoPHP.setCaretPosition( cc.sInitialLocation, false );
    // Type code
    TypeCode( eoPHP, "\n" + cc.sCode );
    // Locate completion position
    eoPHP.setCaretPosition( cc.sCompletionLocation, false );
    // Invoke completion
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 1000 );
    // Check result
    String[] asResult = cc.sResult.split( "[|]" );
    if( CCompletionCase.COMPLETION_LIST == cc.iCompletionType )
    {
      // Check completion list
      CompletionInfo completionInfo = GetCompletion( );
      if( null == completionInfo )
        fail( "NPE instead of competion info." );

      CheckCompletionItems( completionInfo.listItself, asResult );
      completionInfo.listItself.hideAll( );
    }
    else
    if( CCompletionCase.COMPLETION_STRING == cc.iCompletionType )
    {
      Sleep( 1000 );
      // Check string(s)
      for( int i = 0; i < asResult.length; i++ )
      {
        String sCode = eoPHP.getText( eoPHP.getLineNumber( ) - cc.iResultOffset + i );
        if( !sCode.matches( "^[ \t]*" + asResult[ i ] + "[ \t\r\n]*$" ) )
          return false;
          //fail( "Unable to find required string, found: \"" + sCode + "\", expected: \"" + asResult[ i ] + "\"" );
      }
    }
    else
    {
      fail( "Invalid data for code completion case." );
    }
    // Cleanup
    int iLine = eoPHP.getLineNumber( ) + cc.iCleanupOffset;
    for( int i = 0; i < cc.iCleanupCount; i++ )
      eoPHP.deleteLine( iLine );
    return true;
  }

  public void DetailedCodeCompletionTesting( )
  {
    startTest( );

    CCompletionCase[] accTests =
    {
      new CCompletionCase( "*/", "$test=1;\n$test1=$tes;", "$test1=$tes", CCompletionCase.COMPLETION_LIST, 0, "$test|$test1", -1, 2 ),
      new CCompletionCase( "*/", "$test=1;\n$test1=\"a\";\n$newvar=$tes;", "$newvar=$tes", CCompletionCase.COMPLETION_LIST, 0, "$test|$test1", -2, 3 ),
      new CCompletionCase( "*/", "$test=1;\n$newvar=$tes;\n$test1=\"a\";", "$newvar=$tes", CCompletionCase.COMPLETION_LIST, 0, "$test|$test1", -1, 3 ),
      new CCompletionCase( "*/", "$test=1;\n$test1=\"$tes\";", "$test1=\"$tes", CCompletionCase.COMPLETION_LIST, 0, "$test|$test1", -1, 2 ),
      new CCompletionCase( "*/", "$test=1;\nif ($test==1){\n$test1=\"a\";\n}\n$newvar=$tes;", "$newvar=$tes", CCompletionCase.COMPLETION_LIST, 0, "$test|$test1", -4, 6 ),
      new CCompletionCase( "*/", "$test=1;\nif ($test==1){\n$test1=\"a\";\n}\nif ($test==1){\n$newvar=$tes;\n}", "$newvar=$tes", CCompletionCase.COMPLETION_LIST, 0, "$test|$test1", -5, 8 ),
      new CCompletionCase( "*/", "$test=1;\nif ($test==1){\n$test1=\"a\";\nif ($test==1){\n$newvar=$tes;\n}\n}{{", "$newvar=$tes", CCompletionCase.COMPLETION_LIST, 0, "$test|$test1", -4, 9 ),
      new CCompletionCase( "*/", "$test=1;\n$test1=\"a\";\n// $newvar=$tes;", "$newvar=$tes", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -2, 3 ),
      new CCompletionCase( "*/", "$test=1;\n/*  $test1=\"a\";\n$newvar=$tes;", "$newvar=$tes", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -2, 4 ),
      new CCompletionCase( "*/", "$test=1;\n/**  $test1=\"a\";\n$newvar=$tes;", "$newvar=$tes", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -2, 4 ),
      new CCompletionCase( "*/", "/**  @v\n", "@v", CCompletionCase.COMPLETION_LIST, 0, "@var|@version", -1, 3 ),
      new CCompletionCase( "?>", "<html>\n<?php\n$test=1;\n?>\n$newvar=$tes\n</html>", "$newvar=$tes", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -4, 6 ),
      new CCompletionCase( "?>", "<?php\n$test=1;\n?>\nText\n<?php\n$newvar=$tes\n?>", "$newvar=$tes", CCompletionCase.COMPLETION_STRING, 0, "[$]newvar=[$]test", -5, 7 ),
      new CCompletionCase( "?>", "<?php\n$test=1;\n?>\nText\n<%\n$newvar=$tes\n%>", "$newvar=$tes", CCompletionCase.COMPLETION_STRING, 0, "[$]newvar=[$]test", -5, 7 ),
      new CCompletionCase( "?>", "<?php\n$test=1;\n?>\nText\n<?=\n$newvar=$tes\n=>", "$newvar=$tes", CCompletionCase.COMPLETION_STRING, 0, "[$]newvar=[$]test", -5, 7 ),
      new CCompletionCase( "*/", "func", "func", CCompletionCase.COMPLETION_LIST, 0, "func_get_arg|func_get_args|function_exists|function", 0, 1 ),
      new CCompletionCase( "*/", "function func(){\nret", "ret", CCompletionCase.COMPLETION_STRING, 0, "return ;", -1, 3 ),
      new CCompletionCase( "*/", "function func($param){\n$newvar=$par", "$newvar=$par", CCompletionCase.COMPLETION_STRING, 0, "[$]newvar=[$]param", -1, 3 ),
      new CCompletionCase( "*/", "function func(&$param){\n$newvar=$par", "$newvar=$par", CCompletionCase.COMPLETION_STRING, 0, "[$]newvar=[$]param", -1, 3 ),
      new CCompletionCase( "*/", "function func($param){\n$newvar=$param;\n}\n$test=$newv\n{", "$test=$newv", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -3, 6 ),
      new CCompletionCase( "*/", "function func($param){\n$newvar=$param;\n}\n$test=$par\n{", "$test=$par", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -3, 6 ),
      new CCompletionCase( "*/", "cla", "cla", CCompletionCase.COMPLETION_LIST, 0, "class_exists|class_implements|class", 0, 1 ),
      new CCompletionCase( "*/", "class MyCla", "class MyCla", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", 0, 1 ),
      new CCompletionCase( "*/", "class MyClass ext", "class MyClass ext", CCompletionCase.COMPLETION_STRING, 0, "class MyClass extends ", 0, 1 ),
      new CCompletionCase( "*/", "class MyClass extends MyCla", "class MyClass extends MyCla", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", 0, 1 ),
      new CCompletionCase( "*/", "class MyClass {\npubl", "publ", CCompletionCase.COMPLETION_STRING, 0, "public ", -1, 3 ),
      new CCompletionCase( "*/", "class MyClass {\npublic func", "public func", CCompletionCase.COMPLETION_LIST, 0, "function", -1, 3 ),
      new CCompletionCase( "*/", "class MyClass {\npublic function func", "function func", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -1, 3 ),
      new CCompletionCase( "*/", "class MyClass {\npublic function func(){\n$th\n}\n}\n{{", "$th", CCompletionCase.COMPLETION_STRING, 0, "[$]this->", -2, 8 ),
      new CCompletionCase( "*/", "class MyClass {\npublic function func(){\necho \"$th\";\n}\n}\n{{", "$th", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -2, 8 ),
      new CCompletionCase( "*/", "class MyClass {\npublic $test;\npublic function func(){\necho \"Hello\";\n}\n}\n$test=MyClass->\n{{", "MyClass->", CCompletionCase.COMPLETION_LIST, 0, "test|func", -6, 10 ),
      new CCompletionCase( "*/", "class MyClass {\nprotected $test;\nprotected function func(){\necho \"Hello\";\n}\n}\n$test=MyClass->\n{{", "MyClass->", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -6, 10 ),
      new CCompletionCase( "*/", "class MyClass {\nprivate $test;\nprivate function func(){\necho \"Hello\";\n}\n}\n$test=MyClass->\n{{", "MyClass->", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -6, 10 ),
      new CCompletionCase( "*/", "class MyClass {\npublic static $test;\npublic static function func(){\necho \"Hello\";\n}\n}\n$test=MyClass::\n{{", "MyClass::", CCompletionCase.COMPLETION_LIST, 0, "$test|func", -6, 10 ),
      new CCompletionCase( "*/", "class MyClass {\nprotected static $test;\nprotected static function func(){\necho \"Hello\";\n}\n}\n$test=MyClass::\n{{", "MyClass::", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -6, 10 ),
      new CCompletionCase( "*/", "class MyClass {\nprivate static $test;\nprivate static function func(){\necho \"Hello\";\n}\n}\n$test=MyClass::\n{{", "MyClass::", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -6, 10 )
    };

    EditorOperator eoPHP = new EditorOperator( "EmptyPHP.php" );
    String sFailed = "";
    int iFailed = 0;
    for( CCompletionCase cc : accTests )
    {
      if( !CheckCodeCompletion( eoPHP, cc ) )
      {
        iFailed++;
        sFailed = sFailed + "|" + cc.sResult;
      }
    }
    if( 0 != iFailed )
    {
      fail( "" + iFailed + " test(s) failed, invalid results: \"" + sFailed + "\"" );
    }

    endTest( );
  }
}
