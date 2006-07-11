/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui.actions;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.KeyStroke;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class ProjectActionTest extends NbTestCase {
    
    public ProjectActionTest(String name) {
        super( name );
    }

    private FileObject p1;
    private FileObject p2;
    private FileObject f1_1; 
    private FileObject f1_2; 
    private FileObject f2_1;
    private FileObject f2_2;
    private DataObject d1_1; 
    private DataObject d1_2;
    private DataObject d2_1;
    private DataObject d2_2;
    private TestSupport.TestProject project1;
    private TestSupport.TestProject project2;

    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(TestSupport.TestProjectFactory.class);
        clearWorkDir();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
    
        
        p1 = TestSupport.createTestProject( workDir, "project1" );
        f1_1 = p1.createData("f1_1.java");
        f1_2 = p1.createData("f1_2.krava");
        d1_1 = DataObject.find(f1_1);
        d1_2 = DataObject.find(f1_2);
               
        project1 = (TestSupport.TestProject)ProjectManager.getDefault().findProject( p1 );
        project1.setLookup( Lookups.fixed( new Object[] { new TestActionProvider() } ) );  
        
        p2 = TestSupport.createTestProject( workDir, "project2" );
        f2_1 = p2.createData("f2_1.java");
        f2_2 = p2.createData("f2_2.krava");
        d2_1 = DataObject.find(f2_1);
        d2_2 = DataObject.find(f2_2);
               
        project2 = (TestSupport.TestProject)ProjectManager.getDefault().findProject( p2 );                
    }
    
    protected void tearDown() throws Exception {
        clearWorkDir();
        super.tearDown();
    }
    
    public boolean runInEQ () {
        return true;
    }
    
    public void testCommandEnablement() {
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup();
        ProjectAction action = new ProjectAction( "COMMAND", "TestProjectAction", null, lookup );
        
        assertFalse( "Action should NOT be enabled", action.isEnabled() );        
        
        lookup.change(d1_1);
        assertTrue( "Action should be enabled", action.isEnabled() );        
        
        lookup.change(d1_1, d1_2);
        assertFalse( "Action should NOT be enabled", action.isEnabled() );        
        
        lookup.change(d1_1, d2_1);
        assertFalse( "Action should NOT be enabled", action.isEnabled() );        
        
    }
    
    public void testProviderEnablement() {
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup();
        TestActionPerformer tap = new TestActionPerformer();
        ProjectAction action = new ProjectAction( tap, "TestProjectAction", null,lookup );
     
        assertFalse( "Action should NOT be enabled", action.isEnabled() );                
        assertEquals( "enable() should NOT be called: ", 0, tap.enableCount );
        
        tap.clear();
        tap.on( true );
        assertFalse( "Action should NOT be enabled", action.isEnabled() );
        assertEquals( "enable() should NOT be called: ", 0, tap.enableCount );
        
        tap.clear();
        tap.on( false );
        lookup.change(d1_1);
        assertFalse( "Action should NOT be enabled", action.isEnabled() );
        assertEquals( "enable() should be called once: ", 1, tap.enableCount );
        assertEquals( "enable() should be called on right project: ", project1.toString(), tap.project.toString() );
        
        tap.clear();
        tap.on( true );
        lookup.change(d1_2);
        assertTrue( "Action should be enabled", action.isEnabled() );        
        assertEquals( "enable() should be called once: ", 1, tap.enableCount );
        assertEquals( "enable() should be called on right project: ", project1.toString(), tap.project.toString() );
                
        tap.clear();
        tap.on( false );
        lookup.change(d1_1, d2_1);
        assertFalse( "Action should NOT be enabled", action.isEnabled() );
        assertEquals( "enable() should NOT be called: ", 0, tap.enableCount );
        
    }
    
    public void testAcceleratorsPropagated() {
        doTestAcceleratorsPropagated(new ActionCreator() {
            public ProjectAction create(Lookup l) {
                return new ProjectAction("command", "TestProjectAction", null, l);
            }
        }, true);
    }
    
    public static void doTestAcceleratorsPropagated(ActionCreator creator, boolean testMenus) {
        Lookup l1 = Lookups.fixed(new Object[] {"1"});
        Lookup l2 = Lookups.fixed(new Object[] {"2"});
        
        ProjectAction a1 = creator.create(l1);
        
        KeyStroke k1 = KeyStroke.getKeyStroke("shift pressed A");
        KeyStroke k2 = KeyStroke.getKeyStroke("shift pressed A");
        
        assertNotNull(k1);
        assertNotNull(k2);
        
        a1.putValue(Action.ACCELERATOR_KEY, k1);
        
        ProjectAction a2 = creator.create(l2);
        
        assertEquals(k1, a2.getValue(Action.ACCELERATOR_KEY));
        
        a2.putValue(Action.ACCELERATOR_KEY, k2);
        
        assertEquals(k2, a1.getValue(Action.ACCELERATOR_KEY));
        
        if (testMenus) {
            assertEquals(k2, a2.getMenuPresenter().getAccelerator());
        }

        a1.putValue(Action.ACCELERATOR_KEY, k1);
        assertEquals(k1, a2.getValue(Action.ACCELERATOR_KEY));
        
        if (testMenus) {
            assertEquals(k1, a2.getMenuPresenter().getAccelerator());
        }
    }
    
    public static interface ActionCreator {
        public ProjectAction create(Lookup l);
    }
    
    private static class TestActionProvider implements ActionProvider {
        
        public String COMMAND = "COMMAND";
        
        private String[] ACTIONS = new String[] { COMMAND };
        
        private List<String> invocations = new ArrayList<String>();
        
        public String[] getSupportedActions() {
            return ACTIONS;
        }
                
        public void invokeAction( String command, Lookup context ) throws IllegalArgumentException {
            
            if ( COMMAND.equals( command ) ) {
                invocations.add( command );
            }            
            else {
                throw new IllegalArgumentException();
            }
            
        }

        public boolean isActionEnabled( String command, Lookup context) throws IllegalArgumentException {
            
            if ( COMMAND.equals( command ) ) {
                for (DataObject dobj : context.lookupAll(DataObject.class)) {
                    if ( !dobj.getPrimaryFile().getNameExt().endsWith( ".java" ) ) {
                        return false;
                    }                    
                }
                return true;
            }            
            else {
                throw new IllegalArgumentException();
            }
            
        }

        
    }
    
    private static class TestActionPerformer implements ProjectActionPerformer {
        
        private int enableCount;
        private Project project;
        private boolean on;
        
        public boolean enable( Project project ) {
            enableCount ++;
            this.project = project;
            return on;
        }

        public void perform( Project project ) {
            this.project = project;
        }
        
        public void on( boolean on ) {
            this.on = on;
        }
        
        public void clear() {
            this.project = null;
            enableCount = 0;
        }
        
        
    }
        
}
