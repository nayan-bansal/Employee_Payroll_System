package main.java.Employee.Employee_Payroll;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class EmployeePayrollDBService {
	private PreparedStatement employeePayrollDataStatement;
	private static EmployeePayrollDBService employeePayrollDBService;

	private EmployeePayrollDBService() {
	}

	public static EmployeePayrollDBService getInstance() {
		if (employeePayrollDBService == null) {
			employeePayrollDBService = new EmployeePayrollDBService();
		}
		return employeePayrollDBService;
	}

	public static Connection getConnection() throws SQLException {
		String jdbcURL = "jdbc:mysql://localhost:3307/payroll_service?useSSL=false";
		String userName = "root";
		String password = "nayan@1965";
		Connection connection;
		System.out.println("connecting to database: " + jdbcURL);
		connection = DriverManager.getConnection(jdbcURL, userName, password);
		System.out.println("connection successful !!!! " + connection);
		return connection;
	}

	public List<EmployeePayrollData> readData() {
		String sql = "SELECT e.id,e.name,e.start,e.gender,e.salary, d.dept_name from employee_payroll e inner join "
				+ "emp_dept ed on e.id=ed.id inner join department d on ed.dept_id=d.dept_id; ";
		return this.getEmployeePayrollDataUsingQuery(sql);
	}

	private List<EmployeePayrollData> getEmployeePayrollDataUsingQuery(String sql) {
		List<EmployeePayrollData> employeePayrollList=null;
		try (Connection connection = EmployeePayrollDBService.getConnection();){
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			ResultSet result=preparedStatement.executeQuery(sql);
			employeePayrollList=this.getEmployeePayrollData(result);
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	public int updateEmployeeData(String name, double salary) {
		return this.updateEmployeeDataUsingPreparedStatement(name, salary);
	}

	public int updateEmployeeDataUsingPreparedStatement(String name, double salary) {
		try (Connection connection = EmployeePayrollDBService.getConnection();) {
			String sql = "update employee_payroll set salary=? where name=?";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setDouble(1, salary);
			preparedStatement.setString(2, name);
			int status = preparedStatement.executeUpdate();
			return status;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public List<EmployeePayrollData> getEmployeePayrollData(String name) {
		List<EmployeePayrollData> employeeParollList = null;
		if (this.employeePayrollDataStatement == null)
			this.prepareStatementForEmployeeData();
		try {
			employeePayrollDataStatement.setString(1, name);
			ResultSet resultSet = employeePayrollDataStatement.executeQuery();
			employeeParollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeeParollList;
	}

	private List<EmployeePayrollData> getEmployeePayrollData(ResultSet result) {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		List<String> departmentName=new ArrayList<>();
		try {
			while (result.next()) {
				int id = result.getInt("id");
				String name = result.getString("name");
				double Salary = result.getDouble("salary");
				LocalDate startDate = result.getDate("start").toLocalDate();
				char gender=result.getString("gender").charAt(0);
				String dept=result.getString("dept_name");
				departmentName.add(dept);
				String[] deptArray=new String[departmentName.size()];
				employeePayrollList.add(new EmployeePayrollData(id, name, Salary, startDate,gender,departmentName.toArray(deptArray)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private void prepareStatementForEmployeeData() {
		try {
			Connection connection = EmployeePayrollDBService.getConnection();
			String sql = "SELECT e.id,e.name,e.start,e.gender,e.salary, d.dept_name from employee_payroll e inner join "
					+ "emp_dept ed on e.id=ed.id inner join department d on ed.dept_id=d.dept_id WHERE name=?";
			employeePayrollDataStatement = connection.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<EmployeePayrollData> getEmployeeForDateRange(LocalDate startDateTime, LocalDate endDateTime) {
		String sql = String.format("SELECT e.id,e.name,e.start,e.gender,e.salary, d.dept_name from employee_payroll e inner join "
				+ "emp_dept ed on e.id=ed.id inner join department d on ed.dept_id=d.dept_id where start between '%s' AND '%s';",
				Date.valueOf(startDateTime), Date.valueOf(endDateTime));
		return this.getEmployeePayrollDataUsingDB(sql);
	}

	private List<EmployeePayrollData> getEmployeePayrollDataUsingDB(String sql) {
		ResultSet result;
		List<EmployeePayrollData> employeePayrollList = null;
		try (Connection connection = EmployeePayrollDBService.getConnection()) {
			Statement statement = connection.createStatement();
			result = statement.executeQuery(sql);
			employeePayrollList = this.getEmployeePayrollData(result);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	public Map<String, Double> getAverageSalaryByGender() {
		String sql = "SELECT gender,AVG(salary) as avg_salary FROM employee_payroll group by gender;";
		Map<String, Double> genderToAverageSalaryMap = new HashMap<>();
		try (Connection connection = EmployeePayrollDBService.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);
			while (result.next()) {
				String gender = result.getString("gender");
				double salary = result.getDouble("avg_salary");
				genderToAverageSalaryMap.put(gender, salary);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return genderToAverageSalaryMap;
	}
}
