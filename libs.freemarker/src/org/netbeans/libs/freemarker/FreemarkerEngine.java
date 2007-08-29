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
 * The Original Software is scripting.dev.java.net. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. 
 * 
 * Portions Copyrighted 2006 Sun Microsystems, Inc.
 */

package org.netbeans.libs.freemarker;


import javax.script.*;
import java.io.*;
import java.util.Properties;
import java.util.Set;
import freemarker.template.*;
import java.util.Map;
import java.util.WeakHashMap;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;

/* Taken from A. Sundararajan and adopted by Jaroslav Tulach 
 * for NetBeans needs.
 * 
 * @author A. Sundararajan
 */
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
class FreemarkerEngine extends AbstractScriptEngine {

    public static final String STRING_OUTPUT_MODE = "com.sun.script.freemarker.stringOut";
    public static final String FREEMARKER_CONFIG = "com.sun.script.freemarker.config";
    public static final String FREEMARKER_PROPERTIES = "com.sun.script.freemarker.properties";
    public static final String FREEMARKER_TEMPLATE_DIR = "com.sun.script.freemarker.template.dir";
    public static final String FREEMARKER_TEMPLATE = "org.openide.filesystems.FileObject";
    
    private static Map<FileObject,Template> templates = new WeakHashMap<FileObject, Template>();

    // my factory, may be null
    private volatile ScriptEngineFactory factory;
    private volatile Configuration conf;
    private volatile FileObject fo;

    public FreemarkerEngine(ScriptEngineFactory factory) {
        this.factory = factory;
    }   

    public FreemarkerEngine() {
        this(null);
    }
	
    // ScriptEngine methods
    public Object eval(String str, ScriptContext ctx) 
                       throws ScriptException {	
        return eval(new StringReader(str), ctx);
    }

    public Object eval(Reader reader, ScriptContext ctx)
                       throws ScriptException { 
        ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);
        initFreeMarkerConfiguration(ctx);
        String fileName = getFilename(ctx);
        boolean outputAsString = isStringOutputMode(ctx);
        Writer out;
        if (outputAsString) {
            out = new StringWriter();
        } else {
            out = ctx.getWriter();
        }
        
        Template template = null;
        try {
            if (fo != null) {
                template = templates.get(fo);
            }
            
            if (template == null) {
                template = new MyTemplate(fo, fileName, reader, conf);
                if (fo != null) {
                    templates.put(fo, template);
                }
            }
            template.process(null, out);
            out.flush();
        } catch (Exception exp) {
            throw new ScriptException(exp);
        }
        return outputAsString? out.toString() : null;
    }

    public ScriptEngineFactory getFactory() {
        synchronized (this) {
            if (factory == null) {
                factory = new FreemarkerFactory();
            }
        }
        return factory;
    }

    public Bindings createBindings() {
        return new SimpleBindings();
    }

    // internals only below this point  
    private static String getFilename(ScriptContext ctx) {
        Object tfo = ctx.getAttribute(FREEMARKER_TEMPLATE);
        if (tfo instanceof FileObject) {
            return ((FileObject)tfo).getPath();
        }
        Object fileName = ctx.getAttribute(ScriptEngine.FILENAME);
        if (fileName != null) {
            return fileName.toString();
        }
        return "unknown";
    }

    private static boolean isStringOutputMode(ScriptContext ctx) {
        Object flag = ctx.getAttribute(STRING_OUTPUT_MODE);
        if (flag != null) {
            return flag.equals(Boolean.TRUE);
        } else {
            return false;
        }
    }

    private void initFreeMarkerConfiguration(ScriptContext ctx) {
        if (conf == null) {
            synchronized (this) {
                if (conf != null) {
                    return;
                }
                Object cfg = ctx.getAttribute(FREEMARKER_CONFIG);
                if (cfg instanceof Configuration) {
                    conf = (Configuration) cfg;
                    return;
                }

                Object tfo = ctx.getAttribute(FREEMARKER_TEMPLATE);
                fo = tfo instanceof FileObject ? (FileObject)tfo : null;
                
                Configuration tmpConf = new RsrcLoader(fo, ctx);
                try {
                    initConfProps(tmpConf, ctx);
                    initTemplateDir(tmpConf, fo, ctx);
                } catch (RuntimeException rexp) {
                    throw rexp;
                } catch (Exception exp) {
                    throw new RuntimeException(exp);
                }
                conf = tmpConf;
            }
        }
    }    

    private static void initConfProps(Configuration conf, ScriptContext ctx) {         
        try {
            Properties props = null;
            Object tmp = ctx.getAttribute(FREEMARKER_PROPERTIES);
            if (props instanceof Properties) {
                props = (Properties) tmp;
            } else {
                String propsName = System.getProperty(FREEMARKER_PROPERTIES);
                if (propsName != null) {                    
                    File propsFile = new File(propsName);
                    if (propsFile.exists() && propsFile.canRead()) {
                        props = new Properties();
                        props.load(new FileInputStream(propsFile));
                    }               
                }
            }
            if (props != null) {
                Set<Object> keys = props.keySet();
                for (Object obj : keys) {
                    String key;
                    if (obj instanceof String) {
                        key = (String) obj;
                    } else {
                        continue;
                    }
                    try {
                        conf.setSetting(key, props.get(key).toString());
                    } catch (TemplateException te) {
                        // ignore
                    }
                }
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    private static void initTemplateDir(Configuration conf, FileObject fo, ScriptContext ctx) {
        try {
            Object tmp = ctx.getAttribute(FREEMARKER_TEMPLATE_DIR);
            String dirName;
            if (tmp != null) {
                dirName = tmp.toString();
            } else {
                if (fo != null) {
                    return;
                }
                tmp = System.getProperty(FREEMARKER_TEMPLATE_DIR);
                dirName = (tmp == null)? "." : tmp.toString();
            }
            File dir = new File(dirName);
            if (dir.exists() && dir.isDirectory()) {
                conf.setDirectoryForTemplateLoading(dir);
            }
        } catch (IOException exp) {
            throw new RuntimeException(exp);
        }
    }
    
    private static final class MyTemplate extends Template 
    implements FileChangeListener {
        public MyTemplate(FileObject fo, String s, Reader r, Configuration c) throws IOException {
            super(s, r, c);
            fo.addFileChangeListener(FileUtil.weakFileChangeListener(this, fo));
        }
        
        public void fileFolderCreated(FileEvent fe) {
            clear();
        }
        public void fileDataCreated(FileEvent fe) {
            clear();
        }
        public void fileChanged(FileEvent fe) {
            clear();
        }
        public void fileDeleted(FileEvent fe) {
            clear();
        }
        public void fileRenamed(FileRenameEvent fe) {
            clear();
        }
        public void fileAttributeChanged(FileAttributeEvent fe) {
            clear();
        }
        private void clear() {
            templates.clear();
        }
    } // end of MyTemplate
    
    
    
    
}
