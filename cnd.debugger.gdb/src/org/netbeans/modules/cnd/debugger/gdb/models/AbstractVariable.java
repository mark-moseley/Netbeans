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

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.modules.cnd.debugger.gdb.InvalidExpressionException;
import org.netbeans.modules.cnd.debugger.gdb.Field;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbVariable;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariable;
import org.netbeans.modules.cnd.debugger.gdb.TypeInfo;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/*
 * An AbstractVariable is an array, pointer, struct, or union.
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class AbstractVariable implements LocalVariable, Customizer {
    
    private GdbDebugger debugger;
    protected String name;
    protected String type;
    protected String value;
    protected String ovalue;
    protected String derefValue;
    protected Field[] fields;
    protected TypeInfo tinfo;
    protected static Logger log = Logger.getLogger("gdb.logger"); // NOI18N
    
    private Set<PropertyChangeListener> listeners = new HashSet<PropertyChangeListener>();
    
    /** Create the AV from a GV. If the GV has children then create similar children for the AV */
    public AbstractVariable(GdbVariable var) {
        this(var.getName(), var.getValue());
    }
    
    public AbstractVariable(String name) {
        this(name, null);
    }
    
    public AbstractVariable(String name, String value) {
        assert name.indexOf('{') == -1; // this means a mis-parsed gdb response...
        assert !Thread.currentThread().getName().equals("GdbReaderRP"); // NOI18N
        assert !SwingUtilities.isEventDispatchThread();
        this.name = name;
        type = getDebugger().requestWhatis(name);
        ovalue = null;
        fields = new Field[0];
        tinfo = TypeInfo.getTypeInfo(getDebugger(), this);
        
        if (Utilities.getOperatingSystem() != Utilities.OS_MAC) {
            this.value = value;
        } else {
            // Convert the Mac-specific value to standard gdb/mi format
            this.value = GdbUtils.mackHack(value);
        }
        
        if (GdbUtils.isSinglePointer(type)) {
            derefValue = getDebugger().requestValue('*' + name);
        } else {
            derefValue = null;
        }
    }
        
    protected AbstractVariable() {} // used by AbstractField instantiation...
    
    private TypeInfo getTypeInfo() {
        if (tinfo == null) {
            tinfo = TypeInfo.getTypeInfo(getDebugger(), this);
        }
        return tinfo;
    }
    
    /**
     * Declared type of this local.
     *
     * @return declared type of this local
     */
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setTypeToError(String msg) {
        msg = msg.replace("\\\"", "\""); // NOI18N
        if (msg.charAt(msg.length() - 1) == '.') {
            msg = msg.substring(0, msg.length() - 1);
        }
        setType('>' + msg + '<');
        log.fine("AV.setTypeToError[" + Thread.currentThread().getName() + "]: " + getName()); // NOI18N
    }
    
    /**
     * Returns string representation of type of this variable.
     *
     * @return string representation of type of this variable.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets string representation of value of this variable. In this case we ignore the
     * request because we only allow setting values on leaves.
     *
     * @param value string representation of value of this variable.
     */
    public void setValue(String value) {
        String msg = null;
        String rt = getTypeInfo().getResolvedType();
        int pos;
        
        if (getDebugger() != null) {
            value = value.trim();
            if (value.length() > 0 && value.charAt(0) == '(' && (pos = GdbUtils.findMatchingParen(value, 0)) != -1) {
                // Strip a cast
                value = value.substring(pos + 1).trim();
            }
            if (rt.equals("char") || rt.equals("unsigned char")) { // NOI18N
                value = setValueChar(value);
                if (value == null) { // Invalid input
                    msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Char"); // NOI18N
                }
            } else if (rt.equals("char *") || rt.equals("unsigned char *")) { // NOI18N
                value = setValueCharStar(value);
                if (value == null) { // Invalid input
                    msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Char*"); // NOI18N
                }
            } else if ((rt.equals("int") || rt.equals("long"))) { // NOI18N
                value = setValueNumber(value);
                if (value == null) { // Invalid input
                    msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Number"); // NOI18N
                }
            } else if (getDebugger().isCplusPlus() && rt.equals("bool")) { // NOI18N
                if (!value.equals("true") && !value.equals("false") && !isNumber(value)) { // NOI18N
                    msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_CplusPlus_Bool"); // NOI18N
                }
            } else if (rt.startsWith("enum ")) { // NOI18N
                value = setValueEnum(value);
                if (value == null) { // Invalid input
                    msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Enum"); // NOI18N
                }
            } else if (value.charAt(0) == '"' || (value.startsWith("0x") && value.endsWith("\""))) { // NOI18N
                value = setValueCharStar(value);
                if (value == null) { // Invalid input
                    msg = NbBundle.getMessage(AbstractVariable.class, "ERR_SetValue_Invalid_Char*"); // NOI18N
                }
            } else if (GdbUtils.isPointer(rt)) {
                // no current validation
            }
            if (value != null) {
                if (value.endsWith("\\\"")) { // NOI18N
                    pos = value.indexOf('"');
                    if (pos != -1) {
                        value = value.substring(pos, value.length() - 1) + '"';
                    }
                }
                if (value.charAt(0) == '(') {
                    pos = GdbUtils.findMatchingParen(value, 0);
                    if (pos != -1) {
                        value = value.substring(pos + 1).trim();
                    }
                }
            }
            if (msg == null) {
                String fullname;
                if (this instanceof GdbWatchVariable) {
                    fullname = ((GdbWatchVariable) this).getWatch().getExpression();
                } else {
                    if (this instanceof AbstractField) {
                        fullname = ((AbstractField) this).getFullName(false);
                    } else {
                        fullname = name;
                    }
                }
                ovalue = this.value;
                getDebugger().updateVariable(this, fullname, value);
            }
        }
        if (msg != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(msg);
            nd.setTitle("TITLE_SetValue_Warning"); // NOI18N
            DialogDisplayer.getDefault().notify(nd);
        }
    }
    
    public void restoreOldValue() {
        value = ovalue;
    }
    
    public synchronized void setModifiedValue(String value) {
        this.value = value;
        if (fields.length > 0) {
            fields = new Field[0];
            derefValue = null;
            expandChildren();
        }
    }
    
    /**
     * Validate the string passed to setValue. Verify its a correct char format and remove a leading
     * address if needed.
     *
     * @param value The value typed by the user into the value editor
     * @returns A valid value (valid in the sense gdb should accept it) or null
     */
    private String setValueChar(String value) {
        int pos;
        
        if (value.startsWith("0x") && (pos = value.indexOf(" '")) != -1 && value.endsWith("'")) { // NOI18N
            value = value.substring(pos + 1);
        } else if (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'') {
            // OK
        } else {
            value = null;
        }
        return value;
    }
    
    /**
     * Validate the string passed to setValue. Verify its a correct char format and remove a leading
     * address if needed.
     *
     * @param value The value typed by the user into the value editor
     * @returns A valid value (valid in the sense gdb should accept it) or null
     */
    private String setValueCharStar(String value) {
        int pos;
        
        if (value.startsWith("0x") && (pos = value.indexOf(" \\\"")) != -1 && value.endsWith("\\\"")) { // NOI18N
            value = '"' + value.substring(pos + 3, value.length() - 2) + '"'; // NOI18N
        } else if (value.startsWith("0x") && (pos = value.indexOf(" \"")) != -1 && value.endsWith("\"")) { // NOI18N
            value = value.substring(pos + 1);
        } else if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') { // NOI18N
            // OK
        } else {
            value = null;
        }
        return value;
    }
    
    /**
     * Validate the string passed to setValue.
     *
     * @param value The value typed by the user into the value editor
     * @returns A valid value (valid in the sense gdb should accept it) or null
     */
    private String setValueEnum(String value) {
        String rt = getTypeInfo().getResolvedType();
        int pos1 = rt.indexOf('{');
        int pos2 = rt.indexOf('}');
        if (pos1 > 0 && pos2 > 0) {
            String enum_values = rt.substring(pos1 + 1, pos2);
            for (String frag : enum_values.split(", ")) { // NOI18N
                if (value.equals(frag)) {
                    return value;
                }
            }
        }
        return null;
    }
    
    /**
     * Validate the string passed to setValue. Verify its a correct numerical format .
     *
     * @param value The value typed by the user into the value editor
     * @returns A valid value (valid in the sense gdb should accept it) or null
     */
    private String setValueNumber(String value) {
        if (isNumber(value)) {
            // OK
        } else {
            value = null;
        }
        return value;
    }

    public void setObject(Object bean) {
    }

   /**
    * See if this variable <i>will</i> have fields and should show a turner.
    * We're not actually creating or counting fields here.
    *
    * @return 0 if the variable shouldn't have a turner and fields.length if it should
    */
    public int getFieldsCount() {
        if (getDebugger() == null || !getDebugger().getState().equals(GdbDebugger.STATE_STOPPED)) {
            return 0;
        } else if (fields.length > 0) {
            return fields.length;
        } else if (mightHaveFields()) {
            return estimateFieldCount();
        } else {
            return 0;
        }
    }
    
    /**
     * The else-if in getFieldsCount() was getting too complex, so I've factored it out and
     * made it into multiple if/else-if statements. I think its easier to track this way.
     * 
     * @return true if the field should have a turner and false if it shouldn't
     */
    private boolean mightHaveFields() {
        String rt = getTypeInfo().getResolvedType();
        if (GdbUtils.isArray(rt) && !isCharString(rt)) {
            return true;
        } else if (isValidPointerAddress()) {
            if (GdbUtils.isFunctionPointer(rt) || rt.equals("void *") || // NOI18N
                    (isCharString(rt) && !GdbUtils.isMultiPointer(rt))) {
                return false;
            } else {
                return true;
            }
        } else if (value != null && value.length() > 0 && value.charAt(0) == '{') {
            return true;
        }
        return false;
    }
    
    /**
     * I'd like to estimate field count based on the value string. However, this might
     * actually be better. If I set the children count high then it gets reset once the
     * children get created. If I set it too low, only that number of fields are shown
     * (even though the var has more fields).
     */
    private int estimateFieldCount() {
        int count = 100;
        return count;
    }
        
    private boolean isValidPointerAddress() {
        String frag = "";
        int pos1;
        int i;
        
        if (value != null) { // value can be null for watches during initialization...
            if (value.length() > 0 && value.charAt(0) == '(') {
                pos1 = value.indexOf("*) 0x"); // NOI18N
                if (pos1 == -1) {
                    pos1 = value.indexOf("* const) 0x"); // NOI18N
                    if (pos1 != -1) {
                        frag = value.substring(pos1 + 11);
                    }
                } else {
                    frag = value.substring(pos1 + 5);
                }
                if (pos1 != -1) {
                    try {
                        i = Integer.parseInt(frag, 16);
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                    return i > 0;
                }
            } else if (value.startsWith("0x")) { // NOI18N
                try {
                    i = Integer.parseInt(value.substring(2), 16);
                } catch (NumberFormatException ex) {
                    return false;
                }
                return i > 0;
            }
        }
        return false;
    }
    
    /**
     * Returns field defined in this object.
     *
     * @param name a name of field to be returned
     * @return field defined in this object
     */
    public Field getField(String name) {
        int i, k = fields.length;
        for (i=0; i < k; i++) {
            Field f = fields[i];
            if (name.equals(f.getName())) {
                return f;
            }
        }        
        return null; // Not found
    }
    
    /**
     * Returns all fields declared in this type that are in interval
     * &lt;<code>from</code>, <code>to</code>).
     */
    public Field[] getFields(int from, int to) {
        if (to != 0) {
            if (fields.length == 0) {
                expandChildren();
            }
            to = Math.min(fields.length, to);
            from = Math.min(fields.length, from);
            Field[] fv = new Field[to - from];
            System.arraycopy(fields, from, fv, 0, to - from);
            return fv;
        }
        return fields;
    }
    
    /**
     * In the JPDA implementation a value isn't always a String. We're (currently)
     * storing the value as a String so no conversion is done. However, keeping
     * this method makes it possible for us to change at a later date...
     *
     * @return The value of this instance
     */
    public String getToStringValue () throws InvalidExpressionException {
        return getValue();
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof AbstractVariable &&
                    getFullName(true).equals(((AbstractVariable) o).getFullName(true));
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String getName() {
        return name;
    }
    
    protected final GdbDebugger getDebugger() {
        if (debugger == null) {
            DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
            if (currentEngine == null) {
                return null;
            }
            debugger = (GdbDebugger) currentEngine.lookupFirst(null, GdbDebugger.class);
        }
        return debugger;
    }
    
    public synchronized boolean expandChildren() {
        if (fields.length == 0) {
            createChildren();
        }
        return fields.length > 0;
    }
    
    private void createChildren() {
        String resolvedType = getTypeInfo().getResolvedType();
        Map<String, Object> map;
        String t;
        String v;
        
        if (GdbUtils.isPointer(resolvedType) && !isCharString(resolvedType) &&
                        isCast(value) && !GdbUtils.isMultiPointer(resolvedType)) {
            if (value.endsWith(" 0") || value.endsWith(" 0x0")) { // NOI18N
                t = null;
                v = null;
            } else {
                t = GdbUtils.getBaseType(resolvedType);
                v = getDebugger().requestValue('*' + name);
            }
        } else {
            t = resolvedType;
            v = value;
        }
        if (v != null) { // v can be null if we're no longer in a stopped state
            if (GdbUtils.isArray(t)) {
                createChildrenForArray(t, v.substring(1, v.length() - 1));
            } else if (GdbUtils.isMultiPointer(t)) {
                createChildrenForMultiPointer(t);
            } else {
                map = getTypeInfo().getMap();
                if (!map.isEmpty()) {
                    String val = v.substring(1, v.length() - 1);
                    int start = 0;
                    int end = GdbUtils.findNextComma(val, 0);
                    while (end != -1) {
                        String vfrag = val.substring(start, end).trim();
                        addField(completeFieldDefinition(this, map, vfrag));
                        start = end + 1;
                        end = GdbUtils.findNextComma(val, end + 1);
                    }
                    addField(completeFieldDefinition(this, map, val.substring(start).trim()));
                }
            }
        }
    }
    
    private void createChildrenForMultiPointer(String t) {
        int i = 0;
        String fullname = getFullName(false);
        String t2 = t.substring(0, t.length() - 1);
        int max_fields = t2.startsWith("char *") ? 20 : 10; // NOI18N
        
        while (max_fields-- > 0) {
            String v = getDebugger().requestValue(fullname + '[' + i + ']');
            if (v == null || v.length() < 1 || v.endsWith("0x0")) { // NOI18N
                return;
            }
            addField(new AbstractField(this, name + '[' + i++ + ']', t2, v));
        }
    }
    
    /**
     * Check the type. Does it resolve to a char *? If so then we don't want to
     * expand it further. But if its not a char * then we (probably) do.
     * 
     * @param info The string to verify
     * @return true if t is some kind of a character pointer
     */
    private boolean isCharString(String t) {
        if (t != null && t.endsWith("*") && !t.endsWith("**")) { // NOI18N
            t = GdbUtils.getBaseType(t);
            if (t.equals("char") || t.equals("unsigned char")) { // NOI18N
                return true;
            }
        }
        return false;
    }

    private boolean isCast(String info) {
        if (info.length() > 0 && info.charAt(0) == '(') {
            int pos = GdbUtils.findMatchingParen(info, 0);
            if (pos != -1 && info.length() > pos + 2 && info.substring(pos + 1, pos + 4).equals(" 0x")) { // NOI18N
                return true;
            }
        }
        return false;
    }
    
    /**
     * Complete and create the field information. Its OK to return null because addField
     * ignores it.
     */
    private AbstractField completeFieldDefinition(AbstractVariable parent, Map<String, Object> map, String info) {
        String n, t, v;
        int count;
        
        if (info.charAt(0) == '{') { // we've got an anonymous class/struct/union...
            try {
                count = Integer.parseInt((String) map.get("<anon-count>")); // NOI18N
            } catch (NumberFormatException nfe) {
                count = 0;
            }
            info = info.substring(1, info.length() - 1);
            for (int i = 1; i <= count; i++) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) map.get("<anonymous" + i + ">"); // NOI18N
                int start = 0;
                int end = GdbUtils.findNextComma(info, 0);
                while (end != -1) {
                    String vfrag = info.substring(start, end).trim();
                    parent.addField(completeFieldDefinition(parent, m, vfrag));
                    start = end + 1;
                    end = GdbUtils.findNextComma(info, end + 1);
                }
                parent.addField(completeFieldDefinition(parent, m, info.substring(start).trim()));
            }
        } else {
            int pos = info.indexOf('=');
            if (pos != -1) {
                if (info.charAt(0) == '<') {
                    n = NbBundle.getMessage(AbstractVariable.class, "LBL_BaseClass"); // NOI18N
                    t = info.substring(1, pos - 2).trim();
                    v = info.substring(pos + 1).trim();
                    if (n.startsWith("_vptr")) { // NOI18N
                        return null;
                    }
                } else {
                    n = info.substring(0, pos - 1).trim();
                    v = info.substring(pos + 1).trim();
                    if (n.startsWith("_vptr")) { // NOI18N
                        return null;
                    }
                        Object o = map.get(n);
                        if (o instanceof String) {
                            t = o.toString();
                        } else if (o instanceof Map) {
                            t = (String) ((Map) o).get("<typename>"); // NOI18N
                        } else if (isNumber(v)) {
                            t = "int"; // NOI18N - best guess (std::string drops an "int")
                        } else {
                            log.warning("Cannot determine field type for " + n); // NOI18N
                            return null;
                        }
                        }
                return new AbstractField(parent, n, t, v);
            } else if (info.trim().equals("<No data fields>")) { // NOI18N
                return new AbstractField(parent, "", "", info.trim()); // NOI18N
            }
        }
        return null;
    }
    
    private int parseCharArray(AbstractVariable var, String basename, String type, String value) {
        String frag;
        int count = 0;
        int idx = 0;
        int pos;
        boolean truncated = false;
        
        while (idx < value.length()) {
            if (value.substring(idx).startsWith("\\\"")) { // NOI18N
                pos = value.indexOf("\\\",", idx); // NOI18N
                if (pos >= 0) {
                    frag = value.substring(idx + 2, pos);
                    idx += frag.length() + 4;
                } else {
                    // Reached the end of the string...
                    if (value.endsWith("\\\"...")) { // NOI18N
                        frag = value.substring(idx + 2, value.length() - 5);
                        truncated = true;
                    } else {
                        frag = value.substring(idx + 2, value.length() - 2);
                    }
                    idx = value.length(); // stop iterating...
                }
                count += parseCharArrayFragment(var, basename, type, frag);
                if (truncated) {
                    String high;
                    try {
                        high = type.substring(type.indexOf("[") + 1, type.indexOf("]")); // NOI18N
                        int xx = Integer.parseInt(high);
                    } catch (Exception ex) {
                        high = "..."; // NOI18N
                    }
                    
                    var.addField(new AbstractField(var, basename +
                            "[" + var.fields.length + "-" + high + "]", // NOI18N
                            "", "...")); // NOI18N
                }
            } else if (value.charAt(idx) == ' ' || value.charAt(idx) == ',') {
                idx++;
            } else {
                pos = GdbUtils.findNextComma(value, idx);
                if (pos > 0) {
                    frag = value.substring(idx, pos);
                } else {
                    frag = value.substring(idx);
                }
                count += parseRepeatArrayFragment(var, basename, type, frag);
                idx += frag.length();
            }
        }
        return count;
    }
    
    private int parseRepeatArrayFragment(AbstractVariable var, String basename, String type, String value) {
        String t = type.substring(0, type.indexOf('[')).trim();
        int count;
        int idx = var.fields.length;
        int pos = value.indexOf(' ');
        String val = value.substring(0, pos);
        int pos1 = value.indexOf("<repeats "); // NOI18N
        int pos2 = value.indexOf(" times>"); // NOI18N
        
        try {
            count = Integer.parseInt(value.substring(pos1 + 9, pos2));
        } catch (Exception ex) {
            return 0;
        }
        
        while (--count >=0) {
            var.addField(new AbstractField(var, basename + "[" + idx++ + "]", // NOI18N
                t, '\'' + val + '\''));
        }
        return 0;   
    }
    
    private int parseCharArrayFragment(AbstractVariable var, String basename, String type, String value) {
        String t = type.substring(0, type.indexOf('[')).trim();
        int vidx = 0;
        int count = value.length();
        int idx = var.fields.length;
        
        while (vidx < count) {
            String val;
            if (vidx < (count - 2) && value.substring(vidx, vidx + 2).equals("\\\\")) { // NOI18N
                char ch = value.charAt(vidx + 2);
                if (Character.isDigit(ch)) {
                    val = '\\' + value.substring(vidx + 2, vidx + 5);
                    vidx += 5;
                } else {
                    val = '\\' + value.substring(vidx + 2, vidx + 3);
                    vidx += 3;
                }
            } else if (charAt(value, vidx) == '\\') { // we're done...
                val = "\\000"; // NOI18N
            } else {
                val = value.substring(vidx, vidx + 1);
                vidx++;
            }
            var.addField(new AbstractField(var, basename + "[" + idx++ + "]", // NOI18N
                t, '\'' + val + '\''));
        }
        return count;
    }
    private char charAt(String info, int idx) {
        try {
            return info.charAt(idx);
        } catch (StringIndexOutOfBoundsException e) {
            return 0;
        }
    }
    
    private void createChildrenForArray(String type, String value) {
        String t;
        int lbpos;
        int cbrace = type.lastIndexOf('}');
        if (cbrace == -1) {
            lbpos = type.indexOf('[');
        } else {
            lbpos = type.indexOf('[', cbrace);
            cbrace = type.indexOf('{');
        }
        int rbpos = GdbUtils.findMatchingBrace(type, lbpos);
        assert rbpos != -1;
        int vstart = 0;
        int vend;
        int size;
        int nextbrace = type.indexOf('[', rbpos);
        String extra;
        
        if (nextbrace == -1) {
            extra = "";
        } else {
            extra = type.substring(nextbrace);
        }
        if (cbrace == -1) {
            t = type.substring(0, lbpos).trim() + extra;
        } else {
            t = type.substring(0, cbrace).trim() + extra;
        }
        
        try {
            size = Integer.valueOf(type.substring(lbpos + 1, rbpos));
        } catch (Exception ex) {
            size = 0;
        }
        if (t.equals("char")) { // NOI18N
            String nextv;
            for (int i = 0; i < size && vstart != -1; i++) {
                nextv = nextValue(value, vstart < size ? vstart : size);
                addField(new AbstractField(this, name + "[" + i + "]", t, nextv)); // NOI18N
                vstart += nextv.length();
            }
        } else {
            for (int i = 0; i < size && vstart != -1; i++) {
                if (value.charAt(vstart) == '{') {
                    vend = GdbUtils.findNextComma(value, GdbUtils.findMatchingCurly(value, vstart));
                } else {
                    vend = GdbUtils.findNextComma(value, vstart);
                }
                addField(new AbstractField(this, name + "[" + i + "]", t, // NOI18N
                        vend == -1 ? value.substring(vstart) : value.substring(vstart, vend)));
                vstart = GdbUtils.firstNonWhite(value, vend + 1);
            }
        }
    }
    
    private String nextValue(String info, int start) {
        char ch = info.charAt(start);
        
        if (info.charAt(start) == '\\' && info.charAt(start + 1) == '\\') {
            ch = info.charAt(start + 2);
            if (ch == 'n' || ch == 't' || ch == 'b' || ch == 'r' || ch == '"' || ch == '\'') {
                return info.substring(start, start + 3);
            } else {
                try {
                    Integer.parseInt(info.substring(start + 2, start + 5), 8);
                    return info.substring(start, start + 5);
                } catch (NumberFormatException nfe) {}
            }
        } else {
            return info.substring(start, start + 1);
        }
        throw new IllegalStateException();
    }
    
    /**
     * Adds a field.
     *
     * Note: completeFieldDefinition returns null for _vptr data. Its easier to let it return null and
     * ignore it here than to check the return value in expandChildrenFromValue and not call addField
     * if its null.
     *
     * @parameter field A field to add.
     */
    public void addField(Field field) {
        if (field != null) {
            int n = fields.length;
            Field[] fv = new Field[n + 1];
            System.arraycopy(fields, 0, fv, 0, n);
            fields = fv;
            fields[n] = field;
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        listeners.add(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.remove(l);
    }
    
    @Override
    public String toString() {
        return "AbstractVariable "; // NOI18N
    }
        
    private boolean isNumber(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
        
    public String getFullName(boolean showBase) {
        if (this instanceof AbstractField) {
            return ((AbstractField) this).getFullName(showBase);
        } else {
            return getName();
        }
    }
    
    public class AbstractField extends AbstractVariable implements Field {
        
        private AbstractVariable parent;
        
        public AbstractField(AbstractVariable parent, String name, String type, String value) {
            if (name.startsWith("static ")) { // NOI18N
                this.name = name.substring(7);
            } else {
                this.name = name;
            }
            int lcurly = type.indexOf('{');
            if (lcurly == -1) {
                this.type = type;
            } else {
                int rcurly = type.indexOf('}', lcurly);
                this.type = type.substring(0, lcurly).trim() + type.substring(rcurly + 1); 
            }
            this.parent = parent;
            fields = new Field[0];
            derefValue = null;
            tinfo = null;
        
            if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                this.value = GdbUtils.mackHack(value);
            } else {
                this.value = value;
            }
        }
        
        public boolean isStatic() {
            return false;
        }
        
        @Override
        public String getFullName(boolean showBaseClass) {
            String pname; // parent part of name
            String fullname;
            int pos;
            
            if (parent instanceof AbstractField) {
                pname = ((AbstractField) parent).getFullName(showBaseClass);
            } else {
                pname = parent.getName();
            }
            
            if (name.equals(NbBundle.getMessage(AbstractVariable.class, "LBL_BaseClass"))) { // NOI18N
                if (showBaseClass) {
                    fullname = pname + ".<" + type + ">"; // NOI18N
                } else {
                    fullname = pname;
                }
            } else if (name.indexOf('[') != -1) {
                if ((pos = pname.lastIndexOf('.')) != -1) {
                    fullname = pname.substring(0, pos) + '.' + name;
                } else {
                    fullname = name;
                }
            } else if (GdbUtils.isSimplePointer(parent.getType()) && name.startsWith("*")) { // NOI18N
                fullname = '*' + pname;
            } else if (GdbUtils.isPointer(parent.getType())) {
                fullname = pname + "->" + name; // NOI18N
            } else if (name.length() > 0) {
                fullname = pname + '.' + name;
            } else {
                fullname = pname;
            }
            return fullname;
        }
    }
}

