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

package org.netbeans.upgrade.systemoptions;

import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * For: org.netbeans.modules.tasklist.docscan.TaskTags
 * @author Radek Matous
 */
class TaskTagsProcessor extends PropertyProcessor {
    
    /** Creates a new instance of TaskTagsProcessor */
    TaskTagsProcessor() {
        super("org.netbeans.modules.tasklist.docscan.TaskTags");//NOI18N
    }
    
    void processPropertyImpl(String propertyName, Object value) {
        if ("taskTags".equals(propertyName)) {//NOI18N
            List l = ((SerParser.ObjectWrapper)value).data;
            for (Iterator it = l.iterator(); it.hasNext();) {
                Object elem = (Object) it.next();
                if (elem instanceof SerParser.ObjectWrapper) {
                    String clsname = Utils.prettify(((SerParser.ObjectWrapper)elem).classdesc.name);
                    if ("org.netbeans.modules.tasklist.docscan.TaskTag".equals(clsname)) {//NOI18N
                        processTag(elem);//NOI18N
                    }
                }
            }
        }  else {
            throw new IllegalStateException();
        }
    }
    
    private void processTag(final Object value) {
        String tagName = null;
        List l = ((SerParser.ObjectWrapper)value).data;
        for (Iterator it = l.iterator(); it.hasNext();) {
            Object elem = (Object) it.next();
            if (elem instanceof SerParser.ObjectWrapper) {
                String val = ((SerParser.NameValue)(((SerParser.ObjectWrapper)elem).data.get(0))).value.toString();
                assert tagName != null;
                addProperty(tagName, val);
            } else if (elem instanceof String) {
                tagName = "Tag"+(String)elem;//NOI18N
            }
        }
    }
    
}
