package com.luguosong.get_param.pojo;

import lombok.Data;

import java.sql.Date;

/**
 * @author luguosong
 */
@Data
public class Employees {
    private Integer id;
    private String firstName;
    private String lastName;
    private String position;
    private Date hireDate;
    private Integer departmentId;
}
