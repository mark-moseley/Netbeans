/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ForTask.java
 *
 * Created on November 1, 2001, 5:00 PM
 */

package org.netbeans.xtest.driver;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.util.StringTokenizer;
import java.util.LinkedList;
import java.util.List;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Property; 
import java.util.Iterator;
import java.io.File;
import org.apache.tools.ant.taskdefs.CallTarget;

/** Task which behave as for command. Task has two required attributes (list,property) 
 * and one optional (delimiters) and have nested elements ant and antcall.
 * 'For' task divides value of list attribute by delimiters and for every part
 * calls all nested elements with property set to the devided part of the list. 
 * Name of this property is specified by property attribute of for task.
 *
 * Example:
 *
 *  <for list="first,second,third" delimiters=",;" property="selected.part">
 *      <antcall target="{selected.part}_target"/>
 *      <ant target="sometarget">
 *        <property name="some.property" value="somevalue"/>
 *      </ant>
 *  </for>
 *
 * @author lm97939
 */
public class ForTask extends Task {

    private String list;
    private String property;
    private String delim = ",;: \t\n\r\f";
    private List antlist = new LinkedList(); 

    
    public void setList(String list) {
        this.list = list;
    }
    
    public void setProperty(String prop) {
        this.property = prop;
    }
    
    public void setDelimiters(String delim) {
        this.delim = delim;
    }

    public static class MyAnt {
        String antfile; 
        String target;
        File dir;
        List properties = new LinkedList(); 

        public void setTarget(String t) {
            target = t;
        }
        public void setAntfile(String f) {
            antfile = f;
        }
        public void setDir(File d) {
            dir = d;
        }
        public void addProperty(MyProperty p) {
            properties .add(p);
        }
    }
    
    public static class MyAntCall {
        String target;
        List properties = new LinkedList(); 

        public void setTarget(String t) {
            target = t;
        }

        public void addParam(MyProperty p) {
            properties .add(p);
        }
    }
    
    public static class MyProperty {
            String name;
            String value;
            
            public void setName(String n) {
                name = n;
            }
            public void setValue(String v) {
                value = v;
            }
    }
    
    public void addAnt(MyAnt a) {
        antlist.add(a);
    }
    
    public void addAntcall(MyAntCall a) {
        antlist.add(a);
    }

    public void execute() throws BuildException {
        if (list == null) throw new BuildException("Attribute 'list' is empty.");
        if (property == null) throw new BuildException("Attribute 'property' is empty.");
        
        StringTokenizer tokens = new StringTokenizer(list,delim);
        while (tokens.hasMoreTokens()) {
            String name = tokens.nextToken();
            Iterator it = antlist.iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof MyAnt) {
                    MyAnt a = (MyAnt)o;
                    Iterator prop = a.properties.iterator();
                    Ant ant = (Ant) project.createTask ("ant");
                    ant.init ();                
                    ant.setLocation (location);
                    ant.setTarget(a.target);
                    ant.setDir(a.dir);
                    ant.setAntfile(a.antfile);
                    Property p1 = ant.createProperty();
                    p1.setName(property);
                    p1.setValue(name);
                    while (prop.hasNext()) {
                        MyProperty myproperty = (MyProperty) prop.next();
                        Property p = ant.createProperty();
                        StringBuffer buff = new StringBuffer(myproperty.value);
                        int index = buff.toString().indexOf("${"+property+"}");
                        if (index != -1) 
                            buff.replace(index,index+property.length()+3,name);
                        p.setName(myproperty.name);
                        p.setValue(buff.toString());
                    }
                    ant.execute();
                }
                else {
                  if (o instanceof MyAntCall) {
                      MyAntCall a = (MyAntCall)o;
                    Iterator prop = a.properties.iterator();
                    CallTarget ant = (CallTarget) project.createTask ("antcall");
                    ant.init ();                
                    ant.setLocation (location);
                    ant.setTarget(a.target);
                    Property p1 = ant.createParam();
                    p1.setName(property);
                    p1.setValue(name);
                    while (prop.hasNext()) {
                        MyProperty myproperty = (MyProperty) prop.next();
                        Property p = ant.createParam();
                        StringBuffer buff = new StringBuffer(myproperty.value);
                        int index = buff.toString().indexOf("${"+property+"}");
                        if (index != -1) 
                            buff.replace(index,index+property.length()+3,name);
                        p.setName(myproperty.name);
                        p.setValue(buff.toString());
                    }
                    ant.execute();
                  }
                  else {
                      throw new BuildException("Unknown nested element.");
                  }
                }
            }
        }
    }

}
