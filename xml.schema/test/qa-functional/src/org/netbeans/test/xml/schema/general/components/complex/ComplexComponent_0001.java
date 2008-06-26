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

package org.netbeans.test.xml.schema.general.components.complex;

import java.awt.Point;
import javax.swing.JToggleButton;
import java.util.zip.CRC32;
import javax.swing.tree.TreePath;
import junit.framework.TestSuite;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.Property;

import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import java.io.File;
import org.netbeans.jellytools.MainWindowOperator;
import java.awt.event.KeyEvent;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;

import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class ComplexComponent_0001 extends ComplexComponent {
    
    static final String [] m_aTestMethods = {
        "OpenSchema",
        "CheckProperties",
        "CheckingIDProperty",
        "CheckingNameProperty",
        "CheckingAbstractProperty",
        "CheckingMixedContentProperty",
        "CheckingDerivationsProperty",
        "CheckingSubstitutionsProperty",
      };

    String sPathInTree = "Complex Types|ComplexType-0";

    String[] asProperties =
    {
      "Kind|Global Complex Type",
      "ID|| ",
      "Name|ComplexType-0",
      "Structure|Click to customize...",
      "Abstract|False (not set)",
      "Mixed Content|False (not set)",
      "Prohibited Derivations (Final)|| ",
      "Prohibited Substitutions (Block)|| ",
    };

    //String[] asCorrectIDValues = { "qwerty", "asdfg" };
    //String[] asIncorrectIDValues = { "12345" };

    //String[] asCorrectMxOValues = { "*|unbounded", "5", "2" };
    //String[] asIncorrectMxOValues = { "-5" };

    //String[] asCorrectMnOValues = { "5", "0" };
    //String[] asIncorrectMnOValues = { "-5" };

    String[] asCorrectProcessValues = { "Lax", "Skip", "Strict" };
    //String[] asIncorrectProcessValues = { "-5" };

    String[] asCorrectNamespaceValues = { "Local", "Target Namespace", "Other", "Any" };
    //String[] asIncorrectNamespaceValues = { "-5" };

    public ComplexComponent_0001(String arg0) {
        super(arg0);
    }

    /*    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(ComplexComponent_0001.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new ComplexComponent_0001(strMethodName));
        }
        
        return testSuite;
    }
    */

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( ComplexComponent_0001.class ).addTest(
              "OpenSchema",
              "CheckProperties",
              "CheckingIDProperty",
              "CheckingNameProperty",
              "CheckingAbstractProperty",
              "CheckingMixedContentProperty",
              "CheckingDerivationsProperty",
              "CheckingSubstitutionsProperty"
           )
           .enableModules( ".*" )
           .clusters( ".*" )
           //.gui( true )
        );
    }

    public void OpenSchema( )
    {
      startTest( );

      OpenSchemaInternal( sPathInTree );

      endTest( );
    }

    public void CheckProperties( )
    {
      startTest( );

      CheckPropertiesInternal( asProperties );

      endTest( );
    }

    public void CheckingIDProperty( )
    {
      startTest( );

      String[] data =
      {
        "<xsd:complexType name=\"ComplexType-0\">~<xsd:complexType name=\"ComplexType-0\" id=\"qwerty\">~" + sPathInTree + "~ID|qwerty",
        " id=\"qwerty\"~ id=\"12345\"~" + sPathInTree + "~ID|12345",
        " id=\"12345\"~~" + sPathInTree + "~ID|| ",
      };

      CheckProperty( data );

      endTest( );
    }

    public void CheckingNameProperty( )
    {
      startTest( );

      String[] data =
      {
        "<xsd:complexType name=\"ComplexType-0\">~<xsd:complexType name=\"ComplexType-012345\">~Complex Types|ComplexType-012345~Name|ComplexType-012345",
        "<xsd:complexType name=\"ComplexType-012345\">~<xsd:complexType name=\"12345\">~Complex Types|12345~Name|12345",
        "<xsd:complexType name=\"12345\">~<xsd:complexType name=\"ComplexType-0\">~" + sPathInTree + "~Name|ComplexType-0",
      };

      CheckProperty( data );

      endTest( );
    }

    public void CheckingAbstractProperty( )
    {
      startTest( );

      CheckBooleanProperty(
          "complexType",
          "ComplexType-0",
          "abstract",
          "Abstract",
          sPathInTree
        );

      /*
      String[] data =
      {
        "<xsd:complexType name=\"ComplexType-0\">~<xsd:complexType name=\"ComplexType-0\" abstract=\"true\">~" + sPathInTree + "~Abstract|True",
        //" abstract=\"true\"~ abstract=\"true12345\"~" + sPathInTree + "~Abstract|True12345",
        " abstract=\"true\"~ abstract=\"false\"~" + sPathInTree + "~Abstract|False",
        //" abstract=\"true12345\"~ abstract=\"false\"~" + sPathInTree + "~Abstract|False",
        " abstract=\"false\"~~" + sPathInTree + "~Abstract|False (not set)",
      };

      CheckProperty( data );
      */

      endTest( );
    }

    public void CheckingMixedContentProperty( )
    {
      startTest( );

      CheckBooleanProperty(
          "complexType",
          "ComplexType-0",
          "mixed",
          "Mixed Content",
          sPathInTree
        );

      /*
      String[] data =
      {
        "<xsd:complexType name=\"ComplexType-0\">~<xsd:complexType name=\"ComplexType-0\" mixed=\"true\">~" + sPathInTree + "~Mixed Content|True",
        //" mixed=\"true\"~ mixed=\"true12345\"~" + sPathInTree + "~Mixed Content|True12345",
        " mixed=\"true\"~ mixed=\"false\"~" + sPathInTree + "~Mixed Content|False",
        //" mixed=\"true12345\"~ mixed=\"false\"~" + sPathInTree + "~Mixed Content|False",
        " mixed=\"false\"~~" + sPathInTree + "~Mixed Content|False (not set)",
      };

      CheckProperty( data );
      */

      endTest( );
    }

    public void CheckingDerivationsProperty( )
    {
      startTest( );

      String[] data =
      {
        "<xsd:complexType name=\"ComplexType-0\">~<xsd:complexType name=\"ComplexType-0\" final=\"#all\">~" + sPathInTree + "~Prohibited Derivations (Final)|#all",
      };
      CheckProperty( data );

      CheckClicked(
          "Global Complex Type - Prohibited Derivations (Final)",
          "Prevent all type derivations (#all)",
          7,
          1
        );

      String[] data1 =
      {
        " final=\"#all\">~ final=\"restriction extension\">~" + sPathInTree + "~Prohibited Derivations (Final)|extension restriction",
      };
      CheckProperty( data1 );

      CheckClicked(
          "Global Complex Type - Prohibited Derivations (Final)",
          "Prevent type derivations of the following kinds:|Extension|Restriction",
          7,
          1
        );

      String[] data2 =
      {
        " final=\"restriction extension\"~~" + sPathInTree + "~Prohibited Derivations (Final)|| ",
      };
      CheckProperty( data2 );

      endTest( );
    }

    public void CheckingSubstitutionsProperty( )
    {
      startTest( );

      String[] data =
      {
        "<xsd:complexType name=\"ComplexType-0\">~<xsd:complexType name=\"ComplexType-0\" block=\"#all\">~" + sPathInTree + "~Prohibited Substitutions (Block)|#all",
      };
      CheckProperty( data );

      CheckClicked(
          "Global Complex Type - Prohibited Substitutions (Block)",
          "Block all substitutions (#all)",
          8,
          1
        );

      String[] data1 =
      {
        " block=\"#all\">~ block=\"restriction substitution extension\">~" + sPathInTree + "~Prohibited Substitutions (Block)|substitution restriction extension",
      };
      CheckProperty( data1 );

      CheckClicked(
          "Global Complex Type - Prohibited Substitutions (Block)",
          "Block substitutions of the following kinds:|Extension|Restriction|Substitution",
          8,
          1
        );

      String[] data2 =
      {
        " block=\"restriction substitution extension\"~~" + sPathInTree + "~Prohibited Substitutions (Block)|| ",
      };
      CheckProperty( data2 );

      endTest( );
    }

}
