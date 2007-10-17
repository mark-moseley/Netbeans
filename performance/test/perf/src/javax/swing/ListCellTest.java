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

package javax.swing;

import org.netbeans.performance.Benchmark;
import java.awt.*;
import javax.swing.border.*;

/**
 * The Benchmark measuring the difference between creating new JLabel
 * (a typical CellRenderer component) and setting up existing JLabel
 * for usage as a cell renderer in a JList.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class ListCellTest extends Benchmark {

    public ListCellTest(String name) {
        super( name );
    }

    private static JList list = new JList();


    /**
     * Create <i>count</i> new JLabels and give them a name;
     */
    public void testCreateNew() throws Exception {
        int count = getIterationCount();
	doIt( count, new CreatingListCellRenderer() );
    }

    public void testSetupShared() throws Exception {
        int count = getIterationCount();
	doIt( count, new SharingListCellRenderer() );
    }

    private void doIt(int count, ListCellRenderer source) {
    	Component[] arr = new Component[count]; 
        while( --count >= 0 ) {
	    arr[count] = source.getListCellRendererComponent( list,
		"Hello" + count, count, count < 1, count < 1); 
        }
    }


    public static void main( String[] args ) {
	simpleRun( ListCellTest.class );
    }
    
    private static class SharingListCellRenderer implements ListCellRenderer {
	private static JLabel stamp;
	private static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
	    
	    if( stamp == null ) stamp = new JLabel();
	    
	    if( isSelected ) {
		stamp.setBackground(list.getSelectionBackground());
		stamp.setForeground(list.getSelectionForeground());
	    } else {
		stamp.setBackground(list.getBackground());
	        stamp.setForeground(list.getForeground());
	    }
	    
	    stamp.setText(value.toString());
	    stamp.setFont(list.getFont());
	    stamp.setBorder((cellHasFocus) ? 
		    UIManager.getBorder("List.focusCellHighlightBorder") : 
		    noFocusBorder );
		    
	    return stamp;
	}
    }
    
    private static class CreatingListCellRenderer implements ListCellRenderer {
	private static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
	    
	    JLabel stamp = new JLabel();
	    
	    if( isSelected ) {
		stamp.setBackground(list.getSelectionBackground());
		stamp.setForeground(list.getSelectionForeground());
	    } else {
		stamp.setBackground(list.getBackground());
	        stamp.setForeground(list.getForeground());
	    }
	    
	    stamp.setText(value.toString());
	    stamp.setFont(list.getFont());
	    stamp.setBorder((cellHasFocus) ? 
		    UIManager.getBorder("List.focusCellHighlightBorder") : 
		    noFocusBorder );
		    
	    return stamp;
	}
    }
}
