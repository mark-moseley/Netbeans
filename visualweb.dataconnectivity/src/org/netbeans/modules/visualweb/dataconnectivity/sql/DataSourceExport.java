/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.dataconnectivity.sql;

/**
 * Class used to present a datasource available for export
 * can be annotated with whether or not should be exported
 * and whether or not username and/or password should be exported
 *
 * @author John Kline
 */
public class DataSourceExport {

    private boolean      exportable;
    private boolean      usernameExportable;
    private boolean      passwordExportable; // assume false if usernameExportable is false
    private String       name;
    private String       driverClassName;
    private String       url;
    private String       username;
    private String       password;
    private String       validationQuery;

    private String       alias ;

    public DataSourceExport(String name, String driverClassName, String url, String username,
        String password, String validationQuery) {

        exportable           = true;
        usernameExportable   = true;
        passwordExportable   = true;
        this.name            =  name;
        this.driverClassName = driverClassName;
        this.url             = url;
        this.username        = username;
        this.password        = password;
        this.validationQuery = validationQuery;
        this.alias           = null ;
    }

    public DataSourceExport(String name, String alias) {
        exportable           = true;
        this.name            = name ;
        this.alias           = alias ;
    }

    public boolean isExportable() {
        return exportable;
    }

    public void setExportable(boolean exportable) {
        this.exportable = exportable;
    }

    public boolean isUsernameExportable() {
        return usernameExportable;
    }

    public void setUsernameExportable(boolean usernameExportable) {
        this.usernameExportable = usernameExportable;
    }

    public boolean isPasswordExportable() {
        return passwordExportable;
    }

    public void setPasswordExportable(boolean passwordExportable) {
        this.passwordExportable = passwordExportable;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return getName().replaceFirst("java:comp/env/jdbc/", ""); // NOI18N
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    // We only store the ValidationQuery as "select * from <table>"
    // Extract the <table> here.
    public String getValidationTable() {
        // see if it start's with SELECT_PHRASE, otherwise try a hack
        String validationTable = DesignTimeDataSource.parseForValidationTable( getValidationQuery() );
        return ( validationTable == null ? "" : validationTable ) ; // NOI18N
    }
    public void setValidationTable(String table) {
//        setValidationQuery( DesignTimeDataSource.composeValidationQuery(table) ) ;
    }

    public void setAlias(String alias) {
        this.alias = alias ;
    }
    public String getAlias() {
        return this.alias ;
    }
    public boolean isAlias() {
        return ( this.alias != null ) ;
    }
}
