package dk.nota.oxygen.epub.opf;

public enum OutputType {
	
	INSP_AUDIO, INSP_BRAILLE, INSP_ETEXT, INSP_PRINT, INSP_PROOF;
	
	public String getName() {
		switch (this) {
		case INSP_AUDIO: return "Inspiration: Lyd";
		case INSP_BRAILLE: return "Inspiration: Punkt";
		case INSP_ETEXT: return "Inspiration: E-tekst";
		case INSP_PRINT: return "Inspiration: Tryk";
		case INSP_PROOF: return "Inspiration: Korrektur";
		default: return null;
		}
	}
	
	public String getPrefix() {
		switch (this) {
		case INSP_AUDIO: return "INSL";
		case INSP_BRAILLE: return "INSP";
		case INSP_ETEXT: return "INSE";
		case INSP_PRINT: return "INST";
		case INSP_PROOF: return "INSK";
		default: return null;
		}
	}
	
}
