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

package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IOperation extends IBehavioralFeature,
									IParameterableElement
{
	/**
	 * property IsQuery
	*/
	public boolean getIsQuery();

	/**
	 * property IsQuery
	*/
	public void setIsQuery( boolean value );

	/**
	 * method AddPostCondition
	*/
	public void addPostCondition( IConstraint cond );

	/**
	 * method RemovePostCondition
	*/
	public void removePostCondition( IConstraint cond );

	/**
	 * property PostConditions
	*/
	public ETList<IConstraint> getPostConditions();

	/**
	 * method AddPreCondition
	*/
	public void addPreCondition( IConstraint cond );

	/**
	 * method RemovePreCondition
	*/
	public void removePreCondition( IConstraint cond );

	/**
	 * property PreConditions
	*/
	public ETList<IConstraint> getPreConditions();

	/**
	 * Indicates that this Operation can potentially raise the passed in Classifier in an Exception.
	*/
	public void addRaisedException( IClassifier exc );

	/**
	 * Removes the passed in Classifier from the list of Classifiers that can be raised as an exception.
	*/
	public void removeRaisedException( IClassifier exc );

	/**
	 * Retrieves the collection of Classifiers that can be raised in Exceptions from this Operation.
	*/
	public ETList<IClassifier> getRaisedExceptions();

	/**
	 * Adds an exception by name.
	*/
	public void addRaisedException2( String classifierName );

	/**
	 * Determines whether or not this Operation is a constructor.
	*/
	public boolean getIsConstructor();

	/**
	 * Determines whether or not this Operation is a constructor.
	*/
	public void setIsConstructor( boolean value );

    /**
     * Determines whether or not this Operation is a destructor.
    */
    public boolean getIsDestructor();
	
	/**
     * Determines whether or not this Operation is a destructor.
    */
    public void setIsDestructor(boolean value);
	/**
	 * Determines whether or not this Operation represents a property.
	*/
	public boolean getIsProperty();

	/**
	 * Determines whether or not this Operation represents a property.
	*/
	public void setIsProperty( boolean value );

	/**
	 * Determines whether or not this Operation represents a friend operation to the enclosing Classifier.
	*/
	public boolean getIsFriend();

	/**
	 * Determines whether or not this Operation represents a friend operation to the enclosing Classifier.
	*/
	public void setIsFriend( boolean value );

	public boolean getIsSubroutine();
	public void setIsSubroutine( boolean value );

	public boolean getIsVirtual();
	public void setIsVirtual( boolean value );

	public boolean getIsOverride();
	public void setIsOverride( boolean value );

	public boolean getIsDelegate();
	public void setIsDelegate( boolean value );

	public boolean getIsIndexer();
	public void setIsIndexer( boolean value );
    
    public String getRaisedExceptionsAsString();
}
