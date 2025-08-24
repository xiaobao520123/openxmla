package io.kylin.mdx.web.xmla;

import com.alibaba.fastjson.JSONArray;
import io.kylin.mdx.insight.common.SemanticConfig;
import io.kylin.mdx.insight.common.SemanticException;
import io.kylin.mdx.insight.common.util.AESWithECBEncryptor;
import io.kylin.mdx.core.datasource.MdCatalog;
import io.kylin.mdx.core.datasource.MdDatasource;
import mondrian.olap.Util;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

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

    private final String rootPath;

    private final String username;

    private final String password;

    private final String delegate;

    private final String project;

    private Path schema;

    private String jdbc;

    private String jdbcDriver;

    private final boolean force;

    public XmlaDatasource(String rootPath, String username, String password, String project,
                          String delegate, boolean forceRefresh) {
        this.rootPath = rootPath;
        this.username = username;
        this.password = password;
        this.project = project;
        this.delegate = delegate;
        this.force = forceRefresh;
        createSchemaDir();
        loadProject();
    }

    public void initDatasource() {
        createDatasource();
        createMdnSchemas();
    }

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

    private void loadProject() {
        // Load project configuration (main set)
        Path projectCfgPath = parseInsightHomePath(SemanticConfig.getInstance().getProjectConfigurationPath());
        String content;
        try {
            content = new String(Files.readAllBytes(projectCfgPath), "UTF-8");
        } catch (IOException e) {
            throw new SemanticException("Can't read project configuration file: " + projectCfgPath, e);
        }

        // Find project
        JSONObject projectInfo = null;
        try {
            JSONObject jProjects = JSONObject.parseObject(content);
            JSONArray jProjectsArray = jProjects.getJSONArray("projects");
            for (Object o : jProjectsArray) {
                JSONObject obj = (JSONObject) o;
                if (obj.getString("name").equals(project)) {
                    projectInfo = obj;
                    break;
                }
            }
        } catch (Exception e) {
            throw new SemanticException("Can't parse project configuration file: " + projectCfgPath, e);
        }
        if (projectInfo == null) {
            throw new SemanticException("Can't find project configuration: " + project);
        }

        // Parse project configuration
        String schema = projectInfo.getString("schema");
        if (schema == null) {
            throw new SemanticException("Project schema path is null: " + project);
        }
        Path path = parseInsightHomePath(schema);

        String jdbcUrl = projectInfo.getString("jdbc");
        if (jdbcUrl == null) {
            throw new SemanticException("Project jdbc url is null: " + project);
        }
        // Parse INSIGHT_HOME variable
        if (jdbcUrl.contains("${INSIGHT_HOME}")) {
            jdbcUrl = jdbcUrl.replace("${INSIGHT_HOME}", SemanticConfig.getInstance().getInsightHome());
        }

        String driver = projectInfo.getString("jdbc_driver");
        if (driver == null) {
            throw new SemanticException("Project jdbc driver is null: " + project);
        }

        this.schema = path;
        this.jdbc = jdbcUrl;
        this.jdbcDriver = driver;
    }

    private void createMdnSchemas() {
        // Load schema
        String content;
        try {
            content = new String(Files.readAllBytes(schema), "UTF-8");
        } catch (IOException e) {
            throw new SemanticException("Can't serialize schema:" + project);
        }

        byte[] schemaHashCode = Util.digestMd5(content);
        String schemaFilePath = getSchemaPath(getSchemaDir(), username, project, delegate);
        XmlaDatasourceManager.getInstance().checkEqualsAndWrite(project, schemaFilePath, schemaHashCode,
                content, "Can't create schema:%s", project);
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
        // SQLite JDBC
        StringBuilder builder = new StringBuilder("Provider=mondrian;UseContentChecksum=true")
                .append(";Jdbc=").append(jdbc)
                .append(";JdbcDrivers=").append(jdbcDriver)
                .append(";JdbcUser=").append(username)
                .append(";JdbcPassword=").append(AESWithECBEncryptor.encrypt(password));
        if (delegate != null) {
            builder.append(";JdbcDelegate=").append(delegate);
        }
        return builder.toString();
    }

    private List<MdCatalog> getMdCatalogs() {
        List<MdCatalog> mdCatalogs = new LinkedList<>();
        String catalogPath = getSchemaPath(SCHEMA_DIR, username, project, delegate);
        MdCatalog mdCatalog = new MdCatalog(project, catalogPath);
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

    private Path parseInsightHomePath(String... paths) {
        return Paths.get(SemanticConfig.getInstance().getInsightHome(), paths);
    }
}
