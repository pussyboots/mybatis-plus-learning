package com.mp.pojo;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("user")
public class User {

	//主键
	@TableId
	private Long id;
	
	@TableField(value = "name", condition = SqlCondition.LIKE)
	private String name;
	
	@TableField(condition = "%s&lt;#{%s}") // < 小于
	private Integer age;
	
	private String email;
	
	private Long managerId;
	
	private LocalDateTime createTime;
	
	//字段在表中不存在
	@TableField(exist = false)
	private String remark;
}
