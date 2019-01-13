package data;

import java.util.*;

public class Token {
    private int sent_id;
    private int token_id;
    private String token_text;
    private boolean span_closed;
    private Set<Integer> within_coref = new HashSet<>();

    public Token(int sent_id, int token_id, String token_text) {
        this.sent_id = sent_id;
        this.token_id = token_id;
        this.token_text = token_text;
    }

    public int getSent_id() {
        return sent_id;
    }

    public int getToken_id() {
        return token_id;
    }

    public String getToken_text() {
        return token_text;
    }

    public boolean isSpan_closed() {
        return span_closed;
    }

    public Set<Integer> getWithin_coref() {
        return within_coref;
    }

    public void setSpan_closed(boolean span_closed) {
        this.span_closed = span_closed;
    }

    public void setWithin_coref(Set<Integer> within_coref) {
        this.within_coref = within_coref;
    }

    public void addWithin_coref(int withinCoref) {
        this.within_coref.add(withinCoref);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return sent_id == token.sent_id &&
                token_id == token.token_id &&
                span_closed == token.span_closed &&
                Objects.equals(token_text, token.token_text) &&
                Objects.equals(within_coref, token.within_coref);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sent_id, token_id, token_text, span_closed, within_coref);
    }

    @Override
    public String toString() {
        return "Token{" +
                "sent_id=" + sent_id +
                ", token_id=" + token_id +
                ", token_text='" + token_text + '\'' +
                ", span_closed=" + span_closed +
                ", within_coref=" + within_coref +
                '}';
    }
}
