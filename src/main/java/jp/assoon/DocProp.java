/*
 *  Copyright (C) 2016  Yusuke Ota
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 */

package jp.assoon;

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
