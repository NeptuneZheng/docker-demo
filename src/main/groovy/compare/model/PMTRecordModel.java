package compare.model;

public class PMTRecordModel {
	
	private String pmtPath;	
	private String tpId;
	private String msg_type;
	private String dir;
	private String inputFormat;
	private String outputFormat;
	private String runFlag="Normal";

	public String getPmtPath() {
		return pmtPath;
	}
	public void setPmtPath(String pmtPath) {
		this.pmtPath = pmtPath;
	}
	public String getTpId() {
		return tpId;
	}
	public void setTpId(String tpId) {
		this.tpId = tpId;
	}
	public String getMsg_type() {
		return msg_type;
	}
	public void setMsg_type(String msg_type) {
		this.msg_type = msg_type;
	}
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	public String getInputFormat() {
		return inputFormat;
	}
	public void setInputFormat(String inputFormat) {
		this.inputFormat = inputFormat;
	}
	public String getOutputFormat() {
		return outputFormat;
	}
	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}

	public String getRunFlag() {
		return runFlag;
	}

	public void setRunFlag(String runFlag) {
		this.runFlag = runFlag;
	}

	public PMTRecordModel(String runPath) {
		this.pmtPath=runPath;
		String data[]=runPath.split("\\\\");
		this.tpId=data[4];
		this.msg_type=data[2];
		if(data[3].startsWith("O")){
			this.dir="O";
			this.inputFormat="XML";
			if(data[3].contains("XML")){
				this.outputFormat="XML";
			}else
				this.outputFormat="EDI";
		}else if(data[3].startsWith("IRA")){
			this.dir="IRA";
			this.inputFormat="XML";
			if(data[3].contains("XML")){
				this.outputFormat="XML";
			}else
				this.outputFormat="EDI";
		}else{
			this.dir="I";
			this.outputFormat="XML";
			if(data[3].contains("XML")){
				this.inputFormat="XML";
			}else
				this.inputFormat="EDI";
		}
		
		
	}

}
