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

package org.netbeans.api.debugger.jpda.testapps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Sample application used for testing the evaluator algorithm.
 * Testing is done in two parts. First we stop in main() method and all static
 * methods are tested. Then we stop in instanceMethod() and all instance methods
 * are tested.<p>
 * 
 * All methods starting with "test" are invoked automatically. It's expected that
 * they contain just a return statement, which follows an expression that is
 * to be tested. That expression is automatically extracted and it's evaluation
 * is compared to the actual returned value.
 * When a method has &lt;method_name&gt;_undo counterpart, it is called
 * both after the invocation of the expression and invocation of the method.
 * 
 * @author Martin Entlicher
 */
public class EvaluatorApp {

    private static int      ix = 74;
    private static int      ixcopy = 74;
    private static long     lx = 7400L;
    private static long     lxcopy = 7400L;
    private static long     llx = 7400740010101010740L;
    private static long     llxcopy = 7400740010101010740L;
    private static float    fx = 10.0f;
    private static double   dx = 10.0;
    private static boolean  bx = true;
    private static short    sx = 10;
    private static char     cx = 'a';
    private static byte     btx = 127;
    private static enum     e { ONE, TWO, THREE }
    
    private int     ci = 1234;
    private long    cl = 12345678901234l;
    private double  cd = 1243.4312;
    private Map     cm = new HashMap();
    
    public EvaluatorApp() {
    }

    /* **************************************************************************************************
        The following code must stay where it is, on same line numbers, else all unit tests will fail.
    ************************************************************************************************** */
    public static void main(String[] args) {
        EvaluatorApp app = new EvaluatorApp(); // LBREAKPOINT
        app.instanceMethod(args, llxcopy);
    }

    private void instanceMethod(String[] args, long lparam) {
        int instance = 0;
        Runtime lvar = Runtime.getRuntime();
        instance = instance + 2; // LBREAKPOINT
    }
    
    // TEST METHODS
    
    /* Literals:
     */
    
    public static boolean testBoolean1() {
        return true;
    }
    
    public static boolean testBoolean2() {
        return false;
    }
    
    public static byte testByte() {
        return (byte) 127;
    }
    
    public static char testChar1() {
        return 'a';
    }
    
    public static char testChar2() {
        return '\n';
    }
    
    public static char testChar3() {
        return '\r';
    }
    
    public static char testChar4() {
        return '\b';
    }
    
    public static char testChar5() {
        return '\t';
    }
    
    public static char testChar6() {
        return '\f';
    }
    
    public static char testChar7() {
        return '\\';
    }
    
    public static char testChar8() {
        return '\'';
    }
    
    public static char testChar9() {
        return '"';
    }
    
    public static char testChar10() {
        return '\u03a9';
    }
    
    public static char testChar11() {
        return '\uFFFF';
    }
    
    public static char testChar12() {
        return '\177';
    }
    
    public static short testShort() {
        return (short) 2345;
    }
        
    public static int testInt1() {
        return 3342345;
    }
        
    public static int testInt2() {
        return -334234230;
    }
        
    public static int testInt3() {
        return 0347;
    }
        
    public static int testInt4() {
        return 0xaef;
    }
        
    public static int testInt5() {
        return -0123;
    }
        
    public static int testInt6() {
        return -0x4AEFFF1;
    }
        
    public static int testInt7() {
        return -2147483648;
    }
        
    public static long testLong1() {
        return 3342345l;
    }
        
    public static long testLong2() {
        return 3674898763456478329l;
    }
        
    public static long testLong3() {
        return 3674898763456478329L;
    }
        
    public static long testLong4() {
        return 01233211234435456722112L;
    }
        
    public static long testLong5() {
        return 0x456ad56765el;
    }
        
    public static long testLong6() {
        return 0xF456AD56765EL;
    }
        
    public static long testLong7() {
        return 0xFFFFFFFFFFFFFFFFL;
    }
        
    public static float testFloat1() {
        return 1f;
    }
        
    public static float testFloat2() {
        return 1.321243554323456345676543f;
    }
        
    public static float testFloat3() {
        return -1123e-22f;
    }
        
    public static float testFloat4() {
        return 1344e30f;
    }
        
    public static float testFloat5() {
        return 132E30F;
    }
        
    public static float testFloat6() {
        return .234E-10f;
    }
        
    public static float testFloat7() {
        return 0x234Ep1f;
    }
        
    public static float testFloat8() {
        return 0xA34P-9f;
    }
        
    public static float testFloat9() {
        return 0xA.BP0f;
    }
        
    public static double testDouble1() {
        return 1d;
    }
        
    public static double testDouble2() {
        return 23.44444444432345432456453234586547483927364567382763456784392d;
    }
        
    public static double testDouble3() {
        return -1D;
    }
        
    public static double testDouble4() {
        return 123E303D;
    }
        
    public static double testDouble5() {
        return -.1e-307d;
    }
        
    public static double testDouble6() {
        return 0xAP1D;
    }
        
    public static double testDouble7() {
        return 0xFFFFFFFFFFFFFFFFFFFEEEEEEEEEEEEEEFP1;
    }
        
    public static double testDouble8() {
        return 0xA.BP1;
    }
        
    public static double testDouble9() {
        return 0xABCDEF.FEDCBAP0D;
    }
        
    public static double testDouble10() {
        return -0xABCDEF.FEDCBAP0D;
    }
        
    public static String testString1() {
        return "asd";
    }
    
    public static String testString2() {
        return "\"";
    }
    
    public static String testString3() {
        return "\n";
    }
    
    public static String testString4() {
        return "\2345\n\r\t\\ \b\f\'\'\u5678\uffff\uFFFF";
    }
    
    public static Object testNull() {
        return null;
    }
    
    
    /* Operators. All Java operators are:
        >    <    !       ~       ?       :
        ==      <=   >=   !=      &&      ||      ++      --
        +       -       *       /       &   |       ^       %       <<        >>        >>>
        +=      -=      *=      /=      &=  |=      ^=      %=      <<=       >>=       >>>=
     */
    
    public static boolean testOp1() {
        return ix > fx;
    }

    public static boolean testOp2() {
        return 10 < 11;
    }

    public static boolean testOp3() {
        return !bx;
    }

    public static int testOp4() {
        return ~10;
    }

    public static int testOp5a() {
        return bx ? 1 : 0;
    }

    public static int testOp5b() {
        return !bx ? 1 : 0;
    }

    public static boolean testOp6a() {
        return 10 == ix;
    }

    public static boolean testOp6b() {
        return 10 == fx;
    }

    public static boolean testOp6c() {
        return "10" == "10";
    }

    public static boolean testOp6d() {
        return "10" == "11";
    }

    public static boolean testOp6e() {
        return System.in == System.in;
    }

    public static boolean testOp6f() {
        return System.err == System.out;
    }

    public static boolean testOp7() {
        return 10 >= ix;
    }

    public static boolean testOp8() {
        return 10 <= ix;
    }

    public static boolean testOp9a() {
        return 10 != ix;
    }

    public static boolean testOp9b() {
        return 10 != fx;
    }

    public static boolean testOp9c() {
        return "10" != "10";
    }

    public static boolean testOp9d() {
        return "10" != "11";
    }

    public static boolean testOp9e() {
        return System.in != System.in;
    }

    public static boolean testOp9f() {
        return System.err != System.out;
    }

    public static boolean testOp10a() {
        return bx && true;
    }

    public static boolean testOp10b() {
        return bx && false;
    }

    public static boolean testOp11a() {
        return bx || true;
    }

    public static boolean testOp11b() {
        return bx || false;
    }

    public static boolean testOp11c() {
        return !bx || false;
    }

    public static int testOp12a() {
        return ix++;
    }

    public static int testOp12a_undo() {
        return ix--;
    }

    public static int testOp12b() {
        return ++ix;
    }

    public static int testOp12b_undo() {
        return --ix;
    }

    public static int testOp13a() {
        return ix--;
    }

    public static int testOp13a_undo() {
        return ix++;
    }

    public static int testOp13b() {
        return --ix;
    }

    public static int testOp13b_undo() {
        return ++ix;
    }

    public static int testOp14a() {
        return 5+6;
    }

    public static long testOp14b() {
        return 503211234500000000l+6043213l;
    }

    public static float testOp14c() {
        return 50.234f+6.043e5f;
    }

    public static double testOp14d() {
        return -50.234e148+6.043e150;
    }

    public static int testOp14e() {
        return (short) -50 + (short) 6043;
    }

    public static String testOp14f() {
        return "He" + "llo";
    }

    public static int testOp15a() {
        return 5-6;
    }

    public static double testOp15b() {
        return -5e200-6e200;
    }

    public static int testOp16a() {
        return 5*6;
    }

    public static int testOp16b() {
        return 0xF*0xABCD;
    }

    public static double testOp16c() {
        return 0xFP10*0xABCD.FFAP-1;
    }

    public static int testOp17a() {
        return 5/6;
    }

    public static int testOp17b() {
        return 6/5;
    }

    public static int testOp17c() {
        return -6/5;
    }

    public static int testOp17d() {
        return -5/6;
    }

    public static double testOp17e() {
        return 5./6;
    }

    public static double testOp17f() {
        return 5.111111111111111111/(-6e2);
    }
    
    public static int testOp18a() {
        return 1234 & 54321;
    }
    
    public static int testOp18b() {
        return 0x50F23 & 0x51111;
    }
    
    public static boolean testOp18c() {
        return true & false;
    }
    
    public static long testOp19() {
        return 12345432346L | 95432354654321l;
    }
    
    public static long testOp20() {
        return 12345432346L ^ 95432354654321l;
    }
    
    public static long testOp21() {
        return 12345432343642234L % 2345l;
    }
    
    public static long testOp22() {
        return 12345432343642234L >> 10;
    }
    
    public static long testOp23() {
        return 1234543234364223400L << 18;
    }
    
    public static long testOp24() {
        return 12345432343642231L >>> 24;
    }
    
    //    +=      -=      *=      /=      &=  |=      ^=      %=      <<=       >>=       >>>=
    
    public static int testOp25a() {
        return ix += (byte) 10;
    }
    
    public static int testOp25a_undo() {
        return ix -= 10;
    }
    
    public static long testOp25b() {
        return lx += 10;
    }
    
    public static long testOp25b_undo() {
        return lx -= 10;
    }
    
    public static long testOp26() {
        return lx -= 10l;
    }
    
    public static long testOp26_undo() {
        return lx += 10l;
    }
    
    public static long testOp27() {
        return lx *= (char) 10;
    }
    
    public static long testOp27_undo() {
        return lx /= 10;
    }
    
    public static long testOp28() {
        return lx /= 10;
    }
    
    public static long testOp28_undo() {
        return lx *= 10;
    }
    
    public static int testOp29a() {
        return ix &= (byte) 10;
    }
    
    public static int testOp29a_undo() {
        return ix = ixcopy;
    }
    
    public static long testOp29b() {
        return lx &= 123547890l;
    }
    
    public static long testOp29b_undo() {
        return lx = lxcopy;
    }
    
    public static int testOp30a() {
        return ix |= (byte) 10;
    }
    
    public static int testOp30a_undo() {
        return ix = ixcopy;
    }
    
    public static long testOp30b() {
        return lx |= (byte) 10;
    }
    
    public static long testOp30b_undo() {
        return lx = lxcopy;
    }
    
    public static int testOp31a() {
        return ix ^= (short) 10;
    }
    
    public static int testOp31a_undo() {
        return ix = ixcopy;
    }
    
    public static long testOp31b() {
        return lx ^= 101010;
    }
    
    public static long testOp31b_undo() {
        return lx = lxcopy;
    }
    
    public static int testOp32a() {
        return ix %= (short) 1010;
    }
    
    public static int testOp32a_undo() {
        return ix = ixcopy;
    }
    
    public static long testOp32b() {
        return llx %= 11000100;
    }
    
    public static long testOp32b_undo() {
        return llx = llxcopy;
    }
    
    public static int testOp33a() {
        return ix <<= (byte) 3;
    }
    
    public static int testOp33a_undo() {
        return ix = ixcopy;
    }
    
    public static long testOp33b() {
        return llx <<= 15;
    }
    
    public static long testOp33b_undo() {
        return llx = llxcopy;
    }
    
    public static int testOp34a() {
        return ix >>= (byte) 13;
    }
    
    public static int testOp34a_undo() {
        return ix = ixcopy;
    }
    
    public static long testOp34b() {
        return llx >>= 15;
    }
    
    public static long testOp34b_undo() {
        return llx = llxcopy;
    }
    
    public static int testOp35a() {
        return ix >>>= (byte) 13;
    }
    
    public static int testOp35a_undo() {
        return ix = ixcopy;
    }
    
    public static long testOp35b() {
        return llx >>>= 15;
    }
    
    public static long testOp35b_undo() {
        return llx = llxcopy;
    }
    
    public static boolean testOp36a() {
        return (new Thread() instanceof Runnable);
    }
    
    public static boolean testOp36b() {
        return (System.out instanceof Runnable);
    }
    
    public static boolean testOp36c() {
        return (Runtime.getRuntime() instanceof java.lang.Iterable);
    }
    
    public static boolean testOp36d() {
        return (Runtime.getRuntime() instanceof Runtime);
    }
    
    public static boolean testOp36e() {
        return (Runtime.getRuntime() instanceof Object);
    }
    
    
    // Test operand priorities
    
    public static long testOpPrio1() {
        return 2 * llx + ix * lx - ix / 5;
    }

    public static long testOpPrio2() {
        return llx + -ix / 5 | llx & ix;
    }

    public static long testOpPrio3() {
        return llx * ix >> 5 | llx & ix ^ llx % 10;
    }
    
    public static long testOpPrio4() {
        return (llx - 2*ix) * ix >> (5 | llx) & ix ^ llx % 10;
    }
    
    // Test arrays
    
    public static int[] testArray1() {
        return new int[10];
    }

    public static int[] testArray2() {
        return new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
    }

    public static int[][] testArray3a() {
        return new int[][] { { 1, 2 }, { 3190 }, { 123, 16180, -311111113 } };
    }

    public static double[][] testArray3b() {
        return new double[][] { { 1., 2.1222222222 }, { 3.0e190 }, { 12.3, 16e180, -3.1111111111111111113 } };
    }

    public static String[] testArray4() {
        return new String[] { "test", "an", "Array" };
    }

    public static String[][] testArray5() {
        return new String[120][];
    }

    public static Object[][][] testArray6() {
        return new Object[41][120][];
    }

    public static Object[][][] testArray7() {
        return new Object[41][][];
    }

    public static Boolean[][][] testArray8() {
        return new Boolean[][][] { { { Boolean.TRUE } }, { { Boolean.FALSE }, { Boolean.TRUE }, { Boolean.FALSE } }, { { Boolean.FALSE }, { Boolean.TRUE } } };
    }
    
    // Member select
    
    public static int testMember1() {
        return new String().hashCode();
    }

    public static Comparator testMember2() {
        return new String().CASE_INSENSITIVE_ORDER;
    }
    
    public static int testMember3() {
        return new int[10].length;
    }
    
    public static e testMember4() {
        return e.ONE;
    }
    
    // Method calls
    
    public static Object testMethod1() {
        return System.getProperties();
    }

    public static int testMethod2() {
        return java.lang.Runtime.getRuntime().availableProcessors();
    }

    public static long testMethod3() {
        return Runtime.getRuntime().maxMemory();
    }

    public static int testMethod4() {
        return java.io.File.listRoots().length;
    }

    public static boolean testMethod5() {
        return System.err.checkError();
    }

    public static String testMethod6() {
        return Boolean.valueOf(System.in.markSupported()).toString();
    }

    public static String testMethod7() {
        return String.valueOf(Math.min(Double.NEGATIVE_INFINITY, -100000)).intern().toLowerCase().concat(String.valueOf(Double.POSITIVE_INFINITY)).substring(1, 5);
    }
    
    public static int testMethod10() {
        return new Inner1().method1(ix);
    }
    
    public static int testMethod11() {
        return new Inner1.Inner1_2().method1(ix);
    }
    
    public static int testMethod11b() {
        return ((Inner1) new Inner1.Inner1_2()).method1(ix);
    }
    
    public static int testMethod12() {
        return new Inner1.Inner1_3().method1(ix);
    }
    
    public static int testMethod12b() {
        return ((Inner1) new Inner1.Inner1_3()).method1(ix);
    }
    
    public static float testAutobox1() {
        return methodAutobox(ix, llx, 1000);
    }
    
    public static float testAutobox2() {
        return methodAutobox(new Integer(ix), (long) ix, 1.4f);
    }
    
    public static float testAutobox3() {
        return methodAutobox(new Short((short)10), lx, new Double(1.4));
    }
    
    public static double testAutobox4() {
        return new Float[] { methodAutobox(4, new Long(lx), new Float(1.4)), 15f, new Float(1.5) }[0].doubleValue();
    }
    
    public static float testAutobox5() {
        return new float[] { methodAutobox2(4, new Long(lx), new Float(1.4)), 15f, new Float(1.5) }[0];
    }
    
    public static String testConversion1() {
        return "a"+10;
    }
    
    public static String testConversion2() {
        return "a"+10.6+'x'+new Long(1111111111111111111l);
    }
    
    public static String testConversion3() {
        return 10.6+'x'+new Long(1111111111111111111l)+"a";
    }
    
    public static String testConversion4() {
        return null+" a";
    }
    
    public static double testConversion5() {
        return 10.6+'x'+new Long(1111111111111111111l);
    }
    
    public static double testConversion6() {
        return 1 + 1.7 + 4.5f;
    }
    
    public static float testConversion7() {
        return new Integer(123) + new Float(1.222222222) + new Long(999999999999999999l);
    }
    
    // Constructors
    
    public static boolean testConstructor1() {
        return new Boolean(true).booleanValue();
    }

    public static int testConstructor2() {
        return new ArrayList().size();
    }

    public static int testConstructor3() {
        return new java.util.TreeSet(new Vector(Arrays.asList(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" }))).size();
    }
    
    public static int testInnerConstructor1() {
        return new Inner1().method1(ix);
    }

    public static int testInnerConstructor2() {
        return new EvaluatorApp.Inner1().method1(ix);
    }

    public static int testInnerConstructor3() {
        return new org.netbeans.api.debugger.jpda.testapps.EvaluatorApp.Inner1().method1(ix);
    }

    public static int testInnerConstructor4() {
        return new Inner1.Inner1_1().method1_1(ix);
    }

    public static int testInnerConstructor5() {
        return new EvaluatorApp.Inner1.Inner1_1().method1_1(ix);
    }

    public static int testInnerConstructor6() {
        return new org.netbeans.api.debugger.jpda.testapps.EvaluatorApp.Inner1.Inner1_2().method1_2(ix);
    }

    // Generics
    
    public static String testParametrized1() {
        return new ArrayList<String>(Arrays.asList(new String[] {"a", "b", "c"})).get(0);
    }
    
    
    
    // TEST Instance methods
    
    public int testClassVar1() {
        return ci;
    }
    
    public double testClassVar2() {
        return cd;
    }
    
    public Map testClassVar3() {
        return cm;
    }
    
    public int testLocalVar1() {
        int instance = 0;
        return instance;
    }
    
    public Runtime testLocalVar2() {
        Runtime lvar = Runtime.getRuntime();
        return lvar;
    }
    
    public EvaluatorApp testThis1() {
        return this;
    }
    
    public EvaluatorApp testThis2() {
        return EvaluatorApp.this;
    }
    
    public Object testInner1() {
        return new InnerI1().f1;
    }
    
    public String testInner2() {
        return new InnerI1().getString();
    }
    
    public String testInner3() {
        return new InnerI2().methodToOverride();
    }
    
    public String testInner4() {
        return new InnerI2().methodNotToOverride();
    }
    
    public String testInner5() {
        return ((EvaluatorApp) new InnerI2()).methodToOverride();
    }
    
    public String testInner6() {
        return ((EvaluatorApp) new InnerI2()).methodNotToOverride();
    }
    
    
    
    
    
    public String methodToOverride() {
        return "Orig";
    }
    
    public String methodNotToOverride() {
        return "Orig not override";
    }
    
    public static float methodAutobox(int i, Long l, double d) {
        return ((float) (d / l)) * i;
    }
    
    public static Float methodAutobox2(Integer i, long l, double d) {
        return new Float(((d / l)) * i);
    }
    
    public static class Inner1 {
        public Inner1() {}
        
        public int method1(int i) {
            return 2*i;
        }
        
        public static class Inner1_1 {
            public Inner1_1() {}
            
            public int method1_1(int i) {
                return 3*i;
            }
        }
        
        public static class Inner1_2 extends Inner1 {
            public Inner1_2() {}
            
            public int method1_2(int i) {
                return 4*i;
            }
        }
        
        public static class Inner1_3 extends Inner1 {
            public Inner1_3() {}
            
            @Override
            public int method1(int i) {
                return 5*i;
            }
        }
    }
    
    public class InnerI1 {
        Object f1 = new String("Field");
        public InnerI1() {}
        
        public String getString() {
            return "1234";
        }
    }
    
    public class InnerI2 extends EvaluatorApp {
        
        @Override
        public String methodToOverride() {
            return "Overriden";
        }
        
    }
}
