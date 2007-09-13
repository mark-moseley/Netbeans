#Signature file v4.0
#Version 

CLSS public abstract interface java.io.Serializable

CLSS public java.lang.Exception
cons public Exception()
cons public Exception(java.lang.String)
cons public Exception(java.lang.String,java.lang.Throwable)
cons public Exception(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

CLSS public java.lang.Object
cons public Object()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public java.lang.Throwable
cons public Throwable()
cons public Throwable(java.lang.String)
cons public Throwable(java.lang.String,java.lang.Throwable)
cons public Throwable(java.lang.Throwable)
intf java.io.Serializable
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object
hfds backtrace,cause,detailMessage,serialVersionUID,stackTrace

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public EventObject(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract org.netbeans.api.debugger.Breakpoint
cons public Breakpoint()
fld public final static java.lang.String PROP_DISPOSED = "disposed"
fld public final static java.lang.String PROP_ENABLED = "enabled"
fld public final static java.lang.String PROP_GROUP_NAME = "groupName"
fld public final static java.lang.String PROP_HIT_COUNT_FILTER = "hitCountFilter"
fld public final static java.lang.String PROP_VALIDITY = "validity"
innr public final static !enum HIT_COUNT_FILTERING_STYLE
innr public final static !enum VALIDITY
meth protected final void setValidity(org.netbeans.api.debugger.Breakpoint$VALIDITY,java.lang.String)
meth protected void dispose()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract boolean isEnabled()
meth public abstract void disable()
meth public abstract void enable()
meth public final int getHitCountFilter()
meth public final java.lang.String getValidityMessage()
meth public final org.netbeans.api.debugger.Breakpoint$HIT_COUNT_FILTERING_STYLE getHitCountFilteringStyle()
meth public final org.netbeans.api.debugger.Breakpoint$VALIDITY getValidity()
meth public final void setHitCountFilter(int,org.netbeans.api.debugger.Breakpoint$HIT_COUNT_FILTERING_STYLE)
meth public java.lang.String getGroupName()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void setGroupName(java.lang.String)
supr java.lang.Object
hfds groupName,hitCountFilter,hitCountFilteringStyle,pcs,validity,validityMessage

CLSS public abstract org.netbeans.api.debugger.jpda.AbstractDICookie
cons public AbstractDICookie()
meth public abstract com.sun.jdi.VirtualMachine getVirtualMachine() throws com.sun.jdi.connect.IllegalConnectorArgumentsException,com.sun.jdi.connect.VMStartException,java.io.IOException
supr java.lang.Object

CLSS public final org.netbeans.api.debugger.jpda.AttachingDICookie
fld public final static java.lang.String ID = "netbeans-jpda-AttachingDICookie"
meth public com.sun.jdi.VirtualMachine getVirtualMachine() throws com.sun.jdi.connect.IllegalConnectorArgumentsException,java.io.IOException
meth public com.sun.jdi.connect.AttachingConnector getAttachingConnector()
meth public int getPortNumber()
meth public java.lang.String getHostName()
meth public java.lang.String getSharedMemoryName()
meth public java.util.Map<java.lang.String,? extends com.sun.jdi.connect.Connector$Argument> getArgs()
meth public static org.netbeans.api.debugger.jpda.AttachingDICookie create(com.sun.jdi.connect.AttachingConnector,java.util.Map<java.lang.String,? extends com.sun.jdi.connect.Connector$Argument>)
meth public static org.netbeans.api.debugger.jpda.AttachingDICookie create(java.lang.String)
meth public static org.netbeans.api.debugger.jpda.AttachingDICookie create(java.lang.String,int)
supr org.netbeans.api.debugger.jpda.AbstractDICookie
hfds args,attachingConnector

CLSS public abstract interface org.netbeans.api.debugger.jpda.CallStackFrame
meth public abstract boolean isObsolete()
meth public abstract int getLineNumber(java.lang.String)
meth public abstract java.lang.String getClassName()
meth public abstract java.lang.String getDefaultStratum()
meth public abstract java.lang.String getMethodName()
meth public abstract java.lang.String getSourceName(java.lang.String) throws com.sun.jdi.AbsentInformationException
meth public abstract java.lang.String getSourcePath(java.lang.String) throws com.sun.jdi.AbsentInformationException
meth public abstract java.util.List<java.lang.String> getAvailableStrata()
meth public abstract org.netbeans.api.debugger.jpda.JPDAThread getThread()
meth public abstract org.netbeans.api.debugger.jpda.LocalVariable[] getLocalVariables() throws com.sun.jdi.AbsentInformationException
meth public abstract org.netbeans.api.debugger.jpda.This getThisVariable()
meth public abstract org.netbeans.spi.debugger.jpda.EditorContext$Operation getCurrentOperation(java.lang.String)
meth public abstract void makeCurrent()
meth public abstract void popFrame()

CLSS public final org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint
fld public final static int TYPE_CLASS_LOADED = 1
fld public final static int TYPE_CLASS_LOADED_UNLOADED = 3
fld public final static int TYPE_CLASS_UNLOADED = 2
fld public final static java.lang.String PROP_BREAKPOINT_TYPE = "breakpointType"
fld public final static java.lang.String PROP_CLASS_EXCLUSION_FILTERS = "classExclusionFilters"
fld public final static java.lang.String PROP_CLASS_FILTERS = "classFilters"
meth public int getBreakpointType()
meth public java.lang.String toString()
meth public java.lang.String[] getClassExclusionFilters()
meth public java.lang.String[] getClassFilters()
meth public static org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint create(int)
meth public static org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint create(java.lang.String,boolean,int)
meth public void setBreakpointType(int)
meth public void setClassExclusionFilters(java.lang.String[])
meth public void setClassFilters(java.lang.String[])
supr org.netbeans.api.debugger.jpda.JPDABreakpoint
hfds classExclusionFilters,classFilters,type

CLSS public abstract interface org.netbeans.api.debugger.jpda.ClassVariable
intf org.netbeans.api.debugger.jpda.ObjectVariable
meth public abstract org.netbeans.api.debugger.jpda.JPDAClassType getClassType()

CLSS public org.netbeans.api.debugger.jpda.DebuggerStartException
cons public DebuggerStartException(java.lang.String)
cons public DebuggerStartException(java.lang.Throwable)
meth public java.lang.Throwable getTargetException()
supr java.lang.Exception
hfds throwable

CLSS public final org.netbeans.api.debugger.jpda.ExceptionBreakpoint
fld public final static int TYPE_EXCEPTION_CATCHED = 1
fld public final static int TYPE_EXCEPTION_CATCHED_UNCATCHED = 3
fld public final static int TYPE_EXCEPTION_UNCATCHED = 2
fld public final static java.lang.String PROP_CATCH_TYPE = "catchType"
fld public final static java.lang.String PROP_CLASS_EXCLUSION_FILTERS = "classExclusionFilters"
fld public final static java.lang.String PROP_CLASS_FILTERS = "classFilters"
fld public final static java.lang.String PROP_CONDITION = "condition"
fld public final static java.lang.String PROP_EXCEPTION_CLASS_NAME = "exceptionClassName"
meth public int getCatchType()
meth public java.lang.String getCondition()
meth public java.lang.String getExceptionClassName()
meth public java.lang.String toString()
meth public java.lang.String[] getClassExclusionFilters()
meth public java.lang.String[] getClassFilters()
meth public static org.netbeans.api.debugger.jpda.ExceptionBreakpoint create(java.lang.String,int)
meth public void setCatchType(int)
meth public void setClassExclusionFilters(java.lang.String[])
meth public void setClassFilters(java.lang.String[])
meth public void setCondition(java.lang.String)
meth public void setExceptionClassName(java.lang.String)
supr org.netbeans.api.debugger.jpda.JPDABreakpoint
hfds catchType,classExclusionFilters,classFilters,condition,exceptionClassName

CLSS public abstract interface org.netbeans.api.debugger.jpda.Field
intf org.netbeans.api.debugger.jpda.Variable
meth public abstract boolean isStatic()
meth public abstract java.lang.String getClassName()
meth public abstract java.lang.String getDeclaredType()
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.api.debugger.jpda.JPDAClassType getDeclaringClass()
meth public abstract void setValue(java.lang.String) throws org.netbeans.api.debugger.jpda.InvalidExpressionException

CLSS public org.netbeans.api.debugger.jpda.FieldBreakpoint
fld public final static int TYPE_ACCESS = 1
fld public final static int TYPE_MODIFICATION = 2
fld public final static java.lang.String PROP_BREAKPOINT_TYPE = "breakpointType"
fld public final static java.lang.String PROP_CLASS_NAME = "className"
fld public final static java.lang.String PROP_CONDITION = "condition"
fld public final static java.lang.String PROP_FIELD_NAME = "fieldName"
fld public final static java.lang.String PROP_INSTANCE_FILTERS = "instanceFilters"
fld public final static java.lang.String PROP_THREAD_FILTERS = "threadFilters"
meth public int getBreakpointType()
meth public java.lang.String getClassName()
meth public java.lang.String getCondition()
meth public java.lang.String getFieldName()
meth public java.lang.String toString()
meth public org.netbeans.api.debugger.jpda.JPDAThread[] getThreadFilters(org.netbeans.api.debugger.jpda.JPDADebugger)
meth public org.netbeans.api.debugger.jpda.ObjectVariable[] getInstanceFilters(org.netbeans.api.debugger.jpda.JPDADebugger)
meth public static org.netbeans.api.debugger.jpda.FieldBreakpoint create(java.lang.String,java.lang.String,int)
meth public void setBreakpointType(int)
meth public void setClassName(java.lang.String)
meth public void setCondition(java.lang.String)
meth public void setFieldName(java.lang.String)
meth public void setInstanceFilters(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.ObjectVariable[])
meth public void setThreadFilters(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.JPDAThread[])
supr org.netbeans.api.debugger.jpda.JPDABreakpoint
hfds className,condition,fieldName,instanceFilters,threadFilters,type
hcls FieldBreakpointImpl

CLSS public org.netbeans.api.debugger.jpda.InvalidExpressionException
cons public InvalidExpressionException(java.lang.String)
cons public InvalidExpressionException(java.lang.Throwable)
meth public java.lang.Throwable getTargetException()
supr java.lang.Exception
hfds throwable

CLSS public abstract interface org.netbeans.api.debugger.jpda.JPDAArrayType
intf org.netbeans.api.debugger.jpda.JPDAClassType
meth public abstract java.lang.String getComponentTypeName()
meth public abstract org.netbeans.api.debugger.jpda.VariableType getComponentType()

CLSS public org.netbeans.api.debugger.jpda.JPDABreakpoint
fld public final static int SUSPEND_ALL = 2
fld public final static int SUSPEND_EVENT_THREAD = 1
fld public final static int SUSPEND_NONE = 0
fld public final static java.lang.String PROP_HIDDEN = "hidden"
fld public final static java.lang.String PROP_PRINT_TEXT = "printText"
fld public final static java.lang.String PROP_SUSPEND = "suspend"
meth public boolean isEnabled()
meth public boolean isHidden()
meth public int getSuspend()
meth public java.lang.String getPrintText()
meth public void addJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public void disable()
meth public void enable()
meth public void removeJPDABreakpointListener(org.netbeans.api.debugger.jpda.event.JPDABreakpointListener)
meth public void setHidden(boolean)
meth public void setPrintText(java.lang.String)
meth public void setSuspend(int)
supr org.netbeans.api.debugger.Breakpoint
hfds breakpointListeners,enabled,hidden,printText,suspend

CLSS public abstract interface org.netbeans.api.debugger.jpda.JPDAClassType
intf org.netbeans.api.debugger.jpda.VariableType
meth public abstract java.lang.String getSourceName() throws com.sun.jdi.AbsentInformationException
meth public abstract java.util.List<org.netbeans.api.debugger.jpda.Field> staticFields()
meth public abstract java.util.List<org.netbeans.api.debugger.jpda.ObjectVariable> getInstances(long)
meth public abstract long getInstanceCount()
meth public abstract org.netbeans.api.debugger.jpda.ClassVariable classObject()
meth public abstract org.netbeans.api.debugger.jpda.ObjectVariable getClassLoader()
meth public abstract org.netbeans.api.debugger.jpda.Super getSuperClass()

CLSS public abstract org.netbeans.api.debugger.jpda.JPDADebugger
cons public JPDADebugger()
fld public final static int STATE_DISCONNECTED = 4
fld public final static int STATE_RUNNING = 2
fld public final static int STATE_STARTING = 1
fld public final static int STATE_STOPPED = 3
fld public final static int SUSPEND_ALL = 2
fld public final static int SUSPEND_EVENT_THREAD = 1
fld public final static java.lang.String ENGINE_ID = "netbeans-JPDASession/Java"
fld public final static java.lang.String PROP_CURRENT_CALL_STACK_FRAME = "currentCallStackFrame"
fld public final static java.lang.String PROP_CURRENT_THREAD = "currentThread"
fld public final static java.lang.String PROP_STATE = "state"
fld public final static java.lang.String PROP_SUSPEND = "suspend"
fld public final static java.lang.String SESSION_ID = "netbeans-JPDASession"
meth protected void fireBreakpointEvent(org.netbeans.api.debugger.jpda.JPDABreakpoint,org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent)
meth public abstract boolean canFixClasses()
meth public abstract boolean canPopFrames()
meth public abstract int getState()
meth public abstract int getSuspend()
meth public abstract org.netbeans.api.debugger.jpda.CallStackFrame getCurrentCallStackFrame()
meth public abstract org.netbeans.api.debugger.jpda.JPDAThread getCurrentThread()
meth public abstract org.netbeans.api.debugger.jpda.SmartSteppingFilter getSmartSteppingFilter()
meth public abstract org.netbeans.api.debugger.jpda.Variable evaluate(java.lang.String) throws org.netbeans.api.debugger.jpda.InvalidExpressionException
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public abstract void fixClasses(java.util.Map<java.lang.String,byte[]>)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public abstract void setSuspend(int)
meth public abstract void waitRunning() throws org.netbeans.api.debugger.jpda.DebuggerStartException
meth public boolean canBeModified()
meth public boolean canGetInstanceInfo()
meth public java.util.List<org.netbeans.api.debugger.jpda.JPDAClassType> getAllClasses()
meth public java.util.List<org.netbeans.api.debugger.jpda.JPDAClassType> getClassesByName(java.lang.String)
meth public long[] getInstanceCounts(java.util.List<org.netbeans.api.debugger.jpda.JPDAClassType>)
meth public org.netbeans.api.debugger.jpda.JPDAStep createJPDAStep(int,int)
meth public static org.netbeans.api.debugger.jpda.JPDADebugger attach(java.lang.String,int,java.lang.Object[]) throws org.netbeans.api.debugger.jpda.DebuggerStartException
meth public static org.netbeans.api.debugger.jpda.JPDADebugger attach(java.lang.String,java.lang.Object[]) throws org.netbeans.api.debugger.jpda.DebuggerStartException
meth public static org.netbeans.api.debugger.jpda.JPDADebugger listen(com.sun.jdi.connect.ListeningConnector,java.util.Map<java.lang.String,? extends com.sun.jdi.connect.Connector$Argument>,java.lang.Object[]) throws org.netbeans.api.debugger.jpda.DebuggerStartException
meth public static void launch(java.lang.String,java.lang.String[],java.lang.String,boolean)
meth public static void startListening(com.sun.jdi.connect.ListeningConnector,java.util.Map<java.lang.String,? extends com.sun.jdi.connect.Connector$Argument>,java.lang.Object[]) throws org.netbeans.api.debugger.jpda.DebuggerStartException
supr java.lang.Object

CLSS public abstract org.netbeans.api.debugger.jpda.JPDAStep
cons public JPDAStep(org.netbeans.api.debugger.jpda.JPDADebugger,int,int)
fld protected org.netbeans.api.debugger.jpda.JPDADebugger debugger
fld public final static int STEP_INTO = 1
fld public final static int STEP_LINE = -2
fld public final static int STEP_MIN = -1
fld public final static int STEP_OPERATION = 10
fld public final static int STEP_OUT = 3
fld public final static int STEP_OVER = 2
fld public final static java.lang.String PROP_STATE_EXEC = "exec"
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void addStep(org.netbeans.api.debugger.jpda.JPDAThread)
meth public boolean getHidden()
meth public int getDepth()
meth public int getSize()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void setDepth(int)
meth public void setHidden(boolean)
meth public void setSize(int)
supr java.lang.Object
hfds depth,hidden,pcs,size

CLSS public abstract interface org.netbeans.api.debugger.jpda.JPDAThread
fld public final static int STATE_MONITOR = 3
fld public final static int STATE_NOT_STARTED = 5
fld public final static int STATE_RUNNING = 1
fld public final static int STATE_SLEEPING = 2
fld public final static int STATE_UNKNOWN = -1
fld public final static int STATE_WAIT = 4
fld public final static int STATE_ZOMBIE = 0
fld public final static java.lang.String PROP_CALLSTACK = "callStack"
fld public final static java.lang.String PROP_VARIABLES = "variables"
meth public abstract boolean isSuspended()
meth public abstract int getLineNumber(java.lang.String)
meth public abstract int getStackDepth()
meth public abstract int getState()
meth public abstract java.lang.String getClassName()
meth public abstract java.lang.String getMethodName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getSourceName(java.lang.String) throws com.sun.jdi.AbsentInformationException
meth public abstract java.lang.String getSourcePath(java.lang.String) throws com.sun.jdi.AbsentInformationException
meth public abstract java.util.List<org.netbeans.spi.debugger.jpda.EditorContext$Operation> getLastOperations()
meth public abstract org.netbeans.api.debugger.jpda.CallStackFrame[] getCallStack() throws com.sun.jdi.AbsentInformationException
meth public abstract org.netbeans.api.debugger.jpda.CallStackFrame[] getCallStack(int,int) throws com.sun.jdi.AbsentInformationException
meth public abstract org.netbeans.api.debugger.jpda.JPDAThreadGroup getParentThreadGroup()
meth public abstract org.netbeans.api.debugger.jpda.ObjectVariable getContendedMonitor()
meth public abstract org.netbeans.api.debugger.jpda.ObjectVariable[] getOwnedMonitors()
meth public abstract org.netbeans.spi.debugger.jpda.EditorContext$Operation getCurrentOperation()
meth public abstract void interrupt()
meth public abstract void makeCurrent()
meth public abstract void resume()
meth public abstract void suspend()

CLSS public abstract interface org.netbeans.api.debugger.jpda.JPDAThreadGroup
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.api.debugger.jpda.JPDAThreadGroup getParentThreadGroup()
meth public abstract org.netbeans.api.debugger.jpda.JPDAThreadGroup[] getThreadGroups()
meth public abstract org.netbeans.api.debugger.jpda.JPDAThread[] getThreads()
meth public abstract void resume()
meth public abstract void suspend()

CLSS public abstract interface org.netbeans.api.debugger.jpda.JPDAWatch
intf org.netbeans.api.debugger.jpda.Variable
meth public abstract java.lang.String getExceptionDescription()
meth public abstract java.lang.String getExpression()
meth public abstract java.lang.String getToStringValue() throws org.netbeans.api.debugger.jpda.InvalidExpressionException
meth public abstract java.lang.String getType()
meth public abstract java.lang.String getValue()
meth public abstract void remove()
meth public abstract void setExpression(java.lang.String)
meth public abstract void setValue(java.lang.String) throws org.netbeans.api.debugger.jpda.InvalidExpressionException

CLSS public final org.netbeans.api.debugger.jpda.LaunchingDICookie
fld public final static java.lang.String ID = "netbeans-jpda-LaunchingDICookie"
meth public boolean getSuspend()
meth public com.sun.jdi.VirtualMachine getVirtualMachine() throws com.sun.jdi.connect.IllegalConnectorArgumentsException,com.sun.jdi.connect.VMStartException,java.io.IOException
meth public java.lang.String getClassName()
meth public java.lang.String getCommandLine()
meth public static java.lang.String getTransportName()
meth public static org.netbeans.api.debugger.jpda.LaunchingDICookie create(java.lang.String,java.lang.String,java.lang.String,boolean)
meth public static org.netbeans.api.debugger.jpda.LaunchingDICookie create(java.lang.String,java.lang.String[],java.lang.String,boolean)
supr org.netbeans.api.debugger.jpda.AbstractDICookie
hfds args,launchingConnector,mainClassName,suspend

CLSS public org.netbeans.api.debugger.jpda.LineBreakpoint
fld public final static java.lang.String PROP_CONDITION = "condition"
fld public final static java.lang.String PROP_INSTANCE_FILTERS = "instanceFilters"
fld public final static java.lang.String PROP_LINE_NUMBER = "lineNumber"
fld public final static java.lang.String PROP_PREFERRED_CLASS_NAME = "classNamePreferred"
fld public final static java.lang.String PROP_SOURCE_NAME = "sourceName"
fld public final static java.lang.String PROP_SOURCE_PATH = "sourcePath"
fld public final static java.lang.String PROP_STRATUM = "stratum"
fld public final static java.lang.String PROP_THREAD_FILTERS = "threadFilters"
fld public final static java.lang.String PROP_URL = "url"
meth public int getLineNumber()
meth public java.lang.String getCondition()
meth public java.lang.String getPreferredClassName()
meth public java.lang.String getSourceName()
meth public java.lang.String getSourcePath()
meth public java.lang.String getStratum()
meth public java.lang.String getURL()
meth public java.lang.String toString()
meth public org.netbeans.api.debugger.jpda.JPDAThread[] getThreadFilters(org.netbeans.api.debugger.jpda.JPDADebugger)
meth public org.netbeans.api.debugger.jpda.ObjectVariable[] getInstanceFilters(org.netbeans.api.debugger.jpda.JPDADebugger)
meth public static org.netbeans.api.debugger.jpda.LineBreakpoint create(java.lang.String,int)
meth public void setCondition(java.lang.String)
meth public void setInstanceFilters(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.ObjectVariable[])
meth public void setLineNumber(int)
meth public void setPreferredClassName(java.lang.String)
meth public void setSourceName(java.lang.String)
meth public void setSourcePath(java.lang.String)
meth public void setStratum(java.lang.String)
meth public void setThreadFilters(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.JPDAThread[])
meth public void setURL(java.lang.String)
supr org.netbeans.api.debugger.jpda.JPDABreakpoint
hfds className,condition,instanceFilters,lineNumber,sourceName,sourcePath,stratum,threadFilters,url
hcls LineBreakpointImpl

CLSS public final org.netbeans.api.debugger.jpda.ListeningDICookie
fld public final static java.lang.String ID = "netbeans-jpda-ListeningDICookie"
meth public com.sun.jdi.VirtualMachine getVirtualMachine() throws com.sun.jdi.connect.IllegalConnectorArgumentsException,java.io.IOException
meth public com.sun.jdi.connect.ListeningConnector getListeningConnector()
meth public int getPortNumber()
meth public java.lang.String getSharedMemoryName()
meth public java.util.Map<java.lang.String,? extends com.sun.jdi.connect.Connector$Argument> getArgs()
meth public static org.netbeans.api.debugger.jpda.ListeningDICookie create(com.sun.jdi.connect.ListeningConnector,java.util.Map<java.lang.String,? extends com.sun.jdi.connect.Connector$Argument>)
meth public static org.netbeans.api.debugger.jpda.ListeningDICookie create(int)
meth public static org.netbeans.api.debugger.jpda.ListeningDICookie create(java.lang.String)
supr org.netbeans.api.debugger.jpda.AbstractDICookie
hfds args,listeningConnector

CLSS public abstract interface org.netbeans.api.debugger.jpda.LocalVariable
intf org.netbeans.api.debugger.jpda.Variable
meth public abstract java.lang.String getClassName()
meth public abstract java.lang.String getDeclaredType()
meth public abstract java.lang.String getName()
meth public abstract void setValue(java.lang.String) throws org.netbeans.api.debugger.jpda.InvalidExpressionException

CLSS public org.netbeans.api.debugger.jpda.MethodBreakpoint
fld public final static int TYPE_METHOD_ENTRY = 1
fld public final static int TYPE_METHOD_EXIT = 2
fld public final static java.lang.String PROP_BREAKPOINT_TYPE = "breakpointtType"
fld public final static java.lang.String PROP_CLASS_EXCLUSION_FILTERS = "classExclusionFilters"
fld public final static java.lang.String PROP_CLASS_FILTERS = "classFilters"
fld public final static java.lang.String PROP_CONDITION = "condition"
fld public final static java.lang.String PROP_INSTANCE_FILTERS = "instanceFilters"
fld public final static java.lang.String PROP_METHOD_NAME = "methodName"
fld public final static java.lang.String PROP_METHOD_SIGNATURE = "signature"
fld public final static java.lang.String PROP_THREAD_FILTERS = "threadFilters"
meth public int getBreakpointType()
meth public java.lang.String getCondition()
meth public java.lang.String getMethodName()
meth public java.lang.String getMethodSignature()
meth public java.lang.String toString()
meth public java.lang.String[] getClassExclusionFilters()
meth public java.lang.String[] getClassFilters()
meth public org.netbeans.api.debugger.jpda.JPDAThread[] getThreadFilters(org.netbeans.api.debugger.jpda.JPDADebugger)
meth public org.netbeans.api.debugger.jpda.ObjectVariable[] getInstanceFilters(org.netbeans.api.debugger.jpda.JPDADebugger)
meth public static org.netbeans.api.debugger.jpda.MethodBreakpoint create()
meth public static org.netbeans.api.debugger.jpda.MethodBreakpoint create(java.lang.String,java.lang.String)
meth public void setBreakpointType(int)
meth public void setClassExclusionFilters(java.lang.String[])
meth public void setClassFilters(java.lang.String[])
meth public void setCondition(java.lang.String)
meth public void setInstanceFilters(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.ObjectVariable[])
meth public void setMethodName(java.lang.String)
meth public void setMethodSignature(java.lang.String)
meth public void setThreadFilters(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.JPDAThread[])
supr org.netbeans.api.debugger.jpda.JPDABreakpoint
hfds breakpointType,classExclusionFilters,classFilters,condition,instanceFilters,methodName,methodSignature,threadFilters
hcls MethodBreakpointImpl

CLSS public abstract interface org.netbeans.api.debugger.jpda.ObjectVariable
intf org.netbeans.api.debugger.jpda.Variable
meth public abstract int getFieldsCount()
meth public abstract java.lang.String getToStringValue() throws org.netbeans.api.debugger.jpda.InvalidExpressionException
meth public abstract java.util.List<org.netbeans.api.debugger.jpda.ObjectVariable> getReferringObjects(long)
meth public abstract long getUniqueID()
meth public abstract org.netbeans.api.debugger.jpda.Field getField(java.lang.String)
meth public abstract org.netbeans.api.debugger.jpda.Field[] getAllStaticFields(int,int)
meth public abstract org.netbeans.api.debugger.jpda.Field[] getFields(int,int)
meth public abstract org.netbeans.api.debugger.jpda.Field[] getInheritedFields(int,int)
meth public abstract org.netbeans.api.debugger.jpda.JPDAClassType getClassType()
meth public abstract org.netbeans.api.debugger.jpda.Super getSuper()
meth public abstract org.netbeans.api.debugger.jpda.Variable invokeMethod(java.lang.String,java.lang.String,org.netbeans.api.debugger.jpda.Variable[]) throws java.lang.NoSuchMethodException,org.netbeans.api.debugger.jpda.InvalidExpressionException

CLSS public abstract interface org.netbeans.api.debugger.jpda.ReturnVariable
intf org.netbeans.api.debugger.jpda.ObjectVariable
meth public abstract java.lang.String methodName()

CLSS public abstract interface org.netbeans.api.debugger.jpda.SmartSteppingFilter
fld public final static java.lang.String PROP_EXCLUSION_PATTERNS = "exclusionPatterns"
meth public abstract java.lang.String[] getExclusionPatterns()
meth public abstract void addExclusionPatterns(java.util.Set<java.lang.String>)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeExclusionPatterns(java.util.Set<java.lang.String>)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.api.debugger.jpda.Super
intf org.netbeans.api.debugger.jpda.ObjectVariable

CLSS public abstract interface org.netbeans.api.debugger.jpda.This
intf org.netbeans.api.debugger.jpda.ObjectVariable

CLSS public final org.netbeans.api.debugger.jpda.ThreadBreakpoint
fld public final static int TYPE_THREAD_DEATH = 2
fld public final static int TYPE_THREAD_STARTED = 1
fld public final static int TYPE_THREAD_STARTED_OR_DEATH = 3
fld public final static java.lang.String PROP_BREAKPOINT_TYPE = "breakpointtType"
meth public int getBreakpointType()
meth public java.lang.String toString()
meth public static org.netbeans.api.debugger.jpda.ThreadBreakpoint create()
meth public void setBreakpointType(int)
supr org.netbeans.api.debugger.jpda.JPDABreakpoint
hfds breakpointType

CLSS public abstract interface org.netbeans.api.debugger.jpda.Variable
meth public abstract java.lang.String getType()
meth public abstract java.lang.String getValue()

CLSS public abstract interface org.netbeans.api.debugger.jpda.VariableType
meth public abstract java.lang.String getName()

CLSS public final org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent
cons public JPDABreakpointEvent(org.netbeans.api.debugger.jpda.JPDABreakpoint,org.netbeans.api.debugger.jpda.JPDADebugger,int,org.netbeans.api.debugger.jpda.JPDAThread,com.sun.jdi.ReferenceType,org.netbeans.api.debugger.jpda.Variable)
cons public JPDABreakpointEvent(org.netbeans.api.debugger.jpda.JPDABreakpoint,org.netbeans.api.debugger.jpda.JPDADebugger,java.lang.Throwable,org.netbeans.api.debugger.jpda.JPDAThread,com.sun.jdi.ReferenceType,org.netbeans.api.debugger.jpda.Variable)
fld public final static int CONDITION_FAILED = 3
fld public final static int CONDITION_FALSE = 2
fld public final static int CONDITION_NONE = 0
fld public final static int CONDITION_TRUE = 1
meth public boolean getResume()
meth public com.sun.jdi.ReferenceType getReferenceType()
meth public int getConditionResult()
meth public java.lang.Throwable getConditionException()
meth public org.netbeans.api.debugger.jpda.JPDADebugger getDebugger()
meth public org.netbeans.api.debugger.jpda.JPDAThread getThread()
meth public org.netbeans.api.debugger.jpda.Variable getVariable()
meth public void resume()
supr java.util.EventObject
hfds conditionException,conditionResult,debugger,referenceType,resume,thread,variable

CLSS public abstract interface org.netbeans.api.debugger.jpda.event.JPDABreakpointListener
intf java.util.EventListener
meth public abstract void breakpointReached(org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent)

CLSS public abstract org.netbeans.spi.debugger.jpda.EditorContext
cons public EditorContext()
fld public final static java.lang.String BREAKPOINT_ANNOTATION_TYPE = "Breakpoint"
fld public final static java.lang.String CALL_STACK_FRAME_ANNOTATION_TYPE = "CallSite"
fld public final static java.lang.String CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = "CondBreakpoint"
fld public final static java.lang.String CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE = "CurrentExpressionLine"
fld public final static java.lang.String CURRENT_EXPRESSION_SECONDARY_LINE_ANNOTATION_TYPE = "CurrentExpression"
fld public final static java.lang.String CURRENT_LAST_OPERATION_ANNOTATION_TYPE = "LastOperation"
fld public final static java.lang.String CURRENT_LINE_ANNOTATION_TYPE = "CurrentPC"
fld public final static java.lang.String CURRENT_OUT_OPERATION_ANNOTATION_TYPE = "StepOutOperation"
fld public final static java.lang.String DISABLED_BREAKPOINT_ANNOTATION_TYPE = "DisabledBreakpoint"
fld public final static java.lang.String DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = "DisabledCondBreakpoint"
fld public final static java.lang.String DISABLED_FIELD_BREAKPOINT_ANNOTATION_TYPE = "DisabledFieldBreakpoint"
fld public final static java.lang.String DISABLED_METHOD_BREAKPOINT_ANNOTATION_TYPE = "DisabledMethodBreakpoint"
fld public final static java.lang.String FIELD_BREAKPOINT_ANNOTATION_TYPE = "FieldBreakpoint"
fld public final static java.lang.String METHOD_BREAKPOINT_ANNOTATION_TYPE = "MethodBreakpoint"
fld public final static java.lang.String PROP_LINE_NUMBER = "lineNumber"
innr public abstract interface static BytecodeProvider
innr public final static MethodArgument
innr public final static Operation
innr public final static Position
meth protected final org.netbeans.spi.debugger.jpda.EditorContext$Operation createMethodOperation(org.netbeans.spi.debugger.jpda.EditorContext$Position,org.netbeans.spi.debugger.jpda.EditorContext$Position,org.netbeans.spi.debugger.jpda.EditorContext$Position,org.netbeans.spi.debugger.jpda.EditorContext$Position,java.lang.String,java.lang.String,int)
meth protected final org.netbeans.spi.debugger.jpda.EditorContext$Position createPosition(int,int,int)
meth protected final void addNextOperationTo(org.netbeans.spi.debugger.jpda.EditorContext$Operation,org.netbeans.spi.debugger.jpda.EditorContext$Operation)
meth public abstract boolean showSource(java.lang.String,int,java.lang.Object)
meth public abstract int getCurrentLineNumber()
meth public abstract int getFieldLineNumber(java.lang.String,java.lang.String,java.lang.String)
meth public abstract int getLineNumber(java.lang.Object,java.lang.Object)
meth public abstract java.lang.Object annotate(java.lang.String,int,java.lang.String,java.lang.Object)
meth public abstract java.lang.String getClassName(java.lang.String,int)
meth public abstract java.lang.String getCurrentClassName()
meth public abstract java.lang.String getCurrentFieldName()
meth public abstract java.lang.String getCurrentMethodName()
meth public abstract java.lang.String getCurrentURL()
meth public abstract java.lang.String getSelectedIdentifier()
meth public abstract java.lang.String getSelectedMethodName()
meth public abstract java.lang.String[] getImports(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public abstract void createTimeStamp(java.lang.Object)
meth public abstract void disposeTimeStamp(java.lang.Object)
meth public abstract void removeAnnotation(java.lang.Object)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public abstract void updateTimeStamp(java.lang.Object,java.lang.String)
meth public int getMethodLineNumber(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.Object annotate(java.lang.String,int,int,java.lang.String,java.lang.Object)
meth public java.lang.String[] getCurrentMethodDeclaration()
meth public org.netbeans.spi.debugger.jpda.EditorContext$MethodArgument[] getArguments(java.lang.String,int)
meth public org.netbeans.spi.debugger.jpda.EditorContext$MethodArgument[] getArguments(java.lang.String,org.netbeans.spi.debugger.jpda.EditorContext$Operation)
meth public org.netbeans.spi.debugger.jpda.EditorContext$Operation[] getOperations(java.lang.String,int,org.netbeans.spi.debugger.jpda.EditorContext$BytecodeProvider)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.spi.debugger.jpda.EditorContext$BytecodeProvider
meth public abstract byte[] byteCodes()
meth public abstract byte[] constantPool()
meth public abstract int[] indexAtLines(int,int)

CLSS public final static org.netbeans.spi.debugger.jpda.EditorContext$MethodArgument
cons public MethodArgument(java.lang.String,java.lang.String,org.netbeans.spi.debugger.jpda.EditorContext$Position,org.netbeans.spi.debugger.jpda.EditorContext$Position)
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public org.netbeans.spi.debugger.jpda.EditorContext$Position getEndPosition()
meth public org.netbeans.spi.debugger.jpda.EditorContext$Position getStartPosition()
supr java.lang.Object
hfds endPos,name,startPos,type

CLSS public final static org.netbeans.spi.debugger.jpda.EditorContext$Operation
meth public boolean equals(java.lang.Object)
meth public int getBytecodeIndex()
meth public int hashCode()
meth public java.lang.String getMethodClassType()
meth public java.lang.String getMethodName()
meth public java.util.List<org.netbeans.spi.debugger.jpda.EditorContext$Operation> getNextOperations()
meth public org.netbeans.api.debugger.jpda.Variable getReturnValue()
meth public org.netbeans.spi.debugger.jpda.EditorContext$Position getEndPosition()
meth public org.netbeans.spi.debugger.jpda.EditorContext$Position getMethodEndPosition()
meth public org.netbeans.spi.debugger.jpda.EditorContext$Position getMethodStartPosition()
meth public org.netbeans.spi.debugger.jpda.EditorContext$Position getStartPosition()
meth public void setReturnValue(org.netbeans.api.debugger.jpda.Variable)
supr java.lang.Object
hfds bytecodeIndex,endPosition,methodClassType,methodEndPosition,methodName,methodStartPosition,nextOperations,returnValue,startPosition

CLSS public final static org.netbeans.spi.debugger.jpda.EditorContext$Position
meth public boolean equals(java.lang.Object)
meth public int getColumn()
meth public int getLine()
meth public int getOffset()
meth public int hashCode()
supr java.lang.Object
hfds column,line,offset

CLSS public abstract org.netbeans.spi.debugger.jpda.SmartSteppingCallback
cons public SmartSteppingCallback()
meth public abstract boolean stopHere(org.netbeans.spi.debugger.ContextProvider,org.netbeans.api.debugger.jpda.JPDAThread,org.netbeans.api.debugger.jpda.SmartSteppingFilter)
meth public abstract void initFilter(org.netbeans.api.debugger.jpda.SmartSteppingFilter)
supr java.lang.Object

CLSS public abstract org.netbeans.spi.debugger.jpda.SourcePathProvider
cons public SourcePathProvider()
fld public final static java.lang.String PROP_SOURCE_ROOTS = "sourceRoots"
meth public abstract java.lang.String getRelativePath(java.lang.String,char,boolean)
meth public abstract java.lang.String getURL(java.lang.String,boolean)
meth public abstract java.lang.String[] getOriginalSourceRoots()
meth public abstract java.lang.String[] getSourceRoots()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setSourceRoots(java.lang.String[])
meth public java.lang.String getSourceRoot(java.lang.String)
supr java.lang.Object

CLSS public abstract org.netbeans.spi.debugger.jpda.VariablesFilter
cons public VariablesFilter()
meth public abstract boolean isLeaf(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean isReadOnly(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract int getChildrenCount(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object getValueAt(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object[] getChildren(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getDisplayName(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getIconBase(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getShortDescription(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String[] getSupportedAncestors()
meth public abstract java.lang.String[] getSupportedTypes()
meth public abstract javax.swing.Action[] getActions(org.netbeans.spi.viewmodel.NodeActionsProvider,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void performDefaultAction(org.netbeans.spi.viewmodel.NodeActionsProvider,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void setValueAt(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr java.lang.Object

CLSS public abstract org.netbeans.spi.debugger.jpda.VariablesFilterAdapter
cons public VariablesFilterAdapter()
meth public abstract java.lang.String[] getSupportedAncestors()
meth public abstract java.lang.String[] getSupportedTypes()
meth public boolean isLeaf(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean isReadOnly(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public int getChildrenCount(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.Object getValueAt(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.Object[] getChildren(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getDisplayName(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getIconBase(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getShortDescription(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public javax.swing.Action[] getActions(org.netbeans.spi.viewmodel.NodeActionsProvider,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void performDefaultAction(org.netbeans.spi.viewmodel.NodeActionsProvider,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void setValueAt(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr org.netbeans.spi.debugger.jpda.VariablesFilter

