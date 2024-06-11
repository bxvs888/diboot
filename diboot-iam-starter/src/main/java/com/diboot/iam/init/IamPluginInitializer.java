/*
 * Copyright (c) 2015-2020, www.dibo.ltd (service@dibo.ltd).
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
package com.diboot.iam.init;

import com.diboot.core.config.GlobalProperties;
import com.diboot.core.entity.Dictionary;
import com.diboot.core.exception.BusinessException;
import com.diboot.core.service.DictionaryService;
import com.diboot.core.util.ContextHolder;
import com.diboot.core.util.JSON;
import com.diboot.core.util.SqlFileInitializer;
import com.diboot.core.vo.DictionaryVO;
import com.diboot.iam.config.Cons;
import com.diboot.iam.entity.*;
import com.diboot.iam.service.*;
import com.diboot.iam.vo.IamResourceListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * IAM组件相关的初始化
 * @author mazc@dibo.ltd
 * @version v2.0
 * @date 2019/12/23
 */
@Order(912)
@Slf4j
@Component
@ConditionalOnProperty(prefix = "diboot", name = "init-sql", havingValue = "true")
public class IamPluginInitializer implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 检查数据库表是否已存在
        String initDetectSql = "SELECT id FROM dbt_iam_role WHERE id='0'";
        if(!SqlFileInitializer.checkSqlExecutable(initDetectSql)){
            log.info("diboot-IAM 初始化SQL ...");
            // 执行初始化SQL
            SqlFileInitializer.initBootstrapSql(this.getClass(), "iam");
            // 插入相关数据：Dict，Role等
            insertInitData();
            log.info("diboot-IAM 初始化SQL完成.");
        }
    }

    /**
     * 插入初始化数据
     */
    private synchronized void insertInitData(){
        // 插入iam组件所需的数据字典
        DictionaryService dictionaryService = ContextHolder.getBean(DictionaryService.class);
        if(dictionaryService != null && !dictionaryService.exists(Dictionary::getType, "AUTH_TYPE")){
            String[] DICT_INIT_DATA = {
                    "{\"type\":\"AUTH_TYPE\", \"itemName\":\"登录认证方式\", \"itemNameI18n\":\"Authentication Type\", \"description\":\"IAM用户登录认证方式\", \"children\":[{\"itemName\":\"用户名密码\", \"itemNameI18n\":\"Username & Password\", \"itemValue\":\"PWD\", \"sortId\":1},{\"itemName\":\"单点登录\", \"itemNameI18n\":\"Single Sign-On\", \"itemValue\":\"SSO\", \"sortId\":2},{\"itemName\":\"公众号\", \"itemNameI18n\":\"WeChat\", \"itemValue\":\"WX_MP\", \"sortId\":3},{\"itemName\":\"企业微信\", \"itemNameI18n\":\"Enterprise WeChat\", \"itemValue\":\"WX_CP\", \"sortId\":4},{\"itemName\":\"其他\", \"itemNameI18n\":\"Other\", \"itemValue\":\"OTHER\", \"sortId\":5}]}",
                    "{\"type\":\"ACCOUNT_STATUS\",\"itemName\":\"账号状态\",\"itemNameI18n\":\"Account Status\", \"description\":\"IAM登录账号状态\",\"children\":[{\"itemName\":\"有效\",\"itemNameI18n\":\"Active\", \"itemValue\":\"A\",\"extension\":{\"color\":\"#2ECC71\"},\"sortId\":1},{\"itemName\":\"无效\",\"itemNameI18n\":\"Inactive\", \"itemValue\":\"I\",\"sortId\":2},{\"itemName\":\"锁定\",\"itemNameI18n\":\"Locked\", \"itemValue\":\"L\",\"extension\":{\"color\":\"#FF6F00\"},\"sortId\":3}]}",
                    "{\"type\":\"USER_STATUS\",\"itemName\":\"用户状态\",\"itemNameI18n\":\"User Status\", \"description\":\"IAM用户状态\",\"isEditable\":true,\"children\":[{\"itemName\":\"在职\",\"itemNameI18n\":\"Employed\", \"itemValue\":\"A\",\"extension\":{\"color\":\"#2ECC71\"},\"sortId\":1},{\"itemName\":\"离职\",\"itemNameI18n\":\"Left\", \"itemValue\":\"I\",\"sortId\":2}]}",
                    "{\"itemName\":\"用户性别\",\"itemNameI18n\":\"Gender\", \"type\":\"GENDER\",\"description\":\"用户性别数据字典\",\"children\":[{\"itemValue\":\"F\",\"itemName\":\"女\",\"itemNameI18n\":\"Female\",\"extension\":{\"color\":\"#FD8BB8\"}},{\"itemValue\":\"M\",\"itemName\":\"男\",\"itemNameI18n\":\"Male\",\"extension\":{\"color\":\"#55B0EE\"}}]}",
                    "{\"type\":\"RESOURCE_TYPE\", \"itemName\":\"资源类型\", \"itemNameI18n\":\"Resource Type\", \"description\":\"IAM资源类型\", \"children\":[{\"itemName\":\"菜单\", \"itemNameI18n\":\"Menu\", \"itemValue\":\"MENU\", \"sortId\":1},{\"itemName\":\"按钮/操作\", \"itemNameI18n\":\"Button/Operation\", \"itemValue\":\"OPERATION\", \"sortId\":2}]}",
                    "{\"itemName\":\"前端按钮/权限编码\",\"itemNameI18n\":\"Frontend Button/Permission Code\", \"type\":\"RESOURCE_CODE\",\"description\":\"前端按钮/权限编码 常用选项\",\"children\":[{\"sortId\":1,\"itemName\":\"详情\",\"itemNameI18n\":\"Detail\", \"itemValue\":\"detail\"},{\"sortId\":2,\"itemName\":\"新建\",\"itemNameI18n\":\"Create\", \"itemValue\":\"create\"},{\"sortId\":3,\"itemName\":\"更新\",\"itemNameI18n\":\"Update\", \"itemValue\":\"update\"},{\"sortId\":4,\"itemName\":\"删除\",\"itemNameI18n\":\"Delete\", \"itemValue\":\"delete\"},{\"sortId\":5,\"itemName\":\"导出\",\"itemNameI18n\":\"Export\", \"itemValue\":\"export\"},{\"sortId\":6,\"itemName\":\"导入\",\"itemNameI18n\":\"Import\", \"itemValue\":\"import\"}]}",
                    "{\"type\":\"ORG_TYPE\", \"itemName\":\"组织类型\", \"itemNameI18n\":\"Organization Type\", \"description\":\"组织节点类型\", \"isEditable\":false, \"children\":[{\"itemName\":\"部门\", \"itemNameI18n\":\"Department\", \"itemValue\":\"DEPT\", \"sortId\":1},{\"itemName\":\"公司\", \"itemNameI18n\":\"Company\", \"itemValue\":\"COMP\", \"sortId\":2}]}",
                    "{\"type\":\"DATA_PERMISSION_TYPE\", \"itemName\":\"IAM数据权限类型\", \"itemNameI18n\":\"IAM Data Permission Type\", \"description\":\"IAM数据权限类型定义\", \"isEditable\":true, \"children\":[{\"itemName\":\"本人\", \"itemNameI18n\":\"Self\", \"itemValue\":\"SELF\", \"sortId\":1},{\"itemName\":\"本人及下属\", \"itemNameI18n\":\"Self and Subordinates\", \"itemValue\":\"SELF_AND_SUB\", \"sortId\":2},{\"itemName\":\"本部门\", \"itemNameI18n\":\"Current Department\", \"itemValue\":\"DEPT\", \"sortId\":3},{\"itemName\":\"本部门及下属部门\", \"itemNameI18n\":\"Current Department and Sub-departments\", \"itemValue\":\"DEPT_AND_SUB\", \"sortId\":4},{\"itemName\":\"全部\", \"itemNameI18n\":\"All\", \"itemValue\":\"ALL\", \"sortId\":5}]}",
                    "{\"type\":\"POSITION_GRADE\", \"itemName\":\"职级定义\", \"itemNameI18n\":\"Position Grade Definition\", \"description\":\"职务级别定义\", \"isEditable\":true, \"children\":[{\"itemName\":\"初级\", \"itemNameI18n\":\"E1\", \"itemValue\":\"E1\", \"sortId\":1},{\"itemName\":\"中级\", \"itemNameI18n\":\"E2\", \"itemValue\":\"E2\", \"sortId\":2},{\"itemName\":\"高级\", \"itemNameI18n\":\"E3\", \"itemValue\":\"E3\", \"sortId\":3},{\"itemName\":\"专家\", \"itemNameI18n\":\"E4\", \"itemValue\":\"E4\", \"sortId\":4}]}"
            };
            // 插入数据字典
            for(String dictJson : DICT_INIT_DATA){
                DictionaryVO dictVo = JSON.toJavaObject(dictJson, DictionaryVO.class);
                dictVo.setParentId(Cons.ID_PREVENT_NULL);
                dictionaryService.createDictAndChildren(dictVo);
            }
        }

        // 插入iam组件所需的初始权限数据
        IamResourceService resourcePermissionService = ContextHolder.getBean(IamResourceService.class);
        if(resourcePermissionService != null && !resourcePermissionService.exists(IamResource::getResourceCode, "system")){
            GlobalProperties globalProperties = ContextHolder.getBean(GlobalProperties.class);
            String systemConfigJson = (globalProperties == null || !globalProperties.isI18n()) ?
                    "{\"displayType\":\"CATALOGUE\",\"displayName\":\"系统管理\",\"displayNameI18n\":\"System\",\"routePath\":\"system\",\"resourceCode\":\"System\",\"meta\":\"{\\\"icon\\\":\\\"Element:Tools\\\"}\",\"sortId\":\"95\",\"children\":[{\"displayType\":\"MENU\",\"displayName\":\"数据字典\",\"displayNameI18n\":\"Dictionary\",\"routePath\":\"dictionary\",\"resourceCode\":\"Dictionary\",\"permissionCode\":\"Dictionary:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:Collection\\\",\\\"componentPath\\\":\\\"@/views/system/dictionary/List.vue\\\"}\",\"sortId\":\"10\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"Dictionary:read\",\"sortId\":\"6\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"Dictionary:write\",\"sortId\":\"5\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"Dictionary:write,Dictionary:read\",\"sortId\":\"4\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"Dictionary:write\",\"sortId\":\"3\"}]},{\"displayType\":\"MENU\",\"displayName\":\"菜单资源\",\"displayNameI18n\":\"Resource\",\"routePath\":\"resource\",\"resourceCode\":\"Resource\",\"permissionCode\":\"IamResource:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:Menu\\\",\\\"componentPath\\\":\\\"@/views/system/resource/index.vue\\\"}\",\"sortId\":\"20\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"IamResource:read\",\"sortId\":\"23\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"IamResource:write\",\"sortId\":\"21\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"IamResource:write,IamResource:read\",\"sortId\":\"20\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"IamResource:write\",\"sortId\":\"19\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"排序\",\"displayNameI18n\":\"Sort\",\"resourceCode\":\"sort\",\"permissionCode\":\"IamResource:write\",\"sortId\":\"18\"}]},{\"displayType\":\"MENU\",\"displayName\":\"角色权限\",\"displayNameI18n\":\"Role\",\"routePath\":\"role\",\"resourceCode\":\"Role\",\"permissionCode\":\"IamRole:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:Avatar\\\",\\\"componentPath\\\":\\\"@/views/system/role/List.vue\\\"}\",\"sortId\":\"30\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"IamRole:read\",\"sortId\":\"16\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"IamRole:write\",\"sortId\":\"15\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"IamRole:write,IamRole:read,IamResource:read\",\"sortId\":\"14\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"IamRole:write\",\"sortId\":\"13\"}]},{\"displayType\":\"MENU\",\"displayName\":\"定时任务\",\"displayNameI18n\":\"Scheduled Job\",\"routePath\":\"schedule-job\",\"resourceCode\":\"ScheduleJob\",\"permissionCode\":\"ScheduleJob:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:AlarmClock\\\",\\\"componentPath\\\":\\\"@/views/system/schedule-job/List.vue\\\"}\",\"sortId\":\"40\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"ScheduleJob:write\",\"sortId\":\"7\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"ScheduleJob:write,ScheduleJob:read\",\"sortId\":\"6\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"ScheduleJob:write\",\"sortId\":\"5\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"ScheduleJob:read\",\"sortId\":\"4\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"运行一次\",\"displayNameI18n\":\"Run Once\",\"resourceCode\":\"executeOnce\",\"permissionCode\":\"ScheduleJob:write\",\"sortId\":\"3\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"日志记录\",\"displayNameI18n\":\"Log Records\",\"resourceCode\":\"logList\",\"permissionCode\":\"ScheduleJob:read\",\"sortId\":\"2\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"日志删除\",\"displayNameI18n\":\"Log Delete\",\"resourceCode\":\"logDelete\",\"permissionCode\":\"ScheduleJob:write\",\"sortId\":\"1\"}]},{\"displayType\":\"MENU\",\"displayName\":\"消息模板\",\"displayNameI18n\":\"Message Template\",\"routePath\":\"message-template\",\"resourceCode\":\"MessageTemplate\",\"permissionCode\":\"MessageTemplate:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:ChatLineSquare\\\",\\\"componentPath\\\":\\\"@/views/system/message-template/List.vue\\\"}\",\"sortId\":\"50\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"MessageTemplate:read\",\"sortId\":\"16\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"MessageTemplate:write\",\"sortId\":\"15\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"MessageTemplate:write,MessageTemplate:read\",\"sortId\":\"14\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"MessageTemplate:write\",\"sortId\":\"13\"}]},{\"displayType\":\"MENU\",\"displayName\":\"消息记录\",\"displayNameI18n\":\"Message Record\",\"routePath\":\"message\",\"resourceCode\":\"Message\",\"permissionCode\":\"Message:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:ChatDotRound\\\",\\\"componentPath\\\":\\\"@/views/system/message/List.vue\\\"}\",\"sortId\":\"60\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"Message:read\",\"sortId\":\"16\"}]},{\"displayType\":\"MENU\",\"displayName\":\"文件记录\",\"displayNameI18n\":\"File Record\",\"routePath\":\"file-record\",\"resourceCode\":\"FileRecord\",\"permissionCode\":\"FileRecord:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:FolderOpened\\\",\\\"componentPath\\\":\\\"@/views/system/file-record/List.vue\\\"}\",\"sortId\":\"70\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"FileRecord:read\",\"sortId\":\"16\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"FileRecord:write,FileRecord:read\",\"sortId\":\"14\"}]},{\"displayType\":\"MENU\",\"displayName\":\"系统配置\",\"displayNameI18n\":\"Configuration\",\"routePath\":\"config\",\"resourceCode\":\"SystemConfig\",\"permissionCode\":\"SystemConfig:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:Setting\\\",\\\"componentPath\\\":\\\"@/views/system/config/index.vue\\\"}\",\"sortId\":\"80\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"SystemConfig:write\",\"sortId\":\"1\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"SystemConfig:write\",\"sortId\":\"2\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"SystemConfig:write\",\"sortId\":\"3\"}]},{\"displayType\":\"MENU\",\"displayName\":\"操作日志\",\"displayNameI18n\":\"Operation Log\",\"routePath\":\"operation-log\",\"resourceCode\":\"OperationLog\",\"permissionCode\":\"IamOperationLog:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:Pointer\\\",\\\"componentPath\\\":\\\"@/views/system/operation-log/List.vue\\\"}\",\"sortId\":\"90\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"IamOperationLog:read\",\"sortId\":\"1\"}]},{\"displayType\":\"MENU\",\"displayName\":\"登录日志\",\"displayNameI18n\":\"Login Log\",\"routePath\":\"login-trace\",\"resourceCode\":\"LoginTrace\",\"permissionCode\":\"IamLoginTrace:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:Finished\\\",\\\"componentPath\\\":\\\"@/views/system/login-trace/List.vue\\\"}\",\"sortId\":\"100\",\"children\":[]}]}"
                :
                    "{\"displayType\":\"CATALOGUE\",\"displayName\":\"系统管理\",\"displayNameI18n\":\"System\",\"routePath\":\"system\",\"resourceCode\":\"System\",\"meta\":\"{\\\"icon\\\":\\\"Element:Tools\\\"}\",\"sortId\":\"95\",\"children\":[{\"displayType\":\"MENU\",\"displayName\":\"数据字典\",\"displayNameI18n\":\"Dictionary\",\"routePath\":\"dictionary\",\"resourceCode\":\"Dictionary\",\"permissionCode\":\"Dictionary:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:Collection\\\",\\\"componentPath\\\":\\\"@/views/system/dictionary/List.vue\\\"}\",\"sortId\":\"10\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"Dictionary:read\",\"sortId\":\"6\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"Dictionary:write\",\"sortId\":\"5\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"Dictionary:write,Dictionary:read\",\"sortId\":\"4\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"Dictionary:write\",\"sortId\":\"3\"}]},{\"displayType\":\"MENU\",\"displayName\":\"菜单资源\",\"displayNameI18n\":\"Resource\",\"routePath\":\"resource\",\"resourceCode\":\"Resource\",\"permissionCode\":\"IamResource:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:Menu\\\",\\\"componentPath\\\":\\\"@/views/system/resource/index.vue\\\"}\",\"sortId\":\"20\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"IamResource:read\",\"sortId\":\"23\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"IamResource:write\",\"sortId\":\"21\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"IamResource:write,IamResource:read\",\"sortId\":\"20\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"IamResource:write\",\"sortId\":\"19\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"排序\",\"displayNameI18n\":\"Sort\",\"resourceCode\":\"sort\",\"permissionCode\":\"IamResource:write\",\"sortId\":\"18\"}]},{\"displayType\":\"MENU\",\"displayName\":\"角色权限\",\"displayNameI18n\":\"Role\",\"routePath\":\"role\",\"resourceCode\":\"Role\",\"permissionCode\":\"IamRole:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:Avatar\\\",\\\"componentPath\\\":\\\"@/views/system/role/List.vue\\\"}\",\"sortId\":\"30\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"IamRole:read\",\"sortId\":\"16\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"IamRole:write\",\"sortId\":\"15\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"IamRole:write,IamRole:read,IamResource:read\",\"sortId\":\"14\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"IamRole:write\",\"sortId\":\"13\"}]},{\"displayType\":\"MENU\",\"displayName\":\"定时任务\",\"displayNameI18n\":\"Scheduled Job\",\"routePath\":\"schedule-job\",\"resourceCode\":\"ScheduleJob\",\"permissionCode\":\"ScheduleJob:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:AlarmClock\\\",\\\"componentPath\\\":\\\"@/views/system/schedule-job/List.vue\\\"}\",\"sortId\":\"40\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"ScheduleJob:write\",\"sortId\":\"7\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"ScheduleJob:write,ScheduleJob:read\",\"sortId\":\"6\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"ScheduleJob:write\",\"sortId\":\"5\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"ScheduleJob:read\",\"sortId\":\"4\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"运行一次\",\"displayNameI18n\":\"Run Once\",\"resourceCode\":\"executeOnce\",\"permissionCode\":\"ScheduleJob:write\",\"sortId\":\"3\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"日志记录\",\"displayNameI18n\":\"Log Records\",\"resourceCode\":\"logList\",\"permissionCode\":\"ScheduleJob:read\",\"sortId\":\"2\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"日志删除\",\"displayNameI18n\":\"Log Delete\",\"resourceCode\":\"logDelete\",\"permissionCode\":\"ScheduleJob:write\",\"sortId\":\"1\"}]},{\"displayType\":\"MENU\",\"displayName\":\"消息模板\",\"displayNameI18n\":\"Message Templates\",\"routePath\":\"message-template\",\"resourceCode\":\"MessageTemplate\",\"permissionCode\":\"MessageTemplate:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:ChatLineSquare\\\",\\\"componentPath\\\":\\\"@/views/system/message-template/List.vue\\\"}\",\"sortId\":\"50\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"MessageTemplate:read\",\"sortId\":\"16\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"MessageTemplate:write\",\"sortId\":\"15\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"MessageTemplate:write,MessageTemplate:read\",\"sortId\":\"14\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"MessageTemplate:write\",\"sortId\":\"13\"}]},{\"displayType\":\"MENU\",\"displayName\":\"消息记录\",\"displayNameI18n\":\"Message\",\"routePath\":\"message\",\"resourceCode\":\"Message\",\"permissionCode\":\"Message:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:ChatDotRound\\\",\\\"componentPath\\\":\\\"@/views/system/message/List.vue\\\"}\",\"sortId\":\"60\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"Message:read\",\"sortId\":\"16\"}]},{\"displayType\":\"MENU\",\"displayName\":\"文件记录\",\"displayNameI18n\":\"File Record\",\"routePath\":\"file-record\",\"resourceCode\":\"FileRecord\",\"permissionCode\":\"FileRecord:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:FolderOpened\\\",\\\"componentPath\\\":\\\"@/views/system/file-record/List.vue\\\"}\",\"sortId\":\"70\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"FileRecord:read\",\"sortId\":\"16\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"FileRecord:write,FileRecord:read\",\"sortId\":\"14\"}]},{\"displayType\":\"MENU\",\"displayName\":\"国际化管理\",\"displayNameI18n\":\"I18n\",\"routePath\":\"i18n-config\",\"resourceCode\":\"I18nConfig\",\"permissionCode\":\"\",\"meta\":\"{\\\"icon\\\":\\\"Local:Language\\\",\\\"componentPath\\\":\\\"@/views/system/i18n-config/List.vue\\\"}\",\"sortId\":\"85\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"I18nConfig:write\",\"sortId\":\"1\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"I18nConfig:write\",\"sortId\":\"2\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"I18nConfig:write\",\"sortId\":\"3\"}]},{\"displayType\":\"MENU\",\"displayName\":\"系统配置\",\"displayNameI18n\":\"Configuration\",\"routePath\":\"config\",\"resourceCode\":\"SystemConfig\",\"permissionCode\":\"SystemConfig:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:Setting\\\",\\\"componentPath\\\":\\\"@/views/system/config/index.vue\\\"}\",\"sortId\":\"80\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"SystemConfig:write\",\"sortId\":\"1\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"SystemConfig:write\",\"sortId\":\"2\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"SystemConfig:write\",\"sortId\":\"3\"}]},{\"displayType\":\"MENU\",\"displayName\":\"操作日志\",\"displayNameI18n\":\"Operation Log\",\"routePath\":\"operation-log\",\"resourceCode\":\"OperationLog\",\"permissionCode\":\"IamOperationLog:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:Pointer\\\",\\\"componentPath\\\":\\\"@/views/system/operation-log/List.vue\\\"}\",\"sortId\":\"90\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"IamOperationLog:read\",\"sortId\":\"1\"}]},{\"displayType\":\"MENU\",\"displayName\":\"登录日志\",\"displayNameI18n\":\"Login Log\",\"routePath\":\"login-trace\",\"resourceCode\":\"LoginTrace\",\"permissionCode\":\"IamLoginTrace:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:Finished\\\",\\\"componentPath\\\":\\\"@/views/system/login-trace/List.vue\\\"}\",\"sortId\":\"100\",\"children\":[]}]}"
            ;
            String[] RESOURCE_PERMISSION_DATA = {
                    "{\"displayType\":\"CATALOGUE\",\"displayName\":\"组织人员\",\"displayNameI18n\":\"Organization\",\"routePath\":\"org-structure\",\"resourceCode\":\"OrgStructure\",\"meta\":\"{\\\"icon\\\":\\\"Element:UserFilled\\\"}\",\"sortId\":\"90\",\"children\":[{\"displayType\":\"MENU\",\"displayName\":\"人员管理\",\"displayNameI18n\":\"User\",\"routePath\":\"user\",\"resourceCode\":\"User\",\"permissionCode\":\"IamOrg:read,IamUser:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:User\\\",\\\"componentPath\\\":\\\"@/views/org-structure/user/index.vue\\\"}\",\"sortId\":\"1\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"IamUser:write\",\"sortId\":\"40\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"IamUser:write,IamUser:read\",\"sortId\":\"39\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"IamUser:write\",\"sortId\":\"38\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"IamUser:read\",\"sortId\":\"37\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"导入\",\"displayNameI18n\":\"Import\",\"resourceCode\":\"import\",\"permissionCode\":\"IamUserExcel:import\",\"sortId\":\"36\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"导出\",\"displayNameI18n\":\"Export\",\"resourceCode\":\"export\",\"permissionCode\":\"IamUserExcel:export\",\"sortId\":\"35\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"人员岗位设置\",\"displayNameI18n\":\"Position Settings\",\"resourceCode\":\"position\",\"permissionCode\":\"IamPosition:write,IamPosition:read\",\"sortId\":\"34\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"添加岗位\",\"displayNameI18n\":\"Add Position\",\"resourceCode\":\"addPosition\",\"permissionCode\":\"IamPosition:write,IamPosition:read\",\"sortId\":\"33\"}]},{\"displayType\":\"MENU\",\"displayName\":\"组织部门\",\"displayNameI18n\":\"Department\",\"routePath\":\"org\",\"resourceCode\":\"Org\",\"permissionCode\":\"IamOrg:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:Folder\\\",\\\"componentPath\\\":\\\"@/views/org-structure/org/index.vue\\\"}\",\"sortId\":\"2\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"排序\",\"displayNameI18n\":\"Sort\",\"resourceCode\":\"sort\",\"permissionCode\":\"IamOrg:write\",\"sortId\":\"106\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"IamOrg:write\",\"sortId\":\"105\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"IamOrg:write,IamOrg:read\",\"sortId\":\"104\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"IamOrg:write\",\"sortId\":\"103\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"IamOrg:read\",\"sortId\":\"102\"}]},{\"displayType\":\"MENU\",\"displayName\":\"岗位管理\",\"displayNameI18n\":\"Position\",\"routePath\":\"position\",\"resourceCode\":\"Position\",\"permissionCode\":\"IamPosition:read\",\"meta\":\"{\\\"icon\\\":\\\"Element:Postcard\\\",\\\"componentPath\\\":\\\"@/views/org-structure/position/List.vue\\\"}\",\"sortId\":\"3\",\"children\":[{\"displayType\":\"PERMISSION\",\"displayName\":\"删除\",\"displayNameI18n\":\"Delete\",\"resourceCode\":\"delete\",\"permissionCode\":\"IamPosition:write\",\"sortId\":\"112\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"详情\",\"displayNameI18n\":\"Detail\",\"resourceCode\":\"detail\",\"permissionCode\":\"IamPosition:read\",\"sortId\":\"111\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"更新\",\"displayNameI18n\":\"Update\",\"resourceCode\":\"update\",\"permissionCode\":\"IamPosition:write,IamPosition:read\",\"sortId\":\"110\"},{\"displayType\":\"PERMISSION\",\"displayName\":\"新建\",\"displayNameI18n\":\"Create\",\"resourceCode\":\"create\",\"permissionCode\":\"IamPosition:write\",\"sortId\":\"108\"}]}]}",
                    systemConfigJson
            };
            // 插入多层级资源权限初始数据
            try {
                for (String resourcePermissionJson : RESOURCE_PERMISSION_DATA) {
                    IamResourceListVO permissionListVO = JSON.toJavaObject(resourcePermissionJson, IamResourceListVO.class);
                    resourcePermissionService.deepCreateResourceAndChildren(permissionListVO);
                }
            } catch (BusinessException e){
                log.error("初始化资源权限数据出错: {}，请手动配置前端资源初始的权限数据", e.getMessage());
            }
        }

        // 插入公司根节点
        IamOrgService iamOrgService = ContextHolder.getBean(IamOrgService.class);
        IamOrg iamOrg = new IamOrg();
        if(iamOrgService != null && iamOrgService.getEntityListCount(null) == 0){
            iamOrg.setParentId(Cons.ID_PREVENT_NULL);
            iamOrg.setCode("ROOT").setRootOrgId("1").setName("我的公司")
                    .setType(Cons.DICTCODE_ORG_TYPE.COMP.name()).setOrgComment("初始根节点，请按需修改").setId("1");
            iamOrgService.createEntity(iamOrg);
        }

        // 插入超级管理员用户及角色
        IamRoleService iamRoleService = ContextHolder.getBean(IamRoleService.class);
        if(iamRoleService != null && iamRoleService.getEntityListCount(null) == 0){
            IamRole iamRole = new IamRole();
            iamRole.setName("超级管理员").setCode(Cons.ROLE_SUPER_ADMIN);
            iamRoleService.createEntity(iamRole);

            IamUser iamUser = new IamUser();
            iamUser.setOrgId(iamOrg.getId()).setRealname("DIBOOT").setUserNum("0000").setGender("M").setMobilePhone("10000000000");
            ContextHolder.getBean(IamUserService.class).createEntity(iamUser);

            // 插入对象
            IamUserRole iamUserRole = new IamUserRole(IamUser.class.getSimpleName(), iamUser.getId(), iamRole.getId());
            ContextHolder.getBean(IamUserRoleService.class).getMapper().insert(iamUserRole);

            // 创建账号
            IamAccount iamAccount = new IamAccount();
            iamAccount.setUserType(IamUser.class.getSimpleName()).setUserId(iamUser.getId())
                    .setAuthType(Cons.DICTCODE_AUTH_TYPE.PWD.name())
                    .setAuthAccount("admin").setAuthSecret("123456");
            ContextHolder.getBean(IamAccountService.class).createEntity(iamAccount);
        }

    }
}
