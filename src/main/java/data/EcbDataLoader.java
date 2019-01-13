package data;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EcbDataLoader implements IDataLoader {

    @Override
    public List<Doc> loadDataFromCorpusFolder(String ecbPath) {
        List<Doc> corpus = new ArrayList<>();
        Collection<File> files = FileUtils.listFiles(new File(ecbPath), new RegexFileFilter(".*xml$"),
                DirectoryFileFilter.DIRECTORY);

        for (File file : files) {
            corpus.add(buildDoc(file));
        }

        return corpus;
    }

    private Doc buildDoc(File file) {
        Doc retDoc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            boolean isEcbPlus = false;
            System.out.println("building doc from file-" + file.getName());
            if (file.getName().contains("ecbplus")) {
                isEcbPlus = true;
            }
            Document xmlDocument = builder.parse(file);
            Element root = xmlDocument.getDocumentElement();
            final String docId = root.getAttribute("doc_name");
            NodeList tokensNodes = root.getElementsByTagName("token");

            ArrayList<Token> tokens = new ArrayList<>();
            for (int i = 0; i < tokensNodes.getLength(); i++) {
                Node nNode = tokensNodes.item(i);
                Element tokElem = (Element) nNode;
                int sentId = Integer.parseInt(tokElem.getAttribute("sentence"));
                int tokenId = Integer.parseInt(tokElem.getAttribute("number"));
                String tokenText = tokElem.getTextContent();

                if (isEcbPlus && sentId == 0) {
                    continue;
                }

                if (isEcbPlus) {
                    sentId = sentId - 1;
                }

                tokens.add(new Token(sentId, tokenId, tokenText));
            }

            retDoc = new Doc(docId, null, tokens);

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            ex.printStackTrace();
        }

        return retDoc;
    }
}
