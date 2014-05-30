/*
 * Copyright 2008-2009 the original 赵永春(zyc@hasor.net).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.test.simple.db._06_transaction;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.hasor.db.jdbc.core.JdbcTemplate;
import net.hasor.db.transaction.Manager;
import net.hasor.db.transaction.TransactionBehavior;
import net.hasor.db.transaction.TransactionLevel;
import net.hasor.db.transaction.TransactionManager;
import net.hasor.db.transaction.TransactionStatus;
import net.test.simple.db.AbstractJDBCTest;
import org.junit.Before;
import org.more.util.CommonCodeUtils;
import org.more.util.StringUtils;
/***
 * 数据库测试程序基类
 * @version : 2014-1-13
 * @author 赵永春(zyc@hasor.net)
 */
public abstract class AbstractTransactionManagerJDBCTest extends AbstractJDBCTest {
    private TransactionManager transactionManager = null;
    @Before
    public void initContext() throws IOException, URISyntaxException, SQLException {
        super.initContext();
        JdbcTemplate jdbc = this.getJdbcTemplate();
        this.transactionManager = Manager.getTransactionManager(jdbc.getDataSource());
    }
    /**开始事物*/
    protected TransactionStatus begin(TransactionBehavior behavior) throws SQLException {
        return this.transactionManager.getTransaction(behavior, TransactionLevel.ISOLATION_READ_UNCOMMITTED);
    }
    /**递交事物*/
    protected void commit(TransactionStatus status) throws SQLException {
        this.transactionManager.commit(status);
    }
    /**回滚事物*/
    protected void rollBack(TransactionStatus status) throws SQLException {
        this.transactionManager.rollBack(status);
    }
    //
    private Thread watchThread = null;
    /**监视一张表的变化，当表的内容发生变化打印全表的内容。*/
    protected void watchTable(final String tableName) {
        this.watchThread = new Thread(new Runnable() {
            public void run() {
                try {
                    String hashValue = "";
                    Connection conn = getConnection();
                    conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                    while (true) {
                        String selectSQL = "select * from " + tableName;
                        String selectCountSQL = "select count(*) from " + tableName;
                        //
                        JdbcTemplate jdbc = new JdbcTemplate(conn);
                        List<Map<String, Object>> dataList = jdbc.queryForList(selectSQL);
                        int rowCount = jdbc.queryForInt(selectCountSQL);
                        String logData = printMapList(dataList, false);
                        String localHashValue = CommonCodeUtils.MD5.getMD5(logData);
                        if (!StringUtils.equals(hashValue, localHashValue)) {
                            System.out.println(String.format("-->Table ‘%s’ rowCount = %s.", tableName, rowCount));
                            System.out.println(logData);
                            hashValue = localHashValue;
                        } else {
                            System.out.println("table no change.");
                        }
                        //
                        Thread.sleep(500);
                    }
                } catch (Exception e) {}
            }
        });
        this.watchThread.setDaemon(true);
        this.watchThread.start();
    }
}