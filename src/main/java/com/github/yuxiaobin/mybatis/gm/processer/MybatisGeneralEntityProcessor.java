package com.github.yuxiaobin.mybatis.gm.processer;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.baomidou.mybatisplus.toolkit.PackageHelper;
import com.github.yuxiaobin.mybatis.gm.GeneralMapper;
import com.github.yuxiaobin.mybatis.gm.GeneralSqlSessionFactoryBean;
import com.github.yuxiaobin.mybatis.gm.mapper.GeneralMapperSqlInjector;

/**
 * @author Kelly Lake(179634696@qq.com)
 */
public class MybatisGeneralEntityProcessor implements BeanPostProcessor {
	
	private static final Log LOGGER = LogFactory.getLog(MybatisGeneralEntityProcessor.class);

    private GeneralMapperSqlInjector generalSqlInjector;

    private boolean plusInject = false;
    
    private String typeAliasesPackage;
    
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
            }else if (bean instanceof SqlSessionFactoryBean && !plusInject) {
            	if(bean instanceof GeneralSqlSessionFactoryBean){
            		this.typeAliasesPackage = ((GeneralSqlSessionFactoryBean)bean).getTypeAliasesPackage();
            	}
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
     * @param SqlSessionFactory sqlsessionfactoryBean
     */
    private void injectSql(Configuration configuration) {
		if(StringUtils.hasLength(typeAliasesPackage) && typeAliasesPackage.contains("*")){
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("injectSql(): typeAliasesPackage="+typeAliasesPackage);
			}
			String[] typeAliasPackageArray = null;
			if (typeAliasesPackage.contains("*")) {
				typeAliasPackageArray = PackageHelper.convertTypeAliasesPackage(typeAliasesPackage);
			} else {
				typeAliasPackageArray = tokenizeToStringArray(typeAliasesPackage,
						ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
			}
			for (String packageToScan : typeAliasPackageArray) {
				configuration.getTypeAliasRegistry().registerAliases(packageToScan, Object.class );
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Scanned package: '" + packageToScan + "' for aliases");
				}
			}
		}
    	
        for (Map.Entry<String, Class<?>> type : configuration.getTypeAliasRegistry().getTypeAliases().entrySet()) {
        	if(checkValidateClassTypes(type.getValue())){
	            MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, type.getValue().getPackage().getName());
	            assistant.setCurrentNamespace(generateNamespace(type.getValue()));
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
    
    public static String generateNamespace(Class<?> entityClazz){
    	return GeneralMapper.class.getSimpleName().concat(".").concat(entityClazz.getName());
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
