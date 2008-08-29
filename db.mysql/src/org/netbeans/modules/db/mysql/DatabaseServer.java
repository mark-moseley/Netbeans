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

package org.netbeans.modules.db.mysql;

import java.util.Collection;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.openide.nodes.Node.Cookie;

/**
 * An interface to a database server
 * @author David
 */
public interface DatabaseServer extends Cookie {

    /**
     * Connect to the server.  If we already have a connection, close
     * it and open a new one.  NOTE this is synchronous and can not be
     * called on the AWT thread.
     */
    public void reconnect() throws DatabaseException;

    /**
     * Reconnect to the MySQL server asynchronously, can be called on the
     * AWT thread.  If an error occurs, the error is quietly logged but no
     * dialog is displayed.
     */
    public void reconnectAsync();

    /**
     * Connect to the server, with the option not to display
     * a dialog but just write to the log if an error occurs
     * @param quiet true if you don't want this to happen without any dialogs
     * @param async true if you want to run this asychronously
     * being displayed in case of error or to get more information.
     */
    public void reconnect(final boolean quiet, boolean async);

    /**
     * Create a database on the server.  This runs <b>asynchronously</b>
     *
     * @param dbname the name of the database to create
     */
    public void createDatabase(String dbname);

    /**
     * Disconnect from the database.  This runs <b>asynchronously</b>
     */
    public void disconnect();

    /**
     * Disconnect from the database synchronously.  Can not be run
     * on the event dispatch thread.
     */
    public void disconnectSync();

    /**
     * Drop an existing database from the server.  This runs <b>asynchronously</b>
     * This method also removes any Database Connections from the Database Explorer
     * that are for this database.
     *
     * @param dbname the name of the database to drop.
     */
    void dropDatabase(String dbname);

    /**
     * Drop an existing database from the server.  This runs
     * <b>asynchronously</b>
     *
     * @param deleteConnections set to false if you want to delete
     *   associated connections from the Database Explorer
     *
     */
    void dropDatabase(String dbname, boolean deleteConnections);

    /**
     * Get the argument string for running the admin tool
     */
    public String getAdminArgs();

    /**
     * Get the path for the admin tool (could be a URL if the tool is web-based)
     */
    public String getAdminPath();

    /**
     * Get the list of databases.  NOTE that the list is retrieved from
     * a cache to improve performance.  If you want to ensure that the
     * list is up-to-date, call <i>refreshDatabaseList</i>
     *
     * @return
     * @see #refreshDatabaseList()
     * @throws org.netbeans.api.db.explorer.DatabaseException
     */
    public Collection<Database> getDatabases() throws DatabaseException;

    public String getDisplayName();

    public String getHost();

    public String getPassword();

    public String getPort();

    public String getShortDescription();

    public String getStartArgs();

    public String getStartPath();

    public String getStopArgs();

    public String getStopPath();

    public String getURL();

    public String getURL(String databaseName);

    public String getUser();

    /**
     * Get the list of users defined for this server.  This runs
     * <b>synchronously</b> so may cause some delay.
     *
     * @return the list of users
     *
     * @throws org.netbeans.api.db.explorer.DatabaseException
     * if some problem occurred
     */
    public List<DatabaseUser> getUsers() throws DatabaseException;

    /**
     * Grant full rights to the database to the specified user.  This runs
     * <b>asynchronously</b>
     *
     * @param dbname the database whose rights we are granting
     * @param grantUser the name of the user to grant the rights to
     */
    public void grantFullDatabaseRights(String dbname, DatabaseUser grantUser);

    public boolean isConnected();

    public boolean isSavePassword();

    /**
     * Refresh the list of databases for the server.  This runs
     * <b>asynchronously</b>
     */
    public void refreshDatabaseList();

    public void setAdminArgs(String args);

    public void setAdminPath(String path);

    public void setHost(String host);

    public void setPassword(String adminPassword);

    public void setPort(String port);

    public void setSavePassword(boolean savePassword);

    public void setStartArgs(String args);

    public void setStartPath(String path);

    public void setStopArgs(String args);

    public void setStopPath(String path);

    public void setUser(String adminUser);

    public void addChangeListener(ChangeListener listener);

    public void removeChangeListener(ChangeListener listener);

    /**
     * Launch the admin tool.  If the specified admin path is a URL,
     * a browser is launched with the URL.  If the specified admin path
     * is a file, the file path is executed.
     *
     * @return a process object for the executed command if the admin
     * path was a file.  Returns null if the browser was launched.
     *
     * @throws org.netbeans.api.db.explorer.DatabaseException
     */
    void startAdmin() throws DatabaseException;

    /**
     * Run the start command.  Display stdout and stderr to an output
     * window.  Wait the configured wait time, attempt to connect, and
     * then return.
     *
     * @return true if the server is definitely started, false otherwise (the server is
     * not started or the status is unknown).
     *
     * @throws org.netbeans.api.db.explorer.DatabaseException
     *
     * @see #getStartWaitTime()
     */
    void start() throws DatabaseException;

    /**
     * Run the stop command.  Display stdout and stderr to an output window.
     * This also disconnects the database server.
     *
     * @throws org.netbeans.api.db.explorer.DatabaseException
     */
    void stop() throws DatabaseException;

    /**
     * Return true if the database exists, false otherwise.  This is run
     * against the cached list of database so may not be completely accurate.
     */
    boolean databaseExists(String dbname) throws DatabaseException;

    /**
     * Throws a DatabaseException if the database connection is invalid.
     * This may result in a communication with the database server
     * and can be slow, particularly with remote servers, so use with caution.
     */
    void validateConnection() throws DatabaseException;
}
