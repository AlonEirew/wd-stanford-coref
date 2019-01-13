package data;

import edu.stanford.nlp.pipeline.Annotation;

public class DocAnnotationPair {
    private Doc doc;
    private Annotation annotation;

    public DocAnnotationPair(Doc doc, Annotation annotation) {
        this.doc = doc;
        this.annotation = annotation;
    }

    public Doc getDoc() {
        return doc;
    }

    public Annotation getAnnotation() {
        return annotation;
    }
}
