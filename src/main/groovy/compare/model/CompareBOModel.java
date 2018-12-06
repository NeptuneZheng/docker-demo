package compare.model;


import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.File;
import java.util.Date;

public class CompareBOModel {
    private int row;
    private XSSFSheet sheet;
    private String runFlag;  //0
    private String inputFile;  //1
    private String expectedFile;
    private String actualFile;  //6
    private File excel;

    private String result;

    public CompareBOModel(XSSFSheet sheet, int rowNum) {
        this.sheet = sheet;
        this.row = rowNum;
        this.runFlag = this.getString(this.sheet.getRow(this.row).getCell(0));
        this.inputFile = this.getString(this.sheet.getRow(this.row).getCell(1));

        this.expectedFile = this.getString(this.sheet.getRow(this.row).getCell(3));
        this.actualFile = this.getString(this.sheet.getRow(this.row).getCell(6));
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
    public String getActualFile() {
        return actualFile;
    }
    public void setActualFile(String actualFile) {
        this.actualFile = actualFile;
    }


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
