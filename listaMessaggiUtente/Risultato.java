package dev.chris;

import java.util.ArrayList;
import java.util.List;

public class Risultato {

	private String risult;
	private List<List<String>> messaggi;
	
	public Risultato() {
		messaggi = new ArrayList<List<String>>();
	}
	
	public String getRisult() {
		return risult;
	}
	public void setRisult(String risult) {
		this.risult = risult;
	}

	public List<List<String>> getMessaggi() {
		return messaggi;
	}

	public void setMessaggi(List<List<String>> utenti) {
		this.messaggi = utenti;
	}
	
	
	
	
	

	
	
	
	
	
}
