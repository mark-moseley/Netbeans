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

package demo;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * Demo MIDlet creates, runs and displays DemoGameCanvas.
 * 
 * @author  Karel Herink
 * @version 1.0
 */
public class GameMidlet extends MIDlet {
	
	private DemoGameCanvas gameCanvas;
	private Thread t;
	private Display d;

	public void startApp() {
		this.gameCanvas = new DemoGameCanvas();
		this.t = new Thread(gameCanvas);
		t.start();
		d = Display.getDisplay(this);
		d.setCurrent(gameCanvas);
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
		this.gameCanvas.stop();
    }
}
