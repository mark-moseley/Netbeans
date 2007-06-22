/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */   

package org.netbeans.modules.mobility.svgcore.view.svg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

import javax.microedition.m2g.SVGImage;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.svgcore.ExternalEditAction;
import org.netbeans.modules.mobility.svgcore.export.SaveElementAsImage;
import org.netbeans.modules.mobility.svgcore.export.SaveAnimationAsImageAction;
import org.netbeans.modules.mobility.svgcore.export.SaveAsImageAction;
import org.openide.actions.EditAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.PerseusController;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.composer.ScreenManager;
import org.netbeans.modules.mobility.svgcore.export.ScreenSizeHelper;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.w3c.dom.svg.SVGLocatableElement;

/**
 * Top component which displays something.
 */
final public class SVGViewTopComponent extends CloneableTopComponent {    
    private static final long serialVersionUID = 5862679852552354L;
    private static final float ZOOM_STEP = (float) 1.1;
    public static final float DEFAULT_MAX    = 30.0f;
    public static final float DEFAULT_STEP   = 0.1f;

    private static final String PREFERRED_ID = "SVGViewTopComponent"; //NOI18N
    
    private static final String [] ZOOM_VALUES = new String[] { "400%", "300%", "200%", "100%", "75%", "50%", "25%" };
        
    private final InstanceContent content = new InstanceContent();
    private SVGDataObject         svgDataObject;
    private SceneManager          m_sceneMgr;
    private transient ParsingTask parsingTask;
    private Lookup                lookup = null;
    
    private transient JPanel      basePanel;    
    //UI controls
    private JToolBar       toolbar;
    private JSlider        slider;
    private JSpinner       maximumTimeSpinner;
    private JSpinner       currentTimeSpinner;
    private JButton        zoomFitButton;
    private JComboBox      zoomComboBox;
    private JButton        zoomInButton;
    private JButton        zoomOutButton;
    private JToggleButton  startAnimationButton;
    private JButton        pauseAnimationButton;
    private JButton        stopAnimationButton;
    private JToggleButton  toolTipToggleButton;
    private JToggleButton  hoverToggleButton;
    private JToggleButton  scaleToConfigurationToggleButton;
    private JToggleButton  showViewBoxToggleButton;
    private JToggleButton  allowEditToggleButton;
    private ChangeListener changeListener;        
    
    private boolean doScale      = false;
    private float   currentMaximum = DEFAULT_MAX;
        
    //decoration
    private ButtonMouseListener buttonListener;
    private transient PropertyChangeListener nameChangeL;
    
    //actions
    private ToggleScaleAction       scaleAction;
    private ToggleTooltipAction     toolTipAction;
    private ToggleHighlightAction   highlightAction;
    private ZoomToFitAction         zoomToFitAction;
    private ZoomInAction            zoomInAction;
    private ZoomOutAction           zoomOutAction;
    private ToggleShowViewBoxAction showViewBoxAction;

    private AbstractSVGAction       allowEditAction = 
        new AbstractSVGAction("allow_edit.png", "HINT_AllowEdit", "LBL_AllowEdit", true) {
            public void actionPerformed(ActionEvent e) {
                m_sceneMgr.setReadOnly( !m_sceneMgr.isReadOnly());
                updateAnimationActions();
                insertGraphicsAction.setEnabled(!m_sceneMgr.isReadOnly());
            }
    };            
    
    private AbstractSVGAction       startAnimationAction = 
        new AbstractSVGAction("animate_start.png", "HINT_AnimateStart", "LBL_AnimateStart") {
            public void actionPerformed(ActionEvent e) {
                startAnimationButton.setSelected(true);
                getPerseus().startAnimator();
                updateAnimationActions();
            }
    };            

    private AbstractSVGAction       pauseAnimationAction = 
        new AbstractSVGAction("animate_pause.png", "HINT_AnimatePause", "LBL_AnimatePause", false) {
            public void actionPerformed(ActionEvent e) {
                if ( getPerseus().getAnimatorState() == PerseusController.ANIMATION_RUNNING) {
                    getPerseus().pauseAnimator();
                } else {
                    getPerseus().startAnimator();
                }
                updateAnimationActions();                
            }
    };            

    private AbstractSVGAction       stopAnimationAction = 
        new AbstractSVGAction("animate_stop.png", "HINT_AnimateStop", "LBL_AnimateStop", false) {
            public void actionPerformed(ActionEvent e) {
                startAnimationButton.setSelected(false);
                getPerseus().stopAnimator();
                updateAnimationActions();
            }
    };            

    private AbstractSVGAction       insertGraphicsAction = 
        new AbstractSVGAction("animate_stop.png", "HINT_InsertGraphics", "LBL_InsertGraphics", false) {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int r = chooser.showDialog(
                        SwingUtilities.getWindowAncestor(SVGViewTopComponent.this),
                        NbBundle.getMessage(SVGViewTopComponent.class, "LBL_CHOOSE_SVG_FILE"));
                if (r == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    if (!file.isFile()) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                NbBundle.getMessage(SVGViewTopComponent.class, "ERROR_NotSVGFile", file),
                                NotifyDescriptor.Message.WARNING_MESSAGE
                                ));
                        return;
                    } else {
                        try {
                            m_sceneMgr.getPerseusController().mergeImage(file);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);                        
                        }
                        repaintAll();
                    }
                }
            }
    };            
    
    public SVGViewTopComponent() {    
        super();
    } 
    
    private SVGViewTopComponent(SVGDataObject svgDataObject) {
        this.svgDataObject = svgDataObject;
        m_sceneMgr         = new SceneManager(svgDataObject, content);
        m_sceneMgr.initialize();
        initialize();
    }

    private PerseusController getPerseus() {
        return m_sceneMgr.getPerseusController();
    }
    
    private class SVGCookie implements SelectionCookie, AnimationCookie {

        public void startAnimation(final SVGDataObject doj, final DocumentElement de) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    System.out.println("Starting animation");
                    startAnimationAction.actionPerformed(null);
                    String id = doj.getModel().getElementId(de);
                    m_sceneMgr.getPerseusController().startAnimation(id);
                }
            });
        }

        public void stopAnimation(final SVGDataObject doj, final DocumentElement de) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    System.out.println("Stopping animation");
                    String id = doj.getModel().getElementId(de);
                    m_sceneMgr.getPerseusController().stopAnimation(id);
                }
            });
        }

        public void updateSelection(final SVGDataObject doj, final DocumentElement de, boolean doubleClick) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    System.out.println("Updating selection");
                    String id = doj.getModel().getElementId(de);
                    m_sceneMgr.setSelection(id);
                }
            });
        }        
    }
    
    public synchronized Lookup getLookup() {
        if (lookup == null) {
            Lookup elementLookup = new AbstractLookup(content);
        
            lookup = Lookups.fixed( new Object[]{ 
                new FilterNode( svgDataObject.getNodeDelegate(), 
                                null, 
                                new ProxyLookup(new Lookup[]{new SVGElementNode(elementLookup).getLookup(),
                                                             svgDataObject.getNodeDelegate().getLookup()
                                                             })),
                new SVGCookie()
            });
        }
        
        return lookup;        
    }
    
    private void initialize(){
        initComponents();    

        nameChangeL = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (DataObject.PROP_COOKIE.equals(evt.getPropertyName()) ||
                DataObject.PROP_NAME.equals(evt.getPropertyName())) {
                    updateName();
                }
            }
        };
        
        //Project p = null;// = svgDataObject.getPrimaryFile();
        svgDataObject.addPropertyChangeListener(WeakListeners.propertyChange(nameChangeL, svgDataObject));
                
        //add(toolbar  = createToolBar(), BorderLayout.NORTH);
        toolbar = createToolBar();
        //enableComponentsInToolbar(toolbar, false);
        
        changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() == slider){
                    getPerseus().pauseAnimator();
                    currentTimeSpinner.removeChangeListener(changeListener);
                    float currentTime = ((float)slider.getValue()) * DEFAULT_STEP;                    
                    currentTimeSpinner.setValue(new Float(currentTime));
                    getPerseus().setAnimatorTime(currentTime);                    
                    currentTimeSpinner.addChangeListener(changeListener);
                } else if (e.getSource() == maximumTimeSpinner){
                    getPerseus().pauseAnimator();
                    currentMaximum = ((Float)maximumTimeSpinner.getValue()).floatValue();
                    slider.setMaximum((int)(currentMaximum / DEFAULT_STEP));
                    currentTimeSpinner.setModel(new SpinnerNumberModel((Float)currentTimeSpinner.getModel().getValue(), new Float(0.0), (Float)maximumTimeSpinner.getValue(), new Float(0.1)));
                } else if (e.getSource() == currentTimeSpinner){
                    getPerseus().pauseAnimator();
                    slider.removeChangeListener(changeListener);
                    float currentTime = ((Float)currentTimeSpinner.getValue()).floatValue();
                    slider.setValue((int)(currentTime / DEFAULT_STEP));
                    getPerseus().setAnimatorTime(currentTime);                    
                    slider.addChangeListener(changeListener);
                } 
            }            
        };
        slider.addChangeListener( changeListener );
        maximumTimeSpinner.addChangeListener(changeListener);
        currentTimeSpinner.addChangeListener(changeListener);
                
        basePanel = new JPanel();
        basePanel.setBackground(Color.WHITE);
        add(basePanel, BorderLayout.CENTER);
        updateName();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    /**
     * Obtain the SVGViewTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized SVGViewTopComponent findInstance(SVGDataObject svgDataObject) {
        SVGViewTopComponent component = new SVGViewTopComponent(svgDataObject);
        return component;
    }
    
    public JComponent getToolbar() {
        return toolbar;
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }
    
    public void componentOpened() {
        addSvgPanel();
    }
    
    public void componentClosed() {
        removeSvgPanel();
    }
     
    private void addSvgPanel(){
        assert SwingUtilities.isEventDispatchThread() : "Not in AWT event dispach thread";  //NOI18N
        final LoadPanel loadPanel = new LoadPanel();
        basePanel.add(loadPanel); 
        basePanel.setLayout(new BorderLayout());
    }
            
    public void onShow() {        
        if ( getModel().isChanged()) {
            updateImage();
            getModel().setChanged(false);
        }
    }

    public void componentHidden(){
        //TODO
/*
        if (svgAnimator != null && svgAnimator.getState() != SVGAnimatorImpl.STATE_STOPPED){
            svgAnimator.stop();
        }
*/
    }
    
    
    
    
    
    
    private void removeSvgPanel(){
        assert SwingUtilities.isEventDispatchThread() : "Not in AWT event dispach thread";  //NOI18N
        if (m_img != null){
            content.remove(m_img);
            m_img = null;
        }
        
        basePanel.removeAll();
            //TODO
/*
        if (imagePanel != null) {
            imagePanel.removeMouseListener(mouseListener);
            imagePanel.removeMouseMotionListener(mouseMotionListener);
            imagePanel = null;
        }

        if (svgAnimator != null && svgAnimator.getState() != SVGAnimatorImpl.STATE_STOPPED){
            svgAnimator.stop();
        }
        svgAnimator = null;
*/
//        svgImage = null;
        //enableComponentsInToolbar(toolbar, false);        
    }
    
    /** Creates cloned object which uses the same underlying data object. */
    protected CloneableTopComponent createClonedObject () {
        return new SVGViewTopComponent(svgDataObject);
    }
         
    protected String preferredID() {
        return PREFERRED_ID;
    }
    
    /** Updates the name and tooltip of this top component according to associated data object. */
    private void updateName () {
        // update name
        String name = svgDataObject.getNodeDelegate().getDisplayName();
        setName(name);
        // update tooltip
        FileObject fo = svgDataObject.getPrimaryFile();
        setToolTipText(FileUtil.getFileDisplayName(fo));
    }

/*    
    public void updateView(SVGDataObject svgDataObject) {
        this.svgDataObject = svgDataObject;
        updateName();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                removeSvgPanel();
                addSvgPanel();
            }
        });
    }
*/    
    private JToolBar createToolBar(){
        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new GridBagLayout());
        toolbar.setFloatable(false);
        toolbar.setFocusable(true);
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        toolbar.setBorder(b);
        GridBagConstraints constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;   
        constrains.insets = new Insets(0, 3, 0, 2);
        //constrains.weighty = 1.0;
        //constrains.fill = GridBagConstraints.VERTICAL;
        toolbar.add(createToolBarSeparator(), constrains);
        
        slider = new JSlider(JSlider.HORIZONTAL, 0, (int)(currentMaximum/DEFAULT_STEP),0);
        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;
        toolbar.add(slider, constrains);
        toolbar.add(Box.createHorizontalStrut(11));
        currentTimeSpinner = new JSpinner();
        currentTimeSpinner.setToolTipText(NbBundle.getMessage(SVGViewTopComponent.class, "HINT_CurrentTime")); //NOI18N
        currentTimeSpinner.setModel(new SpinnerNumberModel(new Float(0), new Float(0.0), new Float(currentMaximum), new Float(0.1)));
        Font font = currentTimeSpinner.getFont();
        FontMetrics fm = currentTimeSpinner.getFontMetrics(font);
        int w = fm.stringWidth("000.0");
        Dimension d = currentTimeSpinner.getPreferredSize();
        d.width = d.width + w;
        currentTimeSpinner.setPreferredSize(d);
        
        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;
        JLabel currentTimeLabel = new JLabel(NbBundle.getMessage(SVGViewTopComponent.class, "LBL_CurrentTime"));
        toolbar.add( currentTimeLabel, constrains);
        toolbar.add(Box.createHorizontalStrut(4));

        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;        
        toolbar.add(currentTimeSpinner, constrains);
        currentTimeLabel.setLabelFor(currentTimeSpinner);
        toolbar.add(Box.createHorizontalStrut(4));
        
        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;
        JLabel sec = new JLabel(NbBundle.getMessage(SVGViewTopComponent.class, "LBL_Seconds"));
        toolbar.add( sec, constrains);
        toolbar.add(Box.createHorizontalStrut(11));
        
        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;
        JLabel endTime = new JLabel(NbBundle.getMessage(SVGViewTopComponent.class, "LBL_EndTime"));
        toolbar.add( endTime, constrains);
        toolbar.add(Box.createHorizontalStrut(4));

        maximumTimeSpinner = new JSpinner();
        maximumTimeSpinner.setToolTipText(NbBundle.getMessage(SVGViewTopComponent.class, "HINT_EndTime"));
        maximumTimeSpinner.setModel(new SpinnerNumberModel(new Float(currentMaximum), new Float(0.0), null, new Float(0.1)));
        maximumTimeSpinner.setPreferredSize(d);
        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;        
        toolbar.add(maximumTimeSpinner, constrains);
        endTime.setLabelFor(maximumTimeSpinner);
        toolbar.add(Box.createHorizontalStrut(4));

        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;                
        JLabel sec2 = new JLabel(NbBundle.getMessage(SVGViewTopComponent.class, "LBL_Seconds2")); 
        toolbar.add( sec2, constrains);
        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;   
        constrains.insets = new Insets(0, 3, 0, 2);
        //constrains.weighty = 1.0;
        //constrains.fill = GridBagConstraints.VERTICAL;
        toolbar.add(createToolBarSeparator(), constrains);
        
        buttonListener = new ButtonMouseListener();

        initButton(toolbar, zoomFitButton = new JButton(zoomToFitAction = new ZoomToFitAction()));  
        initCombo( toolbar, zoomComboBox = new JComboBox( ZOOM_VALUES));
        zoomComboBox.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String selection = (String) zoomComboBox.getSelectedItem();
                if (selection != null) {
                    selection = selection.trim();
                    if (selection.endsWith("%")) {
                        selection = selection.substring(0, selection.length() - 1);
                    }
                    try {
                        float zoom = Float.parseFloat(selection) / 100;
                        if (zoom > 0 && zoom < 100) {
                            m_sceneMgr.getScreenManager().setZoomRatio(zoom);
                        }
                    } catch( NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }            
        });
        initButton(toolbar, zoomInButton = new JButton(zoomInAction = new ZoomInAction()));
        initButton(toolbar, zoomOutButton = new JButton(zoomOutAction = new ZoomOutAction()));
        toolbar.add(createToolBarSeparator(), constrains);
        
        initButton(toolbar, toolTipToggleButton = new JToggleButton(toolTipAction = new ToggleTooltipAction()));
        
        initButton(toolbar, hoverToggleButton = new JToggleButton(highlightAction = new ToggleHighlightAction()));

        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;   
        constrains.insets = new Insets(0, 3, 0, 2);
        //constrains.weighty = 1.0;
        //constrains.fill = GridBagConstraints.VERTICAL;
        toolbar.add(createToolBarSeparator(), constrains);
                
        initButton(toolbar, scaleToConfigurationToggleButton = new JToggleButton(scaleAction = new ToggleScaleAction()));
        initButton(toolbar, showViewBoxToggleButton = new JToggleButton(showViewBoxAction = new ToggleShowViewBoxAction()));               

        toolbar.add(createToolBarSeparator(), constrains);
        
        initButton(toolbar, startAnimationButton = new JToggleButton( startAnimationAction));
        startAnimationButton.setSelected(false);
        initButton(toolbar, pauseAnimationButton = new JButton( pauseAnimationAction));
        initButton(toolbar, stopAnimationButton  = new JButton( stopAnimationAction));
        
        toolbar.add(createToolBarSeparator(), constrains);
        initButton(toolbar, allowEditToggleButton = new JToggleButton(allowEditAction));               
        allowEditToggleButton.setSelected(!m_sceneMgr.isReadOnly());
        
        for (Action action : m_sceneMgr.getMenuActions()) {
            initButton(toolbar, new JButton( action));
        }       
        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;
        constrains.fill = GridBagConstraints.HORIZONTAL;
        constrains.weightx = 1.0;
        toolbar.add(new JPanel(), constrains);
        
        return toolbar;
    }
    
    private void initButton(JComponent bar, AbstractButton button){
        Border buttonBorder = UIManager.getBorder ("nb.tabbutton.border"); //NOI18N
        if ( buttonBorder != null ) {
            button.setBorder( buttonBorder );
        }
        GridBagConstraints constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;
        
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        if (button instanceof JButton) {
            button.addMouseListener(buttonListener);
        }
        //@inherited fix of issue #69642. Focus shouldn't stay in toolbar
        button.setFocusable(false);

        //button.setRolloverEnabled(true);
        //button.setFocusable(true);
        //button.setFocusPainted(false);
        //button.set;
        bar.add(button, constrains);
    }
    
    private void initCombo(JComponent bar, JComboBox comboBox){
        GridBagConstraints constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;
        
        //@inherited fix of issue #69642. Focus shouldn't stay in toolbar
        comboBox.setFocusable(false);
        
        Dimension size = comboBox.getPreferredSize();
        comboBox.setPreferredSize(size);
        comboBox.setSize(size);
        comboBox.setMinimumSize(size);
        comboBox.setMaximumSize(size);

        comboBox.setEditable(true);
        
        bar.add(comboBox, constrains);
    }

    private void updateAnimationActions() {
        int state = getPerseus().getAnimatorState();
        boolean isReadOnly = m_sceneMgr.isReadOnly();
        
        startAnimationAction.setEnabled(isReadOnly && state == PerseusController.ANIMATION_NOT_RUNNING);
        //startAnimationButton.setEnabled(startAnimationAction.isEnabled());
        
        boolean isActive = isReadOnly && (state == PerseusController.ANIMATION_RUNNING ||
                state == PerseusController.ANIMATION_PAUSED);
        pauseAnimationAction.setEnabled(isActive);
        stopAnimationAction.setEnabled(isActive);
        //pauseAnimationButton.setEnabled(isActive);
        //stopAnimationButton.setEnabled(isActive);
    }
    
    private static JSeparator createToolBarSeparator () {
        JSeparator toolBarSeparator = new JSeparator (JSeparator.VERTICAL);
        Dimension dim = new Dimension(2, 22);
        toolBarSeparator.setPreferredSize(dim);
        toolBarSeparator.setSize(dim);
        toolBarSeparator.setMinimumSize(dim);
        return toolBarSeparator;
    }
/*
    private static void enableComponentsInToolbar(Container component, boolean enable){
        Component[] components = component.getComponents();
        for (int i = 0; i < components.length; i++) {
            components[i].setEnabled(enable);
            enableComponentsInToolbar((Container) components[i], enable);
        }
    }
*/    
    /** Serialize this top component. Serializes its data object in addition
     * to common superclass behaviour.
     * @param out the stream to serialize to
     */
    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(svgDataObject);
        //out.writeFloat(currentTime);
        out.writeFloat(currentMaximum);
        //TODO serialize somewhere else
//        out.writeBoolean(showToolTip);        
//        out.writeBoolean(showHover);
//        out.writeBoolean(showViewBox);
//        out.writeFloat(zoomRatio);
    }
    
    /** Deserialize this top component.
     * Reads its data object and initializes itself in addition
     * to common superclass behaviour.
     * @param in the stream to deserialize from
     */
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        svgDataObject  = (SVGDataObject)in.readObject();
        //currentTime    = in.readFloat();
        currentMaximum = in.readFloat();
        //TODO deserialize somewhere else
//        showToolTip    = in.readBoolean();
//        showHover      = in.readBoolean();
//        showViewBox    = in.readBoolean();
//        zoomRatio      = in.readFloat();
        
        // to reset the listener for FileObject changes //todo???
        //TODO replace???
        //((SVGOpenSupport)svgDataObject.getCookie(SVGOpenSupport.class)).prepareViewer();
        //initialize();
    }    

    protected void updateZoomCombo() {
        zoomComboBox.getEditor().setItem( Integer.toString( (int) (m_sceneMgr.getScreenManager().getZoomRatio() * 100 + 0.5)) + "%");
    }
              
    public synchronized void updateImage() {
        assert SwingUtilities.isEventDispatchThread() : "Not in AWT event dispach thread";  //NOI18N

        basePanel.removeAll();
/*        
        if (svgImage != null) {
            content.remove(svgImage);
        }
        
        svgImage      = null;
 */
        
        if ( parsingTask != null) {
            parsingTask.cancel();
        }
        try {
            parsingTask = new ParsingTask(svgDataObject, this);
            basePanel.add( parsingTask.getPanel(), BorderLayout.CENTER);
            parsingTask.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }        

    private SVGImage m_img;
    
    void showImage(SVGImage img) {
        assert SwingUtilities.isEventDispatchThread() : "Not in AWT event dispach thread";  //NOI18N
        basePanel.removeAll();
        m_img = img;
        content.add(img);
        
        m_sceneMgr.setImage(img);
        final JComponent topComponent = m_sceneMgr.getComposerGUI();
        basePanel.add( topComponent, BorderLayout.CENTER);
        m_sceneMgr.registerPopupActions( new Action[]{
            SystemAction.get(EditAction.class),
            SystemAction.get(ExternalEditAction.class),
            null,
            SystemAction.get(SaveAsImageAction.class),
            SystemAction.get(SaveAnimationAsImageAction.class),
            SystemAction.get(SaveElementAsImage.class),
            null,
            insertGraphicsAction,
            null,
            zoomToFitAction,
            zoomInAction,
            zoomOutAction,
            null,
            highlightAction,
            toolTipAction,
            null,
            scaleAction,
            showViewBoxAction,
            null,
            startAnimationAction,
            pauseAnimationAction,
            stopAnimationAction}, lookup);

        //enableComponentsInToolbar(toolbar, true);
        updateZoomCombo();
        updateAnimationActions();

        SVGLocatableElement elem = m_sceneMgr.getPerseusController().getViewBoxMarker();
        ScreenManager smgr = m_sceneMgr.getScreenManager();
        
        if (elem == null) {
            showViewBoxAction.setEnabled(false);
            smgr.setShowAllArea(true);
        } else {
            showViewBoxAction.setEnabled(true);
        }
        showViewBoxToggleButton.setSelected(smgr.getShowAllArea());
        
        hoverToggleButton.setSelected(smgr.getHighlightObject());
        toolTipToggleButton.setSelected(smgr.getShowTooltip());
        
        //updateSelection(actualSelection);
        repaintAll();
    }

    private void repaintAll() {
        m_sceneMgr.getScreenManager().getAnimatorView().invalidate();
        basePanel.validate();
        basePanel.repaint();        
    }
    
    private SVGFileModel getModel() {
        return svgDataObject.getModel();
    }
    
    /**
     * Loading panel
     */
    private static class LoadPanel extends JPanel{
        LoadPanel(){
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            JLabel loadingLabel = new JLabel(NbBundle.getMessage(SVGViewTopComponent.class, "MSG_Loading")); //NOI18N
            loadingLabel.setBackground(Color.WHITE);
            Font font = loadingLabel.getFont();
            loadingLabel.setFont(font.deriveFont(20.0f));
            add(loadingLabel, BorderLayout.CENTER);
        }
    }
   
    /**
     * Simple proxy node for selected SVG Elements
     */
    private class SVGElementNode extends AbstractNode {
        SVGElementNode(Lookup lookup){
            super(Children.LEAF, lookup);
        }
        
        protected Class[] cookieClasses(){
            return new Class[] {SVGLocatableElement.class};
        }
        
        public Action[] getActions(boolean context) {
            return new SystemAction[] {SystemAction.get(SaveElementAsImage.class)};
        }
    }
    
    private class ButtonMouseListener extends org.openide.awt.MouseUtils.PopupMouseAdapter {
        public void mouseEntered(MouseEvent evt) {
            if (evt.getSource() instanceof JButton) {
                JButton button = (JButton)evt.getSource();
                if (button.isEnabled()){
                    button.setContentAreaFilled(true);
                    button.setBorderPainted(true);
                }
            }
//            AbstractButton b = (AbstractButton)e.getComponent();
//            b.getModel().setRollover(true);
        }
        public void mouseExited(MouseEvent evt) {
            if (evt.getSource() instanceof JButton) {
                JButton button = (JButton)evt.getSource();
                if (button.isEnabled()){
                    button.setContentAreaFilled(false);
                    button.setBorderPainted(false);
                }
            }
//            AbstractButton b = (AbstractButton)e.getComponent();
//            b.getModel().setRollover(false);
        }        

        protected void showPopup(MouseEvent evt) {
        }
    }
        
    private class ToggleScaleAction extends AbstractSVGAction {
        private float m_previousZoomRatio;
        
        ToggleScaleAction() {
            super( "scale.png", "HINT_ToggleScale", "LBL_ToggleScale");
        }
        
        public void actionPerformed(ActionEvent e) {
            ScreenManager smgr = m_sceneMgr.getScreenManager();
            
            doScale = !doScale;
            if (doScale) {
                m_previousZoomRatio = smgr.getZoomRatio();

                
                String activeConfiguration = null;
                final FileObject primaryFile = svgDataObject.getPrimaryFile ();
                Project p = FileOwnerQuery.getOwner (primaryFile);
                if (p != null && p instanceof J2MEProject){
                    J2MEProject project = (J2MEProject) p;
                    activeConfiguration = project.getConfigurationHelper().getActiveConfiguration().getDisplayName();
                }
                Dimension dim       = ScreenSizeHelper.getCurrentDeviceScreenSize(primaryFile, activeConfiguration);
                Rectangle imgBounds = smgr.getImageBounds();
                
                float ratio = (float) (dim.getHeight() / imgBounds.getHeight());
                smgr.setZoomRatio(ratio * m_previousZoomRatio);
            } else {
                smgr.setZoomRatio(m_previousZoomRatio);
            }
            zoomInAction.setEnabled(!doScale);
            zoomOutAction.setEnabled(!doScale);
            zoomToFitAction.setEnabled(!doScale);
            zoomComboBox.setEnabled(!doScale);
            repaint();
        }
    }    
/*    
    
    private class ImageScaleHelper {
        private SVGSVGElement element;
        private J2MEProject project;
        private PropertyChangeListener pcl;
        private boolean scale;
        
        ImageScaleHelper(final SVGSVGElement element){
            this.element = element;                    
            final FileObject primaryFile = svgDataObject.getPrimaryFile ();
            Project p = FileOwnerQuery.getOwner (primaryFile);
            if (p != null && p instanceof J2MEProject){
                project = (J2MEProject) p;
                final ProjectConfigurationsHelper helper = project.getConfigurationHelper();
                helper.addPropertyChangeListener( WeakListeners.propertyChange(pcl = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        scale(scale);
                    }
                }, helper));
            }
        }
        
        void scale(final boolean scale){
            this.scale = scale;
            if (svgAnimator != null && svgAnimator.getState() != SVGAnimatorImpl.STATE_STOPPED){
                svgAnimator.invokeLater(new Runnable() {
                    public void run() {
                        if (!scale){
                            element.setCurrentScale(1.0f);
                            SVGViewTopComponent.this.imagePanel.repaint();
                            return;
                        }
                        String activeConfiguration = null;
                        final FileObject primaryFile = svgDataObject.getPrimaryFile ();
                        Project p = FileOwnerQuery.getOwner (primaryFile);
                        if (p != null && p instanceof J2MEProject){
                            project = (J2MEProject) p;
                            activeConfiguration = project.getConfigurationHelper().getActiveConfiguration().getDisplayName();
                        }
                        Dimension dim = ScreenSizeHelper.getCurrentDeviceScreenSize(primaryFile, activeConfiguration);
                        Dimension dim2 = imagePanel.getSize();
                        double ratio = dim.getHeight() / dim2.getHeight();
                        element.setCurrentScale((float)ratio);                        
                        SVGViewTopComponent.this.imagePanel.repaint();
                    }
                });
            }                
        }
    }
    
    
  */  
    
    
    
    
    private class ToggleHighlightAction extends AbstractAction implements Presenter.Popup {
        ToggleHighlightAction (){
            putValue(Action.SMALL_ICON, new ImageIcon(Utilities.loadImage("org/netbeans/modules/mobility/svgcore/resources/highlight.png"))); //NOI18N
            putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(SVGViewTopComponent.class, "HINT_ToggleHighlight")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent e) {
            ScreenManager smgr = m_sceneMgr.getScreenManager();
            smgr.setHighlightObject(!smgr.getHighlightObject());
            repaint();
        }

        public JMenuItem getPopupPresenter() {
            JMenuItem menu = new JMenuItem(this);
            menu.setText(NbBundle.getMessage(SVGViewTopComponent.class, "LBL_ToggleHighlight")); //NOI18N
            menu.setToolTipText(null);
            menu.setIcon(null);
            return menu;
        }
    }    

    private class ToggleTooltipAction extends AbstractAction implements Presenter.Popup {
        ToggleTooltipAction (){
            putValue(Action.SMALL_ICON, new ImageIcon(Utilities.loadImage("org/netbeans/modules/mobility/svgcore/resources/help.png"))); //NOI18N
            putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(SVGViewTopComponent.class, "HINT_TogglTooltip")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent e) {
            ScreenManager smgr = m_sceneMgr.getScreenManager();
            smgr.setShowTooltip(!smgr.getShowTooltip());
            repaint();
        }

        public JMenuItem getPopupPresenter() {
            JMenuItem menu = new JMenuItem(this);
            menu.setText(NbBundle.getMessage(SVGViewTopComponent.class, "LBL_ToggleTooltip")); //NOI18N
            menu.setToolTipText(null);
            menu.setIcon(null);
            return menu;
        }
    }    
   
    private class ToggleShowViewBoxAction extends AbstractAction implements Presenter.Popup {
        ToggleShowViewBoxAction (){
            putValue(Action.SMALL_ICON, new ImageIcon(Utilities.loadImage("org/netbeans/modules/mobility/svgcore/resources/showViewBox.png"))); //NOI18N
            putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(SVGViewTopComponent.class, "HINT_ToggleShowViewBox")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent e) {
            ScreenManager smgr = m_sceneMgr.getScreenManager();
            smgr.setShowAllArea(!smgr.getShowAllArea());
            repaint();
        }

        public JMenuItem getPopupPresenter() {
            JMenuItem menu = new JMenuItem(this);
            menu.setText(NbBundle.getMessage(SVGViewTopComponent.class, "LBL_ToggleShowViewBox")); //NOI18N
            menu.setToolTipText(null);
            menu.setIcon(null);
            return menu;
        }
    }    
    
    private class ZoomToFitAction extends AbstractSVGAction {
        ZoomToFitAction() {
            super( "zoom_fit.png", "HINT_ZoomFit", "LBL_ZoomFit");
        }
        
        public void actionPerformed(ActionEvent e) {
            ScreenManager smgr        = m_sceneMgr.getScreenManager();
            Rectangle     imgBounds   = smgr.getImageBounds();
            Rectangle     panelBounds = smgr.getComponent().getBounds();
            
            float zoomRatio = Math.min( (float) (panelBounds.width - 2 * SVGImagePanel.CROSS_SIZE)/ imgBounds.width,
                                        (float) (panelBounds.height - 2 * SVGImagePanel.CROSS_SIZE) / imgBounds.height);
            smgr.setZoomRatio(zoomRatio * smgr.getZoomRatio());
            updateZoomCombo();
        }
    }        
    
    private class ZoomInAction extends AbstractSVGAction {
        ZoomInAction() {
            super( "zoom_in.png", "HINT_ZoomIn", "LBL_ZoomIn");
        }
        
        public void actionPerformed(ActionEvent e) {
            ScreenManager smgr = m_sceneMgr.getScreenManager();
            smgr.setZoomRatio(smgr.getZoomRatio() * ZOOM_STEP);
            updateZoomCombo();
        }
    }        
    
    private class ZoomOutAction extends AbstractSVGAction {
        ZoomOutAction() {
            super( "zoom_out.png", "HINT_ZoomOut", "LBL_ZoomOut");
        }
        
        public void actionPerformed(ActionEvent e) {
            ScreenManager smgr = m_sceneMgr.getScreenManager();
            smgr.setZoomRatio(smgr.getZoomRatio() / ZOOM_STEP);
            updateZoomCombo();
        }
    }        
}
