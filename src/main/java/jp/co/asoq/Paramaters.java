package jp.co.asoq;

/**
 * Paramaters of LDA
 * 
 * @author Yusuke Ota
 *
 */
public class Paramaters {
	private String alpha;
	private String beta;
	private String ntopics;
	private String ndocs;
	private String nwords;
	private String liters;
	private String perplexity;

	public String getAlpha() {
		return alpha;
	}

	public void setAlpha(String alpha) {
		this.alpha = alpha;
	}

	public String getBeta() {
		return beta;
	}

	public void setBeta(String beta) {
		this.beta = beta;
	}

	public String getNtopics() {
		return ntopics;
	}

	public void setNtopics(String ntopics) {
		this.ntopics = ntopics;
	}

	public String getNdocs() {
		return ndocs;
	}

	public void setNdocs(String ndocs) {
		this.ndocs = ndocs;
	}

	public String getNwords() {
		return nwords;
	}

	public void setNwords(String nwords) {
		this.nwords = nwords;
	}

	public String getLiters() {
		return liters;
	}

	public void setLiters(String liters) {
		this.liters = liters;
	}

	public String getPerplexity() {
		return perplexity;
	}

	public void setPerplexity(String perplexity) {
		this.perplexity = perplexity;
	}

}
