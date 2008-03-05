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

package org.netbeans.modules.mercurial.ui.log;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatus;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.util.HgUtils;

/**
 *
 * @author jr140578
 */
public class HgLogMessage {
    private char mod = 'M';
    private char add = 'A';
    private char del = 'R';
    private char copy = 'C';
    
    private List<HgLogMessageChangedPath> mpaths;
    private List<HgLogMessageChangedPath> apaths;
    private List<HgLogMessageChangedPath> dpaths;
    private List<HgLogMessageChangedPath> cpaths;
    private String rev;
    private String author;
    private String desc;
    private Date date;
    private String id;
    private String timeZoneOffset;
    
    public HgLogMessage(String changeset){
    }
    
    public HgLogMessage( String rev, String auth, String desc, String date, String id, 
            String fm, String fa, String fd, String fc){
        String splits[];

        this.rev = rev;
        this.author = auth;
        this.desc = desc;
        splits = date.split(" ");
        this.date = new Date(Long.parseLong(splits[0]) * 1000); // UTC in miliseconds       
        this.id = id;
        this.mpaths = new ArrayList<HgLogMessageChangedPath>();
        this.apaths = new ArrayList<HgLogMessageChangedPath>();
        this.dpaths = new ArrayList<HgLogMessageChangedPath>();
        this.cpaths = new ArrayList<HgLogMessageChangedPath>();
        
        if( fm != null && !fm.equals("")){
            splits = fm.split(" ");
            for(String s: splits){
                this.mpaths.add(new HgLogMessageChangedPath(s, mod));             
                logCopied(s);
            }
        }
        if( fa != null && !fa.equals("")){
            splits = fa.split(" ");
            for(String s: splits){
                this.apaths.add(new HgLogMessageChangedPath(s, add));                
                logCopied(s);
            }
        }
        if( fd != null && !fd.equals("")){
            splits = fd.split(" ");
            for(String s: splits){
                this.dpaths.add(new HgLogMessageChangedPath(s, del));                
                logCopied(s);
            }
        }
        if( fc != null && !fc.equals("")){
            splits = fc.split(" ");
            for(String s: splits){
                this.cpaths.add(new HgLogMessageChangedPath(s, copy));                
                logCopied(s);
            }
        }
    }

    private void logCopied(String s){
        File file = new File(s);
        FileInformation fi = Mercurial.getInstance().getFileStatusCache().getStatus(file);
        FileStatus fs = fi != null? fi.getStatus(file): null;
        if (fs != null && fs.isCopied()) {
            OutputLogger logger = OutputLogger.getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
            
            logger.outputInRed("*** Copied: " + s + " : " + fs.getFile() != null ? fs.getFile().getAbsolutePath() : "no filepath");
            logger.closeLog();
        }
    }
    
    public HgLogMessageChangedPath [] getChangedPaths(){
        List<HgLogMessageChangedPath> paths = new ArrayList<HgLogMessageChangedPath>();
        if(!mpaths.isEmpty()) paths.addAll(mpaths);
        if(!apaths.isEmpty()) paths.addAll(apaths);
        if(!dpaths.isEmpty()) paths.addAll(dpaths);
        if(!cpaths.isEmpty()) paths.addAll(cpaths);
        return paths.toArray(new HgLogMessageChangedPath[0]);
    }
    public String getRevision() {
        return rev;
    }
    public long getRevisionAsLong() {
        long revLong;
        try{
            revLong = Long.parseLong(rev);
        }catch(NumberFormatException ex){
            // Ignore number format errors
            return 0;
        }
        return revLong;
    }
    
    public Date getDate() {
        return date;
    }
    public String getAuthor() {
        return author;
    }
    public String getCSetShortID() {
        return id;
    }
    public String getMessage() {
        return desc;
    }
    public String getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(String timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }
    
    @Override
    public String toString(){
        String s = null;

        s = "rev: " + this.rev +
            "\nauthor: " + this.author +
            "\ndesc: " + this.desc +
            "\ndate: " + this.date +
            "\nid: " + this.id +
            "\nfm: " + this.mpaths +
            "\nfa: " + this.apaths +
            "\nfd: " + this.dpaths +
            "\nfc: " + this.cpaths;

        return s;
    }
}
