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

package org.openide.awt;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.Status;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * Simulates deadlock as in issue 163201. Folder Instance Processor is blocked
 * and AWT needlessly waits for it to finish.
 *
 * 
    Thread AWT-EventQueue-0
        at java.lang.Object.wait(Object.java:-2)
        at org.openide.util.Task.waitFinished(Task.java:158)
        at org.openide.util.RequestProcessor$Task.waitFinished(RequestProcessor.java:799)
        at org.openide.util.Task.waitFinished(Task.java:192)
        at org.openide.loaders.FolderInstance.waitFinished(FolderInstance.java:339)
        at org.openide.awt.MenuBar$LazyMenu$MenuFolder.waitFinishedSuper(MenuBar.java:623)
        at org.openide.awt.MenuBar$LazyMenu.doInitialize(MenuBar.java:581)
        at org.openide.awt.MenuBar$LazyMenu.stateChanged(MenuBar.java:555)
        at javax.swing.DefaultButtonModel.fireStateChanged(DefaultButtonModel.java:333)
        at javax.swing.DefaultButtonModel.setMnemonic(DefaultButtonModel.java:274)
        at javax.swing.AbstractButton.setMnemonic(AbstractButton.java:1548)
        at org.openide.awt.Mnemonics.setMnemonic(Mnemonics.java:279)
        at org.openide.awt.Mnemonics.setLocalizedText2(Mnemonics.java:84)
        at org.openide.awt.Mnemonics.setLocalizedText(Mnemonics.java:137)
        at org.openide.awt.MenuBar$LazyMenu.updateProps(MenuBar.java:512)
        at org.openide.awt.MenuBar$LazyMenu.run(MenuBar.java:524)
        at java.awt.event.InvocationEvent.dispatch(InvocationEvent.java:209)
        at java.awt.EventQueue.dispatchEvent(EventQueue.java:597)
        at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:273)
        at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:183)
        at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:173)
        at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:168)
        at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:160)
        at java.awt.EventDispatchThread.run(EventDispatchThread.java:121)
  Thread Folder Instance Processor
        at java.lang.Object.wait(Object.java:-2)
        at java.lang.Object.wait(Object.java:485)
        at org.openide.awt.MenuBarDeadlock163201Test$BlockingAction.<init>(MenuBarDeadlock163201Test.java:209)
        at java.lang.reflect.Constructor.newInstance(Constructor.java:513)
        at java.lang.Class.newInstance0(Class.java:355)
        at java.lang.Class.newInstance(Class.java:308)
        at org.openide.loaders.InstanceSupport.instanceCreate(InstanceSupport.java:217)
        at org.openide.loaders.InstanceDataObject$Ser.instanceCreate(InstanceDataObject.java:1298)
        at org.openide.loaders.InstanceDataObject.instanceCreate(InstanceDataObject.java:760)
        at org.openide.loaders.FolderInstance.instanceForCookie(FolderInstance.java:572)
        at org.openide.loaders.FolderInstance$HoldInstance.instanceCreate(FolderInstance.java:1122)
        at org.openide.loaders.FolderInstance$1R.instances(FolderInstance.java:692)
        at org.openide.loaders.FolderInstance$1R.run(FolderInstance.java:713)
        at org.openide.util.RequestProcessor$Task.run(RequestProcessor.java:573)
        at org.openide.util.RequestProcessor$Processor.run(RequestProcessor.java:1005)
 *
 * @author Jaroslav Tulach
 */
public class MenuBarDeadlock163201Test extends NbTestCase {
    private DataFolder df;
    private MenuBar mb;
    private DataFolder df2;
    private MFS mfs;

    public MenuBarDeadlock163201Test(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        return false;
    }

    @Override
    protected int timeOut() {
        return 15000;
    }

    @Override
    protected void setUp() throws Exception {
        mfs = new MFS();
        FileObject fo = FileUtil.createFolder(
            mfs.getRoot(),
            "Folder" + getName() + "/Old"
        );
        FileObject fo2 = FileUtil.createFolder(
            mfs.getRoot(),
            "Folder2" + getName() + "/Old"
        );
        df = DataFolder.findFolder(fo.getParent());
        df2 = DataFolder.findFolder(fo2.getParent());
        mb = new MenuBar(df);
        mb.waitFinished();
        assertEquals("One submenu", 1, mb.getMenuCount());
        assertEquals("Named Old", "Old", mb.getMenu(0).getText());
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testChangeInNameOfFolderDoesNotDeadlock() throws Throwable {
        class R implements Runnable {
            MenuBar mb2;
            Throwable t;
            private DataFolder ch;

            public void run() {
                try {
                    FileUtil.createData(df.getPrimaryFile(), "some.change");

                    ch = (DataFolder)df2.getChildren()[0];
                    InstanceDataObject.create(ch, null, BlockingAction.class);
                    mb2 = new MenuBar(df2);
                    mb2.waitFinished();
                    assertEquals("One menu", 1, mb2.getMenuCount());
                    // MenuBar.LazyMenu
                    ChangeListener l = (ChangeListener) mb2.getMenu(0);
                    l.stateChanged(new ChangeEvent(this));
                    assertEquals("One action", 1, mb2.getMenu(0).getMenuComponentCount());
                } catch (Throwable ex) {
                    this.t = ex;
                }
            }
        }

        R run = new R();
        Task t = RequestProcessor.getDefault().post(run);
        t.waitFinished(1000);
        assertTrue("Blocking action created", BlockingAction.created);
        if (run.t != null) {
            throw run.t;
        }

        class Rename implements Runnable, NodeListener {
            private boolean ok;
            public void run() {
                mfs.startMorph();
            }

            public void childrenAdded(NodeMemberEvent ev) {
            }

            public void childrenRemoved(NodeMemberEvent ev) {
            }

            public void childrenReordered(NodeReorderEvent ev) {
            }

            public void nodeDestroyed(NodeEvent ev) {
            }

            public synchronized void propertyChange(PropertyChangeEvent evt) {
                if (Node.PROP_DISPLAY_NAME.equals(evt.getPropertyName())) {
                    ok = true;
                    notifyAll();
                }
            }

            public synchronized void waitOK() throws InterruptedException {
                while (!ok) {
                    wait();
                }
            }
        }
        Node node = df.getChildren()[0].getNodeDelegate();
        Rename name = new Rename();
        node.addNodeListener(name);
        EventQueue.invokeAndWait(name);
        name.waitOK();
        assertEquals("Node name changed", "New", node.getDisplayName());

        
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
            }
        });

        assertEquals("One submenu", 1, mb.getMenuCount());
        assertEquals("Named New", "New", mb.getMenu(0).getText());
    }

    public static final class BlockingAction extends AbstractAction {
        private static boolean created;
        public BlockingAction() {
            created = true;
            synchronized (this) {
                for (;;) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
        }
    }

    private static final class MFS extends MultiFileSystem implements FileSystem.Status {
        private boolean morph;

        public MFS() {
            setDelegates(FileUtil.createMemoryFileSystem());
        }


        public String annotateName(String name, Set<? extends FileObject> files) {
            if (morph && name.equals("Old")) {
                return "New";
            }
            return name;
        }

        public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
            return icon;
        }

        public void startMorph() {
            morph = true;
            fireFileStatusChanged(new FileStatusEvent(this, false, true));
        }

        @Override
        public Status getStatus() {
            return this;
        }
    }
}
