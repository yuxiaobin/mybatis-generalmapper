package com.github.yuxiaobin.mybatis.gm.processer;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ClassUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.baomidou.mybatisplus.toolkit.PackageHelper;
import com.github.yuxiaobin.mybatis.gm.GeneralMapper;
import com.github.yuxiaobin.mybatis.gm.GeneralSqlSessionFactoryBean;
import com.github.yuxiaobin.mybatis.gm.mapper.GeneralMapperSqlInjector;
import com.github.yuxiaobin.mybatis.gm.plus.GeneralEntitySubTypesHolder;

/**
 * This class is used to inject SQL for entity.<BR>
 * 
 * V1.8: scan sub-entities for entity, which is used for supporting query by subtypes. 
 * 
 * @author Kelly Lake(179634696@qq.com)
 */
public class MybatisGeneralEntityProcessor implements BeanPostProcessor,ApplicationListener<ApplicationEvent> {
	
	private static final Log LOGGER = LogFactory.getLog(MybatisGeneralEntityProcessor.class);

    private final GeneralMapperSqlInjector generalSqlInjector;

    private boolean plusInject = false;
    
    private String typeAliasesPackage;
    
    private String[] typeAliasPackageArray;
    
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
            throw new MybatisPlusException(e);
        }
        return bean;
    }

    /**
     *
     * @param configuration Mybatis Configuration.
     */
    private void injectSql(Configuration configuration) {
		if(StringUtils.hasLength(typeAliasesPackage) && typeAliasesPackage.contains("*")){
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("injectSql(): typeAliasesPackage="+typeAliasesPackage);
			}
			typeAliasPackageArray = parseTypeAliasPackage(this.typeAliasesPackage);
			if(typeAliasPackageArray!=null){
				for (String packageToScan : typeAliasPackageArray) {
					configuration.getTypeAliasRegistry().registerAliases(packageToScan, Object.class );
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
    
    /**
     * Allow to define the parse strategy for package scan.
     * 
     * for package like "com.github.yuxiaobin.*.persistent", 
     * can be parsed as ["com.github.yuxiaobin.auth.persistent", "com.github.yuxiaobin.buz.persistent",...]
     * 
     * @since 1.2
     * @param typeAliasesPackage entity class path.
     * @return
     */
    public static String[] parseTypeAliasPackage(String typeAliasesPackage){
    	String[] typeAliasPackageArray = tokenizeToStringArray(typeAliasesPackage,
				ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
    	List<String> packageList = new ArrayList<>();
    	for(String pkg : typeAliasPackageArray){
    		if(pkg.contains("*")) {
    			String[] subPackages = PackageHelper.convertTypeAliasesPackage(pkg);
    			for(String s:subPackages){
    				if(!packageList.contains(s)){
    					packageList.add(s);
    				}
    			}
    		}else{
    			if(!packageList.contains(pkg)){
					packageList.add(pkg);
				}
    		}
    	}
    	int size = packageList.size();
    	String[] array = new String[size];
    	packageList.toArray(array);
		return array;
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
     * @param entityClazz entity class
     * @return true
     */
    protected boolean checkBeanType(Class<?> entityClazz){
    	return true;
    }

    /**
     * Scan sub-types for Entity. So that allows to query by sub-types.<BR>
     * <BR>
     * public class User;//is a persistent object.<BR>
     * public class UserVO extends User;<BR>
     * <BR>
     * List&lt;UserVO&gt; list = generalMapper.selectList(UserVO);<BR>
     */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if(event instanceof ContextRefreshedEvent){
			if(typeAliasPackageArray!=null){
				for(String persistentPackage:typeAliasPackageArray){
					if(StringUtils.hasLength(persistentPackage)){
						int index = persistentPackage.lastIndexOf(".");
						String entityVOPackage = persistentPackage.substring(0,index);
						scanEntityVOPackage(entityVOPackage);
					}
				}
			}else{//bug fix for typeAliasesPackage only contains one package.
				if(typeAliasesPackage!=null){
					int index = typeAliasesPackage.lastIndexOf(".");
					scanEntityVOPackage(typeAliasesPackage.substring(0,index));
				}
			}
		}
	}
	private void scanEntityVOPackage(String entityVOPackage){
		Reflections reflections = new Reflections(entityVOPackage);
		Set<Class<?>> entityClazzSet = reflections.getTypesAnnotatedWith(TableName.class, true);
		for(Class<?> entityClazz:entityClazzSet){
			Set<?> modules = reflections.getSubTypesOf(entityClazz);
            for(Object c:modules){
            	GeneralEntitySubTypesHolder.put((Class<?>)c, entityClazz);
            }
            GeneralEntitySubTypesHolder.put(entityClazz, entityClazz);
		}
	}
}
