package com.github.yuxiaobin.mybatis.gm.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.yuxiaobin.mybatis.gm.processer.MybatisGeneralEntityProcessor;

/**
 * @author Kelly Lake(179634696@qq.com)
 */
@Configuration
@Import(DBKeywordsConfig.class)
public class GeneralMapperBootstrapConfiguration {

    @Bean
    public MybatisGeneralEntityProcessor mybatisEntityProcessor() {
        return new MybatisGeneralEntityProcessor();
    }

}
