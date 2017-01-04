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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jgibblda.LDA;

/**
 * Asoon Controler
 * 
 * @author yusuke Ota
 *
 */
@Controller
public class AssoonController {
	private static final Logger logger = LoggerFactory.getLogger(AssoonController.class);
	@Autowired
    private ServletContext context; 
	private String webInfPath;
	private String mecabPropPath;
	
	private String alpha;
	private String beta;
	private String iter;
	private String topic;
	
	private Utility utility = new Utility();

    @RequestMapping("/sample")
    public void downloadSampleFile(HttpServletResponse res) throws IOException {
    	logger.info("Download sample file");
    	String sampleFilePath = context.getRealPath("/WEB-INF/sample.txt");
        File file = new File(sampleFilePath);
        res.setContentLength((int) file.length());
        res.setContentType(MediaType.TEXT_PLAIN_VALUE);
        res.setHeader("Content-Disposition", "attachment; filename=\"sample.txt\"");
        FileCopyUtils.copy(new FileInputStream(file), res.getOutputStream());
    }
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String get(Locale locale, Model model) {
		logger.info("GET Request");
		return "assoon";
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public String post(HttpServletRequest request, @RequestParam MultipartFile file, String nword, String[] word, String topic, Model model) {
		logger.info("POST Request");
		
		this.topic = topic;
		// WEB-INFパス
		webInfPath = context.getRealPath("/WEB-INF");
		// MeCabのプロパティファイル
		mecabPropPath = webInfPath + "/mecab.properties";
		// LDAのプロパティファイル
		String ldaPropPath = webInfPath + "/lda.properties";
		try (InputStreamReader isr = new InputStreamReader(new FileInputStream(ldaPropPath), "UTF-8")) {
			Properties properties = new Properties();
			properties.load(isr);
			alpha = properties.getProperty("lda.alpha");
			beta = properties.getProperty("lda.beta");
			iter = properties.getProperty("lda.iteration");
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		
		// データを格納するディレクトリ生成
		String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Timestamp(System.currentTimeMillis()));
		String userDir =  webInfPath + "/data/" + timestamp;
		if (!new File(userDir).mkdirs()) {
			throw new RuntimeException("ディレクトリが既に存在します");
		}
		// クライアントから送られたファイルをコピーする
		try {
			FileOutputStream out = new FileOutputStream(new File(userDir + "/" + Constants.POST_FILE));
			FileCopyUtils.copy(file.getInputStream(), out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// LDAの繰り返し回数を＋1する(ライブラリが指定した値-1のため)
		iter = String.valueOf(Integer.parseInt(iter) + 1);

		// ファイルの中身の半角スペースを全角スペースに置換
		utility.replaceHalfSpaceInTextFile(userDir + "/" + Constants.POST_FILE);
		
		// 形態素解析
		MeCab mecab = new MeCab(Constants.WORDS_PER_ONE_DOC, mecabPropPath);
		mecab.run(userDir + "/" + Constants.POST_FILE, userDir + "/" + Constants.SPACE_SEP_FILE, word);
		
		//LDA 実行
		List<TopicInfo>topicInfoList = executeLDA(mecab,userDir);
		
		// 解析の詳細をクライアントに送る
		model.addAttribute("postFlg", true);
		model.addAttribute("topicInfo", topicInfoList);

		//userDir削除
		utility.deleteDirectory(userDir);
		
		return "assoon";
	}
	
	private List<TopicInfo> executeLDA(MeCab mecab, String userDir){
		// LDA実行
		LDA.main(new String[] { "-est", "-alpha", alpha, "-beta", beta, "-ntopics", topic, "-niters", iter, "-twords",
				"20", "-dfile", Constants.SPACE_SEP_FILE, "-dir", userDir + "/", "-savestep", "10000" });

		// オブジェクトに文書-トピック分布を入れる
		List<DocProp> listDoc = utility.fileToDocProp(userDir + "/" + Constants.POST_FILE,
				userDir + "/model-final.theta",
				userDir + "/" + Constants.SPACE_SEP_FILE + Constants.SPACE_SEP_FILE_DOC_ID);

		// 各文書の単語に割り当てられたトピック
		List<List<Integer>> wordTopicAssignList = utility.fileToTopicAssing(userDir + "/model-final.tassign");

		// トピックごとの単語の確率
		Map<String, List<Double>> phiMap = utility.fileToPhi(userDir + "/model-final.phi");

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
			double[] topicValue = new double[Integer.parseInt(topic)];
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
		int topicNum = Integer.parseInt(topic);
		for (int i = 0; i < topicNum; i++) {
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

		Collections.sort(topicInfoList, new TopicInfoComparator(TopicInfoComparator.DESC));
		
		return topicInfoList;
	}

}
