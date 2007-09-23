#API master signature file
#Version 1.5.0_11
CLSS public abstract org.netbeans.spi.options.AdvancedOption
cons public AdvancedOption()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.String org.netbeans.spi.options.AdvancedOption.getDisplayName()
meth public abstract java.lang.String org.netbeans.spi.options.AdvancedOption.getTooltip()
meth public abstract org.netbeans.spi.options.OptionsPanelController org.netbeans.spi.options.AdvancedOption.create()
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
CLSS public abstract org.netbeans.spi.options.OptionsCategory
cons public OptionsCategory()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract java.lang.String org.netbeans.spi.options.OptionsCategory.getCategoryName()
meth public abstract java.lang.String org.netbeans.spi.options.OptionsCategory.getTitle()
meth public abstract org.netbeans.spi.options.OptionsPanelController org.netbeans.spi.options.OptionsCategory.create()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.spi.options.OptionsCategory.getIconBase()
meth public javax.swing.Icon org.netbeans.spi.options.OptionsCategory.getIcon()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public abstract org.netbeans.spi.options.OptionsPanelController
cons public OptionsPanelController()
fld  constant public static final java.lang.String org.netbeans.spi.options.OptionsPanelController.PROP_CHANGED
fld  constant public static final java.lang.String org.netbeans.spi.options.OptionsPanelController.PROP_HELP_CTX
fld  constant public static final java.lang.String org.netbeans.spi.options.OptionsPanelController.PROP_VALID
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract boolean org.netbeans.spi.options.OptionsPanelController.isChanged()
meth public abstract boolean org.netbeans.spi.options.OptionsPanelController.isValid()
meth public abstract javax.swing.JComponent org.netbeans.spi.options.OptionsPanelController.getComponent(org.openide.util.Lookup)
meth public abstract org.openide.util.HelpCtx org.netbeans.spi.options.OptionsPanelController.getHelpCtx()
meth public abstract void org.netbeans.spi.options.OptionsPanelController.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.options.OptionsPanelController.applyChanges()
meth public abstract void org.netbeans.spi.options.OptionsPanelController.cancel()
meth public abstract void org.netbeans.spi.options.OptionsPanelController.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void org.netbeans.spi.options.OptionsPanelController.update()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.util.Lookup org.netbeans.spi.options.OptionsPanelController.getLookup()
supr java.lang.Object
