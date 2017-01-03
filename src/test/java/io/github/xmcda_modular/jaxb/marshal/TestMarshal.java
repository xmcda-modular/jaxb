package io.github.xmcda_modular.jaxb.marshal;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.github.xmcda_modular.y2016.jaxb.Alternative;
import io.github.xmcda_modular.y2016.jaxb.Criterion;
import io.github.xmcda_modular.y2016.jaxb.DirectedCriterion;
import io.github.xmcda_modular.y2016.jaxb.ObjectFactory;

public class TestMarshal {

	public String readStream(InputStream stream) throws IOException {
		final ByteArrayOutputStream result = new ByteArrayOutputStream();
		final byte[] buffer = new byte[1024];
		int length;
		while ((length = stream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString("UTF-8");

	}

	@Test
	public void testMarshal() throws JAXBException, ParserConfigurationException, TransformerException, IOException {
		final JAXBContext jc = JAXBContext.newInstance(Alternative.class, Criterion.class);
		final Marshaller marshaller = jc.createMarshaller();
		final ObjectFactory f = new ObjectFactory();

		final XmlSchema annotation = ObjectFactory.class.getPackage().getAnnotation(XmlSchema.class);
		final String namespace = annotation.namespace();

		final Alternative alt = f.createAlternative();
		alt.setId("a01");

		final QName altQName = new QName(namespace, "myAlt", "xm");
		final JAXBElement<Alternative> altEl = new JAXBElement<>(altQName, Alternative.class, alt);

		final DirectedCriterion crit = f.createDirectedCriterion();
		crit.setId("c01");
		crit.setPreferenceDirection("max");

		final QName critQName = new QName(namespace, "myCrit", "xm");
		final JAXBElement<Criterion> critEl = new JAXBElement<>(critQName, Criterion.class, crit);

		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		final Document doc = docBuilder.newDocument();
		docFactory.setNamespaceAware(true);
		final Element rootElement = doc.createElementNS("myNS", "m:AltAndCrit");
		rootElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xm", namespace);
		doc.appendChild(rootElement);

		marshaller.marshal(altEl, rootElement);
		marshaller.marshal(critEl, rootElement);

		final TransformerFactory transformerFactory = TransformerFactory.newInstance();
		final Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		/** Inelegant. (Impl. dependent.) */
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		final DOMSource source = new DOMSource(doc);
		// final StreamResult result = new StreamResult(new File("file.xml"));
		StringWriter writer = new StringWriter();
		final StreamResult resultStream = new StreamResult(writer);
		transformer.transform(source, resultStream);
		String result = writer.toString();

		// System.out.println(result);
		final String expected;
		try (final InputStream expectedStream = TestMarshal.class.getResourceAsStream("AltAndCrit.xml")) {
			expected = readStream(expectedStream);
		}
		assertEquals(expected, result);
	}
}
