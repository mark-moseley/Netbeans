
package generators

def id = new Date().time
def folder = System.getProperty("user.home") + "/NewApplication-" + id;
def name = "NewApplication-" + id;

println "Generating project ${name} in folder ${folder}";

def build = """\
<?xml version="1.0" encoding="UTF-8"?>
<!-- You may freely edit this file. See harness/README in the NetBeans platform -->
<!-- for some information on what you could do (e.g. targets to override). -->
<!-- If you delete this file and reopen the project it will be recreated. -->
<project name="${name}" basedir=".">
    <description>Builds the module suite ${name}.</description>
    <import file="nbproject/build-impl.xml"/>
</project>

"""
def build_impl_xml = """\
<?xml version="1.0" encoding="UTF-8"?>
<!--
*** GENERATED FROM project.xml - DO NOT EDIT  ***
***         EDIT ../build.xml INSTEAD         ***
-->
<project name="${name}-impl" basedir=".." xmlns:sproject="http://www.netbeans.org/ns/nb-module-suite-project/1">
    <property file="nbproject/private/platform-private.properties"/>
    <property file="nbproject/platform.properties"/>
    <macrodef name="property" uri="http://www.netbeans.org/ns/nb-module-suite-project/1">
        <attribute name="name"/>
        <attribute name="value"/>
        <sequential>
            <property name="@{name}" value="\${@{value}}"/>
        </sequential>
    </macrodef>
    <property file="\${user.properties.file}"/>
    <sproject:property name="harness.dir" value="nbplatform.\${nbplatform.active}.harness.dir"/>
    <sproject:property name="netbeans.dest.dir" value="nbplatform.\${nbplatform.active}.netbeans.dest.dir"/>
    <fail message="You must define 'nbplatform.\${nbplatform.active}.harness.dir'">
        <condition>
            <not>
                <available file="\${harness.dir}" type="dir"/>
            </not>
        </condition>
    </fail>
    <import file="\${harness.dir}/suite.xml"/>
</project>

"""
def platform_properties = """\
disabled.clusters=\
    apisupport1,\
    enterprise5,\
    groovy1,\
    gsf1,\
    harness,\
    ide11,\
    java2,\
    profiler3,\
    visualweb2,\
    webcommon1,\
    websvccommon1,\
    xml2
disabled.modules=\
    org.netbeans.api.visual,\
    org.netbeans.core.execution,\
    org.netbeans.core.multiview,\
    org.netbeans.lib.uihandler,\
    org.netbeans.libs.jsr223,\
    org.netbeans.modules.core.kit,\
    org.netbeans.modules.favorites,\
    org.netbeans.modules.ide.branding,\
    org.netbeans.modules.ide.branding.kit,\
    org.netbeans.modules.jellytools,\
    org.netbeans.modules.registration,\
    org.netbeans.modules.reglib,\
    org.netbeans.modules.templates,\
    org.netbeans.modules.uihandler,\
    org.netbeans.modules.uihandler.exceptionreporter,\
    org.netbeans.modules.welcome,\
    org.netbeans.upgrader,\
    org.openide.compat,\
    org.openide.execution,\
    org.openide.options,\
    org.openide.util.enumerations
enabled.clusters=\
    nb6.7,\
    platform10
nbplatform.active=default

"""
def project_properties = """\

app.name=token
app.title=${name}
branding.token=\${app.name}
modules=

"""
def project_xml = """\
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://www.netbeans.org/ns/project/1">
    <type>org.netbeans.modules.apisupport.project.suite</type>
    <configuration>
        <data xmlns="http://www.netbeans.org/ns/nb-module-suite-project/1">
            <name>${name}</name>
        </data>
    </configuration>
</project>

"""
def base = new File(folder);
base.mkdirs();
def nbproject = new File(folder + File.separator + "nbproject");
nbproject.mkdirs();
new File(folder + File.separator  +"build.xml").write(build);
new File(folder + File.separator + "nbproject" + File.separator + "build-impl.xml").write(build_impl_xml);
new File(folder + File.separator + "nbproject" + File.separator + "platform.properties").write(platform_properties);
new File(folder + File.separator + "nbproject" + File.separator + "project.properties").write(project_properties);
new File(folder + File.separator + "nbproject" + File.separator + "project.xml").write(project_xml);

println("Done.")

