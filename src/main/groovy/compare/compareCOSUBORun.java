package compare;

import compare.configure.CSB2BEDIConfigQA;
import compare.core.TestCosuBO;
import compare.demo.DemoGroovy_Common_QA;
import compare.demo.MappingThread;
import compare.handler.DifferenceHandler;
import compare.handler.DifferenceHandlerQA;
import compare.model.CompareBOModel;
import compare.model.RecordModelQA;
import compare.util.FunctionHelperQA;
import compare.util.LocalFileUtilQA;
import groovy.lang.GroovyObject;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;

import javax.swing.*;
import java.io.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;

import static compare.configure.CSB2BEDIConfigQA.pmtPrepMappingScriptFile;
import static compare.demo.DemoGroovy_Common_QA.maxDifferenceCount;
import static compare.demo.GroovyMappingLocalE2EQA.prepareTestExcel;

public class compareCOSUBORun {
    private static Logger logger = LogManager.getLogger(compareCOSUBORun.class.getName());
    public static List<String> cde_conversion_info_list = new ArrayList<>();
    private static XSSFSheet sheetQA = null;

    private static XSSFWorkbook wb = null;
    private static FileOutputStream fileOut = null;
    private static XSSFSheet sheet = null;
    private static InputStream fis = null;
    private static Map<String, String> resultMap = new LinkedHashMap<String, String>();

    public static void main(String[] args) throws Exception {
        Configuration configuration = new PropertiesConfiguration(new ClassPathResource("uif_compare.properties").getFile());

        String testPath = configuration.getString("rootPath");
        File excel =  new File(testPath+"\\UIF_COMPARE_INFO.xlsx");

        if(!excel.exists()){
            logger.info("Prepare test caseexcel first" );
            if(prepareTestExcel(excel,testPath)){
            }else{
                logger.info("Some expected not found ,please check ." );
            }
        }

        File differenceLogModle = new ClassPathResource("Difference-Log-Model.xlsm").getFile();
        String ig_defination = configuration.getString("compare_ig_definition_path");

        File IGDefination = new ClassPathResource(ig_defination).getFile();
        File expected =  new File(testPath+"\\ExpectedComplete");
        File actual =  new File(testPath+"\\ActualComplete");

        TestCosuBO.translateFileToBOXML(expected,IGDefination);
        TestCosuBO.translateFileToBOXML(actual,IGDefination);

        compare(expected,actual,excel,testPath,differenceLogModle);
       // comapreOneFile(expected,actual);

    }



    public static void compare(File expected,File actual,File excel,String testPath,File differenceLogModle) throws IOException, ConfigurationException {
        Configuration configuration = new PropertiesConfiguration(new ClassPathResource("ignore/ignoreLists.properties").getFile());
        String[] vSegmentIgnoreLists = (configuration.getString("SegmentIgnoreLists")==null?"":configuration.getString("SegmentIgnoreLists")).split(",");
        String[] vFieldsIgnoreLists = (configuration.getString("FieldsIgnoreLists")==null?"":configuration.getString("FieldsIgnoreLists")).split(",");

        ArrayList<String> ignoredSegmentList = new ArrayList<String>();
        //ignore whole segment
        for(String s : vSegmentIgnoreLists){
            ignoredSegmentList.add("<" + s +">;</" + s + ">");
        }


        ArrayList<String> ignoredList = new ArrayList<String>();
        // ignore field
        for(String s : vFieldsIgnoreLists){
            ignoredList.add("<" + s + ">");
        }


        String sysTime = GenerateTimestamp();
        ArrayList<CompareBOModel> recordModelList = new ArrayList<CompareBOModel>();

        if(excel.exists()&&excel.getName().endsWith(".xlsx")){
            openExcel(excel);

            int rowCount = sheet.getLastRowNum();

            for(int n=1;n<=rowCount;n++){

                if (sheet.getRow(n) == null) {
                    logger.error("Row "
                            + (n + 1)
                            + " is null,and it is skipped. Advise to remove this null row.");
                    continue;
                }
                CompareBOModel recordModel = new CompareBOModel(sheet,n);
                String flag = recordModel.getRunFlag().toLowerCase().trim();
                if(FunctionHelperQA.isEmpty(flag)){
                    continue;
                }
                String exp = recordModel.getExpectedFile().trim();
                String act = recordModel.getActualFile().trim();
                if(FunctionHelperQA.isEmpty(exp)){
                    continue;

                }

                if(FunctionHelperQA.isEmpty(act)){
                    act = recordModel.getInputFile();
                }

                if(FunctionHelperQA.isNotEmpty(exp)){
                    recordModel.setExpectedFile(exp+".boxml");
                }

                if(FunctionHelperQA.isNotEmpty(act)){
                    recordModel.setActualFile(act+".boxml");
                }

                recordModel.setRow(n);
                recordModel.setExcel(excel);
                recordModelList.add(recordModel);
            }

            closeExcel(excel);
        }

        if(recordModelList.size()==0){
            return;
        }

        try{
            DifferenceHandler differenceHandler = new DifferenceHandler();
            for(int i=0;i<recordModelList.size();i++){
                CompareBOModel recordModel = recordModelList.get(i);
                File expectedfile = new File(expected.getAbsolutePath()+"\\"+recordModel.getExpectedFile());
                File actualFile = new File(actual.getAbsolutePath()+"\\"+recordModel.getActualFile());
                if(expectedfile.getName().endsWith(".boxml")&&actualFile.exists()){

                    String expStr = LocalFileUtilQA.readBigFile(expectedfile.getAbsolutePath());
                    String actStr = LocalFileUtilQA.readBigFile(actualFile.getAbsolutePath());

                    if(ignoredSegmentList.size()>0){
                        expStr = getContentAfterIgnoreSegmentXML(expStr,ignoredSegmentList);
                        actStr = getContentAfterIgnoreSegmentXML(actStr,ignoredSegmentList);
                    }


                    expStr = getContentAfterIgnoreXML(expStr,ignoredList);
                    actStr = getContentAfterIgnoreXML(actStr,ignoredList);
                    String result = null;

//                    expStr = expStr.replaceAll("01</REMARKSCONTENT>","</REMARKSCONTENT>");
//                    expStr = expStr.replaceAll("02</REMARKSCONTENT>","</REMARKSCONTENT>");
//                    expStr = expStr.replaceAll("03</REMARKSCONTENT>","</REMARKSCONTENT>");
//                    expStr = expStr.replaceAll("04</REMARKSCONTENT>","</REMARKSCONTENT>");
//                    expStr = expStr.replaceAll("<BU_INTERMODALCNTR_TS_PORTCDE>SGS</BU_INTERMODALCNTR_TS_PORTCDE>","<BU_INTERMODALCNTR_TS_PORTCDE>SIN</BU_INTERMODALCNTR_TS_PORTCDE>");
//                    expStr = expStr.replaceAll("NEW YORK,NY,NY,US","NEW YORK,NY,NJ,US");
//                    expStr = expStr.replaceAll("New York, New York,NY,US","New York, New York,NJ,US");
//                    expStr = expStr.replaceAll("NEW YORK,NY,US","NEW YORK,NJ,US");
//
//
//                    actStr = actStr.replaceAll("01</REMARKSCONTENT>","</REMARKSCONTENT>");
//                    actStr = actStr.replaceAll("02</REMARKSCONTENT>","</REMARKSCONTENT>");
//                    actStr = actStr.replaceAll("03</REMARKSCONTENT>","</REMARKSCONTENT>");
//                    actStr = actStr.replaceAll("04</REMARKSCONTENT>","</REMARKSCONTENT>");
                    actStr = actStr.replaceAll("~","|");
                    actStr = actStr.replaceAll("\\*","|");
                    //       actStr = actStr.replaceAll("ß"," ");
//                    actStr = actStr.replaceAll("®"," ");
                    //     actStr = actStr.replaceAll("é"," ");
//                    actStr = actStr.replaceAll("á"," ");
//                    actStr = actStr.replaceAll("ä"," ");
//                    actStr = actStr.replaceAll("ü"," ");
//                    actStr = actStr.replaceAll("P´L","P L");



                    if(expStr.equals(actStr)){
                        result = "Pass";
                    }else{
                        result = "Fail";

                        String inputName = recordModel.getInputFile();
                        differenceHandler.logDifference(
                                "InputData\\"+inputName, expected.getName()+"\\"+recordModel.getExpectedFile(),
                                actual.getName()+"\\"+recordModel.getActualFile(), expStr, actStr, sysTime+"-BOXML",testPath,differenceLogModle);


                    }
                    recordModel.setResult(result);
                    //  resultMap.put(recordModel.getInputFile(),result);
                    logger.info( i + "  "+ recordModel.getInputFile()+"  "+result);
                }

            }

            try {
                Thread.sleep(5000);
            } catch (Exception ex) {}

            differenceHandler.saveAll();

            openExcel(excel);
            for(int i=0;i<recordModelList.size();i++) {
                CompareBOModel recordModel = recordModelList.get(i);
                XSSFCell cellfileResult = sheet.getRow(recordModel.getRow()).createCell(11);
                if(!StringUtils.isEmpty(recordModel.getResult()))
                    cellfileResult.setCellValue(recordModel.getResult());
                else
                    cellfileResult.setCellValue("");
            }
            closeExcel(excel);

        }catch (Exception e){

        }


        logger.info("Finish.");

    }


    public static String GenerateTimestamp() {
        String timestamp;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar calendar = Calendar.getInstance();
        timestamp = simpleDateFormat.format(calendar.getTime());
        return timestamp;

    }

    public static String getContentAfterIgnoreSegmentXML(String fileContent, ArrayList<String> ignoredSegmentList){
        StringBuffer newContent = new StringBuffer();
        String[] lines = fileContent.split("\n");
        for (int j = 0; j < ignoredSegmentList.size(); j++) {
            String[] inore = ignoredSegmentList.get(j).split(";");
            String ignoreStart = inore[0];
            String endStart = inore[1];
            boolean flag = false;
            for (int i = 0; i < lines.length; i++) {
                lines[i] = lines[i].trim();
                if(lines[i].contains(ignoreStart)){
                    flag = true;
                }
                if(lines[i].contains(endStart)){
                    lines[i] = "";
                    flag = false;
                }
                if(flag){
                    lines[i] = "";
                }

                if(!lines[i].equals("")){
                    newContent.append(lines[i] + "\n");
                }
            }
        }
        return newContent.toString();

    }

    public static String getContentAfterIgnoreXML(String fileContent, ArrayList<String> ignoredList){

        StringBuffer newContent = new StringBuffer();
        String[] lines = fileContent.split("\n");
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].trim();
            for (int j = 0; j < ignoredList.size(); j++) {
                if(ignoredList.get(j).endsWith(";")){
                    String newInoreS = ignoredList.get(j).substring(0,ignoredList.get(j).length()-1);
                    if (lines[i].contains(newInoreS)) {
                        lines[i] = "";
                    }
                }else if (lines[i].contains(ignoredList.get(j))) {
                  //  logger.info("*******************  "+lines[i]);
                    if(ignoredList.get(j).contains("<CUSTOMERCODE>")){
                        int start = lines[i].indexOf(">") + 1;
                        int end = lines[i].indexOf("</");
                        String pre = lines[i].substring(0, start);

                        if(end>0){
                            String post = lines[i].substring(end);
                            String value =lines[i].substring(start,end);
                          //  logger.info(value);
                            if(value!=null&&value.length()>10){
                                value = value.substring(0,10).trim();
                                lines[i] = pre + value + post;
                            }
                        }

                    }else{
                        int start = lines[i].indexOf(">") + 1;
                        int end = lines[i].indexOf("</");
                        String replaceChar = "#";
                        String pre = lines[i].substring(0, start);
                        if (end > 0) {
                            String post = lines[i].substring(end);
                            lines[i] = pre + replaceChar + post;
                        } else
                            lines[i] = ignoredList.get(j) + replaceChar;
                    }

                }
            }
            if(!lines[i].equals("")){
                newContent.append(lines[i] + "\n");
            }

        }
        return newContent.toString();
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

    public static String getString(XSSFCell cell1) {
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

    public static void demo(File excel) throws Exception {

        //Connection conn = null;
        File  root = new File(excel.getParent());
        if (!FunctionHelperQA.checkExcelAvailable(excel)) {
            return;
        }

        if (FunctionHelperQA.backupFile(excel)) {
            logger.info("Backup Auto log excel successfully !");
        } else {
            logger.error("Fail to backup Auto log excel !");
        }
        ArrayList<RecordModelQA> recordModelList = new ArrayList<RecordModelQA>();
        openExcel(excel);
        int rowCount = sheetQA.getLastRowNum();
        String flag = null;
        boolean fileExistFlag = true;
        for (int i = 1; i <= rowCount; i++) {
            if (sheetQA.getRow(i) == null) {
                logger.error("Row "
                        + (i + 1)
                        + " is null,and it is skipped. Advise to remove this null row.");
                continue;
            }
            RecordModelQA recordModel = new RecordModelQA(sheetQA, i);
            flag = recordModel.getRunFlag().toLowerCase().trim();
            if (!flag.equals("t")) {
                continue;
            }
            if (!org.apache.commons.lang.StringUtils.isEmpty(recordModel.getInputFile())&&!FunctionHelperQA.fileExist((i + 1),recordModel.getInputFilePath()))
                fileExistFlag = false;
            if (!org.apache.commons.lang.StringUtils.isEmpty(recordModel.getExpectedFile())&&!FunctionHelperQA.fileExist((i + 1),recordModel.getExpectedFilePath()))
                fileExistFlag = false;
            if (!org.apache.commons.lang.StringUtils.isEmpty(recordModel.getExpectedFail())&&!FunctionHelperQA.fileExist((i + 1),recordModel.getDbFilePath()))
                fileExistFlag = false;
            recordModel.setRow(i);
            recordModel.setExcel(excel);
            recordModelList.add(recordModel);
        }
        closeExcel(excel);
        if (!fileExistFlag) {

            logger.error("Above files not exist , please check !");
            JOptionPane.showMessageDialog(null,
                    "Some files not exist,please check the log !",
                    "Warning", JOptionPane.YES_OPTION);
            return;
        }

        if (recordModelList.size() <= 0) {
            logger.error("No row needs to be runned.");
            return;
        }
        if(recordModelList.size()>0){
            CSB2BEDIConfigQA.initFileFormat(recordModelList.get(0).getInputFile());
        }


        //20161222 david
        LinkedHashMap<String, String> scripts = new LinkedHashMap<String, String>();

        LinkedHashMap<String, String> scriptsPrep = new LinkedHashMap<String, String>();


        int maxConcurrenThreadCount = 10;

        try {
            if (maxConcurrenThreadCount>1) {
                if (recordModelList.size()<=4) {
                    logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 1 !!!!!");
                    maxConcurrenThreadCount = 1;
                } else if (recordModelList.size()<=10) {
                    logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 2 !!!!!");
                    maxConcurrenThreadCount = 2;
                } else if (recordModelList.size()<=20) {
                    logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 3 !!!!!");
                    maxConcurrenThreadCount = 3;
                } else if (recordModelList.size()<=50) {
                    logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 4 !!!!!");
                    maxConcurrenThreadCount = 4;
                } else if (recordModelList.size()<=100) {
                    logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 5 !!!!!");
                    maxConcurrenThreadCount = 5;
                } else if (recordModelList.size()<=200) {
                    logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 6 !!!!!");
                    maxConcurrenThreadCount = 6;
                } else if (recordModelList.size()<=300) {
                    logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 7 !!!!!");
                    maxConcurrenThreadCount = 7;
                } else if (recordModelList.size()<=500) {
                    logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 8 !!!!!");
                    maxConcurrenThreadCount = 8;
                } else if (recordModelList.size()<=800) {
                    logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 9 !!!!!");
                    maxConcurrenThreadCount = 9;
                } else if (recordModelList.size()<=1000) {
                    logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 10 !!!!!");
                    maxConcurrenThreadCount = 10;
                } else if (recordModelList.size()<=10000) {
                    //if > 1000, limit the min concurrent db is 12
                    if (maxConcurrenThreadCount <= 15) {
                        logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 15 !!!!!");
                        maxConcurrenThreadCount = 15;
                    } else {
                        //keep original setting if > 15 threads
                    }
                } else if (recordModelList.size()>10000 && maxConcurrenThreadCount<30) {
                    logger.info(">>>>> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to 30  !!!!!");
                    maxConcurrenThreadCount = 30;
                }
            }
            int maxLimitationConn = 40;
            if (maxConcurrenThreadCount > maxLimitationConn) {
                logger.info("--> maxConcurrenThreadCount switched from "+maxConcurrenThreadCount+" to "+maxLimitationConn+", need to make sure the env.");
                maxConcurrenThreadCount = maxLimitationConn;
            }
            logger.info(">>>>> maxConcurrenThreadCount: " + maxConcurrenThreadCount);

            for (int i=0; i<maxConcurrenThreadCount; i++) {
                String tid = "T-"+(i+1);
                MappingThread t = new MappingThread(tid, null);
                t.start();
            }


            DifferenceHandlerQA.getInstance().saveAll();
            closeExcel(excel);
        } catch (Exception e1) {
            logger.error("exception run demo function, fail Error:", e1);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e1.printStackTrace(pw);
            BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
            String b = br.readLine();
            StringBuffer sb = new StringBuffer();

        }
        logger.warn("------------------Finish--------------");
    }
}
