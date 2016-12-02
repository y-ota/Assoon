package jp.co.asoq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Utility {
	public List<String> readText(InputStreamReader is) {
		List<String> list = new ArrayList<String>();
		BufferedReader br = null;
		String line = null;

		try {
			br = new BufferedReader(is);
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
			return list;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

	}

	public List<String> readText(String filePath) {
		List<String> list = new ArrayList<String>();
		BufferedReader br = null;
		String line = null;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)), "UTF-8"));
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
			return list;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

		}

	}

	public void write(List<String> list, String outputPath) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath)));
			for (String line : list) {
				bw.write(line);
				bw.newLine();
			}
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public List<WordProp> fileToWordProp(String path) {
		List<String> list = readText(path);
		List<WordProp> returnList = new ArrayList<>();
		// for(String line:list){
		for (int i = 0; i < 20; i++) {
			WordProp wordProp = new WordProp();
			String[] items = list.get(i).split(",");
			wordProp.setWord(items[0]);
			wordProp.setProp(items[1]);
			returnList.add(wordProp);
		}
		return returnList;
	}

	public List<DocProp> fileToDocProp(String docPath, String thetaPath, String sepDocIdFilePath) {
		List<String> docList = readText(docPath);
		List<String> thetaList = readText(thetaPath);

		List<DocProp> returnList = new ArrayList<>();

		List<String> docIdList = readText(sepDocIdFilePath);
		int idx = 0;
		for (String docId : docIdList) {
			DocProp docProp = new DocProp();
			docProp.setDocId(idx);
			docProp.setDoument(docList.get(Integer.parseInt(docId) - 1).replace("\"", "”").replace("'", "\\'")
					.replace("\\", "￥").replace("%", "％").replace("&", "＆").replace("+", "＋"));
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
	public List<List<Integer>> fileToTopicAssing(String assignPath) {
		List<List<Integer>> returnList = new ArrayList<>();
		List<String> asList = readText(assignPath);
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
		List<String> phiText = readText(path);
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

	/**
	 * ファイルの中身の半角スペースを全角スペースに置換
	 * 
	 * @param filePath
	 * @param before
	 * @param after
	 */
	public void replaceTextFile(String filePath, String before, String after) {
		List<String> fileList = readText(filePath);
		for (int i = 0; i < fileList.size(); i++) {
			fileList.set(i, fileList.get(i).replace(before, after));
		}
		write(fileList, filePath);
	}

	/**
	 * データの最大値のトピックを返す
	 * 
	 * @param data
	 * @param topicNum
	 * @return
	 */
	public int maxTopicValueDocId(List<Map<Integer, Double>> data, int topicNum) {
		int docId = 0;
		double maxValue = 0.0;
		if (data.get(0).containsKey(topicNum)) {
			maxValue = data.get(0).get(topicNum);
		}

		int index = 0;
		for (Map<Integer, Double> map : data) {
			if (map.containsKey(topicNum)) {
				if (maxValue < map.get(topicNum)) {
					maxValue = map.get(topicNum);
					docId = index;
				}
			}
			index++;
		}
		return docId;
	}

	/**
	 * 最大トピック番号を返す
	 * 
	 * @param values
	 * @return
	 */
	public Integer maxValueTopic(double[] values) {
		double max = values[0];
		int maxIndex = 0;
		for (int i = 0; i < values.length; i++) {
			if (max < values[i]) {
				max = values[i];
				maxIndex = i;
			}
		}

		// 最大値が複数ある場合は、nullを返す
		for (int i = 0; i < values.length; i++) {
			if (values[i] == max && maxIndex != i) {
				return 99999999;
			}
		}
		return maxIndex;
	}

	private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

	public static boolean isLinux() {
		return OS_NAME.startsWith("linux");
	}

	public static boolean isMac() {
		return OS_NAME.startsWith("mac");
	}

	public static boolean isWindows() {
		return OS_NAME.startsWith("windows");
	}

}