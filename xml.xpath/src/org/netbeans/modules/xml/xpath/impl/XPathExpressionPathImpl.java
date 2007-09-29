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

/*
 * Created on Sep 12, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.xpath.impl;

import org.netbeans.modules.xml.xpath.LocationStep;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.visitor.XPathVisitor;
import org.netbeans.modules.xml.xpath.visitor.impl.ExpressionWriter;


/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class XPathExpressionPathImpl extends XPathExpressionImpl 
	implements XPathExpressionPath {
	
	/** The steps. */
    private LocationStep[] mSteps;
    
    
    private XPathExpression mRootExpression;
    
    /** The absolute flag; defaults to false. */
    private boolean mAbsolute;

    /** Flag to figure out if it is a simple path 
     * Recognized paths formatted as foo/bar[3]/baz[@name = 'biz'] .
     */
    private boolean mIsSimplePath;
    
    
    /**
     * Constructor.
     * @param steps the steps
     * @param isSimplePath flag whether path is simple
     */
    public XPathExpressionPathImpl(XPathExpression rootExpression,
    							   LocationStep[] steps, 
    							   boolean isSimplePath) {
        super();
        setSteps(steps);
        setRootExpression(rootExpression);
        setSimplePath(isSimplePath);
    }

    /**
     * Gets the steps of the location path.
     * @return the steps
     */
    public LocationStep[] getSteps() {
        return mSteps;
    }
    
    
    /**
     * Sets the steps of the location path.
     * @param steps the steps
     */
    public void setSteps(LocationStep[] steps) {
        mSteps = steps;
    }
    
    /**
     * set root expression of this expression path.
     * @param rootExpression root expression of this expression path.
     */
    public void setRootExpression(XPathExpression rootExpression) {
    	this.mRootExpression = rootExpression;
    }
    
    /**
     * get root expression of this expression path.
     * @return root expression of this expression path
     * @return root expression 
     */
    public XPathExpression getRootExpression() {
    	return this.mRootExpression;
    }
    
    /**
     * Describe <code>isSimplePath</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isSimplePath() {
        return mIsSimplePath;
    }

    /**
     * Describe <code>setSimplePath</code> method here.
     *
     * @param isSimplePath a <code>boolean</code> value
     */
    public void setSimplePath(boolean isSimplePath) {
        mIsSimplePath = isSimplePath;
    }

    /**
     * get expression exclusing root expression
     * @return
     */
    public String getExpressionStringExcludingRootExpression() {
    	XPathVisitor visitor = new ExpressionWriter();
        LocationStep[] steps = getSteps();
        if(steps != null) {
        	for(int i = 0; i < steps.length; i++) {
        		visitor.visit(steps[i]);
        	}
        }
        
        return ((ExpressionWriter) visitor).getString();
    }
    
    /**
     * Calls the visitor.
     * @param visitor the visitor
     */
    public void accept(XPathVisitor visitor) {
         visitor.visit(this);
        
    }
}
