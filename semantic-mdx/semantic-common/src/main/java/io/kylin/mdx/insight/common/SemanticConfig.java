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


package io.kylin.mdx.insight.common;

import io.kylin.mdx.insight.common.constants.ConfigConstants;
import io.kylin.mdx.insight.common.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

/**
 * 配置文件加载：
 * #  取默认配置文件: insight.override.properties > insight.properties
 * #  取 propFile + ".override" 覆盖配置
 */
@Slf4j
public class SemanticConfig extends SemanticConfigBase {
    private static class SemanticConfigHolder {
        private static final SemanticConfig INSTANCE = new SemanticConfig();
    }

    private static final String HTTP_LITERAL = "http";

    private static final String HTTPS_LITERAL = "https";

    public static SemanticConfig getInstance() {
        return SemanticConfigHolder.INSTANCE;
    }

    private SemanticConfig() {
        loadConfig();
        initConfig();
    }

    public String getClustersInfo() {
        return getOptional("insight.mdx.cluster.nodes", "");
    }

    public void setClustersInfo(String clustersInfo) {
        getProperties().put("insight.mdx.cluster.nodes", clustersInfo);
    }

    public boolean isConvertorMock() {
        return getBooleanValue("converter.mock", false);
    }

    public boolean isSyncOnStartupEnable() {
        return getBooleanValue("insight.semantic.startup.sync.enable", true);
    }

    public boolean isClearOnStartupEnable() {
        return getBooleanValue("insight.semantic.startup.clear.enable", true);
    }

    public boolean isUpperAdminName() {
        return getBooleanValue("insight.mdx.upper-admin-name", true);
    }

    public boolean isUpperUserName() {
        return getBooleanValue("insight.mdx.upper-user-name", true);
    }

    public String getJDBC() {
        return getOptional("insight.mdx.mondrian.jdbc", "jdbc:sqlite:/Users/xiaobao/sqlite_warehouse.data");
    }

    public String getJDBCDriver() {
        return getOptional("insight.mdx.mondrian.jdbc.driver", "org.sqlite.JDBC");
    }

    public String getSchemaFilePath() {
        return getOptional("insight.mdx.mondrian.schema.path", "/Users/xiaobao/Projects/openxmla/build/conf/openxmla.xml");
    }

    public int getMdxQueryHousekeepMaxRows() {
        return getIntValue("insight.semantic.meta.keep.mdx-query.max.rows", 1000000);
    }

    public int getMdxQueryQueueSize() {
        return getIntValue("insight.semantic.meta.keep.mdx-query.queue.size", 1000);
    }

    public String getTimeZone() {
        return getOptional("insight.semantic.time-zone", "GMT+8:00");
    }

    public boolean isDatasetVerifyEnable() {
        return getBooleanValue("insight.dataset.verify.enable", true);
    }

    public int getDatasetVerifyInterval() {
        return getIntValue("insight.dataset.verify.interval.count", 15);
    }

    public boolean isDatasetAccessByDefault() {
        return getBooleanValue(SemanticConstants.DATASET_ALLOW_ACCESS_BY_DEFAULT, false);
    }

    public int getCookieAge() {
        return getIntValue("insight.semantic.cookie-age", 86400);
    }

    public String getMdxProtocol() {
        return getBooleanValue("insight.semantic.ssl.enabled", false) ? HTTPS_LITERAL : HTTP_LITERAL;
    }

    public String getMdxHost() {
        return getOptional("insight.host", "127.0.0.1");
    }

    public String getMdxPort() {
        return getOptional("insight.semantic.port", "6068");
    }

    public int getConnectTimeout() {
        return getIntValue("insight.semantic.connect.timeout", 5000);
    }

    public int getSocketTimeout() {
        return getIntValue("insight.semantic.socket.timeout", 10000);
    }

    public boolean isEnableSyncSegment() {
        return getBooleanValue("insight.semantic.segment.sync.enable", true);
    }


    public String getDatabaseType() {
        return getOptional("insight.database.type", "mysql");
    }

    public String getDatabaseIp() {
        return getOptional("insight.database.ip", "localhost");
    }

    public String getDatabasePort() {
        return getOptional("insight.database.port", "3306");
    }

    public String getDatabaseName() {
        return getOptional("insight.database.name", "mdx");
    }

    public String getPostgresqlSchema() {
        return getOptional("insight.database.postgres-schema", "public");
    }

    public int getMondrianServerSize() {
        return getIntValue("insight.mdx.mondrian.server.maxSize", 50);
    }

    public long getQueryTimeout() {
        return getLongValue("insight.mdx.mondrian.rolap.queryTimeout", 300L);
    }

    public String getSessionName() {
        boolean optimize = getBooleanValue("insight.semantic.cookie-optimize", true);
        if (isConvertorMock() || !optimize) {
            return "mdx_session";
        }

        String databaseType = getDatabaseType();
        String sessionBase;
        if ("pg".equalsIgnoreCase(databaseType)) {
            sessionBase = databaseType + getDatabaseIp() + getDatabasePort() + getDatabaseName() + getPostgresqlSchema();
        } else {
            sessionBase = databaseType + getDatabaseIp() + getDatabasePort() + getDatabaseName();
        }

        return "mdx_session_" + Base64.encodeBase64String(sessionBase.getBytes(StandardCharsets.UTF_8))
                .replace("=", "")
                .replace("/", "")
                .replace("+", "")
                .trim();
    }

    public boolean isGetHeapDump() {
        return getBooleanValue("insight.semantic.get.heap.dump", false);
    }

    /**
     * 是否禁止异步访问 http
     */
    public boolean isDisableAsyncHttpCall() {
        return getBooleanValue("insight.semantic.http.async.disable", false);
    }

    public boolean isEnableCheckReject() {
        return getBooleanValue("insight.semantic.reject.enable", false);
    }

    public String getRejectRedirectAddress() {
        return getOptional("insight.semantic.reject.redirect-address", "");
    }

    public String getContextPath() {
        return Utils.endWithSlash(getOptional("insight.semantic.context-path", "/"));
    }

    public String getMdxServletPath() {
        return Utils.endWithSlash(getOptional("insight.mdx.servlet-path", "/mdx/xmla/"));
    }

    public String getMdxGatewayPath() {
        return Utils.endWithSlash(getOptional("insight.mdx.gateway-path", "/mdx/xmla_server/"));
    }

    public String getDiscoverCatalogUrl(String project) {
        return getMdxProtocol() + "://" + getMdxHost() + ":" + getMdxPort() + getContextPath()
                + Utils.startWithoutSlash(getMdxServletPath()) + project;
    }

    public String getClearCacheUrl(String project) {
        return getDiscoverCatalogUrl(project) + "/clearCache";
    }

    public boolean isZookeeperEnable() {
        return getBooleanValue("insight.semantic.zookeeper.enable", false);
    }

    public String getZookeeperAddress() {
        return getOptional("insight.semantic.zookeeper.address", "localhost:2181");
    }

    public String getZookeeperNodePath() {
        return getOptional("insight.semantic.zookeeper.node.path", "/mdx");
    }

    public int getZookeeperTimeout() {
        return getIntValue("insight.semantic.zookeeper.session.timeout", 30000);
    }

    public boolean isEnableQueryWithExecuteAs() {
        return getBooleanValue("insight.semantic.query-with-execute-as", false);
    }

    public boolean isEnableCompressResult() {
        return getBooleanValue("insight.semantic.compress-result", true);
    }

    public boolean isEnableCompactResult() {
        return getBooleanValue("insight.semantic.compact-result", false);
    }

    /**
     * Whether to enable support for HIERARCHY_VISIBILITY and MEASURE_VISIBILITY restrictions.
     */
    public boolean isRowsetVisibilitiesSupported() {
        return getBooleanValue("insight.mdx.xmla.support-rowset-visibilities", true);
    }

    public String getProjectRowsetVisibilities() {
        return getOptional("insight.mdx.xmla.project-rowset-visibilities", "*");
    }

    /**
     * Whether to force return default-valued FormatString tags in XML/A responses.
     */
    public boolean isFormatStringDefaultValueForceReturned() {
        return getBooleanValue("insight.mdx.xmla.format-string.default.force-returned", false);
    }

    /**
     * The default value of <i>FormatString</i> tag. The default value of this tag is an empty string since 1.3.1,
     * was "regular" before 1.3.1.
     */
    public String formatStringDefaultValue() {
        return getOptional("insight.mdx.xmla.format-string.default", "");
    }

    public String getUploadFileMaxSize() {
        return getOptional("insight.mdx.upload.max-file-size", "20MB");
    }

    public int getMetaSyncInterval() {
        return getIntValue("insight.semantic.meta-sync.interval", 30);
    }

    public String getExportFileMaxSize() {
        return getOptional("insight.dataset.export-file-limit", "10MB");
    }

    /**
     * Micrometer integration Prometheus
     */
    public String getManagementWebBasePath(){
        return getOptional("insight.management.endpoints.web.base-path", "/actuator");
    }

    public boolean isPostgre() {
        return "pg".equalsIgnoreCase(getDatabaseType()) || "postgresql".equalsIgnoreCase(getDatabaseType()) || "postgres".equalsIgnoreCase(getDatabaseType());
    }

    public int getMdxQueryJobRunPeriodTime() {
        return getIntValue("insight.semantic.startup.query-house-keep.period", 3600);
    }

    public String getSecretKey() {
        return getOptional("insight.semantic.secret-key", "3500d18495a54c54b9a3d56641a8a521");
    }
}
