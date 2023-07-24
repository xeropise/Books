package Chapter01_String.P05_allVowels;

public class Pair<K, V> {

    private K vowels;
    private V consonants;

    public Pair(K vowels, V consonants) {
        this.vowels = vowels;
        this.consonants = consonants;
    }

    static <K,V> Pair<K, V> of(K vowels, V consonants) {
        return new Pair(vowels, consonants);
    }

    public K getVowels() {
        return vowels;
    }

    public void setVowels(K vowels) {
        this.vowels = vowels;
    }

    public V getConsonants() {
        return consonants;
    }

    public void setConsonants(V consonants) {
        this.consonants = consonants;
    }
}
