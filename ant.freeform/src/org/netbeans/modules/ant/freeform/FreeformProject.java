/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.ant.freeform;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.ant.freeform.ui.ProjectCustomizerProvider;
import org.netbeans.modules.ant.freeform.ui.View;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * One freeform project.
 * @author Jesse Glick
 */
public final class FreeformProject implements Project {
    
    public static final Lookup.Result<ProjectNature> PROJECT_NATURES = Lookup.getDefault().lookupResult(ProjectNature.class);
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final Lookup lookup;
    private AuxiliaryConfiguration aux;
    
    public FreeformProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = new FreeformEvaluator(this);
        lookup = initLookup();
        Logger.getLogger(FreeformProject.class.getName()).log(Level.FINER, "Initializing project in {0} with {1}", new Object[] {helper, lookup});
        new ProjectXmlValidator(helper.resolveFileObject(AntProjectHelper.PROJECT_XML_PATH));
    }
    
    public AntProjectHelper helper() {
        return helper;
    }

    /**
     * @see Util#getPrimaryConfigurationData
     */
    public Element getPrimaryConfigurationData() {
        return Util.getPrimaryConfigurationData(helper);
    }

    /**
     * @see Util#putPrimaryConfigurationData
     */
    public void putPrimaryConfigurationData(Element data) {
        Util.putPrimaryConfigurationData(helper, data);
    }

    private Lookup initLookup() throws IOException {
        aux = helper().createAuxiliaryConfiguration(); // AuxiliaryConfiguration
        FreeformFileEncodingQueryImpl FEQImpl = new FreeformFileEncodingQueryImpl(helper(), evaluator());
        helper().addAntProjectListener(FEQImpl);
        Lookup baseLookup = Lookups.fixed(
            this,
            new Info(), // ProjectInformation
            new FreeformSources(this), // Sources
            new Actions(this), // ActionProvider
            new View(this), // LogicalViewProvider
            new ProjectCustomizerProvider(this), // CustomizerProvider
            aux, // AuxiliaryConfiguration
            helper().createCacheDirectoryProvider(), // CacheDirectoryProvider
            new Subprojects(this), // SubprojectProvider
            new ArtifactProvider(this), // AntArtifactProvider
            new LookupMergerImpl(), // LookupMerger or ActionProvider
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            new FreeformProjectOperations(this),
	    new FreeformSharabilityQuery(helper()), //SharabilityQueryImplementation
            Accessor.DEFAULT.createProjectAccessor(this), //Access to AntProjectHelper and PropertyEvaluator
            FEQImpl, // FileEncodingQueryImplementation
            new FreeformTemplateAttributesProvider(helper(), eval)
        );
        return LookupProviderSupport.createCompositeLookup(baseLookup, "Projects/org-netbeans-modules-ant-freeform/Lookup"); //NOI18N
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }

    public String toString() {
        return "FreeformProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                Element data = getPrimaryConfigurationData();
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(FreeformProjectType.NS_GENERAL, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(FreeformProjectType.NS_GENERAL, "name");
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                putPrimaryConfigurationData(data);
                return null;
            }
        });
    }
    
    private final class Info implements ProjectInformation {
        
        public Info() {}
        
        public String getName() {
            return PropertyUtils.getUsablePropertyName(getDisplayName());
        }
        
        public String getDisplayName() {
            return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
                public String run() {
                    Element genldata = getPrimaryConfigurationData();
                    Element nameEl = Util.findElement(genldata, "name", FreeformProjectType.NS_GENERAL); // NOI18N
                    if (nameEl == null) {
                        // Corrupt. Cf. #48267 (cause unknown).
                        return "???"; // NOI18N
                    }
                    return Util.findText(nameEl);
                }
            });
        }
        
        public Icon getIcon() {
            if (usesAntScripting()) {
                return new ImageIcon(Utilities.loadImage("org/netbeans/modules/ant/freeform/resources/freeform-project.png", true)); // NOI18N
            } else {
                return new ImageIcon(Utilities.loadImage("org/netbeans/modules/project/ui/resources/projectTab.png", true)); // NOI18N
            }
        }
        
        public Project getProject() {
            return FreeformProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            // XXX
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            // XXX
        }
        
    }
    
 
    
    /**
     * Utility method to decide if the project actually uses Ant scripting.
     * It does if at least one of these hold:
     * <ol>
     * <li>There is a <code>build.xml</code> at top level.
     * <li>The property <code>ant.script</code> is defined.
     * <li>There are any <code>&lt;action&gt;</code>s bound.
     * </ol>
     */
    public boolean usesAntScripting() {
        return getProjectDirectory().getFileObject("build.xml") != null || // NOI18N
                evaluator().getProperty("ant.script") != null || // NOI18N
                Util.getPrimaryConfigurationData(helper).getElementsByTagName("action").getLength() > 0; // NOI18N
    }

}
