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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
			Files.write(Paths.get(outputPath), list, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<WordProp> fileToWordProp(String path) {
		List<String> list = readText(path);
		List<WordProp> returnList = new ArrayList<>();
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
			docProp.setDoument(replaceHalfEscapeCharToFullEscapeChar(docList.get(Integer.parseInt(docId) - 1)));
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
	 * 半角エスケープ文字を全角エスケープ文字に置換する
	 * @param str 半角エスケープ文字
	 * @return 全角エスケープ文字
	 */
	public String replaceHalfEscapeCharToFullEscapeChar(String str){
		return str.replace("\"", "”").replace("'", "\\'")
		.replace("\\", "￥").replace("%", "％").replace("&", "＆").replace("+", "＋");
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
	public void replaceHalfSpaceInTextFile(String filePath) {
		List<String> fileList = readText(filePath).stream().map(a->a.replace(" ", "　")).collect(Collectors.toList());
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
	
	public void deleteDirectory(String dirPath) {
	    File file = new File(dirPath);
	    recursiveDeleteFile(file);
	}
	
	private void recursiveDeleteFile(File file) {
	    if (!file.exists()) {
	        return;
	    }
	    if (file.isDirectory()) {
	    	Arrays.stream(file.listFiles()).forEach(a->recursiveDeleteFile(a));
	    }
	    file.delete();
	}

}
