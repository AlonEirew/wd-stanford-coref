package data;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
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
import java.util.*;

public class ECBDoc {
    private String doc_id;
    private String text;
    private List<Token> tokens;

    public ECBDoc(String doc_id, String text, List<Token> tokens) {
        this.doc_id = doc_id;
        this.text = text;
        this.tokens = tokens;
    }

    public String getDoc_id() {
        return doc_id;
    }

    public String getText() {
        return text;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    private Token getTokenByIdAndSentNum(int tokenId, int sentNum) {
        for(Token token : this.tokens) {
            if(token.getToken_id() == tokenId && token.getSent_id() == sentNum) {
                return token;
            }
        }

        System.out.println("**** Token not found for-" + this.doc_id + ", SentId-" + sentNum + ", tokenId-" + tokenId);
        return null;
    }

    public void setWithinCoref(Collection<CorefChain> corefChains) {
        for(CorefChain corefChain : corefChains) {
            final List<CorefChain.CorefMention> mentionsInTextualOrder = corefChain.getMentionsInTextualOrder();
            for(CorefChain.CorefMention mention : mentionsInTextualOrder) {
                for(int tokId = mention.startIndex - 1 ; tokId < mention.endIndex - 1 ; tokId++ ) {
                    Token token = getTokenByIdAndSentNum(tokId, mention.sentNum - 1);
                    if (token != null) {
                        token.addWithin_coref(mention.corefClusterID);
                    }
                }
            }
        }
    }

    public List<Mention> createMentionsData() {
        List<Mention> result = new ArrayList<>();
        for(int i = 0 ; i < this.tokens.size() ; i++) {
            Token token = this.tokens.get(i);
            Iterator<Integer> iter = token.getWithin_coref().iterator();
            while (token.getWithin_coref().size() > 0) {
                int curWithinDoc = iter.next();
                iter.remove();
                String mention_str = token.getToken_text();
                List<Integer> tokensIds = new ArrayList<>();
                tokensIds.add(token.getToken_id());
                for (int j = i + 1; j < this.tokens.size(); j++) {
                    if (this.tokens.get(j).getWithin_coref().contains(curWithinDoc)) {
                        mention_str += " " + this.tokens.get(j).getToken_text();
                        tokensIds.add(this.tokens.get(j).getToken_id());
                        Iterator<Integer> tokenWithin = this.tokens.get(j).getWithin_coref().iterator();
                        while(tokenWithin.hasNext()) {
                            int tokenCoref = tokenWithin.next();
                            if (tokenCoref == curWithinDoc) {
                                tokenWithin.remove();
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
                result.add(new Mention(doc_id, token.getSent_id(), tokensIds, mention_str, String.valueOf(curWithinDoc)));
            }
        }
        return result;
    }

    public static List<ECBDocAnnotationPair> readEcbToAnnotation(String ecbPath) throws ParserConfigurationException, IOException, SAXException {
        List<ECBDocAnnotationPair> ecbDocAnnotationPairs = new ArrayList<>();
        Collection<File> files = FileUtils.listFiles(new File(ecbPath), new RegexFileFilter(".*xml$"),
                DirectoryFileFilter.DIRECTORY);

        for (File file : files) {
            ecbDocAnnotationPairs.add(parseFile(file));
        }

        return ecbDocAnnotationPairs;
    }

    static ECBDocAnnotationPair parseFile(File file) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        boolean isEcbPlus = false;
        System.out.println("Processing file-" + file.getName());
        if(file.getName().contains("ecbplus")) {
            isEcbPlus = true;
        }
        Document xmlDocument = builder.parse(file);
        Element root = xmlDocument.getDocumentElement();
        final String docId = root.getAttribute("doc_name");
        NodeList tokensNodes = root.getElementsByTagName("token");

        String documentText = null;
        String sentText = null;
        int tokenSentBeginInx = 0;
        int tokenSentEndInx = 0;
        int tokenBeginIndex = -1;
        List<CoreLabel> coreLabels = new ArrayList<>();
        List<CoreLabel> sentTokens = new ArrayList<>();
        List<CoreMap> sentAnnotations = new ArrayList<>();
        ArrayList<Token> tokens = new ArrayList<>();
        int sentIdLast = -1;
        for(int i = 0 ; i < tokensNodes.getLength() ; i++) {
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

            if (sentId != sentIdLast) {
                if (sentIdLast != -1) {
                    Annotation sentAnnot = getSentenceAnnot(documentText, sentText, sentTokens, sentIdLast, tokenSentBeginInx, tokenSentEndInx);
                    sentAnnotations.add(sentAnnot);
                }
                sentIdLast = sentId;
                tokenSentBeginInx = tokenSentEndInx;
                sentText = null;
                sentTokens.clear();
            }

            tokenSentEndInx++;

            documentText = buildText(documentText, tokenText);
            sentText = buildText(sentText, tokenText);

            int charOffSetBegin = documentText.length() - tokenText.length();

            tokens.add(new Token(sentId, tokenId, tokenText));

            tokenBeginIndex ++;
            CoreLabel coreLabel = getCoreLabel(tokenText, tokenBeginIndex, tokenId, sentId, charOffSetBegin, documentText.length());
            coreLabels.add(coreLabel);
            sentTokens.add(coreLabel);
        }

        Annotation sentAnnot = getSentenceAnnot(documentText, sentText, sentTokens, sentIdLast, tokenSentBeginInx, tokenSentEndInx);
        sentAnnotations.add(sentAnnot);

        ECBDoc ecbDoc = new ECBDoc(docId, documentText, tokens);
        Annotation coreMap = new Annotation();
        coreMap.set(CoreAnnotations.TextAnnotation.class, documentText);
        coreMap.set(CoreAnnotations.TokensAnnotation.class, coreLabels);
        coreMap.set(CoreAnnotations.SentencesAnnotation.class, sentAnnotations);

        ECBDocAnnotationPair ecbAnnotPair = new ECBDocAnnotationPair(ecbDoc, coreMap);

        return ecbAnnotPair;
    }

    private static Annotation getSentenceAnnot(String documentText, String sentText, List<CoreLabel> sentTokens, int sentIdLast, int tokenSentBeginInx, int tokenSentEndInx) {
        Annotation sentAnnot = new Annotation();
        int sentCharOffSetBegin = documentText.indexOf(sentText);
        int sentCharOffSetEnd = sentCharOffSetBegin + sentText.length();
        List<CoreLabel> sentTokensCopy = new ArrayList<>();
        for (CoreLabel coreLabel : sentTokens) {
            sentTokensCopy.add(coreLabel);
        }

        sentAnnot.set(CoreAnnotations.TextAnnotation.class, sentText);
        sentAnnot.set(CoreAnnotations.CharacterOffsetBeginAnnotation.class, sentCharOffSetBegin);
        sentAnnot.set(CoreAnnotations.CharacterOffsetEndAnnotation.class, sentCharOffSetEnd);
        sentAnnot.set(CoreAnnotations.TokensAnnotation.class, sentTokensCopy);
        sentAnnot.set(CoreAnnotations.TokenBeginAnnotation.class, tokenSentBeginInx);
        sentAnnot.set(CoreAnnotations.TokenEndAnnotation.class, tokenSentEndInx);
        sentAnnot.set(CoreAnnotations.SentenceIndexAnnotation.class, sentIdLast);
        return sentAnnot;
    }

    private static CoreLabel getCoreLabel(String tokenText, int i, int indexInSent, int sentId, int charOffSetBegin, int charOffSetEnd) {
        CoreLabel coreLabel = new CoreLabel();
        coreLabel.set(CoreAnnotations.ValueAnnotation.class, tokenText);
        coreLabel.set(CoreAnnotations.TextAnnotation.class, tokenText);
        coreLabel.set(CoreAnnotations.OriginalTextAnnotation.class, tokenText);
        coreLabel.set(CoreAnnotations.CharacterOffsetBeginAnnotation.class, charOffSetBegin);
        coreLabel.set(CoreAnnotations.CharacterOffsetEndAnnotation.class, charOffSetEnd);
        coreLabel.set(CoreAnnotations.TokenBeginAnnotation.class, i);
        coreLabel.set(CoreAnnotations.TokenEndAnnotation.class, i + 1);
        coreLabel.set(CoreAnnotations.IsNewlineAnnotation.class, false);
        coreLabel.set(CoreAnnotations.IndexAnnotation.class, indexInSent + 1);
        coreLabel.set(CoreAnnotations.SentenceIndexAnnotation.class, sentId);
        return coreLabel;
    }

    private static String buildText(String text, String tokenText) {
        if (text == null) {
            text = tokenText;
        } else if (tokenText.matches("\\.|,|\\?|!|'re|'s|'n|'t|'ve|'m|'ll")) {
            text += tokenText;
        } else {
            text += " " + tokenText;
        }
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ECBDoc ecbDoc = (ECBDoc) o;
        return Objects.equals(doc_id, ecbDoc.doc_id) &&
                Objects.equals(text, ecbDoc.text) &&
                Objects.equals(tokens, ecbDoc.tokens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doc_id, text, tokens);
    }
}
