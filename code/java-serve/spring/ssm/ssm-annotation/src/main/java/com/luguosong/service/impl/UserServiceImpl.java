package com.luguosong.service.impl;

import com.luguosong.bean.User;
import com.luguosong.dao.UserDao;
import com.luguosong.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author luguosong
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;

	@Override
	public User selectById(Integer id) {
		return userDao.selectById(id);
	}
}
