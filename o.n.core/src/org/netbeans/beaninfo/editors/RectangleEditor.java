/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.awt.Rectangle;
import java.util.ResourceBundle;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/** A property editor for Rectangle class.
* @author   Petr Hamernik
*/
public class RectangleEditor extends ArrayOfIntSupport {

    // the bundle to use
    static ResourceBundle bundle = NbBundle.getBundle (
                                       RectangleEditor.class);

    public RectangleEditor() {
        super("java.awt.Rectangle", 4); // NOI18N
    }

    /** Abstract method for translating the value from getValue() method to array of int. */
    int[] getValues() {
        Rectangle rect = (Rectangle) getValue();
        return new int[] { rect.x, rect.y, rect.width, rect.height };
    }

    /** Abstract method for translating the array of int to value
    * which is set to method setValue(XXX)
    */
    void setValues(int[] val) {
        if ((val[0] < 0) || (val[1] < 0) || (val[2] < 0) || (val[3] < 0)) {
            //TopManager.getDefault().notify(...) cannot be called synchronous, because when error dialog is displayed
            //PropertyEditor lost focus and setValues() method is called. After closing error dialog is focus returned
            //to PropertyEditor and setValues() method is called again.
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                                                       public void run() {
                                                           TopManager.getDefault().notify(new NotifyDescriptor.Message(bundle.getString("CTL_NegativeSize"), NotifyDescriptor.ERROR_MESSAGE));
                                                       }
                                                   });
        }
        else
            setValue(new Rectangle(val[0], val[1], val[2], val[3]));
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public java.awt.Component getCustomEditor () {
        return new RectangleCustomEditor (this);
    }

    /** @return the format of value set in property editor. */
    String getHintFormat() {
        return bundle.getString ("CTL_HintFormatRE");
    }

    /** Provides name of XML tag to use for XML persistence of the property value */
    protected String getXMLValueTag () {
        return "Rectangle"; // NOI18N
    }

}
