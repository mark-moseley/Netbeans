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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import junit.framework.AssertionFailedError;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;

/**
 * Tests evaluation of various expressions.
 * Automatically parses the expressions from the test methods and compares
 * their evaluations with test methods calls.
 *
 * @author Martin Entlicher
 */
public class EvaluatorTest extends NbTestCase {

    private JPDASupport     support;
    private URL             source;


    public EvaluatorTest (String s) {
        super (s);
    }

    protected void setUp () throws Exception {
        super.setUp ();
        System.setProperty("debugger.evaluator2", "true");
        JPDASupport.removeAllBreakpoints ();
        Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src")+
                                  "org/netbeans/api/debugger/jpda/testapps/EvaluatorApp.java");
        LineBreakpoint lb = bp.getLineBreakpoints().get(0);
        source = new URL(lb.getURL());
        DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
        support = JPDASupport.attach (
            "org.netbeans.api.debugger.jpda.testapps.EvaluatorApp"
        );
        support.waitState (JPDADebugger.STATE_STOPPED);
    }

    public void testStaticEvaluation () throws Exception {
        try {
            List<Method> methods = getMethods(true);
            AssertionFailedError te = null;
            AssertionFailedError ex = null;
            for (Method m : methods) {
                try {
                    checkEval (m);
                } catch (AssertionFailedError e) {
                    if (te == null) {
                        te = ex = e;
                    } else {
                        ex.initCause(e);
                        ex = e;
                    }
                }
            }
            if (te != null) {
                throw te;
            }
            //checkEvalFails ("this");
            checkEvalFails ("NoSuchClass.class");
        } finally {
            support.doFinish ();
        }
    }
    
    public void testInstanceEvaluation() throws Exception {
        try {
            Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src")+
                                      "org/netbeans/api/debugger/jpda/testapps/EvaluatorApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(1);
            DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
            support.doContinue();
            support.waitState (JPDADebugger.STATE_STOPPED);
            
            List<Method> methods = getMethods(false);
            AssertionFailedError te = null;
            AssertionFailedError ex = null;
            for (Method m : methods) {
                try {
                    checkEval (m);
                } catch (AssertionFailedError e) {
                    if (te == null) {
                        te = ex = e;
                    } else {
                        ex.initCause(e);
                        ex = e;
                    }
                }
            }
            if (te != null) {
                throw te;
            }
        } finally {
            support.doFinish ();
        }
    }

    private void checkEvalFails (String expression) {
        try {
            Variable var = support.getDebugger ().evaluate (expression);
            fail (
                "Evaluation of expression was unexpectedly successful: " + 
                expression + " = " + var.getValue ()
            );
        } catch (InvalidExpressionException e) {
            // its ok
            return;
        }
    }
    
    private void checkEval(Method m) {
        try {
            Variable eMethod = support.getDebugger ().evaluate (m.getName()+"()");
            String undo = m.getUndo();
            if (undo != null) {
                support.getDebugger ().evaluate (undo+"()");
            }
            Variable eVal = support.getDebugger ().evaluate (m.getExpression());
            if (undo != null) {
                support.getDebugger ().evaluate (undo+"()");
            }
            /*System.err.println("  eMethod = "+eMethod);
            System.err.println("  eVal = "+eVal);
            System.err.println("   equals = "+eMethod.equals(eVal));*/
            Value eMethodJDIValue = ((JDIVariable) eMethod).getJDIValue();
            Value eValJDIValue = ((JDIVariable) eVal).getJDIValue();
            /*System.err.println("  eMethod JDI Value = "+eMethodJDIValue);
            System.err.println("  eVal JDI Value = "+eValJDIValue);
            System.err.println("   equals = "+eMethodJDIValue.equals(eValJDIValue));*/
            if (eMethod != null) {
                assertEquals (
                    "Evaluation of expression '" + m.getExpression()+"' of method '"+m.getName()+"()' produced a wrong type of result:",
                    eMethod.getType(), 
                    eVal.getType()
                );
            }
            assertEquals (
                "Evaluation of expression '" + m.getExpression()+"' of method '"+m.getName()+"()' produced a wrong value:",
                new JDIValue(eMethodJDIValue),
                new JDIValue(eValJDIValue)
            );
            System.err.println("  Method "+m.getName()+"() evaluated successfully.");
        } catch (InvalidExpressionException e) {
            e.printStackTrace();
            fail (
                "Evaluation of expression was unsuccessful: " + e
            );
        }
    }

    private List<Method> getMethods(boolean staticMethods) throws Exception{
        List<Method> methods = new ArrayList<Method>();
        BufferedReader r = new BufferedReader(new InputStreamReader(source.openStream()));
        try {
            Method m = null;
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("*")) continue;
                if (m != null) {
                    int rt = line.indexOf("return");
                    if (rt != 0) {
                        continue;
                    }
                    String expression;
                    try {
                        expression = line.substring(rt+7, line.lastIndexOf(';'));
                    } catch (RuntimeException rex) {
                        System.err.println("line = '"+line+"', rt = "+rt+", lastIndexOf(';') = "+line.lastIndexOf(';'));
                        rex.printStackTrace();
                        throw rex;
                    }
                    expression = expression.trim();
                    m.setExpression(expression);
                    methods.add(m);
                    m = null;
                    continue;
                }
                if (line.indexOf(" test") < 0 || line.indexOf("()") < 0) {
                    continue;
                }
                if (staticMethods != line.indexOf("static") >= 0) {
                    continue;
                }
                String name = line.substring(line.indexOf("test"), line.indexOf("()"));
                if (name.endsWith("_undo")) {
                    String origName = name.substring(0, name.length() - "_undo".length());
                    for (Method om : methods) {
                        if (om.getName().equals(origName)) {
                            om.setUndo(name);
                            break;
                        }
                    }
                } else {
                    m = new Method(name);
                }
            }
        } finally {
            r.close();
        }
        return methods;
    }
    
    private static class Method {
        
        private String name;
        private String undoName;
        private String expression;
        
        public Method(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public void setUndo(String undoName) {
            this.undoName = undoName;
        }
        
        public String getUndo() {
            return undoName;
        }
        
        public void setExpression(String expression) {
            this.expression = expression;
        }
        
        public String getExpression() {
            return expression;
        }
    }
    
    private static class JDIValue {
        
        private Value value;
        
        public JDIValue(Value value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof JDIValue)) return false;
            Value v = ((JDIValue) obj).value;
            if (value == null) return v == null;
            if (value instanceof StringReference) {
                if (!(v instanceof StringReference)) return false;
                return ((StringReference) value).value().equals(((StringReference) v).value());
            }
            if (value instanceof ArrayReference) {
                if (!(v instanceof ArrayReference)) return false;
                ArrayReference a1 = (ArrayReference) value;
                ArrayReference a2 = (ArrayReference) v;
                if (!a1.type().equals(a2.type())) return false;
                if (a1.length() != a2.length()) return false;
                int n = a1.length();
                for (int i = 0; i < n; i++) {
                    if (!new JDIValue(a1.getValue(i)).equals(new JDIValue(a2.getValue(i)))) {
                        return false;
                    }
                }
                return true;
            }
            return value.equals(v);
        }

        @Override
        public int hashCode() {
            if (value == null) return 0;
            else return value.hashCode();
        }

        @Override
        public String toString() {
            return ""+value;
        }
        
    }
}
