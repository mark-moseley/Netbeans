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
package com.sun.jsfcl.std.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ReferenceDataItem implements Comparable {

    protected ReferenceDataItem aliasFor;
    protected boolean isUnsetMarker;
    protected boolean isRemovable;
    protected String name;
    protected Object value;
    protected String javaInitializationString;

    public static ArrayList sorted(List items) {
        ArrayList result;

        result = new ArrayList(items.size());
        result.addAll(items);
        Collections.sort(result);
        return result;
    }

    protected ReferenceDataItem(String name, Object value, String javaInitializationString,
        boolean isUnsetMarker, boolean isRemovable, ReferenceDataItem aliasFor) {

        super();
        this.name = name;
        this.value = value;
        this.javaInitializationString = javaInitializationString;
        this.isUnsetMarker = isUnsetMarker;
        this.isRemovable = isRemovable;
        this.aliasFor = aliasFor;
    }

    public int compareTo(Object object) {
        ReferenceDataItem otherItem;

        otherItem = (ReferenceDataItem)object;
        return getName().compareToIgnoreCase(otherItem.getName());
    }

    public boolean equals(Object object) {

        if (object instanceof ReferenceDataItem) {
            return equals((ReferenceDataItem)object);
        }
        return false;
    }

    public boolean equals(ReferenceDataItem other) {

        if (value != null && other.value != null) {
            if (value.equals(other.value)) {
                return true;
            }
        }
        return false;
    }

    public ReferenceDataItem getAliasFor() {

        return aliasFor;
    }

    public String getDisplayString() {

        if (value instanceof String) {
            return (String)value;
        }
        return name;
    }

    public String getJavaInitializationString() {

        return javaInitializationString;
    }

    public String getName() {

        return name;
    }

    public Object getValue() {

        return value;
    }

    public int hashCode() {

        if (value == null) {
            return 0;
        }
        return value.hashCode();
    }

    public boolean isRemovable() {

        return isRemovable;
    }

    public boolean isUnsetMarker() {

        return isUnsetMarker;
    }

    public boolean matchesPattern(Pattern pattern) {

        Matcher matcher = pattern.matcher(getName());
        if (matcher.matches()) {
            return true;
        }
        if (getValue() instanceof String) {
            matcher = pattern.matcher((String)getValue());
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    public void setIsRemovable(boolean isRemovable) {

        this.isRemovable = isRemovable;
    }

    public void setIsUnsetMarker(boolean isUnsetValue) {

        this.isUnsetMarker = isUnsetValue;
    }

    public void setJavaInitializationString(String string) {

        javaInitializationString = string;
    }

}
