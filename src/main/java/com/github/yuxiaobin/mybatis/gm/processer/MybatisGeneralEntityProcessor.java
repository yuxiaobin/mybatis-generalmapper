package com.github.yuxiaobin.mybatis.gm.processer;

import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.github.yuxiaobin.mybatis.gm.GeneralMapper;
import com.github.yuxiaobin.mybatis.gm.mapper.GeneralMapperSqlInjector;

/**
 * @author Kelly Lake(179634696@qq.com)
 */
public class MybatisGeneralEntityProcessor implements BeanPostProcessor {

    private GeneralMapperSqlInjector generalSqlInjector;

    private boolean plusInject = false;
    
    public MybatisGeneralEntityProcessor(GeneralMapperSqlInjector generalSqlInjector) {
        this.generalSqlInjector = generalSqlInjector;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 优先选择mybatis-plus配置进行动态扫描注入
     * 
     *
     * @param bean     SessionFactoryBean
     * @param beanName beanName
     * @return bean
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            if (bean instanceof MybatisSqlSessionFactoryBean) {
                injectSql(((MybatisSqlSessionFactoryBean) bean).getObject().getConfiguration());
                plusInject = true;
            } else if (bean instanceof SqlSessionFactoryBean && !plusInject) {
                injectSql(((SqlSessionFactoryBean) bean).getObject().getConfiguration());
                plusInject = true;
            } else if(bean instanceof SqlSessionFactory && !plusInject){
            	 injectSql(((SqlSessionFactory)bean).getConfiguration());
                 plusInject = true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bean;
    }

    /**
     *
     * @param configuration sqlsessionfactoryBean config
     */
    private void injectSql(Configuration configuration) {
        for (Map.Entry<String, Class<?>> type : configuration.getTypeAliasRegistry().getTypeAliases().entrySet()) {
        	if(checkValidateClassTypes(type.getValue())){
	            MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, type.getValue().getPackage().getName());
	            assistant.setCurrentNamespace(GeneralMapper.class.getName().concat(".").concat(type.getValue().getSimpleName()));
	            generalSqlInjector.inject(configuration, assistant, GeneralMapper.class, type.getValue(), null);
        	}
        }
    }

    private boolean checkValidateClassTypes(Class<?> entityClazz){
    	return !ClassUtils.isPrimitiveOrWrapper(entityClazz) 
    			&& !entityClazz.isArray()
    			&& !entityClazz.isInterface()
    			&& !Object.class.equals(entityClazz)
    			&& checkBeanType(entityClazz)
    			;
    }
    /**
     * Preprocess for class filtering.
     * 
     * Allow additional process of mybatis entity.
     * 
     * @param entityClazz
     * @return
     */
    protected boolean checkBeanType(Class<?> entityClazz){
    	return true;
    }
}
