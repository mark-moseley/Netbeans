/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.properties.editors;

/*
 * ColorCustomEditorOperator.java
 *
 * Created on June 13, 2002, 4:01 PM
 */

import java.awt.Color;
import javax.swing.JDialog;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling Color Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class ColorCustomEditorOperator extends NbDialogOperator {
    
    private JColorChooserOperator _colorChooser=null;
    
    /** Creates a new instance of FileCustomEditorOperator
     * @param title String title of custom editor */
    public ColorCustomEditorOperator(String title) {
        super(title);
    }
    
    /** Creates a new instance of FileCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public ColorCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }

    /** locates and returns JColorChooserOperator
     * @return JColorChooserOperator */    
    public JColorChooserOperator colorChooser() {
        if (_colorChooser==null) {
            _colorChooser=new JColorChooserOperator(this);
        }
        return _colorChooser;
    }
    
    /** returns edited color
     * @return Color */    
    public Color getColorValue() {
        return colorChooser().getColor();
    }
    
    /** sets edited color
     * @param color Color */    
    public void setColorValue(Color color) {
        colorChooser().setColor(color);
    }
    
    /** sets edited color
     * @param r int red
     * @param g int green
     * @param b int blue */    
    public void setRGBValue(int r, int g, int b) {
        colorChooser().setColor(r, g, b);
    }
    
}
