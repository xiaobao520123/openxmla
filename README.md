# OpenXMLA

- OpenXMLA is a multidimensional data modeling engine based on [XMLA](https://learn.microsoft.com/en-us/analysis-services/xmla/xml-for-analysis-xmla-reference) protocol.

# Quick Start

## Start OpenXMLA service
```bash
# Setup Java 8
# ...
# Build project
scripts/build.sh
# Build docker image
scripts/dockerbuild.sh
# Start docker container
docker run -itd --name openxmla -p 7080:7080 openxmla:latest
```

## Connect to OpenXMLA

1. Open Microsoft Excel. Create a new workbook.
2. Go to "Data" -> "Get Data" -> "From Other Sources" -> "From Analysis Services".
3. Enter the server name as `http://localhost:7080/mdx/xmla/SalesWarehouse`. 
4. Enter any username and password (e.g., `admin`/`admin`).
5. Click "Next" to connect.

# Features

1. Support any kinds of database system that has a JDBC driver interface in SQL.

2. Front-end support
  - [x] Microsoft Excel
  - [x] Power BI
  - [ ] Tableau

3. Optimization
  - [x] Query cache. Cache consistency.
  - [x] Query rewrite
    - [x] Cross-join involves multiple dimensions. Reduce the impact from [Curse of dimensionality](https://en.wikipedia.org/wiki/Curse_of_dimensionality)
  - [ ] Filter pushdown
  - [ ] Paging

4. Maintenance
  - [x] Admin portal
  - [ ] Schema management
  - [ ] User management

# Configuration

# Evaluation

# License

## Apache Kylin

- Official Site: [https://kylin.apache.org/](https://kylin.apache.org/)
- Modified Version: [https://github.com/Kyligence/mdx-kylin](https://github.com/Kyligence/mdx-kylin)
- Licensed under the Apache License, Version 2.0 (the "License");

## Mondrian

- Official Site: [https://mondrian.pentaho.com/](https://mondrian.pentaho.com/)
- Modified Version: [https://github.com/pentaho/mondrian](https://github.com/pentaho/mondrian)
- Licensed under the
  Business Source License 1.1 (BSL)