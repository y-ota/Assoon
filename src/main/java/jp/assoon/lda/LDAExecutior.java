package jp.assoon.lda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import jgibblda.LDA;
import jp.assoon.mecab.MeCabExcecutor;
import jp.assoon.util.Constants;
import jp.assoon.util.AssoonUtils;

public class LDAExecutior {
	
	public List<TopicInfo> execute(MeCabExcecutor mecab, String userDir, double alpha, double beta, int topic, int iter){
		// LDA実行
		LDA.main(new String[] { "-est", "-alpha", String.valueOf(alpha), "-beta", String.valueOf(beta), "-ntopics", 
				String.valueOf(topic), "-niters", String.valueOf(iter), "-twords",
				"20", "-dfile", Constants.SPACE_SEP_FILE, "-dir", userDir + "/", "-savestep", "10000" });

		// オブジェクトに文書-トピック分布を入れる
		List<DocProp> listDoc = readDocumentPropFile(userDir + "/" + Constants.POST_FILE,
				userDir + "/model-final.theta",
				userDir + "/" + Constants.SPACE_SEP_FILE + Constants.SPACE_SEP_FILE_DOC_ID);

		// 各文書の単語に割り当てられたトピック
		List<List<Integer>> wordTopicAssignList = readAssginFile(userDir + "/model-final.tassign");

		// トピックごとの単語の確率
		Map<String, List<Double>> phiMap = readPhiFile(userDir + "/model-final.phi");

		// 文書ごと、トピックごとに単語の生成確率の平均を出す
		List<Map<Integer, Double>> socreList = getWordProp(mecab, wordTopicAssignList, phiMap);

		// トピックごとの類似意見数を求める
		Map<Integer, Integer> topicCountMap = getTopicCountMap(topic, socreList);

		// オブジェクトに単語分布を入れる
		List<TopicInfo> topicInfoList = new ArrayList<>();
		for (int i = 0; i < topic; i++) {
			TopicInfo topicInfo = new TopicInfo();
			topicInfo.setNum(i);
			topicInfo.setWordProp(readTopicFile(userDir + "/topic" + i + ".csv"));

			if (topicCountMap.containsKey(i)) {
				topicInfo.setSimcount(topicCountMap.get(i));
			} else {
				topicInfo.setSimcount(0);
			}

			List<String> list = getTopText(mecab, listDoc, wordTopicAssignList, socreList, i);
			topicInfo.setDocument(list);
			topicInfoList.add(topicInfo);
		}

		return topicInfoList.stream().sorted((a,b)->b.getSimcount()-a.getSimcount()).collect(Collectors.toList());
	}

	private List<Map<Integer, Double>> getWordProp(MeCabExcecutor mecab, List<List<Integer>> wordTopicAssignList, Map<String, List<Double>> phiMap) {
		List<Map<Integer, Double>> socreList = new ArrayList<>();
		
		for (int i = 0; i < mecab.getWordInfoList().size(); i++) {
			Map<Integer, Double> scoreMap = new LinkedHashMap<>();
			for (int j = 0; j < mecab.getWordInfoList().get(i).size(); j++) {
				if (scoreMap.containsKey(wordTopicAssignList.get(i).get(j))) {
					scoreMap.put(
							wordTopicAssignList.get(i)
									.get(j),
							scoreMap.get(
									wordTopicAssignList.get(i).get(j)) + phiMap
											.get(AssoonUtils.replaceHalfEscapeCharToFullEscapeChar(mecab.getWordInfoList().get(i).get(j).getWord()))
											.get(wordTopicAssignList.get(i).get(j)));
				} else {
					scoreMap.put(wordTopicAssignList.get(i).get(j), phiMap
							.get(AssoonUtils.replaceHalfEscapeCharToFullEscapeChar(mecab.getWordInfoList().get(i).get(j).getWord()))
							.get(wordTopicAssignList.get(i).get(j)));
				}
			}
			socreList.add(scoreMap);
		}
		return socreList;
	}

	/**
	 * トピックごとのテキストの数をカウントのMapを求める
	 * @param topic
	 * @param socreList
	 * @return
	 */
	private Map<Integer, Integer> getTopicCountMap(int topic, List<Map<Integer, Double>> socreList) {
		Map<Integer, Integer> topicCountMap = new TreeMap<>();
		for (Map<Integer, Double> maps : socreList) {
			double[] topicValue = new double[topic];
			for (Integer id : maps.keySet()) {
				topicValue[id] = maps.get(id);
			}

			if (topicCountMap.containsKey(AssoonUtils.maxValueTopic(topicValue))) {
				topicCountMap.put(AssoonUtils.maxValueTopic(topicValue),
						topicCountMap.get(AssoonUtils.maxValueTopic(topicValue)) + 1);
			} else {
				topicCountMap.put(AssoonUtils.maxValueTopic(topicValue), 1);
			}
		}
		
		return topicCountMap;
	}

	/**
	 *  トピック比率がもっとも高いテキストを取得する
	 * @param mecab
	 * @param listDoc
	 * @param wordTopicAssignList
	 * @param socreList
	 * @param i
	 * @return
	 */
	private List<String> getTopText(MeCabExcecutor mecab, List<DocProp> listDoc, List<List<Integer>> wordTopicAssignList, List<Map<Integer, Double>> socreList, int i) {
		List<String> list = new ArrayList<>();

		// トピック比率がもっとも高いテキストをadd
		for (int j = 0; j < 2; j++) {
			List<WordInfo> wordInfoList = mecab.getWordInfoList()
					.get(listDoc.get(AssoonUtils.maxTopicValueDocId(socreList, i)).getDocId());
			List<Integer> topicAssList = wordTopicAssignList
					.get(listDoc.get(AssoonUtils.maxTopicValueDocId(socreList, i)).getDocId());
			String text = listDoc.get(AssoonUtils.maxTopicValueDocId(socreList, i)).getDoument();

			StringBuilder sb = new StringBuilder(text);
			int index = 0;
			int k = 0;
			for (WordInfo wordInfo : wordInfoList) {
				if (topicAssList.get(k) == i) {
					sb.insert(index + wordInfo.getStartIndex(), Constants.HTML_FONT_START);
					index += Constants.HTML_FONT_START.length();
					sb.insert(wordInfo.getStartIndex() + wordInfo.getEndIndex() + index, Constants.HTML_FONT_END);
					index += Constants.HTML_FONT_END.length();
				}
				k++;
			}
			list.add(sb.toString());

		}
		return list;
	}
	
	private List<DocProp> readDocumentPropFile(String docPath, String thetaPath, String sepDocIdFilePath) {
		List<String> docList = AssoonUtils.readText(docPath);
		List<String> thetaList = AssoonUtils.readText(thetaPath);

		List<DocProp> returnList = new ArrayList<>();

		List<String> docIdList = AssoonUtils.readText(sepDocIdFilePath);
		int idx = 0;
		for (String docId : docIdList) {
			DocProp docProp = new DocProp();
			docProp.setDocId(idx);
			docProp.setDoument(AssoonUtils.replaceHalfEscapeCharToFullEscapeChar(docList.get(Integer.parseInt(docId) - 1)));
			String[] theta = thetaList.get(idx).split("\t");
			docProp.setTopics(Arrays.stream(theta).mapToDouble(Double::parseDouble).toArray());
			idx++;
			returnList.add(docProp);
		}

		return returnList;
	}
	
	/**
	 * assginファイルをリストにする
	 * 
	 * @param assignPath
	 * @return
	 */
	private List<List<Integer>> readAssginFile(String assignPath) {
		List<List<Integer>> returnList = new ArrayList<>();
		List<String> asList = AssoonUtils.readText(assignPath);
		for (String line : asList) {
			List<Integer> listInt = new ArrayList<>();
			String[] items = line.split(" ");
			for (String item : items) {
				listInt.add(Integer.parseInt(item.split(":")[1]));
			}
			returnList.add(listInt);
		}

		return returnList;
	}
	
	/**
	 * fileからphiの情報を取得する
	 * 
	 * @param path
	 * @return
	 */
	private Map<String, List<Double>> readPhiFile(String path) {
		List<String> phiText = AssoonUtils.readText(path);
		LinkedHashMap<String, List<Double>> returnValue = new LinkedHashMap<>();

		// 単語ごと、トピックごとの生成確率をセットする
		int index = 0;
		for (String word : phiText.get(0).split("\t")) {

			List<Double> doubleValue = new ArrayList<Double>();
			for (int i = 1; i < phiText.size(); i++) {
				String doubleRec = phiText.get(i).split("\t")[index];
				doubleValue.add(Double.parseDouble(doubleRec));
			}
			index++;

			returnValue.put(word, doubleValue);
		}

		return returnValue;
	}
	
	private List<WordProp> readTopicFile(String path) {
		List<String> list = AssoonUtils.readText(path);
		List<WordProp> returnList = new ArrayList<>();
		list.stream().limit(20).forEach(line->{
			WordProp wordProp = new WordProp();
			String[] items = line.split(",");
			wordProp.setWord(items[0]);
			wordProp.setProp(items[1]);
			returnList.add(wordProp);	
		});
		return returnList;
	}

}
