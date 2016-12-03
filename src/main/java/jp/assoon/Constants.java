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

/**
 * Constants for ASOQ
 * 
 * @author Yusuke Ota
 *
 */
public class Constants {
	// posted text file by user
	public static final String POST_FILE = "postfile.txt";
	// text file after executing MeCab
	public static final String SPACE_SEP_FILE = "spaceSepFile.txt";
	//
	public static final String SPACE_SEP_FILE_DOC_ID = "_docId.txt";

	// html tags for summary
	public static final String HTML_FONT_START = "<span id=\\\"text-word\\\">";
	public static final String HTML_FONT_END = "</span>";

	// number of representative text
	public static final int PRINT_TOPIC_DOC = 1;
	// number of words per one document
	public static final int WORDS_PER_ONE_DOC = 1;

}
