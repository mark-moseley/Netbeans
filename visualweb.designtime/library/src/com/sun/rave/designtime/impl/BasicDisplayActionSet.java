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

package com.sun.rave.designtime.impl;

import java.util.ArrayList;
import java.awt.Image;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.DisplayActionSet;

/**
 * A basic implementation of DisplayActionSet to use for convenience.
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see DisplayActionSet
 */
public class BasicDisplayActionSet extends BasicDisplayAction implements DisplayActionSet {

    protected boolean popup = false;
    protected ArrayList actionList = new ArrayList();

    public BasicDisplayActionSet() {
        super();
    }

    public BasicDisplayActionSet(String displayName) {
        super(displayName);
    }

    public BasicDisplayActionSet(String displayName, String description) {
        super(displayName, description);
    }

    public BasicDisplayActionSet(String displayName, String description, String helpKey) {
        super(displayName, description, helpKey);
    }

    public BasicDisplayActionSet(String displayName, String description, String helpKey,
        Image smallIcon) {
        super(displayName, description, helpKey, smallIcon);
    }

    public BasicDisplayActionSet(String displayName, String description, String helpKey,
        Image smallIcon, Image largeIcon) {
        super(displayName, description, helpKey, smallIcon, largeIcon);
    }

    public int getDisplayActionCount() {
        return actionList.size();
    }

    public DisplayAction getDisplayAction(int index) {
        return (DisplayAction)actionList.get(index);
    }

    public DisplayAction[] getDisplayActions() {
        return (DisplayAction[])actionList.toArray(new DisplayAction[actionList.size()]);
    }

    public void addDisplayAction(DisplayAction action) {
        actionList.add(action);
    }

    public void addDisplayAction(int index, DisplayAction action) {
        actionList.add(index, action);
    }

    public void removeDisplayAction(DisplayAction action) {
        actionList.remove(action);
    }

    public void removeDisplayAction(int index) {
        actionList.remove(index);
    }

    public void setPopup(boolean popup) {
        this.popup = popup;
    }

    public boolean isPopup() {
        return popup;
    }
}
