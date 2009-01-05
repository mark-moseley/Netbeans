
/*
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/, and in the file LICENSE.html in the
 * doc directory.
 * 
 * The Original Code is HAT. The Initial Developer of the
 * Original Code is Bill Foote, with contributions from others
 * at JavaSoft/Sun. Portions created by Bill Foote and others
 * at Javasoft/Sun are Copyright (C) 1997-2004. All Rights Reserved.
 * 
 * In addition to the formal license, I ask that you don't
 * change the history or donations files without permission.
 * 
 */
 
package org.netbeans.modules.profiler.heapwalk.oql;
 
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.modules.profiler.heapwalk.oql.model.Snapshot;
 
/**
 * This is Object Query Language Interpreter
 *
 * @author A. Sundararajan [jhat @(#)OQLEngine.java	1.9 06/06/20]
 * @authoe J. Bachorik [NB Profiler]
 */
public class OQLEngine {
    static {
        try {
            // Do we have javax.script support?
            Class.forName("javax.script.ScriptEngineManager");
            oqlSupported = true;
        } catch (ClassNotFoundException cnfe) {
            oqlSupported = false;
        }
    }

    // check OQL is supported or not before creating OQLEngine 
    public static boolean isOQLSupported() {
        return oqlSupported;
    }
 
    public OQLEngine(Snapshot snapshot) {
        if (!isOQLSupported()) {
            throw new UnsupportedOperationException("OQL not supported");
        }
        init(snapshot);
    }
 
    /**
       Query is of the form
 
          select &lt;java script code to select&gt;
          [ from [instanceof] &lt;class name&gt; [&lt;identifier&gt;]
            [ where &lt;java script boolean expression&gt; ]
          ]
    */
    public synchronized void executeQuery(String query, ObjectVisitor visitor) 
                                          throws OQLException {
        debugPrint("query : " + query);
        StringTokenizer st = new StringTokenizer(query);
        if (st.hasMoreTokens()) {
            String first = st.nextToken();
            if (! first.equals("select") ) {
                // Query does not start with 'select' keyword.
                // Just treat it as plain JavaScript and eval it.
                try {
                    Object res = evalScript(query);
                    visitor.visit(res);
                } catch (Exception e) {
                    throw new OQLException(e);
                }
                return;
            }
        } else {
            throw new OQLException("query syntax error: no 'select' clause");
        }
 
        String selectExpr = ""; 
        boolean seenFrom = false;
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("from")) {
                seenFrom = true;
                break;
            }
            selectExpr += " " + tok;
        }
 
        if (selectExpr.equals("")) {
            throw new OQLException("query syntax error: 'select' expression can not be empty");
        }
             
        String className = null;
        boolean isInstanceOf = false;
        String whereExpr =  null;
        String identifier = null;
 
        if (seenFrom) {
            if (st.hasMoreTokens()) {
                String tmp = st.nextToken();
                if (tmp.equals("instanceof")) {
                    isInstanceOf = true;
                    if (! st.hasMoreTokens()) {
                        throw new OQLException("no class name after 'instanceof'");
                    }
                    className = st.nextToken();
                } else {
                    className = tmp;
                }
            } else {
                throw new OQLException("query syntax error: class name must follow 'from'");
            }
     
            if (st.hasMoreTokens()) {
                identifier = st.nextToken();
                if (identifier.equals("where")) {
                    throw new OQLException("query syntax error: identifier should follow class name");
                }
                if (st.hasMoreTokens()) {
                    String tmp = st.nextToken();
                    if (! tmp.equals("where")) {
                        throw new OQLException("query syntax error: 'where' clause expected after 'from' clause");
                    }
 
                    whereExpr = "";
                    while (st.hasMoreTokens()) {
                        whereExpr += " " + st.nextToken();
                    }
                    if (whereExpr.equals("")) {
                        throw new OQLException("query syntax error: 'where' clause cannot have empty expression");
                    }
                }
            } else {
                throw new OQLException("query syntax error: identifier should follow class name");
            }
        } 
 
        executeQuery(new OQLQuery(selectExpr, isInstanceOf, className, 
                                  identifier, whereExpr), visitor);
    }
 
    private void executeQuery(OQLQuery q, ObjectVisitor visitor) 
                              throws OQLException {
        visitor = visitor != null ? visitor : ObjectVisitor.DEFAULT;
        
        JavaClass clazz = null;
        if (q.className != null) {
            clazz = snapshot.findClass(q.className);
            if (clazz == null) {
                throw new OQLException(q.className + " is not found!");
            }
        }
 
        StringBuffer buf = new StringBuffer();
        buf.append("function __select__(");
        if (q.identifier != null) {
            buf.append(q.identifier);
        }
        buf.append(") { return ");
        buf.append(q.selectExpr.replace('\n', ' '));
        buf.append("; }");
 
        String selectCode = buf.toString();
//        debugPrint(selectCode);
        String whereCode = null;
        if (q.whereExpr != null) {
            buf = new StringBuffer();
            buf.append("function __where__(");
            buf.append(q.identifier);
            buf.append(") { return ");
            buf.append(q.whereExpr.replace('\n', ' '));
            buf.append("; }");
            whereCode = buf.toString();
        }
//        debugPrint(whereCode);
 
        // compile select expression and where condition 
        try {
            evalMethod.invoke(engine, new Object[] { selectCode });
            if (whereCode != null) {
                evalMethod.invoke(engine, new Object[] { whereCode });
            }
 
            if (q.className != null) {
//                Enumeration objects = clazz.getInstances(q.isInstanceOf);
                List objects = clazz.getInstances();
                for(Object obj : objects) {
                    Object[] args = new Object[] { wrapJavaObject((Instance)obj) };
                    boolean b = (whereCode == null);
                    if (!b) {
                        Object res = call("__where__", args);
                        if (res instanceof Boolean) {
                            b = ((Boolean)res).booleanValue();
                        } else if (res instanceof Number) {
                            b = ((Number)res).intValue() != 0;
                        } else {
                            b = (res != null);
                        }
                    }
 
                    if (b) {
                        Object select = call("__select__", args);
                        if (select instanceof Iterator) {
                            Iterator iter = (Iterator)select;
                            while(iter.hasNext()) {
                                if (visitor.visit(iter.next())) return;
                            }
                        } else {
                            if (visitor.visit(select)) return;
                        }
                    }
                }
                // http://www.mozilla.org/rhino/ScriptingJava.html
                XXX
            } else {
                // simple "select <expr>" query
                Object select = call("__select__", new Object[] {});
                visitor.visit(select);
            }
        } catch (Exception e) {
            throw new OQLException(e);
        }
    }

    public Object evalScript(String script) throws Exception {
        return evalMethod.invoke(engine, new Object[] { script });
    }

    public Object wrapJavaObject(Instance obj) throws Exception {
        return call("wrapJavaObject", new Object[] { obj });
    }
 
    public Object toHtml(Object obj) throws Exception {
        return call("toHtml", new Object[] { obj });
    }
 
    public Object call(String func, Object[] args) throws Exception {
        return invokeMethod.invoke(engine, new Object[] { func, args });
    }

    public Object unwrapJavaObject(Object object) {
        try {
            return invokeMethod.invoke(engine, new Object[]{"unwrapJavaObject", new Object[] {object}});
        } catch (Exception ex) {
            if (debug) {
                ex.printStackTrace(System.err);
            }
        }
        return null;
    }
 
    private static void debugPrint(String msg) {
        if (debug) System.out.println(msg);
    }
 
    private void init(Snapshot snapshot) throws RuntimeException {
        this.snapshot = snapshot;
        try {
            // create ScriptEngineManager
            Class managerClass = Class.forName("javax.script.ScriptEngineManager");
            Object manager = managerClass.newInstance();
 
            // create JavaScript engine
            Method getEngineMethod = managerClass.getMethod("getEngineByName",
                                new Class[] { String.class });
            engine = getEngineMethod.invoke(manager, new Object[] {"js"});
 
            // initialize engine with init file (hat.js)
            InputStream strm = getInitStream();
            Class engineClass = Class.forName("javax.script.ScriptEngine");   
            evalMethod = engineClass.getMethod("eval",
                                new Class[] { Reader.class });
            evalMethod.invoke(engine, new Object[] {new InputStreamReader(strm)});
 
            // initialize ScriptEngine.eval(String) and
            // Invocable.invokeFunction(String, Object[]) methods.
            Class invocableClass = Class.forName("javax.script.Invocable");
 
            evalMethod = engineClass.getMethod("eval",
                                  new Class[] { String.class });
            invokeMethod = invocableClass.getMethod("invokeFunction",
                                  new Class[] { String.class, Object[].class });
 
            // initialize ScriptEngine.put(String, Object) method
            Method putMethod = engineClass.getMethod("put",
                                  new Class[] { String.class, Object.class });
 
            // call ScriptEngine.put to initialize built-in heap object
            putMethod.invoke(engine, new Object[] {
                        "heap", call("wrapHeapSnapshot", new Object[] { snapshot })
                    });
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
 
    private InputStream getInitStream() {
        return getClass().getResourceAsStream("/org/netbeans/modules/profiler/heapwalk/oql/resources/hat.js");
    }
 
    private Object engine;
    private Method evalMethod; 
    private Method invokeMethod; 
    private Snapshot snapshot;
    private static boolean debug = true;
    private static boolean oqlSupported;
}
