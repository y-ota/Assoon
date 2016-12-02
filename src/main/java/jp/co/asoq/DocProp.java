package jp.co.asoq;

/**
 * Probability of Document
 * 
 * @author Yusuke Ota
 *
 */
public class DocProp {
	private int docId;
	private String doument;
	private double[] topics;

	public String getDoument() {
		return doument;
	}

	public void setDoument(String doument) {
		this.doument = doument;
	}

	public double[] getTopics() {
		return topics;
	}

	public void setTopics(double[] topics) {
		this.topics = topics;
	}

	public double getTopicProp(int topicNum) {
		return this.topics[topicNum];
	}

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

}
