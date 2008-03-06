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

package org.netbeans.modules.editor.hints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.editor.hints.HintsControllerImpl.CompoundLazyFixList;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;

/**
 *
 * @author Jan Lahoda
 */
public class FixData extends CompoundLazyFixList {

    public FixData(LazyFixList errorFixes, LazyFixList otherFixes) {
        super(Arrays.asList(errorFixes, otherFixes));
    }

    public List<Fix> getSortedFixes() {
        LazyFixList errorFixes = delegates.get(0);
        LazyFixList otherFixes = delegates.get(1);
        List<Fix> result = new LinkedList<Fix>();
        
        result.addAll(sortFixes(new LinkedHashSet<Fix>(errorFixes.getFixes())));
        result.addAll(sortFixes(new LinkedHashSet<Fix>(otherFixes.getFixes())));
        
        return result;
    }

    private List<Fix> sortFixes(Collection<Fix> fixes) {
        List<EnhancedFix> sortableFixes = new ArrayList<EnhancedFix>();
        List<Fix> other = new LinkedList<Fix>();

        for (Fix f : fixes) {
            if (f instanceof EnhancedFix) {
                sortableFixes.add((EnhancedFix) f);
            } else {
                other.add(f);
            }
        }

        Collections.sort(sortableFixes, new FixComparator());

        List<Fix> result = new ArrayList<Fix>();

        result.addAll(sortableFixes);
        result.addAll(other);

        return result;
    }
    private static final class FixComparator implements Comparator<EnhancedFix> {
        public int compare(EnhancedFix o1, EnhancedFix o2) {
            return compareText(o1.getSortText(), o2.getSortText());
        }
    }

    private static int compareText(CharSequence text1, CharSequence text2) {
        int len = Math.min(text1.length(), text2.length());
        for (int i = 0; i < len; i++) {
            char ch1 = text1.charAt(i);
            char ch2 = text2.charAt(i);
            if (ch1 != ch2) {
                return ch1 - ch2;
            }
        }
        return text1.length() - text2.length();
    }
    
}
