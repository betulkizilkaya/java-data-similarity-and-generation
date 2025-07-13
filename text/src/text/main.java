package text;

import java.sql.*;//sorgu çalıştırma
import java.io.*; // Dosya okuma

public class main {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:dictionary.db";
        String word_file = "zemberek-ambigious-words.txt";

        try (Connection conn = DriverManager.getConnection(url)) {//Veritabanına bağlanılır.
            if (conn != null) {
                System.out.println("Database connection successful!");

                Statement stmt = conn.createStatement();
                stmt.execute("CREATE TABLE IF NOT EXISTS Tablo2 (" +
                             "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                             "kelime TEXT NOT NULL)");

                String sql = "INSERT INTO Tablo2(kelime) VALUES(?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);

                BufferedReader reader = new BufferedReader(new FileReader(word_file));//BufferedReader: Dosyayı satır satır okumak için kullanılır.
                String line;//Her satırı geçici olarak tutar.
                int count = 0;//sayaç

                while ((line = reader.readLine()) != null) {//Dosya bitene kadar her satır line değişkenine okunuyor.
                    String kelime = line.trim().toLowerCase();

                    // Satır boşsa atla
                    if (!kelime.isEmpty()) {
                        pstmt.setString(1, kelime);//? yerine kelime değerini yerleştirir
                        pstmt.executeUpdate();//Sorgusyu çalıştırır.
                        count++;
                    }
                }

                reader.close();//Dosyayı kapatır.
                System.out.println(count + " the word was added successfully.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
