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

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class cc_0005 extends cc
{
  static final String TEST_PHP_NAME = "PhpProject5";

  public cc_0005( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( cc_0005.class ).addTest(
          "CreateApplication",
          "Issue141880"
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

  public void Issue141880( ) throws Exception
  {
    startTest( );

    // Get editor
    EditorOperator eoPHP = new EditorOperator( "index.php" );
    Sleep( 1000 );
    // Locate comment
    eoPHP.setCaretPosition( "// put your code here", false );
    // Add new line
    eoPHP.insert( "\n$aaa = \"Hello\";\n" );
    Sleep( 1000 );

    // Check constructor
    String sCode = "$bbb = \"$a";
    for( int i = 0; i < sCode.length( ); i++ )
    {
      // Press Ctrl+Space
      eoPHP.typeKey( sCode.charAt( i ) );
      Sleep( 1000 );
    }
    eoPHP.typeKey( ' ', InputEvent.CTRL_MASK );
    Sleep( 1000 );

    // Check code completion list

    String[] asIdeals = { "aaa", "argc", "argv" };

    CompletionJListOperator jCompl = GetCompletion( );
    List list = jCompl.getCompletionItems( );
    // Magic CC number for complete list
    if( asIdeals.length != list.size( ) )
      fail( "Invalid CC list size: " + list.size( ) + ", expected: " + asIdeals.length );
    // Check each
    CheckCompletionItems( jCompl, asIdeals );

    jCompl.hideAll( );

    endTest( );
  }
}
