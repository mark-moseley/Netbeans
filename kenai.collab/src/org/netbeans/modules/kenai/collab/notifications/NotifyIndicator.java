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
package org.netbeans.modules.kenai.collab.notifications;


import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.SortedSet;
import javax.swing.JLabel;
import org.netbeans.modules.notifications.spi.Notification;
import org.openide.util.RequestProcessor;

/**
 * Lower right icon to show Notifications
 * @see Notification
 * @see Notifications
 * @author Jan Becicka
 */

public class NotifyIndicator implements Runnable {
    private static NotifyIndicator instance;

    private JLabel label;
    private MouseL helper;

    public void update() {
        Notification top = APIAccessor.DEFAULT.top();
        label.setIcon(top!=null?top.getIcon():null);
    }

    Component getComponent() {
        return label;
    }
    public static synchronized NotifyIndicator getDefault() {
        if (instance == null) {
            instance = new NotifyIndicator();
            //Just for domo
            RequestProcessor.getDefault().post(instance);
        }
        return instance;
    }

    public void run() {
        new BuildFailedNotification().add();
        new NewCodeReviewNotification().add();
        new TestFailedNotification().add();
    }
    
    private NotifyIndicator() {
        helper = new MouseL();
        label = new JLabel();
        label.addMouseListener(helper);
    }
    
    
    private class MouseL extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
        NotificationPanel p = new NotificationPanel();
        final SortedSet<Notification> nlist = APIAccessor.DEFAULT.getNotifications();
        synchronized (nlist) {
            Iterator<Notification> i = nlist.iterator();
            while (i.hasNext())
                p.addNotification(i.next());
        }

        BalloonManager.show(label, p, null, 2000);
        }
    }
}

