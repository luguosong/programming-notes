package com.luguosong.one_to_many.pojo;

import lombok.Data;

import java.util.List;

/**
 * 部门
 * @author luguosong
 */
@Data
public class Departments {
    private Integer id;
    private String departmentName;

    private List<Employees> employees;
}
