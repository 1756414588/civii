package com.game.util;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @Description
 * @Date 2022/12/7 11:46
 **/

public class XmlUtil {

	public static Document getXmlDocument(String fileName) {
		try {
			InputStream in = XmlUtil.class.getClassLoader().getResourceAsStream(fileName);
			if (in == null) {
//				LogHelper.GAME_LOGGER.info("请查看文件名是否正确 fileName:{}", fileName);
				return null;
			}
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
			Document doc = db.parse(in);
			return doc;
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	public static NodeList getXmlNodeList(String fileName, String tagName) {
		Document document = getXmlDocument(fileName);
		if (document == null) {
			return null;
		}
		/* 1.获取所有的TagName为Pos的节点，并把他们放到集合nodeList中
		 * 2.NodeList是一个接口
		 */
		NodeList nodeList = document.getElementsByTagName(tagName);

		return nodeList;
	}

//	/**
//	 * 使用案例
//	 */
//	public void test() {
//		NodeList nodeList = getXmlNodeList("world.xml", "Pos");
//
//		for (int i = 0; i < nodeList.getLength(); i++) {
//
//			Node node = nodeList.item(i);//按照循环找出所有的节点
//			/*
//			 * 1.The Element interface represents an element in an HTML or XML
//			 * document. Elements may have attributes associated with them
//			 * */
//			Element elem = (Element) node;
//
//			/*1.获取属性值id
//			 * 2.获取属性值location
//			 * */
//			String x = elem.getAttribute("x");
//			String y = elem.getAttribute("y");
//
//			System.out.println("Pos: x=" + x + ",y=" + y);
//		}
//	}

}
