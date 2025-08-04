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


package io.kylin.mdx.insight.engine.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.kylin.mdx.insight.common.PwdDecryptException;
import io.kylin.mdx.insight.common.SemanticConfig;
import io.kylin.mdx.insight.common.SemanticException;
import io.kylin.mdx.insight.common.SemanticUserAndPwd;
import io.kylin.mdx.insight.common.util.Utils;
import io.kylin.mdx.insight.core.entity.UserInfo;
import io.kylin.mdx.insight.core.manager.CubeManager;
import io.kylin.mdx.insight.core.manager.ProjectManager;
import io.kylin.mdx.insight.core.manager.SegmentManager;
import io.kylin.mdx.insight.core.service.MetaSyncService;
import io.kylin.mdx.insight.core.service.UserService;
import io.kylin.mdx.insight.core.support.Execution;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

/**
 * This class does the following in the background in an infinite loop:
 * <p>
 * 1. pull the Kylin cube metadata periodically
 * 2. compare the cubes in project to the cache, then fire observers
 * 3. Putting together cube-model and dataset information validates dataset's effective
 * 4. if dataset got something wrong, set this dataset's status to broken
 *
 * @author qi.wu
 */

@Slf4j(topic = "meta.sync")
@Service
public class MetaSyncServiceImpl implements MetaSyncService {
    private static final SemanticConfig SEMANTIC_CONFIG = SemanticConfig.getInstance();

    @Autowired
    private CubeManager cubeManager;

    @Autowired
    private SegmentManager segmentManager;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectManager projectManager;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public boolean syncCheck() throws SemanticException {
        Execution syncCheckExecution = new Execution("[Meta-sync]  user info check");
        UserInfo userInfo = userService.selectConfUser();
        if (userInfo == null) {
            String user = SemanticUserAndPwd.getUser();
            String decodedPwd = SemanticUserAndPwd.getDecodedPassword();
            log.info("user info not exists in database, please fetch from config");
            if (StringUtils.isBlank(user) || StringUtils.isBlank(decodedPwd)) {
                return false;
            }
            userService.systemAdminCheck(user, decodedPwd);
            userService.updateConfUsr(user, decodedPwd);
            log.info("user info store success from config to database");
        } else {
            log.info("user info exists in database, get it");
            String decodedPassword;
            try {
                decodedPassword = userInfo.getDecryptedPassword();
            } catch (PwdDecryptException e) {
                throw new SemanticException(e);
            }
            userService.systemAdminCheck(userInfo.getUsername(), decodedPassword);
            SEMANTIC_CONFIG.setKylinUser(userInfo.getUsername());
            SEMANTIC_CONFIG.setKylinPwd(decodedPassword);
            log.info("user info store success from database");
        }
        syncCheckExecution.logTimeConsumed(1000, 3000);
        return true;
    }

    @Override
    public void syncProjects() throws SemanticException {
        Execution projectChangeExecution = new Execution("[Meta-sync] project-change job");
        log.info("sync projects has started");
        projectManager.verifyProjectListChange();
        projectChangeExecution.logTimeConsumed(500, 1000);
    }

    @Override
    public void syncDataset() throws SemanticException {
    }

    @Override
    public void syncCube() throws SemanticException {
        Execution cubeChangeExecution = new Execution("[Meta-sync] cube-monitor job");
        log.info("sync cube has started");
        cubeChangeExecution.logTimeConsumed(1000, 3000);
    }

    @Override
    public void syncUser() throws SemanticException {
        Execution syncUserCExecution = new Execution("[Meta-sync] sync user job");
        log.info("sync user has started");

        syncUserCExecution.logTimeConsumed(1000, 3000);
    }

    @Override
    public void syncSegment() throws SemanticException {
        Execution syncSegmentExecution = new Execution("[Meta-sync] sync segment job");
        // TODO: Implement segment cache sync here
//        List<String> datasetProjects = datasetService.getProjectsRelatedDataset();
//        Set<String> allEffectiveProject = projectManager.getAllProject();
//        for (String project : datasetProjects) {
//            if (!allEffectiveProject.contains(project)) {
//                log.warn("Project: [{}] doesn't exist in Kylin, skip its dataset verify.", project);
//                continue;
//            }
//            Set<String> cacheSegment = segmentManager.getSegmentByCache(project);
//            Set<String> noCacheSegment = segmentManager.getSegmentByKylin(project);
//            if (cacheSegment == null) {
//                segmentManager.saveSegment(project, noCacheSegment);
//                continue;
//            }
//            ImmutableSet<String> changeSegment = Sets.symmetricDifference(cacheSegment, noCacheSegment).immutableCopy();
//            if (changeSegment.size() > 0) {
//                clearProjectCache(project);
//                segmentManager.saveSegment(project, noCacheSegment);
//            }
//        }
        syncSegmentExecution.logTimeConsumed(1000, 3000);

    }

    @Override
    public void clearProjectCache(String project) {
        HttpHeaders requestHeaders = new HttpHeaders();
        String userPwd = Utils.buildBasicAuth(SemanticUserAndPwd.getUser(), SemanticUserAndPwd.getDecodedPassword());
        requestHeaders.add("Authorization", userPwd);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
        String url = SemanticConfig.getInstance().getClearCacheUrl(project);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            log.error("Clear {} cache failed, please check the MDX server、config user or password is normal.", project);
            log.error("current response: {}", responseEntity.getBody());
        } else {
            log.info("Clear {} cache success.", project);
        }

    }

    private ImmutableSet<String> compareSet(Set<String> set1, Set<String> set2) {
        return Sets.difference(set1, set2).immutableCopy();
    }

}
