package compare.util;

import compare.configure.CSB2BEDIConfigQA;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class FileProcessorQA {

	static Logger logger = LogManager.getLogger(FileProcessorQA.class.getName());

	public static String getComparableContent(File file, String msgFormat) throws IOException {

		String fileContent = FunctionHelperQA.readContent(file);
		fileContent = fileContent.replaceAll("\r\n", "\n");
		fileContent = fileContent.replaceAll("\n\r", "\n");
		fileContent = fileContent.replaceAll("\r", "\n");

		if(CSB2BEDIConfigQA.outputFormat.equals("UIF")){
			fileContent = fileContent.replaceAll("%EDI_CTRL_NUM_FORMAT%","%EDI_CTRL_NUM%");
		}

		if(CSB2BEDIConfigQA.outputFormat.equals("XML")&&CSB2BEDIConfigQA.specialRemoveList!=null&&CSB2BEDIConfigQA.specialRemoveList.size()>0){
			try {
				Document document = DocumentHelper.parseText(fileContent);
				Element root = document.getRootElement();
				for(int m=0;m<CSB2BEDIConfigQA.specialRemoveList.size(); m++){
					findAndRemoveElementByXpath(root, CSB2BEDIConfigQA.specialRemoveList.get(m));
				}
				fileContent=document.asXML();
				//    logger.info("fileContent after remove= "+fileContent);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		for(int n=0; CSB2BEDIConfigQA.specialReplaceList!=null && n<CSB2BEDIConfigQA.specialReplaceList.size(); n++){
			String[] replace = CSB2BEDIConfigQA.specialReplaceList.get(n).split(";");
			if(replace!=null&&replace.length>1&&replace[1]!="") {
				String replaceFrom = replace[0];
				String replaceTo = replace[1];
				if (replaceFrom.contains("[space]")) {
					replaceFrom = replaceFrom.replace("[space]", " ");
				}
				fileContent=fileContent.replaceAll(replaceFrom, replaceTo);
			}
		}

		return getContentAfterIgnore(fileContent, msgFormat, CSB2BEDIConfigQA.ignoredList);

	}
	private static void findAndRemoveElementByXpath(Element root,String xpath){
		List list = root.elements();
		for(Iterator its =  list.iterator();its.hasNext();){
			Element chileEle = (Element)its.next();
			if(chileEle.getUniquePath().equals(xpath)) {
				logger.info("Found "+xpath);
				//   foundXpathResult.add("Found");
				chileEle.getParent().remove(chileEle);

			}
			else
				findAndRemoveElementByXpath(chileEle, xpath);
		}
	}

	public static String getContentAfterIgnore(String fileContent, String msgFormat,
											   ArrayList<String> ignoredList){
		if (msgFormat.toLowerCase().equals("xml")) {
			StringBuffer newContent = new StringBuffer();

			String[] lines = fileContent.split("\n");
			for (int i = 0; i < lines.length; i++) {
				// For EU24
				lines[i] = lines[i].trim();

				for (int j = 0; j < ignoredList.size(); j++) {
					String ignogreS="";
					if(ignoredList.get(j).toString().trim().endsWith(";")){
						ignogreS=ignoredList.get(j).toString().trim().split(";")[0];
					}else
						ignogreS=ignoredList.get(j).toString();

					if (lines[i].contains(ignogreS)) {
						if(lines[i].endsWith("/>")||ignoredList.get(j).toString().trim().endsWith(";")){
							//	logger.info("ignore whole line.");
							lines[i]="";
						}else{
							int start = lines[i].indexOf(">") + 1;
							int end = lines[i].indexOf("</");
							String replaceChar = "#";
							String pre = lines[i].substring(0, start);
							if (end > 0) {
								String post = lines[i].substring(end);
								lines[i] = pre + replaceChar + post;
							} else
								lines[i] = ignoredList.get(j).toString()
										+ replaceChar;
						}


						//	logger.info("Ignore compare :" + lines[i]);
					}

				}
				if (! lines[i].equals("")) {
					newContent.append(lines[i] + "\n");
				}
			}
			//	logger.info("newContent222="+newContent.toString());
			return newContent.toString();
		} else if (msgFormat.toLowerCase().equals("x.12")||msgFormat.toLowerCase().equals("edifact")||msgFormat.toLowerCase().equals("edi")) {
			//char delimiter = 0;
			String delimiter = CSB2BEDIConfigQA.recordDelimiter;
			String sep = CSB2BEDIConfigQA.elementDelimiter;
			String subSeperator = ":";
			if (fileContent.startsWith("ST")) {
				String ISA="ISA"+CSB2BEDIConfigQA.elementDelimiter+"#"+CSB2BEDIConfigQA.recordDelimiter+"\n";
				String GS="GS"+CSB2BEDIConfigQA.elementDelimiter+"#"+CSB2BEDIConfigQA.recordDelimiter+"\n";
				String GE="GE"+CSB2BEDIConfigQA.elementDelimiter+"#"+CSB2BEDIConfigQA.recordDelimiter+"\n";
				String IEA="IEA"+CSB2BEDIConfigQA.elementDelimiter+"#"+CSB2BEDIConfigQA.recordDelimiter+"\n";
				fileContent=ISA+GS+fileContent+GE+IEA;
			}

			String[] lines = fileContent.split("\\" + delimiter);
			//	String[] lines = fileContent.split(delimiter);

			StringBuffer newContent = new StringBuffer();


			for (int i = 0; i < lines.length; i++) {
				String temp=lines[i].toString();
				//	temp.replace("\n", "");

				lines[i] = lines[i].replace("\n", "");

				//	lines[i] = lines[i].trim();
				//logger.info("temp="+temp);
				int listSize = ignoredList.size();
				if (!(lines[i].equals(""))) {
					for (int j = 0; j < listSize; j++) {
						String[] ignoreDetail = ignoredList.get(j).toString()
								.split(";");
						if(ignoreDetail.length>1){
							if(ignoreDetail[0].equals("UNB")){
								//logger.info(lines[i]);
								//	logger.info("aaaa="+temp.indexOf("\n"+ignoreDetail[0] + sep));
								if(temp.indexOf("\n"+ignoreDetail[0] + sep)>=0){
									ignoreDetail[0]=lines[i].substring(0, lines[i].indexOf(sep));
									//logger.info("ignoreDetail[0]="+ignoreDetail[0]);
									//ignoreDetail[0].replace("\r\n", "");
									//ignoreDetail[0].replace("\n", "");
									//	logger.info("ignoreDetail[0]="+ignoreDetail[0]);
								}

							}

							if (lines[i].startsWith(ignoreDetail[0] + sep) || lines[i].startsWith(ignoreDetail[0] + subSeperator)) {
								//	logger.info(lines[i]);
								//	logger.info(ignoreDetail[0] + sep);
								String[] data = lines[i].split("\\" + sep);

								int	 valuePos = 0;

								StringBuffer value = new StringBuffer();

								for (int kk = 0; kk < data.length; kk++) {
									if(ignoreDetail[1].contains("-")){
										String[] subignoreDetail = ignoreDetail[1].split("-");
										valuePos = Integer.parseInt(subignoreDetail[1]);
										if (kk == valuePos) {
											int ignorelength=0;
											String leaveString = null;
											//	logger.info("data[kk].length()="+data[kk].length());
											if(subignoreDetail[0].toLowerCase().startsWith("l")){
												ignorelength=Integer.parseInt(subignoreDetail[0].substring(1));
												//	logger.info("ignorelength="+ignorelength);
												leaveString = data[kk].substring(data[kk].length()-ignorelength+1);
												//	logger.info("leaveString="+leaveString);
												value.append("#" +leaveString);
											}else if(subignoreDetail[0].toLowerCase().startsWith("r")){
												ignorelength=Integer.parseInt(subignoreDetail[0].substring(1));
												//		logger.info("ignorelength="+ignorelength);
												leaveString = data[kk].substring(0,data[kk].length()-ignorelength);
												//		logger.info("leaveString="+leaveString);
												value.append(leaveString+"#");
											}else{
												logger.info("You config "+ignoreDetail[1]+" ignore parameter 2 must start with L or R!!");
											}
										}else{
											value.append(data[kk] + sep);
										}

									}else{

										valuePos = Integer.parseInt(ignoreDetail[1]);
										if (kk == valuePos) {
											value.append("#" + sep);

										} else
											value.append(data[kk] + sep);

										//	logger.info("value"+value);
									}


								}
								/*	if (valuePos == data.length - 1)
										value.append("#");
									else
										value.append(data[data.length - 1]);*/

								lines[i] = value.toString();
								//	logger.info("Ignore compare :" + lines[i]);

							}

						}else{
							if (lines[i].startsWith(ignoreDetail[0])){
								StringBuffer value1 = new StringBuffer();
								value1.append(ignoreDetail[0]+sep+"#");
								//	int end_edifact = lines[i].indexOf(delimiter);
								lines[i] = value1.toString();
								//	logger.info("Ignore compare :" + lines[i]);
							}
						}

					}

					newContent.append(lines[i] + delimiter + "\n");


				} else if (i != lines.length - 1) {

					newContent.append(lines[i] + "\n");

				}

			}
			return newContent.toString();
		} else if (msgFormat.toLowerCase().equals("uif")) {

			Pattern p = Pattern.compile("\n", Pattern.DOTALL);

			// System.out.println("P:"+p);
			String[] lines = p.split(fileContent);
			// String[] lines = content.split("\n");
			StringBuffer newContent = new StringBuffer();

			for (int i = 0; i < lines.length; i++) {
				int listSize = ignoredList==null?0:ignoredList.size();
				for (int j = 0; j < listSize; j++) {
					String[] ignoreDetail = ignoredList.get(j).toString()
							.split(";");
					if(ignoreDetail[0].startsWith("_")){
						//	logger.info("Ignore by row # .");
						//	logger.info(ignoreDetail[0]);
						//	logger.info(ignoreDetail[0].substring(1, ignoreDetail[0].length()));
						int row = Integer.parseInt(ignoreDetail[0].substring(1, ignoreDetail[0].length()));
						//	logger.info("ignore row="+row);
						if(i==row-1){
							//	logger.info("ignore row="+row);
							int valueStartPos = Integer.parseInt(ignoreDetail[1]) - 1;
							int valueLength = Integer.parseInt(ignoreDetail[2]);

							String pre = lines[i].substring(0, valueStartPos);

							StringBuffer value = new StringBuffer();
							for (int s = 0; s < valueLength; s++)
								value.append("#");

							String post = lines[i].substring(valueStartPos
									+ valueLength);
							lines[i] = pre + value.toString() + post;
							logger.info("Ignore compare :" + lines[i]);
						}
					}else{

						if (lines[i].trim().startsWith(ignoreDetail[0]) && ignoreDetail.length>2) {
							int valueStartPos = Integer.parseInt(ignoreDetail[1]) - 1;
							int valueLength = Integer.parseInt(ignoreDetail[2]);

							String pre = lines[i].substring(0, valueStartPos);

							StringBuffer value = new StringBuffer();
							for (int s = 0; s < valueLength; s++)
								value.append("#");

							String post = lines[i].substring(valueStartPos
									+ valueLength);
							lines[i] = pre + value.toString() + post;
							//	logger.info("Ignore compare :" + lines[i]);
						}else{
						//	logger.info("Ignore compare :" + ignoreDetail[0]);
							if (lines[i].trim().startsWith(ignoreDetail[0])){
								StringBuffer value1 = new StringBuffer();
								lines[i] = "";
							//	logger.info("Ignore compare :" + lines[i]);
							}
						}

					}

				}
				if(lines[i]!=""){
					newContent.append(lines[i] + "\n");
				}

			}

			return newContent.toString();

		} else if (msgFormat.toLowerCase().equals("ignoreineachline")) {
			Pattern p = Pattern.compile("\n", Pattern.DOTALL);

			// System.out.println("P:"+p);
			String[] lines = p.split(fileContent);
			// String[] lines = content.split("\n");
			StringBuffer newContent = new StringBuffer();

			for (int i = 0; i < lines.length; i++) {

				lines[i] = lines[i].replace("\n", "");
				lines[i] = lines[i].trim();
				int listSize = ignoredList.size();

				for (int j = 0; j < listSize; j++) {
					String[] ignoreDetail = ignoredList.get(j).toString()
							.split(";");

					// String sep = String.valueOf(lines[i].charAt(key
					// .length()));

					String[] data = lines[i].split("\t");

					int valuePos = Integer.parseInt(ignoreDetail[1]);

					StringBuffer value = new StringBuffer();

					for (int kk = 0; kk < data.length - 1; kk++) {

						if (kk == valuePos) {
							value.append("#");

						} else
							value.append(data[kk]);
					}
					if (valuePos == data.length - 1)
						value.append("#");
					else
						value.append(data[data.length - 1]);

					lines[i] = value.toString();
					logger.info("Ignore compare :" + lines[i]);

				}

				newContent.append(lines[i] + "\n");

			}
			return newContent.toString();
		}

		return null;

	}

	public static ArrayList<String> getRunTPList(File f1){
		ArrayList<String> runList=new ArrayList<String>();
		String fileContent = FunctionHelperQA.readContent(f1);
		if(!StringUtils.isEmpty(fileContent)){
			String data[]=fileContent.split("\n");
			for(int i=0;i<data.length;i++){
				//	System.out.println(data[i]);
				if(data[i].contains("\n"))
					data[i]=data[i].replaceAll("\n", "");
				if(data[i].contains("\r\n"))
					data[i]=data[i].replaceAll("\r\n", "");
				if(data[i].contains("\r"))
					data[i]=data[i].replaceAll("\r", "");
				if(StringUtils.isEmpty(data[i].trim())||data[i].trim().startsWith("#"))
					continue;
				else{
					System.out.println(i+" "+data[i]);
					runList.add(data[i]);
				}
			}
		}
		return runList;

	}

	public static String getWorkFolder(File f){
		String result=null;
		String fileContent = FunctionHelperQA.readContent(f);
		if(!StringUtils.isEmpty(fileContent)){
			String data[]=fileContent.split("\n");
			for(int i=0;i<data.length;i++){
				//	System.out.println(data[i]);
				if(data[i].contains("\n"))
					data[i]=data[i].replaceAll("\n", "");
				if(data[i].contains("\r\n"))
					data[i]=data[i].replaceAll("\r\n", "");
				if(data[i].contains("\r"))
					data[i]=data[i].replaceAll("\r", "");
				if(StringUtils.isEmpty(data[i].trim())||data[i].trim().startsWith("#"))
					continue;
				else{
					System.out.println(i+" "+data[i]);
					result=data[i];
					break;
				}
			}
		}
		return result;

	}
}
