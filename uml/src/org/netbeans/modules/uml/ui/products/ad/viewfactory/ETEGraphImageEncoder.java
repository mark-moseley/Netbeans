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



package org.netbeans.modules.uml.ui.products.ad.viewfactory;

import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.ui.swing.drawingarea.SaveAsGraphicKind;
import com.tomsawyer.editor.TSEGraphImageEncoder;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.editor.TSTransform;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.openide.ErrorManager;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * @author KevinM
 * @author Sheryl
 *
 *	This class extends the TSS image Encoder so we can save the transform.
 * You can only use the transform only after the image has been saved,
 */
public class ETEGraphImageEncoder extends TSEGraphImageEncoder
{
    protected TSTransform encoderTransform = null;
    protected String fileName;
    protected int kind;
    private float QUALITY = 75;
    
    public ETEGraphImageEncoder(TSEGraphWindow graphWindow)
    {
        super(graphWindow);
    }
    
    public ETEGraphImageEncoder(TSEGraphWindow graphWindow, String sFilename, int saveAsGraphicKind)
    {
        super(graphWindow);
        
        fileName = sFilename;
        kind = saveAsGraphicKind;
    }
    
        /*
         * Converts the graph window into an image output stream.
         */
    public boolean save(double scale)
    {
        boolean rc = false;
        try
        {
            TSEObjectUI currentUI = getGraphWindow().getGraph().getUI();
            ETImageExportGraphUI ui = new ETImageExportGraphUI();
            getGraphWindow().getGraph().setUI(ui);
            
            switch (kind)
            {
            case SaveAsGraphicKind.SAFK_JPG :
                FileImageOutputStream fio = new FileImageOutputStream(new File(fileName));
                write("jpg", fio, false,   // NOI18N
                        TSEGraphWindow.CUSTOM_SIZE, false, false, QUALITY,
                        (int)(getGraphWindow().getGraph().getFrameBounds().getWidth()*scale),
                        (int)(getGraphWindow().getGraph().getFrameBounds().getHeight()*scale));
                rc = true;
                break;
            case SaveAsGraphicKind.SAFK_PNG :
                writePNGFormat(new FileOutputStream(fileName), false,
                        TSEGraphWindow.CUSTOM_SIZE, false, false,
                        (int)(getGraphWindow().getGraph().getFrameBounds().getWidth()*scale),
                        (int)(getGraphWindow().getGraph().getFrameBounds().getHeight()*scale));
                rc = true;
                break;
            case SaveAsGraphicKind.SAFK_SVG :
                writeSVGFormat(new FileOutputStream(fileName));
                rc = true;
                break;
            }
            encoderTransform = ui.getImageTransform();
            getGraphWindow().getGraph().setUI(currentUI);
        }
        catch (Exception e)
        {
            Log.stackTrace(e);
        }
        return rc;
    }
    
        /*
         * Only valid after a call to save(), it can be used to locate objects on the graphic.
         */
    public TSTransform getEncoderTransform()
    {
        return encoderTransform;
    }
    
    
    // override TSEGraphImageEncoder.writeJPEGFormat() to use ImageIO API
    public void writeJPEGFormat(OutputStream fo)
            throws IOException, com.sun.image.codec.jpeg.ImageFormatException
    {
        writeJPEGFormat(fo, false, TSEGraphWindow.ACTUAL_SIZE, false, false, 100, 0, 0);
    }
    
    public void writeJPEGFormat( OutputStream fo,
            boolean visibleAreaOnly,
            int zoomType,
            boolean drawGrid,
            boolean selectedOnly,
            float quality,
            int width,
            int height)
            throws IOException, com.sun.image.codec.jpeg.ImageFormatException
    {
        write("jpg", fo, visibleAreaOnly, zoomType, drawGrid, selectedOnly,   // NOI18N
                quality,
                (int)getGraphWindow().getGraph().getFrameBounds().getWidth(),
                (int)getGraphWindow().getGraph().getFrameBounds().getHeight());
    }
    
    
    public void writePNGFormat(OutputStream fo)
            throws IOException, com.sun.image.codec.jpeg.ImageFormatException
    {
        writePNGFormat(fo, false, TSEGraphWindow.ACTUAL_SIZE, false, false,
                (int)getGraphWindow().getGraph().getFrameBounds().getWidth(),
                (int)getGraphWindow().getGraph().getFrameBounds().getHeight());
    }
    
    
    public void writePNGFormat(  OutputStream fo,
            boolean visibleAreaOnly,
            int zoomType,
            boolean drawGrid,
            boolean selectedOnly,
            int width,
            int height)
            throws IOException, com.sun.image.codec.jpeg.ImageFormatException
    {
        write("png", fo, visibleAreaOnly, zoomType, drawGrid, selectedOnly, 0, width, height); // NOI18N
    }
    
    public void writeSVGFormat(  OutputStream fo,
            boolean visibleAreaOnly,
            int zoomType,
            boolean drawGrid,
            boolean selectedOnly,
            int width,
            int height)
            throws IOException, com.sun.image.codec.jpeg.ImageFormatException
    {
        write("svg", fo, visibleAreaOnly, zoomType, drawGrid, selectedOnly, 0, width, height); // NOI18N
    }
    
    public void writeSVGFormat(  OutputStream fo )
            
            throws IOException, com.sun.image.codec.jpeg.ImageFormatException
    {
        write("svg", fo, false, TSEGraphWindow.ACTUAL_SIZE, false, false, 0,   // NOI18N
                (int)getGraphWindow().getGraph().getFrameBounds().getWidth(),
                (int)getGraphWindow().getGraph().getFrameBounds().getHeight());
    }
    
    public void write(String format,
            Object fo,
            boolean visibleAreaOnly,
            int zoomType,
            boolean drawGrid,
            boolean selectedOnly,
            float quality,
            int width,
            int height)
    {
        int w = width;
        int h = height;
        boolean svg = "svg".equals(format);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        if (visibleAreaOnly)
        {
            if (! svg) 
            {
                Image visible = getGraphWindow().getVisibleGraphImage(drawGrid, selectedOnly);
                if (visible instanceof BufferedImage)
                    bufferedImage = (BufferedImage)visible;
            }
        }
        else
        {
            if (zoomType == TSEGraphWindow.CUSTOM_SIZE)
                getGraphWindow().createEntireGraphImage(bufferedImage, zoomType,
                        drawGrid, selectedOnly, width, height);
            else
            {
                w = (int)getGraphWindow().getGraph().getFrameBounds().getWidth();
                h = (int)getGraphWindow().getGraph().getFrameBounds().getHeight();
                if (zoomType == TSEGraphWindow.CURRENT_ZOOM_LEVEL)
                {
                    w = (int)(width * getGraphWindow().getZoomLevel());
                    h = (int)(height * getGraphWindow().getZoomLevel());
                }
                else if (zoomType == TSEGraphWindow.FIT_IN_WINDOW)
                {
                    double d1 = getGraphWindow().getWidth()/getGraphWindow().getGraph().getFrameBounds().getWidth();
                    double d2 = getGraphWindow().getHeight()/getGraphWindow().getGraph().getFrameBounds().getHeight();
                    double ratio = Math.max(d1, d2);
                    w = (int)(w * ratio);
                    h = (int)(h * ratio);
                }
                if (! svg) 
                {
                    bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    getGraphWindow().createEntireGraphImage(bufferedImage, zoomType,
                                                            drawGrid, selectedOnly, w, h);
                }
            }
        }
        try
        {
            if ("jpg".equals(format) && (fo instanceof ImageOutputStream))   // NOI18N
            {
                Iterator iter = ImageIO.getImageWritersByFormatName("jpg");   // NOI18N
                ImageWriter writer = (ImageWriter)iter.next();
                
                ImageWriteParam iwp = writer.getDefaultWriteParam();
                iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                iwp.setCompressionQuality(quality/100);
                writer.setOutput(fo);
                IIOImage image = new IIOImage(bufferedImage, null, null);
                writer.write(null, image, iwp);
                
                ((ImageOutputStream)fo).flush();
                ((ImageOutputStream)fo).close();
                
                writer.dispose();
            }
            
            else
            {
                if (fo instanceof OutputStream)
                {
                    if ("svg".equals(format))  // NOI18N
                    {
                        // Get a DOMImplementation.                                             
                        DOMImplementation impl =
                                GenericDOMImplementation.getDOMImplementation();
                        
                        // Create an instance of org.w3c.dom.Document.
                        String svgNS = "http://www.w3.org/2000/svg";   // NOI18N
                        Document myFactory = impl.createDocument(svgNS, "svg", null);   // NOI18N
                        
                        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(myFactory);
                        // # 78585 embed the fonts for drawing strings, so that it does not rely 
                        // on system fonts for display
                        ctx.setEmbeddedFontsOn(true);
                        SVGGraphics2D svgGenerator = new SVGGraphics2D(ctx, true);
                        
                        if (visibleAreaOnly)
                        {
                            w = getGraphWindow().getWidth();
                            h = getGraphWindow().getHeight();
                        }
                        svgGenerator.setSVGCanvasSize(new Dimension(w, h));
                        if (visibleAreaOnly)
                        {
                            getGraphWindow().drawGraph(svgGenerator, drawGrid, selectedOnly);            
                        }
                        else 
                        {
                            com.tomsawyer.editor.graphics.TSEGraphics tseg = getGraphWindow().newGraphics(svgGenerator);
                            getGraphWindow().drawEntireGraph(tseg, zoomType, drawGrid, selectedOnly, w, h);
                        }

                        // Finally, stream out SVG to the output using UTF-8 encoding.
                        boolean useCSS = true; // we want to use CSS style attributes
                        Writer out = new OutputStreamWriter((OutputStream)fo, "UTF-8");  // NOI18N
                        svgGenerator.stream(out, useCSS); 
                    }
                    else
                    {
                        ImageIO.write(bufferedImage, format, (OutputStream)fo);
                    }
                    ((OutputStream)fo).flush();
                    ((OutputStream)fo).close();
                }
            }
        }
        catch (IOException ioe)
        {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
}
