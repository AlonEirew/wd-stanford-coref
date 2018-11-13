package data;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
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

    public void alignWithResourceDoc(Annotation document) {
        final List<CoreLabel> coreLabels = document.get(CoreAnnotations.TokensAnnotation.class);
        int x = 0;
        for(int i = 0 ; i < coreLabels.size() ; i++) {
            for(int j = x ; j < this.tokens.size() ; j++) {
                CoreLabel stfToken = coreLabels.get(i);
                String stfTokenText = stfToken.get(CoreAnnotations.TextAnnotation.class);
                if(this.tokens.get(j).getDoc_tok_id_span() == null) {
                    if (stfTokenText.equals(this.tokens.get(j).getToken_text())) {
                        this.tokens.get(j).addDoc_tok_id_span(i);
                        this.tokens.get(j).addDoc_tok_id_span(i);
                        this.tokens.get(j).setSpan_closed(true);
                        if(j > 0) {
                            this.tokens.get(j - 1).setSpan_closed(true);
                            x = j - 1;
                        } else {
                            x = 0;
                        }
                        break;
                    } else if (this.tokens.get(j).getToken_text().contains(stfTokenText)) {
                        this.tokens.get(j).addDoc_tok_id_span(i);
                        if(j > 0) {
                            x = j - 1;
                        } else {
                            x = 0;
                        }
                        break;
                    } else if (stfTokenText.contains(this.tokens.get(j).getToken_text())) {
                        this.tokens.get(j).addDoc_tok_id_span(i);
                        this.tokens.get(j).setSpan_closed(true);
                        if(j > 0) {
                            this.tokens.get(j - 1).setSpan_closed(true);
                            x = j - 1;
                        } else {
                            x = 0;
                        }
                        break;
                    }
                } else if(this.tokens.get(j).getToken_text().contains(stfTokenText)) {
                    this.tokens.get(j).addDoc_tok_id_span(i);
                    if(j > 0) {
                        x = j - 1;
                    } else {
                        x = 0;
                    }
                    break;
                }
            }
        }
    }

    private void findTokenAndSetClusterId(int tokenId, int clusterId) {
        boolean found = false;
        for(Token token : this.tokens) {
            if(token.getDoc_tok_id_span() != null && token.getDoc_tok_id_span().contains(tokenId)) {
                token.addWithin_coref(clusterId);
                found = true;
            }
        }

        if(!found) {
            System.out.println("**** Token not found for-" + this.doc_id + ", Resource token-" + tokenId);
        }
    }

    public void setWithinCoref(Collection<CorefChain> corefChains) {
        for(CorefChain corefChain : corefChains) {
            final List<CorefChain.CorefMention> mentionsInTextualOrder = corefChain.getMentionsInTextualOrder();
            for(CorefChain.CorefMention mention : mentionsInTextualOrder) {
                List<Integer> corefSpan = new ArrayList<>();
                corefSpan.add(mention.startIndex);
                corefSpan.add(mention.endIndex);
                for(int tokId = corefSpan.get(0) ; tokId < corefSpan.get(1) ; tokId++ ) {
                    findTokenAndSetClusterId(tokId, mention.corefClusterID);
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
                    }
                }
                result.add(new Mention(doc_id, token.getSent_id(), tokensIds, mention_str, String.valueOf(curWithinDoc)));
            }
        }
        return result;
    }

    public static List<ECBDoc> readEcb(String ecbPath) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        List<ECBDoc> documents = new ArrayList<>();
        Collection<File> files = FileUtils.listFiles(new File(ecbPath), new RegexFileFilter(".*xml$"),
                DirectoryFileFilter.DIRECTORY);

        for (File file : files) {
            boolean isEcbPlus = false;
            System.out.println("Processing file-" + file.getName());
            if(file.getName().contains("ecbplus")) {
                isEcbPlus = true;
            }
            Document xmlDocument = builder.parse(file);
            Element root = xmlDocument.getDocumentElement();
            final String docId = root.getAttribute("doc_name");
            String documentText = null;
            NodeList tokensNodes = root.getElementsByTagName("token");
            ArrayList<Token> tokens = new ArrayList<>();
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

                tokens.add(new Token(sentId, tokenId, tokenText));
                if (documentText == null) {
                    documentText = tokenText;
                } else if (tokenText.matches("\\.|,|\\?|!|'re|'s|'n|'t|'ve|'m|'ll")) {
                    documentText += tokenText;
                } else {
                    documentText += " " + tokenText;
                }
            }

            documents.add(new ECBDoc(docId, documentText, tokens));

        }

        return documents;
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
