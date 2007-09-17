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
package org.netbeans.modules.java.hints.spi;

import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.modules.java.hints.spi.AbstractHint.HintSeverity;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbPreferences;

/** Class to be extended by all the Java hints.
 *
 * @author Petr Hrebejk
 */
public abstract class AbstractHint implements TreeRule {
    
    private boolean enableDefault;
    private boolean showInTaskListDefault;
    private HintSeverity severityDefault;
    private String suppressBy[];        
    
    static {
        HintsSettings.HINTS_ACCESSOR = new HintAccessorImpl();
    }
    
    public AbstractHint(  boolean enableDefault, boolean showInTaskListDefault,HintSeverity severityDefault, String... suppressBy) {
        this.enableDefault = enableDefault;
        this.showInTaskListDefault = showInTaskListDefault;
        this.severityDefault = severityDefault;
        this.suppressBy = suppressBy;
    }
    
    
    /** Gets preferences node, which stores the options for given hint. It is not
     * necessary to override this method unless you want to create some special
     * behavior. The default implementation will create the the preferences node
     * by calling <code>NbPreferences.forModule(this.getClass()).node(profile).node(getId());</code>
     * @profile Profile to get the node for. May be null for current profile
     * @return Preferences node for given hint.
     */
    public Preferences getPreferences( String profile ) { 
        profile = profile == null ? HintsSettings.getCurrentProfileId() : profile;
        return NbPreferences.forModule(this.getClass()).node(profile).node(getId());
    }
        
    /** Gets the UI description for this rule. It is fine to return null
     * to get the default behavior. Notice that the Preferences node is a copy
     * of the node returned frok {link:getPreferences()}. This is in oder to permit 
     * canceling changes done in the options dialog.<BR>
     * Default implementation return null, which results in no customizer.
     * It is fine to return null (as default implementation does)
     * @param node Preferences node the customizer should work on.
     * @return Component which will be shown in the options dialog.
     */    
    public JComponent getCustomizer( Preferences node ) {
        return null;
    }
    
    public abstract String getDescription();
    
    /** Finds out whether the rule is currently enabled.
     * @return true if enabled false otherwise.
     */
    public final boolean isEnabled() {
        return HintsSettings.isEnabled( this, getPreferences(HintsSettings.getCurrentProfileId()));        
    }
    
    /** Gets current severiry of the hint.
     * @return Hints severity in current profile.
     */
    public final HintSeverity getSeverity() {
        return HintsSettings.getSeverity( this, getPreferences(HintsSettings.getCurrentProfileId()));        
    }
    
    /** Severity of hint
     *  <li><code>ERROR</code>  - will show up as error
     *  <li><code>WARNING</code>  - will show up as warrnig
     *  <li><code>CURRENT_LINE_WARNING</code>  - will only show up when the caret is placed in the errorneous element
     */
    public static enum HintSeverity {
        ERROR,
        WARNING,
        CURRENT_LINE_WARNING;     
        
        public Severity toEditorSeverity() {
            switch ( this ) {
                case ERROR:
                    return Severity.ERROR;
                case WARNING:
                    return Severity.VERIFIER;
                case CURRENT_LINE_WARNING:
                    return Severity.HINT;
                default:
                    return null;
            }            
        }
    }
    
    // Private section ---------------------------------------------------------
    
    private static class HintAccessorImpl implements HintsSettings.HintsAccessor {

        public boolean isEnabledDefault(AbstractHint hint) {
            return hint.enableDefault;
        }

        public boolean isShowInTaskListDefault(AbstractHint hint) {            
            return hint.showInTaskListDefault;
        }

        public HintSeverity severiryDefault(AbstractHint hint) {
            return hint.severityDefault;
        }
        
        public String[] getSuppressBy(AbstractHint hint) {
            return hint.suppressBy;
        }
        
    }
}
