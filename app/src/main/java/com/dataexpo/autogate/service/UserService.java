package com.dataexpo.autogate.service;

import com.dataexpo.autogate.comm.DBUtils;
import com.dataexpo.autogate.model.User;

public class UserService {
    private UserService() {};

    private static class HolderClass {
        private static final UserService instance = new UserService();
    }

    /**
     * 单例模式
     */
    public static UserService getInstance() {
        return UserService.HolderClass.instance;
    }

    public boolean checkUser(User user) {
        return DBUtils.getInstance().findByCode(user.code);
    }
}
