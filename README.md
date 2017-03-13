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

#Maven
	
	<dependency>
		<groupId>com.baomidou</groupId>
		<artifactId>mybatis-plus</artifactId>
		<version>1.5</version>
	</dependency>
	<dependency>
		<groupId>com.github.yuxiaobin</groupId>
		<artifactId>mybatis-generalmapper</artifactId>
		<version>1.8</version>
	</dependency>

#Release History
* v_1.0: init
* v_1.1: Add support typeAliasesPackage contains *: com.github.yuxiaobin.*.persistent
* v_1.2: Add support typeAliasesPackage contains ;,\t\n: com.github.yuxiaobin.*.persistent;com.your.company.persistent
* v_1.3: Add support typeAliasesPackage contains multiple *: com.projecta.*.persistent;com.projectb.*.persistent;com.projectc.*.persistent
* v_1.4: Add deleteByEW(EntityWrapper);
* v_1.5: Wrap result : use ew.entity.type, to solve returned parent class object;
* v_1.6: Add GeneralEntityWrapper: to solve such as ew.and("col_dt={0}", Date val) sqlSegment issue.
* v_1.7: SQL inject bypass classes under java/apache/spring/hibernate; use GeneralEntityWrapper in byEW methods to avoid v_1.6 issue.
* v_1.8: Add GeneralPaginationInterceptor,GeneralSqlChangeInterceptor, to allow multiple Sql change interceptors for Query ONLY(See Sample Code below).
* v_1.8.1: Bug fixing.

#Roadmap
* support entityWrapper use property/column name: ew.where("name={0}","123") or ew.where("user_name={0}","123") -> SQL: where user_name='123'


#Some Sample Code for Spring Boot
	
	@Configuraion
	public class GeneralMapperConfig {
		private final GeneralSqlChangeInterceptor[] interceptors;
		public GeneralMapperConfig(ObjectProvider<GeneralSqlChangeInterceptor[]> interceptorsProvider){
			this.interceptors = interceptorsProvider.getIfAvailable();
		}
		@Bean
		public GeneralPaginationInterceptor paginationInterceptor(){
			return new GeneralPaginationInterceptor(interceptors);
		}
	}
	
	@Configuration
	@MapperScan("com.github.yuxiaobin.test.mapper")
	@Import({GeneralMapperBootstrapConfiguration.class})
	@EnableConfigurationProperties(MybatisProperties.class)
	public class MybatisConfig{
		private final MybatisProperties properties;
		private final Interceptor[] interceptors;
		private final ResourceLoader resourceLoader;
		private final DatabaseIdProvider databaseIdProvider;
		public MybatisConfig(MybatisProperties properties,
								            ObjectProvider<Interceptor[]> interceptorsProvider,
								            ResourceLoader resourceLoader,
								            ObjectProvider<DatabaseIdProvider> databaseIdProvider){
			this.properties = properties;
			this.interceptors = interceptorsProvider.getIfAvailable();
			this.resourceLoader = resourceLoader;
			this.databaseIdProvider = databaseIdProvider.getIfAvailable();
	}
	
	@Bean
	public GeneralMapper generalMapper(GeneralSqlSessionFactoryBean factoryBean) throws Exception{
		GeneralMapper generalMapper = new GeneralMapper();
		generalMapper.setSqlSessionFactory(factoryBean.getObject());
		return generalMapper;
	}
	
	@Bean
	public GeneralSqlSessionFactoryBean sqlSessionFactory (DataSource dataSource){
		GeneralSqlSessionFactoryBean sqlSessionFactory = new GeneralSqlSessionFactoryBean();
		sqlSessionFactory.setDataSource(dataSource);
		sqlSessionFactory.setVfs(SpringBootVFS.class);
		if (!ObjectUtils.isEmpty(this.interceptors)) {
			sqlSessionFactory.setPlugins(this.interceptors);
		}else{
			sqlSessionFactory.setPlugins(new Interceptor[]{new GeneralPaginationInterceptor(null)});
		}
		if (this.databaseIdProvider != null) {
			sqlSessionFactory.setDatabaseIdProvider(this.databaseIdProvider);
		}
		if(!StringUtils.isEmpty(properties.getTypeAliasesPackage())){
			sqlSessionFactory.setTypeAliasesPackage(properties.getTypeAliasesPackage());//多个不同的包，可以用 , ; 分隔
		}else{
			sqlSessionFactory.setTypeAliasesPackage("com.github.yuxiaobin.test.persistent");
		}
		if(StringUtils.hasText(this.properties.getConfigLocation())) {
			 sqlSessionFactory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
		}
		MybatisConfiguration configuration = new MybatisConfiguration();
		configuration.setDefaultScriptingLanguage(GeneralMybatisXMLLanguageDriver.class);
		configuration.setJdbcTypeForNull(JdbcType.NULL);
		org.apache.ibatis.session.Configuration config = properties.getConfiguration();
		if(config!=null){
			configuration.setMapUnderscoreToCamelCase(config.isMapUnderscoreToCamelCase());
		}
		sqlSessionFactory.setConfiguration(configuration);
		return sqlSessionFactory;
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
		List<SampleTO> list =generalMapper.selectPage(new Page<>(1,10),ew);
	/**********SQL:*********/
	//select * from tbl_sample where sample_name='123' and sample_age>18 and (sample_dt>'2015-12-12' and sample_dt<'2016-1-1') and sample_nkname like '%a%'
