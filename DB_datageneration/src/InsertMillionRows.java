import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class InsertMillionRows {

    public static void main(String[] args) {
        String url = "jdbc:sqlite:basic_data.db"; //SQLite bağlantı adresi (basic_data.db dosyasına bağlanır).
        String insertSQL = "INSERT INTO Tablo1 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; //Veritabanına 10 sütunluk bir satır eklemek için kullanılacak SQL ifadesi

        Random rand = new Random();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");//LocalDateTime'ı "yyyy-MM-dd HH:mm:ss" formatında stringe çevirmek için.

        try (Connection conn = DriverManager.getConnection(url);//Veritabanına bağlanılır.
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {//PreparedStatement performanslı ve güvenli veri ekleme sağlar.
             
            conn.setAutoCommit(false);
            
            try (Statement s = conn.createStatement()) {// Tabloyu temizle
            s.execute("DELETE FROM Tablo1;");
            conn.commit();
            System.out.println("The table was cleared.");
            }


            for (int i = 1; i <= 1_000_000; i++) {//1 milyon satır üretilir. Her iterasyonda bir satırın tüm sütunları üretilip veritabanına eklenir.
                int tamsayi1 = rand.nextInt(10_000_001);//Pozitif dağılım.
                int tamsayi2 = rand.nextInt(20_000_001) - 10_000_000;//Simetrik dağılım.

                int gaussianTamsayi1 = (int) (rand.nextGaussian() * 1_000_000);//Ortalaması 0 olan, pozitif ve negatif yönde ±1 milyon civarında dağılmış tamsayılar üretir.
                double gaussianReel1 = Math.round(rand.nextGaussian() * 1_000_000 * 100_000.0) / 100_000.0;// 1 milyon varyanslı normal dağılmış bir sayı üretmek ve virgülden sonra 5 basamağa yuvarlamak.
                double gaussianReel2 = Math.round((rand.nextGaussian() + 0.5) * 100_000.0) / 100_000.0;//Mean'i 0.5 yaparak ve virgülden sonra 5 basamakla yuvarlayarak double oluşturduk.

                int dayOffset = (int) (rand.nextGaussian() * 10_000);
                int gaussianDate1 = LocalDate.now().plusDays(dayOffset).getDayOfMonth();//Sadece gün bilgisini verir, tarih değil.1–31 arası bir değer döner.

                int secOffset = (int) (rand.nextGaussian() * 1000);
                String gaussianDate2 = LocalDateTime.of(1990, 1, 1, 0, 0).plusSeconds(secOffset).format(dtf);//1990 başlangıçlı 1000 saniye ekleyip çıkartma.

                double gaussianDate3 = LocalDateTime.now().plusSeconds((int)(rand.nextGaussian() * 10))//Şu anki zamana 10 saniye eklenip çıkartılıyor.
                        .atZone(java.time.ZoneId.systemDefault()).toEpochSecond();//atZone saat dilimi. toEpochSecond() geçen saniye.

                byte[] binary = new byte[30]; //30 baytlık sapmalı binary veri
                for (int j = 0; j < 30; j++) {
                    int val = (int) (rand.nextGaussian() * 100 + 128);//mean=128, varyans=100
                    binary[j] = (byte) Math.max(0, Math.min(255, val));//val değeri negatifse 0, val > 255 ise 255 
                }

                StringBuilder hexString = new StringBuilder();
                for (byte b : binary) {//Bu döngü 30 kez döner ve her baytı tek tek işler.
                    hexString.append(String.format("%02x", b));//Binary veri hex string formatına dönüştürülüyor. Bu şekilde metin olarak saklanıyor.
                    //Her baytı arka arkaya ekleyerek toplamda 30 baytlık = 60 karakterlik bir hex string üretir.
                }

                // Verileri SQL'e aktarıyor.
                pstmt.setInt(1, i); //ID
                pstmt.setInt(2, tamsayi1);
                pstmt.setInt(3, tamsayi2);
                pstmt.setInt(4, gaussianTamsayi1);
                pstmt.setDouble(5, gaussianReel1);
                pstmt.setDouble(6, gaussianReel2);
                pstmt.setInt(7, gaussianDate1);
                pstmt.setString(8, gaussianDate2);
                pstmt.setDouble(9, gaussianDate3);
                pstmt.setString(10, hexString.toString());

                pstmt.addBatch(); //addBatch(): Yığın işlemi için.

                if (i % 10_000 == 0) {//10.000 satırda bir batch çalıştırılır ve commit edilir.
                    pstmt.executeBatch();
                    conn.commit();
                    System.out.println(i + " line added...");
                }
            }

            pstmt.executeBatch();//INSERT, UPDATE, DELETE işlemlerini toplu olarak çalıştırır. Tek tek executeUpdate() çağırmak yerine, tümünü tek seferde veritabanına gönderir.
            conn.commit();
            System.out.println("All Data Added Successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
