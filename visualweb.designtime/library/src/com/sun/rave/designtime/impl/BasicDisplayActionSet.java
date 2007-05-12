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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
