package jp.co.asoq;

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
