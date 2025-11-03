package org.example;
import org.antlr.v4.runtime.Token;

/**
 * Representa un error semántico encontrado durante el análisis.
 * Almacena el mensaje y el token donde ocurrió el error para reportar la línea y columna.
 */
public class ErrorSemantico {
    private final String mensaje;
    private final int linea;
    private final int columna;

    public ErrorSemantico(String mensaje, Token token) {
        this.mensaje = mensaje;
        this.linea = token.getLine();
        this.columna = token.getCharPositionInLine();
    }

    @Override
    public String toString() {
        return "Error Semántico en línea " + linea + ":" + columna + " -> " + mensaje;
    }
}