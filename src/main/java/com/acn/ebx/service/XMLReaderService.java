package com.acn.ebx.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLReaderService {

	public HashMap<String, String> getMapFromXML(String outputFilePath, String keyElement, String valueElement) {

		HashMap<String, String> outputFileMap = new HashMap<String, String>();
		try {

			if (outputFilePath != null && !outputFilePath.isEmpty() && keyElement != null && !keyElement.isEmpty()
					&& valueElement != null && !keyElement.isEmpty()) {

				// Get Document Builder
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();

				// Build Document
				Document document = builder.parse(new File(outputFilePath));

				// Normalize the XML Structure; It's just too important !!
				document.getDocumentElement().normalize();

				// Here comes the root node
				Element root = document.getDocumentElement();
				System.out.println(root.getNodeName());

				// Get all employees
				NodeList nList = document.getElementsByTagName(keyElement);
				NodeList nList1 = document.getElementsByTagName(valueElement);

				outputFileMap = visitChildNodes(nList, nList1);

			}

		} catch (ParserConfigurationException pe) {

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return outputFileMap;

	}

	// This function is called recursively
	private static HashMap<String, String> visitChildNodes(NodeList nList, NodeList nList1) {

		HashMap<String, String> outputFileMap = new HashMap<String, String>();

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node node = nList.item(temp);
			Node node1 = nList1.item(temp);
			outputFileMap.put(node.getTextContent(), node1.getTextContent());
		}

		return outputFileMap;
	}

}
