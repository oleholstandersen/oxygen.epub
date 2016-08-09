package dk.nota.oxygen.epub.opf;

public enum InspirationOutputType {
	
	AUDIO, BRAILLE, ETEXT, PRINT, PROOF;
	
	public String getName() {
		switch (this) {
		case AUDIO: return "Inspiration: Lyd";
		case BRAILLE: return "Inspiration: Punkt";
		case ETEXT: return "Inspiration: E-tekst";
		case PRINT: return "Inspiration: Tryk";
		case PROOF: return "Inspiration: Korrektur";
		default: return null;
		}
	}
	
	public String getPrefix() {
		switch (this) {
		case AUDIO: return "NYLK";
		case BRAILLE: return "NYLP";
		case ETEXT: return "NYLD";
		case PRINT: return "TRYK";
		case PROOF: return "KORREKTUR";
		default: return null;
		}
	}
	
}
