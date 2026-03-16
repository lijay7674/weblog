package com.forum.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 网关自定义配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway")
public class WhitelistProperties {
    
    /**
     * 白名单路径列表（无需认证）
     */
    private List<String> whitelist;
}
