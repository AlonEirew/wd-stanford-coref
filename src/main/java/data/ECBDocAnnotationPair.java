package data;

import edu.stanford.nlp.pipeline.Annotation;

public class ECBDocAnnotationPair {
    private ECBDoc doc;
    private Annotation annotation;

    public ECBDocAnnotationPair(ECBDoc doc, Annotation annotation) {
        this.doc = doc;
        this.annotation = annotation;
    }

    public ECBDoc getDoc() {
        return doc;
    }

    public Annotation getAnnotation() {
        return annotation;
    }
}
