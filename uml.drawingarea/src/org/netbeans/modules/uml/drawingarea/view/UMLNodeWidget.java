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
package org.netbeans.modules.uml.drawingarea.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.ResourceTable;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.actions.ResizeStrategyProvider;
import org.netbeans.modules.uml.drawingarea.actions.WindowStyleResizeProvider;
import org.netbeans.modules.uml.drawingarea.border.ResizeBorder;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeReader;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.view.SwitchableWidget.SwitchableViewManger;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/**
 *
 * @author treyspiva
 */
public abstract class UMLNodeWidget extends Widget 
        implements DiagramNodeWriter, DiagramNodeReader, PropertyChangeListener, UMLWidget
{   
    private static final int RESIZE_SIZE = 5;
        
    protected enum ButtonLocation {Left, op, Right, Bottom};
    public enum RESIZEMODE{PREFERREDBOUNDS,PREFERREDSIZE,MINIMUMSIZE};
    private static final String RESIZEMODEPROPERTY = "ResizeMode";
    
    private RESIZEMODE lastResMode;
    private Widget childLayer = null;
    private LayerWidget decoratorLayer = null;
    
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup = new AbstractLookup(lookupContent);
    
    GraphScene scene;
    public static Preferences prefs = NbPreferences.forModule(ResourceValue.class);
    public static String DEFAULT="default";
    protected boolean useGradient = NbPreferences.forModule(DummyCorePreference.class).getBoolean("UML_Gradient_Background", true);
    private ResourceTable localResourceTable = null;
    
    public UMLNodeWidget(Scene scene)
    {
        this(scene, false);
    }

    public UMLNodeWidget(Scene scene, boolean useDefaultNodeResource)
    {
        super(scene);
        this.scene = (GraphScene)scene;
        
        setLayout(LayoutFactory.createOverlayLayout()); 
        
        if (useDefaultNodeResource)
        {
            childLayer = new CustomizableWidget(scene, getResourcePath(), "Default");
//            childLayer.setOpaque(true);
            childLayer.setForeground((Color) null);
            childLayer.setLayout(LayoutFactory.createOverlayLayout());
            addChild(childLayer);
        }
        else
        {
            childLayer = this;
        }
        decoratorLayer = new LayerWidget(scene);
        decoratorLayer.setLayout(LayoutFactory.createAbsoluteLayout());
        addChild(decoratorLayer);
        
        
        localResourceTable = new ResourceTable(scene.getResourceTable());
        ResourceValue.initResources(getResourcePath(), childLayer);
        
    }
    
    public ResizeStrategyProvider getResizeStrategyProvider()
    {
        return new WindowStyleResizeProvider(getResizeControlPoints());
    }
    
    
    protected void addToLookup(Object item)
    {
        lookupContent.add(item);
    }
    
    protected void removeFromLookup(Object item)
    {
        lookupContent.remove(item);
    }
    
    ///////////////////////////////////////////////////////////////
    // Overrides
    
    @Override
    public ResourceTable getResourceTable()
    {
        return localResourceTable;
    }
    
    @Override
    public Lookup getLookup()
    {
        return lookup;
    }
    
    ///////////////////////////////////////////////////////////////
    // Helper Methods.
    
    protected void setCurrentView(Widget view)
    {
        childLayer.removeChildren();
        childLayer.addChild(view);
        revalidate();
    }
    
    protected Widget getCurrentView()
    {
        Widget retVal = null;
        
        if(childLayer.getChildren().size() > 0)
        {
            retVal = childLayer.getChildren().get(0);
        }
        
        return retVal;
    }

    
    public void initializeNode(IPresentationElement element)
    {
        
    }
    
    // display full view widget for preference preview, child class should
    // override this method to provide specific logic 
     public void initializeNode(IPresentationElement element, boolean show)
    {
        initializeNode(element);
    }
     
     public RESIZEMODE getResizeMode()
     {
         if(lastResMode==null)
         {
            recalculateResizeModeBasedOnWidget();
         }
         return lastResMode;
     }
     private void recalculateResizeModeBasedOnWidget()
     {
            if (isPreferredBoundsSet())
            {
                lastResMode=lastResMode.PREFERREDBOUNDS;
            }
            else if(getPreferredSize()!=null)
            {
                lastResMode=lastResMode.PREFERREDSIZE;
           }
            else
            {
                //default is minSize
                 lastResMode=lastResMode.MINIMUMSIZE;
            }
     }
     /**
      * set resize mode, if null resize mode will be recalculated based on widget properties
      * @param mode
      */
     public void setResizeMode(RESIZEMODE mode)
     {
         if(mode==null)recalculateResizeModeBasedOnWidget();
         else lastResMode=mode;
     }

    protected LayerWidget getDecoratorLayer()
    {
        return decoratorLayer;
    }
    
    /*
     * 
     * need to be overrided by widgets with not standard resize points
     * @return all resize points which will be used(active and visible) for resizing of selected widget
     */
    protected ResizeProvider.ControlPoint[] getResizeControlPoints()
    {
        //by default all sized are active for resize;
        return new ResizeProvider.ControlPoint[]
        {
            ResizeProvider.ControlPoint.TOP_LEFT,
            ResizeProvider.ControlPoint.TOP_CENTER,
            ResizeProvider.ControlPoint.TOP_RIGHT,
            ResizeProvider.ControlPoint.CENTER_LEFT,
            ResizeProvider.ControlPoint.BOTTOM_LEFT,
            ResizeProvider.ControlPoint.BOTTOM_CENTER,
            ResizeProvider.ControlPoint.BOTTOM_RIGHT,
            ResizeProvider.ControlPoint.CENTER_RIGHT
        };
    }
    
    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state)
    {
        boolean select = state.isSelected();
        boolean wasSelected = previousState.isSelected();

        if (select && !wasSelected)
        {
            // Allow subclasses to change the resize strategy and provider.
            ResizeStrategyProvider stratProv=getResizeStrategyProvider();
            getActions().addAction(0, ActionFactory.createResizeAction(stratProv,
                                                                       stratProv));
            //setBorder(BorderFactory.createResizeBorder(RESIZE_SIZE));
            setBorder(new ResizeBorder(RESIZE_SIZE, Color.BLACK, getResizeControlPoints()));
            System.out.println("SELECT");
            if (getResizeMode()==RESIZEMODE.PREFERREDBOUNDS)
            {
                Rectangle bnd = getPreferredBounds();
                bnd.width += 2 * RESIZE_SIZE;
                bnd.height += 2 * RESIZE_SIZE;
                setPreferredBounds(bnd);
                Point loc = getPreferredLocation();
                loc.translate(-RESIZE_SIZE, -RESIZE_SIZE);
                setPreferredLocation(loc);
                setMinimumSize(new Dimension(getResizingMinimumSize().width + RESIZE_SIZE * 2,
                                             getResizingMinimumSize().height + RESIZE_SIZE * 2));
                    System.out.println("SELECT IN PREF BOUNDS MODE");
            }
            else if(getResizeMode()==RESIZEMODE.PREFERREDSIZE)
            {
                lastResMode=lastResMode.PREFERREDSIZE;
                setPreferredSize(new Dimension(getPreferredSize().width + 2 * RESIZE_SIZE,
                                             getPreferredSize().height + 2 * RESIZE_SIZE));
                     System.out.println("SELECT IN PREF SIZE MODE");
           }
            else if(getResizeMode()==RESIZEMODE.MINIMUMSIZE)
            {
                if (getMinimumSize() == null)
                {
                    setMinimumSize(new Dimension(getBounds().width-2 * RESIZE_SIZE,getBounds().height-2 * RESIZE_SIZE));System.out.println("NEED TO SET AT LEAST MIN SIZE");
                }
                setMinimumSize(new Dimension(getMinimumSize().width + 2 * RESIZE_SIZE,
                                             getMinimumSize().height + 2 * RESIZE_SIZE));
                System.out.println("SELECT IN MIN SIZE MODE");
            }
        }
        else if (!select && wasSelected)
        {
            //Do not have access to the class to recheck, will consider if was selected is here
            //TBD add some additional possibility to check
            //if(getActions().getActions().get(0) instanceof ResizeAction)
            {
                getActions().removeAction(0);
                setBorder(BorderFactory.createEmptyBorder());
                System.out.println("DESELECT");
                if (lastResMode==lastResMode.PREFERREDBOUNDS)
                {
                    Rectangle bnd = getPreferredBounds();
                    bnd.width -= 2 * RESIZE_SIZE;
                    bnd.height -= 2 * RESIZE_SIZE;
                    setPreferredBounds(bnd);
                    Point loc = getPreferredLocation();
                    loc.translate(RESIZE_SIZE, RESIZE_SIZE);
                    setPreferredLocation(loc);
                    setMinimumSize(getResizingMinimumSize());
                    System.out.println("DESELECT IN PREF BOUNDS MODE");
                }
                else if(lastResMode==lastResMode.PREFERREDSIZE)
                {
                    setPreferredSize(new Dimension(getPreferredSize().width - 2 * RESIZE_SIZE,
                                                 getPreferredSize().height - 2 * RESIZE_SIZE));
                    System.out.println("DESELECT IN PREF SIZE MODE");
                }
                else if (lastResMode==lastResMode.MINIMUMSIZE)
                {
                    setMinimumSize(new Dimension(getMinimumSize().width - 2 * RESIZE_SIZE, getMinimumSize().height - 2 * RESIZE_SIZE));
                    System.out.println("DESELECT IN MIN SIZE MODE");
                }
            //
            }

        }
    //else do nothing
    }
    
    public List getAllChildren(Widget widget, List childList) {
        if (widget.getChildren().size() > 0) //widget has children
        {
            childList.addAll(widget.getChildren());
            for (Widget child : widget.getChildren())
            {
                getAllChildren(child, childList);
            }
        }
        return childList;
    }
      
    public void save(NodeWriter nodeWriter) {
        nodeWriter.getProperties().put(RESIZEMODEPROPERTY, getResizeMode().toString());
        setNodeWriterValues(nodeWriter, this);
        nodeWriter.beginGraphNodeWithModelBridge();
        nodeWriter.beginContained();
        //write contained
        if (getCurrentView() != null)
        {
            saveChildren(getCurrentView(), nodeWriter);
        }
        else
        {
            saveChildren(this, nodeWriter);
        }
        nodeWriter.endContained();
//        if (!scene.findNodeEdges(getObject(), true, true).isEmpty()) {
        if(this.getDependencies().size() > 0) 
        {
            saveAnchorage(nodeWriter);
        }        
        nodeWriter.endGraphNode();

    }
    
    protected void saveChildren(Widget widget, NodeWriter nodeWriter) {
        if (widget == null || nodeWriter == null)
            return;
        
        List<Widget> widList = widget.getChildren();
        for (Widget child : widList) {
            if (child instanceof DiagramNodeWriter) {
                ((DiagramNodeWriter) child).save(nodeWriter);
            } else {
                saveChildren(child, nodeWriter);
            }
        }
    }
    
    protected void setNodeWriterValues(NodeWriter nodeWriter, Widget widget) {
        nodeWriter = PersistenceUtil.populateNodeWriter(nodeWriter, widget);
        nodeWriter.setHasPositionSize(true);
        
         //populate properties key/val
//            HashMap<String, String> properties = nodeWriter.getProperties();
//            properties.put("Font", this.getFont().toString());
//            properties.put("Background", this.getBackground().toString());
//            properties.put("Visible", "True");
//            nodeWriter.setProperties(properties);
        PersistenceUtil.populateProperties(nodeWriter, widget);
    }

    protected void saveAnchorage(NodeWriter nodeWriter)
    {
        //write anchor info
        Collection depList = this.getDependencies();
        if (depList.size() > 0)
        {
            Iterator iter = depList.iterator();
            while (iter.hasNext())
            {
                Object obj = iter.next();
                if (obj instanceof Anchor)
                {
                    Anchor anchor = (Anchor)obj;
                    PersistenceUtil.addAnchor(anchor); // this is to cross ref the anchor ID from the edge later on..

                    List entryList = anchor.getEntries();
                    for (int i = 0; i < entryList.size(); i++)
                    {
                        ConnectionWidget conWid = ((Anchor.Entry) entryList.get(i)).getAttachedConnectionWidget();
                        nodeWriter.addAnchorEdge(anchor, PersistenceUtil.getPEID(conWid));
                    }
                } 
                else // assuming (obj instanceof MovableLabelWidget)
                {       
                    ((UMLLabelWidget)obj).save(nodeWriter);
                }
            }
            if (!PersistenceUtil.isAnchorListEmpty())
            {
                nodeWriter.writeAnchorage();
                //done writing the anchoredgemap.. now time to clear it.
                nodeWriter.clearAnchorEdgeMap();
            }
        }        
    }
    
    protected IPresentationElement getObject()
    {
        return (IPresentationElement)scene.findObject(this);
    }
    
    public void load(NodeInfo nodeReader)
    {        
        //get all the properties
        Hashtable<String, String> props = nodeReader.getProperties();
        //
        String resizeMode=props.get(RESIZEMODEPROPERTY);
         if(resizeMode!=null && resizeMode.length()>0)
         {
            RESIZEMODE mode=RESIZEMODE.valueOf(resizeMode);
            setResizeMode(mode);
         }
        //
        if(nodeReader.getPosition()!=null)setPreferredLocation(nodeReader.getPosition());
        if(nodeReader.getSize()!=null)
        {
            switch(getResizeMode())
            {
                case PREFERREDSIZE:
                setPreferredSize(nodeReader.getSize());
                break;
                case PREFERREDBOUNDS:
                setPreferredBounds(new Rectangle(new Point(0,0),nodeReader.getSize()));
                break;
                case MINIMUMSIZE:
                setMinimumSize(nodeReader.getSize());
                break;
            }
        }
        //get the view name
        String viewName = nodeReader.getViewName();
        //Now try to see if this is a Switchable widget.. if yes, set the correct view
        if (viewName != null && viewName.length() > 0)
        {
            SwitchableViewManger manager = this.getLookup().lookup(SwitchableViewManger.class);
            if (manager != null)
            {
                manager.switchViewTo(viewName);
            }
        }
        //now process color/font properties
        for (Enumeration<String> e = props.keys(); e.hasMoreElements(); ) {
            String key = e.nextElement();
            
            if (key.contains(ResourceValue.BGCOLOR) || key.contains(ResourceValue.FGCOLOR)) {
//                this.setBackgroundFromResource(value);
                Color bgColor = parseColor(props.get(key));
                this.getResourceTable().addProperty(key, bgColor);
                continue;
            }
            if (key.contains(ResourceValue.FONT)) {
                Font font = parseFont(props.get(key));
                this.getResourceTable().addProperty(key, font);
                continue;
            }           
        }
    }
    
    private Color parseColor(String color)
    {
        Color retVal = Color.BLACK;
        String colStr[] = null;
//        Hashtable table = new Hashtable();
        int r = 0 , g = 0 , b = 0;
        if (color != null && color.trim().length() > 0)
        {
            String subStr = color.substring(color.indexOf("[")+1, color.indexOf("]"));
            StringTokenizer tok = new StringTokenizer(subStr, ",");
            int tokenCount = tok.countTokens();
            colStr = new String[tokenCount];
            for (int i = 0; i < tokenCount; i++)
            {
                colStr[i] = tok.nextToken();
            }
            for (int j = 0; j < colStr.length; j++)
            {
                String string = colStr[j];
                int index = string.indexOf("=");
                if (index > 0)
                {
                    String colChar = Character.toString(string.charAt(index - 1));
                    String colVal = string.substring(index + 1);
                    if (colChar.equalsIgnoreCase("r"))
                        r = Integer.parseInt(colVal);
                    if (colChar.equalsIgnoreCase("g"))
                        g = Integer.parseInt(colVal);
                    if (colChar.equalsIgnoreCase("b"))
                        b = Integer.parseInt(colVal);
                }
            }
            retVal = new Color(r,g,b);
        }
        return retVal;
    }
    
     private Font parseFont(String font)
    {
        Font retVal = null;
        String fontStr[] = null;
        String family = "", name = "", style = "";
        int size = 0;
        if (font != null && font.trim().length() > 0)
        {
            String subStr = font.substring(font.indexOf("[")+1, font.indexOf("]"));
            StringTokenizer tok = new StringTokenizer(subStr, ",");
            int tokenCount = tok.countTokens();
            fontStr = new String[tokenCount];
            for (int i = 0; i < tokenCount; i++)
            {
                fontStr[i] = tok.nextToken();
            }
            for (int j = 0; j < fontStr.length; j++)
            {
                String string = fontStr[j];
                int index = string.indexOf("=");
                if (index > 0)
                {
                    String fontKey = string.substring(0, index );
                    String fontVal = string.substring(index + 1);
                    if (fontKey.equalsIgnoreCase("name"))
                        name = fontVal;
                    if (fontKey.equalsIgnoreCase("style"))
                         style = fontVal.toUpperCase();
                    if (fontKey.equalsIgnoreCase("size"))
                        size = Integer.parseInt(fontVal);
                }
            }
            retVal = Font.decode(name+"-"+style+"-"+size);
        }
        return retVal;
    }
     
    
    public void addContainedChild(Widget widget)
    {
        //figure out how to handle attr/oper
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // PropertyChangedListener
    
    /**
     * Handle property changes from the model element.  If the current view 
     * is a property change listener the event is forward the view.  Otherwise
     * nothing happens.
     * 
     * @param event The property change event.
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        Widget view = getCurrentView();
        if (view instanceof PropertyChangeListener)
        {
            PropertyChangeListener listener = (PropertyChangeListener) view;
            listener.propertyChange(event);
        }

    }
    
    
    public void duplicate(boolean setBounds, Widget target)
    {
        //todo
//        if (target instanceof UMLNodeWidget)
//        {
//            ((UMLNodeWidget) target).setNodeBackground(getNodeBackground());
//            ((UMLNodeWidget) target).setNodeForeground(getNodeForeground());
//            ((UMLNodeWidget) target).setNodeFont(getNodeFont());

            if (setBounds)
            {
                //target.setPreferredBounds(this.getBounds());
                if(getResizeMode()==RESIZEMODE.PREFERREDBOUNDS)
                {
                    target.setPreferredBounds(getPreferredBounds());
                }
                else if(getResizeMode()==RESIZEMODE.PREFERREDSIZE)
                {
                    target.setPreferredSize(getPreferredSize());
                }
                else target.setMinimumSize(this.getMinimumSize());
                if(target instanceof UMLNodeWidget)((UMLNodeWidget)target).setResizeMode(getResizeMode());
            }
//        }
    }
    
    @Override
    // to achieve resize cursors and move cursor
    protected Cursor getCursorAt(Point location)
    {
        Border border = getBorder();
        if (! (border instanceof ResizeBorder))
            return getCursor();
        
        Rectangle bounds = getBounds();
        Insets insets = border.getInsets();
        int thickness = insets.bottom;
        
        Rectangle topLeft = new Rectangle(bounds.x, bounds.y, thickness, thickness);

        Rectangle topRight = new Rectangle(bounds.x + bounds.width - thickness, bounds.y, thickness, thickness);

        Rectangle bottomLeft = new Rectangle(bounds.x, bounds.y + bounds.height - thickness, thickness, thickness);

        Rectangle bottomRight = new Rectangle(bounds.x + bounds.width - thickness, bounds.y + bounds.height - thickness, thickness, thickness);

        Point center = new Point(bounds.x+bounds.width/2,bounds.y+bounds.height/2);
        
        Rectangle topCenter = new Rectangle(center.x - thickness / 2, bounds.y, thickness, thickness);
        Rectangle bottomCenter = new Rectangle(center.x - thickness / 2, bounds.y + bounds.height - thickness, thickness, thickness);
        Rectangle leftCenter = new Rectangle(bounds.x, center.y - thickness / 2, thickness, thickness);
        Rectangle rightCenter = new Rectangle(bounds.x + bounds.width - thickness, center.y - thickness / 2, thickness, thickness);
        
        Rectangle[] rects = new Rectangle[] {topLeft, 
                                             topRight, 
                                             bottomLeft, 
                                             bottomRight, 
                                             topCenter, 
                                             bottomCenter, 
                                             leftCenter, 
                                             rightCenter};
        
        Cursor[] cursors = new Cursor[] {   Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR), 
                                            Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR),
                                            Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR),
                                            Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR),
                                            Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR),
                                            Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR),
                                            Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR),
                                            Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR) };
        for (int i= 0; i< rects.length; i++)
        {
            if (rects[i].contains(location))
            {
                return cursors[i];
            }
        }
//        if (getState().isSelected() && scene.getActiveTool().equals(DesignerTools.SELECT))
//            return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        return getCursor();
    }
    
    public void setNodeFont(Font f)
    {
//        setFont(f);
        
        getResourceTable().addProperty(getResourcePath() + "." +ResourceValue.FONT, f);
    }
    
    public void setNodeForeground(Color color)
    {
        setForegroundColor(this, color);
    }
    
    private void setForegroundColor(Widget widget, Color color)
    {
        getResourceTable().addProperty(getResourcePath() + "." +ResourceValue.FGCOLOR, color);
//        widget.setForeground(color);
//        for (Widget child: widget.getChildren())
//            setForegroundColor(child, color);
    }
    
    public void setNodeBackground(Paint paint)
    {
//        setBackground(paint);
        
        getResourceTable().addProperty(getResourcePath() + "." +ResourceValue.BGCOLOR, paint);
    }
    
    public Font getNodeFont()
    {
//        return getFont();
        return (Font)getResourceTable().getProperty(getResourcePath() + "." +ResourceValue.FONT);
    }
    
    public Color getNodeForeground()
    {
//        return getForeground();
        return (Color)getResourceTable().getProperty(getResourcePath() + "." +ResourceValue.FGCOLOR);
    }
    
    public Paint getNodeBackground()
    {
//        return getBackground();
        return (Paint)getResourceTable().getProperty(getResourcePath() + "." +ResourceValue.BGCOLOR);
    }

    
    public void refresh()
    {
        IPresentationElement pe = getObject();
        if (pe != null && pe.getFirstSubject() != null && !pe.getFirstSubject().isDeleted())
        {
            //Rectangle bounds = getBounds();
            initializeNode(getObject());
            //setPreferredBounds(bounds);            
        } else
        {
            remove();
        }
        scene.validate();
    }
    
    public void remove()
    {
        scene.removeNodeWithEdges(getObject()); 
    }
    
    protected String getResourcePath()
    {
        return getWidgetID() + "." + DEFAULT;
    }
    
    public static boolean useGradient()
    {
        return NbPreferences.forModule(DummyCorePreference.class).getBoolean("UML_Gradient_Background", true);
    }
    
    /**
     * instead of Widget::getMinimumSize this is dinamic value used in resizing
     * more used for window like resizing, when content is relative to left-top corner
     * @return size below which resize is not allowed
     */
    public Dimension getResizingMinimumSize()
    {
        return new Dimension(1,1);
    }
    
    /**
     * if node contain some content it may be used for example in resizing
     * in general it may be moved to ContainerNode
     * more used with mask like resizing when content is relative to global coordinates
     * @return
     */
    public Rectangle getContentBounds()
    {
        return null;
    }
    
    /**
     * some widgets may be added by some action like context menu and should be removed the same way instead of common delete/cut etc actions
     * (like interaction boundary widget)
     * by default most nodes can be deleted from diagram
     */
    public boolean isCopyCutDeletable()
    {
        return true;
    }
}
