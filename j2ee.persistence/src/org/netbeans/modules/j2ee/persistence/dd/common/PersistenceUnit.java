/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.persistence.dd.common;

/**
 *
 * @author sp153251
 */
public interface PersistenceUnit {
	static public final String NAME = "Name";	// NOI18N
	static public final String TRANSACTIONTYPE = "TransactionType";	// NOI18N
	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String PROVIDER = "Provider";	// NOI18N
	static public final String JTA_DATA_SOURCE = "JtaDataSource";	// NOI18N
	static public final String NON_JTA_DATA_SOURCE = "NonJtaDataSource";	// NOI18N
	static public final String MAPPING_FILE = "MappingFile";	// NOI18N
	static public final String JAR_FILE = "JarFile";	// NOI18N
	static public final String CLASS2 = "Class2";	// NOI18N
	static public final String EXCLUDE_UNLISTED_CLASSES = "ExcludeUnlistedClasses";	// NOI18N
	static public final String PROPERTIES = "Properties";	// NOI18N

        public void setName(java.lang.String value);
        public java.lang.String getName();
        public void setTransactionType(java.lang.String value);
        public java.lang.String getTransactionType();
        public void setDescription(java.lang.String value);
        public java.lang.String getDescription();
        public void setProvider(java.lang.String value);
        public java.lang.String getProvider();
        public void setJtaDataSource(java.lang.String value);
        public java.lang.String getJtaDataSource();
        public void setNonJtaDataSource(java.lang.String value);
        public java.lang.String getNonJtaDataSource();
        public void setMappingFile(int index, java.lang.String value);
        public java.lang.String getMappingFile(int index);
        public int sizeMappingFile();
        public void setMappingFile(java.lang.String[] value);
        public java.lang.String[] getMappingFile();
        public int addMappingFile(java.lang.String value);
        public int removeMappingFile(java.lang.String value);
        public void setJarFile(int index, java.lang.String value);
        public java.lang.String getJarFile(int index);
        public int sizeJarFile();
        public void setJarFile(java.lang.String[] value);
        public java.lang.String[] getJarFile();
        public int addJarFile(java.lang.String value);

        public int removeJarFile(java.lang.String value);

        public void setClass2(int index, java.lang.String value);
        public java.lang.String getClass2(int index);
        public int sizeClass2();
        public void setClass2(java.lang.String[] value);
        public java.lang.String[] getClass2();
        public int addClass2(java.lang.String value);
        public int removeClass2(java.lang.String value);

        public void setExcludeUnlistedClasses(boolean value);
        public boolean isExcludeUnlistedClasses();

        public void setProperties(Properties valueInterface);
        public Properties getProperties();
        public Properties newProperties();
}
