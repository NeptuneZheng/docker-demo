package compare.demo;


import compare.handler.DifferenceHandler;
import compare.model.CompareBOModel;
import compare.util.FunctionHelperQA;
import compare.util.LocalFileUtilQA;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class compareCOSUBORun {
    private static Logger logger = LogManager.getLogger(compareCOSUBORun.class.getName());
    public static List<String> cde_conversion_info_list = new ArrayList<>();

    private static XSSFWorkbook wb = null;
    private static FileOutputStream fileOut = null;
    private static XSSFSheet sheet = null;
    private static InputStream fis = null;
    private static Map<String, String> resultMap = new LinkedHashMap<String, String>();
    public static void main(String[] args) {
        String testPath = "D:\\1_B2BEDI_Revamp\\BR\\OUT_UIF\\COSU_UIF";
        File excel =  new File(testPath+"\\COSU_UIF.xlsx");
        String gitIS_Path = "D:\\git\\b2b_is";

        File expected =  new File(testPath+"\\ExpectedComplete");
        File actual =  new File(testPath+"\\ActualComplete");


        compare(expected,actual,excel,testPath,gitIS_Path);
       // comapreOneFile(expected,actual);

    }



    public static void compare(File expected,File actual,File excel,String testPath,String gitIS_Path){
        ArrayList<String> ignoredSegmentList = new ArrayList<String>();
        //ignoredSegmentList.add("<CUSTOMER_300>;</CUSTOMER_300>");

        ArrayList<String> ignoredList = new ArrayList<String>();
        ignoredList.add("<BU_AAAA_CTL_NUM>");
        ignoredList.add("<BU_AAAA_DATE_TIME>2018");
        ignoredList.add("<BU_SHIPMENT_CREATION_DTM>");
        ignoredList.add("<BU_SHIPMENT_BKG_OFFICE>");
        ignoredList.add("<EXTERNALREFNUMBER>EDI2018");
        ignoredList.add("<T999BATCHNUMBER>");
        //Use ;  to ignore whole line
        ignoredList.add("<BU_OBDOOR_PICKUPDTM>;");
        ignoredList.add("<BU_OBDOOR_PICKUPDTM/>;");
        ignoredList.add("<CUSTOMERCODE>");

        //To be confirm
        ignoredList.add("<BU_INTERMODALCNTR_VOY>");
//        ignoredList.add("<BU_INTERMODALCNTR_PODCDE>");
//        ignoredList.add("<BU_INTERMODALCNTR_POLCDE>");
//        ignoredList.add("<BU_INTERMODALCNTR_DEST_CITYCDE>");
        ignoredList.add("<BU_INTERMODALCNTR_TS_PORTCDE>");
//        ignoredList.add("<BU_INTERMODALCNTR_DEST_CITYCDE;"); //Ingore whole Line
        ignoredList.add("<REMARKSCONTENT>1st vessel:");
        ignoredList.add("<REMARKSCONTENT>2nd vessel:");

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

                    expStr = expStr.replaceAll("01</REMARKSCONTENT>","</REMARKSCONTENT>");
                    expStr = expStr.replaceAll("02</REMARKSCONTENT>","</REMARKSCONTENT>");
                    expStr = expStr.replaceAll("03</REMARKSCONTENT>","</REMARKSCONTENT>");
                    expStr = expStr.replaceAll("04</REMARKSCONTENT>","</REMARKSCONTENT>");
                    expStr = expStr.replaceAll("<BU_INTERMODALCNTR_TS_PORTCDE>SGS</BU_INTERMODALCNTR_TS_PORTCDE>","<BU_INTERMODALCNTR_TS_PORTCDE>SIN</BU_INTERMODALCNTR_TS_PORTCDE>");
                    expStr = expStr.replaceAll("NEW YORK,NY,NY,US","NEW YORK,NY,NJ,US");
                    expStr = expStr.replaceAll("New York, New York,NY,US","New York, New York,NJ,US");
                    expStr = expStr.replaceAll("NEW YORK,NY,US","NEW YORK,NJ,US");


                    actStr = actStr.replaceAll("01</REMARKSCONTENT>","</REMARKSCONTENT>");
                    actStr = actStr.replaceAll("02</REMARKSCONTENT>","</REMARKSCONTENT>");
                    actStr = actStr.replaceAll("03</REMARKSCONTENT>","</REMARKSCONTENT>");
                    actStr = actStr.replaceAll("04</REMARKSCONTENT>","</REMARKSCONTENT>");
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

}
