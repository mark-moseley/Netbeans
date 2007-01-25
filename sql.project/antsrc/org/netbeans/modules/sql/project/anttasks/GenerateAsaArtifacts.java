/* *************************************************************************
 *
 *          Copyright (c) 2002, SeeBeyond Technology Corporation,
 *          All Rights Reserved
 *
 *          This program, and all the routines referenced herein,
 *          are the proprietary properties and trade secrets of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 *          Except as provided for by license agreement, this
 *          program shall not be duplicated, used, or disclosed
 *          without  written consent signed by an officer of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 ***************************************************************************/
package org.netbeans.modules.sql.project.anttasks;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;



/**
 *
 * @author blu
 */
public class GenerateAsaArtifacts extends Task {
    private String mSrcDirectoryLocation;
    private String mBuildDirectoryLocation;
    private String mJbiDescriptorFileLocation;
    
    /** Creates a new instance of GenerateIEPASAArtifacts */
    public GenerateAsaArtifacts() {
    }
    
    /**
     * @return Returns the srcDirectoryLocation.
     */
    public String getSrcDirectoryLocation() {
        return mSrcDirectoryLocation;
    }
    /**
     * @param srcDirectoryLocation The srcDirectoryLocation to set.
     */
    public void setSrcDirectoryLocation(String srcDirectoryLocation) {
        mSrcDirectoryLocation = srcDirectoryLocation;
    }
    
    /**
     * @return Returns the srcDirectoryLocation.
     */
    public String getBuildDirectoryLocation() {
        return mBuildDirectoryLocation;
    }
    /**
     * @param srcDirectoryLocation The srcDirectoryLocation to set.
     */
    public void setBuildDirectoryLocation(String buildDirectoryLocation) {
        mBuildDirectoryLocation = buildDirectoryLocation;
    }
    
    /**
     * @return Returns the portMapFileLocation.
     */
    public String getJbiDescriptorFileLocation() {
        return mJbiDescriptorFileLocation;
    }
    /**
     * @param portMapFileLocation The portMapFileLocation to set.
     */
    public void setJbiDescriptorFileLocation(String jbiDescriptorFileLocation) {
        mJbiDescriptorFileLocation = jbiDescriptorFileLocation;
    }
    
    public void execute() throws BuildException {
        File srcDir = new File(mSrcDirectoryLocation);
        if (!srcDir.exists()) {
            throw new BuildException("Directory " + mSrcDirectoryLocation + " does not exit.");
        }
        try {
            String srcDirPath = srcDir.getAbsolutePath();
            //1: for appending '/'
            int srcDirPathLen = srcDir.getPath().length() + 1;
            String[] ext = new String[]{".sql"};
        }catch (Exception e) {
            throw new BuildException(e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        GenerateAsaArtifacts tsk = new GenerateAsaArtifacts();
        tsk.setJbiDescriptorFileLocation("c:/temp/portMap.xml");
        tsk.setSrcDirectoryLocation("c:/temp");
        tsk.execute();
    }
    
    
}
