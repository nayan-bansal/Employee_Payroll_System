package main.java.Employee.Employee_Payroll;

import java.io.File;

public class FileUtils {

	public static boolean deleteFiles(File deleteContents) {
		File[] allContents = deleteContents.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteFiles(file);
			}
		}
		return deleteContents.delete();
	}
	
}
