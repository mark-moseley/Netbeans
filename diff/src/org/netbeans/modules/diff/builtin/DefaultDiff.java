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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.diff.builtin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.windows.*;

import org.netbeans.api.diff.Diff;
//import org.netbeans.api.diff.DiffWrapperPanel;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.spi.diff.*;
import org.netbeans.modules.diff.builtin.visualizer.DiffViewImpl;
import org.netbeans.modules.diff.builtin.visualizer.editable.EditableDiffView;
import org.openide.DialogDisplayer;

/**
 *
 * @author  Martin Entlicher
 */
public class DefaultDiff extends Diff implements Serializable {
    
    private boolean showDiffSelector = true;
    
    /** Creates a new instance of DefaultDiff */
    public DefaultDiff() {
    }
    
    /**
     * Get the display name of this diff provider.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(DefaultDiff.class, "DefaultDiff.displayName");
    }
    
    /**
     * Show the visual representation of the diff between two sources.
     * @param name1 the name of the first source
     * @param title1 the title of the first source
     * @param r1 the first source
     * @param name2 the name of the second source
     * @param title2 the title of the second source
     * @param r2 the second resource compared with the first one.
     * @param MIMEType the mime type of these sources
     * @return The Component representing the diff visual representation
     *        or null, when the representation is outside the IDE.
     * @throws IOException when the reading from input streams fails.
     */
    public Component createDiff(String name1, String title1, Reader r1,
                                String name2, String title2, Reader r2,
                                String MIMEType) throws IOException {
                                    /*
        this.name1 = name1;
        this.name2 = name2;
        this.title1 = title1;
        this.title2 = title2;
        this.MIMEType = MIMEType;
        StringWriter out1 = new StringWriter();
        StringWriter out2 = new StringWriter();
        cpStream(r1, out1);
        cpStream(r2, out2);
        this.buffer1 = out1.toString();
        this.buffer2 = out2.toString();
                                     */
        //diffPanel = new DiffWrapperPanel(showDiffSelector, showDiffSelector);
        DiffInfo diffInfo = new DiffInfo(name1, name2, title1, title2,
            MIMEType, showDiffSelector, showDiffSelector, r1, r2);
        // I need to know the initial differences to know whether the files actually
        // differ or not.
        DiffProvider provider = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);
        try {
            Difference[] diffs = provider.computeDiff(diffInfo.createFirstReader(), diffInfo.createSecondReader());
            diffInfo.setInitialDiffs(diffs);
        } catch (IOException ioex) {}
        DiffPresenter diffPanel = new DiffPresenter(diffInfo);
        TopComponent tp = new DiffTopComponent(diffPanel);
        diffInfo.setPresentingComponent(tp);
        diffPanel.setProvider(provider);
        diffPanel.setVisualizer((DiffVisualizer) Lookup.getDefault().lookup(DiffVisualizer.class));
        return tp;
        //if (!initPanel()) return null;
        //return tp;
    }

    public DiffView createDiff(StreamSource s1, StreamSource s2) throws IOException {
        if (System.getProperty("netbeans.experimental.diff55") != null) return new DiffViewImpl(s1, s2);
        return new EditableDiffView(s1, s2);
    }

    /** Getter for property showDiffSelector.
     * @return Value of property showDiffSelector.
     */
    public boolean isShowDiffSelector() {
        return showDiffSelector;
    }
    
    /** Setter for property showDiffSelector.
     * @param showDiffSelector New value of property showDiffSelector.
     */
    public void setShowDiffSelector(boolean showDiffSelector) {
        this.showDiffSelector = showDiffSelector;
    }
    
    /*
    private boolean initPanel() throws IOException {
        Lookup.Result providersResult = Lookup.getDefault().lookup(new Lookup.Template(DiffProvider.class));
        Lookup.Item[] providers = new Lookup.Item[providersResult.allItems().size()];
        int i = 0;
        for (Iterator it = providersResult.allItems().iterator(); it.hasNext(); i++) {
            providers[i] = (Lookup.Item) it.next();
        }
        Lookup.Result visualizersResult = Lookup.getDefault().lookup(new Lookup.Template(DiffVisualizer.class));
        Lookup.Item[] visualizers = new Lookup.Item[visualizersResult.allInstances().size()];
        i = 0;
        for (Iterator it = visualizersResult.allItems().iterator(); it.hasNext(); i++) {
            visualizers[i] = (Lookup.Item) it.next();
        }
        if (providers.length == 0 || visualizers.length == 0) {
            if (providers.length == 0) {
                TopManager.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(DefaultDiff.class, "MSG_ProvidersMissing")));
            } else if (visualizers.length == 0) {
                TopManager.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(DefaultDiff.class, "MSG_VisualizersMissing")));
            }
            return false;
        }
        providersMap = new HashMap();
        visualizersMap = new HashMap();
        String[] providersDisplayNames = new String[providers.length];
        for (i = 0; i < providers.length; i++) {
            providersDisplayNames[i] = providers[i].getDisplayName();
            providersMap.put(providersDisplayNames[i], providers[i]);
        }
        String[] visualizersDisplayNames = new String[visualizers.length];
        for (i = 0; i < visualizers.length; i++) {
            visualizersDisplayNames[i] = visualizers[i].getDisplayName();
            visualizersMap.put(visualizersDisplayNames[i], visualizers[i]);
        }
        defaultProviderName = providersDisplayNames[0];
        defaultVisualizerName = visualizersDisplayNames[0];
        diffPanel.showProvidersChooser(true);
        diffPanel.showVisualizerChooser(true);
        diffPanel.setProviders(providersDisplayNames);
        diffPanel.setVisualizers(visualizersDisplayNames);
        diffPanel.setVisualizer((DiffVisualizer) visualizers[0].getInstance());
        diffPanel.addProvidersChangeListener(new ProvidersChangeListener());
        diffPanel.addVisualizersChangeListener(new VisualizerChangeListener());
        showDiff();
        return true;
    }
     */
    
    /*
    private boolean initPanel() throws IOException {
        DiffProvider provider = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);
        DiffVisualizer visualizer = (DiffVisualizer) Lookup.getDefault().lookup(DiffVisualizer.class);
        diffPanel.setProvider(provider);
        diffPanel.setVisualizer(visualizer);
        ServicesChangeListener l = new ServicesChangeListener();
        diffPanel.addProvidersChangeListener(l);
        diffPanel.addVisualizersChangeListener(l);
        showDiff(provider, visualizer);
        return true;
    }
    
    private synchronized void showDiff(DiffProvider p, DiffVisualizer v) throws IOException {
        Difference[] diffs = p.computeDiff(new StringReader(buffer1),
                                           new StringReader(buffer2));
        Component c = v.createView(diffs, name1, title1, new StringReader(buffer1),
                                   name2, title2, new StringReader(buffer2), MIMEType);
        diffPanel.setVisualizer(c);
        tp.setName(c.getName());
        if (c instanceof TopComponent) {
            TopComponent vtp = (TopComponent) c;
            tp.setToolTipText(vtp.getToolTipText());
            tp.setIcon(vtp.getIcon());
        }
        c.requestFocus();
    }
     */
    
    /*
    private synchronized void showDiff() throws IOException {
        //System.out.println("showDiff("+defaultProviderName+", "+defaultVisualizerName+")");
        Lookup.Item pItem = (Lookup.Item) providersMap.get(defaultProviderName);
        Lookup.Item vItem = (Lookup.Item) visualizersMap.get(defaultVisualizerName);
        DiffProvider p = (DiffProvider) pItem.getInstance();
        DiffVisualizer v = (DiffVisualizer) vItem.getInstance();
        Difference[] diffs = p.computeDiff(new StringReader(buffer1),
                                           new StringReader(buffer2));
        Component c = v.createView(diffs, name1, title1, new StringReader(buffer1),
                                   name2, title2, new StringReader(buffer2), MIMEType);
        diffPanel.setVisualizer(c);
        diffPanel.setDefaultProvider(defaultProviderName);
        diffPanel.setDefaultVisualizer(defaultVisualizerName);
        tp.setName(c.getName());
        if (c instanceof TopComponent) {
            TopComponent vtp = (TopComponent) c;
            tp.setToolTipText(vtp.getToolTipText());
            tp.setIcon(vtp.getIcon());
        }
        c.requestFocus();
    }
     */
    
    private static void cpStream(Reader in, Writer out) throws IOException {
        char[] buff = new char[1024];
        int n;
        while ((n = in.read(buff)) > 0) {
            out.write(buff, 0, n);
        }
        in.close();
        out.close();
    }
    
    /*
    private class ServicesChangeListener implements java.beans.PropertyChangeListener {
        
        /**
         * This method gets called when a bound property is changed.
         * @param evt A PropertyChangeEvent object describing the event source
         *  	and the property that has changed.
         *
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            try {
                showDiff(diffPanel.getProvider(), diffPanel.getVisualizer());
            } catch (IOException ioex) {
                TopManager.getDefault().notifyException(ioex);
            }
        }
        
    }
     */
    
    /*
    private class ProvidersChangeListener implements ItemListener {
        
        /**
         * Invoked when an item has been selected or deselected.
         * The code written for this method performs the operations
         * that need to occur when an item is selected (or deselected).
         *
        public void itemStateChanged(ItemEvent e) {
            if (e.SELECTED == e.getStateChange()) {
                defaultProviderName = (String) e.getItem();
                try {
                    showDiff();
                } catch (IOException ioex) {
                    TopManager.getDefault().notifyException(ioex);
                }
            }
        }
        
    }
    
    private class VisualizerChangeListener implements ItemListener {
        
        /**
         * Invoked when an item has been selected or deselected.
         * The code written for this method performs the operations
         * that need to occur when an item is selected (or deselected).
         *
        public void itemStateChanged(ItemEvent e) {
            if (e.SELECTED == e.getStateChange()) {
                //defaultVisualizerName = (String) e.getItem();
                System.out.println("visualizer = "+e.getItem());
                try {
                    showDiff();
                } catch (IOException ioex) {
                    TopManager.getDefault().notifyException(ioex);
                }
            }
        }
        
    }
     */
    
    private static class DiffInfo extends DiffPresenter.Info {
        
        private String buffer1, buffer2;
        private Difference[] diffs;
        
        public DiffInfo(String name1, String name2, String title1, String title2,
                        String mimeType, boolean chooseProviders, boolean chooseVisualizers,
                        Reader r1, Reader r2) throws IOException {
            super(name1, name2, title1, title2, mimeType, chooseProviders, chooseVisualizers);
            StringWriter out1 = new StringWriter();
            StringWriter out2 = new StringWriter();
            cpStream(r1, out1);
            cpStream(r2, out2);
            this.buffer1 = out1.toString();
            this.buffer2 = out2.toString();
        }
        
        public Reader createFirstReader() {
            return new StringReader(buffer1);
        }
        
        public Reader createSecondReader() {
            return new StringReader(buffer2);
        }
        
        void setInitialDiffs(Difference[] diffs) {
            this.diffs = diffs;
        }
        
        public Difference[] getInitialDifferences() {
            Difference[] diffs = this.diffs;
            this.diffs = null;
            return diffs;
        }
        
    }
    
    public static class DiffTopComponent extends TopComponent {
        
        public DiffTopComponent(Component c) {
            setLayout(new BorderLayout());
            add(c, BorderLayout.CENTER);            
            getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffTopComponent.class, "ACSN_Diff_Top_Component")); // NOI18N
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffTopComponent.class, "ACSD_Diff_Top_Component")); // NOI18N
        }
        
        public int getPersistenceType(){
            return TopComponent.PERSISTENCE_NEVER;
        }
        
        protected String preferredID(){
            return "DiffTopComponent";    //NOI18N       
        }
        
        public void open(Workspace workspace) {
            super.open(workspace);
            //diffPanel.open();
            requestActive();
        }
        
    }
    
}
