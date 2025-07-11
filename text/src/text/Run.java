package text;

import java.sql.*;
import java.util.*;

public class Run {
    public static void main(String[] args) {
        String db = "dictionary.db";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + db)) {
            List<String> kelimeler = DBWordReader.getWords(conn);//Tablo2 tablosundaki tüm kelimeler okunur.
            SimilaritySaver.saveSimilarities(conn, kelimeler);//Her kelime, diğer kelimelerle karşılaştırılır. Eşleşmeler Benzerlikler tablosuna yazar
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
