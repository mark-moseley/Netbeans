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

/************************************************************************

        FILENAME: Entry.java

        AUTHOR: Erica Grevemeyer      DATE: Jan  7 11:21:09 PST 2002

************************************************************************/
package org.netbeans.nbbuild.utils.cvsutils;

import java.util.*;

/**
 *	This CVS utility class will parse a single line in a
 *	CVS/Entries file.
 *
 *   @author Erica Grevemeyer
 *   @version  1.1 Jan  7 11:21:09 PST 2002
 */

public class Entry {
	private static final String DEFAULT_SEPARATOR = "/";
	private String filename, filetype, revno, timestamp;

	// Constructor Methods
	/** Create an Entry instance when all information is already available.
	* @param ft - File type. (Value will be either "F" or "D" indicating file or directory).
	* @param fn - filename listed in the entry
	* @param revno - latest revision number listedx.
	* @param ts - the time stamp string. (ex. Sat Dec 22 00:31:24 2001)
    * This may have additional information if the file is not up to date.
	*/
	public Entry(String ft, String fn, String revno, String ts) {
		setFiletype(ft);
		setFilename(fn);
		setRevno(revno);
		setTimestamp(ts);
	}

	/**
	* @param line - line from the read in file. 
	* @param separator - separator used in this file.  CVS generated files will use &quot;/&quot; 
	*/
	public Entry(String line, String separator) {
	/*
		We use the separator here instead of always 
		defaulting to CVS's "/" so that we can use 
		this class to parse files which are 
		organized the same way, but don't use the '/'.

	*/ 
		StringTokenizer st = new StringTokenizer(line, separator);

		int index = 0;
		String[] item=new String[6];

		while (st.hasMoreTokens()) {
			String ftstr = ""; 
			String tok = st.nextToken();
			
			//  Typical CVS/Entries file lines:
			//   D/dirname////
			//  /filename/1.3/Sat Dec 22 00:31:24 2001//

			if (index == 0 ) {
				if ( ! tok.equals("D") ) {
					item[index++]="F";
				}
			}
			item[index++]=tok;
			//item[0] = filetype
			//item[1] = filename
			//item[2] = revno
			//item[3] = timestamp
			//item[4] = other
			//item[5] = other1

		} //while more tokens
		this.setFiletype(item[0]);
		this.setFilename(item[1]);
		this.setRevno(item[2]);
		this.setTimestamp(item[3]);
		
	}
        
	public Entry(String line) {
		this(line, DEFAULT_SEPARATOR);
	}
	
	public boolean hasName(String queryFilename) {	
		return this.getFilename().trim().equals(queryFilename.trim());
	}	

	//Display Methods
	public String toString() {
		String fn, ft, revno, ts;

		fn=this.getFilename();
		ft=this.getFiletype();
		revno=this.getRevno();
		ts=this.getTimestamp();

		String fullEntry="FileName:\t"
			+fn+"\nFileType:\t"+ft +"\nRevno:\t"+revno
			+"\nTimeStamp:\t" +ts+"\n";
		return fullEntry;	
	}

	// Accessor Methods
	public void setFilename(String str) {
		this.filename=str;
	}	

	public String getFilename() {
		return filename;
	}	

	public void setFiletype(String str) {
		this.filetype=str;
	}	

	public String getFiletype() {
		return filetype;
	}	

	public void setRevno(String str) {
		this.revno=str;
	}	

	public String getRevno() {
		return revno;
	}	

	public void setTimestamp(String str) {
		this.timestamp=str;
	}	

	public String getTimestamp() {
		return timestamp;
	}	
	
}
