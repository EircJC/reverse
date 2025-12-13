package com.yulink.texas.common.admin.service;

import java.util.List;

public abstract class BaseSysRightService {

    public abstract List<String> queryRightIdListByRoleId(String roleId);

}
