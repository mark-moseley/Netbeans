/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.image;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.io.*;

import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.cookies.PrintCookie;
import org.openide.text.PrintPreferences;
import org.openide.util.NbBundle;

/** Printing support.
 * Manipulations of the image to suit page size and orientation
 * occur through the method prepareImage( PageFormat ).
 * Subclass' override this method to honour image handling.
 *
 * @author  michael wever [hair@netbeans.org]
 * @author  Marian Petras
 * @version $Revision$
 */
public class ImagePrintSupport implements PrintCookie, Printable, ImageObserver {
    /* associated dataObject */
    protected ImageDataObject dataObject;
    /* image to print */
    protected Image image;
    /* image to print */
    protected RenderedImage renderedImage;
    
    /** Creates new ImagePrintSupport */
    public ImagePrintSupport( ImageDataObject ido ) {
        dataObject = ido;
    }
    
    /** Prepare the image to fit on the given page, within the given margins. 
     * Returns null if it were unable to prepare the image for the given page. 
     * Throws a IllegalArgumentException if the page were too small for the image.
     **/
    protected static RenderedImage transformImage(RenderedImage image,
                                                  PageFormat pf)
            throws IllegalArgumentException {
        try{
            AffineTransform af = new AffineTransform();
            if( pf.getOrientation() == pf.LANDSCAPE ){
            }else{
                af.translate( (double)pf.getImageableX(), (double)pf.getImageableY() );
            }
            
            /** notify if too big for page **/
            if( pf.getImageableWidth() - pf.getImageableX() < image.getWidth()
                || pf.getImageableHeight() - pf.getImageableY() < image.getHeight() )
                    throw new IllegalArgumentException("Page too small for image");            //NOI18N
            
            /* Translate image */
            AffineTransformOp afo = new AffineTransformOp( af, AffineTransformOp.TYPE_NEAREST_NEIGHBOR );
            BufferedImage o = (BufferedImage)image;
            BufferedImage i = new BufferedImage( o.getWidth()+(int)pf.getImageableX(), o.getHeight()+(int)pf.getImageableY(), o.getType() );
            return afo.filter( (BufferedImage)image, i );
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
    
    /** Print the content of the object.  */
    public void print() {
        
        /* Try to load the image from the ImageDataObject: */
        String errMsgKey;
        try {
            image = dataObject.getImage();
            errMsgKey = (image == null) ? "MSG_CouldNotLoad" : null;    //NOI18N
        } catch (IOException ex) {
            image = null;
            errMsgKey = "MSG_ErrorWhileLoading";                        //NOI18N
        }
        assert (image == null) != (errMsgKey == null);
        
        /* If an error occured during loading, display a message and quit: */
        if (errMsgKey != null) {
            displayMessage(errMsgKey, NotifyDescriptor.WARNING_MESSAGE);
            return;
        }
        
        PrinterJob job = PrinterJob.getPrinterJob();
        Book book = new Book();
        PageFormat pf = PrintPreferences.getPageFormat(job);
        book.append( this, pf );
        job.setPageable( book );

        // Print
        try {
            if (image instanceof RenderedImage) {
                // Make sure not to print in the paper's margin.
                renderedImage = transformImage((RenderedImage) image, pf);
            }
            if (job.printDialog()) {
                job.print();
            }
        } catch (PrinterAbortException e) { // user exception
            displayMessage("CTL_Printer_Abort",                         //NOI18N
                           NotifyDescriptor.INFORMATION_MESSAGE);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        } finally {
            renderedImage = null;
            image = null;
        }
    }
    
    /**
     * Displays a localized user message.
     * It is guaranteed that the displaying routine is called from
     * the AWT event dispatching thread.
     *
     * @param  msgKey  bundle key of the message
     * @param  msgType  message type - see {@link NotifyDescriptor} fields
     */
    private void displayMessage(String msgKey, final int msgType) {
        final String msg = NbBundle.getMessage(ImagePrintSupport.class, msgKey);
        java.awt.EventQueue.invokeLater(new Runnable() { // display in the awt thread
            public void run() {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(msg, msgType));
            }
        });
    }
    
    /* Implements Printable */
    public int print(Graphics graphics, PageFormat pageFormat, int page) throws PrinterException {
        if( page != 0 ) return Printable.NO_SUCH_PAGE;
        
        Graphics2D g2 = (Graphics2D)graphics;
        if( renderedImage == null ){
            /**
             * most probably cause is image does not implement RenderedImage,
             * just draw the image then.
             **/
            graphics.drawImage(image, (int)pageFormat.getImageableX(), (int)pageFormat.getImageableY(), this );
        }else{
            g2.drawRenderedImage( renderedImage, new AffineTransform() );
        }
        return Printable.PAGE_EXISTS;
    }
    
    public boolean imageUpdate(java.awt.Image image, int flags, int param2, int param3, int param4, int param5) {
        return false;
    }
    
}
