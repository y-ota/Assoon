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

public class TopicInfoComparator implements Comparator<TopicInfo> {

	// ソートのフラグ
	public static Integer DESC = 1; // 降順
	public static Integer ASC = 0; // 昇順
	public int order;

	public TopicInfoComparator(int order) {
		if (order != DESC && order != ASC) {
			throw new RuntimeException();
		}
		this.order = order;
	}

	// 比較メソッド（データクラスを比較して-1, 0, 1を返すように記述する）
	public int compare(TopicInfo a, TopicInfo b) {
		double ascore = 0.0;
		double bscore = 0.0;
		// 降順の場合
		if (DESC == order) {
			ascore = a.getSimcount();
			bscore = b.getSimcount();
		} else { // 昇順の場合
			ascore = b.getSimcount();
			bscore = a.getSimcount();
		}

		// 降順でソートされる
		if (ascore < bscore) {
			return 1;

		} else if (ascore == bscore) {
			return 0;

		} else {
			return -1;

		}
	}
}
