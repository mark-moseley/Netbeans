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
package org.netbeans.modules.mobility.svgcore.export;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.ScalableGraphics;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;

public final class SaveElementAsImage extends CookieAction{// implements Presenter.Popup, Presenter.Menu {
    
    protected void performAction(Node[] activatedNodes) {
        Lookup l = activatedNodes[0].getLookup();
        SVGLocatableElement element = (SVGLocatableElement) l.lookup(SVGLocatableElement.class);
        SVGDataObject doj = (SVGDataObject) l.lookup(SVGDataObject.class);
        SVGImage image = (SVGImage) l.lookup(SVGImage.class);

        renderElement(doj.getPrimaryFile(), element, image);        
    }
    
    protected int mode() {
        return CookieAction.MODE_ONE;
    }
    
    public String getName() {
        return NbBundle.getMessage(SaveElementAsImage.class, "CTL_SVGExportAction");
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {
            SVGLocatableElement.class
        };
    }
    
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    /**
     * First method to render a single element, based on the 226 API.
     *
     * @param elt the SVGLocatableElement to render
     * @param svgImage the containing SVGImage
     * @param doc the related Document
     * @param svg the root SVG element.
     */
    private void renderElement(final FileObject primaryFile, final SVGLocatableElement elt,
            final SVGImage svgImage ) {
        
        J2MEProject project = null;
        Project p = FileOwnerQuery.getOwner (primaryFile);
        if (p != null && p instanceof J2MEProject){
            project = (J2MEProject) p;
        }

        //SVGRasterizerPanel panel = new SVGRasterizerPanel(ScreenSizeHelper.getCurrentDeviceScreenSize(primaryFile, null), project != null);
        SVGAnimationRasterizerPanel panel = new SVGAnimationRasterizerPanel(ScreenSizeHelper.getCurrentDeviceScreenSize(primaryFile, null), project != null,false);
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(SaveAnimationAsImageAction.class, "TITLE_ImageExport"));
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        int imageWidth = panel.getImageWidth();
        int imageHeigth = panel.getImageHeigth();
        AnimationRasterizer.ImageType imageType = panel.getSelectedImageFormat();
        float compressionQuality = panel.getCompressionQuality();
        boolean progressive = panel.isProgressive();        
        boolean forAllConfig = panel.isForAllConfigurations();

        if (dd.getValue() == DialogDescriptor.OK_OPTION){
            //
            // The following adjusts the element's transform so that its user space is identical
            // to the root element's user space.
            //
            int h = svgImage.getViewportHeight();
            int w = svgImage.getViewportWidth();
            
            SVGSVGElement svg = (SVGSVGElement) svgImage.getDocument().getDocumentElement();
            
            // svg -> screen
            SVGMatrix svgCTM = svg.getScreenCTM();
            
            // element -> svg -> screen
            SVGMatrix eltCTM = elt.getScreenCTM();
            
            // screen -> svg
            SVGMatrix svgICTM = svgCTM.inverse();
            
            // elt-> svg matrix
            SVGMatrix eltToSvg = svgICTM.mMultiply(eltCTM);
            
            // The current elt transform, if any
            SVGMatrix origTxf = elt.getMatrixTrait("transform");
            SVGMatrix eltTxf = elt.getMatrixTrait("transform");
            
            SVGMatrix toSvgSpace= eltTxf.mMultiply(eltToSvg.inverse());
            
            // Get the current viewBox
            SVGRect viewBox = svg.getRectTrait("viewBox");
            
            // Get the overall content's bounding box, including our element.
            SVGRect allBBox = svg.getBBox();
            
            // Now, move our element 'away' from all content (move it to the right of the content)
            SVGRect bbox = elt.getBBox();
            toSvgSpace.mTranslate(-bbox.getX() + allBBox.getX() + allBBox.getWidth(), 0);
            elt.setMatrixTrait("transform", toSvgSpace);
            
            // Now, set the new viewBox.
            bbox.setX(allBBox.getX() + allBBox.getWidth());
            svg.setRectTrait("viewBox", bbox);

            AnimationRasterizer.exportElement(primaryFile, project, svgImage, elt.getId(), imageWidth, imageHeigth, 
                                                imageType,progressive,compressionQuality,
                                                0.0f, 0.0f, 1, true, forAllConfig);
    
            svg.setRectTrait("viewBox", viewBox);
            elt.setMatrixTrait("transform", origTxf);
            svgImage.setViewportWidth(w);
            svgImage.setViewportHeight(h);
        }

//    /**
//     * The overlay image.
//     */
//        BufferedImage bi = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
//        
//        Graphics g = bi.createGraphics();
//        g.setColor(Color.black);
//        g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
//        ScalableGraphics sg = ScalableGraphics.createInstance();
//        sg.bindTarget(g);
//        
//        // To avoid still rendering content outside the viewport, we set the viewport
//        // to have the same aspect ratio as the viewBox.
//        // We know the height is non-zero because we got here through hit detection.
//        float ar = bbox.getWidth() / bbox.getHeight();
//        int iw = svgImage.getViewportWidth();
//        int ih = svgImage.getViewportHeight();
//        int vw = 0;
//        int vh = 0;
//        if (ar > 1) {
//            vw = bi.getWidth();
//            vh = (int) (vw / ar);
//        } else {
//            vh = bi.getHeight();
//            vw = (int) (vh * ar);
//        }
//        svgImage.setViewportWidth(vw);
//        svgImage.setViewportHeight(vh);
//        sg.render(0, 0, svgImage);
//        sg.releaseTarget();
        
        // Restore values.
    }

//    public JMenuItem getPopupPresenter() {
//        JMenuItem result = new JMenuItem("Export...");  //remember JMenu is a subclass of JMenuItem
//        return result;
//    }
}

