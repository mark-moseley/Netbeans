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

/*
 * HelloMidlet.java
 *
 * Created on 13. duben 2005, 19:27
 */
package hello;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 *
 * @author bohemius
 */
public class HelloMidlet extends MIDlet implements javax.microedition.lcdui.CommandListener {
    
    /** Creates a new instance of HelloMidlet */
    public HelloMidlet() {
    }
    
// --- This section is auto-generated by NetBeans IDE. Do not modify or you may lose your changes.//<editor-fold id="MVDMethods" defaultstate="collapsed" desc="This section is auto-generated by NetBeans IDE.">//GEN-BEGIN:MVDMethods
    /**
     * This method initializes UI of the application.
     */
    private void initialize() {
// For adding user code into this block, select "Design" item in the inspector and invoke property editor on Action property in Properties window.
        javax.microedition.lcdui.Display.getDisplay(this).setCurrent(get_helloForm());
    }
    
    /**
     * Called by the system to indicate that a command has been invoked on a particular displayable.
     * @param command the Command that ws invoked
     * @param displayable the Displayable on which the command was invoked
     **/
    public void commandAction(javax.microedition.lcdui.Command command, javax.microedition.lcdui.Displayable displayable) {
        if (displayable == helloForm) {
            if (command == exitCommand) {
// For adding user code into this block, select "Design | Screens | helloForm [Form] | Assigned Commands | exitCommand" item in the inspector and invoke property editor on Action property in Properties window.
                javax.microedition.lcdui.Display.getDisplay(this).setCurrent(null);
                destroyApp(true);
                notifyDestroyed();
            }
        }
    }
    
    /**
     * This method returns instance for helloForm component and should be called instead of accessing helloForm field directly.
     * @return Instance for helloForm component
     **/
    private javax.microedition.lcdui.Form get_helloForm() {
        if (helloForm == null) {
            helloForm = new javax.microedition.lcdui.Form(null, new javax.microedition.lcdui.Item[] {get_helloStringItem()});
            helloForm.addCommand(get_exitCommand());
            helloForm.setCommandListener(this);
        }
        return helloForm;
    }
    
    /**
     * This method returns instance for helloStringItem component and should be called instead of accessing helloStringItem field directly.
     * @return Instance for helloStringItem component
     **/
    private javax.microedition.lcdui.StringItem get_helloStringItem() {
        if (helloStringItem == null) {
            helloStringItem = new javax.microedition.lcdui.StringItem("Hello", "Hello, World!");
        }
        return helloStringItem;
    }
    
    /**
     * This method returns instance for exitCommand component and should be called instead of accessing exitCommand field directly.
     * @return Instance for exitCommand component
     **/
    private javax.microedition.lcdui.Command get_exitCommand() {
        if (exitCommand == null) {
            exitCommand = new javax.microedition.lcdui.Command("Exit", javax.microedition.lcdui.Command.EXIT, 1);
        }
        return exitCommand;
    }
    
    javax.microedition.lcdui.Form helloForm;
    javax.microedition.lcdui.StringItem helloStringItem;
    javax.microedition.lcdui.Command exitCommand;
// --- This is the end of auto-generated section.//</editor-fold>//GEN-END:MVDMethods
    
    public void startApp() {
        initialize();
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
    
}
