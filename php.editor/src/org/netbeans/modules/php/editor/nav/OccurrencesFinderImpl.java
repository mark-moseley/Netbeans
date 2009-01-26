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

package org.netbeans.modules.php.editor.nav;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.php.editor.model.CodeMarker;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelFactory;
import org.netbeans.modules.php.editor.model.Occurence;
import org.netbeans.modules.php.editor.model.OccurencesSupport;
import org.netbeans.modules.php.editor.model.PhpKind;

/**
 *
 * @author Radek Matous
 */
public class OccurrencesFinderImpl extends OccurrencesFinder {
    private int offset;
    private Map<OffsetRange, ColoringAttributes> range2Attribs;

    public void setCaretPosition(int position) {
        this.offset = position;
        this.range2Attribs = new HashMap<OffsetRange, ColoringAttributes>();
    }

    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return range2Attribs;
    }

    public void cancel() {
        //TODO: implement me 
    }
    
    public void run(Result result, SchedulerEvent event) {
        for (OffsetRange r : compute((ParserResult) result, GsfUtilities.getLastKnownCaretOffset(result.getSnapshot(), event))) {
            range2Attribs.put(r, ColoringAttributes.MARK_OCCURRENCES);
        }
    }
    
    static Collection<OffsetRange> compute(final ParserResult parameter, final int offset) {
        Set<OffsetRange> result = new TreeSet<OffsetRange>(new Comparator<OffsetRange>() {
            public int compare(OffsetRange o1, OffsetRange o2) {
                return o1.compareTo(o2);
            }
        });
        Model model = ModelFactory.getModel(parameter);
        OccurencesSupport occurencesSupport = model.getOccurencesSupport(offset);
        Occurence caretOccurence = occurencesSupport.getOccurence();        
        if (caretOccurence != null) {
            ModelElement decl = caretOccurence.getDeclaration();
            if (decl != null && !decl.getPhpKind().equals(PhpKind.INCLUDE)) {
                List<Occurence> allOccurences = caretOccurence.getAllOccurences();
                for (Occurence occurence : allOccurences) {
                    result.add(occurence.getOccurenceRange());
                }
            }
        } else  {
            CodeMarker codeMarker = occurencesSupport.getCodeMarker();
            if (codeMarker != null) {
                List<? extends CodeMarker> allMarkers = codeMarker.getAllMarkers();
                for (CodeMarker marker : allMarkers) {
                    result.add(marker.getOffsetRange());
                }
            }
        }
        return result;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }
}
