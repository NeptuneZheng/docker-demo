package compare.model;

public class DBResultRecordModelQA {
		
	
	private String ID="###";
	private String MSG_REQ_ID="###";
	private String OCEAN_CARRIER_ID;
	private String CT_EVENT_TYPE_ID;
	private String INTERCHANGE_CTRL_NUM="###";	
	private String GROUP_CTRL_NUM="###";	
	private String TXN_CTRL_NUM="###";

	private String UPDATED_BY="FWK";
	private String TXN_STS_ID;
	private String INT_ERR_CDE;
	private String INT_ERR_MSG;
	private String SEVERITY;
							
	public String getOCEAN_CARRIER_ID() {
		return OCEAN_CARRIER_ID;
	}
	public void setOCEAN_CARRIER_ID(String oCEAN_CARRIER_ID) {
		OCEAN_CARRIER_ID = oCEAN_CARRIER_ID;
	}
	public String getCT_EVENT_TYPE_ID() {
		return CT_EVENT_TYPE_ID;
	}
	public void setCT_EVENT_TYPE_ID(String cT_EVENT_TYPE_ID) {
		CT_EVENT_TYPE_ID = cT_EVENT_TYPE_ID;
	}
	public String getUPDATED_BY() {
		return UPDATED_BY;
	}
	public void setUPDATED_BY(String uPDATED_BY) {
		UPDATED_BY = uPDATED_BY;
	}
	public String getTXN_STS_ID() {
		return TXN_STS_ID;
	}
	public void setTXN_STS_ID(String tXN_STS_ID) {
		TXN_STS_ID = tXN_STS_ID;
	}
	public String getINT_ERR_CDE() {
		return INT_ERR_CDE;
	}
	public void setINT_ERR_CDE(String iNT_ERR_CDE) {
		INT_ERR_CDE = iNT_ERR_CDE;
	}
	public String getINT_ERR_MSG() {
		return INT_ERR_MSG;
	}
	public void setINT_ERR_MSG(String iNT_ERR_MSG) {
		INT_ERR_MSG = iNT_ERR_MSG;
	}
	public String getSEVERITY() {
		return SEVERITY;
	}
	public void setSEVERITY(String sEVERITY) {
		SEVERITY = sEVERITY;
	}

	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public String getMSG_REQ_ID() {
		return MSG_REQ_ID;
	}
	public void setMSG_REQ_ID(String mSG_REQ_ID) {
		MSG_REQ_ID = mSG_REQ_ID;
	}
	public String getINTERCHANGE_CTRL_NUM() {
		return INTERCHANGE_CTRL_NUM;
	}
	public void setINTERCHANGE_CTRL_NUM(String iNTERCHANGE_CTRL_NUM) {
		INTERCHANGE_CTRL_NUM = iNTERCHANGE_CTRL_NUM;
	}
	public String getGROUP_CTRL_NUM() {
		return GROUP_CTRL_NUM;
	}
	public void setGROUP_CTRL_NUM(String gROUP_CTRL_NUM) {
		GROUP_CTRL_NUM = gROUP_CTRL_NUM;
	}
	public String getTXN_CTRL_NUM() {
		return TXN_CTRL_NUM;
	}
	public void setTXN_CTRL_NUM(String tXN_CTRL_NUM) {
		TXN_CTRL_NUM = tXN_CTRL_NUM;
	}

}
