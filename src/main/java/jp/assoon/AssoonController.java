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
import java.util.List;
import java.util.Locale;
import java.util.Properties;

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

import jp.assoon.lda.LDAExecutior;
import jp.assoon.lda.LDAExecutor;
import jp.assoon.lda.TopicInfo;
import jp.assoon.mecab.MeCabExcecutor;
import jp.assoon.util.Constants;
import jp.assoon.util.AssoonUtils;

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
	
	private double alpha;
	private double beta;
	private int iter;
	private int topic;

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
	public String post(HttpServletRequest request, @RequestParam MultipartFile file, String nword, String[] word, String topic, String demoval, Model model) {
		logger.info("POST Request");
		// WEB-INFパス
		webInfPath = context.getRealPath("/WEB-INF");
		// MeCabのプロパティファイル
		mecabPropPath = webInfPath + "/mecab.properties";
		
		this.topic = Integer.parseInt(topic);
		// LDAの繰り返し回数を＋1する(ライブラリが指定した値-1のため)
		iter++;		
		// LDAのプロパティファイル
		String ldaPropPath = webInfPath + "/lda.properties";
		try (InputStreamReader isr = new InputStreamReader(new FileInputStream(ldaPropPath), "UTF-8")) {
			Properties properties = new Properties();
			properties.load(isr);
			alpha = Double.parseDouble(properties.getProperty("lda.alpha"));
			beta = Double.parseDouble(properties.getProperty("lda.beta"));
			iter = Integer.parseInt(properties.getProperty("lda.iteration"));
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
			if("demo".equals(demoval)){
				FileOutputStream out = new FileOutputStream(new File(userDir + "/" + Constants.POST_FILE));
				FileInputStream inputStream = new FileInputStream(webInfPath + "/sampleForDemo.txt");
				FileCopyUtils.copy(inputStream, out);				
			}else{
				FileOutputStream out = new FileOutputStream(new File(userDir + "/" + Constants.POST_FILE));
				FileCopyUtils.copy(file.getInputStream(), out);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// ファイルの中身の半角スペースを全角スペースに置換
		AssoonUtils.replaceHalfSpaceInTextFile(userDir + "/" + Constants.POST_FILE);
		
		// 形態素解析
		MeCabExcecutor mecab = new MeCabExcecutor(Constants.WORDS_PER_ONE_DOC, mecabPropPath);
		mecab.execute(userDir + "/" + Constants.POST_FILE, userDir + "/" + Constants.SPACE_SEP_FILE, word);
		
		//LDA 実行
		List<TopicInfo>topicInfoList = new LDAExecutor().execute(mecab,userDir,alpha,beta, this.topic,iter);
		
		// 解析の詳細をクライアントに送る
		model.addAttribute("postFlg", true);
		model.addAttribute("topicInfo", topicInfoList);

		//userDir削除
		AssoonUtils.deleteDirectory(userDir);
		
		return "assoon";
	}
	
}
