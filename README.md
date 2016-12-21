#Mybatis-generalmapper
* A middle-ware for mybatis plus, you can use it to CRUD single table without any sql.
* 一个基于mybatis-plus的中间件，让单表CURD无SQL

What you need is: 
* create an entity with annotation: @TableId(Required), @TableName(Optional, required if entityName not eq tableName, eg: No need:TblUser - tbl\_user; Required: User - tbl\_user) ,
* then configure typeAliasesPackage to make the entity be scanned by mybatis,
* import GeneralMapper, invoke CRUD method with the entity.

你只需要做：
* 创建创建实体带有@TableId(必须), @TableName(可以有,如果表名和实体类名不相同则需要，如果满足驼峰命名相同，可以不用)
* 配置typeAliasesPackage 可以扫描到该实体
* 引入GeneralMapper, 就可以调用CRUD

##Mybatis-plus部分
具体使用，可参考[github上wiki](http://git.oschina.net/juapk/mybatis-plus)， 生成代码参考MybatisPlusGenerator.java

