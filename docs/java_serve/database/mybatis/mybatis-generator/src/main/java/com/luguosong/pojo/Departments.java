package com.luguosong.pojo;

public class Departments {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column departments.id
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    private Integer id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column departments.department_name
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    private String departmentName;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column departments.id
     *
     * @return the value of departments.id
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column departments.id
     *
     * @param id the value for departments.id
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column departments.department_name
     *
     * @return the value of departments.department_name
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public String getDepartmentName() {
        return departmentName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column departments.department_name
     *
     * @param departmentName the value for departments.department_name
     *
     * @mbg.generated Tue Jul 23 13:46:29 CST 2024
     */
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName == null ? null : departmentName.trim();
    }
}