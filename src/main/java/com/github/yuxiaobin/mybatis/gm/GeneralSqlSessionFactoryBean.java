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

import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionFactoryBean;

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

    private String typeAliasesPackage;
    private Configuration configuration;

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
}
