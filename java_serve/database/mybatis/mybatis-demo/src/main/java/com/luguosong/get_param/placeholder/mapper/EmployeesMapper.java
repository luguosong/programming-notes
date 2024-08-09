package com.luguosong.get_param.placeholder.mapper;

import com.luguosong.get_param.pojo.Employees;

/**
 * @author luguosong
 */
public interface EmployeesMapper {

    public Employees getEmployeesById(Integer id);

    public Employees getEmployeesByName(String firstName);
}
