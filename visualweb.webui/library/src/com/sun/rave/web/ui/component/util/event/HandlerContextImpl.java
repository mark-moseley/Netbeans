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
package com.sun.rave.web.ui.component.util.event;

import com.sun.rave.web.ui.component.util.descriptors.LayoutElement;

import java.util.EventObject;

import javax.faces.context.FacesContext;


/**
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class HandlerContextImpl implements HandlerContext {

    /**
     *	Constructor
     */
    public HandlerContextImpl(FacesContext context, LayoutElement layoutDesc, EventObject event, String eventType) {
	_facesContext = context;
	_layoutDesc = layoutDesc;
	_event = event;
	_eventType = eventType;
    }

    /**
     *	<P> Constructor that gets all its values from the given
     *	    HandlerContext.</P>
     *
     *	@param	context	The HandlerContext to clone.
     */
    public HandlerContextImpl(HandlerContext context) {
	_facesContext = context.getFacesContext();
	_layoutDesc = context.getLayoutElement();
	_event = context.getEventObject();
	_eventType = context.getEventType();
	_handler = context.getHandler();
    }

    /**
     *	<P> Accessor for the FacesContext.</P>
     *
     *	@return FacesContext
     */
    public FacesContext getFacesContext() {
	return _facesContext;
    }

    /**
     *	<P> Accessor for the LayoutElement associated with this Handler.</P>
     */
    public LayoutElement getLayoutElement() {
	return _layoutDesc;
    }

    /**
     *	<P> Accessor for the EventObject associated with this Handler.  This
     *	    may be null if an EventObject was not created for this handler.
     *	    An EventObject, if it does exist, may provide additional details
     *	    describing the context in which this Event is invoked.</P>
     */
    public EventObject getEventObject() {
	return _event;
    }

    /**
     *	<P> This method provides access to the EventType.  This is mostly
     *	    helpful for diagnostics, but may be used in a handler to determine
     *	    more information about the context in which the code is
     *	    executing.</P>
     */
    public String getEventType() {
	return _eventType;
    }

    /**
     *	<P> Accessor for the Handler descriptor for this Handler.  The Handler
     *	    descriptor object contains specific meta information describing
     *	    the invocation of this handler.  This includes details such as
     *	    input values, and where output values are to be set.</P>
     */
    public Handler getHandler() {
	return _handler;
    }

    /**
     *	<P> Setter for the Handler descriptor for this Handler.</P>
     */
    public void setHandler(Handler handler) {
	_handler = handler;
    }

    /**
     *	<P> Accessor for the Handler descriptor for this Handler.  The
     *	    HandlerDefinition descriptor contains meta information about the
     *	    actual Java handler that will handle the processing.  This
     *	    includes the inputs required, outputs produces, and the types for
     *	    both.</P>
     */
    public HandlerDefinition getHandlerDefinition() {
	return _handler.getHandlerDefinition();
    }

    /**
     *	<P> This method returns the value for the named input.  Input values
     *	    are not stored in this HandlerContext itself, but in the Handler.
     *	    If you are trying to set input values for a handler, you must
     *	    create a new Handler object and set its input values.</P>
     *
     *	<P> This method attempts to resolve $...{...} expressions.  It also
     *	    will return the default value if the value is null.  If you don't
     *	    want these things to happen, look at
     *	    Handler.getInputValue(String).</P>
     *
     *	@param	name	    The input name
     *
     *	@return	The value of the input (null if not found)
     */
    public Object getInputValue(String name) {
	return getHandler().getInputValue(this, name);
    }

    /**
     *	<P> This method retrieves an Output value. Output values must not be
     *	    stored in this Context itself (remember HandlerContext objects
     *	    are shared).  Output values are stored according to what is
     *	    specified in the HandlerDefintion.</P>
     *
     *	@param	name	    The output name
     *
     *	@return	The value of the output (null if not found)
     */
    public Object getOutputValue(String name) {
	return getHandler().getOutputValue(this, name);
    }

    /**
     *	<P> This method sets an Output value. Output values must not be
     *	    stored in this Context itself (remember HandlerContext objects
     *	    are shared).  Output values are stored according to what is
     *	    specified in the HandlerDefintion.</P>
     */
    public void setOutputValue(String name, Object value) {
	getHandler().setOutputValue(this, name, value);
    }

    private String		_eventType	= null;
    private FacesContext	_facesContext   = null;
    private LayoutElement	_layoutDesc	= null;
    private EventObject		_event		= null;
    private Handler		_handler	= null;
}
