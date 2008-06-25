/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */ 

/*
 * SVGForm.java
 * 
 * Created on Oct 2, 2007, 3:09:21 PM
 */
package org.netbeans.microedition.svg;

import java.util.Vector;

import javax.microedition.lcdui.Display;
import javax.microedition.m2g.SVGEventListener;
import javax.microedition.m2g.SVGImage;

import org.netbeans.microedition.svg.input.InputHandler;
import org.netbeans.microedition.svg.input.NumPadInputHandler;

/**
 *
 * @author Pavel Benes
 */
public class SVGForm extends SVGPlayer implements InputHandler.CaretVisibilityListener {
    private final Vector             components;
    private       SVGComponent       focusedComponent = null;
    private       NumPadInputHandler inputHandler;

    public interface FocusListener {
        void focusGained();
        void focusLost();
    }
    
    public SVGForm(SVGImage svgImage, Display display) throws IllegalArgumentException {
        super( svgImage, display);
        components = new Vector();
        //this.display = display;
        setResetAnimationWhenStopped(false); // animation cannot be reset automatically        
        setSVGEventListener(new SvgFormEventListener()); // set menu listener        
        setFullScreenMode(true); // menu is usually full screen
    }
    
    public void add(SVGComponent component ){
        components.addElement( component );
    }
         
    public SVGComponent getFocusedField() {
        return focusedComponent;
    }    
    
    synchronized void requestFocus(SVGComponent comp) {
        if ( focusedComponent != comp) {
            if ( focusedComponent != null) {
                focusedComponent.focusLost();
            }
            focusedComponent = comp;
            if ( focusedComponent != null) {
                getSVGImage().focusOn(focusedComponent.getElement());
                focusedComponent.focusGained();
            } else {
                getSVGImage().focusOn(null);
            }
        }
    }

    public void activate(SVGComponent comp) {
        requestFocus(comp);
        getSVGImage().activate();
    }
    
    public void setCaretVisible(boolean isVisible) {
        if (focusedComponent instanceof SVGTextField  ) {
            ((SVGTextField)focusedComponent).setCaretVisible(isVisible);
        }
        if ( focusedComponent instanceof SVGTextArea ){
            ((SVGTextArea)focusedComponent).setCaretVisible(isVisible);
        }
    }
     
    /*private final Hashtable groups = new Hashtable(10);
    
    private SVGButtonGroup getGroup(String id) {
        synchronized(groups) {
            SVGButtonGroup group = (SVGButtonGroup) groups.get(id);
            if (group == null) {
                group = new SVGButtonGroup(id);
                groups.put(id, group);
            }
            return group;
        }
    }
    
    public boolean registerRadioButton(SVGRadioButton button) {
        Node node = button.getElement();
        while (node != null) {
            if ( node instanceof SVGElement) {
                SVGElement svgElem = (SVGElement) node;
                String id = svgElem.getId();
                if ( id != null && id.startsWith( SVGButtonGroup.BUTTON_GROUP_PREFIX)) {
                    SVGButtonGroup group = getGroup(id);
                    group.add(button);
                    return group.size() == 1;
                }
            }
            node = node.getParentNode();
        }
        
        return true;
    }*/
    
    public synchronized NumPadInputHandler getNumPadInputHandler() {
        if ( inputHandler == null) {
            inputHandler = new NumPadInputHandler(getDisplay());
            inputHandler.addVisibilityListener(this);            
        }
        return inputHandler;
    }
    
    private class SvgFormEventListener implements SVGEventListener {
        public void keyPressed(int keyCode) {
            if ( focusedComponent != null) {
                System.out.println("Pressed: " + keyCode + " [" + (char)keyCode + "]");
                int index;
                switch( keyCode) {
                    case InputHandler.UP:
                        index = components.indexOf(focusedComponent);
                        if ( --index < 0) {
                            index = components.size() - 1;
                        }
                        requestFocus( (SVGComponent) components.elementAt(index));
                        break;
                    case InputHandler.DOWN:
                        index = components.indexOf(focusedComponent);
                        if ( ++index >= components.size()) {
                            index = 0;
                        }
                        requestFocus( (SVGComponent) components.elementAt(index));
                        break;
                    default:
                        InputHandler handler = focusedComponent.getInputHandler();
                        if ( handler != null) {
                            handler.handleKeyPress( focusedComponent, keyCode);
                        }
                        break;
                }
            }
        }
        
        public void keyReleased(int keyCode) {
            if ( focusedComponent != null) {
                InputHandler handler = focusedComponent.getInputHandler();
                if ( handler != null) {
                    handler.handleKeyRelease( focusedComponent, keyCode);
                }
            }
        }
        
        public void pointerPressed(int x, int y) {
        }
        
        public void pointerReleased(int x, int y) {
        }
        
        public void hideNotify() {
        }
        
        public void showNotify() {
        }
        
        public void sizeChanged(int width, int height) {
        }
    }

}
