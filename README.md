#Mybatis-generalmapper
* A middle-ware for mybatis plus, you can use it to CRUD single table without any sql.
* 一个基于mybatis-plus的中间件，让单表CURD无SQL

What you need is: 
* create an entity with annotation: @TableId(Required), @TableName(Optional, required if entityName not eq tableName, eg: No need:TblUser - tbl\_user; Required: User - tbl\_user) ,
* then configure typeAliasesPackage to make the entity be scanned by mybatis(v_1.1 add support for `com.github.yuxiaobin.*.persistent`),
* import GeneralMapper, invoke CRUD method with the entity.

你只需要做：
* 创建创建实体带有@TableId(必须), @TableName(可以有,如果表名和实体类名不相同则需要，如果满足驼峰命名相同，可以不用)
* 配置typeAliasesPackage 可以扫描到该实体(v_1.1支持通配符扫描实体,`com.github.yuxiaobin.*.persistent`)
* 引入GeneralMapper, 就可以调用CRUD

##Mybatis-plus部分
具体使用，可参考[github上wiki](http://git.oschina.net/juapk/mybatis-plus)， 生成代码参考MybatisPlusGenerator.java
* About plus: a middle-ware for mybatis, CRUD operation for single table, no need to configure mapper.xml, SQL will auto be injected.
* Mybatis-generalmapper uses the sql injection by plus(currently use plus v_1.5).

#Release History
* v_1.0: init
* v_1.1: Add support typeAliasesPackage contains *: com.github.yuxiaobin.*.persistent
* v_1.2: Add support typeAliasesPackage contains ;,\t\n: com.github.yuxiaobin.*.persistent;com.your.company.persistent

