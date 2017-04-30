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

package jp.assoon.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import jp.assoon.lda.WordProp;

public class AssoonUtils {
	
	private AssoonUtils(){};
	
	/**
	 * After reading the file of the inputStream specified, return list
	 */
	public static List<String> readText(InputStreamReader is) {
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
			throw new UncheckedIOException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		}
	}

	/**
	 * After reading the file of the path specified, return list
	 */
	public static List<String> readText(String filePath) {
		try {
			return Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 *  Write the list specified to the output path specified 
	 */
	public static void write(List<String> list, String outputPath) {
		try {
			Files.write(Paths.get(outputPath), list, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static List<WordProp> fileToWordProp(String path) {
		List<String> list = readText(path);
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


	
	/**
	 * 半角エスケープ文字を全角エスケープ文字に置換する
	 * @param str 半角エスケープ文字
	 * @return 全角エスケープ文字
	 */
	public static String replaceHalfEscapeCharToFullEscapeChar(String str){
		return str.replace("\"", "”").replace("'", "\\'")
		.replace("\\", "￥").replace("%", "％").replace("&", "＆").replace("+", "＋");
	}


	/**
	 * ファイルの中身の半角スペースを全角スペースに置換
	 * 
	 * @param filePath
	 * @param before
	 * @param after
	 */
	public static void replaceHalfSpaceInTextFile(String filePath) {
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
	public static int maxTopicValueDocId(List<Map<Integer, Double>> data, int topicNum) {
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
	 * Return the index of the max value in specified double array
	 */
	public static Integer maxValueTopic(double[] values) {
		List<Double> doubles = Arrays.stream(values).boxed().collect(Collectors.toList());
		int maxIndex = IntStream.range(0, values.length)
				         .boxed().max(Comparator.comparingDouble(doubles::get)).get();
		double max = doubles.get(maxIndex);
		// if the values have multiple max values, return null value.
		Map<Double,Long> map = Arrays.stream(values).boxed()
				                .collect(Collectors.groupingBy(Double::valueOf,Collectors.counting()));
		return  map.get(max).longValue() >= 2? null:maxIndex;
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
	
	public static void deleteDirectory(String dirPath) {
	    recursiveDeleteFile(new File(dirPath));
	}
	
	private static void recursiveDeleteFile(File file) {
	    if (!file.exists()) {
	        return;
	    }
	    if (file.isDirectory()) {
	    	Arrays.stream(file.listFiles()).forEach(a->recursiveDeleteFile(a));
	    }
	    file.delete();
	}

}
