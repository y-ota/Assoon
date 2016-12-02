package jp.co.asoq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

	public List<List<WordInfo>> getWordInfoList() {
		return wordInfoListList;
	}

	public MeCab(int nword, String mecabPropPath) {
		this.nword = nword;

		try {
			// ストップワードファイルを読み込む
			stopwordList = new Utility()
					.readText(new InputStreamReader(getClass().getResourceAsStream("stopword.txt"), "UTF-8"));

			// MeCabパスの取得
			Properties properties = new Properties();
			try (InputStreamReader is = new InputStreamReader(new FileInputStream(mecabPropPath), "UTF-8")) {
				properties.load(is);
			} catch (Exception e) {
				throw new RuntimeException("mecab.propertiesが見つかりません。");
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
				throw new RuntimeException("MeCabの実行パスが存在しません。mecab.propertiesの設定を見直してください。");
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
						System.out.println("空行のためスキップします 行番号:" + docId);
					}
					wordN = 0;
					wordLength = 0;
					sb = new StringBuilder();
					wordInfoList = new ArrayList<>();
					continue;
				} else {
					String targetType1 = "";
					String targetType2 = "";
					String word = "";
					Pattern targetTypePattern = Pattern.compile("([^\\t]+)\\t([^,]+),([^,]+),.+");
					Matcher matcher = targetTypePattern.matcher(targetLine);
					if (matcher.find()) {
						targetType1 = matcher.group(2);
						targetType2 = matcher.group(3);
						word = matcher.group(1);
					} else {
						throw new RuntimeException();
					}

					// 指定して品詞で、かつストップワードでないこと
					// 名詞でかつサ変接続じゃないこと
					if ((listHinshi.contains("ALL") || listHinshi.contains(targetType1)) && !stopwordList.contains(word)
							&& !(targetType1.equals("名詞") && targetType2.equals("サ変接続"))) {
						WordInfo wordInfo = new WordInfo();
						wordInfo.setStartIndex(wordLength);
						wordInfo.setEndIndex(word.length());
						wordInfo.setWord(word);
						wordInfoList.add(wordInfo);

						// エスケープ文字は前買うに置換
						sb.append(word.replace("\"", "”").replace("'", "‘").replace("\\", "￥").replace("%", "％")
								.replace("&", "＆").replace("+", "＋") + " ");
						wordN++;
					}
					wordLength += word.length();
				}
			}
			// 文書数を１行目にセット
			list.set(0, String.valueOf(doccnt));
			ps.waitFor();
			Utility utility = new Utility();
			utility.write(list, outputPath);
			utility.write(docIdList, outputPath + Constants.SPACE_SEP_FILE_DOC_ID);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}