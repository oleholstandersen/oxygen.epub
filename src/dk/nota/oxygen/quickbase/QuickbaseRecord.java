package dk.nota.oxygen.quickbase;

import java.util.HashMap;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

public class QuickbaseRecord {
	
	private HashMap<Integer,String> fields = new HashMap<Integer,String>();
	private String pid;
	private int rid;
	
	public QuickbaseRecord() {
		
	}
	
	public QuickbaseRecord(String pid) {
		this.pid = pid;
	}
	
	public QuickbaseRecord(int rid) {
		this.rid = rid;
	}
	
	public QuickbaseRecord(String pid, int rid) {
		this.pid = pid;
		this.rid = rid;
	}
	
	public String getEpubFileUrl() {
		return fields.get(18);
	}
	
	public HashMap<Integer,String> getFields() {
		return fields;
	}
	
	public String getFieldValue(int fieldId) {
		return fields.get(fieldId);
	}
	
	public String getPid() {
		return pid;
	}
	
	public int getRid() {
		return rid;
	}
	
	public void parseRecordNode(XdmNode recordNode) {
		XdmSequenceIterator iterator = recordNode.axisIterator(Axis.CHILD,
				new QName("f"));
		iterator.forEachRemaining(
				fieldItem -> {
					XdmNode fieldNode = (XdmNode)fieldItem;
					int fid = Integer.parseInt(((XdmNode)fieldNode)
							.getAttributeValue(new QName("id")));
					String value = "";
					if (fid == 18)
						value = fieldNode.axisIterator(Axis.CHILD,
								new QName("url")).next().getStringValue();
					else value = fieldNode.getStringValue();
					setFieldValue(fid, value);
				});
	}
	
	public void setFieldValue(int fieldId, String value) {
		if (fieldId == 3) rid = Integer.parseInt(value);
		else if (fieldId == 14) pid = value;
		fields.put(fieldId, value);
	}

}
