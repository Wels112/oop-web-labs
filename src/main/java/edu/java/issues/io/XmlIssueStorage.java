package edu.java.issues.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.java.issues.exception.StorageException;
import edu.java.issues.model.Issue;

/**
 * Сохранение и загрузка списка выдач из XML-документа (ООП, лабораторная
 * работа № 6). Документ строится и разбирается через объектную модель DOM.
 * Структура файла:
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
 * @version 2.0
 */
public class XmlIssueStorage {

	/** Журнал работы с файлами (лабораторная работа № 10). */
	private static final Logger log = LogManager
			.getLogger(XmlIssueStorage.class);

	/** Имена дочерних элементов записи в порядке столбцов таблицы. */
	private static final String[] FIELDS = { "reader", "book", "issued", "due",
			"returned" };

	/**
	 * Строит DOM-документ и записывает его в XML-файл.
	 *
	 * @param fileName полное имя файла
	 * @param issues   записи, которые нужно сохранить
	 * @throws StorageException если документ не удалось построить или записать
	 */
	public void save(String fileName, List<Issue> issues)
			throws StorageException {
		try {
			DocumentBuilder builder = newBuilder();
			Document doc = builder.newDocument();
			Element root = doc.createElement("issues");
			doc.appendChild(root);
			for (Issue issue : issues) {
				Element item = doc.createElement("issue");
				String[] row = issue.toRow();
				for (int i = 0; i < FIELDS.length; i++) {
					Element field = doc.createElement(FIELDS[i]);
					field.setTextContent(row[i]);
					item.appendChild(field);
				}
				root.appendChild(item);
			}
			TransformerFactory factory = TransformerFactory.newInstance();
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			Transformer transformer = factory.newTransformer();
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
			log.info("Записан XML-файл {}: {} записей", fileName, issues.size());
		} catch (ParserConfigurationException | TransformerException e) {
			log.error("Ошибка записи XML-файла {}", fileName, e);
			throw new StorageException("Ошибка записи XML: " + e.getMessage(),
					e);
		}
	}

	/**
	 * Читает записи из XML-файла.
	 *
	 * @param fileName полное имя файла
	 * @return прочитанные записи
	 * @throws StorageException если файл не найден или имеет неверную
	 *                          структуру
	 */
	public List<Issue> load(String fileName) throws StorageException {
		File file = new File(fileName);
		if (!file.isFile()) {
			log.error("XML-файл не найден: {}", fileName);
			throw new StorageException("Файл не найден: " + fileName, null);
		}
		try {
			Document doc = newBuilder().parse(file);
			NodeList items = doc.getDocumentElement()
					.getElementsByTagName("issue");
			List<Issue> result = new ArrayList<>();
			for (int i = 0; i < items.getLength(); i++) {
				Node node = items.item(i);
				if (node instanceof Element) {
					Element item = (Element) node;
					result.add(new Issue(text(item, "reader"),
							text(item, "book"), text(item, "issued"),
							text(item, "due"), text(item, "returned")));
				}
			}
			log.info("Прочитан XML-файл {}: {} записей", fileName,
					result.size());
			return result;
		} catch (SAXException | IOException | ParserConfigurationException e) {
			log.error("Ошибка разбора XML-файла {}", fileName, e);
			throw new StorageException("Ошибка чтения XML: " + e.getMessage(),
					e);
		}
	}

	/**
	 * Создаёт безопасный построитель DOM-документов: внешние сущности
	 * и DTD отключены, чтобы чужой XML-файл не мог читать файлы диска.
	 *
	 * @return построитель документов
	 * @throws ParserConfigurationException если параметры не поддерживаются
	 */
	private DocumentBuilder newBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",
				true);
		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);
		return factory.newDocumentBuilder();
	}

	/**
	 * Возвращает текст дочернего элемента.
	 *
	 * @param parent элемент записи
	 * @param name   имя дочернего элемента
	 * @return текст элемента или пустая строка, если элемента нет
	 */
	private String text(Element parent, String name) {
		NodeList list = parent.getElementsByTagName(name);
		return list.getLength() == 0 ? "" : list.item(0).getTextContent();
	}
}
