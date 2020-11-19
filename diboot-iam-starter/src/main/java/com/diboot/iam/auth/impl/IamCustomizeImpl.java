/*
 * Copyright (c) 2015-2021, www.dibo.ltd (service@dibo.ltd).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.diboot.iam.auth.impl;

import com.diboot.iam.auth.IamCustomize;
import com.diboot.iam.entity.BaseLoginUser;
import com.diboot.iam.entity.IamAccount;
import com.diboot.iam.exception.PermissionException;
import com.diboot.iam.starter.IamBaseProperties;
import com.diboot.core.util.AnnotationUtils;
import com.diboot.iam.util.IamSecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * IAM自定义扩展
 * @author JerryMa
 * @version v2.2
 * @date 2020/11/09
 */
@Service
public class IamCustomizeImpl implements IamCustomize {
    @Autowired
    private IamBaseProperties iamBaseProperties;

    @Override
    public BaseLoginUser getCurrentUser() {
        return IamSecurityUtils.getCurrentUser();
    }

    /***
     * 对用户密码加密
     * @param iamAccount
     */
    @Override
    public void encryptPwd(IamAccount iamAccount) {
        IamSecurityUtils.encryptPwd(iamAccount);
    }

    @Override
    public String encryptPwd(String password, String salt) {
        return IamSecurityUtils.encryptPwd(password, salt);
    }

    @Override
    public void checkPermission(String permissionCode) throws PermissionException {
        try{
            IamSecurityUtils.getSubject().checkPermission(permissionCode);
        }
        catch (Exception e){
            throw new PermissionException(e);
        }
    }

    @Override
    public boolean checkCurrentUserHasRole(String role) {
        return IamSecurityUtils.getSubject().hasRole(role);
    }

    @Override
    public void clearAuthorizationCache(String username) {
        IamSecurityUtils.clearAuthorizationCache(username);
    }

    @Override
    public void clearAllAuthorizationCache() {
        IamSecurityUtils.clearAllAuthorizationCache();
    }

    @Override
    public boolean isEnablePermissionCheck() {
        return iamBaseProperties.isEnablePermissionCheck();
    }

    @Override
    public String[] getOrignPermissionCodes(Method method) {
        RequiresPermissions requiresPermissions = AnnotationUtils.getAnnotation(method, RequiresPermissions.class);
        if(requiresPermissions != null){
            return requiresPermissions.value();
        }
        return null;
    }

}
