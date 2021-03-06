package data;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.junit.Test;

import java.util.Properties;

public class TestTokenize {

//    @Test
    public static void TestStanfTok() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit");
        props.setProperty("coref.algorithm", "neural");
        StanfordCoreNLP sPipeline = new StanfordCoreNLP(props, false);

        Annotation document = new Annotation(TestTokenize.getText());
        sPipeline.annotate(document);
        System.out.println();
    }

    private static String getText() {
        return "Lindsay Lohan rehires longtime lawyer Shawn Holley, heads to rehab Published May 03, 2013 It seems " +
                "Lindsay Lohan has had enough of New York attorney Mark Jay Heller. The actress re - hired " +
                "longtime attorney Shawn Holley to handle her case. Heller told a judge during a hearing " +
                "Thursday morning that Lohan had checked in to the Morningside Recovery rehab facility, " +
                "but the starlet left it after a few minutes. The actress, however, has since checked in to a " +
                "different rehab but will not face a probation violation for leaving another treatment facility after " +
                "a few minutes, a prosecutor said Friday. Lohan made it to the new facility late Thursday, TMZ reported. " +
                "White said he has received confirmation that Lohan has checked in to a rehab facility and he is " +
                "satisfied with her location. He declined to say where Lohan is receiving treatment, " +
                "but reports soon surfaced that she was at the Betty Ford Center in Rancho Mirage, Calif. " +
                "\"Lindsay never checked into Morningside but is back at Betty Ford. The challenge now is " +
                "to keep her there for the 90 days, \" the source told us. Lohan has spent time at Betty " +
                "Ford before. She served another mandatory rehab sentence at the treatment center, although her stay " +
                "there was not without drama.";
    }
}
