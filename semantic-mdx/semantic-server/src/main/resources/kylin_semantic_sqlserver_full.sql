SET NOCOUNT ON;

-- ----------------------------
--  Table structure for `calculate_measure`
-- ----------------------------
CREATE TABLE [calculate_measure] (
  [id] int IDENTITY(1,1) NOT NULL,
  [dataset_id] int NOT NULL,
  [name] varchar(300) NOT NULL,
  [expression] varchar(5000) NOT NULL,
  [format] varchar(150) NOT NULL,
  [format_type] varchar(50) DEFAULT '',
  [measure_folder] varchar(200) NOT NULL DEFAULT '',
  [extend] varchar(max) NOT NULL DEFAULT '',
  [visible_flag] tinyint NOT NULL DEFAULT 1,
  [translation] varchar(500) NULL,
  [subfolder] varchar(100) NULL,
  [non_empty_behavior] varchar(3000) NOT NULL DEFAULT '[]',
  PRIMARY KEY ([id])
);

CREATE INDEX [dataset_id_key] ON [calculate_measure] ([dataset_id]);

-- ----------------------------
--  Table structure for `common_dim_relation`
-- ----------------------------
CREATE TABLE [common_dim_relation] (
  [id] int IDENTITY(1,1) NOT NULL,
  [dataset_id] int NOT NULL,
  [model] varchar(100) NOT NULL,
  [model_related] varchar(100) NOT NULL,
  [relation] varchar(2000) NOT NULL,
  PRIMARY KEY ([id])
);

CREATE INDEX [dataset_id_key] ON [common_dim_relation] ([dataset_id]);
CREATE UNIQUE INDEX [unique_pk] ON [common_dim_relation] ([dataset_id], [model], [model_related]);

-- ----------------------------
--  Table structure for `custom_hierarchy`
-- ----------------------------
CREATE TABLE [custom_hierarchy] (
  [id] int IDENTITY(1,1) NOT NULL,
  [dataset_id] int NOT NULL,
  [model] varchar(100) NOT NULL,
  [dim_table] varchar(300) NOT NULL,
  [name] varchar(300) NOT NULL,
  [dim_col] varchar(300) NOT NULL,
  [description] varchar(1000) NOT NULL DEFAULT '',
  [translation] varchar(500) NULL,
  [weight_col] varchar(300) NULL,
  PRIMARY KEY ([id])
);

CREATE INDEX [dataset_id_key] ON [custom_hierarchy] ([dataset_id]);

-- ----------------------------
--  Table structure for `dataset`
-- ----------------------------
CREATE TABLE [dataset] (
  [id] int IDENTITY(1,1) NOT NULL,
  [project] varchar(100) NOT NULL,
  [dataset] varchar(100) NOT NULL,
  [status] varchar(10) NOT NULL DEFAULT 'NORMAL',
  [broken_msg] NVARCHAR(MAX) NULL,
  [canvas] varchar(5000) NOT NULL DEFAULT '',
  [front_v] varchar(20) NOT NULL DEFAULT 'v0.1',
  [create_user] varchar(255) NOT NULL,
  [create_time] bigint NOT NULL DEFAULT 0,
  [modify_time] bigint NOT NULL DEFAULT 0,
  [extend] varchar(max) NOT NULL DEFAULT '',
  [translation_types] varchar(100) NULL,
  [access] int DEFAULT 0,
  PRIMARY KEY ([id])
);

CREATE UNIQUE INDEX [unique_pk] ON [dataset] ([project], [dataset]);

-- ----------------------------
--  Table structure for `dim_table_model_rel`
-- ----------------------------
CREATE TABLE [dim_table_model_rel] (
  [id] int IDENTITY(1,1) NOT NULL,
  [dataset_id] int NOT NULL,
  [model] varchar(100) NOT NULL,
  [dim_table] varchar(300) NOT NULL,
  [relation] int NOT NULL,
  [intermediate_dim_table] varchar(300) NOT NULL DEFAULT '',
  [primary_dim_col] varchar(300) NOT NULL DEFAULT '',
  PRIMARY KEY ([id])
);

CREATE INDEX [dataset_id_key] ON [dim_table_model_rel] ([dataset_id]);

-- ----------------------------
--  Table structure for `measure_group`
-- ----------------------------
CREATE TABLE [measure_group] (
  [id] int IDENTITY(1,1) NOT NULL,
  [dataset_id] int NOT NULL,
  [name] varchar(300) NOT NULL,
  [dim_table] varchar(300) NOT NULL DEFAULT '',
  [calculate_measure] varchar(300) NOT NULL DEFAULT '',
  PRIMARY KEY ([id])
);

CREATE INDEX [dataset_id_key] ON [measure_group] ([dataset_id]);

-- ----------------------------
--  Table structure for `named_dim_col`
-- ----------------------------
CREATE TABLE [named_dim_col] (
  [id] int IDENTITY(1,1) NOT NULL,
  [dataset_id] int NOT NULL,
  [model] varchar(100) NOT NULL,
  [dim_table] varchar(300) NOT NULL,
  [dim_col] varchar(300) NOT NULL,
  [dim_col_alias] varchar(300) NOT NULL,
  [col_type] int NOT NULL DEFAULT 0,
  [data_type] varchar(50) NOT NULL DEFAULT '',
  [extend] varchar(max) NOT NULL DEFAULT '',
  [visible_flag] tinyint NOT NULL DEFAULT 1,
  [name_column] varchar(300) NULL,
  [value_column] varchar(300) NULL,
  [translation] varchar(500) NULL,
  [subfolder] varchar(100) NULL,
  [default_member] varchar(1000) NULL,
  PRIMARY KEY ([id])
);

CREATE INDEX [dataset_id_key] ON [named_dim_col] ([dataset_id]);

-- ----------------------------
--  Table structure for `named_dim_table`
-- ----------------------------
CREATE TABLE [named_dim_table] (
  [id] int IDENTITY(1,1) NOT NULL,
  [dataset_id] int NOT NULL,
  [model] varchar(100) NOT NULL,
  [dim_table] varchar(300) NOT NULL,
  [dim_table_alias] varchar(300) NOT NULL DEFAULT '',
  [dim_table_type] varchar(100) NOT NULL DEFAULT 'regular',
  [actual_table] varchar(300) NOT NULL DEFAULT '',
  [fact_table] varchar(300) NOT NULL DEFAULT '',
  [translation] varchar(500) NULL,
  PRIMARY KEY ([id])
);

CREATE INDEX [dataset_id_key] ON [named_dim_table] ([dataset_id]);

-- ----------------------------
--  Table structure for `named_measure`
-- ----------------------------
CREATE TABLE [named_measure] (
  [id] int IDENTITY(1,1) NOT NULL,
  [dataset_id] int NOT NULL,
  [model] varchar(100) NOT NULL,
  [name] varchar(300) NOT NULL,
  [alias] varchar(300) NOT NULL,
  [dim_column] varchar(300) NOT NULL DEFAULT '',
  [data_type] varchar(50) NOT NULL DEFAULT '',
  [expression] varchar(200) NOT NULL DEFAULT '',
  [extend] varchar(max) NOT NULL DEFAULT '',
  [visible_flag] tinyint NOT NULL DEFAULT 1,
  [translation] varchar(500) NULL,
  [format] varchar(150) NULL,
  [format_type] varchar(50) DEFAULT '',
  [subfolder] varchar(100) NULL,
  PRIMARY KEY ([id])
);

CREATE INDEX [dataset_id_key] ON [named_measure] ([dataset_id]);

-- ----------------------------
--  Table structure for `user_info`
-- ----------------------------
CREATE TABLE [user_info] (
  [id] int IDENTITY(1,1) NOT NULL,
  [username] varchar(255) NOT NULL,
  [password] varchar(4096) NOT NULL,
  [active] int NOT NULL,
  [license_auth] int NOT NULL,
  [login_count] int NOT NULL DEFAULT 0,
  [last_login] bigint NOT NULL DEFAULT 0,
  [create_time] bigint NOT NULL DEFAULT 0,
  [conf_usr] int NULL,
  PRIMARY KEY ([id])
);

CREATE UNIQUE INDEX [username_key] ON [user_info] ([username]);

-- ----------------------------
--  Table structure for `named_set`
-- ----------------------------
CREATE TABLE [named_set] (
  [id] int IDENTITY(1,1) NOT NULL,
  [dataset_id] int NOT NULL,
  [name] varchar(300) NOT NULL,
  [expression] varchar(5000) NOT NULL,
  [folder] varchar(200) NOT NULL DEFAULT '',
  [location] varchar(300) NOT NULL DEFAULT '',
  [extend] varchar(max) NOT NULL DEFAULT '',
  [visible_flag] tinyint NOT NULL DEFAULT 1,
  [translation] varchar(500) NULL,
  PRIMARY KEY ([id])
);

CREATE INDEX [dataset_id_key] ON [named_set] ([dataset_id]);

-- ----------------------------
--  Table structure for `role_info`
-- ----------------------------
CREATE TABLE [role_info] (
  [id] int IDENTITY(1,1) NOT NULL,
  [name] varchar(20) NOT NULL,
  [extend] varchar(max) NOT NULL DEFAULT '',
  [description] varchar(500) NOT NULL,
  [create_time] bigint NOT NULL DEFAULT 0,
  [modify_time] bigint NOT NULL DEFAULT 0,
  PRIMARY KEY ([id])
);

CREATE INDEX [name_key] ON [role_info] ([name]);

-- ----------------------------
--  Table structure for `MDX_QUERY`
-- ----------------------------
CREATE TABLE [mdx_query] (
  [id] int IDENTITY(1,1) NOT NULL,
  [mdx_query_id] char(36) NOT NULL,
  [mdx_text] NVARCHAR(MAX) NOT NULL,
  [start] bigint NULL,
  [total_execution_time] bigint NULL,
  [username] varchar(255) NOT NULL,
  [success] tinyint NOT NULL DEFAULT 0,
  [project] varchar(255) NOT NULL,
  [application] varchar(64) NOT NULL,
  [mdx_cache_used] tinyint NOT NULL DEFAULT 0,
  [before_connection] bigint NULL,
  [connection] bigint NULL,
  [hierarchy_load] bigint NULL,
  [olaplayout_construction] bigint NULL,
  [aggregationqueries_construction] bigint NULL,
  [aggregationqueries_execution] bigint NULL,
  [otherresult_construction] bigint NULL,
  [network_package] int NULL,
  [timeout] tinyint NOT NULL DEFAULT 0,
  [message] NVARCHAR(MAX) NULL,
  [calculate_axes] bigint NULL,
  [calculate_cell] bigint NULL,
  [calculate_cellrequest_num] bigint NULL,
  [create_rolapresult] bigint NULL,
  [create_multidimensional_dataset] bigint NULL,
  [marshall_soap_message] bigint NULL,
  [dataset_name] varchar(255) NOT NULL,
  [gateway] tinyint NOT NULL DEFAULT 0,
  [other_used] tinyint NOT NULL DEFAULT 0,
  [node] varchar(64) NULL,
  [reserved_field_1] varchar(2000) NULL,
  [reserved_field_2] varchar(2000) NULL,
  [reserved_field_3] varchar(5000) NULL,
  [reserved_field_4] varchar(5000) NULL,
  [reserved_field_5] NVARCHAR(MAX) NULL,
  PRIMARY KEY ([id])
);

CREATE INDEX [mdx_query_id_key] ON [mdx_query] ([mdx_query_id]);
CREATE INDEX [project_key] ON [mdx_query] ([project]);
CREATE INDEX [start_key] ON [mdx_query] ([start]);
CREATE INDEX [success_key] ON [mdx_query] ([success]);
CREATE INDEX [total_execution_time_key] ON [mdx_query] ([total_execution_time]);
CREATE INDEX [dataset_name_key] ON [mdx_query] ([dataset_name]);
CREATE INDEX [node_key] ON [mdx_query] ([node]);
CREATE UNIQUE INDEX [unique_pk] ON [mdx_query] ([mdx_query_id]);

-- ----------------------------
--  Table structure for `SQL_QUERY`
-- ----------------------------
CREATE TABLE [sql_query] (
  [id] int IDENTITY(1,1) NOT NULL,
  [mdx_query_id] char(36) NOT NULL,
  [sql_text] NVARCHAR(MAX) NOT NULL,
  [sql_execution_time] bigint NULL,
  [sql_cache_used] tinyint NOT NULL DEFAULT 0,
  [exec_status] tinyint NOT NULL DEFAULT 1,
  [ke_query_id] varchar(100) NULL,
  [reserved_field_1] varchar(2000) NULL,
  [reserved_field_2] varchar(2000) NULL,
  [reserved_field_3] varchar(5000) NULL,
  [reserved_field_4] varchar(5000) NULL,
  [reserved_field_5] NVARCHAR(MAX) NULL,
  PRIMARY KEY ([id])
);

CREATE INDEX [mdx_query_id_key] ON [sql_query] ([mdx_query_id]);

-- 添加外键约束
ALTER TABLE [sql_query] ADD CONSTRAINT [sql_mdx_query_id]
FOREIGN KEY ([mdx_query_id]) REFERENCES [mdx_query] ([mdx_query_id]) ON DELETE CASCADE;

-- ----------------------------
--  Table structure for `mdx_info`
-- ----------------------------
CREATE TABLE [mdx_info] (
  [id] int IDENTITY(1,1) NOT NULL,
  [mdx_version] varchar(50) NULL,
  [create_time] bigint NOT NULL DEFAULT 0,
  [modify_time] bigint NOT NULL DEFAULT 0,
  PRIMARY KEY ([id])
);

-- 插入初始数据
INSERT INTO [mdx_info] ([mdx_version], [create_time], [modify_time])
VALUES ('', 0, 0);

INSERT INTO [role_info] ([name], [extend], [description], [create_time], [modify_time])
VALUES ('Admin', '{"contains":[{"name":"ADMIN","type":"user"}]}', 'This role is an admin role with all semantic information access to all datasets', 0, 0);