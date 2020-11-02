package main.java.Employee.Employee_Payroll;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

public class DBConnect {

	public static void main(String[] args) {
	String jdbcURL = "jdbc:mysql://localhost:3307/payroll_service?useSSL=false";
	String username = "root";
	String password = "nayan@1965";
	
	Connection connection;
	
	
	try {
		Class.forName("com.mysql.jdbc.Driver");
		System.out.println("Driver Loaded!");	
	} 
	
	catch(ClassNotFoundException e) {
		throw new IllegalStateException("Cannot find the driver in the class path ", e);
	}
	
	listDrivers();
	
	try {
		System.out.println("Connecting to Database: "+jdbcURL);
		connection = DriverManager.getConnection(jdbcURL,username,password);
		System.out.println("Connection is Successful!! "+connection);
		
	}
	catch(Exception e) {
		e.printStackTrace();
	}
	
	
	}
	
	public static void listDrivers() {
		Enumeration<Driver> driverList = DriverManager.getDrivers();
		while(driverList.hasMoreElements()) {
			Driver driverClass = (Driver) driverList.nextElement();
			System.out.println(" "+driverClass.getClass().getName());
		}
	}
	
}