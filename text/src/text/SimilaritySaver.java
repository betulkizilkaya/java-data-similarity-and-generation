package text;

import java.sql.*;
import java.util.*;

public class SimilaritySaver {
    public static void saveSimilarities(Connection conn, List<String> words) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS Similarities (" +
                     "kelime1 TEXT, kelime2 TEXT, skor REAL)");

        PreparedStatement pstmt = conn.prepareStatement(
            "INSERT INTO Similarities(kelime1, kelime2, skor) VALUES (?, ?, ?)");

        for (int i = 0; i < words.size(); i++) {//w1 kelimesine benzer bulunan kelimeleri ve skorları tutan liste
            String w1 = words.get(i);
            List<String[]> similar = new ArrayList<>();

            for (int j = i + 1; j < words.size(); j++) {//j = i + 1 kullanılması simetriyi önler.
                String w2 = words.get(j);
                double score = SimilarityCalculator.compute(w1, w2);//w1 ve w2 kelimeleri arasındaki benzerlik skoru (0–1 arası)

                if (score >= 0.70) {//0.70 veya üzeri olanları aldık.Duruma göre düşürülüp arttırılabilir.
                	similar.add(new String[]{w1, w2, String.valueOf(score)});
                }
            }

            similar.sort((a, b) -> Double.compare(Double.parseDouble(b[2]), Double.parseDouble(a[2])));//Benzerlik skoru yüksekten düşüğe sıralanır.
            
            for (int k = 0; k < Math.min(10, similar.size()); k++) {//En fazla 10 tanesi alınır.
                String[] pair = similar.get(k);
                pstmt.setString(1, pair[0]);
                pstmt.setString(2, pair[1]);
                pstmt.setDouble(3, Double.parseDouble(pair[2]));
                pstmt.executeUpdate();
            }

            if (i % 10 == 0)
                System.out.println((i + 1) + "/" + words.size() + " word processed.");//Her 10 kelimede bilgilendirme mesajı
        }

        System.out.println("The similarities were written successfully.");
    }
}
