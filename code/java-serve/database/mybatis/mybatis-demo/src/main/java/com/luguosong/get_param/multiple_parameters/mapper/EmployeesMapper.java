package com.luguosong.get_param.multiple_parameters.mapper;

import com.luguosong.get_param.pojo.Employees;

/**
 * @author luguosong
 */
public interface EmployeesMapper {

    Employees getEmployeesByName1(String firstName, String lastName);

    Employees getEmployeesByName2(String firstName, String lastName);
}
