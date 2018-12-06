package compare.configure;

import compare.model.PMTRecordModel;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class CSB2BEDIConfigQA {
	// Folder
	public static String folderPath;
	public static String testInputFileNamePath;

	// mappingScript config
	public static String rootPath;
	public static String InputDataFolder;
	public static String ActualCompleteFolder;
	public static String ActualFailFolder;
	public static String ExpectedFailFolder;
	public static String ExpectedUnknowErrorFolder;
	public static String ExpectedCompleteFolder;
	public static String E2EActualCompleteFolder;
	public static String E2EActualFailFolder;
	public static String E2EExpectedFailFolder;
	public static String E2EExpectedUnknowErrorFolder;
	public static String E2EExpectedCompleteFolder;
	public static String CS2ExtraFolder;
	public static String mappingrootPath;
	public static String pmtMappingScriptFile;
	public static String pmtPrepMappingScriptFile;
	public static String mappingUtilScriptFile;
	public static String javaBeanCommonScriptFile;
	public static String javaBeanMessageTypeScriptFile;
	public static String mappingUtilMessageTypeCommonScriptFile;
	public static String definitionFilePath;

	public static String E2EpmtMappingScriptFile;
	public static String E2EmappingUtilMessageTypeCommonScriptFile;
	public static String E2EdefinitionFilePath;

	public static String TP_ID;
	public static String MSG_TYPE_ID;
	public static String DIR_ID;
	public static String expect;
	public static String kukriworkFolder;
	// log
	public static String logPath;
	public static String groovyErrorLog;
	public static String belugaErrorLog;

	// compare use
	
	//public static String groovyOutputFolder;
	public static String modelPath;
	public static String defaultExtensionName;
	//public static String groovyExceptionFolder;
	//public static String groovyErrorFolder;
	//public static String groovyObsoleteFolder;
	public static String ModelFileFolder;
	// format
	public static String inputFormat;
	public static String outputFormat;
	// filename match
	public static int startFileNameIndex;
	public static int endFileNameIndex;
	// delimiter
	public static String recordDelimiter;
	public static String elementDelimiter;
	public static String subElementDelimiter;
	public static String escapeCharacter;
	// edi ignoreField
	public static String xmlIngoreProperties;
	public static String ediIngoreProperties;
	public static String uifIngoreProperties;
	public static ArrayList<String> ignoredList = null;
	public static ArrayList<String> specialReplaceList = null;
	public static ArrayList<String> specialRemoveList = null;
	
	public static String DBSID;
	public static String DBIP;
	public static String  workingConfigFilePath;

	public static String uif_fefinition;
	public static String e2eCS2DestinationQueue;
	public static String e2eCS2ReplyQueue;
	public static String NeedE2EOutput = "Y";


	public static int compareRound = 100;
	private static Logger logger = LogManager.getLogger(CSB2BEDIConfigQA.class.getName());
	
//	public static void main(String[] args) throws Exception {
//		initalization();
//		System.out.println(ediIngoreProperties);
//	}

	public static void initalization(PMTRecordModel pmtrecordModel, String kukriworkFolderS) throws Exception {
		kukriworkFolder = kukriworkFolderS;
		//rootPath = "D:/B2BEDIWorkingFolder"+"/"+pmtrecordModel.getPmtPath();
		System.out.println(CSB2BEDIConfigQA.class.getResource("").getPath());
		String gitISPath =  CSB2BEDIConfigQA.class.getResource("").getPath();
		String gitreconciToolFolder = null;
		if(gitISPath.contains("build/")){
			gitreconciToolFolder = CSB2BEDIConfigQA.class.getResource("").getPath().split("build/")[0];
		}else{
			gitreconciToolFolder = CSB2BEDIConfigQA.class.getResource("").getPath().split("out/")[0];
		}

		System.out.println(gitreconciToolFolder);
		String reconciToolFolder = gitreconciToolFolder.substring(1,gitreconciToolFolder.length());
		System.out.println("reconciToolFolder: "+reconciToolFolder);
		rootPath = pmtrecordModel.getPmtPath();	
		TP_ID = pmtrecordModel.getTpId();
		MSG_TYPE_ID = pmtrecordModel.getMsg_type();
		DIR_ID = pmtrecordModel.getDir();
		InputDataFolder = rootPath + "/InputData";
		ExpectedCompleteFolder = rootPath+"/ExpectedComplete";

		ActualCompleteFolder=rootPath + "/ActualComplete";
		ActualFailFolder=rootPath + "/ActualFail";
		ExpectedFailFolder=rootPath + "/ExpectedFail";
		ExpectedUnknowErrorFolder=rootPath + "/ExpectedUnknowError";

		E2EExpectedCompleteFolder = rootPath+"/E2EExpectedComplete";
		E2EActualCompleteFolder=rootPath + "/E2EActualComplete";
		E2EActualFailFolder=rootPath + "/E2EActualFail";
		E2EExpectedFailFolder=rootPath + "/E2EExpectedFail";
		E2EExpectedUnknowErrorFolder=rootPath + "/E2EExpectedUnknowError";
		CS2ExtraFolder = rootPath + "/CS2Extra";

		ModelFileFolder = reconciToolFolder+"\\src/main\\resources\\ModelFile/";


		File applicationFile =  new File(reconciToolFolder+"\\src\\main\\resources\\Application.properties");
		if(applicationFile.exists()){
			PropertiesConfiguration appProperties = new PropertiesConfiguration(applicationFile);
			if(appProperties.containsKey(pmtrecordModel.getMsg_type()+".E2E_CS2_DestinationQueue")){
				System.out.println(pmtrecordModel.getMsg_type()+".E2E_CS2_DestinationQueue");
				e2eCS2DestinationQueue = appProperties.getString(pmtrecordModel.getMsg_type()+".E2E_CS2_DestinationQueue").trim();
			}

			if(appProperties.containsKey(pmtrecordModel.getMsg_type()+".E2E_CS2_ReplyQueue")){
				e2eCS2ReplyQueue = appProperties.getString(pmtrecordModel.getMsg_type()+".E2E_CS2_ReplyQueue").trim();
			}

		}
		
		inputFormat = pmtrecordModel.getInputFormat();
		outputFormat = pmtrecordModel.getOutputFormat();
		// read mapping config
		System.out.println("rootPath: "+rootPath);
		System.out.println("TP_ID: "+TP_ID + " msg_type:"+MSG_TYPE_ID+" dir_id: "+DIR_ID);
		File F=new File(kukriworkFolder+"\\WorkingConfig\\MappingConfig");
		System.out.println(MSG_TYPE_ID+"_"+DIR_ID+"_"+TP_ID+"_"+"mapping.properties");
		workingConfigFilePath = kukriworkFolder+"\\WorkingConfig\\MappingConfig\\"+MSG_TYPE_ID+"_"+DIR_ID+"_"+TP_ID+"_"+"mapping.properties";
		File f1 = new File(workingConfigFilePath);
		PropertiesConfiguration mappingScriptProperties = new PropertiesConfiguration(f1);
						
		mappingrootPath = kukriworkFolder;
		javaBeanCommonScriptFile = mappingrootPath+mappingScriptProperties.getProperty("mappingScript.javaBeanCommonScriptFile").toString();
		javaBeanMessageTypeScriptFile = mappingrootPath+mappingScriptProperties.getProperty("mappingScript.javaBeanMessageTypeScriptFile").toString();
		mappingUtilScriptFile = mappingrootPath+mappingScriptProperties.getProperty("mappingScript.mappingUtilScriptFile").toString();
//		mappingUtilMessageTypeCommonScriptFile = mappingrootPath+mappingScriptProperties.getProperty("mappingScript.mappingUtilMessageTypeCommonScriptFile").toString();
		pmtMappingScriptFile = mappingrootPath+mappingScriptProperties.getProperty("mappingScript.pmtMappingScriptFile").toString();
		definitionFilePath = mappingrootPath+mappingScriptProperties.getProperty("mappingScript.definitionFilePath").toString();
	//	uif_fefinition = reconciToolFolder+"\\IG_Definition\\CAR_UIF_BKGUIF_IRIS2.xml";
		uif_fefinition = mappingrootPath+mappingScriptProperties.getProperty("mappingScript.definitionFilePath").toString();
		if(mappingScriptProperties.containsKey("mappingScript.pmtPrepMappingScriptFile"))
			pmtPrepMappingScriptFile = mappingrootPath+mappingScriptProperties.getProperty("mappingScript.pmtPrepMappingScriptFile").toString();
		else
			pmtPrepMappingScriptFile=null;

		if(mappingScriptProperties.containsKey("NeedE2EOutput")){
			NeedE2EOutput = mappingScriptProperties.getProperty("NeedE2EOutput").toString();
		}

		if(mappingScriptProperties.containsKey("compareRound")){
			compareRound = mappingScriptProperties.getInt("compareRound");
		}
		expect = mappingScriptProperties.getProperty("mappingScript.expect").toString();
		
		DBSID = mappingScriptProperties.getProperty("DBSID").toString();
		DBIP = mappingScriptProperties.getProperty("DBIP").toString();
		// read compare config
		File f2 = new File(kukriworkFolder+"\\src\\main\\resources\\compare.properties");
		PropertiesConfiguration compareProperties = new PropertiesConfiguration(f2);
		modelPath = compareProperties.getProperty("compare.modelPath").toString();
		defaultExtensionName = compareProperties.getProperty("compare.defaultExtensionName").toString();
		startFileNameIndex = Integer.parseInt(compareProperties.getProperty("compare.startFileNameIndex").toString());
		endFileNameIndex = Integer.parseInt(compareProperties.getProperty("compare.endFileNameIndex").toString());
		subElementDelimiter = compareProperties.getProperty("compare.subElementDelimiter").toString();
		escapeCharacter = compareProperties.getProperty("compare.escapeCharacter").toString();
		if (compareProperties.getProperty("compare.xmlIngoreProperties") != null) {
			xmlIngoreProperties = compareProperties.getProperty("compare.xmlIngoreProperties").toString();
		} 
		if (compareProperties.getProperty("compare.ediIngoreProperties") != null) {
			ediIngoreProperties = compareProperties.getProperty("compare.ediIngoreProperties").toString();
		}

		if (compareProperties.getProperty("compare.uifIngoreProperties") != null) {
			uifIngoreProperties = compareProperties.getProperty("compare.uifIngoreProperties").toString();
		}


		File acf =new File(ActualCompleteFolder);
		File af =new File(ActualFailFolder);

		File e2eacf =new File(E2EActualCompleteFolder);
		File e2eaf =new File(E2EActualFailFolder);

		File cs2Extraf =new File(CS2ExtraFolder);

		if (!acf.exists()) {
			acf.mkdirs();
		} 
		if (!af.exists()) {
			af.mkdirs();
		}
			
		if (!e2eacf.exists()) {
			e2eacf.mkdirs();
		}
		if (!e2eaf.exists()) {
			e2eaf.mkdirs();
		}

		if (!cs2Extraf.exists()) {
			cs2Extraf.mkdirs();
		}

	}
	
	public static void initFileFormat(String inputFileName){
		
		File df = new File(InputDataFolder);
		File bof = new File(ExpectedCompleteFolder);
		BufferedReader br;

			File df1 = new File(InputDataFolder+"/"+inputFileName);
			try {
				br = new BufferedReader(new FileReader(df1));
				String r = br.readLine();

				if (r.contains("xml")) {
					inputFormat = "XML";
					br.close();
				} else if (r.contains("ISA") || r.contains("UNB") || r.contains("UNA") || r.contains("ST")) {
					inputFormat = "EDI";
					br.close();
					/*if (r.contains("ISA")) {
						elementDelimiter = String.valueOf(r.charAt(3));
						recordDelimiter = String.valueOf(r.charAt(r.length() - 1));
					}else if(r.contains("ST")){
						elementDelimiter = String.valueOf(r.charAt(2));
						recordDelimiter = String.valueOf(r.charAt(r.length() - 1));
					}
					else if (r.contains("UNB")) {
						elementDelimiter = String.valueOf(r.charAt(3));
						recordDelimiter = String.valueOf(r.charAt(r.length() - 1));
					} else if (r.contains("UNA")) {
						elementDelimiter = String.valueOf(r.charAt(3));
						recordDelimiter = String.valueOf(r.charAt(r.length() - 1));
					}*/

				} else {
					inputFormat = "UIF";
					br.close();
				}
			} catch (Exception e) {
				logger.error(">>> initFileFormat - Unknow exception, plesae contact DEV to check, with your log.");
				logger.error("CSB2BEDIConfigQA.initFileFormat1", e);
				System.exit(-1);
			}
			if (bof.isDirectory()) {
				File of=new File(bof.list()[0]);
				File bof1=null;
				if(of.getName().equals("readme.txt")){
					bof1 = new File(ExpectedCompleteFolder + "/" + bof.list()[1]);
				}else{
					bof1 = new File(ExpectedCompleteFolder + "/" + bof.list()[0]);
				}
				
				try {
					br = new BufferedReader(new FileReader(bof1));
					String r = br.readLine();
					 PropertiesConfiguration config = null;
					if (r.contains("xml") || r.contains("XML")) {
						outputFormat = "XML";
						config = new PropertiesConfiguration(new File(kukriworkFolder+"\\src\\main\\resources\\xmlProerties.properties"));
						ignoredList = (ArrayList) config.getList("xml.ingoreCompareField");
						specialReplaceList = (ArrayList) config.getList("xml.specialReplace");
						specialRemoveList = (ArrayList) config.getList("xml."+CSB2BEDIConfigQA.MSG_TYPE_ID+".specialRemove");
						br.close();
					//} else if (r.contains("ISA") || r.contains("UNB") || r.contains("UNA") || r.contains("UNH") || r.contains("ST")) {
					} else if (r.startsWith("ISA") || r.startsWith("UNB") || r.startsWith("UNA") || r.startsWith("UNH") || r.startsWith("ST")) {
						outputFormat = "EDI";
						config = new PropertiesConfiguration(new File(kukriworkFolder+"\\src\\main\\resources\\ediProerties.properties"));
						 ignoredList = (ArrayList) config.getList("edi.ingoreCompareField");
						 specialReplaceList = (ArrayList) config.getList("edi.specialReplace");
						 specialRemoveList = (ArrayList) config.getList("edi."+CSB2BEDIConfigQA.MSG_TYPE_ID+".specialRemove");
						br.close();
						if (r.startsWith("ISA")) {
							elementDelimiter = String.valueOf(r.charAt(3));
							recordDelimiter = String.valueOf(r.charAt(r.length() - 1));
						}else if(r.startsWith("ST")){
							elementDelimiter = String.valueOf(r.charAt(2));
							recordDelimiter = String.valueOf(r.charAt(r.length() - 1));
						} else if (r.startsWith("UNB") || r.startsWith("UNH")) {
							elementDelimiter = String.valueOf(r.charAt(3));
							recordDelimiter = String.valueOf(r.charAt(r.length() - 1));
						} else if (r.startsWith("UNA")) {
							elementDelimiter = String.valueOf(r.charAt(3));
							recordDelimiter = String.valueOf(r.charAt(r.length() - 1));
						}
					} else {
						outputFormat = "UIF";
						config = new PropertiesConfiguration(new File(kukriworkFolder+"\\src\\main\\resources\\uifProerties.properties"));
						ignoredList = (ArrayList) config.getList("uif.ingoreCompareField");
						br.close();
					}
					logger.info("OutputFormat:" + outputFormat);
				} catch (Exception e) {
					logger.error(">>> initFileFormat2 - Unknow exception, plesae contact DEV to check, with your log.");
					logger.error("CSB2BEDIConfigQA.initFileFormat2", e);
					System.exit(-1);
				}
			}
		
		
	}

	public static void initFileFormatE2E(String inputFileName){

		String expectedF = E2EExpectedCompleteFolder;
		File df = new File(InputDataFolder);
		File bof = new File(expectedF);
		BufferedReader br;

		File df1 = new File(InputDataFolder+"/"+inputFileName);
		try {
			br = new BufferedReader(new FileReader(df1));
			String r = br.readLine();

			if (r.contains("xml")) {
				inputFormat = "XML";
				br.close();
			} else if (r.contains("ISA") || r.contains("UNB") || r.contains("UNA") || r.contains("ST")) {
				inputFormat = "EDI";
				br.close();
			} else {
				inputFormat = "UIF";
				br.close();
			}
		} catch (Exception e) {
			logger.error(">>> initFileFormat - Unknow exception, plesae contact DEV to check, with your log.");
			logger.error("CSB2BEDIConfigQA.initFileFormat1", e);
			System.exit(-1);
		}
		if (bof.isDirectory()) {
			File of=new File(bof.list()[0]);
			File bof1=null;
			if(of.getName().equals("readme.txt")){
				bof1 = new File(expectedF + "/" + bof.list()[1]);
			}else{
				bof1 = new File(expectedF + "/" + bof.list()[0]);
			}

			try {
				br = new BufferedReader(new FileReader(bof1));
				String r = br.readLine();
				PropertiesConfiguration config = null;
				if (r.contains("xml") || r.contains("XML")) {
					outputFormat = "XML";
					config = new PropertiesConfiguration(new File(kukriworkFolder+"\\src\\main\\resources\\xmlProerties.properties"));
					ignoredList = (ArrayList) config.getList("xml.ingoreCompareField");
					specialReplaceList = (ArrayList) config.getList("xml.specialReplace");
					specialRemoveList = (ArrayList) config.getList("xml."+CSB2BEDIConfigQA.MSG_TYPE_ID+".specialRemove");
					br.close();
					//} else if (r.contains("ISA") || r.contains("UNB") || r.contains("UNA") || r.contains("UNH") || r.contains("ST")) {
				} else if (r.startsWith("ISA") || r.startsWith("UNB") || r.startsWith("UNA") || r.startsWith("UNH") || r.startsWith("ST")) {
					outputFormat = "EDI";
					config = new PropertiesConfiguration(new File(kukriworkFolder+"\\src\\main\\resources\\ediProerties.properties"));
					ignoredList = (ArrayList) config.getList("edi.ingoreCompareField");
					specialReplaceList = (ArrayList) config.getList("edi.specialReplace");
					specialRemoveList = (ArrayList) config.getList("edi."+CSB2BEDIConfigQA.MSG_TYPE_ID+".specialRemove");
					br.close();
					if (r.startsWith("ISA")) {
						elementDelimiter = String.valueOf(r.charAt(3));
						recordDelimiter = String.valueOf(r.charAt(r.length() - 1));
					}else if(r.startsWith("ST")){
						elementDelimiter = String.valueOf(r.charAt(2));
						recordDelimiter = String.valueOf(r.charAt(r.length() - 1));
					} else if (r.startsWith("UNB") || r.startsWith("UNH")) {
						elementDelimiter = String.valueOf(r.charAt(3));
						recordDelimiter = String.valueOf(r.charAt(r.length() - 1));
					} else if (r.startsWith("UNA")) {
						elementDelimiter = String.valueOf(r.charAt(3));
						recordDelimiter = String.valueOf(r.charAt(r.length() - 1));
					}
				} else {
					outputFormat = "UIF";
					config = new PropertiesConfiguration(new File(kukriworkFolder+"\\src\\main\\resources\\uifProerties.properties"));
					ignoredList = (ArrayList) config.getList("uif.ingoreCompareField");
					br.close();
				}
			} catch (Exception e) {
				logger.error(">>> initFileFormat2 - Unknow exception, plesae contact DEV to check, with your log.");
				logger.error("CSB2BEDIConfigQA.initFileFormat2", e);
				System.exit(-1);
			}
		}



	}
}
