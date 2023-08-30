package example.spring.data.nosql.hbase;

import cn.hutool.core.io.IoUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * HBase 管理工具类
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-03-27
 */
public class HBaseAdminHelper implements Closeable {

    private final Connection connection;
    private final Configuration configuration;

    protected HBaseAdminHelper(Configuration configuration) throws IOException {
        this.configuration = configuration;
        this.connection = ConnectionFactory.createConnection(configuration);
    }

    protected HBaseAdminHelper(Connection connection) throws IOException {
        this.configuration = connection.getConfiguration();
        this.connection = connection;
    }

    public synchronized static HBaseAdminHelper newInstance(Configuration configuration) throws IOException {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration can not be null!");
        }
        return new HBaseAdminHelper(configuration);
    }

    public synchronized static HBaseAdminHelper newInstance(Connection connection) throws IOException {
        if (connection == null) {
            throw new IllegalArgumentException("connection can not be null!");
        }
        return new HBaseAdminHelper(connection);
    }

    /**
     * 关闭内部持有的 HBase Connection 实例
     */
    @Override
    public synchronized void close() {
        if (null == connection || connection.isClosed()) {
            return;
        }
        IoUtil.close(connection);
    }

    /**
     * 获取 HBase 连接实例
     *
     * @return /
     */
    public Connection getConnection() {
        if (null == connection) {
            throw new RuntimeException("HBase connection init failed...");
        }
        return connection;
    }

    /**
     * 获取 HBase 配置
     *
     * @return /
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 创建命名空间
     *
     * @param namespace 命名空间
     */
    public void createNamespace(String namespace) throws IOException {
        NamespaceDescriptor nd = NamespaceDescriptor.create(namespace).build();
        Admin admin = getAdmin();
        admin.createNamespace(nd);
        admin.close();
    }

    /**
     * 删除命名空间
     *
     * @param namespace 命名空间
     */
    public void dropNamespace(String namespace) throws IOException {
        dropNamespace(namespace, false);
    }

    /**
     * 删除命名空间
     *
     * @param namespace 命名空间
     * @param force     是否强制删除
     */
    public void dropNamespace(String namespace, boolean force) throws IOException {
        Admin admin = getAdmin();
        if (force) {
            TableName[] tableNames = getAdmin().listTableNamesByNamespace(namespace);
            for (TableName name : tableNames) {
                admin.disableTable(name);
                admin.deleteTable(name);
            }
        }
        admin.deleteNamespace(namespace);
        admin.close();
    }

    /**
     * 指定表是否存在
     *
     * @param tableName 表名
     */
    public boolean existsTable(TableName tableName) throws IOException {
        Admin admin = getAdmin();
        boolean result = admin.tableExists(tableName);
        admin.close();
        return result;
    }

    /**
     * 创建表
     *
     * @param tableName 表名
     * @param families  列族
     */
    public void createTable(TableName tableName, String... families) throws IOException {
        createTable(tableName, null, families);
    }

    /**
     * 创建表
     *
     * @param tableName 表名
     * @param splitKeys 表初始区域的拆分关键字
     * @param families  列族
     */
    public void createTable(TableName tableName, byte[][] splitKeys, String... families) throws IOException {

        List<ColumnFamilyDescriptor> columnFamilyDescriptorList = new ArrayList<>();
        TableDescriptorBuilder builder = TableDescriptorBuilder.newBuilder(tableName);
        for (String cf : families) {
            ColumnFamilyDescriptor columnFamilyDescriptor = ColumnFamilyDescriptorBuilder.of(cf);
            columnFamilyDescriptorList.add(columnFamilyDescriptor);
        }
        builder.setColumnFamilies(columnFamilyDescriptorList);

        TableDescriptor td = builder.build();
        Admin admin = getAdmin();
        if (splitKeys != null) {
            admin.createTable(td, splitKeys);
        } else {
            admin.createTable(td);
        }
        admin.close();
    }

    /**
     * 删除表
     *
     * @param tableName 表名
     */
    public void dropTable(TableName tableName) throws IOException {
        if (existsTable(tableName)) {
            Admin admin = getAdmin();
            if (admin.isTableEnabled(tableName)) disableTable(tableName);
            admin.deleteTable(tableName);
            admin.close();
        }
    }

    /**
     * 禁用表
     *
     * @param tableName 表名
     */
    public void disableTable(TableName tableName) throws IOException {
        Admin admin = getAdmin();
        admin.disableTable(tableName);
        admin.close();
    }

    /**
     * 启用表
     *
     * @param tableName 表名
     */
    public void enableTable(TableName tableName) throws IOException {
        Admin admin = getAdmin();
        admin.enableTable(tableName);
        admin.close();
    }

    /**
     * 获取 {@link Table} 实例
     *
     * @param tableName 表名
     * @return /
     */
    public Table getTable(TableName tableName) throws IOException {
        return getConnection().getTable(tableName);
    }

    /**
     * 获取 {@link Admin} 实例
     *
     * @return /
     */
    public Admin getAdmin() throws IOException {
        return getConnection().getAdmin();
    }

}
