<%@page import="jp.assoon.Paramaters"%>
<%@page import="jp.assoon.TopicInfo"%>
<%@page import="jp.assoon.WordProp"%>
<%@page import="java.util.List"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html>
<html lang="jp">
<head>
<meta charset="UTF-8">
<title>Assoon</title>
<link rel="stylesheet" type="text/css"
	href="<c:url value="/resources/css/style.css" />" />
<link rel="stylesheet" type="text/css"
	href="<c:url value="/resources/css/skins/minimal/minimal.css" />" />
<link rel="stylesheet"
	href="http://code.jquery.com/ui/1.9.2/themes/base/jquery-ui.css" />
<link rel="icon" type="image/png"
	href="<c:url value="/resources/image/asoq_logo.png" />" sizes="32x32">
<script src='<c:url value="/resources/js/d3.js" />'></script>
<script src='<c:url value="/resources/js/d3.layout.cloud.js" />'></script>
<script src='<c:url value="/resources/js/wordcloud.js" />'></script>
<script src='<c:url value="/resources/js/jquery-1.8.3.js" />'></script>
<script src='<c:url value="/resources/js/jquery-ui.js" />'></script>
<script src='<c:url value="/resources/js/icheck.js" />'></script>
<script src='<c:url value="/resources/js/assoon.js" />'></script>
</head>
<body>
	<div id="demo"></div>
	<div id="container">
		<h1>
			<img src="<c:url value="/resources/image/asoq_logo.png" />"
				width="100" height="90" class="stepbtn">Assoon<span
				class="version">Ver 0.5.0</span>
		</h1>
		<div class="top">
			<p>
				Assoonは大量の自由記述アンケートの回答を自動的に要約するWebシステムです。<br>
				テキストファイルをアップロードするだけで簡単にご利用できます。
			</p>
		</div>
		<form id="form" method="post" enctype="multipart/form-data">
			<table id="run">
				<tr>
					<td>
						<div class="ui-widget">
							<input id="path" readonly="readonly" size="70" />
						</div>
					</td>
					<td><input type="button" name="btn-run" id="btn-run"
						value="Open" /> <input type="file" name="file" id="file" value="" />
						<input type="submit" name="submit2" id="submit2" value="qaa"
						style="display: none" /></td>
					<td><input type="button" name="btn-upload" id="btn-upload"
						value="Run" /></td>
					<td>
						<Button type="button" id="setting">Setting</Button>
					</td>
				</tr>
			</table>
			<p />

			<div class="setting-form">
				<input type="checkbox" name="word" value="1" checked="checked">名詞
				<input type="checkbox" name="word" value="2" id="check">動詞 <input
					type="checkbox" name="word" value="3" id="check">形容詞 <input
					type="checkbox" name="word" value="4" id="check">副詞 <input
					type="checkbox" name="word" value="5" id="check">助詞 <input
					type="checkbox" name="word" value="6" id="check">すべての品詞
				<p></p>
				<label for="topic">トピック数</label> <input type="text" name="topic"
					id="topic" value="10" maxlength='3' required
					style="font-size: 18px;">
			</div>
			<c:if test="${empty postFlg}">
				<div id="step_container">
					<div id="step-div">
						<span id="text-word">STEP1 ファイル選択</span>
					</div>
					<p>
						<img src="<c:url value="/resources/image/openbtn.png" />"
							width="60" height="35" class="stepbtn">を押してテキストファイルを選択します。<br>
						テキストファイルの形式は、<span style="text-decoration: underline">1行につき1意見</span>です。
					</p>
					<div class="divider_line">&nbsp;</div>

					<div id="step-div">
						<span id="text-word">STEP2 実行</span>
					</div>
					<p>
						<img src="<c:url value="/resources/image/runbtn.png" />"
							width="60" height="35" class="stepbtn">を押すと処理が始まり、要約結果が表示されます。
					</p>
					<img src="<c:url value="/resources/image/summary2.png" />"
						width="329" height="168">
					<div class="divider_line">&nbsp;</div>

					<div id="step-div">
						<span id="text-word">オプション</span>
					</div>
					<p>
						<img src="<c:url value="/resources/image/confbtn.png" />"
							width="35" height="35" class="stepbtn">で必要に応じて設定を変更します。
					</p>

					<div class="divider_line">&nbsp;</div>


				</div>
			</c:if>
		</form>
		<p />
		<div id="loading2">
			<img src="<c:url value="/resources/image/load.gif" />">
		</div>
		<c:if test="${!empty postFlg}">
			<div id="loading">
				<img src="<c:url value="/resources/image/load.gif" />">
			</div>
			<div id="summary-div">SUMMARY</div>
			<!-- <div id="summary"><p>トピックの特徴を考慮した要約です。</p></div> -->
			<table class="bordered">

				<tbody id="output"></tbody>
			</table>
		</c:if>

		<div class="footer">
			<p>© 2016 Yusuke Ota All Rights Reserved.</p>
		</div>
	</div>
	<c:if test="${!empty postFlg}">
		<script type="text/javascript">
 			<%List<TopicInfo> list = (List<TopicInfo>) request.getAttribute("topicInfo");
				int rankingNum = 0;
				for (TopicInfo topicInfo : list) {
					int num = 0;%>
				 var texts = [];
				 var wordCloud = new Array();
			<%for (WordProp wordProp : topicInfo.getWordProp()) {%>
				      wordCloud[<%=num%>] = { word : "<%=wordProp.getWord()%>", prop : "<%=wordProp.getProp()%>"};
			<%num++;
					}
					for (String text : topicInfo.getDocument()) {%>
					  texts.push("<%=text%>");
			<%}%>
		    <%int topic = topicInfo.getNum() + 1;%>
		    <%int simNum = topicInfo.getSimcount();%>
		    <%rankingNum++;%>
		    show("<%=rankingNum%>", wordCloud, texts,
		<%=simNum%>
			,
		<%=topic%>
			);
		<%}%>
			
		</script>
	</c:if>
	<div id="dialog" title="Error">
		<p>ファイルを選択してください。</p>
	</div>
</body>
</html>
