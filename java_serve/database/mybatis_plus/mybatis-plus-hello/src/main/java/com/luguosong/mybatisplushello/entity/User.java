package com.luguosong.mybatisplushello.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author luguosong
 */
@Data
@TableName("`user`")
public class User {
	private Long id;
	private String name;
	private Integer age;
	private String email;
}
