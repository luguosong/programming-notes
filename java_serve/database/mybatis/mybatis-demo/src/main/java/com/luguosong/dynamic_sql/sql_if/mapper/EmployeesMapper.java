package com.luguosong.dynamic_sql.sql_if.mapper;

import com.luguosong.dynamic_sql.pojo.Employees;

import java.util.List;

/**
 * @author luguosong
 */
public interface EmployeesMapper {
    List<Employees> selectEmployees(Employees e);
}
