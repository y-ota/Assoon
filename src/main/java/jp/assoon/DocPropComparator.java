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

import java.util.Comparator;

/**
 * Comparator for DocProp
 * 
 * @author Yusuke Ota
 *
 */
public class DocPropComparator implements Comparator<DocProp> {
	private int topic = 0;

	public DocPropComparator(int topic) {
		this.topic = topic;
	}

	// 比較メソッド（データクラスを比較して-1, 0, 1を返すように記述する）
	public int compare(DocProp a, DocProp b) {
		double aTopic = a.getTopicProp(topic);
		double bTopic = b.getTopicProp(topic);

		// 降順でソートされる
		if (aTopic < bTopic) {
			return 1;

		} else if (aTopic == bTopic) {
			return 0;

		} else {
			return -1;

		}
	}
}
