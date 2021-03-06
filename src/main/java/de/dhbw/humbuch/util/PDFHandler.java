package de.dhbw.humbuch.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.vaadin.server.StreamResource.StreamSource;


public abstract class PDFHandler {

	private Document document;
	private HeaderFooter event;
	protected static float TABLEWIDTH = 418f;

	/**
	 * 
	 * @param path
	 *            links to the directory where the PDF file should be saved
	 */
	public PDFHandler(String path) {
		this.document = new Document();
	}

	public PDFHandler() {
		this.document = new Document();
	}

	/**
	 * Creates the pdf with the information in the object that was passed to the
	 * constructor previously.
	 * 
	 * @param path
	 *            where the file will be saved
	 */
	public void savePDF(String path) {
		try {
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path));
			event = new HeaderFooter();
			writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
			writer.setPageEvent(event);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (DocumentException e) {
			e.printStackTrace();
		}
		this.document.open();
		this.addMetaData(document);
		this.insertDocumentParts(document);
		this.document.close();
	}

	/**
	 * User can choose a printer where this pdf is printed then. The pdf
	 * contains the information stored in the object that was send to the
	 * constructor previously.
	 * 
	 */
	public void printPDF() {
		ByteArrayOutputStream byteArrayOutputStream;
		try {
			byteArrayOutputStream = new ByteArrayOutputStream();
			PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
			event = new HeaderFooter();
			writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
			writer.setPageEvent(event);

			this.document.open();
			this.addMetaData(document);
			this.insertDocumentParts(document);
			this.document.close();

			new PDFPrinter(byteArrayOutputStream);
		}
		catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a ByteArrayOutputStream which contains the PDF as a byte array.
	 * 
	 * @return the byteArrayOutputStream the PDF is stored in, null if an error
	 *         occurred.
	 */
	public ByteArrayOutputStream createByteArrayOutputStreamForPDF() {
		ByteArrayOutputStream byteArrayOutputStream;
		try {
			byteArrayOutputStream = new ByteArrayOutputStream();
			PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
			event = new HeaderFooter();
			writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
			writer.setPageEvent(event);

			this.document.open();
			this.addMetaData(document);
			int initialDocumentSize = writer.getCurrentDocumentSize();
			this.insertDocumentParts(document);

			if (byteArrayOutputStream.size() > 0 || writer.getCurrentDocumentSize() > initialDocumentSize) {
				this.document.close();
			}
			else {
				return null;
			}

			return byteArrayOutputStream;
		}
		catch (DocumentException e) {
			System.err.println("Could not create ByteArrayOutputStream of PDF data. " + e.getMessage());
		}

		return null;
	}

	/**
	 * Adds meta data to the PDF document. The information of using iText must
	 * be part of the meta data due to the license of the iText library!
	 * 
	 * @param document
	 *            represents the PDF before it is saved
	 */
	private void addMetaData(Document document) {
		document.addTitle("Humbuch Schule");
		document.addSubject("Using iText");
		document.addKeywords("Java, PDF, iText");
		document.addAuthor("Schlager");
		document.addCreator("Schlager");
	}

	/**
	 * Set the logo of Humboldt on the left corner and the current date on the
	 * right corner
	 * 
	 * @param document
	 *            reference of the pdfDocument object
	 */
	protected void addHeading(Document document) {
		Paragraph paragraph = new Paragraph();
		PdfPTable table = createMyStandardTable(2);

		table.setTotalWidth(TABLEWIDTH);
		PdfPCell cell;

		try {
			Image img = Image.getInstance("./res/Logo_Humboldt_Gym_70_klein_3.png");
			img.setAlignment(Element.ALIGN_BOTTOM);
			cell = new PdfPCell(img);

			cell.setBorder(0);
			table.addCell(cell);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (BadElementException e) {
			e.printStackTrace();
		}

		String date = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN).format(Calendar.getInstance().getTime());

		cell = new PdfPCell(new Phrase(date));
		cell.setBorder(0);
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase(""));
		cell.setBorder(Rectangle.BOTTOM);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(""));
		cell.setBorder(Rectangle.BOTTOM);
		table.addCell(cell);

		paragraph.add(table);
		addEmptyLine(paragraph, 1);

		try {
			document.add(paragraph);
		}
		catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds a signature field with a date field to the document. Should be the
	 * last part that is added to the document.
	 * 
	 * @param document
	 *            represents the PDF before it is saved
	 * @param role
	 *            word for the kind of person that shall sign the paper
	 */
	protected void addSignatureField(Document document, String role) {
		Paragraph paragraph = new Paragraph();
		//addEmptyLine(paragraph, 1);

		//this table contains the signatureTable and the dataTable.
		// this purpose makes it easier to format
		PdfPTable table = new PdfPTable(2);

		//the first column is double times greater than the second column
		try {
			table.setWidths(new float[] { 10f, 20f });
		}
		catch (DocumentException e) {
			e.printStackTrace();
		}

		//create and fill date table
		PdfPTable dateTable = new PdfPTable(1);

		PdfPCell cell = new PdfPCell(new Phrase(""));
		//just the bottom border will be displayed (line for date)
		cell.setBorderWidthTop(0);
		cell.setBorderWidthLeft(0);
		cell.setBorderWidthRight(0);

		dateTable.addCell(cell);

		cell = new PdfPCell(new Phrase("Datum"));
		cell.setBorder(0);

		dateTable.addCell(cell);

		//put date table into the 'parent' table
		cell = new PdfPCell(dateTable);
		cell.setBorder(0);
		table.addCell(cell);

		//create and fill signature table
		PdfPTable signatureTable = new PdfPTable(1);
		cell = new PdfPCell(new Phrase(""));
		//just the bottom border will be displayed (line for signature)
		cell.setBorderWidthTop(0);
		cell.setBorderWidthLeft(0);
		cell.setBorderWidthRight(0);

		signatureTable.addCell(cell);

		cell = new PdfPCell(new Phrase("Unterschrift " + role));
		cell.setBorder(0);

		signatureTable.addCell(cell);

		//put signature table into the 'parent' table
		cell = new PdfPCell(signatureTable);
		cell.setBorder(0);
		table.addCell(cell);

		paragraph.add(table);
		try {
			document.add(paragraph);
		}
		catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * A table is generated with the header: Klasse, Bezeichnung Lehrmittel,
	 * Unterschrift
	 * 
	 * @return PdfPTable
	 */
	protected PdfPTable createTableWithRentalInformationHeader() {
		PdfPTable table = createMyStandardTable(3, new float[] { 3f, 1f, 1f });
		Font font = FontFactory.getFont("Helvetica", 12, Font.BOLD);
		new PDFHandler.TableBuilder(table, new String[] { "Bezeichnung Lehrmittel", "bis Klasse", "Unterschrift" }).withBorder(true)
				.isCenterAligned(true).font(font).fillTable();

		return table;
	}

	/**
	 * A table is generated with the header: Klasse, Bezeichnung Lehrmittel,
	 * Anzahl
	 * 
	 * @return PdfPTable
	 */
	protected PdfPTable createTableWithRentalInformationHeaderForClass() {
		PdfPTable table = createMyStandardTable(2, new float[] { 3f, 1f });
		Font font = FontFactory.getFont("Helvetica", 12, Font.BOLD);
		new PDFHandler.TableBuilder(table, new String[] { "Bezeichnung Lehrmittel", "Anzahl" }).withBorder(true).font(font).fillTable();

		return table;
	}

	protected void addInformationAboutDocument(Document document, String informationText) {
		PdfPTable table = createMyStandardTable(1);
		new PDFHandler.TableBuilder(table, new String[] { informationText })
				.font(FontFactory.getFont("Times New Roman", 14, Font.BOLD)).fillTable();
		try {
			document.add(table);
			PDFHandler.addEmptyLineToDocument(document, 1);
		}
		catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	protected void resetPageNumber() {
		this.event.resetPageNumber();
	}

	protected static void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}

	protected static void addEmptyLineToDocument(Document document, int number) {
		Paragraph paragraph = new Paragraph();
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}

		try {
			document.add(paragraph);
		}
		catch (DocumentException e) {
			System.err.println("Could not add empty line to documnet " + e.getStackTrace());
		}
	}

	/**
	 * Create a standard table with constant table width.
	 * 
	 * @param columnNumber
	 *            set how many columns the table will have
	 * @return table
	 */
	protected static PdfPTable createMyStandardTable(int columnNumber) {
		PdfPTable table = new PdfPTable(columnNumber);
		table.setLockedWidth(true);
		table.setTotalWidth(TABLEWIDTH);

		return table;
	}

	/**
	 * Create a standard table with constant table width.
	 * 
	 * @param columnNumber
	 *            set how many columns the table will have
	 * @param columnWidths
	 *            set the ratio between the columns. If null, all columns will
	 *            be equal.
	 * @return table
	 */
	protected static PdfPTable createMyStandardTable(int columnNumber, float[] columnWidths) {
		PdfPTable table = new PdfPTable(columnNumber);
		table.setLockedWidth(true);
		table.setTotalWidth(TABLEWIDTH);

		if (!(columnWidths == null) && (columnWidths.length != 0)) {
			try {
				table.setWidths(columnWidths);
			}
			catch (DocumentException e) {
				System.err.println("Could not set columnWidths of standardTable " + e.getStackTrace());
			}
		}

		return table;
	}

	/**
	 * Convenience method to add content to a table in a standard way
	 * 
	 * @param table
	 * @param withBorder
	 *            if true a standard border is used, if false no border is used
	 * @param contentArray
	 *            an array with all cell contents
	 * @param isAlignedCenter
	 *            if true the content is horizontally and vertically aligned
	 */
	@Deprecated
	protected static void fillTableWithContent(PdfPTable table, boolean withBorder, String[] contentArray, boolean isAlignedCenter) {
		PdfPCell cell = null;

		for (int i = 0; i < contentArray.length; i++) {
			//append '\n' to each String to have an empty space-line before cell ends
			cell = new PdfPCell(new Phrase(contentArray[i] + "\n  "));
			cell.setLeading(1.25f, 1.25f);
			if (isAlignedCenter) {
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			}

			if (withBorder == false) {
				cell.setBorder(0);
			}
			cell.setPadding(0f);
			table.addCell(cell);
		}
	}

	/**
	 * Convenience method to add content to a table in a standard way
	 * 
	 * @param table
	 * @param withBorder
	 *            if true a standard border is used, if false no border is used
	 * @param contentArray
	 *            an array with all cell contents
	 * @param font
	 *            set a font for the cell content
	 */
	@Deprecated
	protected static void fillTableWithContent(PdfPTable table, boolean withBorder, String[] contentArray, Font font) {
		PdfPCell cell = null;

		for (int i = 0; i < contentArray.length; i++) {
			//append '\n' to each String to have an empty space-line before cell ends
			cell = new PdfPCell(new Phrase(contentArray[i] + "\n  ", font));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			if (withBorder == false) {
				cell.setBorder(0);
			}
			table.addCell(cell);
		}
	}

	/**
	 * Convenience method to add content to a table in a standard way
	 * 
	 * @param table
	 * @param withBorder
	 *            if true a standard border is used, if false no border is used
	 * @param contentArray
	 *            an array with all cell contents
	 * @param isAlignedCenter
	 *            if true the content is horizontally and vertically aligned
	 */
	@Deprecated
	protected static void fillTableWithContent(PdfPTable table, boolean withBorder, String[] contentArray, boolean isAlignedCenter, Font font) {
		PdfPCell cell = null;

		for (int i = 0; i < contentArray.length; i++) {
			//append '\n' to each String to have an empty space-line before cell ends
			cell = new PdfPCell(new Phrase(contentArray[i] + "\n  ", font));
			cell.setLeading(1.25f, 1.25f);
			if (isAlignedCenter) {
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			}

			if (withBorder == false) {
				cell.setBorder(0);
			}
			cell.setPadding(0f);
			table.addCell(cell);
		}
	}

	/**
	 * Convenience method to add content to a table in a standard way
	 * 
	 * @param table
	 * @param withBorder
	 *            if true a standard border is used, if false no border is used
	 * @param contentArray
	 *            an array with all cell contents
	 * @param font
	 *            set a font for the cell content
	 */
	@Deprecated
	protected static void fillTableWithContentWithoutAlignment(PdfPTable table, boolean withBorder, String[] contentArray, Font font) {
		PdfPCell cell = null;

		for (int i = 0; i < contentArray.length; i++) {
			//append '\n' to each String to have an empty space-line before cell ends
			cell = new PdfPCell(new Phrase(contentArray[i] + "\n  ", font));
			if (withBorder == false) {
				cell.setBorder(0);
			}
			table.addCell(cell);
		}
	}

	/**
	 * Cell entry is not followed by \n
	 * 
	 * @param table
	 * @param withBorder
	 *            if true a standard border is used, if false no border is used
	 * @param contentArray
	 *            an array with all cell contents
	 */
	@Deprecated
	protected static void fillTableWithContentWithoutSpace(PdfPTable table, boolean withBorder,
			String[] contentArray, boolean isAlignedCenter, float padding) {
		PdfPCell cell = null;

		for (int i = 0; i < contentArray.length; i++) {

			cell = new PdfPCell(new Phrase(contentArray[i]));
			if (isAlignedCenter) {
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			}
			if (withBorder == false) {
				cell.setBorder(0);
			}
			cell.setPadding(padding);
			table.addCell(cell);
		}
	}

	private static void fillTableWithContent(TableBuilder tableBuilder) {
		PdfPCell cell = null;
		for (int i = 0; i < tableBuilder.contentArray.length; i++) {

			if (tableBuilder.font != null) {
				cell = new PdfPCell(new Phrase(tableBuilder.contentArray[i], tableBuilder.font));
			}
			else {
				cell = new PdfPCell(new Phrase(tableBuilder.contentArray[i]));
			}
			if (tableBuilder.isAlignedCentrally) {
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			}
			if (!tableBuilder.withBorder) {
				cell.setBorder(0);
			}
			if (tableBuilder.padding != 0f) {
				cell.setPadding(tableBuilder.padding);
			}
			if (tableBuilder.leading != 0f) {
				cell.setLeading(tableBuilder.leading, tableBuilder.leading);
			}
			tableBuilder.table.addCell(cell);
		}
	}

	/**
	 * In this method all parts of the document shall be 'put' together.
	 * 
	 * @param document
	 *            represents the PDF before it is saved
	 */
	protected abstract void insertDocumentParts(Document document);

	/**
	 * In this method the PDF-specific information shall be inserted into the
	 * document.
	 * 
	 * @param document
	 *            represents the PDF before it is saved
	 */
	protected abstract void addContent(Document document);


	class TableBuilder {

		private PdfPTable table;
		private String[] contentArray;
		private boolean withBorder;
		private boolean isAlignedCentrally;
		private float padding;
		private Font font;
		private float leading;

		public TableBuilder(PdfPTable table, String[] contentArray) {
			this.table = table;
			this.contentArray = contentArray;
		}

		public TableBuilder withBorder(boolean withBorder) {
			this.withBorder = withBorder;
			return this;
		}

		public TableBuilder isCenterAligned(boolean isCenterAligned) {
			this.isAlignedCentrally = isCenterAligned;
			return this;
		}

		public TableBuilder padding(float padding) {
			this.padding = padding;
			return this;
		}

		public TableBuilder font(Font font) {
			this.font = font;
			return this;
		}

		public TableBuilder leading(float leading) {
			this.leading = leading;
			return this;
		}

		public void fillTable() {
			PDFHandler.fillTableWithContent(this);
		}
	}


	/** Inner class to add a header and a footer. */
	class HeaderFooter extends PdfPageEventHelper {

		/** Alternating phrase for the header. */
		Phrase[] header = new Phrase[2];
		/** Current page number (will be reset for every chapter). */
		int pagenumber;

		/**
		 * Initialize one of the headers.
		 * 
		 * @see com.itextpdf.text.pdf.PdfPageEventHelper#onOpenDocument(com.itextpdf.text.pdf.PdfWriter,
		 *      com.itextpdf.text.Document)
		 */
		public void onOpenDocument(PdfWriter writer, Document document) {
			header[0] = new Phrase("Movie history");
		}

		/**
		 * Initialize one of the headers, based on the chapter title; reset the
		 * page number.
		 * 
		 * @see com.itextpdf.text.pdf.PdfPageEventHelper#onChapter(com.itextpdf.text.pdf.PdfWriter,
		 *      com.itextpdf.text.Document, float, com.itextpdf.text.Paragraph)
		 */
		public void onChapter(PdfWriter writer, Document document,
				float paragraphPosition, Paragraph title) {
			header[1] = new Phrase(title.getContent());
			pagenumber = 1;
		}

		/**
		 * Increase the page number.
		 * 
		 * @see com.itextpdf.text.pdf.PdfPageEventHelper#onStartPage(com.itextpdf.text.pdf.PdfWriter,
		 *      com.itextpdf.text.Document)
		 */
		public void onStartPage(PdfWriter writer, Document document) {
			pagenumber++;
		}

		/**
		 * Adds the header and the footer.
		 * 
		 * @see com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(com.itextpdf.text.pdf.PdfWriter,
		 *      com.itextpdf.text.Document)
		 */
		public void onEndPage(PdfWriter writer, Document document) {
			Rectangle rect = writer.getBoxSize("art");
			//            switch(writer.getPageNumber() % 2) {
			//            case 0:
			//                ColumnText.showTextAligned(writer.getDirectContent(),
			//                        Element.ALIGN_RIGHT, header[0],
			//                        rect.getRight(), rect.getTop(), 0);
			//                break;
			//            case 1:
			//                ColumnText.showTextAligned(writer.getDirectContent(),
			//                        Element.ALIGN_LEFT, header[0],
			//                        rect.getLeft(), rect.getTop(), 0);
			//                break;
			//            }
			ColumnText.showTextAligned(writer.getDirectContent(),
					Element.ALIGN_CENTER, new Phrase(String.format("- Seite %d -", pagenumber)),
					(rect.getLeft() + rect.getRight()) / 2, rect.getBottom() - 18, 0);
		}

		public void resetPageNumber() {
			this.pagenumber = 1;
		}
	}


	public static class PDFStreamSource implements StreamSource {

		private static final long serialVersionUID = 1L;
		ByteArrayOutputStream byteArrayOutputstream;

		public PDFStreamSource(ByteArrayOutputStream byteArrayOutputStream) {
			this.byteArrayOutputstream = byteArrayOutputStream;
		}

		@Override
		public InputStream getStream() {
			// Here we return the pdf contents as a byte-array
			return new ByteArrayInputStream(this.byteArrayOutputstream.toByteArray());
		}
	}
}