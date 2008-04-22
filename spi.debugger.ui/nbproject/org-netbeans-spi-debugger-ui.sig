#API master signature file
#Version 2.11
CLSS public abstract org.netbeans.spi.debugger.ui.AttachType
cons public AttachType()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.String org.netbeans.spi.debugger.ui.AttachType.getTypeDisplayName()
meth public abstract javax.swing.JComponent org.netbeans.spi.debugger.ui.AttachType.getCustomizer()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public abstract org.netbeans.spi.debugger.ui.BreakpointAnnotation
cons public BreakpointAnnotation()
fld  constant public static final java.lang.String org.openide.text.Annotation.PROP_ANNOTATION_TYPE
fld  constant public static final java.lang.String org.openide.text.Annotation.PROP_MOVE_TO_FRONT
fld  constant public static final java.lang.String org.openide.text.Annotation.PROP_SHORT_DESCRIPTION
meth protected final void org.openide.text.Annotation.firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.openide.text.Annotation.notifyAttached(org.openide.text.Annotatable)
meth protected void org.openide.text.Annotation.notifyDetached(org.openide.text.Annotatable)
meth public abstract java.lang.String org.openide.text.Annotation.getAnnotationType()
meth public abstract java.lang.String org.openide.text.Annotation.getShortDescription()
meth public abstract org.netbeans.api.debugger.Breakpoint org.netbeans.spi.debugger.ui.BreakpointAnnotation.getBreakpoint()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final org.openide.text.Annotatable org.openide.text.Annotation.getAttachedAnnotatable()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.openide.text.Annotation.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.openide.text.Annotation.attach(org.openide.text.Annotatable)
meth public final void org.openide.text.Annotation.detach()
meth public final void org.openide.text.Annotation.moveToFront()
meth public final void org.openide.text.Annotation.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr org.openide.text.Annotation
CLSS public abstract org.netbeans.spi.debugger.ui.BreakpointType
cons public BreakpointType()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract boolean org.netbeans.spi.debugger.ui.BreakpointType.isDefault()
meth public abstract java.lang.String org.netbeans.spi.debugger.ui.BreakpointType.getCategoryDisplayName()
meth public abstract java.lang.String org.netbeans.spi.debugger.ui.BreakpointType.getTypeDisplayName()
meth public abstract javax.swing.JComponent org.netbeans.spi.debugger.ui.BreakpointType.getCustomizer()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.debugger.ui.Constants
fld  constant public static final java.lang.String org.netbeans.spi.debugger.ui.Constants.BREAKPOINT_ENABLED_COLUMN_ID
fld  constant public static final java.lang.String org.netbeans.spi.debugger.ui.Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID
fld  constant public static final java.lang.String org.netbeans.spi.debugger.ui.Constants.LOCALS_TO_STRING_COLUMN_ID
fld  constant public static final java.lang.String org.netbeans.spi.debugger.ui.Constants.LOCALS_TYPE_COLUMN_ID
fld  constant public static final java.lang.String org.netbeans.spi.debugger.ui.Constants.LOCALS_VALUE_COLUMN_ID
fld  constant public static final java.lang.String org.netbeans.spi.debugger.ui.Constants.SESSION_HOST_NAME_COLUMN_ID
fld  constant public static final java.lang.String org.netbeans.spi.debugger.ui.Constants.SESSION_LANGUAGE_COLUMN_ID
fld  constant public static final java.lang.String org.netbeans.spi.debugger.ui.Constants.SESSION_STATE_COLUMN_ID
fld  constant public static final java.lang.String org.netbeans.spi.debugger.ui.Constants.THREAD_STATE_COLUMN_ID
fld  constant public static final java.lang.String org.netbeans.spi.debugger.ui.Constants.THREAD_SUSPENDED_COLUMN_ID
fld  constant public static final java.lang.String org.netbeans.spi.debugger.ui.Constants.WATCH_TO_STRING_COLUMN_ID
fld  constant public static final java.lang.String org.netbeans.spi.debugger.ui.Constants.WATCH_TYPE_COLUMN_ID
fld  constant public static final java.lang.String org.netbeans.spi.debugger.ui.Constants.WATCH_VALUE_COLUMN_ID
supr null
CLSS public abstract interface org.netbeans.spi.debugger.ui.Controller
fld  constant public static final java.lang.String org.netbeans.spi.debugger.ui.Controller.PROP_VALID
meth public abstract boolean org.netbeans.spi.debugger.ui.Controller.cancel()
meth public abstract boolean org.netbeans.spi.debugger.ui.Controller.isValid()
meth public abstract boolean org.netbeans.spi.debugger.ui.Controller.ok()
meth public abstract void org.netbeans.spi.debugger.ui.Controller.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.debugger.ui.Controller.removePropertyChangeListener(java.beans.PropertyChangeListener)
supr null
