/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.suites.focus.data.testfiles.FocusTestPerformer;

import org.netbeans.test.editor.app.gui.*;
import java.beans.*;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import org.netbeans.test.editor.app.util.Scheduler;
import org.netbeans.test.editor.app.core.TestAction;
import org.w3c.dom.Element;

import java.util.Vector;
import java.util.Collection;

import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.core.properties.ArrayProperty;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.BooleanProperty;
import org.netbeans.test.editor.app.core.properties.IntegerProperty;
import org.netbeans.test.editor.app.core.properties.MultiLineStringProperty;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.netbeans.test.editor.app.gui.actions.TestDeleteAction;
import org.netbeans.test.editor.app.gui.actions.TestGrabInputAction;
import org.netbeans.test.editor.app.gui.actions.TestGrabOutputAction;
import org.netbeans.test.editor.app.gui.tree.ActionsCache;
import org.netbeans.test.editor.app.util.ParsingUtils;

/**
 *
 * @author  ehucka
 * @version
 */
public class Test extends TestAction {
    
    public static final String INPUT="Input";
    public static final String OUTPUT="Output";
    public static final String COMMENT="Comment";
    public static final String TOCALL="Call";
    public static final String TOSET="Set";
    public static final String ENABLE="Enable";
    public static final String REPEAT="Repeat";
    public static final String LOGGERDELAY="Delay";
    
    private String input,output,toCall,toSet,comment;
    private int repeat,loggerDelay;
    
    private boolean enable;
    /** Creates new TestCallAction */
    
    public Test(int num) {
        this("call"+Integer.toString(num));
    }
    
    public Test(String name) {
        super(name);
        input="";
        output="";
        toCall="";
        toSet="";
        comment="";
        enable=true;
        loggerDelay=50;
        repeat=1;
    }
    
    public Test(Element node) {
        super(node);
        
        if ((input = ParsingUtils.loadString(node, INPUT)) == null) {
            input="";
        }
        if ((output = ParsingUtils.loadString(node, OUTPUT)) == null) {
            output="";
        }
        if ((toCall = ParsingUtils.loadString(node, TOCALL)) == null) {
            toCall = ParsingUtils.fromSafeString(node.getAttribute(TOCALL)); // backward compatibility
            if (toCall == null) {
                toCall="";
            }
        }
        if ((toSet = ParsingUtils.loadString(node, TOSET)) == null) {
            toSet = ParsingUtils.fromSafeString(node.getAttribute(TOSET)); // backward compatibility
            if (toSet == null) {
                toSet="";
            }
        }
        if ((comment = ParsingUtils.loadString(node, COMMENT)) == null) {
            comment="";
        }
        enable = ParsingUtils.readBoolean(node,ENABLE);
        repeat = ParsingUtils.parseInt(node.getAttribute(REPEAT),1);
        loggerDelay = ParsingUtils.parseInt(node.getAttribute(LOGGERDELAY),50);
    }
    
    public Element toXML(Element node) {
        node = super.toXML(node);
        node = ParsingUtils.saveString(node, INPUT, input);
        node = ParsingUtils.saveString(node, OUTPUT, output);
        node = ParsingUtils.saveString(node, COMMENT, comment);
        node.setAttribute(TOCALL, ParsingUtils.toSafeString(toCall));
        node.setAttribute(TOSET, ParsingUtils.toSafeString(toSet));
        node.setAttribute(ENABLE, enable ? "true" : "false");
        node.setAttribute(REPEAT, Integer.toString(repeat));
        node.setAttribute(LOGGERDELAY, Integer.toString(loggerDelay));
        return node;
    }
    
    public void setRepeat(int i) {
        int oldValue = repeat;
        repeat = i;
        firePropertyChange(REPEAT,new Integer(oldValue),new Integer(repeat));
    }
    
    public int getRepeat() {
        return repeat;
    }
    
    public void setLoggerDelay(int value) {
        int oldValue = repeat;
        loggerDelay = value;
        firePropertyChange(LOGGERDELAY,new Integer(oldValue),new Integer(loggerDelay));
    }
    
    public int getLoggerDelay() {
        return loggerDelay;
    }
    
    public void setInput(String value) {
        String oldValue = input;
        input = value;
        firePropertyChange(INPUT, oldValue, input);
    }
    
    public String getInput() {
        return input;
    }
    
    public void setOutput(String value) {
        String oldValue = output;
        output = value;
        firePropertyChange(OUTPUT, oldValue, output);
    }
    
    public String getOutput() {
        return output;
    }
    
    public void setToCall(String value) {
        String oldValue = toCall;
        toCall = value;
        firePropertyChange(TOCALL, oldValue, toCall);
    }
    
    public String getToCall() {
        return toCall;
    }
    
    public void setToSet(String value) {
        String oldValue = toSet;
        toSet = value;
        firePropertyChange(TOSET, oldValue, toSet);
    }
    
    public String getToSet() {
        return toSet;
    }
    
    public void setComment(String value) {
        String oldValue = comment;
        comment = value;
        firePropertyChange(COMMENT, oldValue, comment);
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setEnabled(boolean value) {
        boolean oldValue = enable;
        enable = value;
        firePropertyChange(ENABLE, oldValue ? Boolean.TRUE : Boolean.FALSE, enable ? Boolean.TRUE : Boolean.FALSE);
    }
    
    public boolean isEnable() {
        return enable;
    }
    
    private String[] getToCalls(boolean empty) {
        TestStep st;
        ArrayList lst=new ArrayList();
        
        if (empty)
            lst.add("");
        for(int i=0;i < owner.getChildCount();i++) {
            if (owner.get(i) instanceof TestStep) {
                st=(TestStep)(owner.get(i));
                lst.add(st.getName());
            }
        }
        return (String[])(lst.toArray(new String[] {}));
    }
    
    private TestStep readStepToCall(String toCall) {
        for(int i=0;i < owner.getChildCount();i++) {
            TestNode n = owner.get(i);
            if (n == null)
                System.err.println("Node: "+toCall+" got from owner is null!");
            else {
                if (n.getName().equals(toCall) && n instanceof TestStep) {
                    return (TestStep)n;
                }
            }
        }
        return null;
    }
    
    public void grabInput() {
        Scheduler.getDefault().addTask(new Thread() {
            public void run() {
                String old=input;
                input=Main.frame.getEditor().getText();
                firePropertyChange(INPUT,old ,input );
            }
        });
    }
    
    public void grabOutput() {
        Scheduler.getDefault().addTask(new Thread() {
            public void run() {
                String old=output;
                output=Main.frame.getEditor().getText();
                firePropertyChange(INPUT,old ,output );
            }
        });
    }
    
    long time=0;
    long memory=0;
    
    public void perform() {
        if (!enable) return;
        System.err.println("Call action: "+name+" starts performing.");
        System.err.println(">>>>>Comment: "+comment);
        isPerforming=true;
        
        //Scheduler.getDefault().addTask(new Thread() {
        new Thread() {
            public void run() {
                if (!enable) return;
                TestStep call;
                TestStep set;
                Main.frame.getEditor().grabFocus();
                Main.frame.getEditor().requestFocus();
                call=readStepToCall(toCall);
                set=readStepToCall(toSet);
                if (set != null) {
                    set.perform();
                }
                if (call != null) {
                    getLogger().setDelay(getLoggerDelay());
                    time=System.currentTimeMillis();
                    memory=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
                    for(int i=0;i < repeat;i++) {
                        getLogger().clear();
                        getLogger().loadActions(call);
                        Main.frame.getEditor().setText(input);
                        if (i == repeat-1) {
                            getLogger().addPropertyChangeListener(new PropertyChangeListener() {
                                public void propertyChange(final java.beans.PropertyChangeEvent p1) {
                                    if (p1.getPropertyName().compareTo(Logger.PERFORMING) == 0) {
                                        if (!((Boolean)(p1.getNewValue())).booleanValue()) {
                                            time=System.currentTimeMillis()-time;
                                            memory=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()-memory;
                                            String content=Main.frame.getEditor().getText();
                                            System.err.println("Call action: "+name+" finished performing");
                                            if (!Test.isTesting()) {
                                                if (content.compareTo(output) != 0 )
                                                    System.err.println("***************** Call action: "+name+" error in outputs comparation. ******************");
                                            } else {
                                                System.out.print(content);
                                            }
                                            System.err.println("Test time="+(time/1000.0)+" (s).");
                                            System.err.println("Test memory="+(memory/1024.0)+" (KB).");
                                            getLogger().removePropertyChangeListener(this);
                                            isPerforming=false;
                                        }
                                    }
                                }
                            });
                        } else {
                            getLogger().addPropertyChangeListener(new PropertyChangeListener() {
                                public void propertyChange(final java.beans.PropertyChangeEvent p1) {
                                    if (p1.getPropertyName().compareTo(Logger.PERFORMING) == 0) {
                                        if (!((Boolean)(p1.getNewValue())).booleanValue()) {
                                            getLogger().removePropertyChangeListener(this);
                                            isPerforming=false;
                                        }
                                    }
                                }
                            });
                        }
                        isPerforming=true;
                        
                        getLogger().startPerforming();
                        long sleeps=0;
                        while (isPerforming && sleeps < 600000) {  //max 10 minutes waits
                            try {
                                Thread.currentThread().sleep(250);
                                sleeps+=250;
                            } catch (Exception ex) {
                            }
                        }
                        System.err.println("Sleeps="+sleeps);
                    }
                } else {
                    isPerforming = false;
                }
            }
            //});
        }.start();
    }
    
    private static final long TIMEOUT = 60 * 1000;
    
    public void performAndWait() {
        perform();
        
        long time = System.currentTimeMillis();
        
        while (isPerforming) {
            long actualTime = System.currentTimeMillis();
            
            if ((actualTime - time) > TIMEOUT) {
                return;
            }
            Thread.yield();
        }
    }
    
    public Vector getPerformedActions() {
        TestStep set = readStepToCall(toSet);
        TestStep call = readStepToCall(toCall);
        Collection setActions = set == null ? new Vector(0) : set.getChildNodes();
        Collection callActions = call == null ? new Vector(0) : call.getChildNodes();
        Vector res = new Vector(setActions);
        
        res.addAll(callActions);
        return res;
    }
    
    public void stop() {
        getLogger().stopPerforming();
    }
    
    public void fromXML(Element node) throws BadPropertyNameException {
        super.fromXML(node);
        if ((input = ParsingUtils.loadString(node, INPUT)) == null) {
            input="";
        }
        if ((output = ParsingUtils.loadString(node, OUTPUT)) == null) {
            output="";
        }
        if ((toCall = ParsingUtils.loadString(node, TOCALL)) == null) {
            toCall = ParsingUtils.fromSafeString(node.getAttribute(TOCALL)); // backward compatibility
            if (toCall == null) {
                toCall="";
            }
        }
        if ((toSet = ParsingUtils.loadString(node, TOSET)) == null) {
            toSet = ParsingUtils.fromSafeString(node.getAttribute(TOSET)); // backward compatibility
            if (toSet == null) {
                toSet="";
            }
        }
        if ((comment = ParsingUtils.loadString(node, COMMENT)) == null) {
            comment="";
        }
        enable = ParsingUtils.readBoolean(node,ENABLE);
        repeat = ParsingUtils.parseInt(node.getAttribute(REPEAT),1);
        loggerDelay = ParsingUtils.parseInt(node.getAttribute(LOGGERDELAY),50);
    }
    
    public Properties getProperties() {
        Properties ret=super.getProperties();
        ret.put(INPUT, new MultiLineStringProperty(input));
        ret.put(OUTPUT, new MultiLineStringProperty(output));
        ret.put(TOCALL, new ArrayProperty(toCall,getToCalls(false)));
        ret.put(TOSET, new ArrayProperty(toSet,getToCalls(true)));
        ret.put(COMMENT, new MultiLineStringProperty(comment));
        ret.put(ENABLE, new BooleanProperty(enable));
        ret.put(REPEAT, new IntegerProperty(repeat));
        ret.put(LOGGERDELAY, new IntegerProperty(loggerDelay));
        return ret;
    }
    
    public Object getProperty(String name) throws BadPropertyNameException {
        if (name.compareTo(INPUT) == 0) {
            return new MultiLineStringProperty(input);
        } else if (name.compareTo(OUTPUT) == 0) {
            return new MultiLineStringProperty(output);
        } else if (name.compareTo(TOCALL) == 0) {
            return new ArrayProperty(toCall,getToCalls(false));
        } else if (name.compareTo(TOSET) == 0) {
            return new ArrayProperty(toSet,getToCalls(false));
        } else if (name.compareTo(COMMENT) == 0) {
            return new MultiLineStringProperty(comment);
        } else if (name.compareTo(ENABLE) == 0) {
            return new BooleanProperty(enable);
        } else if (name.compareTo(REPEAT) == 0) {
            return new IntegerProperty(repeat);
        } else if (name.compareTo(LOGGERDELAY) == 0) {
            return new IntegerProperty(loggerDelay);
        } else {
            return super.getProperty(name);
        }
    }
    
    public void setProperty(String name, Object value)  throws BadPropertyNameException {
        if (value == null) {
            throw new NullPointerException();
        } else if (name.compareTo(INPUT) == 0) {
            setInput(((MultiLineStringProperty)(value)).getProperty());
        } else if (name.compareTo(OUTPUT) == 0) {
            setOutput(((MultiLineStringProperty)(value)).getProperty());
        } else if (name.compareTo(TOCALL) == 0) {
            setToCall(((ArrayProperty)(value)).getProperty());
        } else if (name.compareTo(TOSET) == 0) {
            setToSet(((ArrayProperty)(value)).getProperty());
        } else if (name.compareTo(COMMENT) == 0) {
            setComment(((MultiLineStringProperty)(value)).getProperty());
        } else if (name.compareTo(ENABLE) == 0) {
            setEnabled(((BooleanProperty)value).getValue());
        } else if (name.compareTo(REPEAT) == 0) {
            setRepeat(((IntegerProperty)(value)).getValue());
        } else if (name.compareTo(LOGGERDELAY) == 0) {
            setLoggerDelay(((IntegerProperty)(value)).getValue());
        } else {
            super.setProperty(name, value);
        }
    }
    
    protected void registerActions() {
        super.registerActions();
        ActionsCache.getDefault().addNodeAction(getClass(), new TestGrabInputAction());
        ActionsCache.getDefault().addNodeAction(getClass(), new TestGrabOutputAction());
    }
}
