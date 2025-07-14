package text;

import java.sql.*;
import java.io.File;
import java.io.FileWriter;

import guru.nidi.graphviz.engine.*;
import guru.nidi.graphviz.model.*;
import guru.nidi.graphviz.attribute.Label;

import static guru.nidi.graphviz.model.Factory.*;

public class text_Graphviz {
    public static void main(String[] args) {
        System.out.println("The code is initialized.");
        Graphviz.useEngine(new GraphvizCmdLineEngine());//Graphviz'in komut satırı motorunu (dot.exe) kullanacağını belirtir.
        System.out.println("Dot engine selected.");

        MutableGraph graph = mutGraph("Similarities").setDirected(true);//Adı Similarities olan ve yönlü (ok yönü olan) bir grafik nesnesi oluşturur.


        File dotOut = new File("similarities.dot"); //File dotOut = new File("similarities.dot");

        try (
            Connection conn = DriverManager.getConnection("jdbc:sqlite:dictionary.db");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT kelime1, kelime2, skor FROM Similarities LIMIT 10");// Büyük verilerde sistem kilitlenmesin diye sınırlı.
        ) {
            System.out.println("✅ Database connection established.");

            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                String source = rs.getString("kelime1");//Her kelime çifti için ok eklenir. 
                String target = rs.getString("kelime2");
                double score = rs.getDouble("skor");//Label olarak skoru gösterir.

                graph.add(mutNode(source).addLink(to(mutNode(target)).with(Label.of(String.format("%.2f", score)))));//Her kelime çifti için ok eklenir, Label skoru gösterir.
            }

            if (hasData) {
                System.out.println("Writing dot file...");
                FileWriter writer = new FileWriter(dotOut);//DOT dosyası diske yazılır.
                writer.write(graph.toString());
                writer.close();
                System.out.println("Dot file written: " + dotOut.getAbsolutePath());

                System.out.println("Creating PNG...");
                File svgOut = new File("Similarities-graph.png");

                ProcessBuilder pb = new ProcessBuilder(
                    "dot", "-Tpng", dotOut.getAbsolutePath(), "-o", svgOut.getAbsolutePath()//dot -Tpng similarities.dot -o Similarities-graph.png
                );
                pb.inheritIO().start().waitFor();
                //pb.inheritIO()->Terminalde dot komutunun çıktısını Java konsolunda görmesini sağlar.
                //pb.start()->dot programı bu noktada çalışır ve PNG üretmeye başlar.
                //.waitFor()->dot işlemi tamamlanana kadar bekler

                System.out.println("✅ PNG successfully created: " + svgOut.getAbsolutePath());
            } else {
                System.out.println("⚠️ There is no data in the database.");
            }
            

        } catch (Exception e) {
            e.printStackTrace(); // Hataları göster
        }
    }
}
