package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    //微信登录的接口
    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    @Override
    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    public User wxLogin(UserLoginDTO userLoginDTO) {
        //调用微信接口获取用户信息（openid）
        String openId = getOpenId(userLoginDTO.getCode());
        //判断openid是否为空，如果空就是登录失败，抛出异常
        if (openId == null) {
            throw new RuntimeException(MessageConstant.LOGIN_FAILED);
        }
        //判断当前用户是否为新用户，如果是新用户，自动完成注册，把openid存到数据库
        User user = userMapper.getByOpenId(openId);
        if (user == null){
            user = User.builder()
                    .openid(openId)
                    .createTime(LocalDateTime.now())
                    .build();

            userMapper.insert(user);
        }
        //返回用户对象
        return user;
    }

    private String getOpenId(String code) {
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN_URL, map);
        JSONObject jsonObject = JSON.parseObject(json);
        String openId = jsonObject.getString("openid");
        return openId;
     }
}
