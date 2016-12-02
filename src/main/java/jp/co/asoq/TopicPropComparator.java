package jp.co.asoq;

import java.util.Comparator;

public class TopicPropComparator implements Comparator<TopicProp> {

	// 比較メソッド（データクラスを比較して-1, 0, 1を返すように記述する）
	public int compare(TopicProp a, TopicProp b) {
		double aTopic = a.getProp();
		double bTopic = b.getProp();

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
