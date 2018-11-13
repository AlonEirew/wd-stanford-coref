package data;

import java.util.List;
import java.util.Objects;

public class Mention {
    private String doc_id;
    private int sent_id;
    private List<Integer> tokens_number;
    private String tokens_str;
    private String coref_chain;

    public Mention(String doc_id, int sent_id, List<Integer> tokens_number, String tokens_str, String coref_chain) {
        this.doc_id = doc_id;
        this.sent_id = sent_id;
        this.tokens_number = tokens_number;
        this.tokens_str = tokens_str;
        this.coref_chain = coref_chain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mention mention = (Mention) o;
        return sent_id == mention.sent_id &&
                Objects.equals(doc_id, mention.doc_id) &&
                Objects.equals(tokens_number, mention.tokens_number) &&
                Objects.equals(tokens_str, mention.tokens_str) &&
                Objects.equals(coref_chain, mention.coref_chain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doc_id, sent_id, tokens_number, tokens_str, coref_chain);
    }

    @Override
    public String toString() {
        return "Mention{" +
                "doc_id='" + doc_id + '\'' +
                ", sent_id=" + sent_id +
                ", tokens_number=" + tokens_number +
                ", tokens_str='" + tokens_str + '\'' +
                ", coref_chain='" + coref_chain + '\'' +
                '}';
    }
}
