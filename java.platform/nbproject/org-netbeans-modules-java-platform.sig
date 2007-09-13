#Signature file v4.0
#Version 

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

CLSS public abstract org.netbeans.api.java.platform.JavaPlatform
cons protected JavaPlatform()
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_JAVADOC_FOLDER = "javadocFolders"
fld public final static java.lang.String PROP_SOURCE_FOLDER = "sourceFolders"
fld public final static java.lang.String PROP_SYSTEM_PROPERTIES = "systemProperties"
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void setSystemProperties(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getVendor()
meth public abstract java.util.Collection<org.openide.filesystems.FileObject> getInstallFolders()
meth public abstract java.util.List<java.net.URL> getJavadocFolders()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public abstract org.netbeans.api.java.classpath.ClassPath getBootstrapLibraries()
meth public abstract org.netbeans.api.java.classpath.ClassPath getSourceFolders()
meth public abstract org.netbeans.api.java.classpath.ClassPath getStandardLibraries()
meth public abstract org.netbeans.api.java.platform.Specification getSpecification()
meth public abstract org.openide.filesystems.FileObject findTool(java.lang.String)
meth public final java.util.Map<java.lang.String,java.lang.String> getSystemProperties()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static org.netbeans.api.java.platform.JavaPlatform getDefault()
supr java.lang.Object
hfds supp,sysproperties

CLSS public final org.netbeans.api.java.platform.JavaPlatformManager
cons public JavaPlatformManager()
fld public final static java.lang.String PROP_INSTALLED_PLATFORMS = "installedPlatforms"
meth public org.netbeans.api.java.platform.JavaPlatform getDefaultPlatform()
meth public org.netbeans.api.java.platform.JavaPlatform[] getInstalledPlatforms()
meth public org.netbeans.api.java.platform.JavaPlatform[] getPlatforms(java.lang.String,org.netbeans.api.java.platform.Specification)
meth public static org.netbeans.api.java.platform.JavaPlatformManager getDefault()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds cachedPlatforms,instance,lastProviders,pListener,pcs,providers,providersValid

CLSS public final org.netbeans.api.java.platform.PlatformsCustomizer
meth public static boolean showCustomizer(org.netbeans.api.java.platform.JavaPlatform)
supr java.lang.Object

CLSS public org.netbeans.api.java.platform.Profile
cons public Profile(java.lang.String,org.openide.modules.SpecificationVersion)
meth public boolean equals(java.lang.Object)
meth public final java.lang.String getName()
meth public final org.openide.modules.SpecificationVersion getVersion()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds name,version

CLSS public final org.netbeans.api.java.platform.Specification
cons public Specification(java.lang.String,org.openide.modules.SpecificationVersion)
cons public Specification(java.lang.String,org.openide.modules.SpecificationVersion,org.netbeans.api.java.platform.Profile[])
meth public boolean equals(java.lang.Object)
meth public final java.lang.String getName()
meth public final org.netbeans.api.java.platform.Profile[] getProfiles()
meth public final org.openide.modules.SpecificationVersion getVersion()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds name,profiles,version

CLSS public abstract org.netbeans.spi.java.platform.CustomPlatformInstall
cons public CustomPlatformInstall()
meth public abstract org.openide.WizardDescriptor$InstantiatingIterator<org.openide.WizardDescriptor> createIterator()
supr org.netbeans.spi.java.platform.GeneralPlatformInstall

CLSS public abstract org.netbeans.spi.java.platform.GeneralPlatformInstall
meth public abstract java.lang.String getDisplayName()
supr java.lang.Object

CLSS public abstract org.netbeans.spi.java.platform.PlatformInstall
cons public PlatformInstall()
meth public abstract boolean accept(org.openide.filesystems.FileObject)
meth public abstract org.openide.WizardDescriptor$InstantiatingIterator<org.openide.WizardDescriptor> createIterator(org.openide.filesystems.FileObject)
supr org.netbeans.spi.java.platform.GeneralPlatformInstall

