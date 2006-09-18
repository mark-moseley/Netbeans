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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.sendopts;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import junit.framework.TestCase;
import org.netbeans.spi.sendopts.OptionGroups;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;

/** Checks that error reports from the system are sane.
 *
 * @author Jaroslav Tulach
 */
public class ErrorMessagesTest extends TestCase 
implements Processor {
    private CommandLine l;

    private ArrayList<Option> options = new ArrayList<Option>();
    
    public ErrorMessagesTest(String s) {
        super(s);
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

    protected void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        
        
        Option no = Option.withoutArgument((char)-1, "no");
        Option open = Option.additionalArguments('o', "open");
        Option close = Option.additionalArguments('c', "close");
        Option one = Option.requiredArgument('1', "one");
        Option two = Option.requiredArgument('2', "two");
        Option optional = Option.optionalArgument((char)-1, "option");
        Option both = OptionGroups.allOf(no, one);
        Option bothDef = OptionGroups.someOf(both, open);
        Option allOf = OptionGroups.oneOf(open, two, both);
        
        Provider.clearAll();
        Provider.add(this, no, open, close, one, two, optional, both, bothDef, allOf);
        
        l = CommandLine.getDefault();
    }
    
    public void testMissingArgument() {
        try {
            l.process(new String[] { "--one" });
            fail("This is going to fail");
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("Option --one") == -1) {
                fail(ex.getLocalizedMessage());
            }
            
            if (ex.getLocalizedMessage().indexOf("needs") == -1) {
                fail(ex.getLocalizedMessage());
            }

            if (ex.getLocalizedMessage().indexOf("argument") == -1) {
                fail(ex.getLocalizedMessage());
            }
        }
    }
    
    public void testShortMissingArgument() {
        try {
            l.process(new String[] { "-1" });
            fail("This is going to fail");
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("Option -1") == -1) {
                fail(ex.getLocalizedMessage());
            }
            
            if (ex.getLocalizedMessage().indexOf("needs") == -1) {
                fail(ex.getLocalizedMessage());
            }

            if (ex.getLocalizedMessage().indexOf("argument") == -1) {
                fail(ex.getLocalizedMessage());
            }
        }
    }
    
    public void testCannotBeUsedAtOnce() {
        try {
            l.process(new String[] { "-c", "-o" });
            fail("Cannot be used at once");
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("-c") == -1) {
                fail("-c should be there: " + ex.getLocalizedMessage());
            }
            if (ex.getLocalizedMessage().indexOf("-o") == -1) {
                fail("-o should be there: " + ex.getLocalizedMessage());
            }
        }
    }
    
    public void testCannotBeUsedAtOnce2() {
        try {
            l.process(new String[] { "--close", "-o" });
            fail("Cannot be used at once");
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("--close") == -1) {
                fail("-c should be there: " + ex.getLocalizedMessage());
            }
            if (ex.getLocalizedMessage().indexOf("-o") == -1) {
                fail("-o should be there: " + ex.getLocalizedMessage());
            }
        }
    }
    public void testCannotBeUsedAtOnce3() {
        try {
            l.process(new String[] { "--close", "--open" });
            fail("Cannot be used at once");
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("--close") == -1) {
                fail("-c should be there: " + ex.getLocalizedMessage());
            }
            if (ex.getLocalizedMessage().indexOf("--open") == -1) {
                fail("-o should be there: " + ex.getLocalizedMessage());
            }
        }
    }
    public void testCannotBeUsedAtOnce4() {
        try {
            l.process(new String[] { "-c", "--open" });
            fail("Cannot be used at once");
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("-c") == -1) {
                fail("-c should be there: " + ex.getLocalizedMessage());
            }
            if (ex.getLocalizedMessage().indexOf("--open") == -1) {
                fail("-o should be there: " + ex.getLocalizedMessage());
            }
        }
    }
    public void testNoOneCannotBeWithTwo() {
        try {
            l.process(new String[] { "--no", "--one", "anArg", "--two", "anotherArg" });
            fail("Cannot be used at once: " + options);
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("--two") == -1) {
                fail("--two should be there: " + ex.getLocalizedMessage());
            }
            if (ex.getLocalizedMessage().indexOf("--no") == -1) {
                fail("--no should be there: " + ex.getLocalizedMessage());
            }
        }
    }
    public void testNoOneCannotBeWithTwo2() {
        try {
            l.process(new String[] { "--no", "--one", "anArg", "-2anotherArg" });
            fail("Cannot be used at once: " + options);
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("-2") == -1) {
                fail("--two should be there: " + ex.getLocalizedMessage());
            }
            if (ex.getLocalizedMessage().indexOf("--no") == -1) {
                fail("--no should be there: " + ex.getLocalizedMessage());
            }
        }
    }

    public void process(Env env, Map<Option, String[]> values) throws CommandException {
        options.addAll(values.keySet());
    }
}
