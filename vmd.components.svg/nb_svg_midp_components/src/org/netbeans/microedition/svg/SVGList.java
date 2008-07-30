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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.microedition.svg;

import java.util.Enumeration;
import java.util.Vector;

import org.netbeans.microedition.svg.input.InputHandler;
import org.netbeans.microedition.svg.meta.MetaData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;


/**
 * Suggested svg snippet:
 * <pre>
 *  &lt;g id="list" transform="translate(20,220)" >
 *       &lt;g>
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=selection&lt;/text>
 *       
 *       &lt;rect  x="5" y="0" stroke="black" stroke-width="1" fill="rgb(200,200,255)" visibility="inherit" width="80" height="0"/>
 *       &lt;g>
 *         &lt;text id="list_hidden_text" visibility="hidden" x="10" y="13" stroke="black" font-size="15" font-family="SunSansSemiBold">
 *          HIDDEN TEXT&lt;/text>
 *         &lt;text display="none">type=hidden_text&lt;/text>
 *       &lt;/g>
 *   &lt;g id="list_content" > 
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=content&lt;/text> 
 *   &lt;/g>
 *       &lt;rect x="0" y="-5" rx="5" ry="5" width="90" height="70" fill="none" stroke="rgb(255,165,0)" stroke-width="2" visibility="hidden">
 *           &lt;set attributeName="visibility" attributeType="XML" begin="list.focusin" fill="freeze" to="visible"/>
 *           &lt;set attributeName="visibility" attributeType="XML" begin="list.focusout" fill="freeze" to="hidden"/>
 *       &lt;/rect>
 *   &lt;g>
 *           &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=bound&lt;/text>
 *
 *       &lt;rect id="list_bound" x="5.0" y="0.0" width="80" height="60" fill="none" stroke="black" stroke-width="2"/>
 *   &lt;/g>
 *   &lt;/g>
 * </pre>
 * 
 * Nested elements 'content' and 'bound' are necessary.
 * All other is used by default renderer. 
 * See {@link SVGDefaultListCellRenderer.}  
 * @author ads
 *
 */
public class SVGList extends SVGComponent implements DataListener {
    
    private static final String CONTENT             = "content";          // NOI18N
    private static final String SELECTION           = "selection";        // NOI18N
    private static final String HIDDEN_TEXT         = "hidden_text";      // NOI18N
    private static final String BOUNDS              = "bound";            // NOI18N
    
    private static final String CONTENT_SUFFIX      = DASH + CONTENT;
    private static final String SELECTION_SUFFIX    = DASH + SELECTION;
    private static final String BOUNDS_SUFIX        = DASH + BOUNDS;
    private static final String HIDDEN_TEXT_SUFFIX  = DASH +HIDDEN_TEXT;


    public SVGList( SVGForm form, SVGLocatableElement element) {
        super(form, element);
        
        SVGLocatableElement bounds = initNestedElements();
        
        verify( bounds );
        
        float height = myHiddenText.getFloatTrait( SVGTextField.TRAIT_FONT_SIZE );
        myCount = (int)(bounds.getBBox().getHeight()/height);
        
        //setTraitSafely( hiddenText, TRAIT_VISIBILITY, TR_VALUE_HIDDEN);
        
        isSlave = TR_VALUE_HIDDEN.equals(getElement().getTrait( TRAIT_VISIBILITY ));
        
        myRenderer = new SVGDefaultListCellRenderer( height );
        setSelectionModel(  new DefaultSelectionModel() );
        myHandler = new ListHandler();
    }

    public SVGList( SVGForm form, String elemId ){
        this( form , (SVGLocatableElement)
                form.getDocument().getElementById( elemId ));
    }


    public SVGListCellRenderer getRenderer(){
        return myRenderer;
    }
    
    public ListModel getModel(){
        return myModel;
    }
    
    public void setModel( ListModel model ){
        if ( myModel != null ){
            myModel.removeDataListener( this );
        }
        myModel = model;
        myModel.addDataListener( this );
        renderList();
        
    }
    
    public void setRenderer( SVGListCellRenderer renderer ){
        myRenderer = renderer;
    }
    
    public SelectionModel getSelectionModel(){
        return mySelectionModel;
    }
    
    public void setSelectionModel( SelectionModel model ){
        if ( mySelectionModel != null ){
            mySelectionModel.removeDataListener( this );
        }
        mySelectionModel = model;
        mySelectionModel.addDataListener( this );
    }
    
    public InputHandler getInputHandler(){
        return myHandler;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.microedition.svg.DataListener#contentsChanged(java.lang.Object)
     */
    public void contentsChanged( Object source ) {
        if ( source == getSelectionModel() ){
            synchronized (myUILock) {
                if ( !isUIAction ){
                    renderList();
                }
            }
        }
    }
    
    SVGLocatableElement getHiddenText(){
        return myHiddenText;
    }
    
    SVGLocatableElement getContent(){
        return myContent;
    }
    
    SVGLocatableElement getSelection(){
        return mySelection;
    }
    
    boolean isSlave(){
        return isSlave;
    }
    
    private SVGLocatableElement  initNestedElements() {
        SVGLocatableElement bounds = null;
        if (getElement().getId() != null) {
            myHiddenText = (SVGLocatableElement) getElementById(getElement(),
                    getElement().getId() + HIDDEN_TEXT_SUFFIX);
            bounds = (SVGLocatableElement) getElementById(getElement(),
                    getElement().getId() + BOUNDS_SUFIX);
            myContent = (SVGLocatableElement)getElementById( getElement(), 
                    getElement().getId() +CONTENT_SUFFIX );
            mySelection = (SVGLocatableElement)getElementById( getElement(), 
                    getElement().getId() +SELECTION_SUFFIX );
        }
        if (myHiddenText == null) {
            myHiddenText = (SVGLocatableElement) getNestedElementByMeta(getElement(),
                    TYPE, HIDDEN_TEXT);
        }

        if (bounds == null) {
            bounds = (SVGLocatableElement) getElementByMeta(getElement(), TYPE,
                    BOUNDS);
        }
        
        if ( myContent == null ){
            myContent = (SVGLocatableElement)getElementByMeta(
                getElement(), SVGList.TYPE,  SVGList.CONTENT );
        }
        
        if ( mySelection == null ){
            mySelection = (SVGLocatableElement)getNestedElementByMeta(getElement(), 
                    SVGList.TYPE,SELECTION);
        }
        
        return bounds;
    }
    
    private void verify( SVGLocatableElement bounds ) {
        if ( bounds ==null ){
            throw new IllegalArgumentException("Element with id="+
                    getElement().getId()+" couldn't be List element" +
                    		" becuase it doesn't contain nested 'bound' " +
                    		"element. See javadoc for SVG snippet.");
        }
        if ( myContent == null ){
            throw new IllegalArgumentException("Element with id="+
                    getElement().getId()+" couldn't be List element" +
                            " becuase it doesn't contain nested 'content' " +
                            " element. See javadoc for SVG snippet.");
        }
    }
    
    private void renderList() {
        if (myCurrentIndex >= myTopIndex + myCount) {
            myTopIndex++;
        }
        else if (myCurrentIndex < myTopIndex) {
            myTopIndex--;
        }
        
        final int top = myTopIndex;
        
        getForm().invokeLaterSafely(new Runnable() {

            public void run() {
                removeContent();
                hideSelection();
                
                ListModel model = getModel();
                int size = model.getSize();
                SVGListCellRenderer renderer = getRenderer();
                for (int i = top; i < Math.min(top + myCount, size); i++) {
                    renderer.getCellRendererComponent(SVGList.this, model
                            .getElementAt(i), i - top, getSelectionModel()
                            .isSelectedIndex(i));
                }
            }
        });
    }

    private void hideSelection() {
        if ( !isSlave ){
            getSelection().setTrait( TRAIT_VISIBILITY, TR_VALUE_HIDDEN);
        }
    }


    private void removeContent() {
        final SVGLocatableElement content = (SVGLocatableElement) getElementByMeta(
                getElement(), TYPE, SVGList.CONTENT );//, true );
        Node node = content.getFirstElementChild();
        while (node != null) {
            Element next = null;
            if (node instanceof SVGElement) {
                next = ((SVGElement) node).getNextElementSibling();
            }
            if (!MetaData.METADATA.equals(node.getLocalName())) {
                content.removeChild(node);
            }
            else if (node instanceof SVGElement) {
                String display = ((SVGElement) node).getTrait(MetaData.DISPLAY);
                if (!MetaData.NONE.equals(display)) {
                    final Node forRemove = node;
                    content.removeChild(forRemove);
                }
            }
            node = next;
        }
    }

    public interface ListModel {
        Object getElementAt( int index );
        int getSize();
        void addDataListener( DataListener listener );
        void removeDataListener( DataListener listener );
    }
    
    public interface SelectionModel {
        void clearSelection();
        boolean isSelectedIndex( int index );
        void addSelectionInterval( int from , int to);
        void addDataListener( DataListener listener );
        void removeDataListener( DataListener listener );
    }
    
    public static class DefaultListMoldel implements ListModel {
        
        public DefaultListMoldel( Vector data ){
            myListeners = new Vector(1);
            myData = new Vector();
            for ( int i=0; i<data.size() ; i++ ){
                myData.addElement( data.elementAt( i ));
            }
        }
        
        public Object getElementAt( int indx ) {
            return myData.elementAt(indx);
        }

        public int getSize() {
            return myData.size();
        }
        
        public  void addDataListener( DataListener listener ) {
            synchronized ( myListeners ) {
                myListeners.addElement( listener );
            }
        }

        public synchronized void removeDataListener( DataListener listener ) {
            synchronized ( myListeners ) {
                myListeners.removeElement( listener );
            }            
        }
        
        protected void fireDataChanged(){
            synchronized ( myListeners ) {
                Enumeration en = myListeners.elements();
                while ( en.hasMoreElements() ){
                    DataListener listener = (DataListener)en.nextElement();
                    listener.contentsChanged( this );
                }
            }
        }
        
        private Vector myData;
        private Vector myListeners;
    }
    
    private class DefaultSelectionModel implements SelectionModel {
        
        DefaultSelectionModel() {
            myListeners = new Vector(1);
        }

        public boolean isSelectedIndex( int index ) {
            return index == mySelectedIndex;
        }
        
        public void addSelectionInterval( int from, int to ) {
            if ( to != from ){
                throw new IllegalArgumentException( 
                        DefaultSelectionModel.class.getName() +" is not designed" +
                        		" for multiple selection");
            }
            mySelectedIndex = from;
            fireDataChanged();
        }
        
        public void clearSelection() {
            mySelectedIndex = -1;
            fireDataChanged();
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.microedition.svg.SVGList.SelectionModel#addDataListener(org.netbeans.microedition.svg.DataListener)
         */
        public void addDataListener( DataListener listener ) {
            synchronized (myListeners) {
                myListeners.addElement( listener );
            }
        }

        /* (non-Javadoc)
         * @see org.netbeans.microedition.svg.SVGList.SelectionModel#removeDataListener(org.netbeans.microedition.svg.DataListener)
         */
        public void removeDataListener( DataListener listener ) {
            synchronized (myListeners) {
                myListeners.removeElement( listener );
            }
        }
        
        protected void fireDataChanged(){
            synchronized ( myListeners ) {
                Enumeration en = myListeners.elements();
                while ( en.hasMoreElements() ){
                    DataListener listener = (DataListener)en.nextElement();
                    listener.contentsChanged( this );
                }
            }
        }
        
        private int mySelectedIndex;
        private Vector myListeners;

    }
    
    private class ListHandler extends InputHandler {

        public boolean handleKeyPress( SVGComponent comp, int keyCode ) {
            return (comp instanceof SVGList) && 
                 ( keyCode == LEFT || keyCode == RIGHT || keyCode == FIRE ); 
        }

        public boolean handleKeyRelease( SVGComponent comp, int keyCode ) {
            boolean ret = false;
            if ( comp instanceof SVGList ){
                if ( keyCode == LEFT ){
                    myCurrentIndex = Math.max( 0 , myCurrentIndex -1 );
                    synchronized (myUILock) {
                        isUIAction = true;
                        getSelectionModel().clearSelection();
                        getSelectionModel().addSelectionInterval(
                                myCurrentIndex, myCurrentIndex);
                        isUIAction = false;
                    }
                    renderList();
                    ret = true;
                }
                else if ( keyCode == RIGHT ){
                    myCurrentIndex = Math.min( myCurrentIndex +1 , 
                            getModel().getSize() -1  );
                    synchronized (myUILock) {
                        isUIAction = true;
                        getSelectionModel().clearSelection();
                        getSelectionModel().addSelectionInterval(
                                myCurrentIndex, myCurrentIndex);
                        isUIAction = false;
                    }
                    renderList();
                    ret = true;
                }
                else if( keyCode == FIRE ){
                    fireActionPerformed();
                }
            }
            return ret;
        }
        
    }
    
    private SVGListCellRenderer myRenderer;
    private ListModel myModel;
    private SelectionModel mySelectionModel;
    private InputHandler myHandler;
    
    private SVGLocatableElement myHiddenText;
    private SVGLocatableElement myContent;
    private SVGLocatableElement mySelection;
    
    private int myTopIndex ;
    private int myCurrentIndex;
    
    private int myCount=-1;
    
    private boolean isUIAction;
    private Object myUILock = new Object();
    
    /*
     * This flag means that list is not standalone component but
     * used as additional component for rendering list of choices.
     * In this case "selection" should be always hidden/visible along with
     * list.
     */
    private boolean isSlave;

}
