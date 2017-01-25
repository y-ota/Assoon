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

package jp.assoon.mecab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jp.assoon.lda.WordInfo;
import jp.assoon.util.AssoonUtils;
import jp.assoon.util.Constants;

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
	// ストップワード
	private List<String> stopwordList; 
	// MeCab実行パス
	private static String mecabBinPath; 

	public List<List<WordInfo>> getWordInfoList() {
		return wordInfoListList;
	}

	public MeCab(int nword, String mecabPropPath) {
		this.nword = nword;
		// ストップワードファイルを読み込む
		InputStream stopwordStream = getClass().getResourceAsStream("stopword.txt");
		stopwordList = stopwordStream == null ? 
				Collections.emptyList():AssoonUtils.readText(new InputStreamReader(stopwordStream, StandardCharsets.UTF_8));
		// MeCabパスの取得
		Properties properties = new Properties();
		try (InputStreamReader is = new InputStreamReader(new FileInputStream(mecabPropPath), StandardCharsets.UTF_8)) {
			properties.load(is);
		} catch (Exception e) {
			throw new RuntimeException("Not found mecab.properties.");
		}
		// OSによって取得するパスを変更する
		if (AssoonUtils.isWindows()) {
			mecabBinPath = properties.getProperty("mecab.windows.bin");
		} else if (AssoonUtils.isMac()) {
			mecabBinPath = properties.getProperty("mecab.mac.bin");
		} else {
			mecabBinPath = properties.getProperty("mecab.linux.bin");
		}

		// Mecabの実行パスが存在するか
		if (!new File(mecabBinPath).exists()) {
			throw new RuntimeException("Invalid meCab execution path. Please set the value in mecab.properties correctly.");
		}

	}

	public void run(String inputfile, String outputPath, String[] hinshi) {
		//入力ファイルが正しいかチェック
		if(!new File(inputfile).exists()){
			throw new IllegalArgumentException("Inputfile does not found.");
		}
		
		//解析対象の品詞をマッピングする
		List<String> listHinshi = Arrays.stream(hinshi).map(str-> {
			if ("1".equals(str)) {
				return "名詞";
			} else if ("2".equals(str)) {
				return "動詞";
			} else if ("3".equals(str)) {
				return "形容詞";
			} else if ("4".equals(str)) {
				return "副詞";
			} else {
				throw new IllegalArgumentException();
			}
		}).collect(Collectors.toList());
		
		//MeCab実行
		BufferedReader br = null;
		Process ps = null;
		try {
			List<String> list = new ArrayList<String>();
			List<WordInfo> wordInfoList = new ArrayList<>();
			List<String> docIdList = new ArrayList<String>();
			list.add("0");
			StringBuilder sb = new StringBuilder();
			int doccnt = 0;
			int docId = 0;
			int wordLength = 0;
			int wordN = 0;

			// DOSに投げるコマンドと引数を指定する
			String[] command = { mecabBinPath, inputfile };
			ps = Runtime.getRuntime().exec(command);
			br = new BufferedReader(new InputStreamReader(ps.getInputStream(), StandardCharsets.UTF_8));
			String targetLine;

			// 形態素解析結果を全て解析する
			while ((targetLine = br.readLine())!=null) {
				// 1文書の解析の終わりの場合
				if (targetLine.equals("EOS")) {
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

					// 形態素解析の列(例： 保育園  名詞,一般,*,*,*,*,保育園,ホイクエン,ホイクエン)を処理する
				} else {
					// 保育園  名詞,一般,*,*,*,*,保育園,ホイクエン,ホイクエン
					Pattern targetTypePattern = Pattern.compile("([^\\t]+)\\t([^,]+),([^,]+),[^,]+,[^,]+,[^,]+,[^,]+,([^,]+)");
					Matcher matcher = targetTypePattern.matcher(targetLine);
					if (!matcher.find()) throw new RuntimeException("The line dose not match. :" + targetLine); 
					String word = matcher.group(1);
					String targetType1 = matcher.group(2);
					String targetType2 = matcher.group(3);
					String targetEnd = matcher.group(4);

					// 指定して品詞で、かつストップワードでないことかつ
					// 一般名詞、固有名詞、サ変接続 のみとし、読みがない場合(*)はリストに追加しない
					// 参考：http://www.unixuser.org/~euske/doc/postag/
					if (listHinshi.contains(targetType1) 
							&& !stopwordList.contains(word)
							&& !targetEnd.equals("*") 
							&& (targetType2.equals("一般") || targetType2.equals("固有名詞") || targetType2.equals("サ変接続"))) {
						WordInfo wordInfo = new WordInfo();
						wordInfo.setStartIndex(wordLength);
						wordInfo.setEndIndex(word.length());
						wordInfo.setWord(word);
						wordInfoList.add(wordInfo);
						// 半角エスケープ文字を全角に置換
						sb.append(AssoonUtils.replaceHalfEscapeCharToFullEscapeChar(word) + " ");
						wordN++;
					}
					wordLength += word.length();
				}
			}
			// 文書数を１行目にセット
			list.set(0, String.valueOf(doccnt));
			ps.waitFor();
			AssoonUtils.write(list, outputPath);
			AssoonUtils.write(docIdList, outputPath + Constants.SPACE_SEP_FILE_DOC_ID);

		} catch (Exception e) {
			throw new RuntimeException(e);

		} finally {
			if(br!=null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(ps!=null){
				ps.destroy();
			}
		}
	}

}