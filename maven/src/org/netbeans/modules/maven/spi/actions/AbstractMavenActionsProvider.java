/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.spi.actions;


import org.netbeans.modules.maven.api.execute.RunConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.execute.ModelRunConfig;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Writer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 * a default implementation of MavenActionsProvider, a fallback when nothing is
 * user configured or overriden by a more specialized provider.
 * @author mkleint
 */
public abstract class AbstractMavenActionsProvider implements MavenActionsProvider {

    protected ActionToGoalMapping originalMappings;
    protected NetbeansBuildActionXpp3Reader reader = new NetbeansBuildActionXpp3Reader();
    private NetbeansBuildActionXpp3Writer writer = new NetbeansBuildActionXpp3Writer();

    /** Creates a new instance of DefaultActionProvider */
    public AbstractMavenActionsProvider() {
    }

    /**
     * just gets the array of FOs from lookup.
     */
    protected static FileObject[] extractFileObjectsfromLookup(Lookup lookup) {
        List<FileObject> files = new ArrayList<FileObject>();
        Iterator<? extends DataObject> it = lookup.lookup(new Lookup.Template<DataObject>(DataObject.class)).allInstances().iterator();
        while (it.hasNext()) {
            DataObject d = it.next();
            FileObject f = d.getPrimaryFile();
            files.add(f);
        }
        return files.toArray(new FileObject[files.size()]);
    }

    public boolean isActionEnable(String action, Project project, Lookup lookup) {
        ActionToGoalMapping rawMappings = getRawMappings();
        Iterator it = rawMappings.getActions().iterator();
        NbMavenProject mp = project.getLookup().lookup(NbMavenProject.class);
        String prjPack = mp.getPackagingType();
        while (it.hasNext()) {
            NetbeansActionMapping elem = (NetbeansActionMapping) it.next();
            if (action.equals(elem.getActionName()) &&
                    (elem.getPackagings().isEmpty() ||
                    elem.getPackagings().contains(prjPack.trim()) ||
                    elem.getPackagings().contains("*"))) {//NOI18N
                return true;
            }
        }

        return false;
    }

    public final RunConfig createConfigForDefaultAction(String actionName, Project project, Lookup lookup) {
        FileObject[] fos = extractFileObjectsfromLookup(lookup);
        @SuppressWarnings("unchecked")
        Map<String, String> replaceMap = lookup.lookup(Map.class);
        if (replaceMap == null) { //#159698
            replaceMap = new HashMap<String, String>();
            Logger.getLogger(AbstractMavenActionsProvider.class.getName()).log(Level.FINE, "Missing replace tokens map when executing maven build. Could lead to problems with execution. See issue #159698 for details.", new Exception()); //NOI18N
        }
        FileObject fo = null;
        if (fos.length > 0) {
            fo = fos[0];
        }
//        if (group != null && MavenSourcesImpl.NAME_TESTSOURCE.equals(group.getName()) &&
//                ActionProvider.COMMAND_RUN_SINGLE.equals(actionName)) {
//            //TODO how to allow running main() in tests?
//            actionName = ActionProvider.COMMAND_TEST_SINGLE;
//        }
//        if (group != null && MavenSourcesImpl.NAME_TESTSOURCE.equals(group.getName()) &&
//                ActionProvider.COMMAND_DEBUG_SINGLE.equals(actionName)) {
//            //TODO how to allow running main() in tests?
//            actionName = ActionProvider.COMMAND_DEBUG_TEST_SINGLE;
//        }
        return mapGoalsToAction(project, actionName, replaceMap, fo);
    }
  
    public ActionToGoalMapping getRawMappings() {
        if (originalMappings == null || reloadStream()) {
            InputStream in = getActionDefinitionStream();
            if (in == null) {
                originalMappings = new ActionToGoalMapping();
            } else {
                Reader rdr = null;
                try {
                    rdr = new InputStreamReader(in);
                    originalMappings = reader.read(rdr);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    originalMappings = new ActionToGoalMapping();
                } catch (XmlPullParserException ex) {
                    ex.printStackTrace();
                    originalMappings = new ActionToGoalMapping();
                } finally {
                    if (rdr != null) {
                        try {
                            rdr.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        return originalMappings;
    }

    public String getRawMappingsAsString() {
        StringWriter str = new StringWriter();
        try {
            writer.write(str, getRawMappings());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return str.toString();
    }

    /**
     * default implementation will look in the content of the
     * @return
     */
    public Set<String> getSupportedDefaultActions() {
        HashSet<String> toRet = new HashSet<String>();
        ActionToGoalMapping raw = getRawMappings();
        for (Object obj : raw.getActions()) {
            NetbeansActionMapping nb = (NetbeansActionMapping) obj;
            String name = nb.getActionName();
            if (name != null && !name.startsWith("CUSTOM-")) {
                toRet.add(name);
            }
        }
        return toRet;
    }
    

    /**
     * override in children that are listening on changes of model and need refreshing..
     */
    protected boolean reloadStream() {
        return false;
    }

    /**
     * get a action to maven mapping configuration for the given action.
     * No replacements happen.
     * The instance returned is always a new copy, can be modified or reused.
     */
    public NetbeansActionMapping getMappingForAction(String actionName, Project project) {
        NetbeansActionMapping action = null;
        try {
            // just a converter for the To-Object reader..
            Reader read = performDynamicSubstitutions(Collections.EMPTY_MAP, getRawMappingsAsString());
            // basically doing a copy here..
            ActionToGoalMapping mapping = reader.read(read);
            Iterator it = mapping.getActions().iterator();
            NbMavenProject mp = project.getLookup().lookup(NbMavenProject.class);
            String prjPack = mp.getPackagingType();
            while (it.hasNext()) {
                NetbeansActionMapping elem = (NetbeansActionMapping) it.next();
                if (actionName.equals(elem.getActionName()) &&
                        (elem.getPackagings().isEmpty() ||
                         elem.getPackagings().contains(prjPack.trim()) ||
                         elem.getPackagings().contains("*"))) {//NOI18N
                    action = elem;
                    break;
                }
            }
        } catch (XmlPullParserException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return action;

    }

    /**
     * content of the input stream shall be the xml with action definitions
     */
    protected abstract InputStream getActionDefinitionStream();

    private RunConfig mapGoalsToAction(Project project, String actionName, Map<String, String> replaceMap, FileObject selectedFile) {
        try {
            // TODO need some caching really badly here..
            Reader read = performDynamicSubstitutions(replaceMap, getRawMappingsAsString());
            ActionToGoalMapping mapping = reader.read(read);
            Iterator it = mapping.getActions().iterator();
            NetbeansActionMapping action = null;
            NbMavenProject mp = project.getLookup().lookup(NbMavenProject.class);
            String prjPack = mp.getPackagingType();
            while (it.hasNext()) {
                NetbeansActionMapping elem = (NetbeansActionMapping) it.next();
                if (actionName.equals(elem.getActionName()) &&
                        (elem.getPackagings().contains(prjPack.trim()) ||
                        elem.getPackagings().contains("*") || elem.getPackagings().size() == 0)) {//NOI18N
                    action = elem;
                    break;
                }
            }
            if (action != null) {
                return new ModelRunConfig(project, action, actionName, selectedFile);
            }
        } catch (XmlPullParserException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * takes the input stream and a map, and for each occurence of ${<mapKey>}, replaces it with map entry value..
     */
    protected Reader performDynamicSubstitutions(final Map replaceMap, final String in) throws IOException {
        StringBuffer buf = new StringBuffer(in);
        Iterator it = replaceMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry elem = (Map.Entry) it.next();
            String replaceItem = "${" + elem.getKey() + "}";//NOI18N
            int index = buf.indexOf(replaceItem);
            while (index > -1) {
                String newItem = (String) elem.getValue();
                if (newItem == null) {
//                    System.out.println("no value for key=" + replaceItem);
                }
                newItem = newItem == null ? "" : newItem;//NOI18N
                buf.replace(index, index + replaceItem.length(), newItem);
                index = buf.indexOf(replaceItem);
            }
        }
        return new StringReader(buf.toString());
    }
}
