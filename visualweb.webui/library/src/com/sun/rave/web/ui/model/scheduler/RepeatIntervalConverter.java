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
package com.sun.rave.web.ui.model.scheduler;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import com.sun.rave.web.ui.model.scheduler.RepeatInterval;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.ThemeUtilities;

import javax.faces.context.FacesContext;

// Delete the setters once you have reimplemented this not to
// use the default Serializable mechanism, but the same as
// in the converter....

public class RepeatIntervalConverter implements Converter, Serializable {

    public String getAsString(FacesContext context, UIComponent component,
            Object value) {
        if(value == null) {
            return null;
        }
        return ((RepeatInterval)value).getRepresentation();
    }
    
    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {
        return RepeatInterval.getInstance(value);      
    }
}

