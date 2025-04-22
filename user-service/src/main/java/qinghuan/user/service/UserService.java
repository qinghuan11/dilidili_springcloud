package qinghuan.user.service;

import qinghuan.common.Result;
import qinghuan.dao.domain.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param password 密码
     * @return 注册结果
     */
    Result<String> register(String username, String password);

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含 JWT 令牌
     */
    Result<String> login(String username, String password);

    /**
     * 获取用户信息
     *
     * @param token JWT 令牌
     * @return 用户信息
     */
    Result<User> getUserInfo(String token);

    /**
     * 更新用户信息
     *
     * @param username 用户名
     * @param token    JWT 令牌
     * @return 更新结果
     */
    Result<String> updateUser(String username, String token);
}
