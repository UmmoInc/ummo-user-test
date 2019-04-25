package xyz.ummo.user;

/*
The public service name is the department
 */
public class Department {

    private  String departmentName;

    public Department(String departmentName) {
        //
        this.departmentName = departmentName;
    }

    public String getDepartmentName() {

        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}
