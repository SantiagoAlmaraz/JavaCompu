package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import javax.swing.*;

public class Main {

    private static final String EXTENSION = "lang";
    private static final String DIRBASE = "src/test/resources/";

    public static void main(String[] args) throws IOException {
        String files[] = args.length==0? new String[]{ "test." + EXTENSION } : args;
        System.out.println("Dirbase: " + DIRBASE);
        for (String file : files){
            System.out.println("START: " + file);

            System.out.println("\n--- CODIGO ---");
            System.out.println(new String(Files.readAllBytes(Paths.get(DIRBASE+file))));
            System.out.println("--------------\n");

            CharStream in = CharStreams.fromFileName(DIRBASE + file);
            LanguageLexer lexer = new LanguageLexer(in);
            CommonTokenStream tokens = new CommonTokenStream(lexer);


            //----------------------------------------------------------------
            // Analisis sintactico (entrega 3)
            LanguageParser parser = new LanguageParser(tokens);
            LanguageParser.InicioContext tree = parser.inicio();
            JFrame frame = new JFrame("Arbol Sintactico");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1300,1000);
            List<String> reglas = Arrays.asList(parser.getRuleNames());
            TreeViewer arbol = new TreeViewer(reglas, tree);
            arbol.setScale(1.7);
            frame.add(new JScrollPane(arbol));
            frame.setVisible(true);
            //----------------------------------------------------------------

            //----------------------------------------------------------------
            // Análisis Semántico (entrega final)
            AnalizadorSemanticoVisitor analizador = new AnalizadorSemanticoVisitor();
            analizador.visit(tree);

            //  Comprobación de Errores
            if (analizador.hayErrores()) {
                System.out.println("Se encontraron errores semánticos. El programa no se ejecutará.");
                for (ErrorSemantico error : analizador.getErrores()) {
                    System.err.println(error);
                }
            } else {
                // Pasada de Ejecución
                System.out.println("Análisis semántico exitoso. Ejecutando el programa...");
                EjecutorVisitor ejecutor = new EjecutorVisitor();
                ejecutor.visit(tree);
            }
            //----------------------------------------------------------------

            System.out.println("FINISH: " + file);
        }
    }
}
