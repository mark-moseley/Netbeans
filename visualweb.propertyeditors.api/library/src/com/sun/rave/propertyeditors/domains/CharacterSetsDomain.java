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
package com.sun.rave.propertyeditors.domains;

/**
 * Editable domain of character set names, as specified by the IANA. Only the more
 * commonly used character sets are provided by default, but the user may add
 * more. No explicit support is provided for charset aliases, but these may be
 * added as new elements. The default names are given in the IANA preferred form.
 * Edits of this domain are available Project-wide.
 */
// TODO - When DesignContext.getProject().getGlobalData() fixed, make this domain IDE-scoped
public class CharacterSetsDomain extends EditableDomain {

    public CharacterSetsDomain() {
        super(EditableDomain.PROJECT_STORAGE, String.class);
        this.elements.add( new Element("ISO-8859-1"));
        this.elements.add( new Element("US-ASCII"));
        this.elements.add( new Element("UTF-8"));
        this.elements.add( new Element("SHIFT_JIS"));
        this.elements.add( new Element("EUC-JP"));
        this.elements.add( new Element("EUC-KR"));
        this.elements.add( new Element("GB2312"));
        this.elements.add( new Element("Big5"));
    }

    public String getDisplayName() {
        return bundle.getMessage("CharacterSets.displayName");
    }

}
