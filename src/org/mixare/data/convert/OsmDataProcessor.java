package org.mixare.data.convert;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONException;
import org.mixare.MixView;
import org.mixare.NavigationMarker;
import org.mixare.data.DataHandler;
import org.mixare.lib.marker.Marker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

public class OsmDataProcessor extends DataHandler implements DataProcessor {

	public static final int MAX_JSON_OBJECTS = 1000;

	@Override
	public String[] getUrlMatch() {
		String[] str = { "mapquestapi", "osm" };
		return str;
	}

	@Override
	public String[] getDataMatch() {
		String[] str = { "mapquestapi", "osm" };
		return str;
	}

	@Override
	public List<Marker> load(String rawData, int taskId, int colour)
			throws JSONException {
		Element root = convertToXmlDocument(rawData).getDocumentElement();

		List<Marker> markers = new ArrayList<Marker>();
		NodeList nodes = root.getElementsByTagName("node");

		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			NamedNodeMap att = node.getAttributes();
			NodeList tags = node.getChildNodes();
			for (int j = 0; j < tags.getLength(); j++) {
				Node tag = tags.item(j);
				if (tag.getNodeType() != Node.TEXT_NODE) {
					String key = tag.getAttributes().getNamedItem("k")
							.getNodeValue();
					if (key.equals("name")) {

						String name = tag.getAttributes().getNamedItem("v")
								.getNodeValue();
						double lat = Double.valueOf(att.getNamedItem("lat")
								.getNodeValue());
						double lon = Double.valueOf(att.getNamedItem("lon")
								.getNodeValue());

						Log.v(MixView.TAG, "OSM Node: " + name + " lat " + lat
								+ " lon " + lon + "\n");

						Marker ma = new NavigationMarker(
								name,
								lat,
								lon,
								0,
								"http://www.openstreetmap.org/?node="
										+ att.getNamedItem("id").getNodeValue(),
								taskId, colour);
						markers.add(ma);

						// skip to next node
						continue;
					}
				}
			}
		}
		return markers;
	}

	public Document convertToXmlDocument(String rawData) {
		Document doc = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			// Document doc = builder.parse(is);d
			doc = builder.parse(new InputSource(new StringReader(rawData)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}

}
