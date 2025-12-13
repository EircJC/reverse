package com.yulink.texas.common.admin.service;

public abstract class BaseAdminUserService {

    public abstract String queryRoleIdByUserId(String userId);

    public abstract String queryStatusByUserId(String userId);

}
