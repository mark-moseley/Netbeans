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

package org.netbeans.modules.bpel.design.decoration;

import java.awt.Color;
import java.util.List;
import org.netbeans.modules.bpel.design.RightStripe;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class StripeDescriptor implements Descriptor {


    public final boolean selection;
    public final int errors;
    public final int warnings;
    public final int advices;
    public final int breakpoints;
    
    public StripeDescriptor() {
        this(false, 0, 0, 0, 0);
    } 
    
    public StripeDescriptor(boolean selection, int errors, int warnings, 
            int advices, int breakpoints)
    {
        this.selection = selection;
        this.errors = errors;
        this.warnings = warnings;
        this.advices = advices;
        this.breakpoints = breakpoints;
    }
    
    
    public Color getStatusColor() {
        if (errors > 0) {
            return ERROR_COLOR;
        } else if (warnings > 0) {
            return WARNING_COLOR;
        }
        
        return NO_ERRORS;
    }
    
    
    public boolean isFilled() {
        return selection;
    }
    
    
    public Color getColor() {
        if (errors > 0) {
            return ERROR_COLOR;
        } 
        
        if (breakpoints > 0) {
            return BREAKPOINT_COLOR;
        } 
        
        if (warnings > 0) {
            return WARNING_COLOR;
        } 
        
        if (advices > 0) {
            return ADVICE_COLOR;
        } 
        
        if (selection) {
            return SELECTION_COLOR;
        }
        
        return null;
    }
    
    
    public String getStatusText() {
        return (errors + advices + warnings > 0) 
                ? getText(false, false)
                : getMessage("LBL_NoErrors"); // NOI18N
    }
    
    
    public String getText() {
        return getText(true, true);
    }
    
    
    public String getText(boolean includeBreakpoints, boolean includeSelection) 
    {
        StringBuffer buffer = new StringBuffer("<html><body>"); // NOI18N
        
        boolean ok = false;
        
        if (errors > 0) {
            if (errors == 1) {
                buffer.append(getMessage("LBL_Error")); // NOI18N
            } else {
                buffer.append(errors);
                buffer.append(" "); // NOI18N
                buffer.append(getMessage("LBL_Errors")); // NOI18N
            }
            buffer.append("<br>"); // NOI18N
            ok = true;
        }

        if (warnings > 0) {
            if (warnings == 1) {
                buffer.append(getMessage("LBL_Warning")); // NOI18N
            } else {
                buffer.append(warnings);
                buffer.append(" "); // NOI18N
                buffer.append(getMessage("LBL_Warnings")); // NOI18N
            }
            buffer.append("<br>"); // NOI18N
            ok = true;
        }

        if (advices > 0) {
            if (advices == 1) {
                buffer.append(getMessage("LBL_Advice")); // NOI18N
            } else {
                buffer.append(advices);
                buffer.append(" "); // NOI18N
                buffer.append(getMessage("LBL_Advices")); // NOI18N
            }
            buffer.append("<br>"); // NOI18N
            ok = true;
        }

        if (includeBreakpoints && breakpoints > 0) {
            if (breakpoints == 1) {
                buffer.append(getMessage("LBL_Breakpoint")); // NOI18N
            } else {
                buffer.append(breakpoints);
                buffer.append(" "); // NOI18N
                buffer.append(getMessage("LBL_Breakpoints")); // NOI18N
            }
            buffer.append("<br>"); // NOI18N
            ok = true;
        }
        
        if (includeSelection && selection) {
            buffer.append(getMessage("LBL_Selection")); // NOI18N
            ok = true;
        }
        
        buffer.append("</body></html>"); // NOI18N
        
        return (ok) ? buffer.toString() : null;
    }
    
    
    private String getMessage(String key) {
        return NbBundle.getMessage(StripeDescriptor.class, key);
    }
    
    
    public static StripeDescriptor createSelection() {
        return new StripeDescriptor(true, 0, 0, 0, 0);
    }
    
    
    public static StripeDescriptor createBreakpoint() {
        return new StripeDescriptor(false, 0, 0, 0, 1);
    }
    
    
    public static StripeDescriptor createValidation(List<ResultItem> results) {
        if (results == null) {
            return null;
        }
        
        int errors = 0;
        int warnings = 0;
        int advices = 0;
        
        for (ResultItem item : results) {
            switch (item.getType()) {
                case ERROR: 
                    errors++;
                    break;
                case WARNING:
                    warnings++;
                    break;
                case ADVICE:
                    advices++;
                    break;
            }
        }
        
        if (warnings + errors + advices == 0) {
            return null;
        }
        
        return new StripeDescriptor(false, errors, warnings, advices, 0);
    }
    
    
    
    public static StripeDescriptor merge(StripeDescriptor stripe1, 
            StripeDescriptor stripe2) 
    {
        if (stripe2 == null) {
            return stripe1;
        } 
        
        if (stripe1 == null) {
            return stripe2;
        }
        
        return new StripeDescriptor(
                stripe1.selection || stripe2.selection,
                stripe1.errors + stripe2.errors,
                stripe1.warnings + stripe2.warnings,
                stripe1.advices + stripe2.advices,
                stripe1.breakpoints + stripe2.breakpoints);
    }


    public static final Color WARNING_COLOR = new Color(0xE1AA00);
    public static final Color ERROR_COLOR = new Color(0xFF2A1C);
    public static final Color ADVICE_COLOR = new Color(0xE9CA54);
    public static final Color BREAKPOINT_COLOR = new Color(0xFF8FD5);
    public static final Color SELECTION_COLOR = new Color(0xC0B883);
    public static final Color NO_ERRORS = new Color(0x65B56B);
}
