package compare.demo;

import compare.configure.CSB2BEDIConfigQA;
import compare.model.PMTRecordModel;
import compare.util.FileProcessorQA;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.ArrayList;


public class GroovyMappingLocalE2EQA {
	private static Logger logger = LogManager.getLogger(GroovyMappingLocalE2EQA.class.getName());
	public static byte[] mapUtilBytes = null;
	public static String mapUtilName = null;
	private static XSSFWorkbook wb = null;
	private static FileOutputStream fileOut = null;
	private static XSSFSheet sheet = null;
	private static InputStream fis = null;


	static int workingThreadNumber = 30;


	public GroovyMappingLocalE2EQA() throws IOException {
	}

	public static void run(String kukriworkFolder) throws Exception {

		logger.info("E2E Start, pre-setting workingThreadNumber asking: "+workingThreadNumber);
		
		long tsStart = System.currentTimeMillis();
		try {
			//System.out.println(System.getProperty("user.dir"));
			//Read runing TP info from ./WorkingConfig/config.txt
			File f = new File(kukriworkFolder+"\\WorkingConfig\\config.txt");
			String workFolder= FileProcessorQA.getWorkFolder(f);
			logger.info("workFolder: "+workFolder);
			File workF=new File(workFolder);
			if(workF.exists()&&workF.isDirectory()){
				File f1 = new File(workFolder+"/pmt_config.txt");
				ArrayList<String> runList = FileProcessorQA.getRunTPList(f1);
				if(runList!=null){
					for (int i=0;i<runList.size();i++) {
						String runLine = runList.get(i);
						if (runLine==null || runLine.trim().length()==0 || runLine.startsWith("##")) {
							continue;
						}
						logger.info("==================\r\nTesting with line : " + runLine+"\r\n===================="); 
						PMTRecordModel pmtrecordModel = new PMTRecordModel(runLine);
						CSB2BEDIConfigQA.initalization(pmtrecordModel,kukriworkFolder);
						String excelPath=CSB2BEDIConfigQA.rootPath+"/"+CSB2BEDIConfigQA.TP_ID+".xlsx";
						File excel = new File(excelPath);
						logger.info("inputFormat : " + CSB2BEDIConfigQA.inputFormat);
						logger.info("outputFormat : " + CSB2BEDIConfigQA.outputFormat);
						File dataFolder= new File(CSB2BEDIConfigQA.rootPath);
				
						if(dataFolder.exists()&&dataFolder.isDirectory()){
							if(!excel.exists()){
								logger.info("Prepare test caseexcel first" );
								if(prepareTestExcel(excel,"")){
									DemoGroovy_Common_QA demoQA = new DemoGroovy_Common_QA(workingThreadNumber);
								}else{
									logger.info("Some expected not found ,please check ." );
								}
							} else {
								DemoGroovy_Common_QA demoQA = new DemoGroovy_Common_QA(workingThreadNumber);
							}
						} else {
							logger.error("Error: Not found "+CSB2BEDIConfigQA.rootPath);
						}
					}
				}
			}else{
				logger.error("Not found workFolder!! Please check your ./WorkingConfig/config.txt .");
			}
			
			long tsFinish = System.currentTimeMillis();
			
			logger.info("E2E Finished. Total cost: "+(tsFinish-tsStart)+" ms.");
			
		} catch (Exception e) {
			Throwable t = e.getCause() == null ? e : e.getCause();
			while (t.getCause() != null) {
				t = t.getCause();
			}
			t.printStackTrace();
			
			logger.error(">>> GroovyMappingLocalE2EQA.Main - Unknow exception, plesae contact DEV to check, with your log.");
			logger.error("Unexpected error and exit, please check ............");
			
		}
	}

	public static void runCOSUE2E(String kukriworkFolder) throws Exception {
		logger.info("E2E Start, pre-setting workingThreadNumber asking: "+workingThreadNumber);

		long tsStart = System.currentTimeMillis();
		try {
			//System.out.println(System.getProperty("user.dir"));
			//Read runing TP info from ./WorkingConfig/config.txt
			File f = new File(kukriworkFolder+"\\WorkingConfig\\config.txt");
			String workFolder=FileProcessorQA.getWorkFolder(f);
			logger.info("workFolder: "+workFolder);
			File workF=new File(workFolder);
			if(workF.exists()&&workF.isDirectory()){
				File f1 = new File(workFolder+"/pmt_config.txt");
				ArrayList<String> runList = FileProcessorQA.getRunTPList(f1);
				if(runList!=null){
					for (int i=0;i<runList.size();i++) {
						String runLine = runList.get(i);
						if (runLine==null || runLine.trim().length()==0 || runLine.startsWith("##")) {
							continue;
						}
						logger.info("==================\r\nTesting with line : " + runLine+"\r\n====================");
						PMTRecordModel pmtrecordModel = new PMTRecordModel(runLine);
						pmtrecordModel.setRunFlag("E2E");
						CSB2BEDIConfigQA.initalization(pmtrecordModel,kukriworkFolder);
						String excelPath=CSB2BEDIConfigQA.rootPath+"/"+CSB2BEDIConfigQA.TP_ID+".xlsx";
						File excel = new File(excelPath);
						logger.info("inputFormat : " + CSB2BEDIConfigQA.inputFormat);
						logger.info("outputFormat : " + CSB2BEDIConfigQA.outputFormat);
						File dataFolder= new File(CSB2BEDIConfigQA.rootPath);

						if(dataFolder.exists()&&dataFolder.isDirectory()){
							if(!excel.exists()){
								logger.info("Prepare test caseexcel first" );
								if(prepareTestExcel(excel,"")){
									DemoGroovy_Common_E2E demoQA = new DemoGroovy_Common_E2E(workingThreadNumber);
								}else{
									logger.info("Some expected not found ,please check ." );
								}
							} else {
								DemoGroovy_Common_E2E demoQA = new DemoGroovy_Common_E2E(workingThreadNumber);
							}
						} else {
							logger.error("Error: Not found "+CSB2BEDIConfigQA.rootPath);
						}
					}
				}
			}else{
				logger.error("Not found workFolder!! Please check your ./WorkingConfig/config.txt .");
			}

			long tsFinish = System.currentTimeMillis();

			logger.info("E2E Finished. Total cost: "+(tsFinish-tsStart)+" ms.");

		} catch (Exception e) {
			Throwable t = e.getCause() == null ? e : e.getCause();
			while (t.getCause() != null) {
				t = t.getCause();
			}
			t.printStackTrace();

			logger.error(">>> GroovyMappingLocalE2EQA.Main - Unknow exception, plesae contact DEV to check, with your log.");
			logger.error("Unexpected error and exit, please check ............");

		}
	}

	public static boolean prepareTestExcel(File excel,String rootPath) throws IOException {
		boolean flag=true;
		File modelF=new ClassPathResource("Auto-Log-Model.xlsx").getFile();
		if(modelF.exists()){
			try {
				FileUtils.copyFile(modelF, excel);
				logger.info("Satrt to fill test data to excel.");
				File inputF= new File(rootPath+"\\ExpectedComplete");
				openExcel(excel);
				int rowC=sheet.getLastRowNum();
				logger.info(rowC);
				if(rowC>1){
					for(int n=rowC;n>0;n--)
					{
						logger.info("n="+n);
						sheet.removeRow(sheet.getRow(n));
					}
				}
				closeExcel(excel);
				File[] inputFiles=inputF.listFiles();  
				openExcel(excel);
				int row=0;
				logger.info("inputFiles.length="+inputFiles.length);
				for(int i=0;i<inputFiles.length;i++){
					File f=inputFiles[i];
					if(f.exists()&&f.isFile()&&!f.getName().equals("readme.txt")){
						row=1+i;
						sheet.createRow(row);
						XSSFCell runflag = sheet.getRow(row).createCell(0);
						runflag.setCellValue("t");
						/*XSSFCell InputPath = sheet.getRow(row).createCell(1);
						InputPath.setCellValue("/InputData");*/
						XSSFCell InputFile = sheet.getRow(row).createCell(1);
						InputFile.setCellValue(f.getName());
						XSSFCell expectedStatus = sheet.getRow(row).createCell(2);
						XSSFCell expectedFile = sheet.getRow(row).createCell(3);
						XSSFCell actualStatus = sheet.getRow(row).createCell(5);
						XSSFCell actualFile = sheet.getRow(row).createCell(6);
						File of=new File(rootPath+"\\ExpectedComplete"+"/"+f.getName());
						File ac=new File(rootPath+"\\ActualComplete"+"/"+f.getName());
						File ff=new File("" +f.getName());
						File uf=new File(CSB2BEDIConfigQA.ExpectedUnknowErrorFolder+"/"+f.getName());

						if(of.exists()){
							expectedFile.setCellValue(f.getName());
							expectedStatus.setCellValue("C");
						//	flag=true;
						}else{
							logger.info("Not found expected for "+f.getName());
							flag=false;
							expectedStatus.setCellValue("E");
						}
						if(ac.exists()){
							actualFile.setCellValue(f.getName());
							actualStatus.setCellValue("C");
							//	flag=true;
						}else{
							logger.info("Not found expected for "+f.getName());
							flag=false;
							actualStatus.setCellValue("E");
						}
					}
				}
				closeExcel(excel);
				
			} catch (IOException e) {
				logger.error(">>> GroovyMappingLocalE2EQA.prepareTestExcel - Unexpected fatal exception, plesae contact DEV to check, with your log.");
				logger.error("GroovyMappingLocalE2EQA.prepareTestExcel", e);
				System.exit(-1);
			}
		}
		
		return flag;
	}
	
	private static void openExcel(File excel) {
		try {
			fis = new FileInputStream(excel);
			wb = new XSSFWorkbook(fis);

			sheet = wb.getSheetAt(0);
			// logger.info("Excel is opened !");
		} catch (Exception e) {
			logger.error(">>> GroovyMappingLocalE2EQA.openExcel - Unexpected fatal exception, plesae contact DEV to check, with your log.");
			logger.error("GroovyMappingLocalE2EQA.openExcel", e);
			System.exit(-1);
		}
	}

	private static void closeExcel(File excel) {
		try {
			fileOut = new FileOutputStream(excel);
			wb.write(fileOut);
			fileOut.close();
			// logger.info("Excel is closed !");
		} catch (FileNotFoundException e) {
			logger.error("Encounter exception : " + e.getMessage(), e);
		} catch (IOException e) {
			logger.error("Encounter exception : " + e.getMessage(), e);
		}
	}
}
