package com.luguosong.get_param.object_and_mapper.mapper;

import com.luguosong.get_param.pojo.Employees;

import java.util.HashMap;

/**
 * @author luguosong
 */
public interface EmployeesMapper {

    Employees getEmployeesByName1(Employees employees);

    Employees getEmployeesByName2(HashMap<String, String> searchMap);
}
