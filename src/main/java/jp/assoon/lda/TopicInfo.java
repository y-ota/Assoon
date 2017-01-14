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

package jp.assoon.lda;

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
