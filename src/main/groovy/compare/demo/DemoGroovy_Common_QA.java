package compare.demo;


import compare.configure.CSB2BEDIConfigQA;
import groovy.lang.GroovyObject;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Element;

import javax.swing.*;
import java.io.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;


public class DemoGroovy_Common_QA {
	
	private static XSSFWorkbook wbQA = null;
	private static FileOutputStream fileOutQA = null;
	private static XSSFSheet sheetQA = null;
	private static InputStream fisQA = null;

	static String inputFileFolder = CSB2BEDIConfigQA.rootPath;
	static String javaBeanCommonScriptFile = CSB2BEDIConfigQA.javaBeanCommonScriptFile;
	static String javaBeanMessageTypeScriptFile = CSB2BEDIConfigQA.javaBeanMessageTypeScriptFile;
	static String mappingUtilScriptFile = CSB2BEDIConfigQA.mappingUtilScriptFile;
	static String mappingUtilMessageTypeCommonScriptFile = CSB2BEDIConfigQA.mappingUtilMessageTypeCommonScriptFile;
	static String pmtMappingScriptFile = CSB2BEDIConfigQA.pmtMappingScriptFile;
	static String pmtPrepMappingScriptFile = CSB2BEDIConfigQA.pmtPrepMappingScriptFile;
	public static String definitionFilePath = CSB2BEDIConfigQA.definitionFilePath;

	private static List<Element> appErrorList = new ArrayList<Element>();
	
	private static List<String> expectStatusAndCurrentStatus = new ArrayList<String>();
	
	private static Map<String,List<String>> blankResultForExcel = new HashMap<String,List<String>>();
	
	public static Map<String, String> belugaGroovyError = new HashMap<String, String>();
	
	private static Map<String, String> groovyRuntimeError = new HashMap<String, String>();
	
	private static Logger logger = LogManager.getLogger(DemoGroovy_Common_QA.class.getName());
	
	private static String thisClassName = DemoGroovy_Common_QA.class.getName().substring(DemoGroovy_Common_QA.class.getName().lastIndexOf(".") + 1,
			DemoGroovy_Common_QA.class.getName().length());
	private static String groovyMappingClassName = pmtMappingScriptFile.substring(pmtMappingScriptFile.lastIndexOf("/") + 1,
			pmtMappingScriptFile.length());
	
	public static int successCount = 0;
	public static int failCount = 0;
	public static int exceptionCount = 0;
	public static int errorCount = 0;
	public static int obsoleteCount = 0;
	public static int maxDifferenceCount = 0;
	//public static int maxDifferenceCount = 30;
	public static int currentDifferenceCount = 0;
//	public static int maxDifferenceCount = 0;
	public static boolean isRunning = false;

//	public static Hashtable<Integer, Long> mappingThreadWorkingIds = new Hashtable<Integer, Long>();
//	public static List<Integer> mappingThreadCompleteIds = new ArrayList<Integer>();
	
	public static Hashtable<String, MappingThread> workingThreads = new Hashtable<String, MappingThread>();
	int maxConcurrenThreadCount = 20;

	public DemoGroovy_Common_QA(int threadCount) {
		this.maxConcurrenThreadCount = threadCount;
	}
	
	public MappingThread getWorkingThread() {
		for (MappingThread tt : workingThreads.values()) {
			if (! tt.getWorkingStatus()) {
				return tt;
			}
		}
		return null;
	}
	
	public void listWorkingThreads() {
		System.out.println("---- working threads ----");
		for (MappingThread tt : workingThreads.values()) {
			if (tt.getWorkingStatus()) {
				System.out.println("tid: "+tt.getThreadId()+", seq: "+tt.getWorkingSeq());
				if (System.currentTimeMillis() - tt.getStartTs() > 300000) {
					System.out.println("---------------------------------");
					System.out.println("--thrad running over 5 mins, system exit and please check this case to avoid running too long: "+tt.getName()+", "+tt.getId());
					System.out.println("---------------------------------");
					System.exit(1);
				}
			}
		}
		System.out.println("---- ----");
	}
	
	public int workingThreadsCount() {
		int ret = 0;
		for (MappingThread tt : workingThreads.values()) {
			if (tt.getWorkingStatus()) {
				ret++;
			}
		}
		return ret;
	}
	
	public void closeWorkingThreads() {
		for (MappingThread tt : workingThreads.values()) {
			tt.stopService();
		}
		try {
			Thread.sleep(2000);
		} catch (Exception ex) {}
		for (MappingThread tt : workingThreads.values()) {
			tt.closeDBConnection();
		}
		isRunning = false;

	}
//
//	public void demo(File excel) throws Exception {
//		isRunning = true;
//
//		ConnectionForQA testDBConn = new ConnectionForQA();
//		//Connection conn = null;
//		File  root = new File(excel.getParent());
//		if (!FunctionHelperQA.checkExcelAvailable(excel)) {
//			return;
//		}
//
//		if (FunctionHelperQA.backupFile(excel)) {
//			logger.info("Backup Auto log excel successfully !");
//		} else {
//			logger.error("Fail to backup Auto log excel !");
//		}
//		ArrayList<RecordModelQA> recordModelList = new ArrayList<RecordModelQA>();
//		openExcel(excel);
//		int rowCount = sheetQA.getLastRowNum();
//		String flag = null;
//		boolean fileExistFlag = true;
//		for (int i = 1; i <= rowCount; i++) {
//			if (sheetQA.getRow(i) == null) {
//				logger.error("Row "
//						+ (i + 1)
//						+ " is null,and it is skipped. Advise to remove this null row.");
//				continue;
//			}
//			RecordModelQA recordModel = new RecordModelQA(sheetQA, i);
//			flag = recordModel.getRunFlag().toLowerCase().trim();
//			if (!flag.equals("t")) {
//				continue;
//			}
//			if (!StringUtils.isEmpty(recordModel.getInputFile())&&!FunctionHelperQA.fileExist((i + 1),recordModel.getInputFilePath()))
//				fileExistFlag = false;
//			if (!StringUtils.isEmpty(recordModel.getExpectedFile())&&!FunctionHelperQA.fileExist((i + 1),recordModel.getExpectedFilePath()))
//				fileExistFlag = false;
//			if (!StringUtils.isEmpty(recordModel.getExpectedFail())&&!FunctionHelperQA.fileExist((i + 1),recordModel.getDbFilePath()))
//				fileExistFlag = false;
//			recordModel.setRow(i);
//			recordModel.setExcel(excel);
//			recordModelList.add(recordModel);
//		}
//		closeExcel(excel);
//		if (!fileExistFlag) {
//
//			logger.error("Above files not exist , please check !");
//			JOptionPane.showMessageDialog(null,
//					"Some files not exist,please check the log !",
//					"Warning", JOptionPane.YES_OPTION);
//			return;
//		}
//
//		if (recordModelList.size() <= 0) {
//			logger.error("No row needs to be runned.");
//			return;
//		}
//		if(recordModelList.size()>0){
//			CSB2BEDIConfigQA.initFileFormat(recordModelList.get(0).getInputFile());
//		}
//
//		//Mapping Part
//		GroovyClassDefinition groovyDefClass = null;
//
//		//20161222 david
//		LinkedHashMap<String, String> scripts = new LinkedHashMap<String, String>();
//
//		//Mapping Part
//		GroovyClassDefinition groovyDefPrepClass = null;
//
//		//20161222 david
//		LinkedHashMap<String, String> scriptsPrep = new LinkedHashMap<String, String>();
//
//		if(CSB2BEDIConfigQA.DIR_ID.toLowerCase().startsWith("o")){
//			//1, put message type javabean common script
//			scripts.put(new File(javaBeanCommonScriptFile).getName(), LocalFileUtilQA.readBigFile(javaBeanCommonScriptFile));
//
//		}
//		//2, put message type javabean message script
//		scripts.put(new File(javaBeanMessageTypeScriptFile).getName(), LocalFileUtilQA.readBigFile(javaBeanMessageTypeScriptFile));
//
//		//3, put general mapping util script
//		scripts.put(new File(mappingUtilScriptFile).getName(), LocalFileUtilQA.readBigFile(mappingUtilScriptFile));
//		//4, put message type common script
////		scripts.put(new File(mappingUtilMessageTypeCommonScriptFile).getName(), LocalFileUtilQA.readBigFile(mappingUtilMessageTypeCommonScriptFile));
//		//5, put pmt mapping script
//		scripts.put(new File(pmtMappingScriptFile).getName(), LocalFileUtilQA.readBigFile(pmtMappingScriptFile));
//
//		groovyDefClass = GroovyScriptHelper.getClassDef(scripts);
//		//6. put prep mapping script
//		if(StringUtils.isNotEmpty(pmtPrepMappingScriptFile)){
//			scriptsPrep.put(new File(mappingUtilScriptFile).getName(), LocalFileUtilQA.readBigFile(mappingUtilScriptFile));
//			scriptsPrep.put(new File(pmtPrepMappingScriptFile).getName(), LocalFileUtilQA.readBigFile(pmtPrepMappingScriptFile));
//			groovyDefPrepClass = GroovyScriptHelper.getClassDef(scriptsPrep);
//		}
//
//		try {
//			if (maxConcurrenThreadCount>1) {
//				if (recordModelList.size()<=4) {
//					logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 1 !!!!!");
//					maxConcurrenThreadCount = 1;
//				} else if (recordModelList.size()<=10) {
//					logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 2 !!!!!");
//					maxConcurrenThreadCount = 2;
//				} else if (recordModelList.size()<=20) {
//					logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 3 !!!!!");
//					maxConcurrenThreadCount = 3;
//				} else if (recordModelList.size()<=50) {
//					logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 4 !!!!!");
//					maxConcurrenThreadCount = 4;
//				} else if (recordModelList.size()<=100) {
//					logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 5 !!!!!");
//					maxConcurrenThreadCount = 5;
//				} else if (recordModelList.size()<=200) {
//					logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 6 !!!!!");
//					maxConcurrenThreadCount = 6;
//				} else if (recordModelList.size()<=300) {
//					logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 7 !!!!!");
//					maxConcurrenThreadCount = 7;
//				} else if (recordModelList.size()<=500) {
//					logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 8 !!!!!");
//					maxConcurrenThreadCount = 8;
//				} else if (recordModelList.size()<=800) {
//					logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 9 !!!!!");
//					maxConcurrenThreadCount = 9;
//				} else if (recordModelList.size()<=1000) {
//					logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 10 !!!!!");
//					maxConcurrenThreadCount = 10;
//				} else if (recordModelList.size()<=10000) {
//					//if > 1000, limit the min concurrent db is 12
//					if (maxConcurrenThreadCount <= 15) {
//						logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 15 !!!!!");
//						maxConcurrenThreadCount = 15;
//					} else {
//						//keep original setting if > 15 threads
//					}
//				} else if (recordModelList.size()>10000 && maxConcurrenThreadCount<30) {
//					logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 30  !!!!!");
//					maxConcurrenThreadCount = 30;
//				}
//			}
//			int maxLimitationConn = 40;
//			if (maxConcurrenThreadCount > maxLimitationConn) {
//				logger.info("--> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to "+maxLimitationConn+", need to make sure the env.");
//				maxConcurrenThreadCount = maxLimitationConn;
//			}
//			logger.info(">>>>> maxConcurrenThreadCount: " + maxConcurrenThreadCount);
//
//			for (int i=0; i<maxConcurrenThreadCount; i++) {
//				String tid = "T-"+(i+1);
//				Connection conn = testDBConn.getB2BEDIQA1_DEV_DBConn();
//				MappingThread t = new MappingThread(tid, conn);
//				workingThreads.put(tid, t);
//				t.start();
//			}
//
//			new Thread(){
//				public void run() {
//					if(maxDifferenceCount != 0){
//						while(isRunning){
//							try {
//								Thread.sleep(2000);
//								System.err.println(">> Current difference count   " + currentDifferenceCount);
//								if(currentDifferenceCount > maxDifferenceCount){
//									closeWorkingThreads();
//									DifferenceHandlerQA.getInstance().saveAll();
//									System.err.println(">> Current difference count " + currentDifferenceCount + " > max difference count " + maxDifferenceCount + " .");
//									System.err.println(">> Stop compare thread.");
//									System.exit(0);
//								}
//							} catch (InterruptedException e) {
//								e.printStackTrace();
//							}
//						}
//					}
//				}
//			}.start();
//			openExcel(excel);
//			for (int i=0;i<recordModelList.size();i++) {
//				RecordModelQA recordModel = (RecordModelQA) recordModelList.get(i);
//				GroovyObject instance = groovyDefClass.getInstance();
//				GroovyObject instancePrep = null;
//				if(StringUtils.isNotEmpty(pmtPrepMappingScriptFile)){
//					instancePrep = groovyDefPrepClass.getInstance();
//				}
//				long waitFrom = System.currentTimeMillis();
//
//				MappingThread mappingThread = getWorkingThread();
//				while (mappingThread == null) {
//					System.out.println("waiting working thread ... ");
//					Thread.sleep(2000);
//					long now = System.currentTimeMillis();
//					if (now - waitFrom > 15000) {
//						listWorkingThreads();
//						waitFrom = System.currentTimeMillis();
//					}
//					mappingThread = getWorkingThread();
//				}
//
//				int threadNumber = i+1;
////				DemoGroovy_Common_QA.mappingThreadWorkingIds.put(threadNumber, System.currentTimeMillis());
////				System.out.println("Thread "+threadNumber+" starting...");
//
//				// *** start mapping
//				mappingThread.setRunningParams(threadNumber, recordModel, instance, instancePrep);
//
//				// *** end of mapping
//				Thread.sleep(200);
//			}
//
//			//wait all finish
//			Thread.sleep(2000);
//
//			long waitFrom = System.currentTimeMillis();
//			int workingCount = workingThreadsCount();
//			while (workingCount != 0) {
//				System.out.println(">> Waiting working thread finish ... working count: "+workingCount);
//				Thread.sleep(2000);
//				long now = System.currentTimeMillis();
//				if (now - waitFrom > 20000) {
//					listWorkingThreads();
//					waitFrom = System.currentTimeMillis();
//				}
//				workingCount = workingThreadsCount();
//			}
//
//			//close all thread's db connection
//			closeWorkingThreads();
//			System.out.println(">> All mapping works done.");
//
//			DifferenceHandlerQA.getInstance().saveAll();
//			closeExcel(excel);
//			logger.info("failCount:"+failCount);
//			logger.info("exceptionCount:"+exceptionCount);
//			logger.info("successCount:"+successCount);
//		} catch (Exception e1) {
//			logger.error("exception run demo function, fail Error:", e1);
//			StringWriter sw = new StringWriter();
//	        PrintWriter pw = new PrintWriter(sw);
//	        e1.printStackTrace(pw);
//	        BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
//	        String b = br.readLine();
//	        StringBuffer sb = new StringBuffer();
//	        if (b.contains("groovy")) {
//				sb.append(b + "\r\n");
//				for (String line = br.readLine(); line != null; line = br.readLine()) {
//					if (line.contains(pmtMappingScriptFile.substring(
//							pmtMappingScriptFile.lastIndexOf("/") + 1,
//							pmtMappingScriptFile.length()))
//							|| line.contains(pmtMappingScriptFile.substring(
//									pmtMappingScriptFile.lastIndexOf("/") + 1,
//									pmtMappingScriptFile.length() - 7))) {
//						sb.append(line + "\r\n");
//					}
//				}
//				groovyRuntimeError.put(groovyMappingClassName, sb.toString());
//			}
//		} finally {
////			if (conn!=null) {
////				try {
////			//	writeBelugaErrorLog();
////			//	writeBlankResultLog();
////				conn.close();
////				} catch (Exception e) {}
////			}
//
//			//important, clean up to avoid memory leak
//			if (groovyDefClass!=null) {
//				groovyDefClass.close();
//			}
//		}
//
//
//		if(recordModelList.size()>0){
////			compareResult(excel,recordModelList);
//			updateResultToExcel(excel,recordModelList);
//			summaryToDoList(excel,recordModelList);
//		}
//		logger.warn("------------------Finish--------------");
//	}
//
//	public void summaryToDoList(File excel,ArrayList<RecordModelQA> recordModelList){
//		logger.warn("-------------------Start to export the fail to do list ---------------");
//		File toDoListF = new File(CSB2BEDIConfigQA.rootPath+"/"+"To_Do_List.txt");
//		if (toDoListF.exists()) {
//			toDoListF.delete();
//		}
//
////		if(!toDoListF.exists()){
////			toDoListF.mkdir();
////		}
////		String fileContent = FunctionHelperQA.readContent(toDoListF);
////		fileContent="";
//		Date systemTime = new Date();
//		SimpleDateFormat form = new SimpleDateFormat("yyyyMMddHHmmssSSS");
//		String systemString = form.format(systemTime);
//
//		openExcel(excel);
//		StringBuffer ignoreSB = new StringBuffer();
//		ignoreSB.append("--------------------------"+systemTime+"--------------------------");
//		ignoreSB.append("\r\n");
//		for (int n = 0; n < recordModelList.size(); n++) {
//			RecordModelQA recordModel = (RecordModelQA) recordModelList.get(n);
//			if(StringUtils.isEmpty(recordModel.getStatusResult())){
//				ignoreSB.append("Row "+recordModel.getRow()+": Input file: "+recordModel.getInputFile()+"; Status compare is NULL; " +"Expected Status:"+
//						recordModel.getExpectedStatus()+"; Actual Status:"+recordModel.getActualStatus()+".");
//			}else if(recordModel.getStatusResult().toLowerCase().equals("fail")){
//				ignoreSB.append("Row "+recordModel.getRow()+": Input file: "+recordModel.getInputFile()+"; Status compare is Fail;"+"Expected Status:"+
//						recordModel.getExpectedStatus()+"; Actual Status:"+recordModel.getActualStatus()+".");
//
//			}else if(!StringUtils.isEmpty(recordModel.getFileResult())&&recordModel.getFileResult().toLowerCase().equals("fail")){
//				ignoreSB.append("Row "+recordModel.getRow()+": Input file: "+recordModel.getInputFile()+"; Content compare is Fail.");
//
//			}else if(!StringUtils.isEmpty(recordModel.getDbresult())&&recordModel.getDbresult().toLowerCase().equals("fail")){
//				ignoreSB.append("Row "+recordModel.getRow()+": Input file: "+recordModel.getInputFile()+"; Error message compare is Fail.");
//			}else if(recordModel.getStatusResult().toLowerCase().equals("pass")&&
//					StringUtils.isEmpty(recordModel.getFileResult())&&
//					StringUtils.isEmpty(recordModel.getDbresult())){
//				ignoreSB.append("Row "+recordModel.getRow()+": Input file: "+recordModel.getInputFile()+"; UnExpected Error ; " +"Expected Status:"+
//						recordModel.getExpectedStatus()+"; Actual Status:"+recordModel.getActualStatus()+".");
//			}else
//				continue;
//
//			ignoreSB.append("\r\n");
//
//		}
//		closeExcel(excel);
//		try {
//			LocalFileUtilQA.writeToFile(toDoListF.getAbsolutePath(), ignoreSB.toString(), false);
//		} catch (Exception e) {
//			logger.error(">>> summaryToDoList - Unknow exception, plesae contact DEV to check, with your log.");
//			logger.error("Reconci Tools fatal error in summaryToDoList", e);
//			System.exit(-1);
//		}
//	}
//
//	public void updateResultToExcel(File excel,ArrayList<RecordModelQA> recordModelList){
//		logger.warn("-------------------Start to update result to excel---------------");
//		openExcel(excel);
//		for (int n = 0; n < recordModelList.size(); n++) {
//			RecordModelQA recordModel = (RecordModelQA) recordModelList.get(n);
//			XSSFCell cellactualStatus = sheetQA.getRow(recordModel.getRow()).createCell(5);
//			XSSFCell cellactualFile = sheetQA.getRow(recordModel.getRow()).createCell(6);
//			XSSFCell cellactualDB = sheetQA.getRow(recordModel.getRow()).createCell(7);
//			XSSFCell cellstatusResult = sheetQA.getRow(recordModel.getRow()).createCell(8);
//			XSSFCell cellfileResult = sheetQA.getRow(recordModel.getRow()).createCell(9);
//			XSSFCell celldbresult = sheetQA.getRow(recordModel.getRow()).createCell(10);
//			if(!StringUtils.isEmpty(recordModel.getActualStatus()))
//				cellactualStatus.setCellValue(recordModel.getActualStatus());
//			else
//				cellactualStatus.setCellValue("");
//
//			if(!StringUtils.isEmpty(recordModel.getActualFile()))
//				cellactualFile.setCellValue(recordModel.getActualFile());
//			else
//				cellactualFile.setCellValue("");
//
//			if(!StringUtils.isEmpty(recordModel.getActualFail()))
//				cellactualDB.setCellValue(recordModel.getActualFail());
//			else
//				cellactualDB.setCellValue("");
//			if(!StringUtils.isEmpty(recordModel.getStatusResult()))
//				cellstatusResult.setCellValue(recordModel.getStatusResult());
//			else
//				cellstatusResult.setCellValue("");
//
//			if(!StringUtils.isEmpty(recordModel.getFileResult()))
//				cellfileResult.setCellValue(recordModel.getFileResult());
//			else
//				cellfileResult.setCellValue("");
//
//			if(!StringUtils.isEmpty(recordModel.getDbresult()))
//				celldbresult.setCellValue(recordModel.getDbresult());
//			else
//				celldbresult.setCellValue("");
//
//		}
//		closeExcel(excel);
//	}
//
//
	
    public static String getErrorInfoFromException(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return "\r\n" + sw.toString() + "\r\n";
        } catch (Exception e2) {
            return ">>>> getErrorInfoFromException, "+e.toString();
        }
    }
	
	private static void openExcel(File excel) {
		try {
			fisQA = new FileInputStream(excel);
			wbQA = new XSSFWorkbook(fisQA);

			sheetQA = wbQA.getSheetAt(0);
			// logger.info("Excel is opened !");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("Encounter FileNotFoundException in openExcel : " + excel.getAbsolutePath() + "\r\n" + e.getMessage(), e);
			logger.error("Unexpected error and exit, please check ............");
			System.exit(-1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Encounter IOException in openExcel : " + excel.getAbsolutePath() + "\r\n" + e.getMessage(), e);
			logger.error("Unexpected error and exit, please check ............");
			System.exit(-1);
		} catch (Exception ex) {
			logger.error("Encounter Exception in openExcel : " + excel.getAbsolutePath() + "\r\n" + ex.getMessage(), ex);
			logger.error("Unexpected error and exit, please check ............");
			System.exit(-1);
		}

	}

	private static void closeExcel(File excel) {
		try {
			fileOutQA = new FileOutputStream(excel);
			wbQA.write(fileOutQA);
			fileOutQA.close();
			// logger.info("Excel is closed !");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("Encounter exception in closeExcel : " + excel.getAbsolutePath() + "\r\n" + e.getMessage(), e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Encounter exception in closeExcel : " + excel.getAbsolutePath() + "\r\n" + e.getMessage(), e);
			e.printStackTrace();
		}

	}
	

	public void setMaxDifferenceCount(int maxDifferenceCount) {
		this.maxDifferenceCount = maxDifferenceCount;
	}

}
