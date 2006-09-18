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

import java.util.Arrays;
import java.util.Map;
import junit.framework.TestCase;
import org.netbeans.spi.sendopts.OptionGroups;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;

/** Test for option alterning for each other.
 *
 * @author Jaroslav Tulach
 */
public class OneOfComplexTest extends TestCase implements Processor {
    protected CommandLine l;
    private TuneProc tuneProc = new TuneProc();
    private Option tune;
    private Option station;
    private TuneProc stationProc = new TuneProc();
    private Option channel;
    private Option stream;
    private Option record;
    
    protected String valueChannel;
    protected String valueStation;
    protected String valueStream;
    protected String valueTune;
    
    public OneOfComplexTest(String s) {
        super(s);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected void setUp() throws Exception {
        Provider.clearAll();
        
        tune = Option.requiredArgument((char)-1, "tune");
        Provider.add(tuneProc, tune);
        station = Option.requiredArgument((char)-1, "station");
        Provider.add(stationProc, station);
        channel = defineOneOf(tune, station);
            
        stream = Option.requiredArgument((char)-1, "stream");
        
        record = OptionGroups.allOf(channel, stream);
        Provider.add(this, record);
        
        l = CommandLine.getDefault();
    }

    protected Option defineOneOf(Option... arr) {
        return OptionGroups.oneOf(arr);
    }
    
    public void testNothingIsGood() throws Exception {
        l.process(new String[0]); // ok
    }

    public void testTuneWithoutStreamIsBad() throws Exception {
        try {
            l.process(new String[] { "--stream", "10" });
            fail("We need --tune");
        } catch (CommandException ex) {
            // ok
        }
    }
    
    public void testTuneIsOk() throws Exception {
        l.process(new String[] { "--stream", "x.mpeg", "--tune", "10"  });
        
        assertEquals("Tune is 10", "10", tuneProc.value);
        assertEquals("Tune is the option", tune, tuneProc.option);
        
        assertEquals("Value1 is 10", "10", valueTune);
        assertEquals("Value2 is x.mpeg", "x.mpeg", valueStream);
        
    }

    public void testStationIsOk() throws Exception {
        l.process(new String[] { "--station", "Radio1", "--stream", "y.mpeg"});
        
        assertEquals("Station is ok", "Radio1", stationProc.value);
        assertEquals("Station is the option", station, stationProc.option);
        
        assertEquals("Value1 is Radio1", "Radio1", valueStation);
        assertEquals("Value2 is mpeg", "y.mpeg", valueStream);
    }

    public void process(Env env, Map<Option, String[]> values) throws CommandException {
        this.valueChannel = getString(values.get(channel));
        this.valueStream = getString(values.get(stream));
        this.valueStation = getString(values.get(station));
        this.valueTune = getString(values.get(tune));
    }
    
    private static String getString(String[] arr) {
        if (arr == null) {
            return null;
        }
        if (arr.length > 1) {
            fail("Too long: " + Arrays.asList(arr));
        }
        return arr.length == 1 ? arr[0] : null;
    }
    
    static final class TuneProc implements Processor {
        Option option;
        String value;

        public void process(Env env, Map<Option, String[]> values) throws CommandException {
            assertNull("Not processed yet", this.option);
            assertEquals("An option is provided", 1, values.size());
            this.option = values.keySet().iterator().next();
            this.value = values.values().iterator().next()[0];
            assertNotNull("A value is here", value);
        }
    }
}
