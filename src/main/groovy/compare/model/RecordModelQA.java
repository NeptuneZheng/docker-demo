package compare.model;

import compare.configure.CSB2BEDIConfigQA;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.File;
import java.util.Date;


public class RecordModelQA {
	
	private int row;
	private XSSFSheet sheet;
	private String runFlag;  //0
	private String tpWorkingFolder;  //1
	private String inputFile;  //2
	private String expectedStatus; //3
	private String expectedFile; //4
	private String expectedFail;  //5
	private String actualStatus;  //6
	private String actualFile;  //7
	private String actualFail;  //8
	private String statusResult;  //9
	private String fileResult;  //10
	private String dbresult;  //11
	private File excel;
	private String inputFilePath;

	private String expectedFilePath;
	private String actualFilePath;
	private String dbFilePath;


	private String e2eExpectedFilePath;
	private String e2eActualFilePath;


	private String inMsgResID;
	private String outMsgResID;



	private String cs2ExtraFilePath;

	private boolean outPutFlag=false;

	private String bookingNumber;


	private String oldBookingNumber;

	public RecordModelQA(XSSFSheet sheet, int rowNum) {
		this.sheet = sheet;
		this.row = rowNum;
		this.runFlag = this.getString(this.sheet.getRow(this.row).getCell(0));
	//	this.tpWorkingFolder = this.getString(this.sheet.getRow(this.row).getCell(1));
		this.inputFile = this.getString(this.sheet.getRow(this.row).getCell(1));
		this.expectedStatus = this.getString(this.sheet.getRow(this.row).getCell(2));
		this.expectedFile = this.getString(this.sheet.getRow(this.row).getCell(3));
		this.expectedFail = this.getString(this.sheet.getRow(this.row).getCell(4));
		if(!StringUtils.isEmpty(this.expectedFile))
			this.expectedFilePath = CSB2BEDIConfigQA.ExpectedCompleteFolder + "/"+this.expectedFile;
		if(!StringUtils.isEmpty(this.inputFile))
			this.inputFilePath = CSB2BEDIConfigQA.InputDataFolder+ "/"+this.inputFile;
		if(!StringUtils.isEmpty(this.expectedFail))
			this.dbFilePath = CSB2BEDIConfigQA.ExpectedFailFolder+"/"+this.expectedFail;

        this.e2eExpectedFilePath = CSB2BEDIConfigQA.E2EExpectedCompleteFolder + "/"+this.expectedFile;
		this.e2eActualFilePath = CSB2BEDIConfigQA.E2EActualCompleteFolder + "/"+this.inputFile;

		this.cs2ExtraFilePath = CSB2BEDIConfigQA.CS2ExtraFolder + "/"+this.inputFile;


		if (!StringUtils.isEmpty(this.runFlag)) {
			this.setActualStatus("");
			this.setActualFile("");
			this.setActualFail("");
			this.setStatusResult("");
			this.setFileResult("");
			this.setDbresult("");
			this.setOutMsgResID("");
			this.setBookingNumber("");
			this.setInMsgResID("");
		}

	}
	public String getString(XSSFCell cell1) {
		String cell1value = "";
		if (cell1 != null) {

			switch (cell1.getCellType()) {

			case XSSFCell.CELL_TYPE_NUMERIC: {

				if (HSSFDateUtil.isCellDateFormatted(cell1)) {

					Date date = cell1.getDateCellValue();

					cell1value = date.toString();
				}

				else {

					Integer num = new Integer((int) cell1.getNumericCellValue());
					cell1value = String.valueOf(num);
				}
				break;
			}

			case XSSFCell.CELL_TYPE_STRING:

				cell1value = cell1.getStringCellValue();
				break;

			default:
				cell1value = "";
			}
		} else {
			cell1value = "";
		}
		return cell1value.trim();
	}
	
	public File getExcel() {
		return excel;
	}

	public void setExcel(File excel) {
		this.excel = excel;
	}
	
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public XSSFSheet getSheet() {
		return sheet;
	}
	public void setSheet(XSSFSheet sheet) {
		this.sheet = sheet;
	}
	public String getRunFlag() {
		return runFlag;
	}
	public void setRunFlag(String runFlag) {
		this.runFlag = runFlag;
	}
	public String getTpWorkingFolder() {
		return tpWorkingFolder;
	}
	public void setTpWorkingFolder(String tpWorkingFolder) {
		this.tpWorkingFolder = tpWorkingFolder;
	}
	public String getInputFile() {
		return inputFile;
	}
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}
	public String getExpectedFile() {
		return expectedFile;
	}
	public void setExpectedFile(String expectedFile) {
		this.expectedFile = expectedFile;
	}
	
		
	public String getExpectedStatus() {
		return expectedStatus;
	}
	public void setExpectedStatus(String expectedStatus) {
		this.expectedStatus = expectedStatus;
	}
	public String getExpectedFail() {
		return expectedFail;
	}
	public void setExpectedDB(String expectedFail) {
		this.expectedFail = expectedFail;
	}
	
	public String getActualStatus() {
		return actualStatus;
	}
	public void setActualStatus(String actualStatus) {
		this.actualStatus = actualStatus;
		/*XSSFCell cellactualStatus = sheet.getRow(row).createCell(6);
		cellactualStatus.setCellValue(this.actualStatus);*/
	}
	
	public String getActualFile() {
		return actualFile;
	}
	public void setActualFile(String actualFile) {
		this.actualFile = actualFile;
		/*XSSFCell cellactualFile = sheet.getRow(row).createCell(7);
		cellactualFile.setCellValue(this.actualFile);*/
	}

	public String getActualFail() {
		return actualFail;
	}
	public void setActualFail(String actualFail) {
		this.actualFail = actualFail;
		/*XSSFCell cellactualDB = sheet.getRow(row).createCell(8);
		cellactualDB.setCellValue(this.actualDB);*/
	}
	public String getStatusResult() {
		return statusResult;
	}
	public void setStatusResult(String statusResult) {
		this.statusResult = statusResult;
		/*XSSFCell cellstatusResult = sheet.getRow(row).createCell(9);
		cellstatusResult.setCellValue(this.statusResult);*/
	}
	public String getFileResult() {
		return fileResult;
	}
	public void setFileResult(String fileResult) {
		this.fileResult = fileResult;
		/*XSSFCell cellfileResult = sheet.getRow(row).createCell(10);
		cellfileResult.setCellValue(this.fileResult);*/
	}
	public String getDbresult() {
		return dbresult;
	}
	public void setDbresult(String dbresult) {
		this.dbresult = dbresult;
		/*XSSFCell celldbresult = sheet.getRow(row).createCell(11);
		celldbresult.setCellValue(this.dbresult);*/
	}

	public String getExpectedFilePath() {
		return expectedFilePath;
	}
	public void setExpectedFilePath(String expectedFilePath) {
		this.expectedFilePath = expectedFilePath;
	}
	public String getActualFilePath() {
		return actualFilePath;
	}
	public void setActualFilePath(String actualFilePath) {
		this.actualFilePath = actualFilePath;
	}
	
	

	public String getInputFilePath() {
		return inputFilePath;
	}
	public void setInputFilePath(String inputFilePath) {
		this.inputFilePath = inputFilePath;
	}
	

	
	public String getDbFilePath() {
		return dbFilePath;
	}
	public void setDbFilePath(String dbFilePath) {
		this.dbFilePath = dbFilePath;
	}

	public String getOutMsgResID() {
		return outMsgResID;
	}

	public void setOutMsgResID(String outMsgResID) {
		this.outMsgResID = outMsgResID;
	}


	public String getInMsgResID() {
		return inMsgResID;
	}

	public void setInMsgResID(String inMsgResID) {
		this.inMsgResID = inMsgResID;
	}



	public String getE2eExpectedFilePath() {
		return e2eExpectedFilePath;
	}

	public void setE2eExpectedFilePath(String e2eExpectedFilePath) {
		this.e2eExpectedFilePath = e2eExpectedFilePath;
	}

	public String getE2eActualFilePath() {
		return e2eActualFilePath;
	}

	public void setE2eActualFilePath(String e2eActualFilePath) {
		this.e2eActualFilePath = e2eActualFilePath;
	}

	public String getBookingNumber() {
		return bookingNumber;
	}

	public void setBookingNumber(String bookingNumber) {
		this.bookingNumber = bookingNumber;
	}

	public boolean isOutPutFlag() {
		return outPutFlag;
	}

	public void setOutPutFlag(boolean outFileFlag) {
		this.outPutFlag = outFileFlag;
	}

	public void setExpectedFail(String expectedFail) {
		this.expectedFail = expectedFail;
	}

	public String getCs2ExtraFilePath() {
		return cs2ExtraFilePath;
	}


	public void setCs2ExtraFilePath(String cs2ExtraFilePath) {
		this.cs2ExtraFilePath = cs2ExtraFilePath;
	}

	public String getOldBookingNumber() {
		return oldBookingNumber;
	}

	public void setOldBookingNumber(String oldBookingNumber) {
		this.oldBookingNumber = oldBookingNumber;
	}

}
