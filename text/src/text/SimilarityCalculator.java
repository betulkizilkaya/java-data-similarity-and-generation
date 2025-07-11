package text;

import org.apache.commons.text.similarity.*;//LevenshteinDistance, JaroWinklerDistance benzerlik alg. içeri aktarır.

public class SimilarityCalculator {
    private static final LevenshteinDistance lev = new LevenshteinDistance();
    private static final JaroWinklerDistance jw = new JaroWinklerDistance();

    public static double compute(String s1, String s2) {
        double l = normalizedLevenshtein(s1, s2);// 0–1 arası skor hesaplar
        double j = jw.apply(s1, s2);//ikinci skor hesaplanır
        return (l + j) / 2.0;//ortalaması alınır.
    }

    private static double normalizedLevenshtein(String s1, String s2) {
        int d = lev.apply(s1, s2);//kaç harf eklenmeli/silinmeli/değiştirilmeli ki s1 → s2 olsun
        int maxLen = Math.max(s1.length(), s2.length());//maxLen'e böler böylece uzun kelimelerle kısa kelimeleri adil karşılaştırır
        return (maxLen == 0) ? 1.0 : 1.0 - ((double) d / maxLen);//1.0'dan çıkarır → böylece:1.0 → tamamen aynı/0.0 → tamamen farklı
    }
}
