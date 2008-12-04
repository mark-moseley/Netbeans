/*
 * PythonTargetExecutor.java
*
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
*
*
*/

package org.netbeans.modules.python.debugger.spi;

import org.openide.execution.ExecutorTask;
import java.io.IOException ;
import java.io.OutputStream;

/**
 *
 * @author jean-yves
 */
public class PythonDebuggerTargetExecutor
{
  private final Env _env ;
  
  /** Creates a new instance of PythonTargetExecutor */
  public PythonDebuggerTargetExecutor( Env env )
  { _env = env ; }
  
  /** Factory method for creation of AntTargetExecutor with the given environment.
  * The factory does not clone Env what means that any change to Env will
  * influence the factory.
  * @param env a configuration for the executor
  * @return an executor which can run projects with the given configuration
  */
  public static PythonDebuggerTargetExecutor createTargetExecutor(Env env)
  { return new PythonDebuggerTargetExecutor(env); }

  
  /** Execute given target(s).
  * <p>The {@link AntProjectCookie#getFile} must not be null, since Ant can only
  * run files present on disk.</p>
  * <p>The returned task may be used to wait for completion of the script
  * and check result status.</p>
  * <p class="nonnormative">
  * The easiest way to get the project cookie is to get a <code>DataObject</code>
  * representing an Ant build script and to ask it for this cookie. Alternatively,
  * you may implement the cookie interface directly, where
  * <code>getFile</code> is critical and other methods may do nothing
  * (returning <code>null</code> as needed).
  * While the specification for <code>AntProjectCookie</code> says that
  * <code>getDocument</code> and <code>getParseException</code> cannot
  * both return <code>null</code> simultaneously, the <em>current</em>
  * executor implementation does not care; to be safe, return an
  * {@link UnsupportedOperationException} from <code>getParseException</code>.
  * </p>
  * @param antProject a representation of the project to run
  * @param targets non-empty list of target names to run; may be null to indicate default target
  * @return task for tracking of progress of execution
  * @throws IOException if there is a problem running the script
  */
  public ExecutorTask execute( PythonSourceDebuggee pyProject  ) throws IOException
  {
  TargetExecutor te = new TargetExecutor( pyProject );
    //te.setVerbosity(env.getVerbosity());
    //te.setProperties(env.getProperties());
    if ( _env.getLogger() == null) 
    {
       return te.execute();
    } else 
    {
      return te.execute(_env.getLogger() );
    }
  }
  
  /** 
   Class describing the environment in which the Python script will be executed.
  */
  final public static class Env 
  {
    private OutputStream _outputStream = null ;

 
    /** Create instance of Env class describing environment for Ant target execution.
     */
    public Env() 
    {}
    
    public OutputStream getLogger()
    { return _outputStream ; }
    public void getLogger( OutputStream logger )
    { _outputStream = logger ; }
    
  }  
}
