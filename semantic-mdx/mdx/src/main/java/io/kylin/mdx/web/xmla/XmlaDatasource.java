/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.kylin.mdx.web.xmla;

import io.kylin.mdx.insight.common.SemanticException;
import io.kylin.mdx.insight.common.util.AESWithECBEncryptor;
import io.kylin.mdx.core.MdxConfig;
import io.kylin.mdx.core.datasource.MdCatalog;
import io.kylin.mdx.core.datasource.MdDatasource;
import mondrian.olap.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.kylin.mdx.insight.common.util.DatasourceUtils.getDatasourceName;
import static io.kylin.mdx.insight.common.util.DatasourceUtils.getDatasourcePath;
import static io.kylin.mdx.insight.common.util.DatasourceUtils.getSchemaPath;

/**
 * XMLA 数据源: datasource -> catalogs
 * 普通模式:
 * #  username + project + "datasources.xml"
 * #  ->  username + project + catalog + ".xml"
 * 委任模式:
 * #  delegate + username + project + "datasources.xml"
 * #  ->  username + project + catalog + ".xml"
 * 两种模式的 schema(catalog) 名字会出现重复
 */
public class XmlaDatasource {

    private static final String SCHEMA_DIR = "/WEB-INF/schema/";

    /**
     * 保存 username(delegate) + project -> last modified time
     */
    private static final Map<String, Long> ACL_UPDATES = new ConcurrentHashMap<>();

    private final MdxConfig config = MdxConfig.getInstance();

    private final String rootPath;

    private final String username;

    private final String password;

    private final String delegate;

    private final String project;

    private final boolean force;

    private final static String catalogName = "SalesWarehouse";

    public XmlaDatasource(String rootPath, String username, String password, String project,
                          String delegate, boolean forceRefresh) {
        this.rootPath = rootPath;
        this.username = username;
        this.password = password;
        this.project = project;
        this.delegate = delegate;
        this.force = forceRefresh;
        createSchemaDir();
    }

//    public void initDatasource() {
//        initDatasource(loadMdnSchemas());
//    }
//
//    public void initDatasource(MdnSchemaSet mdnSchemas) {
//        if (CollectionUtils.isNotEmpty(mdnSchemas.getMdnSchemas())) {
//            createDatasource(mdnSchemas);
//            createMdnSchemas(mdnSchemas);
//        }
//    }

    public void initDatasource() {
        createDatasource();
        createMdnSchemas();
    }

//    private MdnSchemaSet loadMdnSchemas() {
//        ConnectionInfo connectionInfo = ConnectionInfo.builder()
//                .user(username)
//                .password(password)
//                .project(project)
//                .delegate(delegate)
//                .build();
//        if (MdxConfig.getInstance().isCreateSchemaFromDataSet()) {
//            return new ModelManager().buildMondrianSchemaFromDataSet(connectionInfo);
//        } else {
//            return new ModelManager().buildMondrianSchemaByKylin(connectionInfo);
//        }
//    }

    private void createSchemaDir() {
        File file = new File(getSchemaDir());
        if (!file.exists()) {
            synchronized (XmlaDatasource.class) {
                if (!file.exists() && !file.mkdirs()) {
                    throw new SemanticException("Can't create directory:" + file.getAbsolutePath());
                }
            }
        }
    }

    private void createMdnSchemas() {
        // Load schema from local file
        String sourceSchemaFilePath = "/Users/xiaobao/Projects/openxmla/build/conf/openxmla.xml";
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(sourceSchemaFilePath)), "UTF-8");
        } catch (IOException e) {
            throw new SemanticException("Can't serialize schema:" + catalogName);
        }

        byte[] schemaHashCode = Util.digestMd5(content);
        String schemaFilePath = getSchemaPath(getSchemaDir(), username, project, catalogName, delegate);
        XmlaDatasourceManager.getInstance().checkEqualsAndWrite(project, schemaFilePath, schemaHashCode,
                content, "Can't create schema:%s", catalogName);
    }

    private void createDatasource() {
        String datasourceName = getDatasourceName(username, project, delegate);
        String datasourceInfo = buildDatasourceInfo();
        List<MdCatalog> catalogs = getMdCatalogs();
        long lastModified = getLastModified();
        MdDatasource mdDatasource = new MdDatasource(datasourceName, datasourceInfo, catalogs, lastModified);

        String datasourceFile = getDatasourcePath(getDatasourceDir(), username, project, delegate);
        String content = mdDatasource.toString();
        byte[] datasourceHashCode = Util.digestMd5(content);
        XmlaDatasourceManager.getInstance().checkEqualsAndWrite(project, datasourceFile, datasourceHashCode,
                content, "Can't create datasource:%s", datasourceName);
    }

    private String buildDatasourceInfo() {
//        StringBuilder builder = new StringBuilder("Provider=mondrian;UseContentChecksum=true;Jdbc=jdbc:kylin://")
//                .append(config.getKylinHost()).append(":").append(config.getKylinPort()).append("/").append(project)
//                .append(";JdbcDrivers=org.apache.kylin.jdbc.Driver")
//                .append(";JdbcUser=").append(username)
//                .append(";JdbcPassword=").append(AESWithECBEncryptor.encrypt(password));

        // SQLite JDBC
        String databaseFile = "/Users/xiaobao/sqlite_warehouse.data";

        StringBuilder builder = new StringBuilder("Provider=mondrian;UseContentChecksum=true")
                .append(";Jdbc=jdbc:sqlite:").append(databaseFile)
                .append(";JdbcDrivers=org.sqlite.JDBC")
                .append(";JdbcUser=").append(username)
                .append(";JdbcPassword=").append(AESWithECBEncryptor.encrypt(password));
        if (delegate != null) {
            builder.append(";JdbcDelegate=").append(delegate);
        }
        if ("https".equals(config.getKeProtocol())) {
            builder.append(";ssl=true");
        }
        return builder.toString();
    }

    private List<MdCatalog> getMdCatalogs() {
        List<MdCatalog> mdCatalogs = new LinkedList<>();
        String catalogPath = getSchemaPath(SCHEMA_DIR, username, project, catalogName, delegate);
        MdCatalog mdCatalog = new MdCatalog(catalogName, catalogPath);
        mdCatalogs.add(mdCatalog);
        return mdCatalogs;
    }

    private synchronized long getLastModified() {
        return System.currentTimeMillis();
    }

    private String getDatasourceDir() {
        return this.rootPath + "/";
    }

    private String getSchemaDir() {
        return this.rootPath + "/schema/";
    }

}
