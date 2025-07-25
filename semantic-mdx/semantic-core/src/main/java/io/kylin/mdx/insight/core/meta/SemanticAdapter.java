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


package io.kylin.mdx.insight.core.meta;

import io.kylin.mdx.insight.common.http.Response;
import io.kylin.mdx.insight.core.model.generic.KylinGenericModel;
import io.kylin.mdx.insight.core.model.generic.KylinUserInfo;
import io.kylin.mdx.insight.core.model.acl.AclProjectModel;

import java.util.*;

public class SemanticAdapter {

    public static final SemanticAdapter INSTANCE = new SemanticAdapter();

    private static final String USER = "admin";
    private static final String PROJECT = "openxmla";
    private static final String CUBE = "SalesWarehouseCube";
    private static final String AUTHORITY = "ROLE_ADMIN";
    private static final String ACCESS_INFO = "GLOBAL_ADMIN";

    private SemanticAdapter() {
    }

    public List<String> getNoCacheCubeNames(String project) {
        List<String> cubeNames = new LinkedList<>();
        cubeNames.add(this.CUBE);
        return cubeNames;
    }

    public List<KylinUserInfo> getNoCacheUsers() {
        List<KylinUserInfo> users = new LinkedList<>();
        users.add(new KylinUserInfo(this.USER, null));
        return users;
    }

    public List<String> getNoCacheGroups() {
        return Collections.emptyList();
    }

    public List<String> getUserAuthority(ConnectionInfo connInfo) {
        List<String> authorities = new LinkedList<>();
        authorities.add(this.AUTHORITY);
        return Collections.emptyList();
    }

    public List<String> getSegments(String project) {
        // TODO: Implement segment updates to allow cache expiration.
        return Collections.emptyList();
    }

    public Map<String, Long> getDimensionCardinality(String project) {
        return Collections.EMPTY_MAP;
    }

    public List<KylinGenericModel> getNocacheGenericModels(String project) {
        return Collections.emptyList();
    }

    public Set<String> getActualProjectSet(ConnectionInfo connectionInfo) {
        Set<String> projectSet = new HashSet<>();
        projectSet.add(this.PROJECT);
        return projectSet;
    }

    public Response authentication(String basicAuth) {
        // TODO: something to do here.
        return new Response();
    }

    public Response getLicense() {
        return new Response();
    }

    public String getAccessInfo(ConnectionInfo connectionInfo) {
        return ACCESS_INFO;
    }

    public AclProjectModel getAclModel(String project, String type, String name, List<String> tables) {
        return new AclProjectModel(type, name, project);
    }

    public List<String> getGroupsByProject(String project) {
        return Collections.emptyList();
    }

    public List<String> getUsersByProject(String project) {
        List<String> users = new LinkedList<>();
        users.add(this.USER);
        return users;
    }

    public Response getProfileInfo(ConnectionInfo connectionInfo) {
        return new Response();
    }

}
