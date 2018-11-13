import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import data.ECBDoc;
import data.Mention;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class ExtractStanfordCoref {
    private static StanfordCoreNLP sPipeline;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref");
        props.setProperty("coref.algorithm", "neural");
        sPipeline = new StanfordCoreNLP(props);
    }

    private static List<Mention> evaluateCoref(String ecbDocPath) throws IOException, SAXException, ParserConfigurationException {
        final List<ECBDoc> ecbDocs = ECBDoc.readEcb(ecbDocPath);
        final List<Mention> allMentions = new ArrayList<>();
        for(ECBDoc doc : ecbDocs) {
            String docText = doc.getText();
            System.out.println("\n #### DOCUMENT:" + doc.getDoc_id() + ", TEXT: #####\n" + docText);
            Annotation document = new Annotation(docText);
            sPipeline.annotate(document);
            doc.alignWithResourceDoc(document);
            final Collection<CorefChain> corefChains = document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values();
            doc.setWithinCoref(corefChains);
            allMentions.addAll(doc.createMentionsData());
            System.out.println("Done with doc-" + doc.getDoc_id());
        }

        return allMentions;
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        final String resource = ExtractStanfordCoref.class.getClassLoader().getResource("ECB+/1").getFile();
        final File outfile = new File("/Users/aeirew/workspace/ecb-wd-stanford-coref/output/ecb_wd_coref.json");
        long startTime = System.currentTimeMillis();
        List<Mention> mentions = evaluateCoref(resource);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        FileUtils.write(outfile, gson.toJson(mentions), "UTF-8");
        System.out.println("Processing All Docs took-" + totalTime + "ms");
    }
}
