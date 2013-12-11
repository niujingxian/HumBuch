package de.dhbw.humbuch.pdfExport;

import java.util.Iterator;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import de.dhbw.humbuch.model.GradeHandler;
import de.dhbw.humbuch.model.MapperAmountAndBorrowedMaterial;
import de.dhbw.humbuch.model.entity.Grade;


public final class MyPDFClassList extends MyPDFHandler {

	private Grade grade;

	//object student has to be replaced by a class object
	public MyPDFClassList(Grade grade) {
		super();
		this.grade = grade;
	}

	protected void insertDocumentParts(Document document) {
		this.addHeading(document, "Ausgabe-Liste 2013");
		this.addContent(document);
	}

	protected void addContent(Document document) {
		PdfPTable table = this.createTableWithRentalInformationHeader();

		List<MapperAmountAndBorrowedMaterial> gradeRentalList = GradeHandler.getAllRentedBooksOfGrade(this.grade);

		Iterator<MapperAmountAndBorrowedMaterial> iterator = gradeRentalList.iterator();
		MapperAmountAndBorrowedMaterial gradeRental;
		while (iterator.hasNext()) {
			gradeRental = iterator.next();
			String[] contentArray = {gradeRental.getBorrowedMaterial().getTeachingMaterial().getSubject().getName(),
			                         ""+gradeRental.getBorrowedMaterial().getTeachingMaterial().getToGrade(),
			                         gradeRental.getBorrowedMaterial().getTeachingMaterial().getName(),
			                         ""+gradeRental.getAmount()};
			MyPDFHandler.fillTableWithContent(table, true, contentArray);
		}

		try {
			document.add(table);
		}
		catch (DocumentException e) {
			e.printStackTrace();
		}
	}
}