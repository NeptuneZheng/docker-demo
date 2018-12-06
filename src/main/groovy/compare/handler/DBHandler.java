package compare.handler;

import compare.util.FunctionHelperQA;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.zip.ZipInputStream;

/**
 * Created by HUANGSU3 on 3/14/2018.
 */
public class DBHandler {

    static Logger logger = LogManager.getLogger(DBHandler.class.getName());
    Connection conn = null;
    String sql  = null;
    public DBHandler(Connection conn) {
        this.conn = conn;
    }

    public String getStringBySQL(String sql) throws Exception {
        if (conn == null) {
            throw new Exception("DB Connection is not available for query. ");
        }

        String ret = "";
        PreparedStatement pre = null;
        ResultSet result = null;
        try {
            pre = conn.prepareStatement(sql);
            pre.setMaxRows(getDBRowLimit());
            pre.setQueryTimeout(getDBTimeOutInSeconds());
            result = pre.executeQuery();

            if (result.next()) {
                ret = result.getString(1);
            }
        } finally {
            if (result != null)
                result.close();
            if (pre != null)
                pre.close();
        }
        return ret;

    }
    public Integer getIntBySQL(String sql) throws Exception {
        if (conn == null) {
            throw new Exception("DB Connection is not available for query. ");
        }

        int ret = 0;
        PreparedStatement pre = null;
        ResultSet result = null;

        try {
            pre = conn.prepareStatement(sql);
            pre.setMaxRows(1);
            pre.setQueryTimeout(200);
            result = pre.executeQuery();

            if (result.next()) {
                ret = result.getInt(1);
            }
        } finally {
            if (result != null)
                result.close();
            if (pre != null)
                pre.close();
        }
        return ret;

    }

    public String getStringFromBlob(String sql) throws Exception {
        if (conn == null) {
            throw new Exception("DB Connection is not available for query. ");
        }

        String ret = "";
        PreparedStatement pre = null;
        ResultSet result = null;
        try {
            pre = conn.prepareStatement(sql);
            pre.setMaxRows(getDBRowLimit());
            pre.setQueryTimeout(getDBTimeOutInSeconds());
            result = pre.executeQuery();

            if (result.next()) {
                Blob blob = result.getBlob(1);
                if(blob==null){
                    return ret;
                }
                byte[] samDocContent = blob.getBytes(1,
                        (int) blob.length());
                ret = upzipBlob(samDocContent);
            }

            if (FunctionHelperQA.isNotEmpty(ret)) {

                String startLabel = "<ns0:Body>";
                String endLabel = "</ns0:Body>";
                if (ret.contains(startLabel) && ret.contains(endLabel)) {
                    ret = ret.substring(ret.indexOf(startLabel) + startLabel.length(),
                            ret.lastIndexOf(endLabel));
                }
                if (FunctionHelperQA.isNotEmpty(ret)) {
                    ret = ret.replaceAll("&lt;", "<");
                    ret = ret.replaceAll("&gt;", ">");
                    ret = ret.replaceAll("&amp;", "&");
                    ret = ret.replaceAll("&apos;", "\'");
                    ret = ret.replaceAll("&quot;", "\"");
                    if (ret.contains("&#xD;")) {
                        if (ret.contains("\n")) {
                            ret = ret.replaceAll("&#xD;", ""); // &#xD; sometime
                        } else {
                            ret = ret.replaceAll("&#xD;", "\n");
                        }
                    }
                }
            }

        } finally {
            if (result != null)
                result.close();
            if (pre != null)
                pre.close();
        }
        return ret;

    }

    public int getMaxProcId(String msgReqID)throws Exception{
        sql = "select max(proc_seq) from b2b_msg_req_process where msg_req_id='"+msgReqID+"'";
        int result = getIntBySQL(sql);
        return result;
    }




    public String upzipBlob(byte[] content) throws Exception {
        String unzippedContent = "";
        try {
            unzippedContent = new String(unzip(content), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return unzippedContent;
    }

    private byte[] unzip(byte[] content) throws Exception {
        byte[] unzippedContent = null;
        ByteArrayInputStream bais = null;
        ZipInputStream zis = null;
        try {
            bais = new ByteArrayInputStream(content);
            zis = new ZipInputStream(bais);
            zis.getNextEntry();
            unzippedContent = IOUtils.toByteArray(zis);
        } catch (IOException e) {
        } finally {
            IOUtils.closeQuietly(bais);
            IOUtils.closeQuietly(zis);
        }
        return unzippedContent;
    }

    public int getDBRowLimit() {
        int DB_MAX_RETURN_ROW = 10000;
        return DB_MAX_RETURN_ROW;
    }

    public int getDBTimeOutInSeconds() {
        int DB_TIMEOUT_IN_SECCOND = 20;
        return DB_TIMEOUT_IN_SECCOND;
    }

    public void closeQueryResource(PreparedStatement pre, ResultSet result) {
        if (result != null) {
            try {
                result.close();
            } catch (Exception e) {}
        }
        if (pre != null) {
            try {
                pre.close();
            } catch (Exception e) {}
        }
    }
}
