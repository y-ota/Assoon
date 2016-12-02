package jp.co.asoq;

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
