package main.java.Employee.Employee_Payroll;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

public class EmployeePayrollService {
	Logger log = Logger.getLogger(EmployeePayrollService.class.getName());

	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	}

	private List<EmployeePayrollData> employeePayrollList;
	private Map<String, Double> genderToAverageSalaryMap;
	private EmployeePayrollDBService employeePayrollDBService;
	private EmployeePayrollDBServiceNew employeePayrollDBServiceNew;

	public EmployeePayrollService(List<EmployeePayrollData> employeePayrollList) {
		this();
		this.employeePayrollList = employeePayrollList;
	}

	public EmployeePayrollService() {
		employeePayrollDBService = EmployeePayrollDBService.getInstance();
		employeePayrollDBServiceNew = EmployeePayrollDBServiceNew.getInstance();
	}

	public static void main(String[] args) {
		System.out.println("Welcome to Employee Payroll Service");
		ArrayList<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		EmployeePayrollService employeePayroll = new EmployeePayrollService(employeePayrollList);
		Scanner consoleInputReader = new Scanner(System.in);
		employeePayroll.readEmployeePayrollData(consoleInputReader);
		employeePayroll.writeEmployeePayrollData(IOService.CONSOLE_IO);
	}

	private void readEmployeePayrollData(Scanner consoleInputReader) {
		System.out.println("Enter Employee ID: ");
		int id = consoleInputReader.nextInt();
		System.out.println("Enter Employee Name ");
		String name = consoleInputReader.next();
		System.out.println("Enter Employee Salary");
		Double salary = consoleInputReader.nextDouble();
		employeePayrollList.add(new EmployeePayrollData(id, name, salary));
	}

	public void writeEmployeePayrollData(IOService ioService) {
		if (ioService.equals(IOService.CONSOLE_IO))
			System.out.println("Employee Payroll Data " + employeePayrollList);
		else if (ioService.equals(IOService.FILE_IO)) {
			new EmployeePayrollFileIOService().writeData(employeePayrollList);
		}
	}

	public void printData(IOService fileIo) {
		if (fileIo.equals(IOService.FILE_IO)) {
			new EmployeePayrollFileIOService().printData();
		}
	}

	public long countEntries(IOService fileIo) {
		if (fileIo.equals(IOService.FILE_IO)) {
			return new EmployeePayrollFileIOService().countEntries();
		} else {
			this.employeePayrollList = employeePayrollDBService.readData();
			return employeePayrollList.size();
		}
	}

	public List<EmployeePayrollData> readPayrollData(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			this.employeePayrollList = new EmployeePayrollFileIOService().readData();
		return employeePayrollList;
	}

	public List<EmployeePayrollData> readEmployeePayrollData(IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollDBService.readData();
		return this.employeePayrollList;
	}

	public void updateEmployeeSalary(String name, double salary) throws PayrollSystemException {
		try {
			int result = employeePayrollDBService.updateEmployeeData(name, salary);
			if (result == 0) {
				throw new PayrollSystemException("no rows updated",
						PayrollSystemException.ExceptionType.UPDATE_FILE_EXCEPTION);
			}
			EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
			if (employeePayrollData != null)
				employeePayrollData.salary = salary;
		} catch (PayrollSystemException e) {
			System.out.println(e);
		}
	}

	private EmployeePayrollData getEmployeePayrollData(String name) {
		return this.employeePayrollList.stream().filter(empPayrollDataItem -> empPayrollDataItem.name.equals(name))
				.findFirst().orElse(null);
	}

	public boolean checkEmployeePayrollInSyncWithDB(String name) {
		List<EmployeePayrollData> employeePayrollDataList = employeePayrollDBService.getEmployeePayrollData(name);
		return employeePayrollDataList.get(0).equals(getEmployeePayrollData(name));
	}

	public List<EmployeePayrollData> readEmployeePayrollForDateRange(IOService ioService, LocalDate startDate,
			LocalDate endDate) {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollDBService.getEmployeeForDateRange(startDate, endDate);
		return employeePayrollList;
	}

	public Map<String, Double> getAvgSalary(IOService ioService) throws PayrollSystemException {
		try {
			if (ioService.equals(IOService.DB_IO))
				this.genderToAverageSalaryMap = employeePayrollDBService.getAverageSalaryByGender();
			if (genderToAverageSalaryMap.isEmpty()) {
				throw new PayrollSystemException("no data retrieved",
						PayrollSystemException.ExceptionType.RETRIEVE_EXCEPTION);
			}
		} catch (PayrollSystemException e) {
			System.out.println(e);
		}
		return genderToAverageSalaryMap;
	}

	public void addEmployeeToPayroll(String name, double salary, LocalDate joiningDate, char gender)
			throws PayrollSystemException {
		employeePayrollList.add(employeePayrollDBServiceNew.addEmployeeToPayroll(name, salary, joiningDate, gender));
	}

	public void addEmployeesToPayroll(List<EmployeePayrollData> employeePayrollDataList) {
		employeePayrollDataList.forEach(employeePayrollData -> {
			log.info("Employee being added : " + employeePayrollData.name);
			try {
				this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.salary,
						employeePayrollData.startDate, employeePayrollData.gender);
			} catch (PayrollSystemException e) {
				e.printStackTrace();
			}
			log.info("Employee added : " + employeePayrollData.name);
		});
		log.info(" " + this.employeePayrollList);
	}

	public void addEmployeesToPayrollUsingThreads(List<EmployeePayrollData> employeePayrollDataList) {
		Map<Integer, Boolean> employeeAdditionStatus = new HashMap<>();
		employeePayrollDataList.forEach(employeePayrollData -> {
			Runnable task = () -> {
				employeeAdditionStatus.put(employeePayrollData.hashCode(), false);
				log.info("Employee being added: " + Thread.currentThread().getName());
				try {
					this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.salary,
							employeePayrollData.startDate, employeePayrollData.gender);
				} catch (PayrollSystemException e) {
					e.printStackTrace();
				}
				employeeAdditionStatus.put(employeePayrollData.hashCode(), true);
				log.info("Employee added: " + Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, employeePayrollData.name);
			thread.start();
		});
		while (employeeAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		log.info(" " + this.employeePayrollList);
	}

	public int removeEmployeeFromPayroll(String name, IOService ioService) {
		int employeeCount = 0;
		if (ioService.equals(IOService.DB_IO))
			employeeCount = employeePayrollDBServiceNew.removeEmployee(name);
		return employeeCount;
	}

	public List<EmployeePayrollData> readActiveEmployeePayrollData(IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollDBService.readActiveEmployeeData();
		return this.employeePayrollList;
	}

	public void updateEmployeeToPayroll(List<EmployeePayrollData> employeePayrollDataList) {
		Map<Integer, Boolean> employeeAdditionStatus = new HashMap<Integer, Boolean>();
		employeePayrollDataList.forEach(employeePayrollData -> {
			Runnable task = () -> {
				employeeAdditionStatus.put(employeePayrollData.hashCode(), false);
				log.info("Employee being updated: " + Thread.currentThread().getName());
				try {
					this.updateEmployeeSalary(employeePayrollData.name, employeePayrollData.salary);
				} catch (PayrollSystemException e) {
					e.printStackTrace();
				}
				employeeAdditionStatus.put(employeePayrollData.hashCode(), true);
				log.info("Employee updated: " + Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, employeePayrollData.name);
			thread.start();
		});
		while (employeeAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		log.info(" " + this.employeePayrollList);

	}
}