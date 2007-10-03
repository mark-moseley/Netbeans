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
/*
 * ValidationError.java
 *
 * Created on March 3, 2004, 2:15 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

/**
 *
 * @author Peter Williams
 */
public final class ValidationError implements Comparable {
	
	private final Partition partition;
	private final String fieldId;
	private final String message;
	
	/** Creates a new instance of ValidationError */
	private ValidationError(String fieldId, String message) {
		this(PARTITION_GLOBAL, fieldId, message);
	}
	
	/** Creates a new instance of ValidationError */
	private ValidationError(Partition partition, String fieldId, String message) {
		this.partition = partition;
		this.fieldId = fieldId;
		this.message = message;
	}
	
	/** Returns the partition.  This features is to allow us to partition error
	 *  messages by subpanel of a customizer and only display error messages
	 *  associated with the current panel.  It could have other uses as well.
	 *
	 *  @return the partition.
	 */
	public Partition getPartition() {
		return partition;
	}
	
	/** Returns the field Id, which is the absolute xpath describing this fieldId.
	 *
	 *  @return the field Id.
	 */
	public String getFieldId() {
		return fieldId;
	}
	
	/** Returns the validation message describing the error that this field
	 *  contains.
	 *
	 *  @return the validation error message.
	 */
	public String getMessage() {
		return message;
	}

	/** Two ValidationError's are equal if they hvae the same partition and
	 *  fieldId.
	 *
	 *  @param obj ValidationError to compare equality with.
	 *  @return true if equal, false otherwise.
	 */
	public boolean equals(Object obj) {
		boolean result = false;
		
		if(this == obj) {
			result = true;
		} else if(obj instanceof ValidationError) {
			ValidationError target = (ValidationError) obj;
			result = partition.equals(target.partition) && fieldId.equals(target.fieldId);
		}
		
		return result;
	}
	
	private volatile int hashCode = 0;
	
	/** Hashcode for a ValidationError object.  Overridden for consistency with
	 *  equals.
	 *
	 *  @return integer hashcode
	 */
	public int hashCode() {
		if(hashCode == 0) {
			int result = fieldId.hashCode();
			if(partition != null) {
				result = 37*result + partition.hashCode();
			}
			hashCode = result;
		}
		return hashCode;
	}
	
	/** Compare this instance of ValidationError with the target instance.
	 *  We index by partition first, then fieldId.  Partition ordering doesn't
	 *  really matter as long as members of a partition are grouped.
	 *
	 * @param Instance of ValidationError to compare with.
	 */
	public int compareTo(Object obj) {
		int result;
		
		if(this == obj) {
			result = 0;
		} else {
			ValidationError target = (ValidationError) obj;
			result = partition.compareTo(target.partition);

			if(result == 0) {
				result = fieldId.compareTo(target.fieldId);
			}
		}

		return result;
	}
	
	/** Creates a new ValidationError Object
	 *
	 *  @param fieldId Absolute Xpath of the field this messages applies to
	 *  @param message Error message describing the error in this field.
	 */
	public static ValidationError getValidationError(String fieldId, String message) {
		return new ValidationError(fieldId, message);
	}
	
	public static ValidationError getValidationErrorMask(String fieldId) {
		return new ValidationError(fieldId, "");
	}
	
	/** Creates a new ValidationError Object
	 *
	 *  @param fieldId Absolute Xpath of the field this messages applies to
	 *  @param panelId1 Customizer panel ID this field is displayed
	 *  @param message Error message describing the error in this field.
	 */
	public static ValidationError getValidationError(Partition partition, String fieldId, String message) {
		return new ValidationError(partition, fieldId, message);
	}
	
	public static ValidationError getValidationErrorMask(Partition partition, String fieldId) {
		return new ValidationError(partition, fieldId, "");
	}
	
	/** -----------------------------------------------------------------------
	 *  Partitions defined for customizer ui.
	 */
	
	// Global partition
	public static final Partition PARTITION_GLOBAL = 
		new Partition("Global");
	
	// Partitions for sun-web-app
	public static final Partition PARTITION_WEB_GENERAL = 
		new Partition("WebGeneral", 0);	// NOI18N
	public static final Partition PARTITION_WEB_CLASSLOADER = 
		new Partition("WebClassLoader", 1);	// NOI18N
	public static final Partition PARTITION_WEB_PROPERTIES = 
		new Partition("WebProperties", 2);	// NOI18N
	public static final Partition PARTITION_SESSION_MANAGER = 
		new Partition("SessionManager", 3, 0);	// NOI18N
	public static final Partition PARTITION_SESSION_STORE = 
		new Partition("SessionStore", 3, 1);	// NOI18N
	public static final Partition PARTITION_SESSION_SESSION = 
		new Partition("SessionSession", 3, 2);	// NOI18N
	public static final Partition PARTITION_SESSION_COOKIE = 
		new Partition("SessionCookie", 3, 3);	// NOI18N
	public static final Partition PARTITION_WEB_MESSAGES = 
		new Partition("WebMessages", 4);	// NOI18N
	public static final Partition PARTITION_WEB_LOCALE = 
		new Partition("WebLocale", 5);	// NOI18N
	public static final Partition PARTITION_CACHE_GENERAL = 
		new Partition("CacheGeneral", 6, 0);	// NOI18N
	public static final Partition PARTITION_CACHE_HELPERS = 
		new Partition("CacheHelpers", 6, 1);	// NOI18N
	public static final Partition PARTITION_CACHE_CONSTRAINTS = 
		new Partition("CacheConstraints", 6, 2);	// NOI18N
	
	// Partitions for SecurityRoleMapping
	public static final Partition PARTITION_SECURITY_ASSIGN = 
		new Partition("SecurityAssign", 0);	// NOI18N
	public static final Partition PARTITION_SECURITY_MASTER = 
		new Partition("SecurityMaster", 1);	// NOI18N
	
	// Partitions for ServiceRef
	public static final Partition PARTITION_SERVICEREF_GENERAL = 
		new Partition("ServiceRefGeneral", 0);	// NOI18N
	public static final Partition PARTITION_SERVICEREF_PORTINFO = 
		new Partition("ServiceRefPortInfo", 1);	// NOI18N
	
	// Partitions for ConnectorRoot
	public static final Partition PARTITION_CONNECTOR_ADAPTER = 
		new Partition("ConnectorAdapter", 0);	// NOI18N
	public static final Partition PARTITION_CONNECTOR_ROLES = 
		new Partition("ConnectorRoles", 1);	// NOI18N

	// Partitions for sun-ejb-jar
	public static final Partition PARTITION_EJBJAR_CMP_RESOURCE = 
		new Partition("EjbJarCmpResource", 0);	// NOI18N
	public static final Partition PARTITION_EJBJAR_PM_DESCRIPTORS = 
		new Partition("EjbJarPmDescriptors", 1);	// NOI18N
	public static final Partition PARTITION_EJBJAR_MESSAGES = 
		new Partition("EjbJarMessages", 2);	// NOI18N

    // Partitions for Ejb panels
	public static final Partition PARTITION_EJB_GLOBAL = 
		new Partition("EjbGlobal", 0);	// NOI18N
	public static final Partition PARTITION_EJB_IORSECURITY = 
		new Partition("EjbIorSecurity", 1);	// NOI18N
	public static final Partition PARTITION_EJB_BEANPOOL = 
		new Partition("EjbBeanPool", 2);	// NOI18N
	public static final Partition PARTITION_EJB_BEANCACHE = 
		new Partition("EjbBeanCache", 3);	// NOI18N
	public static final Partition PARTITION_EJB_CHECKPOINT = 
		new Partition("EjbCheckpoint", 4);	// NOI18N
	public static final Partition PARTITION_EJB_FINDER = 
		new Partition("EjbFinder", 5);	// NOI18N
	public static final Partition PARTITION_EJB_PREFETCH = 
		new Partition("EjbPrefetch", 6);	// NOI18N
	public static final Partition PARTITION_EJB_CMPMAPPING = 
		new Partition("EjbCmpMapping", 7);	// NOI18N
	public static final Partition PARTITION_EJB_MDBCONNFACTORY = 
		new Partition("EjbMdbConnFactory", 8);	// NOI18N
	public static final Partition PARTITION_EJB_MDBACTIVATION = 
		new Partition("EjbMdbActivation", 9);	// NOI18N
    
	public static final class Partition implements Comparable {
		private final String partitionName;
		private int tabIndex;
		private int subTabIndex;

		private Partition(final String name) {
			this(name, -1, -1);
		}
		
		private Partition(final String name, final int index) {
			this(name, index, -1);
		}
		
		private Partition(final String name, final int index, final int subIndex) {
			partitionName = name;
			tabIndex = index;
			subTabIndex = subIndex;
		}

		public String toString() {
			return partitionName;
		}
		
		public int getTabIndex() {
			return tabIndex;
		}
		
		public int getSubTabIndex() {
			return subTabIndex;
		}
		
		public int compareTo(Object obj) {
			Partition target = (Partition) obj;
			return partitionName.compareTo(target.partitionName);
		}
	}
}

