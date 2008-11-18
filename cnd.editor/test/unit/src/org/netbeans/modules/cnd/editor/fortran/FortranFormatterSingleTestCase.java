/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.fortran;

import org.netbeans.modules.cnd.editor.deprecated.fortran.options.FortranCodeStyle;

/**
 *
 * @author Alexander Simon
 */
public class FortranFormatterSingleTestCase  extends FortranEditorBase {

    public FortranFormatterSingleTestCase(String testMethodName) {
        super(testMethodName);
    }
    public void testIfFree() {
        setLoadDocumentText(
                "  implicit double precision (a-h)\n" +
                "  implicit doubleprecision (o-z)\n" +
                "  do i=-1,1\n" +
                "  if (i.eq.0) then\n" +
                "  write(*,*)a(i)\n" +
                "  elseif(i.gt.0) then\n" +
                "  write(*,*)b(i)\n" +
                "  else if(i.lt.0) then\n" +
                "  write(*,*)c(i)\n" +
                "  endif\n" +
                "  enddo\n" +
                "  end\n" +
                "  real*8 function a(n)\n" +
                "  a=dble(n+10)\n" +
                "  return\n" +
                "  endfunction\n" +
                "  double precision function b(n)\n" +
                "  b=dble(n*10)\n" +
                "  end\n" +
                "  doubleprecision function c(n)\n" +
                "  c=dble(n-10)\n" +
                "  end function");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect if reformat (free form)",
                "implicit double precision (a - h)\n" +
                "implicit doubleprecision (o - z)\n" +
                "do i = -1, 1\n" +
                "    if (i .eq. 0) then\n" +
                "        write(*, *)a(i)\n" +
                "    elseif (i .gt. 0) then\n" +
                "        write(*, *)b(i)\n" +
                "    else if(i .lt. 0) then\n" +
                "        write(*, *)c(i)\n" +
                "    endif\n" +
                "enddo\n" +
                "end\n" +
                "real * 8 function a(n)\n" +
                "    a = dble(n + 10)\n" +
                "    return\n" +
                "endfunction\n" +
                "double precision function b(n)\n" +
                "    b = dble(n * 10)\n" +
                "end\n" +
                "doubleprecision function c(n)\n" +
                "    c = dble(n - 10)\n" +
                "end function");
    }
}
