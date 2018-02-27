package com.github.yuxiaobin.mybatis.gm;

import com.baomidou.mybatisplus.MybatisConfiguration;

/**
 * <p>
 * this class is used to store typeAliasesPackage for some special cases
 * that won't execute
 * {@link com.github.yuxiaobin.mybatis.gm.processer.MybatisGeneralEntityProcessor}.postProcessAfterInitialization()
 * so that multiple typeAliasesPackage cannot be registered.
 * </p>
 *
 * @author Kelly Lake (179634696@qq.com).
 */
public class GeneralConfiguration extends MybatisConfiguration{

    private String typeAliasesPackage;

    public String getTypeAliasesPackage() {
        return typeAliasesPackage;
    }

    public GeneralConfiguration setTypeAliasesPackage(String typeAliasesPackage) {
        this.typeAliasesPackage = typeAliasesPackage;
        return this;
    }
}
