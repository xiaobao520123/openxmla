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


package io.kylin.mdx.insight.core.service;

import io.kylin.mdx.insight.common.SemanticException;
import io.kylin.mdx.insight.core.sync.*;

/**
 * @author qi.wu
 */
public interface MetaSyncService {
    /**
     * sync check
     *
     * @param null
     * @Return boolean
     * @throws SemanticException
     */
    boolean syncCheck() throws SemanticException;

    /**
     * sync projects
     *
     * @param null
     * @Return null
     * @throws SemanticException
     */
    void syncProjects() throws SemanticException;

    /**
     * sync datasets
     *
     * @param null
     * @Return null
     * @throws SemanticException
     */
    void syncDataset() throws SemanticException;

    /**
     * sync cube
     *
     * @param null
     * @Return null
     * @throws SemanticException
     */
    void syncCube() throws SemanticException;

    /**
     * sync user
     *
     * @param null
     * @Return null
     * @throws SemanticException
     */
    void syncUser() throws SemanticException;

    /**
     * sync segment
     *
     * @param null
     * @Return null
     * @throws SemanticException
     */
    void syncSegment() throws SemanticException;

    /**
     * clear project cache
     *
     * @param project
     * @Return null
     * @throws SemanticException
     */
    void clearProjectCache(String project ) throws SemanticException;
}
