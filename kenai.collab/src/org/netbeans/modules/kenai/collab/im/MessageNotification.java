/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.collab.im;

import java.util.LinkedList;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;
import org.netbeans.modules.notifications.spi.Notification;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author Jan Becicka
 */
class MessageNotification extends Notification{

    private LinkedList<Message> messageQueue = new LinkedList<Message>();

    @Override
    public String getLinkTitle() {
        return "read";
    }

    @Override
    public String getTitle() {
        return "<b>New Private Message</b>";
    }

    @Override
    public String getDescription() {
        final Message top = messageQueue.getFirst();
        String from= StringUtils.parseName(top.getFrom());
        return "<i>"+from + " says: </i>" + top.getBody();
    }

    @Override
    public void showDetails() {
        JEditorPane pane = new JEditorPane();
        pane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(pane);
        DialogDescriptor newMessages = new DialogDescriptor(scrollPane, "New Messages ");
        StringBuffer b = new StringBuffer();
        for (Message m : messageQueue) {
            b.append(StringUtils.parseName(m.getFrom()) + " says: \n");
            b.append(m.getBody() + "\n");
        }
        pane.setText(b.toString());
        DialogDisplayer.getDefault().createDialog(newMessages).setVisible(true);
        clear();
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    void addMessage(Message msg) {
        messageQueue.add(msg);
    }

    Message removeMessage() {
        return messageQueue.removeFirst();
    }

    boolean isEmpty() {
        return messageQueue.isEmpty();
    }

    void clear() {
        this.remove();
        messageQueue.clear();
    }
}
