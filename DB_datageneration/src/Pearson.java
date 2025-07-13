import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Pearson {

    public static void main(String[] args) {
        String url = "jdbc:sqlite:basic_data.db";

        String[] columns = { //Korelasyon hesaplanacak sütunları tanımlandı
            "Tamsayi1", "Tamsayi2", "GaussianTamsayi1",
            "GaussianReelsayi1", "GaussianReelsayi2",
            "GaussianDate1", "GaussianDate3"
        };

        try (Connection conn = DriverManager.getConnection(url)) {

            createSimilarityTable(conn);//BinaryColumnSimilarity oluşturur

            for (int i = 0; i < columns.length; i++) {//Tüm sütun çiftleri için korelasyon hesaplanır.
                for (int j = i + 1; j < columns.length; j++) {
                    List<Double> col1 = getColumnData(conn, columns[i]);//Her iki sütunun verileri ayrı ayrı alınır.
                    List<Double> col2 = getColumnData(conn, columns[j]);
                    double correlation = computePearson(col1, col2);//Pearson korelasyon katsayısı hesaplanır.

                    System.out.printf("Correlation between %s and %s: %.4f%n",//Sonuç
                            columns[i], columns[j], correlation);

                    insertSimilarity(conn, columns[i], columns[j], correlation);//DB kaydedilir.
                }
            }

            System.out.println("All correlations were recorded in the database.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Double> getColumnData(Connection conn, String columnName) throws SQLException {//Tablo1'deki sütunun tüm değerleri
        List<Double> values = new ArrayList<>();
        String sql = "SELECT " + columnName + " FROM Tablo1";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql))//Select sorgusunu çalıştırdık
        {
            while (rs.next()) {
                values.add(rs.getDouble(1));//ResultSet'ten ilk sütunu double olarak alır.
            }
        }
        return values;
    }

    public static double computePearson(List<Double> x, List<Double> y) {
        if (x.size() != y.size()) return 0;//Veri listeleri eşit uzunlukta değilse hesaplama yapılmaz.

        int n = x.size();
        double sumX = 0, sumY = 0, sumXY = 0;
        double sumX2 = 0, sumY2 = 0;

        for (int i = 0; i < n; i++) {
            double xi = x.get(i);
            double yi = y.get(i);
            sumX += xi;
            sumY += yi;
            sumXY += xi * yi;
            sumX2 += xi * xi;
            sumY2 += yi * yi;
        }

        double numerator = (n * sumXY) - (sumX * sumY);
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        if (denominator == 0) return 0;//Bölme kontrol(payda 0 olamaz)
        return numerator / denominator;
    }

    public static void createSimilarityTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS BinaryColumnSimilarity (
                Column1 TEXT,
                Column2 TEXT,
                Similarity REAL
            );
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public static void insertSimilarity(Connection conn, String col1, String col2, double similarity) throws SQLException {
        String insertSQL = "INSERT INTO BinaryColumnSimilarity (Column1, Column2, Similarity) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, col1);
            pstmt.setString(2, col2);
            pstmt.setDouble(3, similarity);
            pstmt.executeUpdate();
        }
    }
}
