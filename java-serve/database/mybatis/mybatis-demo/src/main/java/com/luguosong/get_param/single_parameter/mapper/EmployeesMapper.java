package com.luguosong.get_param.single_parameter.mapper;

import com.luguosong.get_param.pojo.Employees;

/**
 * @author luguosong
 */
public interface EmployeesMapper {

    public Employees getEmployeesById(Integer id);

    public Employees getEmployeesByName(String firstName);
}
