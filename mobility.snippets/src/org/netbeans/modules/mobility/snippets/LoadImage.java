/*

 * LoadResource.java

 *

 * Created on October 20, 2006, 3:58 PM

 *

 * To change this template, choose Tools | Template Manager

 * and open the template in the editor.

 */



package org.netbeans.modules.mobility.snippets;



import javax.swing.text.BadLocationException;

import javax.swing.text.JTextComponent;

import org.openide.text.ActiveEditorDrop;



/**

 *

 * @author bohemius

 */

public class LoadImage implements ActiveEditorDrop {

    

    /** Creates a new instance of LoadResource */

    public LoadImage() {

    }



    public boolean handleTransfer(JTextComponent targetComponent) {

        StringBuffer body=new StringBuffer("");

        

        body.append("public javax.microedition.lcdui.Image loadImage(String location) {\n");

        body.append("try {\n");

        body.append("return javax.microedition.lcdui.Image.createImage (location);\n");

        body.append("}\n");

        body.append("catch (java.io.IOException e) {\n");

        body.append("throw new RuntimeException(\"Unable to load Image: \"+e);\n");

        body.append("}\n");

        body.append("}\n\n");

              

        try {

            SnippetsPaletteUtilities.insert(body.toString(), targetComponent);

        } catch (BadLocationException ble) {

            return false;

        }

        return true;

    }   

}

