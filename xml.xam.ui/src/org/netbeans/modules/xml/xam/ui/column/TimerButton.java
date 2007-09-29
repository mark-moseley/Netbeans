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

package org.netbeans.modules.xml.xam.ui.column;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.Timer;

/**
 * A convenience button class which will continue re-firing its action
 * on a timer for as long as the button is depressed. Used for left-right
 * scroll buttons.
 */
public class TimerButton extends JButton implements ActionListener {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    private Timer timer;
    private Image disabledImage;
    private Image enabledImage;
    private int count;

    public TimerButton(Action a) {
        super(a);
    }

    private Timer getTimer() {
        if (timer == null) {
            timer = new Timer(400, this);
            timer.setRepeats(true);
        }
        return timer;
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        count++;
        if (count > 5) {
            timer.setDelay(75);
        } else if (count > 2) {
            timer.setDelay(200);
        }
        performAction();
    }

    private void performAction() {
        if (!isEnabled()) {
            stopTimer();
            return;
        }
        getAction().actionPerformed(new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED, getActionCommand()));
    }

    private void startTimer() {
        performAction();
        Timer t = getTimer();
        if (t.isRunning()) {
            return;
        }
        repaint();
        t.setDelay(400);
        t.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
        repaint();
        count = 0;
    }

    protected void processMouseEvent(MouseEvent me) {
        if (isEnabled() && me.getID() == me.MOUSE_PRESSED) {
            startTimer();
        } else if (me.getID() == me.MOUSE_RELEASED) {
            stopTimer();
        } else {
            super.processMouseEvent(me);
        }
    }

    protected void processFocusEvent(FocusEvent fe) {
        super.processFocusEvent(fe);
        if (fe.getID() == fe.FOCUS_LOST) {
            stopTimer();
        }
    }
}
