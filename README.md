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
* v_1.3: Add support typeAliasesPackage contains multiple *: com.projecta.*.persistent;com.projectb.*.persistent;com.projectc.*.persistent
* v_1.4: Add deleteByEW(EntityWrapper);

#Roadmap
* support entityWrapper use property/column name: ew.where("name={0}","123") or ew.where("user_name={0}","123") -> SQL: where user_name='123'

#NOTE
not released to maven central repository(v_1.2 released) due to publish key missing... 

#Some Sample Code for Spring Boot
	
	@MapperScan("com.*.*.mapper")//use this path to scan *Mapper.xml
	MybatisConfig{
		@Bean
		public SqlSessionFactoryBean sqlSessionFactory (DataSource dataSource){
			SqlSessionFactoryBean sqlSessionFactory = new GeneralSqlSessionFactoryBean();
			sqlSessionFactory.setDataSource(dataSource);
			sqlSessionFactory.setVfs(SpringBootVFS.class);
			sqlSessionFactory.setTypeAliasesPackage("com.*.*.persistent");
			MybatisConfiguration configuration = new MybatisConfiguration();
			configuration.setDefaultScriptingLanguage(GeneralMybatisXMLLanguageDriver.class);
			sqlSessionFactory.setConfiguration(configuration);
			return sqlSessionFactory;
		}
		@Bean
		public GeneralMapper generalMapper(SqlSessionFactoryBean sqlSessionFactory){
			GeneralMapper generalMapper = new RsGeneralMapper();
			generalMapper.setSqlSessionFactoryBean(sqlSessionFactory);
			return generalMapper;
		}
	}
	
	Service:
		@Autowired
		GeneralMapper generalMapper;//use spring framework
		
		SampleTO sample= new Sample();
		sample.setName("123");//add condition: sample_name='123'
		EntityWrapper<SampleTO> ew = new EntityWrapper<>(sample);
		ew.where("sample_age>{0}",18);// add condition: sample_age>18 //will support use property name in future
		ew.and(" sample_dt > {0} and sample_dt < {1} ", LocalDate.of(2015, 12, 12).toString(), LocalDate.of(2016, 1, 1).toString());
		ew.like("sample_nkname", "a");
		List<SampleTO> list =generalMapper.selectList(ew);
	/**********SQL:*********/
	//select * from tbl_sample where sample_name='123' and sample_age>18 and (sample_dt>'2015-12-12' and sample_dt<'2016-1-1') and sample_nkname like '%a%'
