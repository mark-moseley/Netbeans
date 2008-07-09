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

package org.netbeans.test.xml.schema.general;

import java.awt.Point;
import java.util.zip.CRC32;
import javax.swing.tree.TreePath;
import junit.framework.TestSuite;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jemmy.JemmyException;
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
//import org.netbeans.test.xml.schema.lib.SchemaMultiView;
//import org.netbeans.test.xml.schema.lib.util.Helpers;
import javax.swing.ListModel;

import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import java.io.File;
import org.netbeans.jellytools.MainWindowOperator;
import java.awt.event.KeyEvent;
//import java.awt.Robot;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import java.util.List;
import javax.swing.JEditorPane;
import java.awt.Rectangle;
import javax.swing.text.BadLocationException;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class GeneralXMLTest extends JellyTestCase {
    
    static final String JAVA_CATEGORY_NAME = "Java";
    static final String JAVA_PROJECT_NAME = "Java Application";

    public class CFulltextStringComparator implements Operator.StringComparator
    {
      public boolean equals( java.lang.String caption, java.lang.String match )
      {
        return caption.equals( match );
      }
    }

    public class CStartsStringComparator implements Operator.StringComparator
    {
      public boolean equals( java.lang.String caption, java.lang.String match )
      {
        return caption.startsWith( match );
      }
    }

    public class CImportClickData
    {
      public boolean inshort;
      public int row;
      public int col;
      public int count;
      public int result;
      public String error;
      public String checker;
      
      public CImportClickData(
          boolean _inshort,
          int _row,
          int _col,
          int _count,
          int _result,
          String _error,
          String _checker
        )
      {
        inshort = _inshort;
        row = _row;
        col = _col;
        count = _count;
        result = _result;
        error = _error;
        checker = _checker;
      }
    }

    public GeneralXMLTest( String arg0 )
    {
      super( arg0 );
    }

    public void Dummy( )
    {
      startTest( );
      System.out.println( "=== DUMMY ===" );
      endTest( );
    }

    protected void CreateSimpleProjectInternal(
        String sCategory,
        String sProject,
        String sName
      )
    {
      // Create Java application
      NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke( );
      opNewProjectWizard.selectCategory( sCategory );
      opNewProjectWizard.selectProject( sProject );
      opNewProjectWizard.next( );

      NewProjectNameLocationStepOperator opNewProjectNameLocationStep = new NewProjectNameLocationStepOperator( );
      opNewProjectNameLocationStep.txtProjectLocation( ).setText( GetWorkDir( ) );
      opNewProjectNameLocationStep.txtProjectName( ).setText( sName );
      opNewProjectWizard.finish( );
    }

    protected void CreateJavaApplicationInternal(
        String sName
      )
    {
      CreateSimpleProjectInternal(
          JAVA_CATEGORY_NAME,
          JAVA_PROJECT_NAME,
          sName
        );

        //org.netbeans.junit.ide.ProjectSupport.waitScanFinished( );
    }

    protected void AddSampleSchemaInternal(
        String sProject,
        String sPackage,
        String sSchema,
        String sSchemaName
      )
    {
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode( sProject );
      prn.select( );

      NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke( );
      opNewFileWizard.selectCategory( "XML" );
      opNewFileWizard.selectFileType( sSchema );
      opNewFileWizard.next( );

      if( null != sSchemaName )
      {
        JDialogOperator jdNew = new JDialogOperator( "New " + sSchema );
        JTextComponentOperator jt = new JTextComponentOperator( jdNew, 0 );
        jt.setText( sSchemaName );
      }

      if( null != sPackage )
      {
        JDialogOperator jdNew = new JDialogOperator( "New " + sSchema );
        JButtonOperator jbBrowse = new JButtonOperator( jdNew, "Browse..." );
        jbBrowse.pushNoBlock( );
        JDialogOperator jdBrowse = new JDialogOperator( "Browse Folders" );
        JTreeOperator jtBrowse = new JTreeOperator( jdBrowse, 0 );
        jtBrowse.selectPath( jtBrowse.findPath( sProject + "|src|" + sPackage ) );
        JButtonOperator jbSelect = new JButtonOperator( jdBrowse, "Select Folder" );
        jbSelect.push( );
        jdBrowse.waitClosed( );
      }

      opNewFileWizard.finish( );

      // Check created schema in project tree
      String sPath = sProject + "|Source Packages|"
          + ( ( null == sPackage ) ? "<default package>" : sPackage )
          + "|" + sSchemaName + ".xsd";
      prn = pto.getProjectRootNode( sProject );
      prn.select( );
    }

    protected void AddLoanApplicationSchemaInternal(
        String sProject,
        String sPackage
      )
    {
      AddSampleSchemaInternal(
          sProject,
          sPackage,
          "Loan Application Sample Schema",
          "newLoanApplication"
        );
    }

    protected void AddPurchaseOrderSchemaInternal(
        String sProject,
        String sPackage
      )
    {
      AddSampleSchemaInternal(
          sProject,
          sPackage,
          "Purchase Order Sample Schema",
          "newPurchaseOrder"
        );
    }

    protected void ExpandByClicks(
        JTableOperator table,
        int row,
        int col,
        int count,
        int result,
        String error
      )
    {
      // Normal version
      // just click
      table.clickOnCell( row, col, count );
      table.pushKey( KeyEvent.VK_RIGHT );

      // HaCk version
      /*
      Point pt = table.getPointToClick( row, col );
      //table.enterMouse( );
      //try { Thread.sleep( 50 ); } catch( InterruptedException ex ) { }
      table.pressMouse( pt.x, pt.y );
      try { Thread.sleep( 50 ); } catch( InterruptedException ex ) { }
      table.releaseMouse( pt.x, pt.y );
      try { Thread.sleep( 50 ); } catch( InterruptedException ex ) { }
      table.pressMouse( pt.x, pt.y );
      try { Thread.sleep( 50 ); } catch( InterruptedException ex ) { }
      table.releaseMouse( pt.x, pt.y );
      */

      try { Thread.sleep( 750 ); } catch( InterruptedException ex ) { }
      int iRows = table.getRowCount( );
      if( result != iRows )
        fail( error + iRows );

      return;
    }

    public void CreateConstrainedInternal(
        String sApplication,
        CImportClickData[] aimpData,
        String sRoot,
        int iRoot,
        int iPrimary
      )
    {
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode( sApplication + "|Source Packages|" + sApplication );
      prn.select( );
      
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("File|New File...");

      // JDialogOperator jdNew = new JDialogOperator( "New File" );
      NewFileWizardOperator fwNew = new NewFileWizardOperator( "New File" );
      fwNew.selectCategory( "XML" );
      fwNew.selectFileType( "XML Document" );
      fwNew.next( );

      fwNew.next( );

      JDialogOperator jnew = new JDialogOperator( "New File" );
      JRadioButtonOperator jbut = new JRadioButtonOperator( jnew, "XML Schema-Constrained Document" );
      jbut.setSelected( true );
      jbut.clickMouse( );
      fwNew.next( );

      // === PAGE ===
      jnew = new JDialogOperator( "New File" );
      JButtonOperator jBrowse = new JButtonOperator( jnew, "Browse" );
      jBrowse.pushNoBlock( );

      JDialogOperator jBrowser = new JDialogOperator( "Schema Browser" );
      JTableOperator jto = new JTableOperator( jBrowser, 0 );

      for( CImportClickData cli : aimpData )
      {
        try { Thread.sleep( 1000 ); } catch( InterruptedException ex ) { }
        ExpandByClicks( jto, cli.row, cli.col, cli.count, cli.result, cli.error );
      }

      JButtonOperator jOk = new JButtonOperator( jBrowser, "OK" );
      jOk.push( );
      jBrowser.waitClosed( );

      JTableOperator jtable = new JTableOperator( jnew, 0 );

      jtable.clickOnCell( iRoot, 2, 1 );
      JComboBoxOperator jcom = new JComboBoxOperator( jnew, 0 );
      jcom.selectItem( sRoot );

      jnew = new JDialogOperator( "New File" );
      jtable.clickOnCell( iPrimary, 0, 1 );

      fwNew.next( );

      fwNew.finish( );

      prn = pto.getProjectRootNode(
          sApplication + "|Source Packages|" + sApplication + "|newXMLDocument.xml"
        );
      prn.select( );
     
    }

    public void SimpleGenerateInternal(
        String sApplication,
        String sBaseName,
        String sAction,
        String sNewExtension
      )
    {
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode(
          sApplication + "|Source Packages|" + sApplication + "|" + sBaseName
        );
      prn.select( );

      prn.performPopupActionNoBlock( sAction );

      JDialogOperator jdNew = new JDialogOperator( "Select File Name" );
      JTextComponentOperator jtText = new JTextComponentOperator( jdNew, 0 );
      String sName = jtText.getText( );
      JButtonOperator jbOk = new JButtonOperator( jdNew, "OK" );
      jbOk.push( );
      jdNew.waitClosed( );

      prn = pto.getProjectRootNode(
          sApplication + "|Source Packages|" + sApplication + "|" + sName + "." + sNewExtension
        );
      prn.select( );
    }

    public void CreateSchemaInternal(
        String sProject,
        String sName,
        String sNamespace
      )
    {
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode( sProject );
      prn.select( );

      NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke( );
      opNewFileWizard.selectCategory( "XML" );
      opNewFileWizard.selectFileType( "XML Schema" );
      opNewFileWizard.next( );

      if( null != sName )
      {
        JDialogOperator jdNew = new JDialogOperator( "New XML Schema" );
        JTextComponentOperator jtText = new JTextComponentOperator( jdNew, 0 );
        jtText.setText( sName );
      }
      else
        sName = "newXmlSchema";

      if( null != sNamespace )
      {
        JDialogOperator jdNew = new JDialogOperator( "New XML Schema" );
        JTextComponentOperator jtText = new JTextComponentOperator( jdNew, 4 );
        jtText.setText( sNamespace );
      }

      opNewFileWizard.finish( );

      // Check created schema in project tree
      if( null == ( prn = pto.getProjectRootNode( sProject + "|" + sName + ".xsd" ) ) )
      {
        fail( "Unable to check created schema." );
      }
      prn.select( );
    }

    public void CreateSchemaInternal( String sProject, String sName )
    {
      CreateSchemaInternal( sProject, sName, null );
    }

    public void CreateSchemaInternal( String sProject )
    {
      CreateSchemaInternal( sProject, null );
    }

    public void SelectInFirstColumn(
        SchemaMultiView xml,
        String name
      )
    {
      SelectItemInColumn( xml, 0, name );
    }

    public void SelectItemInColumn(
        SchemaMultiView xml,
        int iColumn,
        String name
      )
    {
      JListOperator list = xml.getColumnListOperator( iColumn );
      int iIndex = list.findItemIndex( name, new CStartsStringComparator( ) );
      if( -1 == iIndex )
        fail( "Unable to select \"" + name + "\" in first column." );
      list.selectItem( iIndex );
    }

    public void CheckNoItemInColumn(
        SchemaMultiView xml,
        int iColumn,
        String name
      )
    {
      JListOperator list = xml.getColumnListOperator( iColumn );
      int iIndex = list.findItemIndex( name, new CStartsStringComparator( ) );
      if( -1 != iIndex )
        fail( "Item \"" + name + "\" still exists in column." );
    }

    protected void ClickForTextPopup( EditorOperator eo )
    {
      JEditorPaneOperator txt = eo.txtEditorPane( );
      JEditorPane epane =  ( JEditorPane )txt.getSource( );
      try
      {
        Rectangle rct = epane.modelToView( epane.getCaretPosition( ) );
        txt.clickForPopup( rct.x, rct.y );
      }
      catch( BadLocationException ex )
      {
        System.out.println( "=== Bad location" );
      }

      return;
    }

    protected void ClickForTextPopup( EditorOperator eo, String menu )
    {
      JEditorPaneOperator txt = eo.txtEditorPane( );
      JEditorPane epane =  ( JEditorPane )txt.getSource( );
      try
      {
        Rectangle rct = epane.modelToView( epane.getCaretPosition( ) );
        txt.clickForPopup( rct.x, rct.y );
        JPopupMenuOperator popup = new JPopupMenuOperator( );
        popup.pushMenu( menu );
      }
      catch( BadLocationException ex )
      {
        System.out.println( "=== Bad location" );
      }

      return;
    }

    protected void ClickForTextPopupNoBlock( EditorOperator eo, String menu )
    {
      JEditorPaneOperator txt = eo.txtEditorPane( );
      JEditorPane epane =  ( JEditorPane )txt.getSource( );
      try
      {
        Rectangle rct = epane.modelToView( epane.getCaretPosition( ) );
        txt.clickForPopup( rct.x, rct.y );
        JPopupMenuOperator popup = new JPopupMenuOperator( );
        popup.pushMenuNoBlock( menu );
      }
      catch( BadLocationException ex )
      {
        System.out.println( "=== Bad location" );
      }

      return;
    }

    protected String Dump( SchemaMultiView xml, int iLevel, String sCurrent )
    {
      String sTemp = sCurrent;
      // Select each list element
      JListOperator list = xml.getColumnListOperator( iLevel );
      if( null != list )
      {
        ListModel lm = list.getModel( );
        int iCount = lm.getSize( );
        // Click on each list element
        for( int i = 0; i < iCount; i++ )
        {
          sTemp = sTemp + " " + iLevel + ":" + lm.getElementAt( i );
          //list.selectItem( i );
          list.clickOnItem( i, 1 );
          try { Thread.sleep( 300 ); } catch( InterruptedException ex ) { }
          // Call deeper
          sTemp = Dump( xml, iLevel + 1, sTemp );
        }
      }
      return sTemp;
    }

    protected String Dump(
        JTreeOperator tree,
        Object node,
        int iDeep,
        String sCurrent
      )
    {
      String sTemp = sCurrent + " " + ( ( -1 == iDeep ) ? 0 : iDeep ) + ":" + node;
      //System.out.println( " " + ( ( -1 == iDeep ) ? 0 : iDeep ) + ":" + node );
      int iCount = tree.getChildCount( node );
      for( int i = 0; i < iCount; i++ )
      {
        Object o = tree.getChild( node, i );
        sTemp = Dump( tree, o, iDeep + 1, sTemp );
      }
      return sTemp;
    }

  protected void Sleep( long lms )
  {
    try
    {
      Thread.sleep( lms );
    }
    catch( InterruptedException ex )
    {
      System.err.println( "Interrupted: " + ex.getMessage( ) );
    }
  }

  protected void CheckOutputLines( String sOutputTitle, String[] asIdeals )
  {
    OutputTabOperator oto = new OutputTabOperator( sOutputTitle );
    oto.waitText( asIdeals[ asIdeals.length - 1 ] );
    int iCount = oto.getLineCount( );

    for( int i = 0; i < iCount; i++ )
      System.out.println( ">>>" + oto.getText( i, i ) + "<<<" );

    String sLast = oto.getLine( iCount - 1 );
    if( sLast.endsWith( "\r" ) || sLast.endsWith( "\n" ) )
      iCount--;
    if( asIdeals.length != iCount )
      fail( "Wrong number of output lines: " + iCount );
    for( int i = 0; i < asIdeals.length; i++ )
    {
      String sText = oto.getText( i, i );
      if( -1 == sText.indexOf( asIdeals[ i ] ) )
        fail( "Unable to find required text in output: " + asIdeals[ i ] + "; found: " + sText );
    }
  }

  protected String GetWorkDir( )
  {
    // return System.getProperty( "xtest.workdir" ); // XTest
    return getDataDir( ).getPath( );
    //return System.getProperty( "nbjunit.workdir" ) + File.separator + ".." + File.separator + "data"; // SimpleTest
  }

    public void CallPopupOnListItem(
        SchemaMultiView xml,
        int iList,
        String sItem,
        String sMenu
      )
    {
      CallPopupOnListItem(
          xml,
          iList,
          sItem,
          new CFulltextStringComparator( ),
          sMenu
        );
   }

    public void CallPopupOnListItem(
        SchemaMultiView xml,
        int iList,
        String sItem,
        Operator.StringComparator cmp,
        String sMenu
      )
    {
      JListOperator list = xml.getColumnListOperator( iList );
      int iIndex = list.findItemIndex( sItem, cmp );
      if( -1 == iIndex )
        fail( "Unable to call popup for item " + sItem );
      list.selectItem( iIndex );
      //int iIndex = list.findItemIndex( sItem );
      Point pt = list.getClickPoint( iIndex );
      list.clickForPopup( pt.x, pt.y );
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenu( sMenu );
   }

    public void CallPopupOnListItemNoBlock(
        SchemaMultiView xml,
        int iList,
        String sItem,
        String sMenu
      )
    {
      CallPopupOnListItemNoBlock(
          xml,
          iList, 
          sItem,
          new CStartsStringComparator( ),
          sMenu
        );
   }

    public void CallPopupOnListItemNoBlock(
        SchemaMultiView xml,
        int iList,
        String sItem,
        Operator.StringComparator cmp,
        String sMenu
      )
    {
      JListOperator list = xml.getColumnListOperator( iList );
      int iIndex = list.findItemIndex( sItem, cmp );
      if( -1 == iIndex )
        fail( "Unable to call popup for item " + sItem );
      list.selectItem( iIndex );
      //int iIndex = list.findItemIndex( sItem );
      Point pt = list.getClickPoint( iIndex );
      list.clickForPopup( pt.x, pt.y );
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenuNoBlock( sMenu );
   }

   // The purpose of this function is to avoid strange issue
   // which appear after few calls to Refactor submenu items:
   // state stalls and doesn't reflect actual situation. Open
   // then close Refactor submenu -- helps.
   // TODO : reallife version should check success of forst
   // attempt or use check instead of attempt first time.
   protected void CallRefactorSubmenu( String name )
   {
      JMenuBarOperator jm = new JMenuBarOperator(MainWindowOperator.getDefault());

      jm = new JMenuBarOperator(MainWindowOperator.getDefault());
      try
      {
        jm.pushMenu("Refactor|" + name );
      }
      catch( JemmyException ex )
      {
      }
      jm.closeSubmenus( );
      jm.pushMenuNoBlock("Refactor|" + name );
   }
}
