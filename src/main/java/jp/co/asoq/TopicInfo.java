package jp.co.asoq;

import java.util.List;

public class TopicInfo {
	private int num;
	private List<WordProp> wordProp;
	private List<String> document;
	private int simcount;

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public List<WordProp> getWordProp() {
		return wordProp;
	}

	public void setWordProp(List<WordProp> wordProp) {
		this.wordProp = wordProp;
	}

	public List<String> getDocument() {
		return document;
	}

	public void setDocument(List<String> document) {
		this.document = document;
	}

	public int getSimcount() {
		return simcount;
	}

	public void setSimcount(int simcount) {
		this.simcount = simcount;
	}

}
