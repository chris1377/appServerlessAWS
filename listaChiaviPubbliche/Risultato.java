package dev.chris;

import java.util.ArrayList;
import java.util.List;

public class Risultato {

	private String risult;
	private List<List<String>> chiavi;
	
	public Risultato() {
		chiavi = new ArrayList<List<String>>();
	}
	
	public String getRisult() {
		return risult;
	}
	public void setRisult(String risult) {
		this.risult = risult;
	}

	public List<List<String>> getChiavi() {
		return chiavi;
	}

	public void setChiavi(List<List<String>> chiavi) {
		this.chiavi = chiavi;
	}

	
	
	
	
	

	
	
	
	
	
}
