package de.dhbw.humbuch.util;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.junit.Test;

import au.com.bytecode.opencsv.CSVReader;
import de.dhbw.humbuch.model.GradeHandler;

public class CSVTest {
	
	@Test
	public void testCreateStudentObjectsFromCSV(){
		CSVReader csvReader;
		try {
			csvReader = new CSVReader(new FileReader("./src/test/java/de/dhbw/humbuch/util/schueler_stamm.csv"), ';', '\'', 0);
			ArrayList<de.dhbw.humbuch.model.entity.Student> list = CSVHandler.createStudentObjectsFromCSV(csvReader);
			assertEquals(99, list.size());		
			assertEquals("Zivko", list.get(1).getLastname());
			assertEquals("5a", GradeHandler.getFullGrade(list.get(1).getGrade()));
			assertEquals("Adelina", list.get(1).getFirstname());		
			DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
			assertEquals("02.01.1989", dateFormat.format(list.get(1).getBirthday()));
			assertEquals("m", list.get(1).getGender());
			//assertEquals("E", ProfileHandler.getLanguageProfile(list.get(1).getProfile()));
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
