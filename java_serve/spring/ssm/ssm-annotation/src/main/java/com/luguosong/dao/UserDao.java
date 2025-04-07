package com.luguosong.dao;

import com.luguosong.bean.User;
import org.apache.ibatis.annotations.Select;

/**
 * @author luguosong
 */
public interface UserDao {
	@Select("select * from user where id = #{id}")
	User selectById(Integer id);
}
