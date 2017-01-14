package jp.assoon.lda;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import jgibblda.LDA;
import jp.assoon.mecab.MeCab;
import jp.assoon.util.Constants;
import jp.assoon.util.Utility;

public class LDAExecution {
	
	public List<TopicInfo> executeLDA(MeCab mecab, String userDir, double alpha, double beta, int topic, int iter){
		// LDA実行
		LDA.main(new String[] { "-est", "-alpha", String.valueOf(alpha), "-beta", String.valueOf(beta), "-ntopics", 
				String.valueOf(topic), "-niters", String.valueOf(iter), "-twords",
				"20", "-dfile", Constants.SPACE_SEP_FILE, "-dir", userDir + "/", "-savestep", "10000" });

		// オブジェクトに文書-トピック分布を入れる
		List<DocProp> listDoc = fileToDocProp(userDir + "/" + Constants.POST_FILE,
				userDir + "/model-final.theta",
				userDir + "/" + Constants.SPACE_SEP_FILE + Constants.SPACE_SEP_FILE_DOC_ID);

		// 各文書の単語に割り当てられたトピック
		List<List<Integer>> wordTopicAssignList = fileToTopicAssing(userDir + "/model-final.tassign");

		// トピックごとの単語の確率
		Map<String, List<Double>> phiMap = fileToPhi(userDir + "/model-final.phi");

		// 文書ごと、トピックごとに単語の生成確率の平均を出す
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
											.get(utility.replaceHalfEscapeCharToFullEscapeChar(mecab.getWordInfoList().get(i).get(j).getWord()))
											.get(wordTopicAssignList.get(i).get(j)));
				} else {
					scoreMap.put(wordTopicAssignList.get(i).get(j), phiMap
							.get(utility.replaceHalfEscapeCharToFullEscapeChar(mecab.getWordInfoList().get(i).get(j).getWord()))
							.get(wordTopicAssignList.get(i).get(j)));
				}
			}
			socreList.add(scoreMap);
		}

		Map<Integer, Integer> topicCountMap = new TreeMap<>();
		for (Map<Integer, Double> maps : socreList) {
			double[] topicValue = new double[topic];
			for (Integer id : maps.keySet()) {
				topicValue[id] = maps.get(id);
			}

			if (topicCountMap.containsKey(utility.maxValueTopic(topicValue))) {
				topicCountMap.put(utility.maxValueTopic(topicValue),
						topicCountMap.get(utility.maxValueTopic(topicValue)) + 1);
			} else {
				topicCountMap.put(utility.maxValueTopic(topicValue), 1);
			}
		}

		// オブジェクトに単語分布を入れる
		List<TopicInfo> topicInfoList = new ArrayList<>();
		for (int i = 0; i < topic; i++) {
			TopicInfo topicInfo = new TopicInfo();
			topicInfo.setNum(i);
			topicInfo.setWordProp(utility.fileToWordProp(userDir + "/topic" + i + ".csv"));

			if (topicCountMap.containsKey(i)) {
				topicInfo.setSimcount(topicCountMap.get(i));
			} else {
				topicInfo.setSimcount(0);
			}

			List<String> list = new ArrayList<>();

			// トピック比率がもっとも高いテキストをadd
			for (int j = 0; j < 2; j++) {
				List<WordInfo> wordInfoList = mecab.getWordInfoList()
						.get(listDoc.get(utility.maxTopicValueDocId(socreList, i)).getDocId());
				List<Integer> topicAssList = wordTopicAssignList
						.get(listDoc.get(utility.maxTopicValueDocId(socreList, i)).getDocId());
				String text = listDoc.get(utility.maxTopicValueDocId(socreList, i)).getDoument();

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
			topicInfo.setDocument(list);
			topicInfoList.add(topicInfo);
		}

		return topicInfoList.stream().sorted((a,b)->b.getSimcount()-a.getSimcount()).collect(Collectors.toList());
	}
	
	Utility utility = new Utility();
	private List<DocProp> fileToDocProp(String docPath, String thetaPath, String sepDocIdFilePath) {
		List<String> docList = utility.readText(docPath);
		List<String> thetaList = utility.readText(thetaPath);

		List<DocProp> returnList = new ArrayList<>();

		List<String> docIdList = utility.readText(sepDocIdFilePath);
		int idx = 0;
		for (String docId : docIdList) {
			DocProp docProp = new DocProp();
			docProp.setDocId(idx);
			docProp.setDoument(utility.replaceHalfEscapeCharToFullEscapeChar(docList.get(Integer.parseInt(docId) - 1)));
			String[] theta = thetaList.get(idx).split("\t");
			double[] thetaDouble = new double[theta.length];

			for (int j = 0; j < theta.length; j++) {
				thetaDouble[j] = Double.parseDouble(theta[j]);
			}
			docProp.setTopics(thetaDouble);
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
	private List<List<Integer>> fileToTopicAssing(String assignPath) {
		List<List<Integer>> returnList = new ArrayList<>();
		List<String> asList = utility.readText(assignPath);
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
	public Map<String, List<Double>> fileToPhi(String path) {
		List<String> phiText = utility.readText(path);
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

}
