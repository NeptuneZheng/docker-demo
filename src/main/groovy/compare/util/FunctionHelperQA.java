package compare.util;

import groovy.util.Node;
import groovy.util.XmlParser;
import groovy.xml.XmlUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class FunctionHelperQA {
	
	static Logger logger = LogManager.getLogger(FunctionHelperQA.class.getName());
	
	public static String readContent(File file) {
		
		String body = null;
		try {
			body = LocalFileUtilQA.readBigFileContentDirectly(file.getAbsolutePath());
		} catch (Exception e) {
			logger.error(">>> readContent - Unknow fatal exception, plesae contact DEV to check, with your log.");
			logger.error("FunctionHelperQA.readContent", e);
			logger.error("file: "+file.getAbsolutePath());
			System.exit(1);
		}
		return body;
	}
	
	public static boolean checkInColumnList(List list, String key) {
		boolean inFlag = false;

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).toString().toLowerCase().equals(
					key.toLowerCase().trim())) {
				inFlag = true;
				break;
			}
		}
		return inFlag;
	}

	public static void prettyXMLByTool(File xmlfile) {
		String filepath=xmlfile.getAbsolutePath();
		String newFilePath=xmlfile.getAbsolutePath()+"-temp";
		String root=filepath.split(":")[0];
		logger.info("root="+root);
		String command = "cmd /c "+ root + ":\\1_B2BEDI_Revamp\\toolsxml.exe ed \"" + filepath +"\""+ "> \""+newFilePath+"\"";
		String command1= "taskkill /f /t /im xml.exe";

		try {
			logger.info(command);
			Process process = Runtime.getRuntime().exec(command);
			try {
				Thread.sleep(2*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info(command1);
			process = Runtime.getRuntime().exec(command1);
			try {
				Thread.sleep(2*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			process.destroy();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		File file = new File(filepath);
		File newFile = new File(newFilePath);
		System.gc();
		//	logger.info("new file length="+newFile.length());
		if(newFile.length()>0)
		{
			System.gc();
			xmlfile.delete();
			newFile.renameTo(file);
		}else
		{
			System.gc();
			newFile.delete();
		}

	}


	public static boolean prettyFormatXML(String xmlFilePath) {
		File f1= new File(xmlFilePath);
		String fileContent = FunctionHelperQA.readContent(f1);
		String data[] = fileContent.split("\n");
		if(data!=null && data.length > 5)
			return true;
		try {
			FileReader fr;
			fr = new FileReader(new File(xmlFilePath));
			SAXReader reader = new SAXReader();

		//	Document document = reader.read(new ByteArrayInputStream(fileContent.getBytes()));
			Document document = reader.read(fr);
			fr.close();

			 OutputFormat format = OutputFormat.createCompactFormat(); // ����XML�ĵ������ʽ
             format.setEncoding("UTF-8"); // ����XML�ĵ��ı�������             
			format.setTrimText(false);
			//format.setExpandEmptyElements(false);
			XMLWriter output = new XMLWriter(new FileWriter(new File(
					xmlFilePath)), format);

			output.write(document);
			output.close();
			prettyFormatXML2(xmlFilePath);
		} catch (Exception e) {
			logger.error("FunctionHelperQA.prettyFormatXML", e);
			logger.error("xmlFilePath: "+xmlFilePath);
			return false;
		}
		return true;
	}
	
	public static boolean prettyFormatXML2(String xmlFilePath) {

		try {
			FileReader fr;
			fr = new FileReader(new File(xmlFilePath));
			SAXReader reader = new SAXReader();

		//	Document document = reader.read(new ByteArrayInputStream(fileContent.getBytes()));
			Document document = reader.read(fr);
			fr.close();

			 OutputFormat format = OutputFormat.createPrettyPrint(); // ����XML�ĵ������ʽ
             format.setEncoding("UTF-8"); // ����XML�ĵ��ı�������             
			format.setTrimText(false);
			//format.setExpandEmptyElements(false);
			XMLWriter output = new XMLWriter(new FileWriter(new File(
					xmlFilePath)), format);

			output.write(document);
			output.close();

		} catch (Exception e) {
			logger.error("FunctionHelperQA.prettyFormatXML2", e);
			logger.error("xmlFilePath2: "+xmlFilePath);
			return false;
		}
		return true;
	}
	
	public static boolean checkExcelAvailable(File excel) {
		if (!excel.renameTo(excel)) {
			logger.error("Auto-Log excel : " + excel.getAbsolutePath());
			logger.error("  Error! Please make sure your Auto-Log excel exists and it is closed !  ");

			return false;
		} else {
			return true;
		}

	}
	public static boolean backupFile(File file) {
		File backFile = new File(file.getParent()+"/Auto-Backup-" + file.getName());
		if (file.exists()) {
			try {
				FileUtils.copyFile(file, backFile);
				return true;
			} catch (Exception e) {
				logger.error("FunctionHelperQA.backupFile", e);
				logger.error("file: "+file.getAbsolutePath());
				return false;
			}
		} else {
			return false;
		}

	}
	
	public static boolean fileExist(int row, String filePath) {

		boolean existFlag = true;
		File file = new File(filePath);
		if ((!file.isFile()) || (!file.exists())) {
			logger.error("File doesn't exist: Row " + row
					+ " , file: " + filePath);

			existFlag = false;
			
		}		
		return existFlag;
	}

	public static boolean notEmpty(Object data) {
		if (data == null)
			return false;

		if (data instanceof String) {
			return (((String) data).trim().length() > 0);
		} else {
			return (data != null && data.toString().trim().length()>0);
		}
	}

	public static boolean isNotEmpty(Object data) {
		return notEmpty(data);
	}

	public static boolean isEmpty(Object data) {
		return (! notEmpty(data));
	}


	public static String modifyEDI(String fileContent, String modifyDetailStr,
								 String newValue) {
		String[] modifyDetail;
		String newText = fileContent;
		String oldValue=null;
		if (!modifyDetailStr.contains("-")) {
			logger.warn("Can't modify edi field by config like "
					+ modifyDetailStr);
			return newText;
		} else {
			modifyDetail = modifyDetailStr.split("-");
			if (modifyDetail.length < 2) {
				logger.warn("Can't modify edi field by config like "
						+ modifyDetailStr);
				return newText;
			}
		}
		if (modifyDetail.length >= 2) {

			fileContent = fileContent.replaceAll("\r\n", "\n");
			fileContent = fileContent.replaceAll("\r", "\n");

			char delimiter = 0;
			String seperator = "+";
			String subSeperator = ":";
			String releaseIndicator = "?";

			if (fileContent.startsWith("ISA")) {
				delimiter = fileContent.charAt(105);
				seperator = String.valueOf(fileContent.charAt(3));
				subSeperator = fileContent.substring(104, 105);
			} else if (fileContent.startsWith("UNA")) {
				delimiter = fileContent.charAt(fileContent.indexOf("UNA") + 8);
				seperator = String.valueOf(fileContent.charAt(4));
				subSeperator = fileContent.substring(3, 4);
				releaseIndicator = fileContent.substring(6, 7);
			}

			else {
				seperator = String.valueOf(fileContent.charAt(3));
				String data[] = fileContent.split("UNH");

				if (data[0].equals("\n") || data[0].endsWith("\n"))
					delimiter = fileContent.charAt(data[0].length() - 2);
				else
					delimiter = fileContent.charAt(data[0].length() - 1);
				// logger.info("delimiter if not have UNA:" + delimiter);

			}


			logger.info("Delimiter :" + delimiter + "  Seperator :" + seperator
					+ "  Sub-Seperator :" + subSeperator
					+ "  Release-Indicator :" + releaseIndicator);
			String releaseDelimiter = "releaseDelimiterCode";
			fileContent =fileContent.replaceAll("\\"+releaseIndicator+delimiter,releaseDelimiter);
			boolean linefeedFlag=true;
			if(fileContent.endsWith("\n")){
				linefeedFlag=true;
				logger.info("This file use linefeed.");
			}
			else{
				linefeedFlag=false;
				logger.info("This file no use linefeed.");
			}

			String[] lines = fileContent.split(String.valueOf(delimiter));
			StringBuffer newContent = new StringBuffer();

			for (int i = 0; i < lines.length; i++) {

				lines[i] = lines[i].replace("\n", "");
				// lines[i] = lines[i].trim();

				if (!(lines[i].equals(""))) {

					if (lines[i].startsWith(modifyDetail[0])) {

						String[] data = lines[i].split("\\" + seperator);

						int valuePos = Integer.parseInt(modifyDetail[1]);

						StringBuffer value = new StringBuffer();

						for (int k = 0; k <= data.length - 1; k++) {

							if (k == valuePos) {
								if (modifyDetail.length >= 3) {

									String[] subData = data[k].split("\\"
											+ subSeperator);
									int subValuePos = Integer
											.parseInt(modifyDetail[2]);
									if (subValuePos > 0) {
										subValuePos = subValuePos - 1;
									}
									for (int m = 0; m <= subData.length - 1; m++) {

										if (m == subValuePos) {
											oldValue = subData[m];
											if (m == subData.length - 1) {

												value.append(newValue);
											}

											else {
												value.append(newValue
														+ subSeperator);
											}
										} else {

											if (m == subData.length - 1) {
												value.append(subData[m]);

											} else {
												value.append(subData[m]
														+ subSeperator);
											}
										}

									}

								} else if (modifyDetail.length == 2) {
									oldValue = data[k];
									value.append(newValue);
								}
								if (k != data.length - 1) {
									value.append(seperator);
								}
							} else {
								if (k != data.length - 1) {
									value.append(data[k] + seperator);
								} else {
									value.append(data[k]);
								}
							}

						}

						lines[i] = value.toString();
						logger.info("Modify " + modifyDetail + " with "
								+ newValue + " : " + lines[i]);

					}

					if(delimiter=='\n'){
						newContent.append(lines[i] + delimiter);
					}else{

						newContent.append(lines[i] + delimiter + "\n");
					}

				} else if (i != lines.length - 1) {
					newContent.append(lines[i] + "\n");
				}

			}

			newText = newContent.toString();
			newText = newText.replaceAll(releaseDelimiter,releaseIndicator+delimiter);

		}

		return newText;

	}


	public static String genMsgReq(){
		String number = "0123456789";
		Date time = new Date();
		SimpleDateFormat form = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String systime = form.format(time);
// String RandomNum = getRandomString(number, 2);
		String RandomNum2 = getRandomString(number, 2);
		String result="EDI"+systime+RandomNum2;
// String result="EDI"+systime+RandomNum+"-"+RandomNum2;
		return result;
	}

	public static String getRandomString(String base, int length) {
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	public static String cleanXml(String xml) throws Exception {
		Node root = new XmlParser().parseText(xml);
		return XmlUtil.serialize(root);
	}

	public static boolean prettyFormatXML(String fileContent, String xmlFilePath) throws Exception {

		Document document = DocumentHelper.parseText(fileContent);
		OutputFormat format = OutputFormat.createPrettyPrint(); //     XML ĵ      ʽ
		format.setEncoding("UTF-8"); //     XML ĵ  ı
		format.setTrimText(false);
		//format.setExpandEmptyElements(false);
		XMLWriter output = new XMLWriter(new FileWriter(new File(
				xmlFilePath)), format);

		output.write(document);
		output.close();
		return true;
	}


	public static void prettyXMLByTools(File xmlfile) {
		String filepath=xmlfile.getAbsolutePath();
		String newFilePath=xmlfile.getAbsolutePath()+"-temp";
		String root=filepath.split(":")[0];
		logger.info("root="+root);
		String command = "cmd /c "+ root + ":\\1_B2BEDI_Revamp\\tools\\xml.exe ed \"" + filepath +"\""+ "> \""+newFilePath+"\"";
		String command1= "taskkill /f /t /im xml.exe";

		try {
			logger.info(command);
			Process process = Runtime.getRuntime().exec(command);
			try {
				Thread.sleep(2*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info(command1);
			process = Runtime.getRuntime().exec(command1);
			try {
				Thread.sleep(2*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			process.destroy();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		File file = new File(filepath);
		File newFile = new File(newFilePath);
		System.gc();
		//	logger.info("new file length="+newFile.length());
		if(newFile.exists())
		{
			System.gc();
			xmlfile.delete();
			newFile.renameTo(file);
		}else
		{
			System.gc();
			newFile.delete();
		}

	}

	public static String translateCodeToXML(String content) {

		content = StringEscapeUtils.escapeXml(content);

		return content;

	}
}
