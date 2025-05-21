package com.luguosong.many_to_one.pojo;

import lombok.Data;

import java.sql.Date;

/**
 * 员工
 * @author luguosong
 */
@Data
public class Employees {
    private Integer id;
    private String firstName;
    private String lastName;
    private String position;
    private Date hireDate;
    //private Integer departmentId;

    // 关联部门
    private Departments departments;
}
