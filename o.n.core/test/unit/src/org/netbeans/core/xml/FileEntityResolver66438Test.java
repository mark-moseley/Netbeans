/*
 * FileEntityResolver66438Test.java
 * JUnit based test
 *
 * Created on 23. listopad 2005, 9:39
 */

package org.netbeans.core.xml;

import java.io.IOException;
import java.util.Date;
import org.netbeans.core.LoggingTestCaseHid;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.*;
import org.openide.cookies.InstanceCookie;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/** Checks race condition in the Lkp.beforeLookup
 *
 * @author Jaroslav Tulach
 */
public class FileEntityResolver66438Test extends LoggingTestCaseHid {
    
    public FileEntityResolver66438Test(String testName) {
        super(testName);
    }

    public void testRaceCondition() throws Exception {
        registerIntoLookup(new ErrManager());
        
        // register Env as a handler for PublicIDs "-//NetBeans//Test//EN" which
        // is will contain the settings file we create
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        FileObject register = FileUtil.createData (root, "/xml/lookups/NetBeans/Test.instance");
        register.setAttribute("instanceCreate", Env.INSTANCE);
        assertTrue (register.getAttribute("instanceCreate") instanceof Environment.Provider);
        
        
        // prepare an object to ask him for cookie
        FileObject fo = FileEntityResolverDeadlock54971Test.createSettings (root, "x.settings");
        final DataObject obj = DataObject.find (fo);

        class QueryIC implements Runnable {
            public InstanceCookie ic;
            
            public void run() {
                ic = (InstanceCookie)obj.getCookie(InstanceCookie.class);
            }
        }
        
        QueryIC i1 = new QueryIC();
        QueryIC i2 = new QueryIC();
                
        RequestProcessor.Task t1 = new RequestProcessor("t1").post(i1);
        RequestProcessor.Task t2 = new RequestProcessor("t2").post(i2);
        
        t1.waitFinished();
        t2.waitFinished();
        
        assertEquals("First has cookie", Env.INSTANCE, i1.ic);
        assertEquals("Second has cookie", Env.INSTANCE, i2.ic);
    }
    
    private static final class ErrManager extends ErrorManager {
        private boolean block;
        
        public Throwable attachAnnotations(Throwable t, ErrorManager.Annotation[] arr) {
            return null;
        }

        public ErrorManager.Annotation[] findAnnotations(Throwable t) {
            return null;
        }

        public Throwable annotate(Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
            return null;
        }

        public void notify(int severity, Throwable t) {
        }

        public void log(int severity, String s) {
            if (block && s.indexOf("change the lookup") >= 0) {
                block = false;
                ErrorManager.getDefault().log("Going to sleep");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                ErrorManager.getDefault().log("Done sleeping");
            }
        }

        public ErrorManager getInstance(String name) {
            if (name.equals("org.netbeans.core.xml.FileEntityResolver")) {
                ErrManager e = new ErrManager();
                e.block = true;
                return e;
            }
            return this;
        }
        
    }

    private static final class Env 
    implements InstanceCookie, org.openide.loaders.Environment.Provider {
        public static int howManyTimesWeHandledRequestForEnvironmentOfOurObject;
        public static final Env INSTANCE = new Env ();
        
        private Env () {
            assertNull (INSTANCE);
        }

        public String instanceName() {
            return getClass ().getName();
        }

        public Object instanceCreate() throws IOException, ClassNotFoundException {
            return this;
        }

        public Class instanceClass() throws IOException, ClassNotFoundException {
            return getClass ();
        }

        public Lookup getEnvironment(DataObject obj) {
            return Lookups.singleton(this);
        }
        
    }
    
}
