package org.example;
import org.antlr.v4.runtime.Token;

/**
 * Representa un identificador (símbolo) en el código fuente.
 *
 * Esta clase almacena toda la información relevante para un símbolo durante
 * las fases de análisis semántico y ejecución:
 * - Nombre: El identificador de la variable (ej: "x").
 * - Tipo: El tipo de dato (ej: "int").
 * - Valor: El valor actual de la variable durante la ejecución. Es null durante el análisis.
 * - Token de Definición: El token exacto donde fue declarado. Esencial para
 *   reportar errores precisos (línea y columna).
 */
public class Simbolo {

    private final String nombre;
    private final String tipo;
    private final Token tokenDefinicion; // Guarda el contexto de la declaración.
    private Integer valor;

    // Constructor para la fase de analisis semantico
    public Simbolo(String nombre, String tipo, Token tokenDefinicion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.tokenDefinicion = tokenDefinicion;
        this.valor = null; // El valor se asigna durante la declaración o asignación.
    }

    // Constructor para la fase de ejecución
    public Simbolo(String nombre, String tipo, Token tokenDefinicion, Integer valor) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.tokenDefinicion = tokenDefinicion;
        this.valor = valor;
    }

    // --- Getters ---

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public Integer getValor() {
        return valor;
    }

    public Token getTokenDefinicion() {
        return tokenDefinicion;
    }

    // --- Setter ---

    public void setValor(Integer valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "Simbolo{" +
                "nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", definidoEn=" + tokenDefinicion.getLine() + ":" + tokenDefinicion.getCharPositionInLine() +
                '}';
    }
}
