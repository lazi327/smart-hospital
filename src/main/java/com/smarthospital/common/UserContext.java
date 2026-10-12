package com.smarthospital.common;

import com.smarthospital.entity.User;

/** 当前登录用户上下文 */
public class UserContext {

    private static final ThreadLocal<User> HOLDER = new ThreadLocal<>();

    public static void set(User user) {
        HOLDER.set(user);
    }

    public static User get() {
        User user = HOLDER.get();
        if (user == null) {
            throw new BizException(401, "请先登录");
        }
        return user;
    }

    public static void requireRole(User.Role... roles) {
        User user = get();
        for (User.Role role : roles) {
            if (user.getRole() == role) {
                return;
            }
        }
        throw new BizException(403, "没有权限执行该操作");
    }

    public static void clear() {
        HOLDER.remove();
    }
}
