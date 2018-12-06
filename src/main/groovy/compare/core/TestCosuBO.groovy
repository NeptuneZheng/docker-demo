package compare.core

import cs.b2b.beluga.api.EDIProcessResult
import cs.b2b.beluga.common.fileparser.UIFFileParser
import groovy.xml.XmlUtil
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.io.OutputFormat
import org.dom4j.io.XMLWriter

class TestCosuBO {

    public static void main(String[] args) {

        File expected =  new File("D:\\1_B2BEDI_Revamp\\BR\\OUT_UIF\\COSU_MIGRATION\\ExpectedComplete");
        File IGDefinition= new File("D:\\git\\b2b_is\\Tibco\\Kukri\\CSB2BCommonCoreGroovyLibrary\\IG_Definition\\CAR_UIF_BKGUIF_IRIS2.xml");

        translateFileToBOXML(expected,IGDefinition);

    }


    public static translateFileToBOXML(File expected,File IGDefinition){
        def definitionBody = IGDefinition.getText();
        UIFFileParser parser = new UIFFileParser();
        int n=0;
        expected?.eachFile { file ->
            if(file.name?.endsWith("xml")){
                return;
            }
            n=n+1;
            println(n + " "+file.name )

            String expStr = file?.getText();
            String transformSettingStr = "{\"isFieldValueTrimLeadingSpace\"=\"false\",\"isFieldValueTrimRightSpace\"=\"false\"}";
            EDIProcessResult expediResult = parser.edi2xml(expStr, definitionBody);
            String expS = expediResult?.outputString;
            //expStr = cleanXml(expS)
            if(expS){
                prettyFormatXML(expS,expected?.getAbsolutePath()+"//"+file.name+".boxml")
            }else{
                println "Please check if UIF can pass definition file~!"
            }

//            def exp_out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(expected?.getAbsolutePath()+"//"+file.name+".boxml"), "UTF-8"));
//            exp_out.write(expStr)
//            exp_out.flush()
//            exp_out.close()

        }
    }
    public static String cleanXml(String xml) throws Exception {
        Node root = new XmlParser().parseText(xml)
        XmlUtil.serialize(root)

    }
    public static boolean prettyFormatXML(String fileContent, String xmlFilePath) throws Exception {
        //println fileContent
        Document document = DocumentHelper.parseText(fileContent);
        OutputFormat format = OutputFormat.createPrettyPrint(); //     XML ĵ      ʽ
        format.setEncoding("UTF-8"); //     XML ĵ  ı
        format.setTrimText(false);
        //format.setExpandEmptyElements(false);
        XMLWriter output = new XMLWriter(new FileWriter(new File(xmlFilePath)), format);

        output.write(document);
        output.close();
        return true;
    }
}
