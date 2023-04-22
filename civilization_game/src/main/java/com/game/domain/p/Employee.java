package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.CommonPb.EmployeeInfo;

// 发给客户端是剩余时间，序列化是结束时间
public class Employee implements Cloneable {
    private int employeeId;
    private int useTimes;
    private long endTime;

    @Override
    public Employee clone() {
        Employee employee = null;
        try {
            employee = (Employee) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return employee;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getUseTimes() {
        return useTimes;
    }

    public void setUseTimes(int useTimes) {
        this.useTimes = useTimes;
    }

    public EmployeeInfo.Builder serEmployee() {
        EmployeeInfo.Builder builder = EmployeeInfo.newBuilder();
        builder.setEmployeeId(employeeId);
        builder.setEndTime(endTime);
        builder.setFreeTimes(useTimes);
        return builder;
    }

    public void dserEmployee(EmployeeInfo builder) {
        employeeId = builder.getEmployeeId();
        endTime = builder.getEndTime();
        useTimes = builder.getFreeTimes();
    }

    public boolean hasFreeTimes() {
        return getUseTimes() > 0;
    }

    public CommonPb.Employee.Builder wrapPb() {
        CommonPb.Employee.Builder builder = CommonPb.Employee.newBuilder();
        builder.setEmployeeId(employeeId);
        builder.setLeftTime(endTime);
        builder.setUseTimes(useTimes);
        return builder;
    }


    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
