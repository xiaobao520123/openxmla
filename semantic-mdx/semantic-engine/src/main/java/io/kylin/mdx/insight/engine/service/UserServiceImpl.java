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

import com.github.pagehelper.Page;
import com.google.common.collect.Sets;
import io.kylin.mdx.insight.common.SemanticConfig;
import io.kylin.mdx.insight.common.SemanticConstants;
import io.kylin.mdx.insight.common.SemanticException;
import io.kylin.mdx.insight.common.http.Response;
import io.kylin.mdx.insight.common.util.AESWithECBEncryptor;
import io.kylin.mdx.insight.common.util.Utils;
import io.kylin.mdx.insight.core.dao.UserInfoMapper;
import io.kylin.mdx.insight.core.entity.KylinPermission;
import io.kylin.mdx.insight.core.entity.SyncResult;
import io.kylin.mdx.insight.core.entity.UserInfo;
import io.kylin.mdx.insight.core.service.UserService;
import io.kylin.mdx.insight.core.support.SpringHolder;
import io.kylin.mdx.insight.core.manager.ProjectManager;
import io.kylin.mdx.insight.core.meta.ConnectionInfo;
import io.kylin.mdx.insight.core.meta.SemanticAdapter;
import io.kylin.mdx.insight.core.support.UserOperResult;
import io.kylin.mdx.insight.core.sync.MetaStore;
import io.kylin.mdx.ErrorCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserInfoMapper userInfoMapper;

    private final ProjectManager projectManager;

    @Override
    public void setSemanticAdapter(SemanticAdapter semanticAdapter) {
        this.semanticAdapter = semanticAdapter;
    }

    @Getter
    private SemanticAdapter semanticAdapter = SemanticAdapter.INSTANCE;

    private MetaStore metaStore = MetaStore.getInstance();

    @Autowired
    public UserServiceImpl(UserInfoMapper userInfoMapper,
                           ProjectManager projectManager) {
        this.userInfoMapper = userInfoMapper;
        this.projectManager = projectManager;
    }

    /**
     * 登录 MDX
     * 如果指定了 delegate, 则要求 user 必须拥有管理员权限
     */
    @Override
    public UserOperResult loginForMDX(String user, String password, String project, String delegate) throws SemanticException {
        UserOperResult result = login(Utils.buildBasicAuth(user, password));
        return result;
    }

    @Override
    public boolean checkUserAndPwd(String user, String encryptedPwd) {
        return userInfoMapper.selectByUserAndPwd(user.toUpperCase(), encryptedPwd) != null;
    }

    @Override
    public UserOperResult login(String basicAuth) throws SemanticException {
        //1. find user in database
        ConnectionInfo connectionInfo = new ConnectionInfo(basicAuth);
        String userName = connectionInfo.getUser();
        String actualBasicAuth = StringUtils.substringAfter(basicAuth, "Basic ");

        getSemanticAdapter().authentication(actualBasicAuth);

        UserInfo userInfo = userInfoMapper.selectByUserName(userName.toUpperCase());
        if (userInfo != null) {
            UserInfo updateUser = UserInfo.builder()
                    .id(userInfo.getId())
                    .password(AESWithECBEncryptor.encrypt(connectionInfo.getPassword()))
                    .active(ActiveStatus.ACTIVE.ordinal())
                    .lastLogin(Utils.currentTimeStamp())
                    .loginCount(userInfo.getLoginCount() + 1)
                    .build();
            userInfoMapper.updateByPrimaryKeySelective(updateUser);
        } else {
            try {
                userInfoMapper.insertSelective(new UserInfo(connectionInfo, ActiveStatus.ACTIVE.ordinal(), LicenseAuth.AUTHORIZED.ordinal()));
            } catch (Exception e) {
                log.error("Insert user {} to database failed", connectionInfo.getUser(), e);
            }
        }
        return UserOperResult.LOGIN_SUCCESS;
    }

    @Override
    public boolean hasAdminPermission(ConnectionInfo connInfo) {
        return hasAdminPermission(connInfo, false);
    }

    /**
     * 检查用户管理员权限
     * #    用户属于 ROLE_ADMIN 用户组（即系统管理员）
     * #    具有最少一个项目的管理员权限（仅当 global 为 false）
     *
     * @param global 是否限定系统管理员
     * @return 是否具有管理员权限
     */
    @Override
    public boolean hasAdminPermission(ConnectionInfo connInfo, boolean global) {
        return true;
    }

    private UserOperResult handleSemanticException(SemanticException semanticException) {
        // user locked
        if (ErrorCode.USER_LOCKED.equals(semanticException.getErrorCode())) {
            UserOperResult userOperResult = UserOperResult.USER_LOCKED;
            userOperResult.setMessage(semanticException.getErrorMsg());
            return userOperResult;
        }
        // user disabled
        if (ErrorCode.USER_DISABLE.equals(semanticException.getErrorCode())) {
            return UserOperResult.LOGIN_USER_DISABLED;
        }
        // license expired
        if (ErrorCode.EXPIRED_LICENSE.equals(semanticException.getErrorCode())) {
            return UserOperResult.KYLIN_LOGIN_LICENSE_OUTDATED;
        }
        // invalidate password
        if (ErrorCode.USER_OR_PASSWORD_ERROR.equals(semanticException.getErrorCode())) {
            return UserOperResult.LOGIN_INVALID_USER_PWD;
        }
        throw semanticException;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public UserOperResult changeUserLicense(String username, Integer licenseAuth) {
        userInfoMapper.updateLicenseAuthByUsername(username.toUpperCase(), LicenseAuth.AUTHORIZED.ordinal());
        return UserOperResult.USER_UPDATE_AUTH_SUCCESS;
    }

    @Override
    public List<UserInfo> selectAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public UserInfo selectOne(String username) {
        return userInfoMapper.selectByUserName(username.toUpperCase());
    }

    @Override
    public List<UserInfo> getAllUsers(Integer pageNum, Integer pageSize) {
        return userInfoMapper.selectAllUsersByPage(new RowBounds(pageNum, pageSize));
    }

    @Override
    public UserInfo selectConfUser() {
        return userInfoMapper.selectConfUsr();
    }

    @Override
    public void updateConfUsr(String userName, String password) {
        UserInfoService userInfoService;
        if (!SemanticConfig.getInstance().isPostgre()) {
            userInfoService = SpringHolder.getBean(UserInfoServiceImpl.class);
        } else {
            userInfoService = SpringHolder.getBean(PostgreUserInfoServiceImpl.class);
        }
        userInfoService.updateConfUsr(userName, password, userInfoMapper);
    }

    @Override
    public int updateConfUsr(UserInfo userInfo) {
        return userInfoMapper.updateConfUsr(userInfo);
    }


    @Override
    public Set<String> getUsersNameByDatabase() {
        List<String> users = userInfoMapper.selectAllUsersName();
        return Sets.newHashSet(users);
    }

    @Override
    public void insertUsers(List<UserInfo> userInfos) {
        userInfoMapper.insertUsers(userInfos);
    }

    @Override
    public void deleteUsers(List<String> users) {
        userInfoMapper.deleteUsersByNames(users);
    }

    @Override
    public List<String> getUsersByProjectFromCache(String project) {
        Set<String> users = metaStore.getAllUserOnProject(project);
        return new ArrayList<String>(users);
    }

    @Override
    public List<UserInfo> getAllUserByProject(String project, Integer pageNum, Integer pageSize) {
        List<String> users = getSemanticAdapter().getUsersByProject(project);
        int start = Math.min(pageNum * pageSize, users.size());
        int end = Math.min(start + pageSize, users.size());
        List<String> subList = users.subList(start, end);
        Page<UserInfo> page = new Page<>();
        page.setTotal(users.size());
        page.setPageNum(pageNum + 1);
        page.setPageSize(subList.size());
        for (String username : subList) {
            page.add(new UserInfo(username));
        }
        return page;
    }

    @Override
    public void systemAdminCheck(String user, String password) throws SemanticException {
        String actualBasicAuth = Utils.buildAuthentication(user, password);
        // TODO: /api/user/authentication 可以获得用户的分组信息
        Response authResp = semanticAdapter.authentication(actualBasicAuth);
        if (org.apache.http.HttpStatus.SC_OK != authResp.getHttpStatus()) {
            if (authResp.getContent().contains("User is disabled")) {
                throw new SemanticException(SyncResult.INACTIVE_USER.getMessage(), ErrorCode.INACTIVE_USER);
            }
            if (authResp.getContent().contains("expired")) {
                throw new SemanticException(SyncResult.EXPIRED_LICENSE.getMessage(), ErrorCode.EXPIRED_LICENSE);
            }
            throw new SemanticException(SyncResult.SYNC_NOT_AUTHORIZED.getMessage(), ErrorCode.AUTH_FAILED);
        }
        ConnectionInfo connInfo = ConnectionInfo.builder().user(user).password(password).project(null).build();
        Map<String, String> usrAccess = projectManager.getUserAccessProjects(connInfo);
        if (usrAccess.isEmpty()) {
            List<String> authorities = semanticAdapter.getUserAuthority(connInfo);
            if (authorities.contains(SemanticConstants.ROLE_ADMIN)) {
                return;
            }
        }

        boolean accessFlag = false;
        for (String project : usrAccess.keySet()) {
            if (KylinPermission.GLOBAL_ADMIN.name().equalsIgnoreCase(usrAccess.get(project))) {
                accessFlag = true;
                break;
            }
        }
        if (!accessFlag) {
            throw new SemanticException(ErrorCode.NOT_ADMIN_USER, user);
        }
    }

    @Override
    public int insertSelective(UserInfo record) {
        return userInfoMapper.insertSelective(record);
    }

    public enum LicenseAuth {
        /**
         * user hasn't been authorized
         */
        NOT_AUTHORIZED,

        AUTHORIZED;

        public static LicenseAuth of(int val) {
            if (NOT_AUTHORIZED.ordinal() == val) {
                return NOT_AUTHORIZED;
            } else {
                return AUTHORIZED;
            }
        }
    }

    public enum ActiveStatus {

        /**
         * user has been disabled in KYLIN
         */
        INACTIVE,

        /**
         * user has been enabled in KYLIN
         */
        ACTIVE;

        public static ActiveStatus of(int val) {
            if (INACTIVE.ordinal() == val) {
                return INACTIVE;
            } else {
                return ACTIVE;
            }
        }

    }

}
