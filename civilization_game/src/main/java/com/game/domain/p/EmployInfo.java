package com.game.domain.p;

import java.util.HashMap;
import java.util.Map;

import com.game.pb.CommonPb;

// 内政官、科技官、武器大师
// 内政官开启是由主城开启
public class EmployInfo implements Cloneable {
    private Map<Integer, Employee> employeeMap = new HashMap<Integer, Employee>();
    // 当前内政官
    private int officerId;
    // 当前的武器大师
    private int blackSmithId;
    // 当前的研究员
    private int researcherId;

    public Map<Integer, Employee> getEmployeeMap() {
        return employeeMap;
    }

    @Override
    public EmployInfo clone() {
        EmployInfo employInfo = null;
        try {
            employInfo = (EmployInfo) super.clone();
            HashMap<Integer, Employee> map = new HashMap<>();
            this.employeeMap.forEach((key, value) -> {
                map.put(key, value);
            });
            employInfo.setEmployeeMap(map);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return employInfo;
    }

    public void addEmplyee(int employId, CommonPb.EmployeeInfo employeeInfo) {
        Employee employee = new Employee();
        employee.dserEmployee(employeeInfo);
        employeeMap.put(employId, employee);
    }

    public void setOfficerId(int officerId) {
        this.officerId = officerId;
    }

    public Employee getEmployee(int employeeId) {
        return employeeMap.get(employeeId);
    }

    public int getOfficerId() {
        return officerId;
    }

    //内政官
    public Employee getOfficer() {
        return employeeMap.get(officerId);
    }

    public long getOfficerTime() {
        Employee employee = getOfficer();
        if (employee == null) {
            return 0L;
        }
        return employee.getEndTime();
    }

    public int getBlackSmithId() {
        return blackSmithId;
    }

    public void setBlackSmithId(int blackSmithId) {
        this.blackSmithId = blackSmithId;
    }

    //武器大师
    public Employee getBlackSmith() {
        return employeeMap.get(blackSmithId);
    }

    // 武器大师时间
    public long getBlackSmithTime() {
        Employee employee = getBlackSmith();
        if (employee == null) {
            return 0L;
        }
        return employee.getEndTime();
    }

    public int getResearcherId() {
        return researcherId;
    }

    public void setResearcherId(int researcherId) {
        this.researcherId = researcherId;
    }

    public void setEmployeeMap(Map<Integer, Employee> employeeMap) {
        this.employeeMap = employeeMap;
    }

    // 研究员
    public Employee getResearcherId(int researcherId) {
        return employeeMap.get(researcherId);
    }

    // 研究员时间
    public long getResearcherTime() {
        int researchId = getResearcherId();
        Employee employee = getEmployee(researchId);
        if (employee == null) {
            return 0L;
        }
        return employee.getEndTime();
    }

    // 研究员
    public Employee getResearcher() {
        int researchId = getResearcherId();
        return employeeMap.get(researchId);
    }


}
