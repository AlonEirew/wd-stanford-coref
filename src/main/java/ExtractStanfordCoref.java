import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import data.*;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

public class ExtractStanfordCoref {
    private static StanfordCoreNLP sPipeline;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static ExecutorService executorService = null;

    private static List<Mention> evaluateCoref(String ecbDocPath, IDataLoader parser) {
        final List<DocAnnotationPair> docAnnotationPairs = Doc.readToAnnotation(ecbDocPath, parser);
        final List<Mention> allMentions = new ArrayList<>();
        final List<Future<List<Mention>>> futureMentions = new ArrayList<>();
        docAnnotationPairs.stream().forEach(pair -> futureMentions.add(executorService.submit(() -> getMentions(pair))));

        futureMentions.stream().forEach(future -> {
            try {
                allMentions.addAll(future.get(10, TimeUnit.MINUTES));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        });

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                executorService.shutdownNow(); // Cancel currently executing tasks
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return allMentions;
    }

    private static List<Mention> getMentions(DocAnnotationPair pair) {
        Annotation annotDocument = pair.getAnnotation();
        Doc doc = pair.getDoc();
        sPipeline.annotate(annotDocument);
        final Collection<CorefChain> corefChains =
                annotDocument.get(CorefCoreAnnotations.CorefChainAnnotation.class).values();
        doc.setWithinCoref(corefChains);
        System.out.println("Done with doc-" + doc.getDoc_id());
        return doc.createMentionsData();
    }

    public static void main(String[] args) throws IOException {
        String in_corpus = null;
        String output = null;
        int threads = 2;
        for ( int i = 0; i < args.length; i++ ) {
            if ( args[i].startsWith("-corpus=") ) {
                in_corpus = args[i].split("=")[1];
            } else if ( args[i].startsWith("-output=") ) {
                output = args[i].split("=")[1];
            } else if ( args[i].startsWith("-threads=") ) {
                threads = Integer.parseInt(args[i].split("=")[1]);
            }
        }

        System.out.println("Starting process: corpus=" + in_corpus + ", output=" + output + ", threads=" + threads);

        Properties props = new Properties();
        props.setProperty("annotators", "pos,lemma,ner,parse,depparse,coref");
        props.setProperty("coref.algorithm", "neural");
//        props.setProperty("threads", String.valueOf(threads));
        sPipeline = new StanfordCoreNLP(props, false);
        executorService = Executors.newFixedThreadPool(threads);

        final File outfile = new File(output);
        long startTime = System.currentTimeMillis();

        IDataLoader parser = new EcbDataLoader();
        List<Mention> mentions = evaluateCoref(in_corpus, parser);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        FileUtils.write(outfile, gson.toJson(mentions), "UTF-8");
        System.out.println("Processing All Docs took-" + totalTime + "ms");
    }
}
