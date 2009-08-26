/*
 * Configuration.java
 */

package cz.kebrt.html2latex;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Program configuration. All of the configuration from the XML configuration
 * file is stored in this class.
 */
class Configuration {

	/**
	 * Mapping between HTML elements and LaTeX commands. <br />
	 * key : &lt;elementName&gt;
	 */
	private HashMap<String, ElementConfigItem> _elements;

	/**
	 * Mapping between HTML named entities (ie. &amp;lt;) and LaTeX commands.
	 * <br />
	 * key : &lt;entityName&gt; <br />
	 * value : LaTeX command
	 */
	private HashMap<String, String> _chars;

	/**
	 * Mapping between HTML decimal decimal entities and LaTeX commands. <br />
	 * key : &lt;entityDecimalNum&gt; <br />
	 * value : LaTeX command
	 */
	private HashMap<Integer, String> _charsNum;

	/**
	 * CSS styles used with converted file. <br />
	 * key : &lt;styleName&gt;
	 */
	private HashMap<String, CSSStyle> _styles;

	/**
	 * Mappings between CSS properties and LaTeX commands.<br />
	 * key : &lt;propertyName&gt;-&lt;propertyValue&gt;
	 */
	private HashMap<String, CSSPropertyConfigItem> _stylesConf;

	/** The way of converting hyperlinks. */
	private LinksConversion _linksConversion;
	/** Make new LaTeX commands from the CSS styles. */
	private boolean _makeCmdsFromCSS = false;
	/** Prefix of new LaTeX commands generated by the program. */
	private String _commandsPrefix = "";

	/**
	 * Loads all the configuration.
	 *
	 * @throws FatalErrorException
	 *             when error during processing configuration occurs
	 */
	public Configuration() throws FatalErrorException {
		_elements = new HashMap<String, ElementConfigItem>(100);
		_chars = new HashMap<String, String>(250);
		_charsNum = new HashMap<Integer, String>(250);
		_styles = new HashMap<String, CSSStyle>(20);
		_stylesConf = new HashMap<String, CSSPropertyConfigItem>(50);

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(HTML2LaTeX
					.getConfigFile()));
			Element root = document.getDocumentElement();
			loadElementsConfiguration(root);
			loadLinksConfiguration(root);
			loadCharsConfiguration(root);
			loadStylesConfiguration(root);
			if (!HTML2LaTeX.getCSSFile().equals(""))
				loadStyleSheet(new File(HTML2LaTeX.getCSSFile()));
		} catch (Exception e) {
			throw new FatalErrorException("Can't load configuration.\n"
					+ e.getMessage());
		}
	}

	/**
	 * Adds user style to the configuration.
	 *
	 * @param name
	 *            style name
	 * @param style
	 *            CSS style
	 */
	public void addStyle(String name, CSSStyle style) {
		_styles.put(name, style);
	}

	/**
	 * Finds style for element.
	 *
	 * @param elementName
	 *            element name
	 * @return CSS style
	 */
	public CSSStyle findStyle(String elementName) {
		CSSStyle style;
		if ((style = _styles.get(elementName)) != null)
			return style;
		else
			return null;
	}

	/**
	 * Finds style for element with specified <code>class</code> attribute
	 *
	 * @param className
	 *            element's <code>class</code> attribute
	 * @param elementName
	 *            element name
	 * @return CSS style
	 */
	public CSSStyle findStyleClass(String className, String elementName) {
		CSSStyle style;
		if ((style = _styles.get(elementName + "." + className)) != null)
			return style;
		else if ((style = _styles.get("." + className)) != null)
			return style;
		else
			return null;
	}

	/**
	 * Finds style for element with specified <code>id</code> attribute
	 *
	 * @param elementId
	 *            element's <code>id</code> attribute
	 * @param elementName
	 *            element name
	 * @return CSS style
	 */
	public CSSStyle findStyleId(String elementId, String elementName) {
		CSSStyle style;
		if ((style = _styles.get(elementName + "#" + elementId)) != null)
			return style;
		else if ((style = _styles.get("#" + elementId)) != null)
			return style;
		else
			return null;
	}

	/**
	 * Returns LaTeX command for the specified entity.
	 *
	 * @param charNum
	 *            entity number
	 * @return LaTeX command for the specified entity
	 * @throws NoItemException
	 *             when entity isn't found in the configuration
	 */
	public String getChar(Integer charNum) throws NoItemException {
		String convertTo;
		if ((convertTo = _charsNum.get(charNum)) != null)
			return convertTo;

		throw new NoItemException(charNum.toString());
	}

	/**
	 * Returns LaTeX command for the specified entity.
	 *
	 * @param charName
	 *            entity name
	 * @return LaTeX command for the specified entity
	 * @throws NoItemException
	 *             when entity isn't found in the configuration
	 */
	public String getChar(String charName) throws NoItemException {
		String convertTo;
		if ((convertTo = _chars.get(charName)) != null)
			return convertTo;

		throw new NoItemException(charName);
	}

	/**
	 * style name without special chars
	 *
	 * @param styleName
	 *            style name
	 * @return style name without special chars (ie. &quot;#&quot;) - suitable
	 *         fo creating new LaTeX command
	 */
	public String getCmdStyleName(String styleName) {
		// TODO: lepsi
		return "\\"
				+ _commandsPrefix
				+ styleName.replaceAll("\\W", "").replaceAll("\\d", "")
						.replace("_", "");
	}

	/**
	 * Returns element's configuration.
	 *
	 * @param name
	 *            element's name
	 * @return element's configuration
	 * @throws NoItemException
	 *             when element isn't found in the configuration
	 */
	public ElementConfigItem getElement(String name) throws NoItemException {
		ElementConfigItem item;
		if ((item = _elements.get(name)) != null)
			return item;

		throw new NoItemException(name);
	}

	/**
	 * Returns the way of converting hyperlinks.
	 *
	 * @return the way of converting hyperlinks
	 */
	public LinksConversion getLinksConversionType() {
		return _linksConversion;
	}

	/**
	 * Returns true when new LaTeX commands are to be made from CSS styles.
	 *
	 * @return true when new LaTeX commands are to be made from CSS styles
	 */
	public boolean getMakeCmdsFromCSS() {
		return _makeCmdsFromCSS;
	}

	/**
	 * Returns CSS property configuration.
	 *
	 * @param property
	 *            property and value name
	 *            (&lt;propertyName&gt;-&lt;valueName&gt;)
	 * @return CSS property configuration
	 * @throws NoItemException
	 *             when property isn't found in the configuration
	 */
	public CSSPropertyConfigItem getPropertyConf(String property)
			throws NoItemException {
		CSSPropertyConfigItem ret;
		if ((ret = _stylesConf.get(property)) != null)
			return ret;

		throw new NoItemException(property);
	}

	/**
	 * Returns style defined in the user stylesheet.
	 *
	 * @param styleName
	 *            style name
	 * @return style defined in the user stylesheet
	 */
	public CSSStyle getStyle(String styleName) {
		return _styles.get(styleName);
	}

	/**
	 * Makes new LaTeX commands from the CSS styles.
	 *
	 * @return string containing new commands definitions
	 */
	public String makeCmdsFromCSS() {
		String ret = "\n"; // % commands generated by html2latex
		for (Iterator<Map.Entry<String, CSSStyle>> iterator = _styles.entrySet().iterator(); iterator
				.hasNext();) {
			Map.Entry<String, CSSStyle> entry = iterator.next();
			String styleName = entry.getKey();
			CSSStyle style = entry.getValue();
			ret += "\n\\newcommand{" + getCmdStyleName(styleName) + "}[1]{ "
					+ style.getStart() + "#1" + style.getEnd() + " }";
		}
		return ret + "\n";
	}

	/**
	 * Loads mapping between HTML named entities (ie. &amp;lt;) and LaTeX
	 * commands. Mappings between HTML decimal decimal entities and LaTeX
	 * commands are also loaded.
	 *
	 * @param root
	 *            root element of the XML configuration file
	 */
	private void loadCharsConfiguration(Element root) {
		// get chars conversion configuration
		NodeList nl = root.getElementsByTagName("char");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			String charName = e.getAttribute("name");
			String convertTo = e.getAttribute("convertTo");
			String charNum = e.getAttribute("num");
			try {
				if (!charNum.equals("")) {
					Integer charNumInt = Integer.valueOf(charNum);
					_charsNum.put(charNumInt, replaceSpecialStrings(convertTo));
				}
			} catch (NumberFormatException ex) {
				System.err.println("Error in configuration.\n" + ex.toString());
			}

			_chars.put(charName, replaceSpecialStrings(convertTo));
		}

		// get chars conversion configuration
		nl = root.getElementsByTagName("charNum");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			String convertTo = e.getAttribute("convertTo");
			String charNum = e.getAttribute("num");
			try {
				if (!charNum.equals("")) {
					Integer charNumInt = Integer.valueOf(charNum);
					_charsNum.put(charNumInt, replaceSpecialStrings(convertTo));
				}
			} catch (NumberFormatException ex) {
				System.err.println("Error in configuration.\n" + ex.toString());
			}
		}
	}

	/**
	 * Loads mapping between HTML elements and LaTeX commands.
	 *
	 * @param root
	 *            root element of the XML configuration file
	 */
	private void loadElementsConfiguration(Element root) {
		// get elements' configuration
		NodeList nl = root.getElementsByTagName("element");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			String elementName = e.getAttribute("name");
			String leaveText = e.getAttribute("leaveText");
			String ignoreContent = e.getAttribute("ignoreContent");
			String ignoreStyles = e.getAttribute("ignoreStyle");

			NodeList nl2 = e.getElementsByTagName("start");
			String elementStart = nl2.item(0).getTextContent();
			nl2 = e.getElementsByTagName("end");
			String elementEnd = nl2.item(0).getTextContent();

			_elements.put(elementName, new ElementConfigItem(
					replaceSpecialStrings(elementStart),
					replaceSpecialStrings(elementEnd), leaveText,
					ignoreContent, ignoreStyles));
		}
	}

	/**
	 * Loads {@link LinksConversion options} for converting hyperlinks.
	 *
	 * @param root
	 *            root element of the XML configuration file
	 */
	private void loadLinksConfiguration(Element root) {
		// get links conversion configuration
		NodeList nl = root.getElementsByTagName("links");
		Element links = (Element) nl.item(0);
		String type = links.getAttribute("conversion");

		if (type.equals("footnotes"))
			_linksConversion = LinksConversion.FOOTNOTES;
		else if (type.equals("biblio"))
			_linksConversion = LinksConversion.BIBLIO;
		else if (type.equals("hypertex"))
			_linksConversion = LinksConversion.HYPERTEX;
		else
			_linksConversion = LinksConversion.IGNORE;
	}

	/**
	 * Loads mappings between CSS properties and LaTeX commands.
	 *
	 * @param root
	 *            root element of the XML configuration file
	 */
	private void loadStylesConfiguration(Element root) {
		NodeList nl = root.getElementsByTagName("cssStyles");
		if (nl.getLength() == 1) {
			Element elem = (Element) nl.item(0);
			String makeCmds = elem.getAttribute("makeCommands");
			_commandsPrefix = elem.getAttribute("commandsPrefix");
			if (makeCmds.equals("yes"))
				_makeCmdsFromCSS = true;
		}
		nl = root.getElementsByTagName("property");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			String propertyName = e.getAttribute("name");

			NodeList nl2 = e.getElementsByTagName("value");
			for (int j = 0; j < nl2.getLength(); j++) {
				Element el = (Element) nl2.item(j);
				String propertyValue = el.getAttribute("name");
				String start = el.getAttribute("start");
				String end = el.getAttribute("end");

				_stylesConf.put(propertyName + "-" + propertyValue,
						new CSSPropertyConfigItem(replaceSpecialStrings(start),
								replaceSpecialStrings(end)));
			}
		}
	}

	/**
	 * Loads user style sheet.
	 *
	 * @param f
	 *            CSS file
	 */
	private void loadStyleSheet(File f) {
		CSSParser parser = new CSSParser();
		parser.parse(f, new CSSParserHandler(this));
		for (Iterator<Map.Entry<String, CSSStyle>> iterator = _styles
				.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, CSSStyle> entry = iterator.next();
			CSSStyle style = entry.getValue();
			style.makeLaTeXCommands(this);
		}
	}

	/**
	 * Replaces special
	 *
	 * @-strings with appropriate strings (ie. &quot;@NL&quot; with
	 *           &quot;\n&quot;).
	 * @return string without special
	 * @-strings
	 * @param str
	 *            input string
	 */
	private String replaceSpecialStrings(String str) {
		str = str.replace("@NL", "\n").replace("@TAB", "\t").replace("@QUOT",
				"'").replace("@DOUBLEQUOT", "\"").replace("@AMP", "&").replace(
				"@LT", "<").replace("@GT", ">");

		return str;
	}

}

/**
 * Class representing configuration of each CSS property defined in the
 * configuration file.
 */
class CSSPropertyConfigItem {

	/**
	 * Mapping between the CSS property and LaTeX (start command). Example:
	 * <code>\texttt{<code> for &quot;monospace&quot; value
	 *  of &quot;font-family&quot; property.
	 */
	private String _start;
	/** Mapping between the CSS property and LaTeX (end command). */
	private String _end;

	/**
	 * Cstr.
	 *
	 * @param start
	 *            start command
	 * @param end
	 *            end command
	 */
	public CSSPropertyConfigItem(String start, String end) {
		_start = start;
		_end = end;
	}

	/*
	 * @return end command
	 */
	/**
	 * Returns end command.
	 *
	 * @return end command
	 */
	public String getEnd() {
		return _end;
	}

	/**
	 * Returns start command.
	 *
	 * @return start command
	 */
	public String getStart() {
		return _start;
	}
}

/**
 * Class representing configuration of each HTML element.
 */
class ElementConfigItem {
	/**
	 * Mapping between start tag and LaTeX command. (ie. &quot;\textbf{&quot;
	 * for the <code>b</code> tag).
	 */
	private String _start;
	/**
	 * Mapping between end tag and LaTeX command. (ie. &quot;}&quot; for the
	 * <code>b</code> tag).
	 */
	private String _end;
	/** The element's content mustn't be touched. (ie. for <code>pre</code>) */
	private boolean _leaveText = false;
	/** The element's content will be ignored. (ie. for <code>script</code>) */
	private boolean _ignoreContent = false;
	/** The element's CSS style will be ignored. */
	private boolean _ignoreStyles = false;

	/**
	 * Cstr.
	 *
	 * @param start
	 *            mapping between start tag and LaTeX command
	 * @param end
	 *            mapping between end tag and LaTeX command
	 * @param leaveText
	 *            &quot;yes&quot; if the element's content mustn't be touched
	 * @param ignoreContent
	 *            &quot;yes&quot; if the element's content will be ignored
	 * @param ignoreStyles
	 *            &quot;yes&quot; if the element's CSS styles will be ignored
	 */
	public ElementConfigItem(String start, String end, String leaveText,
			String ignoreContent, String ignoreStyles) {
		_start = start;
		_end = end;
		if (leaveText.equals("yes")) {
			_leaveText = true;
		}
		if (ignoreContent.equals("yes")) {
			_ignoreContent = true;
		}
		if (ignoreStyles.equals("yes")) {
			_ignoreStyles = true;
		}
	}

	/**
	 * Returns mapping between end tag and LaTeX command.
	 *
	 * @return mapping between end tag and LaTeX command
	 */
	public String getEnd() {
		return _end;
	}

	/**
	 * Returns mapping between start tag and LaTeX command.
	 *
	 * @return mapping between start tag and LaTeX command
	 */
	public String getStart() {
		return _start;
	}

	/**
	 * Returns ignoreContent property.
	 *
	 * @return true if the element's content is ignored
	 */
	public boolean ignoreContent() {
		return _ignoreContent;
	}

	/**
	 * Returns ignoreStyles property.
	 *
	 * @return true if the element's CSS styles are ignored
	 */
	public boolean ignoreStyles() {
		return _ignoreStyles;
	}

	/**
	 * Returns leaveText property.
	 *
	 * @return true if the element's content mustn't be touched
	 */
	public boolean leaveText() {
		return _leaveText;
	}
}

/**
 * The way of converting hyperlinks.
 */
enum LinksConversion {
	/** Hyperlinks will be converted into the footnotes containing the link. */
	FOOTNOTES,
	/** Hyperlinks will be converted into the bibliography items. */
	BIBLIO,
	/**
	 * Hyperlinks will be converted using the hyperref package which is built on
	 * hypertex package. Hyperlinks starting with &quot;#&quot; are also
	 * included.
	 */
	HYPERTEX,
	/** Hyperlinks are ignored. */
	IGNORE
}
