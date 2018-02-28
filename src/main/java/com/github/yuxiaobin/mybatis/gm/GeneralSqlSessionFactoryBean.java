/**
 * Copyright (c) 2016-2017, Kelly Lake (179634696@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.yuxiaobin.mybatis.gm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.ClassUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.MybatisConfiguration;
import com.baomidou.mybatisplus.exceptions.MybatisPlusException;
import com.github.yuxiaobin.mybatis.gm.conf.DBKeywordsConfig;
import com.github.yuxiaobin.mybatis.gm.mapper.GeneralMapperSqlInjector;
import com.github.yuxiaobin.mybatis.gm.processer.MybatisGeneralEntityProcessor;
import com.github.yuxiaobin.mybatis.gm.utils.GeneralJdbcReflectionUtil;

/**
 * <p>暴露get方法，用来解析通配符的包名(com.github.yuxiaobin.*.persistent)</p>
 * <p>Expose get method for {@code typeAliasesPackage}, so that can parse package path with *(wildcard support: com.github.yuxiaobin.*.persistent)</p>
 * <p>
 * 初始化sessionFactory的时候，使用该类型.
 * When init sessionFactory, should use this class{@code GeneralSqlSessionFactoryBean}.
 *
 * @author Kelly Lake (179634696@qq.com).
 * @since 1.8.7
 */
public class GeneralSqlSessionFactoryBean extends SqlSessionFactoryBean {

    private static final Log LOGGER = LogFactory.getLog(GeneralSqlSessionFactoryBean.class);

    private String typeAliasesPackage;
    private Configuration configuration;
    private DataSource dataSource;
    private DBKeywordsConfig.DBKeywords dbKeywords;
    private boolean injectFlag = false;

    public GeneralSqlSessionFactoryBean setDbKeywords(DBKeywordsConfig.DBKeywords dbKeywords) {
        this.dbKeywords = dbKeywords;
        return this;
    }

    @Override
    public void setTypeAliasesPackage(String typeAliasesPackage) {
        super.setTypeAliasesPackage(typeAliasesPackage);
        this.typeAliasesPackage = typeAliasesPackage;
        if (configuration != null && configuration instanceof GeneralConfiguration) {
            ((GeneralConfiguration) configuration).setTypeAliasesPackage(typeAliasesPackage);
        }
    }

    public String getTypeAliasesPackage() {
        return typeAliasesPackage;
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        this.configuration = configuration;
        if (typeAliasesPackage != null && configuration instanceof GeneralConfiguration) {
            ((GeneralConfiguration) configuration).setTypeAliasesPackage(typeAliasesPackage);
        }
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        super.setDataSource(dataSource);
        this.dataSource = dataSource;
    }

    @Override
    public SqlSessionFactory getObject() throws Exception {
        if (!injectFlag) {
            injectSql(initSqlInjector());
            injectFlag = true;
        }
        return super.getObject();
    }

    private GeneralMapperSqlInjector initSqlInjector() {
        GeneralMapperSqlInjector sqlInjector = new GeneralMapperSqlInjector();
        try (Connection conn = dataSource.getConnection();) {
            GeneralJdbcReflectionUtil.DBType dbType = GeneralJdbcReflectionUtil.getDbType(conn.getMetaData().getURL());
            if (dbType == null) {
                throw new MybatisPlusException("unsupported jdbc url");
            }
            switch (dbType) {
                case MYSQL:
                    sqlInjector.setDBType(com.baomidou.mybatisplus.mapper.DBType.MYSQL);
                    MybatisConfiguration.DB_TYPE = com.baomidou.mybatisplus.mapper.DBType.MYSQL;
                    break;
                case ORACLE:
                    sqlInjector.setDBType(com.baomidou.mybatisplus.mapper.DBType.ORACLE);
                    MybatisConfiguration.DB_TYPE = com.baomidou.mybatisplus.mapper.DBType.ORACLE;
                    break;
                default:
                    sqlInjector.setDBType(com.baomidou.mybatisplus.mapper.DBType.MYSQL);
                    MybatisConfiguration.DB_TYPE = com.baomidou.mybatisplus.mapper.DBType.MYSQL;
                    break;
            }
        } catch (SQLException e) {
            throw new MybatisPlusException(e);
        }
        if (dbKeywords != null) {
            sqlInjector.addKeyWords(dbKeywords.getKeywords());
        }
        return sqlInjector;
    }

    private void injectSql(GeneralMapperSqlInjector generalSqlInjector) {
        if (configuration instanceof GeneralConfiguration) {
            typeAliasesPackage = ((GeneralConfiguration) configuration).getTypeAliasesPackage();
        }
        if (StringUtils.hasLength(typeAliasesPackage)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("injectSql(): typeAliasesPackage=" + typeAliasesPackage);
            }
            String[] typeAliasPackageArray = MybatisGeneralEntityProcessor.parseTypeAliasPackage(this.typeAliasesPackage);
            if (typeAliasPackageArray != null) {
                MybatisGeneralEntityProcessor.typeAliasPackageArray = typeAliasPackageArray;
                for (String packageToScan : typeAliasPackageArray) {
                    configuration.getTypeAliasRegistry().registerAliases(packageToScan, Object.class);
                }
            }
        }
        for (Map.Entry<String, Class<?>> type : configuration.getTypeAliasRegistry().getTypeAliases().entrySet()) {
            if (checkValidateClassTypes(type.getValue())) {
                MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, type.getValue().getPackage().getName());
                assistant.setCurrentNamespace(MybatisGeneralEntityProcessor.generateNamespace(type.getValue()));
                generalSqlInjector.inject(configuration, assistant, GeneralMapper.class, type.getValue(), null);
            }
        }
    }


    private boolean checkValidateClassTypes(Class<?> entityClazz) {
        return !ClassUtils.isPrimitiveOrWrapper(entityClazz)
                && !entityClazz.isArray()
                && !entityClazz.isInterface()
                && !Object.class.equals(entityClazz)
                && checkBeanType(entityClazz)
                ;
    }

    /**
     * Preprocess for class filtering.
     * <p>
     * Allow additional process of mybatis entity.
     *
     * @param entityClazz entity class
     * @return true
     */
    protected boolean checkBeanType(Class<?> entityClazz) {
        return true;
    }
}
