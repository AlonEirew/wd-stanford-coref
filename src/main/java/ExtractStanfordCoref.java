import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import data.ECBDoc;
import data.ECBDocAnnotationPair;
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

    private static List<Mention> evaluateCoref(String ecbDocPath) throws IOException, SAXException, ParserConfigurationException {
        final List<ECBDocAnnotationPair> ecbDocAnnotationPairs = ECBDoc.readEcbToAnnotation(ecbDocPath);
        final List<Mention> allMentions = new ArrayList<>();
        for(ECBDocAnnotationPair pair : ecbDocAnnotationPairs) {
            Annotation annotDocument = pair.getAnnotation();
            ECBDoc ecbDoc = pair.getDoc();
            sPipeline.annotate(annotDocument);
            final Collection<CorefChain> corefChains = annotDocument.get(CorefCoreAnnotations.CorefChainAnnotation.class).values();
            ecbDoc.setWithinCoref(corefChains);
            allMentions.addAll(ecbDoc.createMentionsData());
            System.out.println("Done with doc-" + ecbDoc.getDoc_id());
        }

        return allMentions;
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        String ecb_path = null;
        String output = null;
        for ( int i = 0; i < args.length; i++ ) {
            if ( args[i].equals("-ecb") ) {
                i++;
                ecb_path = args[i];
            } else if ( args[i].equals("-output") ) {
                i++;
                output = args[i];
            }
        }

        Properties props = new Properties();
        props.setProperty("annotators", "pos,lemma,ner,parse,depparse,coref");
        props.setProperty("coref.algorithm", "neural");
        sPipeline = new StanfordCoreNLP(props, false);

        final File outfile = new File(output);
        long startTime = System.currentTimeMillis();
        List<Mention> mentions = evaluateCoref(ecb_path);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        FileUtils.write(outfile, gson.toJson(mentions), "UTF-8");
        System.out.println("Processing All Docs took-" + totalTime + "ms");
    }
}
