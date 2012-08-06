/*
 * Copyright (C) 2012- Peer internet solutions & Finalist IT Group
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.mixare.R;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

/**
 * Singleton class that manages the storage of DataSources. You can add, edit or
 * delete a DataSource through this class.
 */
public class DataSourceStorage {
	private SharedPreferences settings;
	private static Context ctx;
	public static DataSourceStorage instance;
	private static String xmlPreferencesKey = "xmlDataSources";

	/**
	 * Private constructor to ensure that only one instance can be created
	 * 
	 * @param ctx
	 *            The context to be able to access the SharedPreferences and the
	 *            Resources
	 */
	private DataSourceStorage(Context ctx) {
		DataSourceStorage.ctx = ctx;
		settings = ctx.getSharedPreferences(DataSourceList.SHARED_PREFS, 0);
	}

	/**
	 * Creates a new instance of a DataSourceSotrage
	 * 
	 * @param ctx
	 *            The context to create the DataSourceSotrage
	 */
	public static void init(Context ctx) {
		instance = new DataSourceStorage(ctx);
	}

	/**
	 * @return Returns the instance of a DataSourceStorage or null if no
	 *         instance was created yet
	 * @throws Exception
	 *             Throws Exception if instance is null
	 */
	public static DataSourceStorage getInstance() {
		if (instance == null) {
			if (ctx != null) {
				init(ctx);
			} else {
				Log.d("DataSourceStorage", "instance and ctx are null");
			}
		}
		return instance;
	}

	/**
	 * Returns the instance of a DataSourceStorage and creates an instance if no
	 * instance was created yet.
	 * 
	 * @param ctx
	 *            The context to create a new instance if no instance was
	 *            created yet
	 * @return The instance of a DataSourceStorage
	 */
	public static DataSourceStorage getInstance(Context ctx) {
		if (instance == null) {
			instance = new DataSourceStorage(ctx);
		}
		return instance;
	}

	/**
	 * Creates a XML Element in this form:
	 * 
	 * <pre>
	 * <datasource id="0">
	 * 	<name></name>
	 * 	<url></url>
	 * 	<type></type>
	 * 	<display></display>
	 * 	<visible></visible>
	 * </datasource>
	 * </pre>
	 * 
	 * @param doc
	 *            The XML Document to create the Element
	 * @param id
	 *            The id of the DataSource
	 * @param name
	 *            The name of the DataSource
	 * @param url
	 *            The Url of the DataSource
	 * @param type
	 *            The Type of the DataSource
	 * @param display
	 *            The Displaytype of the DataSource
	 * @param visible
	 *            The Visibility of the DataSource
	 * @return The XML Element of the DataSource
	 */
	private Element createDataSourceElement(Document doc, String id,
			String name, String url, String type, String display,
			boolean visible, boolean editable) {
		// Set rootElement to "DataSource"
		Element rootElement = doc.createElement("datasource");

		// add attribute id to rootElement
		rootElement.setAttribute("id", id);

		// create "Name" Element and add it to rootElement
		Element nameElement = doc.createElement("name");
		nameElement.appendChild(doc.createTextNode(name));
		rootElement.appendChild(nameElement);

		// create "Url" Element and add it to rootElement
		Element urlElement = doc.createElement("url");
		urlElement.appendChild(doc.createTextNode(url));
		rootElement.appendChild(urlElement);

		// create "Type" Element and add it to rootElement
		Element typeElement = doc.createElement("type");
		typeElement.appendChild(doc.createTextNode(type));
		rootElement.appendChild(typeElement);

		// create "Display" Element and add it to rootElement
		Element displayElement = doc.createElement("display");
		displayElement.appendChild(doc.createTextNode(display));
		rootElement.appendChild(displayElement);

		// create "enabled" Element and add it to rootElement
		Element enabled = doc.createElement("visible");
		enabled.appendChild(doc.createTextNode(String.valueOf(visible)));
		rootElement.appendChild(enabled);

		// create "enabled" Element and add it to rootElement
		Element editableElement = doc.createElement("editable");
		editableElement
				.appendChild(doc.createTextNode(String.valueOf(editable)));
		rootElement.appendChild(editableElement);

		return rootElement;
	}

	/**
	 * Saves a DataSource to the SharedPreferences
	 * 
	 * @param dataSource
	 *            The DataSource to save
	 */
	public void add(DataSource dataSource) {
		// Get XML from SharedPreferences
		String xml = settings
				.getString(DataSourceStorage.xmlPreferencesKey, "");
		try {
			// Create a Document out of the XML String or a new one
			Document doc = convertToXmlDocument(xml);
			if (doc == null) {
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder documentBuilder = documentBuilderFactory
						.newDocumentBuilder();
				doc = documentBuilder.newDocument();
				doc.appendChild(doc.createElement("datasources"));
			}

			Element documentRoot = doc.getDocumentElement();

			// Check if this DataSource has already been added
			NodeList nList = documentRoot.getElementsByTagName("datasource");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (Integer.valueOf(eElement.getAttribute("id")) == dataSource
							.getDataSourceId()) {
						return;
					}
				}
			}

			// Create the XML Element for the new DataSource
			Element dataSourceElement = createDataSourceElement(doc,
					String.valueOf(dataSource.getDataSourceId()),
					dataSource.getName(), dataSource.getUrl(),
					String.valueOf(dataSource.getTypeId()),
					String.valueOf(dataSource.getDisplayId()),
					dataSource.getEnabled(), dataSource.isEditable());

			// Append
			documentRoot.appendChild(dataSourceElement);

			// Convert Document to String
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			xml = writer.getBuffer().toString().replaceAll("\n|\r", "");

			// Save it to the SharedPreferences
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(xmlPreferencesKey, xml);
			editor.commit();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes the saved DataSources from the SharedPreferences
	 */
	public void clear() {
		SharedPreferences.Editor dataSourceEditor = settings.edit();
		dataSourceEditor.clear();
		dataSourceEditor.commit();
	}

	/**
	 * Saves the default DataSources from the Resources to the SharedPreferences
	 */
	public void fillDefaultDataSources() {
		String defaultXml = inputStreamToString(ctx.getResources()
				.openRawResource(R.raw.defaultdatasources));

		SharedPreferences.Editor editor = settings.edit();
		editor.putString(DataSourceStorage.xmlPreferencesKey, defaultXml);
		editor.commit();
	}

	/**
	 * Converts a InputStream to a String
	 * 
	 * @param iS
	 *            The InputStream to convert
	 * @return The String that the InputStream was containing
	 */
	public String inputStreamToString(InputStream iS) {
		try {
			// create a buffer that has the same size as the InputStream
			byte[] buffer = new byte[iS.available()];
			// read the text file as a stream, into the buffer
			iS.read(buffer);
			// create a output stream to write the buffer into
			ByteArrayOutputStream oS = new ByteArrayOutputStream();
			// write this buffer to the output stream
			oS.write(buffer);
			// Close the Input and Output streams
			oS.close();
			iS.close();

			// return the output stream as a String
			return oS.toString();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Retrieves a DataSource out of the saved Xml
	 * 
	 * @param id
	 *            The id of the DataSource to return
	 * @return The DataSource at the specified index
	 */
	public DataSource getDataSource(int id) {
		try {
			Log.d("DataSourceStorage", "getDataSource: " + id + ", getSize(): "
					+ getSize());

			String defaultXml = null;
			try {
				defaultXml = inputStreamToString(ctx.getResources()
						.openRawResource(R.raw.defaultdatasources));
			} catch (NotFoundException nfe) {
				Log.d("DataSourceStorage", "getDataSource: " + id
						+ " openRawResource Failed");
			}

			String xml = null;
			try {
				xml = settings.getString(xmlPreferencesKey, defaultXml);
			} catch (ClassCastException e) {
				Log.d("DataSourceStorage", "getDataSource: " + id
						+ " getString Failed");
			}

			Document doc = convertToXmlDocument(xml);

			Log.d("DataSourceStorage", "xml: " + xml);
			NodeList nList = null;
			try {
				nList = doc.getElementsByTagName("datasource");
			} catch (Exception e) {
				Log.d("DataSourceStorage", "getDataSource: " + id
						+ " getString Failed");
			}

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (Integer.valueOf(eElement.getAttribute("id")) == id) {
						return new DataSource(Integer.valueOf(eElement
								.getAttribute("id")), getTagValue("name",
								eElement), getTagValue("url", eElement),
								getTagValue("type", eElement), getTagValue(
										"display", eElement), getTagValue(
										"visible", eElement),
								Boolean.parseBoolean(getTagValue("editable",
										eElement)));
					}
				}
			}
		} catch (Exception e) {
			Log.d("DataSourceStorage", "getDataSource: " + id + " for Failed");
		}
		return null;
	}

	/**
	 * Retrievs a Value of a Tag out of an Xml Element
	 * 
	 * @param sTag
	 *            The Tag to look for
	 * @param element
	 *            The Element in which the Tag should be looked for
	 * @return The value of the Tag
	 */
	private static String getTagValue(String sTag, Element element) {
		NodeList nlList = element.getElementsByTagName(sTag).item(0)
				.getChildNodes();
		Node nValue = (Node) nlList.item(0);
		return nValue.getNodeValue();
	}

	/**
	 * Converts a String to a Xml Document
	 * 
	 * @param rawData
	 *            The String to convert
	 * @return The Xml Document or null if an error occurred
	 */
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

	/**
	 * @return How many DataSources are added
	 */
	public int getSize() {
		try {
			String xml = settings.getString(
					xmlPreferencesKey,
					inputStreamToString(ctx.getResources().openRawResource(
							R.raw.defaultdatasources)));

			Document doc = convertToXmlDocument(xml);
			return doc.getElementsByTagName("datasource").getLength();
		} catch (Exception e) {
			return 0;
		}
	}
}