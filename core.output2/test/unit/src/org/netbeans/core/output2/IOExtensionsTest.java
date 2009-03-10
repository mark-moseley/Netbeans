/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.core.output2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.core.output2.ui.AbstractOutputPane;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColors;
import org.openide.windows.IOContainer;
import org.openide.windows.IOPosition;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author Tomas Holy
 */
public class IOExtensionsTest extends NbTestCase {

    public IOExtensionsTest(String name) {
        super(name);
    }
    private IOContainer iowin;
    private NbIO io;
    private AbstractOutputPane pane;
    private JFrame jf = null;
    static int testNum;

    @Override
    protected void setUp() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                iowin = IOContainer.getDefault();
                JComponent wnd = LifecycleTest.getIOWindow();
                jf = new JFrame();
                jf.getContentPane().setLayout(new BorderLayout());
                jf.getContentPane().add(wnd, BorderLayout.CENTER);
                jf.setBounds(20, 20, 700, 300);
                jf.setVisible(true);
                io = (NbIO) new NbIOProvider().getIO("Test" + testNum++, false);
                pane = ((OutputTab) iowin.getSelected()).getOutputPane();
            }
        });
    }

    @Override
    protected void tearDown() throws Exception {
        io.closeInputOutput();
        jf.dispose();
    }

    public void testSetDefColors() throws IOException {
        IOColors.setColor(io, IOColors.OutputType.OUTPUT, Color.GRAY);
        IOColors.setColor(io, IOColors.OutputType.ERROR, Color.PINK);
        IOColors.setColor(io, IOColors.OutputType.HYPERLINK, Color.MAGENTA);
        IOColors.setColor(io, IOColors.OutputType.HYPERLINK_IMPORTANT, Color.GREEN);
        io.getOut().println("Test out");
        io.getErr().println("Test err");
        io.getOut().println("Test hyperlink", new L(), false);
        io.getOut().println("Test important hyperlink", new L(), true);
        IOColors.setColor(io, IOColors.OutputType.OUTPUT, Color.BLACK);
        IOColors.setColor(io, IOColors.OutputType.ERROR, Color.RED);
        IOColors.setColor(io, IOColors.OutputType.HYPERLINK, Color.BLUE);
        IOColors.setColor(io, IOColors.OutputType.HYPERLINK_IMPORTANT, Color.MAGENTA);
    }

    public void testVertScroll() {
        for (int i = 0; i < 10; i++) {
            io.getOut().println("Test line " + i);
        }
        IOPosition.Position pos = IOPosition.currentPosition(io);
        assertNotNull("IOPosition should be supported", pos);
        for (int i = 0; i < 50; i++) {
            io.getOut().println("Another test line " + i);
        }

        waitEq();
        pos.scrollTo();
        waitEq();
        int line = pane.getCaretLine();
        assertEquals(10, line);
    }

    public void testHorzScroll() {
        for (int i = 0; i < 10; i++) {
            io.getOut().print("Test part" + i + ". ");
        }
        IOPosition.Position pos = IOPosition.currentPosition(io);
        assertNotNull("IOPosition should be supported", pos);
        for (int i = 0; i < 10; i++) {
            io.getOut().print("Another test " + i + ". ");
        }
        for (int i = 0; i < 50; i++) {
            io.getOut().println("New line " + i);
        }

        waitEq();
        pos.scrollTo();
        waitEq();
        int p = pane.getCaretPos();
        assertEquals(120, p);
    }

    public void testWrappedScroll() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                ((OutputTab) iowin.getSelected()).getOutputPane().setWrapped(true);
            }
        });

        for (int i = 0; i < 100; i++) {
            io.getOut().print("Test part" + i + ". ");
        }

        IOPosition.Position pos = IOPosition.currentPosition(io);
        assertNotNull("IOPosition should be supported", pos);

        for (int k = 0; k < 5; k++) {
            io.getOut().println();
            for (int i = 0; i < 100; i++) {
                io.getOut().print("Another part " + k + "_" + i + ". ");
            }
        }

        waitEq();
        pos.scrollTo();
        waitEq();
        int p = pane.getCaretPos();
        assertEquals(1290, p);
    }

    public void testColorPrinting() throws IOException {
        for (int i = 0; i < 11; i++) {
            int c = 55 + i * 20;
            IOColorLines.println(io, "Test line " + i, new Color(c, 0, 0));
            IOColorLines.println(io, "Test line " + i, new Color(0, c, 0));
            IOColorLines.println(io, "Test line " + i, new Color(0, 0, c));
        }
        IOColorLines.println(io, "Line with listener", new L(), false, new Color(120, 120, 0));
        IOColorLines.println(io, "          Line with listener", new L(), false, new Color(120, 120, 0));
        IOColorLines.println(io, "Another line with importnat listener", new L(), true, new Color(0, 100, 255));

        for (int i = 0; i < 50; i++) {
            io.getOut().println(i);
        }

        StringBuilder longLine = new StringBuilder("Long line ...");
        for (int i = 0; i < 100; i++) {
            longLine.append("...test " + i + " ...");
        }
        IOColorLines.println(io, longLine, new Color(255, 0, 0));
        IOColorLines.println(io, longLine, new L(), false, new Color(255, 0, 0));
    }

    public class L implements OutputListener {

        public void outputLineSelected(OutputEvent ev) {
        }

        public void outputLineAction(OutputEvent ev) {
        }

        public void outputLineCleared(OutputEvent ev) {
        }
    }

    private void waitEq() {
        try {
            Thread.sleep(500);
            while (IOEvent.pendingCount > 0) {
                Thread.sleep(10);
            }
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                }
            });
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
