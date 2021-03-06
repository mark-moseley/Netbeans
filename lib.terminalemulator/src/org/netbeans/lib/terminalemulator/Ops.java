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
 *
 * Contributor(s): Ivan Soleimanipour.
 */

/*
 * "Ops.java"
 * Ops.java 1.14 01/07/23
 * The abstract operations the terminal can perform.
 */

package org.netbeans.lib.terminalemulator;

/**
 * The mnemonics for the ops are _roughly_ based on termcap entries.
 */

public
interface Ops {
    public void op_pause();
    public void op_char(char c);
    public void op_carriage_return();
    public void op_line_feed();
    public void op_back_space();
    public void op_tab();
    public void op_bel();

    public void op_soft_reset();
    public void op_full_reset();

    public void op_al(int count); // add new blank line
    public void op_bc(int count); // back cursor/column
    public void op_cm(int row, int col); // cursor motion
    public void op_cl(); // clear screen and home cursor
    public void op_ce(); // clear to end of line
    public void op_cd(); // clear to end of screen
    public void op_dc(int count);	// delete character
    public void op_dl(int count); // delete line & scroll everything under it up
    public void op_do(int count); // down 1 line
    public void op_ho(); // cursor home (upper left of the screen)
    public void op_ic(int count); // insert character
    public void op_nd(int count); // cursor right (non-destructive space)
    public void op_up(int count); // cursor up
    public void op_sc();	// save cursor position
    public void op_rc();	// restore saved cursor position
    public void op_margin(int from, int to);	// set vertical scroll margins

    public void op_attr(int mode);	// set ANSI attributes
    public void op_set_mode(int mode);
    public void op_reset_mode(int mode);

    // These cause data to be sent back:
    public void op_status_report(int code);


    // ops mimicing certain DtTerm features
    public void op_glyph(int glyph, int rendition);	// assign glyph
							// to current row
    public void op_reverse(boolean reverse);
    public void op_cursor_visible(boolean cursor);


    // querying operations
    public int op_get_width();
    public int op_get_column();	// ... cursor is currently located on (0-origin)
					    

    // ops unique to Term
    public void op_time(boolean refresh);	// dump time into output &
						// control refreshEnabled prop
}
