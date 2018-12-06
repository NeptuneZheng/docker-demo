package compare.demo;

import compare.configure.CSB2BEDIConfigQA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Element;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class DemoGroovy_Common_E2E {

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

	private static Logger logger = LogManager.getLogger(DemoGroovy_Common_E2E.class.getName());

	private static String thisClassName = DemoGroovy_Common_E2E.class.getName().substring(DemoGroovy_Common_E2E.class.getName().lastIndexOf(".") + 1,
			DemoGroovy_Common_E2E.class.getName().length());
	private static String groovyMappingClassName = pmtMappingScriptFile.substring(pmtMappingScriptFile.lastIndexOf("/") + 1,
			pmtMappingScriptFile.length());

	public static int successCount = 0;
	public static int failCount = 0;
	public static int exceptionCount = 0;

	public static boolean isRunning = false;

//	public static Hashtable<Integer, Long> MappingThreadE2EWorkingIds = new Hashtable<Integer, Long>();
//	public static List<Integer> MappingThreadE2ECompleteIds = new ArrayList<Integer>();

	public static Hashtable<String, MappingThreadE2E> workingThreads = new Hashtable<String, MappingThreadE2E>();
	int maxConcurrenThreadCount = 20;
    private static SimpleDateFormat sdfMMddHHmmssSSS = new SimpleDateFormat("MMddHHmmssSSS");
	public DemoGroovy_Common_E2E(int threadCount) {
		this.maxConcurrenThreadCount = threadCount;
	}
	
	public MappingThreadE2E getWorkingThread() {
		for (MappingThreadE2E tt : workingThreads.values()) {
			if (! tt.getWorkingStatus()) {
				return tt;
			}
		}
		return null;
	}
	
	public void listWorkingThreads() {
		System.out.println("---- working threads ----");
		for (MappingThreadE2E tt : workingThreads.values()) {
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
		for (MappingThreadE2E tt : workingThreads.values()) {
			if (tt.getWorkingStatus()) {
				ret++;
			}
		}
		return ret;
	}
	
	public void closeWorkingThreads() {
		for (MappingThreadE2E tt : workingThreads.values()) {
			tt.stopService();
		}
		try {
			Thread.sleep(2000);
		} catch (Exception ex) {}
		for (MappingThreadE2E tt : workingThreads.values()) {
			tt.closeDBConnection();
		}
		isRunning = false;

	}
	
//	public void demo(File excel) throws Exception {
//		isRunning = true;
//
//		ConnectionForQA testDBConn = new ConnectionForQA();
//		//Connection conn = null;
//		JMSHandler jms = new JMSHandler("csb2bediqa1ems01:9222","admin","csadmin");
//		jms.connectEMSServer();
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
//			if (!StringUtils.isEmpty(recordModel.getExpectedFile())&&!FunctionHelperQA.fileExist((i + 1),recordModel.getE2eExpectedFilePath()))
//				fileExistFlag = false;
//			if (!StringUtils.isEmpty(recordModel.getExpectedFail())&&!FunctionHelperQA.fileExist((i + 1),recordModel.getDbFilePath()))
//				fileExistFlag = false;
//			recordModel.setRow(i);
//			recordModel.setExcel(excel);
//			//recordModel.setBookingNumber(UUID.randomUUID().toString().replaceAll("-", ""));
//			String bn = FunctionHelperQA.getRandomString("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ",30);
//			recordModel.setBookingNumber(bn);
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
//			CSB2BEDIConfigQA.initFileFormatE2E(recordModelList.get(0).getInputFile());
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
//		scripts.put(new File(mappingUtilMessageTypeCommonScriptFile).getName(), LocalFileUtilQA.readBigFile(mappingUtilMessageTypeCommonScriptFile));
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
//				MappingThreadE2E t = new MappingThreadE2E(tid, conn, jms);
//				workingThreads.put(tid, t);
//				t.start();
//			}
//
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
//				MappingThreadE2E MappingThreadE2E = getWorkingThread();
//				while (MappingThreadE2E == null) {
//					System.out.println("waiting working thread ... ");
//					Thread.sleep(2000);
//					long now = System.currentTimeMillis();
//					if (now - waitFrom > 15000) {
//						listWorkingThreads();
//						waitFrom = System.currentTimeMillis();
//					}
//					MappingThreadE2E = getWorkingThread();
//				}
//
//				int threadNumber = i+1;
////				DemoGroovy_Common_QA.MappingThreadE2EWorkingIds.put(threadNumber, System.currentTimeMillis());
////				System.out.println("Thread "+threadNumber+" starting...");
//
//				// *** start mapping
//				MappingThreadE2E.setRunningParams(threadNumber, recordModel, instance, instancePrep);
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
//			Connection conn = testDBConn.getB2BEDIQA1_DEV_DBConn();
//			DBHandler dbHandler =  new DBHandler(conn);
//		//	ArrayList<RecordModelQA> recordModelListCopy = new ArrayList<RecordModelQA>();
//		//	recordModelListCopy.addAll(recordModelList);
//			processOutMsg(recordModelList,dbHandler);
//            differenceHandler.saveAll();
//			updateResultToExcel(excel,recordModelList);
//			summaryToDoList(excel,recordModelList);
//			if(conn!=null){
//				conn.close();
//			}
//		}
//		jms.disconnectEMSServer();
//		logger.warn("------------------Finish--------------");
//	}
//
//	public void processOutMsg(ArrayList<RecordModelQA> recordModelList,DBHandler dbHandler)throws Exception {
//		logger.warn("Start to check output message for E2E .");
//		logger.warn(recordModelList.size());
//		int round = 0 ;
//		boolean allFinish = false;
//		int total = recordModelList.size();
//		while(!allFinish){
//			round ++ ;
//			logger.warn("------Round "+round);
//			for (int n = 0; n < recordModelList.size(); n++) {
//				RecordModelQA recordModel = recordModelList.get(n);
//				if(recordModel.isOutPutFlag()){
//					continue;
//				}
//				String sql = null;
//				logger.warn(recordModel.getInputFile());
//				logger.warn(recordModel.getInMsgResID());
//				if(FunctionHelperQA.isNotEmpty(recordModel.getInMsgResID())){
////					sql = " select MSG_REQ_ID from b2b_msg_req_detail where tp_id ='COSU_MIGRATION' and msg_req_id in (" +
////							" SELECT MSG_REQ_ID FROM B2B_MSG_REQ_DETAIL_ASSO WHERE MSG_REQ_DETAIL_ASSO_TYPE_ID = 'ASS_MSG_REQ_ID'" +
////							" AND DIR_ID = 'O' AND MSG_REQ_DETAIL_ASSO in (SELECT MSG_REQ_ID FROM B2B_MSG_REQ_DETAIL_ASSO WHERE " +
////							" MSG_REQ_DETAIL_ASSO_TYPE_ID='ASS_MSG_REQ_ID' AND DIR_ID ='O'" +
////							" AND MSG_REQ_DETAIL_ASSO like '"+recordModel.getInMsgResID()+ "%')) and dir_id = 'O'";
//
//					sql = " select MSG_REQ_ID from b2b_msg_req_detail where tp_id in ('COSU_MIGRATION','COSU_XML','COSU_UIF') and msg_req_id in (" +
//							" SELECT MSG_REQ_ID FROM B2B_MSG_REQ_DETAIL_ASSO WHERE MSG_REQ_DETAIL_ASSO_TYPE_ID = 'ASS_MSG_REQ_ID'" +
//							" AND DIR_ID = 'O' AND MSG_REQ_DETAIL_ASSO LIKE '"+recordModel.getInMsgResID()+ "%') and dir_id = 'O'";
//					logger.warn(sql);
//					String outMsgId = dbHandler.getStringBySQL(sql);
//					logger.warn("outMsgId="+outMsgId);
//					if(FunctionHelperQA.isNotEmpty(outMsgId)){
//						recordModel.setOutMsgResID(outMsgId);
//					}
//				}
//
//				if(FunctionHelperQA.isNotEmpty(recordModel.getOutMsgResID())){
//					logger.info("Try to get CS2 extra file:");
//					sql = "select message from b2b_msg_req_detail where tp_id in ('COSU_MIGRATION','COSU_XML','COSU_UIF') and msg_req_id='"+ recordModel.getOutMsgResID() + "' and dir_id='O'";
//					String cs2ExtraText= "";
//					logger.info(sql);
//					cs2ExtraText = dbHandler.getStringFromBlob(sql);
//					if(FunctionHelperQA.isNotEmpty(cs2ExtraText)){
//						//  logger.warn(outputText);
//						LocalFileUtilQA.writeToFile(
//								recordModel.getCs2ExtraFilePath(),
//								cs2ExtraText, false);
////                            recordModelList.remove(n);
////                            n--;
//					}
//
//					if(CSB2BEDIConfigQA.NeedE2EOutput.equals("N")){
//						recordModel.setActualStatus("C");
//						recordModel.setOutPutFlag(true);
//						total--;
////						recordModelList.remove(n);
////						n--;
//						if(total==0){
//							return;
//						}
//						continue;
//					}
//
//                    sql = "select proc_sts_id from b2b_msg_req_process where proc_type_id='X2EX' and msg_req_id='"+ recordModel.getOutMsgResID() + "'";
//					logger.info(sql);
//					String status = dbHandler.getStringBySQL(sql);
//					if(FunctionHelperQA.isNotEmpty(status)&&(status.equals("C")||status.equals("E"))){
//
//						recordModel.setActualStatus(status);
//					}
//					logger.info(recordModel.getOutMsgResID()+":"+status);
//				}
//
//                String outputText = "";
//				if(FunctionHelperQA.isNotEmpty(recordModel.getActualStatus())){
//					if(recordModel.getActualStatus().startsWith("C")){
//						logger.info("Try to get E2E output file:");
//						int max_proc = dbHandler.getMaxProcId(recordModel.getOutMsgResID());
//						sql ="select out_msg from b2b_msg_req_process where  proc_seq='"+max_proc+"' and msg_req_id='"+ recordModel.getOutMsgResID() + "'";
//						 outputText =  dbHandler.getStringFromBlob(sql);
//                        logger.info(sql);
//						if(FunctionHelperQA.isNotEmpty(outputText)){
//							if(outputText.startsWith("<?xml")){
//								String data[] = outputText.split("\n");
//								if(data.length<5){
//                                    LocalFileUtilQA.writeToFile(
//                                            recordModel.getE2eActualFilePath(),
//                                            outputText, false);
//                                  //  File xmlFile = new File(recordModel.getE2eActualFilePath())
//									//FunctionHelperQA.prettyFormatXML(outputText,recordModel.getE2eActualFilePath());
//                                    FunctionHelperQA.prettyXMLByTools(new File(recordModel.getE2eActualFilePath()));
//
//								}else{
//									LocalFileUtilQA.writeToFile(
//											recordModel.getE2eActualFilePath(),
//											outputText, false);
//								}
//								//  outputText = FunctionHelper.cleanXml(outputText);
//
//						}else{
//								//  logger.warn(outputText);
//								LocalFileUtilQA.writeToFile(
//										recordModel.getE2eActualFilePath(),
//										outputText, false);
//
////                            recordModelList.remove(n);
////                            n--;
//							}
//							recordModel.setActualFile(recordModel.getInputFile());
//							recordModel.setOutPutFlag(true);
//							logger.info("Start to compare output.");
//						}
//
//					}else if(recordModel.getActualStatus().startsWith("E")){
//						recordModel.setOutPutFlag(true);
//						sql = "select INT_ERR_MSG from b2b_msg_req_detail where msg_req_id='"+ recordModel.getOutMsgResID() + "'";
//						String errorMsg = dbHandler.getStringBySQL(sql);
//						if(FunctionHelperQA.isNotEmpty(errorMsg)){
//							LocalFileUtilQA
//									.writeToFile(
//											CSB2BEDIConfigQA.E2EActualFailFolder + "/"
//													+ recordModel.getInputFile(),
//											errorMsg, false);
//							recordModel.setActualFail(recordModel.getInputFile());
//						}
////                        recordModelList.remove(n);
////                        n--;
//                    }
//				}
//
//				if(recordModel.isOutPutFlag()){
//
//					compareResult(recordModel);
//					total--;
////                    recordModelList.remove(n);
////                        n--;
//				}
//			}
//
//          //  if (recordModelList.size() == 0)
//			if(total==0)
//            {
//                logger.warn("Finish !");
//                return;
//            }
//
//			if(round>CSB2BEDIConfigQA.compareRound){
//				logger.warn("Compare " +round
//						+ " rounds," + recordModelList.size()
//						+ " row(s) haven't been compared.");
//				return;
//			}
//            logger.info("Wait 5 seconds.");
//            Thread
//                    .sleep(5 * 1000);
//		}
//
//
//
//	}
//
//	public void compareResult(RecordModelQA recordModel){
//		logger.warn("-------------------Start to compare---------------");
//
//		logger.info("Compare row "+recordModel.getRow()+",input file is "+recordModel.getInputFile());
//		logger.info("Actual status:"+recordModel.getActualStatus());
//		if(!StringUtils.isEmpty(recordModel.getActualStatus())){
//			logger.info("Compare status......");
//			if(recordModel.getActualStatus().equals(recordModel.getExpectedStatus()))
//				recordModel.setStatusResult("Pass");
//			else
//				recordModel.setStatusResult("Fail");
//			logger.warn("Status result :"+recordModel.getStatusResult());
//		}
//		if(!StringUtils.isEmpty(recordModel.getActualFile())&&!StringUtils.isEmpty(recordModel.getExpectedFile())){
//			String FileResult=compareFile(recordModel);
//			recordModel.setFileResult(FileResult);
//
//		}
//
//		if(!StringUtils.isEmpty(recordModel.getStatusResult())&&recordModel.getStatusResult().equals("Pass")){
//			if(!StringUtils.isEmpty(recordModel.getActualFail())&&!StringUtils.isEmpty(recordModel.getExpectedFail())){
//				logger.info("Compare Error result......");
//
//				String expectedDBPath=CSB2BEDIConfigQA.ExpectedFailFolder+"/"+recordModel.getExpectedFail();
//				String actualDBPath=CSB2BEDIConfigQA.ActualFailFolder +"/"+ recordModel.getActualFail();
//				logger.info("expectedDBPath:"+expectedDBPath);
//				logger.info("actualDBPath="+actualDBPath);
//
//				//compareErrorMsg(recordModel);
//
//				logger.warn("DB result :"+recordModel.getDbresult());
//			}
//		}else{
//			logger.info("Status result is null or Pass , no need to compare Error msg.");
//		}
//
////			}
////			closeExcel(excel);
//	}
//
//	public String compareFile(RecordModelQA recordModel) {
//		String FileResult=null;
//		logger.info("Compare output file......");
//		try {
//
//
//			String expectedStr = FileProcessorQA.getComparableContent(
//					new File(recordModel.getE2eExpectedFilePath()),CSB2BEDIConfigQA.outputFormat);
//			String actualStr = FileProcessorQA.getComparableContent(new File(
//							recordModel.getE2eActualFilePath()),
//                    CSB2BEDIConfigQA.outputFormat);
//
//			if (expectedStr.equals(actualStr)) {
//				//recordModel.setFileResult("Pass");
//				FileResult="Pass";
//				logger.warn("File result : Pass");
//			} else {
//				//recordModel.setFileResult("Fail");
//				FileResult="Fail";
//				logger.warn("File result : Fail");
//
//                logger.warn(recordModel.getExcel());
//                differenceHandler.logDifference(CSB2BEDIConfigQA.InputDataFolder+"/"+recordModel
//								.getInputFile(), recordModel
//								.getE2eExpectedFilePath(), recordModel
//								.getE2eActualFilePath(), expectedStr,
//						actualStr, recordModel.getExcel());
//			}
//
//		} catch (IOException e) {
//			logger.error(">>> compareFile - Unknow fatal exception, plesae contact DEV to check, with your log.");
//			logger.error("MappintThread.compareFile", e);
//			logger.error("recordModel: "+recordModel.getExpectedStatus()+" v.s. "+recordModel.getActualStatus()+", expected file: "+recordModel.getExpectedFile());
//			System.exit(-1);
//		}
//
//		return FileResult;
//
//	}
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
//			XSSFCell cellINMsgID = sheetQA.getRow(recordModel.getRow()).createCell(11);
//			XSSFCell cellOutMsgID = sheetQA.getRow(recordModel.getRow()).createCell(12);
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
//			if(!StringUtils.isEmpty(recordModel.getInMsgResID()))
//				cellINMsgID.setCellValue(recordModel.getInMsgResID());
//			else
//				cellINMsgID.setCellValue("");
//
//			if(!StringUtils.isEmpty(recordModel.getOutMsgResID()))
//				cellOutMsgID.setCellValue(recordModel.getOutMsgResID());
//			else
//				cellOutMsgID.setCellValue("");
//
//		}
//		closeExcel(excel);
//	}
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

}
