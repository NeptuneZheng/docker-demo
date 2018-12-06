package compare.demo;

import compare.util.FunctionHelperQA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class XMLXpath {
	static Logger logger = LogManager.getLogger(XMLXpath.class.getName());
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File file=new File("D:/1_B2BEDI_Revamp/SI/IN_D99B/OECLOGSHA/ActualComplete/EDI2017041210111405-16_1_CMA-SI_201CANDY_CHEN_20170412181106.TXT");
		String fileContent = FunctionHelperQA.readContent(file);
		fileContent = fileContent.replaceAll("\r\n", "\n");
		fileContent = fileContent.replaceAll("\n\r", "\n");
		fileContent = fileContent.replaceAll("\r", "\n");
		
		try {
			Document document = DocumentHelper.parseText(fileContent);
		    Element root = document.getRootElement();
		    findElement(root);
		    
		//    fileContent=document.asXML();
		//    logger.info("fileContent after remove= "+fileContent);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  		    
		
	}
	
	private static void findElement(Element root){
        List list = root.elements();
        for(Iterator its =  list.iterator();its.hasNext();){
            Element chileEle = (Element)its.next();
            logger.info("---------"+chileEle.getUniquePath());
     
            findElement(chileEle);
        }
    }

}
