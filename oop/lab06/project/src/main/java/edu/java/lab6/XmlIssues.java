package edu.java.lab6;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Сохранение и загрузка списка выдач в XML-документе (лабораторная работа
 * № 6). Документ строится и разбирается через объектную модель DOM:
 *
 * <pre>
 * &lt;issues&gt;
 *   &lt;issue&gt;
 *     &lt;reader&gt;...&lt;/reader&gt; &lt;book&gt;...&lt;/book&gt;
 *     &lt;issued&gt;...&lt;/issued&gt; &lt;due&gt;...&lt;/due&gt; &lt;returned&gt;...&lt;/returned&gt;
 *   &lt;/issue&gt;
 * &lt;/issues&gt;
 * </pre>
 *
 * @author Рябов И.А., группа 5315
 * @version 6.0
 */
public final class XmlIssues {

	/** Имена дочерних элементов записи в порядке столбцов таблицы. */
	private static final String[] FIELDS = { "reader", "book", "issued", "due",
			"returned" };

	/** Служебный класс: экземпляры не создаются. */
	private XmlIssues() {
	}

	/**
	 * Строит DOM-документ и записывает его в XML-файл.
	 *
	 * @param fileName полное имя файла
	 * @param issues   записи, которые нужно сохранить
	 * @throws Exception если документ не удалось построить или записать
	 */
	public static void save(String fileName, List<String[]> issues)
			throws Exception {
		Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().newDocument();
		Element root = doc.createElement("issues");
		doc.appendChild(root);
		for (String[] row : issues) {
			Element item = doc.createElement("issue");
			for (int i = 0; i < FIELDS.length; i++) {
				Element field = doc.createElement(FIELDS[i]);
				field.setTextContent(row[i]);
				item.appendChild(field);
			}
			root.appendChild(item);
		}
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "2");
		File file = new File(fileName);
		File parent = file.getAbsoluteFile().getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		transformer.transform(new DOMSource(doc), new StreamResult(file));
	}

	/**
	 * Читает записи из XML-файла.
	 *
	 * @param fileName полное имя файла
	 * @return прочитанные записи
	 * @throws Exception если файл не найден или имеет неверную структуру
	 */
	public static List<String[]> load(String fileName) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// Чужой XML-файл не должен читать файлы диска через DTD
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",
				true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new File(fileName));
		NodeList items = doc.getDocumentElement().getElementsByTagName("issue");
		List<String[]> result = new ArrayList<String[]>();
		for (int i = 0; i < items.getLength(); i++) {
			Node node = items.item(i);
			if (node instanceof Element) {
				Element item = (Element) node;
				String[] row = new String[FIELDS.length];
				for (int j = 0; j < FIELDS.length; j++) {
					NodeList list = item.getElementsByTagName(FIELDS[j]);
					row[j] = list.getLength() == 0 ? ""
							: list.item(0).getTextContent().trim();
				}
				result.add(row);
			}
		}
		return result;
	}
}
