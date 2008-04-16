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

package org.netbeans.test.xml.cpr;

import junit.framework.TestSuite;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;

import org.netbeans.api.project.ProjectInformation;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class AcceptanceTestCaseBPEL2JAVAAPP extends AcceptanceTestCaseXMLCPR {
    
    static final String [] m_aTestMethods = {
        "CreateBluePrint1Sample",
        "CreateJAVAAPPModule",
        "AddProjectReference",
        "DeleteProjectReference",
        "AddSampleSchema",
        "ImportReferencedSchema",
        "ImportReferencedSchema2",
        "DeleteReferencedSchema",
        "FindUsages", // TODO : How to find find usages output?
        "ValidateAndBuild",
        "AddAttribute",
        "ExploreAttribute",
        "ManipulateAttribute",
        "AddComplex",
        "ExploreComplex",
        "DeleteComplex",
        "UndoRedoComplex",
        "AddElement",
        "ExploreElement",
        "DeleteElement",
        "UndoRedoElement",
        "AddSimple",
        "ExploreSimple",
        "DeleteSimple",
        "UndoRedoSimple",
        "RenameSampleSchema",
        "UndoRenameSampleSchema",
        "RedoRenameSampleSchema",
        "ValidateAndBuild",

        // Move, fix

        "ValidateAndBuild",
        "BuildCompositeApplication",
        //"DeployCompositeApplication", // This will failed, followed skipped
        //"CreateNewTest",
        //"RunNewTest",
    };

    static final String SAMPLE_CATEGORY_NAME = "Samples|SOA|BPEL BluePrints";
    static final String SAMPLE_PROJECT_NAME = "BluePrint 1";
    static final String SAMPLE_NAME = "SampleApplication2Java";
    static final String COMPOSITE_APPLICATION_NAME = SAMPLE_NAME + "Application";

    static final String MODULE_CATEGORY_NAME = "Java";
    static final String MODULE_PROJECT_NAME = "Java Application";
    static final String MODULE_NAME = "JavaApplication";

    static final String SAMPLE_SCHEMA_PATH = "Source Packages|<default package>";

    public AcceptanceTestCaseBPEL2JAVAAPP(String arg0) {
        super(arg0);
    }
    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(AcceptanceTestCaseBPEL2JAVAAPP.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new AcceptanceTestCaseBPEL2JAVAAPP(strMethodName));
        }
        
        return testSuite;
    }
    
    public void CreateBluePrint1Sample( )
    {
        startTest( );

        CreateBluePrint1SampleInternal(
            SAMPLE_CATEGORY_NAME,
            SAMPLE_PROJECT_NAME,
            SAMPLE_NAME
          );

        endTest( );
    }
    
    public void CreateJAVAAPPModule( )
    {
        startTest( );

        // Create BluePrint1 Sample
        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke( );
        opNewProjectWizard.selectCategory( MODULE_CATEGORY_NAME );
        opNewProjectWizard.selectProject( MODULE_PROJECT_NAME );
        opNewProjectWizard.next( );

        NewProjectNameLocationStepOperator opNewProjectNameLocationStep = new NewProjectNameLocationStepOperator( );
        opNewProjectNameLocationStep.txtProjectLocation( ).setText( System.getProperty( "xtest.ide.open.projects" ) );
        opNewProjectNameLocationStep.txtProjectName( ).setText( MODULE_NAME );
        opNewProjectWizard.finish( );

        endTest( );
    }

    public void AddProjectReference( )
    {
      startTest( );

      AddProjectReferenceInternal( SAMPLE_NAME, MODULE_NAME );

      endTest( );
    }
    
    public void DeleteProjectReference( )
    {
      startTest( );

      DeleteProjectReferenceInternal( SAMPLE_NAME, MODULE_NAME );

      AddProjectReferenceInternal( SAMPLE_NAME, MODULE_NAME );

      endTest( );
    }

    public void AddSampleSchema( )
    {
      startTest( );
      
      AddSampleSchemaInternal( MODULE_NAME, SAMPLE_SCHEMA_PATH );

      endTest( );
    }

    private CImportClickData[] acliImport =
    {
      new CImportClickData( true, 0, 0, 2, 4, "Unknown import table state after first click, number of rows: ", null ),
      new CImportClickData( true, 1, 0, 2, 5, "Unknown import table state after second click, number of rows: ", null ),
      new CImportClickData( true, 2, 0, 2, 7, "Unknown import table state after third click, number of rows: ", null ),
      new CImportClickData( true, 5, 0, 2, 9, "Unknown import table state after forth click, number of rows: ", null ),
      new CImportClickData( true, 6, 0, 2, 11, "Unknown import table state after fifth click, number of rows: ", null ),
      new CImportClickData( true, 7, 0, 2, 12, "Unknown import table state after sixth click, number of rows: ", null ),
      new CImportClickData( false, 3, 1, 1, 12, "Unknown to click on checkbox. #", null ),
      new CImportClickData( true, 8, 1, 1, 12, "Unknown to click on checkbox. #", null )
    };

    private CImportClickData[] acliCheck =
    {
      new CImportClickData( true, 0, 0, 2, 4, "Unknown import table state after first click, number of rows: ", null ),
      new CImportClickData( true, 1, 0, 2, 5, "Unknown import table state after second click, number of rows: ", null ),
      new CImportClickData( true, 2, 0, 2, 7, "Unknown import table state after third click, number of rows: ", null ),
      new CImportClickData( true, 5, 0, 2, 9, "Unknown import table state after forth click, number of rows: ", null ),
      new CImportClickData( true, 6, 0, 2, 11, "Unknown import table state after fifth click, number of rows: ", null ),
      new CImportClickData( true, 7, 0, 2, 12, "Unknown import table state after sixth click, number of rows: ", null ),
      new CImportClickData( true, 3, 1, 1, 12, "Unknown to click on checkbox. #", "Selected document is already referenced." ),
      new CImportClickData( true, 4, 1, 1, 12, "Unknown to click on checkbox. #", "Document cannot reference itself." ),
      new CImportClickData( true, 8, 1, 1, 12, "Unknown to click on checkbox. #", "Selected document is already referenced." )
    };

    public void ImportReferencedSchema( )
    {
      startTest( );
      
      ImportReferencedSchemaInternal(
          SAMPLE_NAME,
          MODULE_NAME,
          false,
          acliImport
        );

      endTest( );
    }

    public void ImportReferencedSchema2( )
    {
      startTest( );

      ImportReferencedSchema2Internal( acliCheck );
      
      endTest( );
    }

    public void DeleteReferencedSchema( )
    {
      startTest( );

      DeleteReferencedSchemaInternal( MODULE_NAME );

      ImportReferencedSchemaInternal(
          SAMPLE_NAME,
          MODULE_NAME,
          true,
          acliImport
        );

      endTest( );
    }

    public void FindUsages( )
    {
      startTest( );

      FindUsagesInternal( MODULE_NAME, SAMPLE_SCHEMA_PATH );

      endTest( );
    }

    public void ValidateAndBuild( )
    {
      startTest( );
      
      ValidateAndBuildInternal( SAMPLE_NAME );

      endTest( );
    }

    public void AddAttribute( )
    {
      startTest( );

      AddItInternal(
          "Attributes",
          "Add Attribute",
          null, 
          "Referenced Schemas|import|Simple Types|StateType",
          ATTRIBUTES_NAMES[ 0 ]
        );

      endTest( );
    }

    public void ExploreAttribute( )
    {
      startTest( );

      ExploreSimpleInternal(
          ATTRIBUTES_NAMES[ 0 ],
          "StateType",
          "attribute",
          "ns2:"
        );

      endTest( );
    }

    public void ManipulateAttribute( )
    {
      startTest( );

      ManipulateAttributeInternal( SAMPLE_NAME );

      endTest( );
    }

    public void AddComplex( )
    {
      startTest( );

      AddItInternal(
          "Complex Types",
          "Add Complex Type",
          "Use Existing Definition", 
          "Referenced Schemas|import|Complex Types|CarType",
          COMPLEX_NAMES[ 0 ]
        );

      endTest( );
    }

    public void ExploreComplex( )
    {
      startTest( );



      endTest( );
    }

    public void DeleteComplex( )
    {
      startTest( );



      endTest( );
    }

    public void UndoRedoComplex( )
    {
      startTest( );



      endTest( );
    }

    public void AddElement( )
    {
      startTest( );

      AddItInternal(
          "Elements",
          "Add Element",
          "Use Existing Type", 
          "Referenced Schemas|import|Complex Types|AddressType",
          "newElement"
        );

      endTest( );
    }

    public void ExploreElement( )
    {
      startTest( );



      endTest( );
    }

    public void DeleteElement( )
    {
      startTest( );



      endTest( );
    }

    public void UndoRedoElement( )
    {
      startTest( );



      endTest( );
    }

    public void AddSimple( )
    {
      startTest( );

      AddItInternal(
          "Simple Types",
          "Add Simple Type",
          null, 
          "Referenced Schemas|import|Simple Types|LoanType",
          SIMPLE_NAMES[ 0 ]
        );

      endTest( );
    }

    public void ExploreSimple( )
    {
      startTest( );

      ExploreSimpleInternal(
          SIMPLE_NAMES[ 0 ],
          "LoanType",
          "simpleType",
          null
        );

      endTest( );
    }

    public void DeleteSimple( )
    {
      startTest( );



      endTest( );
    }

    public void UndoRedoSimple( )
    {
      startTest( );



      endTest( );
    }

    public void RenameSampleSchema( )
    {
      startTest( );

      RenameSampleSchemaInternal( MODULE_NAME, SAMPLE_SCHEMA_PATH );

      endTest( );
    }

    public void UndoRenameSampleSchema( )
    {
      startTest( );

      UndoRenameSampleSchemaInternal( MODULE_NAME, SAMPLE_SCHEMA_PATH );

      endTest( );
    }

    public void RedoRenameSampleSchema( )
    {
      startTest( );

      RedoRenameSampleSchemaInternal( MODULE_NAME, SAMPLE_SCHEMA_PATH );

      endTest( );
    }

    public void BuildCompositeApplication( )
    {
      startTest( );
      
      BuildCompositeApplicationInternal( COMPOSITE_APPLICATION_NAME );

      endTest( );
    }

    public void DeployCompositeApplication( )
    {
      startTest( );

      DeployCompositeApplicationInternal( COMPOSITE_APPLICATION_NAME );
      
      endTest( );
    }

    public void CreateNewTest( )
    {
      startTest( );



      endTest( );
    }

    public void RunNewTest( )
    {
      startTest( );



      endTest( );
    }

}
