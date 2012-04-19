package org.mixare.lib;

import java.util.HashMap;

public class HtmlUnescape {

	private static HashMap<String, String> htmlEntities;
	static {
		htmlEntities = new HashMap<String, String>();
		htmlEntities.put("&lt;", "<");
		htmlEntities.put("&gt;", ">");
		htmlEntities.put("&amp;", "&");
		htmlEntities.put("&quot;", "\"");
		htmlEntities.put("&agrave;", "à");
		htmlEntities.put("&Agrave;", "À");
		htmlEntities.put("&acirc;", "â");
		htmlEntities.put("&auml;", "ä");
		htmlEntities.put("&Auml;", "Ä");
		htmlEntities.put("&Acirc;", "Â");
		htmlEntities.put("&aring;", "å");
		htmlEntities.put("&Aring;", "Å");
		htmlEntities.put("&aelig;", "æ");
		htmlEntities.put("&AElig;", "Æ");
		htmlEntities.put("&ccedil;", "ç");
		htmlEntities.put("&Ccedil;", "Ç");
		htmlEntities.put("&eacute;", "é");
		htmlEntities.put("&Eacute;", "É");
		htmlEntities.put("&egrave;", "è");
		htmlEntities.put("&Egrave;", "È");
		htmlEntities.put("&ecirc;", "ê");
		htmlEntities.put("&Ecirc;", "Ê");
		htmlEntities.put("&euml;", "ë");
		htmlEntities.put("&Euml;", "Ë");
		htmlEntities.put("&iuml;", "ï");
		htmlEntities.put("&Iuml;", "Ï");
		htmlEntities.put("&ocirc;", "ô");
		htmlEntities.put("&Ocirc;", "Ô");
		htmlEntities.put("&ouml;", "ö");
		htmlEntities.put("&Ouml;", "Ö");
		htmlEntities.put("&oslash;", "ø");
		htmlEntities.put("&Oslash;", "Ø");
		htmlEntities.put("&szlig;", "ß");
		htmlEntities.put("&ugrave;", "ù");
		htmlEntities.put("&Ugrave;", "Ù");
		htmlEntities.put("&ucirc;", "û");
		htmlEntities.put("&Ucirc;", "Û");
		htmlEntities.put("&uuml;", "ü");
		htmlEntities.put("&Uuml;", "Ü");
		htmlEntities.put("&nbsp;", " ");
		htmlEntities.put("&copy;", "\u00a9");
		htmlEntities.put("&reg;", "\u00ae");
		htmlEntities.put("&euro;", "\u20a0");
	}

	public static String unescapeHTML(String source, int start) {
		int i, j;

		i = source.indexOf("&", start);
		if (i > -1) {
			j = source.indexOf(";", i);
			if (j > i) {
				String entityToLookFor = source.substring(i, j + 1);
				String value = (String) htmlEntities.get(entityToLookFor);
				if (value != null) {
					source = new StringBuffer().append(source.substring(0, i))
							.append(value).append(source.substring(j + 1))
							.toString();
					return unescapeHTML(source, i + 1); // recursive call
				}
			}
		}
		return source;
	}

}