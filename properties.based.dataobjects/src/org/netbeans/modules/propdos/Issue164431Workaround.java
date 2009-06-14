/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.propdos;

import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

final class Issue164431Workaround extends ProxyLookup {

    //See http://www.netbeans.org/nonav/issues/show_bug.cgi?id=164431
    //We cannot just use InstanceContent.Converter, because the created
    //object will remain referenced - we cannot remove from the
    //AbstractLookup.  This workaround replaces the Lookup containing
    //the created one with a new Lookup using ProxyLookup.setLookups().
    //Probably much less efficient but will have to do until
    //AbstractLookup is fixed

    //Note.  Jarda closed 164431, but it appears still to be broken, hence
    //this class is still here.
    
    private InstanceContent ownContent = new InstanceContent();
    private Lookup ownLookup = new AbstractLookup(ownContent);
    private Lookup[] lookups;

    @SuppressWarnings("Unchecked")
    public Issue164431Workaround(InstanceContent.Convertor converter, Lookup... others) {
        super();
        assert converter != null;
        assert others != null;
        ownContent.add(converter, converter);
        set(others);
    }

    private void set(Lookup... lookups) {
        this.lookups = lookups;
        Lookup[] all = new Lookup[lookups.length + 1];
        System.arraycopy(lookups, 0, all, 1, lookups.length);
        synchronized (this) {
            all[0] = ownLookup;
        }
        super.setLookups(all);
    }

    @SuppressWarnings("Unchecked")
    public void replaceConverter(InstanceContent.Convertor converter) {
        assert converter != null;
        assert lookups != null;
        synchronized (this) {
            ownContent = new InstanceContent();
            ownLookup = new AbstractLookup(ownContent);
            ownContent.add(converter, converter);
        }
        set(lookups);
    }
}
