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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MeCab
 * 
 * @author Yusuke Ota
 *
 */
public class MeCab {

	// 1文書あたりの単語数
	private int nword;
	private List<List<WordInfo>> wordInfoListList = new ArrayList<List<WordInfo>>();
	private List<String> stopwordList = new ArrayList<String>(); // ストップワード
	private static String mecabBinPath; // MeCab実行パス
	private Utility utility = new Utility();

	public List<List<WordInfo>> getWordInfoList() {
		return wordInfoListList;
	}

	public MeCab(int nword, String mecabPropPath) {
		this.nword = nword;

		try {
			// ストップワードファイルを読み込む
			InputStream stopwordStream = getClass().getResourceAsStream("stopword.txt");
			if(stopwordStream == null){
				stopwordList = Collections.emptyList();
			}else{
				stopwordList = utility.readText(new InputStreamReader(stopwordStream, "UTF-8"));
			}

			// MeCabパスの取得
			Properties properties = new Properties();
			try (InputStreamReader is = new InputStreamReader(new FileInputStream(mecabPropPath), "UTF-8")) {
				properties.load(is);
			} catch (Exception e) {
				throw new RuntimeException("Not found mecab.properties.");
			}
			// OSによって取得するパスを変更する
			if (Utility.isWindows()) {
				mecabBinPath = properties.getProperty("mecab.windows.bin");
			} else if (Utility.isMac()) {
				mecabBinPath = properties.getProperty("mecab.mac.bin");
			} else {
				mecabBinPath = properties.getProperty("mecab.linux.bin");
			}

			// Mecabの実行パスが存在するか
			if (!new File(mecabBinPath).exists()) {
				throw new RuntimeException("Invalid meCab execution path. Please set the value in mecab.properties correctly.");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public void run(String inputfile, String outputPath, String[] hinshi) {
		try {
			List<String> listHinshi = new ArrayList<String>();
			for (String str : hinshi) {
				if ("1".equals(str)) {
					listHinshi.add("名詞");
				} else if ("2".equals(str)) {
					listHinshi.add("動詞");
				} else if ("3".equals(str)) {
					listHinshi.add("形容詞");
				} else if ("4".equals(str)) {
					listHinshi.add("副詞");
				} else if ("5".equals(str)) {
					listHinshi.add("助詞");
				} else if ("6".equals(str)) {
					listHinshi.add("ALL");
				}
			}

			// DOSに投げるコマンドと引数を指定する
			String[] command = { mecabBinPath, inputfile };

			// コマンド結果をProcessで受け取る
			Process ps = Runtime.getRuntime().exec(command);

			// 標準出力
			BufferedReader bReader_i = new BufferedReader(new InputStreamReader(ps.getInputStream(), "UTF-8"));

			// 標準出力を1行ずつ受け取る一時オブジェクト
			String targetLine;

			List<String> list = new ArrayList<String>();
			list.add("0");
			StringBuilder sb = new StringBuilder();
			List<WordInfo> wordInfoList = new ArrayList<>();
			int doccnt = 0;
			int docId = 0;
			int wordLength = 0;
			int wordN = 0;

			List<String> docIdList = new ArrayList<String>();
			// 形態素解析結果を全て解析する
			while (true) {

				// 形態素解析結果を1行ずつ受け取る
				targetLine = bReader_i.readLine();

				// 最終行まで解析が完了したらループを抜ける
				if (targetLine == null) {
					break;
				} else if (targetLine.equals("EOS")) {
					docId++;
					// 末尾のスペース削除
					if (sb.length() > 0 && wordN >= this.nword) {
						doccnt++;
						sb.deleteCharAt(sb.length() - 1);
						docIdList.add(String.valueOf(docId));
						list.add(sb.toString());
						wordInfoListList.add(wordInfoList);

					} else {
						System.out.println("skip row number:" + docId);
					}
					wordN = 0;
					wordLength = 0;
					sb = new StringBuilder();
					wordInfoList = new ArrayList<>();
					continue;
				} else {
					String targetType1 = "";
					String targetType2 = "";
					String targetNotChar = "";
					String word = "";
					Pattern targetTypePattern = Pattern.compile("([^\\t]+)\\t([^,]+),([^,]+),[^,]+,[^,]+,[^,]+,[^,]+,([^,]+)");
					Matcher matcher = targetTypePattern.matcher(targetLine);
					if (matcher.find()) {
						// 保育園  名詞,一般,*,*,*,*,保育園,ホイクエン,ホイクエン
						targetType1 = matcher.group(2);
						targetType2 = matcher.group(3);
						targetNotChar = matcher.group(4);
						word = matcher.group(1);
					} else {
						throw new RuntimeException(targetLine);
					}

					// 指定して品詞で、かつストップワードでないことかつ
					// 読みがない場合(*)はリストに追加しない
					if ((listHinshi.contains("ALL") || listHinshi.contains(targetType1)) && !stopwordList.contains(word)
							&& !targetNotChar.equals("*")
							&& !targetType2.equals("代名詞")
							&& !targetType2.equals("接尾")
							&& !targetType2.equals("数")
							&& !targetType2.equals("副詞可能")) {
						WordInfo wordInfo = new WordInfo();
						wordInfo.setStartIndex(wordLength);
						wordInfo.setEndIndex(word.length());
						wordInfo.setWord(word);
						wordInfoList.add(wordInfo);

						// 半角エスケープ文字を全角に置換
						sb.append(utility.replaceHalfEscapeCharToFullEscapeChar(word) + " ");
						wordN++;
					}
					wordLength += word.length();
				}
			}
			// 文書数を１行目にセット
			list.set(0, String.valueOf(doccnt));
			ps.waitFor();
			utility.write(list, outputPath);
			utility.write(docIdList, outputPath + Constants.SPACE_SEP_FILE_DOC_ID);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}