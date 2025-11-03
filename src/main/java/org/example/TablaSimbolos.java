package org.example;
import org.antlr.v4.runtime.Token;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Implementa la Tabla de Símbolos para el compilador.
 *
 * Esta clase gestiona todos los ámbitos (scopes) y los símbolos declarados.
 * Utiliza una pila de mapas para manejar los ámbitos anidados, permitiendo
 * el sombreado de variables (variable shadowing) y asegurando que las variables
 * locales se destruyan al salir de su ámbito.
 */
public class TablaSimbolos {

    private final Stack<Map<String, Simbolo>> ambitos;

    public TablaSimbolos() {
        this.ambitos = new Stack<>();
        abrirAmbito(); // Abrimos el ámbito global al iniciar.
    }

    /**
     * Inicia un nuevo ámbito anidado (ej: al entrar a un bloque 'if').
     * Empuja un nuevo mapa a la cima de la pila.
     */
    public void abrirAmbito() {
        ambitos.push(new HashMap<>());
    }

    /**
     * Cierra el ámbito actual (ej: al salir de un bloque 'if').
     * Elimina el mapa de la cima de la pila.
     */
    public void cerrarAmbito() {
        if (!ambitos.isEmpty()) {
            ambitos.pop();
        }
    }

    /**
     * MÉTODO 1: Inserción con Validación Semántica.
     * Usado por el AnalizadorSemanticoVisitor (Pasada 1).
     *
     * Crea un nuevo símbolo y lo inserta, pero ANTES comprueba si ya existe
     * uno con el mismo nombre en el ámbito actual para detectar errores de redeclaración.
     *
     * @param nombre El nombre del símbolo a declarar.
     * @param tipo El tipo del símbolo.
     * @param token El token de la declaración.
     * @return El Simbolo recién creado.
     * @throws ErrorSemanticoException Si el símbolo ya está definido en el ámbito actual.
     */
    public Simbolo insertar(String nombre, String tipo, Token token) {
        Map<String, Simbolo> ambitoActual = ambitos.peek();

        if (ambitoActual.containsKey(nombre)) {
            Simbolo existente = ambitoActual.get(nombre);
            String mensaje = "La variable '" + nombre + "' ya fue declarada en este ámbito en la línea "
                    + existente.getTokenDefinicion().getLine();
            throw new ErrorSemanticoException(mensaje, token);
        }

        Simbolo nuevoSimbolo = new Simbolo(nombre, tipo, token);
        ambitoActual.put(nombre, nuevoSimbolo);
        return nuevoSimbolo;
    }

    /**
     * MÉTODO 2: Inserción Directa (Sobrecargado).
     * Usado por el EjecutorVisitor (Pasada 2).
     *
     * Simplemente coloca un objeto Simbolo preexistente en el ámbito actual.
     * No realiza ninguna validación, ya que asume que el código es semánticamente
     * correcto gracias a la primera pasada.
     *
     * @param simbolo El objeto Simbolo a insertar.
     */
    public void insertar(Simbolo simbolo) {
        Map<String, Simbolo> ambitoActual = ambitos.peek();
        ambitoActual.put(simbolo.getNombre(), simbolo);
    }

    /**
     * Busca un símbolo por su nombre en todos los ámbitos, desde el más
     * interno (actual) hasta el más externo (global).
     *
     * @param nombre El nombre del símbolo a buscar.
     * @return El objeto Simbolo si se encuentra; de lo contrario, retorna null.
     */
    public Simbolo buscar(String nombre) {
        // Itera desde la cima de la pila (ámbito actual) hacia la base (ámbito global).
        for (int i = ambitos.size() - 1; i >= 0; i--) {
            Map<String, Simbolo> ambito = ambitos.get(i);
            if (ambito.containsKey(nombre)) {
                return ambito.get(nombre);
            }
        }
        return null; // El símbolo no fue encontrado en ningún ámbito visible.
    }

}

/**
 * Excepción personalizada para errores semánticos.
 * Esto es más limpio que usar RuntimeException genéricas.
 */
class ErrorSemanticoException extends RuntimeException {
    private final Token token;

    public ErrorSemanticoException(String mensaje, Token token) {
        super(mensaje);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
