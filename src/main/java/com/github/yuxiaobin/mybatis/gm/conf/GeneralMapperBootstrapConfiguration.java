package com.github.yuxiaobin.mybatis.gm.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.yuxiaobin.mybatis.gm.mapper.GeneralMapperSqlInjector;
import com.github.yuxiaobin.mybatis.gm.processer.MybatisGeneralEntityProcessor;

/**
 * @author Kelly Lake(179634696@qq.com)
 */
@Configuration
public class GeneralMapperBootstrapConfiguration {

    @Bean
    public MybatisGeneralEntityProcessor mybatisEntityProcessor() {
        return new MybatisGeneralEntityProcessor(generalSqlInjector());
    }

    @Bean
    public GeneralMapperSqlInjector generalSqlInjector() {
        return new GeneralMapperSqlInjector();
    }

}
