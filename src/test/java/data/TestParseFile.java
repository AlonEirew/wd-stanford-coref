package data;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class TestParseFile {

//    @Test
    public void testParsePage() throws ParserConfigurationException, SAXException, IOException {
        final String resource = TestParseFile.class.getClassLoader().getResource("ECB+/1/1_8ecbplus.xml").getFile();
        EcbDataLoader parser = new EcbDataLoader();
        final DocAnnotationPair docAnnotationPair = Doc.convertToStanfordAnnotation(parser.buildDoc(new File(resource)));
        System.out.println();
    }
}
