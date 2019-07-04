package com.mp.test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.mp.dao.UserMapper;
import com.mp.pojo.User;
import com.mp.service.UserServuce;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SimpleTest {

	@Autowired
	private UserMapper userMapper;
	@Autowired
	private UserServuce userServuce;
	
	@Test
	public void select() {
		List<User> list = userMapper.selectList(null);
		Assert.assertEquals(5, list.size());
		list.forEach(System.out::println);
	}
	
	@Test
	public void insert() {
		new User();
		//User.builder().name("小王").age(10).managerId(1087982257332887553L).createTime(LocalDateTime.now()).build();
		int rows = userMapper.insert(User.builder().name("小王").age(10).managerId(1087982257332887553L).createTime(LocalDateTime.now()).build());
		System.out.println(rows);
	}
	
	@Test
	public void selectById() {
		User user = userMapper.selectById(1087982257332887553L);
		System.out.println(user);
	}
	
	@Test
	public void selectIds() {
		List<Long> ids = Arrays.asList(1087982257332887553L, 1088248166370832385L, 1088250446457389058L);
		List<User> list = userMapper.selectBatchIds(ids);
		list.forEach(System.out::println);
	}
	
	@Test
	public void selectByMap() {
		// map.put("name", "aa");
		// map.put("age", "11"); where name = 'aa' and age = 11  key要是数据库中的列相对应 不是实体类中的对象
		Map<String, Object> columnMap = new HashMap<String, Object>();
		columnMap.put("name", "王天风");
		columnMap.put("age", 25);
		List<User> list = userMapper.selectByMap(columnMap);
		list.forEach(System.out::println);
	}
	
	/**
	 * name like '%雨%' and age < 40
	 */
	@Test
	public void selecyByWrapper() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
//		QueryWrapper<User> wrapper = Wrappers.<User>query();
		queryWrapper.like("name", "雨").lt("age", 40); //key对应数据库中的列  lt 小于
		List<User> list =userMapper.selectList(queryWrapper);
		list.forEach(System.out::println);
	}
	
	/**
	 * name like '%雨%' and age between 20 and 40 and email is not null
	 */
	@Test
	public void selecyByWrapper1() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		queryWrapper.like("name", "雨").between("age", 20, 40).isNotNull("email"); //key对应数据库中的列  lt 小于
		List<User> list =userMapper.selectList(queryWrapper);
		list.forEach(System.out::println);
	}
	
	/**
	 * name like '王%' or age >= 25 order by age desc, id asc
	 */
	@Test
	public void selecyByWrapper2() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		queryWrapper.likeRight("name", "王").or().ge("age", 25).orderByDesc("age").orderByAsc("id"); //key对应数据库中的列  lt 小于
		List<User> list =userMapper.selectList(queryWrapper);
		list.forEach(System.out::println);
	}
	
	/**
	 * 创建日期为 yyyy-MM-dd 并且直属上级名字为王姓
	 */
	@Test
	public void selecyByWrapper3() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		// SELECT id,name,age,email,manager_id,create_time FROM user WHERE date_format(create_time, '%Y-%m-%d') = ? AND manager_id IN (select id from user where name like '王%') 
		queryWrapper.apply("date_format(create_time, '%Y-%m-%d') = {0}", "2019-02-14").inSql("manager_id", "select id from user where name like '王%'");
		List<User> list =userMapper.selectList(queryWrapper);
		list.forEach(System.out::println);
	}
	
	/**
	 * name like '王%' and (age < 40 or email is not null)
	 */
	@Test
	public void selecyByWrapper4() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		//SELECT id,name,age,email,manager_id,create_time FROM user WHERE name LIKE ? AND ( age < ? OR email IS NOT NULL ) 
		queryWrapper.likeRight("name", "王").and(wq -> wq.lt("age", 40).or().isNotNull("email"));
		List<User> list =userMapper.selectList(queryWrapper);
		list.forEach(System.out::println);
	}
	
	/**
	 * name like '王%' or (age < 40 and age > 20 and email is not null)
	 */
	@Test
	public void selecyByWrapper5() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		//SELECT id,name,age,email,manager_id,create_time FROM user WHERE name LIKE ? OR ( age < ? AND age > ? AND email IS NOT NULL ) 
		queryWrapper.likeRight("name", "王").or(wq -> wq.lt("age", 40).gt("age", 20).isNotNull("email"));
		List<User> list =userMapper.selectList(queryWrapper);
		list.forEach(System.out::println);
	}
	
	/**
	 * (age < 40 or email is not null) and name like '王%'
	 */
	@Test
	public void selecyByWrapper6() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		// SELECT id,name,age,email,manager_id,create_time FROM user WHERE ( age < ? OR email IS NOT NULL ) AND name LIKE ? 
		queryWrapper.nested(wq -> wq.lt("age", 40).or().isNotNull("email")).likeRight("name", "王");
		List<User> list =userMapper.selectList(queryWrapper);
		list.forEach(System.out::println);
	}
	
	/**
	 * age in (30, 31, 32, 33)
	 */
	@Test
	public void selecyByWrapper7() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		// SELECT id,name,age,email,manager_id,create_time FROM user WHERE age IN (?,?,?,?) limit 1 
		queryWrapper.in("age", Arrays.asList(30, 31, 33, 35)).last("limit 1");
		List<User> list =userMapper.selectList(queryWrapper);
		list.forEach(System.out::println);
	}
	
	/**
	 * name like '%雨%' and age < 40
	 */
	@Test
	public void selecyByWrapperSupper() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		queryWrapper.select("id", "name").like("name", "雨").lt("age", 40); //key对应数据库中的列  lt 小于
		List<User> list =userMapper.selectList(queryWrapper);
		list.forEach(System.out::println);
	}
	
	@Test
	public void selecyByWrapperSupper1() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		//SELECT id,name,age,email FROM user WHERE name LIKE ? AND age < ? 
		queryWrapper.like("name", "雨").lt("age", 40)
			.select(User.class, info -> !info.getColumn().equals("create_time")
					&& !info.getColumn().equals("manager_id")); //key对应数据库中的列  lt 小于
		List<User> list =userMapper.selectList(queryWrapper);
		list.forEach(System.out::println);
	}
	
	@Test
	public void testCodition() {
		String name = "王";
		String email = "";
		condition(name, email);
	}
	public void condition(String name, String email) {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		// 
		queryWrapper.like(StringUtils.isNotEmpty(name), "name", name).like(StringUtils.isNotEmpty(email), "email", email);
		List<User> list =userMapper.selectList(queryWrapper);
		list.forEach(System.out::println);
	}
	
	@Test
	public void selecyByWrapperEntity() {
		User user = new User();
		user.setName("刘红雨");
		user.setAge(32);
		// SELECT id,name,age,email,manager_id,create_time FROM user WHERE name=? AND age=? 
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>(user);
		List<User> list =userMapper.selectList(queryWrapper);
		list.forEach(System.out::println);
	}
	
	@Test
	public void selecyByWrapperAllEq() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", "王天风");
		map.put("age", null);
//		queryWrapper.allEq(map, false); // false 值为null时忽略
		
		
		// SELECT id,name,age,email,manager_id,create_time FROM user WHERE age IS NULL
		queryWrapper.allEq((k, v) -> !k.equals("name"), map);
		List<User> list =userMapper.selectList(queryWrapper);
		list.forEach(System.out::println);
	}
	
	@Test
	public void selecyByWrapperMaps() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
//		queryWrapper.like("name", "雨").lt("age", 40);
		//{create_time=2019-01-14 20:45:15.0, manager_id=1088248166370832385, name=张雨琪, id=1094590409767661570, age=31, email=zjq@baomidou.com}
		queryWrapper.like("name", "雨").lt("age", 40).select("id", "name");// {name=刘红雨, id=1094592041087729666}
		List<Map<String, Object>> list = userMapper.selectMaps(queryWrapper);
		list.forEach(System.out::println);
	}
	
	/**
	 * SELECT avg(age) avg_age,min(age) min_age,max(age) max_age FROM user GROUP BY manager_id HAVING sum(age) < 500
	 */
	@Test
	public void selecyByWrapperMaps1() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		queryWrapper.select("avg(age) avg_age", "min(age) min_age", "max(age) max_age").groupBy("manager_id").having("sum(age) < {0}", 500);
		List<Map<String, Object>> list = userMapper.selectMaps(queryWrapper);
		list.forEach(System.out::println);
	}
	
	/**
	 *  只返回第一个字段的值
	 */
	@Test
	public void selecyByWrapperObjs() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		queryWrapper.like("name", "雨").lt("age", 40).select("id", "name");
		List<Object> list = userMapper.selectObjs(queryWrapper);
		list.forEach(System.out::println);
	}
	
	/**
	 * 查询总记录数
	 */
	@Test
	public void selecyByWrapperCount() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		queryWrapper.like("name", "雨").lt("age", 40);
		Integer list = userMapper.selectCount(queryWrapper);
		System.out.println(list); //2
	}
	
	@Test
	public void selecyByWrapperOne() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		queryWrapper.like("name", "刘红雨").lt("age", 40);
		User list = userMapper.selectOne(queryWrapper);
		System.out.println(list); //返回超过1个会报错
	}
	
	/**
	 * lambda 条件构造器
	 */
	@Test
	public void selectLambda() {
		//1.
//		LambdaQueryWrapper<User> lambdaQueryWrapper = new QueryWrapper<User>().lambda();
		// 2.
//		LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
		//3.
		LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery();
		
		queryWrapper.like(User::getName, "雨").lt(User::getAge, 40);
		//SELECT id,name,age,email,manager_id,create_time FROM user WHERE name LIKE ? AND age < ? 
		List<User> list = userMapper.selectList(queryWrapper);
		list.forEach(System.out::println);
	}
	
	@Test
	public void selectLambda1() {
		List<User> list = new LambdaQueryChainWrapper<User>(userMapper).like(User::getName, "雨").lt(User::getAge, 40).list();
		list.forEach(System.out::println);
	}
	
	@Test
	public void selectMy() {
		LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery();
		queryWrapper.likeRight(User::getName, "雨")
			.and(lqw -> lqw.lt(User::getAge, 40).or().isNotNull(User::getEmail));
		//select * from user WHERE name LIKE ? AND ( age < ? OR email IS NOT NULL ) 
		List<User> list = userMapper.selectAll(queryWrapper);
		list.forEach(System.out::println);
	}
	
	@Test
	public void selectMy1() {
		LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery();
		queryWrapper.likeRight(User::getName, "雨")
		.and(lqw -> lqw.lt(User::getAge, 40).or().isNotNull(User::getEmail));
		//select * from user WHERE name LIKE ? AND ( age < ? OR email IS NOT NULL ) 
		List<User> list = userMapper.selectTest(queryWrapper);
		list.forEach(System.out::println);
	}
	
	@Test
	public void selectPage() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		queryWrapper.gt("age", 26);
		Page<User> page = new Page<User>(1, 2, false); // false不查总记录数
		IPage<User> iPage = userMapper.selectPage(page, queryWrapper);
		System.out.println("总页数:" + iPage.getPages());
		System.out.println("总记录数:" + iPage.getTotal());
		List<User> list = iPage.getRecords();
		list.forEach(System.out::println);
	}
	
	@Test
	public void selectPage1() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		queryWrapper.gt("age", 26);
		Page<User> page = new Page<User>(1, 2);
		IPage<Map<String, Object>> iPage = userMapper.selectMapsPage(page, queryWrapper);
		System.out.println("总页数:" + iPage.getPages());
		System.out.println("总记录数:" + iPage.getTotal());
		List<Map<String, Object>> list = iPage.getRecords();
		list.forEach(System.out::println);
	}
	
	@Test
	public void selectMyPage() {
		QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
		queryWrapper.gt("age", 26);
		Page<User> page = new Page<User>(1, 2);
		IPage<User> iPage = userMapper.selectUserPage(page, queryWrapper);
		System.out.println("总页数:" + iPage.getPages());
		System.out.println("总记录数:" + iPage.getTotal());
		List<User> list = iPage.getRecords();
		list.forEach(System.out::println);
	}
	
	/**
	 * ************************************UPDATE********************************
	 */
	
	@Test
	public void updateById() {
		User user = new User();
		user.setId(1088250446457389058L);
		user.setAge(22);
		user.setEmail("test@se.com");
		int i = userMapper.updateById(user);
		System.out.println(i);
	}
	
	@Test
	public void updateByWrapper() {
		UpdateWrapper<User> updateWrapper = new UpdateWrapper<User>();
		updateWrapper.eq("name", "李艺伟").eq("age", 22);
		User user = new User();
		user.setAge(29);
		user.setEmail("211@baomidou.com");
		int i = userMapper.update(user, updateWrapper);
		System.out.println(i);
	}
	
	@Test
	public void updateByWrapper1() {
		UpdateWrapper<User> updateWrapper = new UpdateWrapper<User>();
		updateWrapper.eq("name", "李艺伟").eq("age", 22).set("age", 28);
		//UPDATE user SET age=? WHERE name = ? AND age = ? 
		int i = userMapper.update(null, updateWrapper);
		System.out.println(i);
	}
	
	@Test
	public void updateByWrapperLambda() {
		LambdaUpdateWrapper<User> lambdaUpdateWrapper = Wrappers.<User>lambdaUpdate();
		lambdaUpdateWrapper.eq(User::getName, "李艺伟").eq(User::getAge, 28).set(User::getAge, 30);
		
		//UPDATE user SET age=? WHERE name = ? AND age = ? 
		int i = userMapper.update(null, lambdaUpdateWrapper);
		System.out.println(i);
	}
	
	@Test
	public void updateByWrapperLambdaChain() {
		boolean update = new LambdaUpdateChainWrapper<User>(userMapper).eq(User::getName, "李艺伟").eq(User::getAge, 28).set(User::getAge, 30).update();
		System.err.println(update);
	}
	
	/**
	 * **********************************DELETE***********************************************
	 */
	
	@Test
	public void deleteById() {
		int i = userMapper.deleteById(1145780609926508545L);
		System.out.println(i);
	}
	
	@Test
	public void deleteByMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", "小王");
		map.put("age", 20);
		int i = userMapper.deleteByMap(map);
		System.out.println(i);
	}
	
	@Test
	public void deleteBechIds() {
		int i = userMapper.deleteBatchIds(Arrays.asList(1, 2, 3));
		System.out.println(i);
	}
	
	@Test
	public void deleteByWrapper() {
		LambdaQueryWrapper<User> lambdaQueryWrapper = Wrappers.<User>lambdaQuery();
		lambdaQueryWrapper.eq(User::getAge, 27).or().gt(User::getAge, 60);
		int i = userMapper.delete(lambdaQueryWrapper);
		System.out.println(i);
	}
	
	/**
	 * *******************************SERVICETEST*********************************
	 */
	
	@Test
	public void getOne() {
		User user = userServuce.getOne(Wrappers.<User>lambdaQuery().gt(User::getAge, 30), false);// false 时 当查询数据多条时警告不报错
		System.out.println(user);
	}
}
