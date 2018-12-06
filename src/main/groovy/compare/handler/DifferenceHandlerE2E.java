package compare.handler;

import compare.configure.CSB2BEDIConfigQA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DifferenceHandlerE2E {

	static Logger logger = LogManager.getLogger(DifferenceHandlerE2E.class.getName());

	private static String excelPath = null;
	private static String excelDBlogPath = null;
	private static String diffFolderString = "";

	private static File excel = null;
	private static XSSFWorkbook workbook = null;

	public boolean logDifference(String inputFilePath, String expectedFilePath,
			String actualFilePath, String expectedStr, String actualStr,
			File autoLogExcel) {

		boolean successFlag = false;

			if (DifferenceHandlerE2E.excelPath == null) {
				diffFolderString = autoLogExcel.getParent();
				String autoLogExcelName = autoLogExcel.getName().substring(0,autoLogExcel.getName().lastIndexOf("."));

				if (!copyExcel(new File(CSB2BEDIConfigQA.ModelFileFolder+"/Difference-Log-Model.xlsm"),
						new File(diffFolderString),autoLogExcelName))

				{
					logger.info("Copy Excel Model Fail !");

				} else {
					logger.info("Copy Excel Model successfully !");
				}
			}

			// create report
			try {
				if(workbook==null){
					excel = new File(excelPath);
					if(excel.renameTo(excel))
						workbook = new XSSFWorkbook(new FileInputStream(excel));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}


			try {
				writeDifInfo(expectedFilePath, actualFilePath,
						inputFilePath, diffFolderString);
				String[] expArray = expectedStr.split("\n");
				String[] actArray = actualStr.split("\n");
				String act = "";
				String exp = "";

				if(expArray.length>16000&&actArray.length>16000){
					act = "Actual file and Expected file row number is bigger than 16000 !!!"+"\r\n"
							+ "Please compare this 2 files by manual !!!!";
					writeDif(inputFilePath,exp, act);
					return successFlag;
				}

				// number of lines of each file
				int M = expArray.length;
				int N = actArray.length;

				for (int i = 0; i <= M - 1; i++) {
					expArray[i] = expArray[i];
				}
				for (int j = 0; j <= M - 1; j++) {
					expArray[j] = expArray[j];
				}

				// opt[i][j] = length of LCS of x[i..M] and y[j..N]
				int[][] opt = new int[M + 1][N + 1];

				// compute length of LCS and all subproblems via dynamic programming
				for (int i = M - 1; i >= 0; i--) {
					for (int j = N - 1; j >= 0; j--) {
						if (expArray[i].equals(actArray[j]))
							opt[i][j] = opt[i + 1][j + 1] + 1;

						else
							opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
					}
				}

				// recover LCS itself and print out non-matching lines to standard
				// output
				int i = 0, j = 0;
				int samei = 0;
				int samej = 0;
				while (i < M && j < N) {
					String s = new String();
					if (expArray[i].equals(actArray[j])) {

						i++;
						j++;
						samei = i;
						samej = j;
						// System.out.println("i:"+i+" j:"+j);
						if (!exp.equals("") || !act.equals("")) {
							writeDif(inputFilePath,exp, act);
							act = "";
							exp = "";
						}

					} else {

						if (opt[i + 1][j] >= opt[i][j + 1]) {
							s = expArray[i++];
							if (exp.equals("")) {
								exp = s;
							} else
								exp = exp + "\r\n" + s;
						} else {
							s = actArray[j++];
							if (act.equals("")) {
								act = s;
							} else
								act = act + "\r\n" + s;
						}
					}
				}
				act = "";
				exp = "";
				String actTem = "";
				String expTem = "";
				while (j < N) {
					if (i == M) {
						actTem = actArray[j++];
						if (act.equals("")) {
							act = actTem;
						} else
							act = act + "\r\n" + actTem;
						// System.out.println("hereACT");
					}

				}
				while (samei < M) {

					if (samej == N) {

						expTem = expArray[samei++];
						if (exp.equals("")) {
							exp = expTem;
						} else
							exp = exp + "\r\n" + expTem;
						// System.out.println("hereexp");
					} else
						samej++;

				}

				if (!(act.equals("")) || !(exp.equals(""))) {
					writeDif(inputFilePath,exp, act);
				}
				logger.info("Write difference successfully !");




		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Encouter error when logging difference !");
			e.printStackTrace();
		}

	return successFlag;

}



public synchronized void saveAll(){
	if(workbook!=null && excel!=null){
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(excel);
			workbook.write(fOut);
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
	}
	
	public void logDBDifference(String tpID, String msgReqID,File autoLogExcel,String infilename,String expstatus, String actstatus,
			String txnerr, String deterr) {
		try{
			if(excelDBlogPath==null){
				String autoLogExcelName = autoLogExcel.getName().substring(0,autoLogExcel.getName().lastIndexOf("."));
				excelDBlogPath=copyExcelWithName(new File(
						"com/differenceExcelModel/Difference-DB-Log-Model.xlsm"),
						new File(autoLogExcel.getParent()),"Difference-DB-Log-"+autoLogExcelName+"-");
				if (excelDBlogPath==null)
				{
					logger.info("Copy Excel DB Model Fail !");

				} else {
					logger.info("Copy Excel DB Model successfully !");
				}
			}
			writeDBDifInfo(tpID,msgReqID,infilename,expstatus, actstatus,txnerr, deterr);
		}
		
		
		catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Encouter error when logging DB difference !");
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public void writeDif(String inputFilePath,String e, String a) throws Exception {
//		File excel = new File(excelPath);
//		if (excel.renameTo(excel)) {
			XSSFSheet XSSFSheet = workbook.getSheetAt(0);
			XSSFCellStyle cellstyle = workbook.createCellStyle();
			cellstyle.setAlignment(XSSFCellStyle.ALIGN_LEFT); //
			cellstyle.setWrapText(true);
			XSSFCellStyle cellstyle1 = workbook.createCellStyle();
			cellstyle1.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			cellstyle1.setFillForegroundColor(HSSFColor.YELLOW.index);
			cellstyle1.setAlignment(XSSFCellStyle.ALIGN_LEFT);
			cellstyle1.setWrapText(true);
			XSSFCellStyle cellstyle2 = workbook.createCellStyle();
			cellstyle2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			cellstyle2.setFillForegroundColor(HSSFColor.ORANGE.index);
			cellstyle2.setAlignment(XSSFCellStyle.ALIGN_LEFT);
			cellstyle2.setWrapText(true);
			int rownum = XSSFSheet.getLastRowNum();
			XSSFRow row = XSSFSheet.createRow(rownum + 1);
			XSSFCell cellreqid = row.createCell((int) 1);
			cellreqid.setCellType(XSSFCell.CELL_TYPE_STRING);
			cellreqid.setCellValue(inputFilePath);
			XSSFCell cellexpcontent = row.createCell((int) 2);
			cellexpcontent.setCellType(XSSFCell.CELL_TYPE_STRING);
			/*int CellMaxLength=config.getInt("CellMaxLength");
			if(e.length()>CellMaxLength)
			{
				logger.info("length og exp="+e.length());
				e=e.substring(0,CellMaxLength);	
				cellexpcontent.setCellStyle(cellstyle1);				
			}
			else*/
			cellexpcontent.setCellStyle(cellstyle);
			XSSFCell cellactcontent = row.createCell((int) 3);
			cellactcontent.setCellType(XSSFCell.CELL_TYPE_STRING);
		/*	if(a.length()>CellMaxLength)
			{	
				logger.info("length og act="+a.length());
				a=a.substring(0,CellMaxLength);
				cellactcontent.setCellStyle(cellstyle1);
			}else if(a.startsWith("Actual file and Expected file row number is bigger than 16000")){
				cellactcontent.setCellStyle(cellstyle2);	
			}else*/
			cellactcontent.setCellStyle(cellstyle);
			
			cellexpcontent.setCellValue(e);
			cellactcontent.setCellValue(a);
//			FileOutputStream fOut = new FileOutputStream(excel);
//			workbook.write(fOut);
//			fOut.flush();
//			fOut.close();

//		}
	}

	public void writeDifInfo(String expectedFilePath, String actualFilePath,
			String inputFilePath, String workingFolder) throws Exception {

//		File excel = new File(excelPath);
//		if (excel.renameTo(excel)) {
//			XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(excel));
			XSSFCellStyle style1 = workbook.createCellStyle();
			style1.setBorderBottom(XSSFCellStyle.BORDER_THIN); //
			style1.setBottomBorderColor(IndexedColors.BLACK.getIndex()); //
			style1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			style1.setLeftBorderColor(IndexedColors.BLACK.getIndex());
			style1.setBorderRight(XSSFCellStyle.BORDER_THIN);
			style1.setRightBorderColor(IndexedColors.BLACK.getIndex());
			style1.setBorderTop(XSSFCellStyle.BORDER_MEDIUM);
			style1.setTopBorderColor(IndexedColors.BLACK.getIndex());
			style1.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);//

			style1.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex()); //
			style1.setAlignment(XSSFCellStyle.ALIGN_LEFT); //
			style1.setWrapText(true);
			XSSFSheet XSSFSheet = workbook.getSheetAt(0);
			int rownum = XSSFSheet.getLastRowNum();
			XSSFRow row = XSSFSheet.createRow(rownum + 1);
			// row.setRowStyle(style1);
			XSSFCell celltpid = row.createCell((int) 0);
			celltpid.setCellType(XSSFCell.CELL_TYPE_STRING);
			celltpid.setCellStyle(style1);
			XSSFCell cellreqid = row.createCell((int) 1);
			cellreqid.setCellStyle(style1);
			cellreqid.setCellType(XSSFCell.CELL_TYPE_STRING);
			XSSFCell cellexpfile = row.createCell((int) 2);
			cellexpfile.setCellStyle(style1);
			cellexpfile.setCellType(XSSFCell.CELL_TYPE_STRING);
			XSSFCell cellactfile = row.createCell((int) 3);
			cellactfile.setCellStyle(style1);
			cellactfile.setCellType(XSSFCell.CELL_TYPE_STRING);
			XSSFCell cellactinputfile = row.createCell((int) 4);
			cellactinputfile.setCellStyle(style1);
			cellactinputfile.setCellType(XSSFCell.CELL_TYPE_STRING);


			// add for right-click run
			workingFolder = (new File(workingFolder)).getAbsolutePath();
			inputFilePath = (new File(inputFilePath)).getAbsolutePath();
			expectedFilePath = (new File(expectedFilePath)).getAbsolutePath();
			actualFilePath = (new File(actualFilePath)).getAbsolutePath();

			logger.info("Input File Path:" + inputFilePath);
			logger.info("Expected File Path:" + expectedFilePath);
			logger.info("Actual File Path:" + actualFilePath);
			logger.info("Difference Excel floder:" + workingFolder);

			cellexpfile.setCellValue(expectedFilePath
					.substring(expectedFilePath.indexOf(workingFolder)
							+ workingFolder.length() + 1));

			cellactfile.setCellValue(actualFilePath.substring(actualFilePath
					.indexOf(workingFolder)
					+ workingFolder.length() + 1));
			cellactinputfile.setCellValue(inputFilePath.substring(inputFilePath
					.indexOf(workingFolder)
					+ workingFolder.length() + 1));
//			FileOutputStream fOut = new FileOutputStream(excel);
//			workbook.write(fOut);
//
//			fOut.flush();
//			fOut.close();
//		}
	}


	public void writeTxnInfo(String txnSeq, int sheet) throws Exception {
//		File excel = new File(excelPath);
//		if (excel.renameTo(excel)) {
//			XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(excel));
			XSSFCellStyle styleTxn = workbook.createCellStyle();
			styleTxn.setBorderBottom(XSSFCellStyle.BORDER_THIN); //
			styleTxn.setBottomBorderColor(IndexedColors.BLACK.getIndex()); //
			styleTxn.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			styleTxn.setLeftBorderColor(IndexedColors.BLACK.getIndex());
			styleTxn.setBorderRight(XSSFCellStyle.BORDER_THIN);
			styleTxn.setRightBorderColor(IndexedColors.BLACK.getIndex());
			styleTxn.setBorderTop(XSSFCellStyle.BORDER_MEDIUM);
			styleTxn.setTopBorderColor(IndexedColors.BLACK.getIndex());
			styleTxn.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);//

			styleTxn.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE
					.getIndex()); //
			styleTxn.setAlignment(XSSFCellStyle.ALIGN_LEFT); //
			styleTxn.setWrapText(true);
			// style.setFillPattern(XSSFCellStyle.//);

			// ***************************
			XSSFSheet XSSFSheet = workbook.getSheetAt(sheet);

			int rownum = XSSFSheet.getLastRowNum();

			XSSFRow row = XSSFSheet.createRow(rownum + 1);
			// row.setRowStyle(styleTxn);

			XSSFCell celltxnSeq = row.createCell((int) 0);
			celltxnSeq.setCellStyle(styleTxn);
			celltxnSeq.setCellValue(txnSeq);
//			FileOutputStream fOut = new FileOutputStream(excel);
//			workbook.write(fOut);
//
//			fOut.flush();
//			fOut.close();
//		}
	}

	public void writeDBDifInfo(String tpID, String msgReqID,String infilename,
			String expstatus, String actstatus,
			String txnerr, String deterr) throws Exception {

		File excel = new File(excelDBlogPath);
		if (excel.renameTo(excel)) {
			XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(excel));
			XSSFCellStyle style1 = workbook.createCellStyle();
			style1.setBorderBottom(XSSFCellStyle.BORDER_THIN); //
			style1.setBottomBorderColor(IndexedColors.BLACK.getIndex()); //
			style1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			style1.setLeftBorderColor(IndexedColors.BLACK.getIndex());
			style1.setBorderRight(XSSFCellStyle.BORDER_THIN);
			style1.setRightBorderColor(IndexedColors.BLACK.getIndex());
			style1.setBorderTop(XSSFCellStyle.BORDER_MEDIUM);
			style1.setTopBorderColor(IndexedColors.BLACK.getIndex());
			style1.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);//

			style1.setFillForegroundColor(IndexedColors.WHITE.getIndex());
			style1.setAlignment(XSSFCellStyle.ALIGN_LEFT); //
			style1.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
			style1.setWrapText(true);
			XSSFSheet XSSFSheet = workbook.getSheetAt(0);
			int rownum = XSSFSheet.getLastRowNum();
			XSSFRow row = XSSFSheet.createRow(rownum + 1);
			// row.setRowStyle(style1);
			XSSFCell celltpid = row.createCell((int) 0);
			celltpid.setCellType(XSSFCell.CELL_TYPE_STRING);
			celltpid.setCellStyle(style1);
			XSSFCell cellreqid = row.createCell((int) 1);
			cellreqid.setCellStyle(style1);
			cellreqid.setCellType(XSSFCell.CELL_TYPE_STRING);
			XSSFCell cellactinputfile = row.createCell((int) 2);
			cellactinputfile.setCellStyle(style1);
			cellactinputfile.setCellType(XSSFCell.CELL_TYPE_STRING);
			XSSFCell cellexpstatus = row.createCell((int) 3);
			cellexpstatus.setCellStyle(style1);
			cellexpstatus.setCellType(XSSFCell.CELL_TYPE_STRING);
			XSSFCell cellactstatus = row.createCell((int) 4);
			cellactstatus.setCellStyle(style1);
			cellactstatus.setCellType(XSSFCell.CELL_TYPE_STRING);
			XSSFCell celltxnerrmsg = row.createCell((int) 5);
			celltxnerrmsg.setCellStyle(style1);
			celltxnerrmsg.setCellType(XSSFCell.CELL_TYPE_STRING);
			XSSFCell celldeterrmsg = row.createCell((int) 6);
			celldeterrmsg.setCellStyle(style1);
			celldeterrmsg.setCellType(XSSFCell.CELL_TYPE_STRING);
			
			celltpid.setCellValue(tpID);
			cellreqid.setCellValue(msgReqID);
			cellactinputfile.setCellValue(infilename);
			cellexpstatus.setCellValue(expstatus);
			cellactstatus.setCellValue(actstatus);
			celltxnerrmsg.setCellValue(txnerr);
			celldeterrmsg.setCellValue(deterr);
			
			
			FileOutputStream fOut = new FileOutputStream(excel);
			workbook.write(fOut);

			fOut.flush();
			fOut.close();
		}
	}
	public boolean copyExcel(File f, File dir,String autoLogExcelName) {

		if (!f.isFile()) {
			logger.warn("Can't find the difference model : "
					+ f.getAbsolutePath());
			return false;
		} else {
			try {
				Date time = new Date();
				SimpleDateFormat form = new SimpleDateFormat("yyyyMMddHHmmss");
				String stime = form.format(time);
				String newfileName = "Difference-Log-" +autoLogExcelName+"-"+ stime + ".xlsm";

				File fcopy = new File(dir, newfileName);
				fcopy.createNewFile();
				FileInputStream finpen = new FileInputStream(f);
				FileOutputStream foutpen = new FileOutputStream(fcopy);
				FileChannel fin = finpen.getChannel(), fou = foutpen
						.getChannel();

				ByteBuffer byb = ByteBuffer.allocate(1024);
				int i = 1;
				while (i != -1) {
					byb.clear();
					i = fin.read(byb);
					byb.flip();
					fou.write(byb);
				}
				fin.close();
				fou.close();
				finpen.close();
				foutpen.close();
				excelPath = dir + "/" + newfileName;
				return true;
			} catch (Exception e) {
				System.out.println("Copy Excel fail....");
				return false;
			}
		}

	}
	
	public String copyExcelWithName(File f, File dir,String filename) {

		if (!f.isFile()) {
			logger.warn("Can't find the difference model : "
					+ f.getAbsolutePath());
			return null;
		} else {
			try {
				Date time = new Date();
				SimpleDateFormat form = new SimpleDateFormat("yyyyMMddHHmmss");
				String stime = form.format(time);
				filename = filename + stime + ".xlsm";

				File fcopy = new File(dir, filename);
				fcopy.createNewFile();
				FileInputStream finpen = new FileInputStream(f);
				FileOutputStream foutpen = new FileOutputStream(fcopy);
				FileChannel fin = finpen.getChannel(), fou = foutpen
						.getChannel();

				ByteBuffer byb = ByteBuffer.allocate(1024);
				int i = 1;
				while (i != -1) {
					byb.clear();
					i = fin.read(byb);
					byb.flip();
					fou.write(byb);
				}
				fin.close();
				fou.close();
				finpen.close();
				foutpen.close();
				return  dir + "/" + filename;
			} catch (Exception e) {
				System.out.println("Copy Excel fail....");
				return null;
			}
		}

	}

}
