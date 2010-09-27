
package org.mixare.data;

import org.mixare.MixView;
import org.mixare.reality.PhysicalPlace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

/**
 * @author hannes
 *
 */
public class XMLHandler extends DataHandler {

	private void processOSM(Element root) {

        NodeList nodes = root.getElementsByTagName("node");
        
        for (int i =0; i< nodes.getLength(); i++) {
        	Node node = nodes.item(i);
        	NamedNodeMap att = node.getAttributes();
        	NodeList tags = node.getChildNodes();
        	for(int j=0;j<tags.getLength();j++) {
        		Node tag = tags.item(j);
        		if(tag.getNodeType()!=Node.TEXT_NODE) {
	        		String key = tag.getAttributes().getNamedItem("k").getNodeValue();
	        		if (key.equals("name")) {
	        			
	        			String name = tag.getAttributes().getNamedItem("v").getNodeValue();
	                	double lat = Double.valueOf(att.getNamedItem("lat").getNodeValue());
	                	double lon = Double.valueOf(att.getNamedItem("lon").getNodeValue());
	        			
	                	Log.d(MixView.TAG,"OSM Node: "+name+" lat "+lat+" lon "+lon+"\n");

	                	if(markers.size()<MAX_OBJECTS)
	                		createMarker(name, lat, lon, 0, "http://www.openstreetmap.org/?node="+att.getNamedItem("id").getNodeValue());
	        			//skip to next node
	        			continue;
	        		}
        		}
        	}
        }
	}
	
	public static String getOSMBoundingBox(double lat, double lon, double radius) {
		String bbox = "[bbox=";
		PhysicalPlace lb = new PhysicalPlace(); // left bottom
		PhysicalPlace rt = new PhysicalPlace(); // right top
		PhysicalPlace.calcDestination(lat, lon, 225, radius*1414, lb); // 1414: sqrt(2)*1000
		PhysicalPlace.calcDestination(lat, lon, 45, radius*1414, rt);
		bbox+=lb.getLongitude()+","+lb.getLatitude()+","+rt.getLongitude()+","+rt.getLatitude()+"]";
		return bbox;

		//return "[bbox=16.365,48.193,16.374,48.199]";
	}
	
	public void load(Document doc) {
        Element root = doc.getDocumentElement();
        
        // If the root tag is called "osm" we got an 
        // openstreetmap .osm xml document
        if(root.getTagName().equals("osm"))
        	processOSM(root);
     
	}
}
