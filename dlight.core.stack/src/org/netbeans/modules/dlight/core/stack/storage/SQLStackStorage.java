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
package org.netbeans.modules.dlight.core.stack.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import org.netbeans.modules.dlight.api.storage.types.Time;
import org.netbeans.modules.dlight.core.stack.api.Function;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.spi.DemanglingFunctionNameService;
import org.netbeans.modules.dlight.spi.DemanglingFunctionNameServiceFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Stack data storage over a relational database.
 *
 * @author Alexey Vladykin
 */
public class SQLStackStorage {

    public static final List<FunctionMetric> METRICS = Arrays.<FunctionMetric>asList(
        FunctionMetric.CpuTimeInclusiveMetric, FunctionMetric.CpuTimeExclusiveMetric);
    protected final SQLDataStorage sqlStorage;
    private final Map<CharSequence, Integer> funcCache;
    private final Map<NodeCacheKey, Integer> nodeCache;
    private int funcIdSequence;
    private int nodeIdSequence;
    private final ExecutorThread executor;
    private final DemanglingFunctionNameService demanglingService;

    public SQLStackStorage(SQLDataStorage sqlStorage) throws SQLException, IOException {
        this.sqlStorage = sqlStorage;
        initTables();
        funcCache = new HashMap<CharSequence, Integer>();
        nodeCache = new HashMap<NodeCacheKey, Integer>();
        funcIdSequence = 0;
        nodeIdSequence = 0;
        executor = new ExecutorThread();
        executor.setPriority(Thread.MIN_PRIORITY);
        executor.start();
        DemanglingFunctionNameServiceFactory factory = Lookup.getDefault().lookup(DemanglingFunctionNameServiceFactory.class);
        if (factory != null) {
            demanglingService = factory.getForCurrentSession();
        } else {
            demanglingService = null;
        }
    }

    private void initTables() throws SQLException, IOException {
        InputStream is = SQLStackStorage.class.getClassLoader().getResourceAsStream("org/netbeans/modules/dlight/core/stack/resource/schema.sql");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            sqlStorage.execute(reader);
        } finally {
            reader.close();
        }
    }

    public int putStack(List<CharSequence> stack, long sampleDuration) {
        int callerId = 0;
        Set<Integer> funcs = new HashSet<Integer>();
        for (int i = 0; i < stack.size(); ++i) {
            boolean isLeaf = i + 1 == stack.size();
            CharSequence funcName = stack.get(i);
            int funcId = generateFuncId(funcName);
            updateMetrics(funcId, false, sampleDuration, !funcs.contains(funcId), isLeaf);
            funcs.add(funcId);
            int nodeId = generateNodeId(callerId, funcId);
            updateMetrics(nodeId, true, sampleDuration, true, isLeaf);
            callerId = nodeId;
        }
        return callerId;
    }

    public void flush() throws InterruptedException {
        executor.flush();
    }

    public List<Long> getPeriodicStacks(long startTime, long endTime, long interval) throws SQLException {
        List<Long> result = new ArrayList<Long>();
        PreparedStatement ps = sqlStorage.prepareStatement(
            "SELECT time_stamp FROM CallStack " +
            "WHERE ? <= time_stamp AND time_stamp < ? ORDER BY time_stamp");
        ps.setMaxRows(1);
        for (long time1 = startTime; time1 < endTime; time1 += interval) {
            long time2 = Math.min(time1 + interval, endTime);
            ps.setLong(1, time1);
            ps.setLong(2, time2);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result.add(rs.getLong(1));
            }
            rs.close();
        }
        return result;
    }

    public List<FunctionMetric> getMetricsList() {
        return METRICS;
    }

    public List<FunctionCall> getCallers(FunctionCall[] path, boolean aggregate) throws SQLException {
        List<FunctionCall> result = new ArrayList<FunctionCall>();
        PreparedStatement select = prepareCallersSelect(path);
        ResultSet rs = select.executeQuery();
        while (rs.next()) {
            Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();
            metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(4)));
            metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(5)));
            result.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(1), rs.getString(2), rs.getString(3)), metrics));
        }
        rs.close();
        return result;
    }

    public List<FunctionCall> getCallees(FunctionCall[] path, boolean aggregate) throws SQLException {
        List<FunctionCall> result = new ArrayList<FunctionCall>();
        PreparedStatement select = prepareCalleesSelect(path);
        ResultSet rs = select.executeQuery();
        while (rs.next()) {
            Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();
            metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(4)));
            metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(5)));
            result.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(1), rs.getString(2), rs.getString(3)), metrics));
        }
        rs.close();
        return result;
    }

    public List<FunctionCall> getHotSpotFunctions(FunctionMetric metric, int limit) throws SQLException {
        List<FunctionCall> result = new ArrayList<FunctionCall>();
        PreparedStatement select = sqlStorage.prepareStatement(
            "SELECT func_id, func_name, func_full_name,  time_incl, time_excl " +
            "FROM Func ORDER BY " + metric.getMetricID() + " DESC");
        select.setMaxRows(limit);
        ResultSet rs = select.executeQuery();
        while (rs.next()) {
            Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();
            metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(4)));
            metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(5)));
            result.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(1), rs.getString(2), rs.getString(3)), metrics));
        }
        rs.close();
        return result;
    }

////////////////////////////////////////////////////////////////////////////////
    private void updateMetrics(int id, boolean funcOrCall, long sampleDuration, boolean addIncl, boolean addExcl) {
        if (0 < sampleDuration) {
            UpdateMetrics cmd = new UpdateMetrics();
            cmd.objId = id;
            cmd.funcOrNode = funcOrCall;
            if (addIncl) {
                cmd.cpuTimeInclusive = sampleDuration;
            }
            if (addExcl) {
                cmd.cpuTimeExclusive = sampleDuration;
            }
            executor.submitCommand(cmd);
        }
    }

    private int generateNodeId(int callerId, int funcId) {
        synchronized (nodeCache) {
            NodeCacheKey cacheKey = new NodeCacheKey(callerId, funcId);
            Integer nodeId = nodeCache.get(cacheKey);
            if (nodeId == null) {
                nodeId = ++nodeIdSequence;
                AddNode cmd = new AddNode();
                cmd.id = nodeId;
                cmd.callerId = callerId;
                cmd.funcId = funcId;
                executor.submitCommand(cmd);
                nodeCache.put(cacheKey, nodeId);
            }
            return nodeId;
        }
    }

    private int generateFuncId(CharSequence funcName) {
        synchronized (funcCache) {
            Integer funcId = funcCache.get(funcName);
            if (funcId == null) {
                funcId = ++funcIdSequence;
                AddFunction cmd = new AddFunction();
                cmd.id = funcId;
                cmd.name = funcName;
                executor.submitCommand(cmd);
                funcCache.put(funcName, funcId);
            }
            return funcId;
        }
    }

    private PreparedStatement prepareCallersSelect(FunctionCall[] path) throws SQLException {
        StringBuilder buf = new StringBuilder();
        buf.append(" SELECT F.func_id, F.func_name, F.func_full_name, SUM(N.time_incl), SUM(N.time_excl) FROM Node AS N ");
        buf.append(" LEFT JOIN Func AS F ON N.func_id = F.func_id ");
        buf.append(" INNER JOIN Node N1 ON N.node_id = N1.caller_id ");
        for (int i = 1; i < path.length; ++i) {
            buf.append(" INNER JOIN Node AS N").append(i + 1);
            buf.append(" ON N").append(i).append(".node_id = N").append(i + 1).append(".caller_id ");
        }
        buf.append(" WHERE ");
        for (int i = 1; i <= path.length; ++i) {
            if (1 < i) {
                buf.append("AND ");
            }
            buf.append("N").append(i).append(".func_id = ? ");
        }
        buf.append(" GROUP BY F.func_id, F.func_name, F.func_full_name");
        PreparedStatement select = sqlStorage.prepareStatement(buf.toString());
        for (int i = 0; i < path.length; ++i) {
            select.setInt(i + 1, ((FunctionImpl) path[i].getFunction()).getId());
        }
        return select;
    }

    private PreparedStatement prepareCalleesSelect(FunctionCall[] path) throws SQLException {
        StringBuilder buf = new StringBuilder();
        buf.append("SELECT F.func_id, F.func_name, F.func_full_name, SUM(N.time_incl), SUM(N.time_excl) FROM Node AS N1 ");
        for (int i = 1; i < path.length; ++i) {
            buf.append(" INNER JOIN Node AS N").append(i + 1);
            buf.append(" ON N").append(i).append(".node_id = N").append(i + 1).append(".caller_id ");
        }
        buf.append(" INNER JOIN Node N ON N").append(path.length).append(".node_id = N.caller_id ");
        buf.append(" LEFT JOIN Func AS F ON N.func_id = F.func_id WHERE ");
        for (int i = 1; i <= path.length; ++i) {
            if (1 < i) {
                buf.append(" AND ");
            }
            buf.append(" N").append(i).append(".func_id = ? ");
        }
        buf.append(" GROUP BY F.func_id, F.func_name, F.func_full_name");
        PreparedStatement select = sqlStorage.prepareStatement(buf.toString());
        for (int i = 0; i < path.length; ++i) {
            select.setInt(i + 1, ((FunctionImpl) path[i].getFunction()).getId());
        }
        return select;
    }

////////////////////////////////////////////////////////////////////////////////
    private static class NodeCacheKey {

        private final int callerId;
        private final int funcId;

        public NodeCacheKey(int callerId, int funcId) {
            this.callerId = callerId;
            this.funcId = funcId;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof NodeCacheKey)) {
                return false;
            }
            final NodeCacheKey that = (NodeCacheKey) obj;
            return callerId == that.callerId && funcId == that.funcId;
        }

        @Override
        public int hashCode() {
            return 13 * callerId + 17 * funcId;
        }
    }

    protected static class FunctionImpl implements Function {

        private final int id;
        private final String name;
        private final String quilifiedName;

        public FunctionImpl(int id, String name, String qualified_name) {
            this.id = id;
            this.name = name;
            this.quilifiedName = qualified_name;
        }

//        public FunctionImpl(int id, String name) {
//            this.id = id;
//            this.name = name;
//        }
        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getQuilifiedName() {
            return quilifiedName;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    protected static class FunctionCallImpl extends FunctionCall {

        private final Map<FunctionMetric, Object> metrics;

        FunctionCallImpl(Function function, Map<FunctionMetric, Object> metrics) {
            super(function);
            this.metrics = metrics;
        }

        @Override
        public Object getMetricValue(FunctionMetric metric) {
            return metrics.get(metric);
        }

        @Override
        public Object getMetricValue(String metric_id) {
            for (FunctionMetric metric : metrics.keySet()) {
                if (metric.getMetricID().equals(metric_id)) {
                    return metrics.get(metric);
                }
            }
            return null;
        }

        @Override
        public boolean hasMetric(String metric_id) {
            for (FunctionMetric metric : metrics.keySet()) {
                if (metric.getMetricID().equals(metric_id)) {
                    return true;
                }
            }
            return false;

        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("FunctionCall{ function=").append(getFunction());
            buf.append(", metrics=").append(metrics).append(" }");
            return buf.toString();
        }
    }

    private static class AddFunction {

        public int id;
        public CharSequence name;
//        public CharSequence full_name;
    }

    private static class AddNode {

        public int id;
        public int callerId;
        public int funcId;
    }

    private static class UpdateMetrics {

        public int objId;
        public boolean funcOrNode; // false => func, true => node
        public long cpuTimeInclusive;
        public long cpuTimeExclusive;

        public void add(UpdateMetrics delta) {
            cpuTimeInclusive += delta.cpuTimeInclusive;
            cpuTimeExclusive += delta.cpuTimeExclusive;
        }
    }

    private class ExecutorThread extends Thread {

        private static final int MAX_COMMANDS = 1000;
        private static final long SLEEP_INTERVAL = 200l;
        private final LinkedBlockingQueue<Object> queue;

        public ExecutorThread() {
            queue = new LinkedBlockingQueue<Object>();
        }

        public void submitCommand(Object cmd) {
            queue.offer(cmd);
        }

        public synchronized void flush() throws InterruptedException {
            // Proper synchronization is needed to guarantee that
            // queue.isEmpty() is checked either before commands are
            // taken from the queue, or after they are executed.
            while (!queue.isEmpty()) {
                wait();
            }
        }

        @Override
        public void run() {
            try {
                Map<Integer, UpdateMetrics> funcMetrics = new HashMap<Integer, UpdateMetrics>();
                Map<Integer, UpdateMetrics> nodeMetrics = new HashMap<Integer, UpdateMetrics>();
                List<Object> cmds = new LinkedList<Object>();
                while (true) {
                    // Taking commands from queue and executing them should be one atomic action.
                    synchronized (this) {
                        queue.drainTo(cmds, MAX_COMMANDS);

                        // first pass: collect metrics
                        Iterator<Object> cmdIterator = cmds.iterator();
                        while (cmdIterator.hasNext()) {
                            Object cmd = cmdIterator.next();
                            if (cmd instanceof UpdateMetrics) {
                                UpdateMetrics updateMetricsCmd = (UpdateMetrics) cmd;
                                Map<Integer, UpdateMetrics> map = updateMetricsCmd.funcOrNode ? nodeMetrics : funcMetrics;
                                UpdateMetrics original = map.get(updateMetricsCmd.objId);
                                if (original == null) {
                                    map.put(updateMetricsCmd.objId, updateMetricsCmd);
                                } else {
                                    original.add(updateMetricsCmd);
                                }
                                cmdIterator.remove();
                            }
                        }

                        // second pass: execute inserts
                        cmdIterator = cmds.iterator();
                        while (cmdIterator.hasNext()) {
                            Object cmd = cmdIterator.next();
                            try {
                                if (cmd instanceof AddFunction) {
                                    AddFunction addFunctionCmd = (AddFunction) cmd;
                                    //demagle here
                                    PreparedStatement stmt = sqlStorage.prepareStatement("INSERT INTO Func (func_id, func_full_name, func_name, time_incl, time_excl) VALUES (?, ?, ?, ?, ?)");
                                    stmt.setInt(1, addFunctionCmd.id);
                                    stmt.setString(2, addFunctionCmd.name.toString());
                                    if (demanglingService == null) {
                                        stmt.setString(3, addFunctionCmd.name.toString());
                                    } else {
                                        Future<String> demangled = demanglingService.demangle(addFunctionCmd.name.toString());
                                        try {
                                            stmt.setString(3, demangled.get());
                                        } catch (ExecutionException ex) {
                                            stmt.setString(3, addFunctionCmd.name.toString());
                                        }
                                    }
                                    UpdateMetrics metrics = funcMetrics.remove(addFunctionCmd.id);
                                    if (metrics == null) {
                                        stmt.setLong(4, 0);
                                        stmt.setLong(5, 0);
                                    } else {
                                        stmt.setLong(4, metrics.cpuTimeInclusive);
                                        stmt.setLong(5, metrics.cpuTimeExclusive);
                                    }
                                    stmt.executeUpdate();
                                } else if (cmd instanceof AddNode) {
                                    AddNode addNodeCmd = (AddNode) cmd;
                                    PreparedStatement stmt = sqlStorage.prepareStatement("INSERT INTO Node (node_id, caller_id, func_id, time_incl, time_excl) VALUES (?, ?, ?, ?, ?)");
                                    stmt.setInt(1, addNodeCmd.id);
                                    stmt.setInt(2, addNodeCmd.callerId);
                                    stmt.setInt(3, addNodeCmd.funcId);
                                    UpdateMetrics metrics = nodeMetrics.remove(addNodeCmd.id);
                                    if (metrics == null) {
                                        stmt.setLong(4, 0);
                                        stmt.setLong(5, 0);
                                    } else {
                                        stmt.setLong(4, metrics.cpuTimeInclusive);
                                        stmt.setLong(5, metrics.cpuTimeExclusive);
                                    }
                                    stmt.executeUpdate();
                                }
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                        cmds.clear();

                        // third pass: execute updates
                        for (UpdateMetrics cmd : funcMetrics.values()) {
                            try {
                                PreparedStatement stmt = sqlStorage.prepareStatement("UPDATE Func SET time_incl = time_incl + ?, time_excl = time_excl + ? WHERE func_id = ?");
                                stmt.setLong(1, cmd.cpuTimeInclusive);
                                stmt.setLong(2, cmd.cpuTimeExclusive);
                                stmt.setInt(3, cmd.objId);
                                stmt.executeUpdate();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                        funcMetrics.clear();

                        for (UpdateMetrics cmd : nodeMetrics.values()) {
                            try {
                                PreparedStatement stmt = sqlStorage.prepareStatement("UPDATE Node SET time_incl = time_incl + ?, time_excl = time_excl + ? WHERE node_id = ?");
                                stmt.setLong(1, cmd.cpuTimeInclusive);
                                stmt.setLong(2, cmd.cpuTimeExclusive);
                                stmt.setInt(3, cmd.objId);
                                stmt.executeUpdate();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                        nodeMetrics.clear();

                        notifyAll();
                    }

                    Thread.sleep(SLEEP_INTERVAL);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

}
