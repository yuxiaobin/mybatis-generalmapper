package com.github.yuxiaobin.mybatis.gm.processer;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.toolkit.PackageHelper;
import com.github.yuxiaobin.mybatis.gm.GeneralMapper;
import com.github.yuxiaobin.mybatis.gm.plus.GeneralEntitySubTypesHolder;

/**
 * <p>
 * V1.8: scan sub-entities for entity, which is used for supporting query by subtypes.
 * V1.8.7-beta: only used to scan sub-entities. Move sql inject to {@link com.github.yuxiaobin.mybatis.gm.GeneralSqlSessionFactoryBean}
 * for some special cases(spring boot), bean init order issue which will cause invoke generalMapper failed in {@code javax.annotation..PostConstruct}
 *
 * @author Kelly Lake(179634696@qq.com)
 */
public class MybatisGeneralEntityProcessor implements ApplicationListener<ApplicationEvent> {

    public static String[] typeAliasPackageArray;

    public static String generateNamespace(Class<?> entityClazz) {
        return GeneralMapper.class.getSimpleName().concat(".").concat(entityClazz.getName());
    }

    /**
     * Allow to define the parse strategy for package scan.
     * <p>
     * for package like "com.github.yuxiaobin.*.persistent",
     * can be parsed as ["com.github.yuxiaobin.auth.persistent", "com.github.yuxiaobin.buz.persistent",...]
     *
     * @param typeAliasesPackage entity class path.
     * @return
     * @since 1.2
     */
    public static String[] parseTypeAliasPackage(String typeAliasesPackage) {
        String[] typeAliasPackageArray = tokenizeToStringArray(typeAliasesPackage,
                ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
        List<String> packageList = new ArrayList<>();
        for (String pkg : typeAliasPackageArray) {
            if (pkg.contains("*")) {
                String[] subPackages = PackageHelper.convertTypeAliasesPackage(pkg);
                for (String s : subPackages) {
                    if (!packageList.contains(s)) {
                        packageList.add(s);
                    }
                }
            } else {
                if (!packageList.contains(pkg)) {
                    packageList.add(pkg);
                }
            }
        }
        int size = packageList.size();
        String[] array = new String[size];
        packageList.toArray(array);
        return array;
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
        if (event instanceof ContextRefreshedEvent) {
            if (typeAliasPackageArray != null) {
                for (String persistentPackage : typeAliasPackageArray) {
                    if (StringUtils.hasLength(persistentPackage)) {
                        int index = persistentPackage.lastIndexOf(".");
                        String entityVOPackage = persistentPackage.substring(0, index);
                        scanEntityVOPackage(entityVOPackage);
                    }
                }
            }
        }
    }

    private void scanEntityVOPackage(String entityVOPackage) {
        Reflections reflections = new Reflections(entityVOPackage);
        Set<Class<?>> entityClazzSet = reflections.getTypesAnnotatedWith(TableName.class, true);
        for (Class<?> entityClazz : entityClazzSet) {
            Set<?> modules = reflections.getSubTypesOf(entityClazz);
            for (Object c : modules) {
                GeneralEntitySubTypesHolder.put((Class<?>) c, entityClazz);
            }
            GeneralEntitySubTypesHolder.put(entityClazz, entityClazz);
        }
    }
}
