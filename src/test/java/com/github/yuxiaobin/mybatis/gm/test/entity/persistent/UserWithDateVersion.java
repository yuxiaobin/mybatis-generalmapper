package com.github.yuxiaobin.mybatis.gm.test.entity.persistent;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.github.yuxiaobin.mybatis.gm.annotations.Version;

@TableName("user")
public class UserWithDateVersion implements Serializable {

	/* 表字段注解，false 表中不存在的字段，可无该注解 默认 true */
	@TableField(exist = false)
	private static final long serialVersionUID = 1L;

	/* 主键ID 注解，value 字段名，type 用户输入ID */
	@TableId(value = "test_id")
	private Long id;

	/* 测试忽略验证 */
	private String name;

	@TableField(value = "test_date")
	@Version
	private Date testDate;

	private Integer age;

	/*BigDecimal 测试*/
	private BigDecimal price;

	@TableField(value = "test_type")
	private Integer testType;

	private Integer version;

	public UserWithDateVersion() {

	}

	public UserWithDateVersion(String name) {
		this.name = name;
	}

	public UserWithDateVersion(Integer testType) {
		this.testType = testType;
	}

	public UserWithDateVersion(String name, Integer age) {
		this.name = name;
		this.age = age;
	}

	public UserWithDateVersion(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public UserWithDateVersion(Long id, Integer age) {
		this.id = id;
		this.age = age;
	}

	public UserWithDateVersion(Long id, String name, Integer age, Integer testType) {
		this.id = id;
		this.name = name;
		this.age = age;
		this.testType = testType;
	}

	public UserWithDateVersion(String name, Integer age, Integer testType) {
		this.name = name;
		this.age = age;
		this.testType = testType;
	}

	public Date getTestDate() {
		return testDate;
	}

	public void setTestDate(Date testDate) {
		this.testDate = testDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Integer getTestType() {
		return testType;
	}

	public void setTestType(Integer testType) {
		this.testType = testType;
	}


	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", testDate=" + testDate + ", age=" + age + ", price=" + price
				+ ", testType=" + testType + ", version=" + version + "]";
	}

	

}
