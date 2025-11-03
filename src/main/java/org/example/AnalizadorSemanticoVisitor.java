package org.example;

import org.antlr.v4.runtime.Token;
import org.example.LanguageBaseVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor para la Pasada 1: Análisis Semántico con Comprobación de Tipos.
 *
 * El tipo de retorno genérico es <String> porque los métodos que visitan
 * expresiones ahora deben devolver el tipo de dato de la expresión que analizan
 * (ej: "int", "boolean"). Los métodos que visitan sentencias devuelven null.
 */
public class AnalizadorSemanticoVisitor extends LanguageBaseVisitor<String> {

    // --- Constantes para representar los tipos del lenguaje ---
    private static final String TIPO_INT = "int";
    private static final String TIPO_BOOLEAN = "boolean";
    private static final String TIPO_ERROR = "error"; // Un tipo especial para propagar errores.

    private final TablaSimbolos tablaSimbolos = new TablaSimbolos();
    private final List<ErrorSemantico> errores = new ArrayList<>();

    // --- Métodos públicos para obtener el resultado del análisis ---
    public List<ErrorSemantico> getErrores() {
        return errores;
    }
    public boolean hayErrores() {
        return !errores.isEmpty();
    }

    // --- Métodos de visita para las Reglas Semánticas Principales ---

    /**
     * Visita la regla 'declaracion'.
     * ANTLR Rule: declaracion: INT ID IGUAL expresion;
     *
     * Reglas Semánticas Verificadas:
     * 1. No redeclaración de variables en el mismo ámbito.
     * 2. El tipo de la expresión asignada debe coincidir con el tipo de la variable declarada.
     */
    @Override
    public String visitDeclaracion(LanguageParser.DeclaracionContext ctx) {
        String nombreVar = ctx.ID().getText();
        String tipoVar = ctx.INT().getText(); // En nuestro lenguaje, siempre es "int".
        Token token = ctx.ID().getSymbol();

        try {
            tablaSimbolos.insertar(nombreVar, tipoVar, token);
        } catch (ErrorSemanticoException e) {
            errores.add(new ErrorSemantico(e.getMessage(), e.getToken()));
        }

        // Ahora, comprobamos los tipos.
        String tipoExpresion = visit(ctx.expresion());

        // Si la expresión ya tuvo un error, no reportamos un segundo error de tipos.
        if (!tipoExpresion.equals(TIPO_ERROR) && !tipoExpresion.equals(tipoVar)) {
            errores.add(new ErrorSemantico(
                    "No se puede asignar un valor de tipo '" + tipoExpresion + "' a una variable de tipo '" + tipoVar + "'.",
                    ctx.IGUAL().getSymbol()
            ));
        }
        return null; // Las sentencias no tienen un tipo.
    }

    /**
     * Visita la regla 'asignacion'.
     * ANTLR Rule: asignacion: ID IGUAL expresion;
     *
     * Reglas Semánticas Verificadas:
     * 1. La variable debe estar declarada.
     * 2. El tipo de la expresión debe coincidir con el tipo de la variable.
     */
    @Override
    public String visitAsignacion(LanguageParser.AsignacionContext ctx) {
        String nombreVar = ctx.ID().getText();
        Token token = ctx.ID().getSymbol();
        Simbolo simbolo = tablaSimbolos.buscar(nombreVar);

        if (simbolo == null) {
            errores.add(new ErrorSemantico("La variable '" + nombreVar + "' no ha sido declarada.", token));
        }

        String tipoExpresion = visit(ctx.expresion());

        // Si la variable no fue declarada o la expresión tuvo un error, no continuamos.
        if (simbolo != null && !tipoExpresion.equals(TIPO_ERROR)) {
            if (!tipoExpresion.equals(simbolo.getTipo())) {
                errores.add(new ErrorSemantico(
                        "No se puede asignar un valor de tipo '" + tipoExpresion + "' a una variable de tipo '" + simbolo.getTipo() + "'.",
                        ctx.IGUAL().getSymbol()
                ));
            }
        }
        return null; // Las sentencias no tienen un tipo.
    }

    /**
     * Visita la regla 'sentenciaSi'.
     * ANTLR Rule: if: IF PAREN_ABIERTO expresion PAREN_CERRADO bloque;
     *
     * Regla Semántica Verificada: La expresión dentro de la condición debe ser de tipo 'boolean'.
     */
    @Override
    public String visitIf(LanguageParser.IfContext ctx) {
        String tipoCondicion = visit(ctx.expresion());

        if (!tipoCondicion.equals(TIPO_ERROR) && !tipoCondicion.equals(TIPO_BOOLEAN)) {
            errores.add(new ErrorSemantico(
                    "La condición de la sentencia 'if' debe ser de tipo 'boolean', pero se encontró de tipo '" + tipoCondicion + "'.",
                    ctx.PAREN_ABIERTO().getSymbol()
            ));
        }
        visit(ctx.bloque()); // Continuamos validando el bloque.
        return null; // Las sentencias no tienen un tipo.
    }

    // --- Métodos de visita que devuelven el TIPO de las Expresiones ---

    /**
     * Visita una expresión de suma.
     * ANTLR Rule: expresion: expresion SUMA expresion # ExpSuma
     *
     * Regla Semántica Verificada: Ambos operandos deben ser de tipo 'int'.
     */
    @Override
    public String visitExpSuma(LanguageParser.ExpSumaContext ctx) {
        String tipoIzq = visit(ctx.expresion(0));
        String tipoDer = visit(ctx.expresion(1));

        if (tipoIzq.equals(TIPO_ERROR) || tipoDer.equals(TIPO_ERROR)) {
            return TIPO_ERROR; // Propagamos el error para evitar mensajes en cascada.
        }

        if (tipoIzq.equals(TIPO_INT) && tipoDer.equals(TIPO_INT)) {
            return TIPO_INT; // El resultado de int + int es int.
        }

        errores.add(new ErrorSemantico(
                "El operador '+' solo se puede aplicar a operandos de tipo 'int', pero se encontraron '" + tipoIzq + "' y '" + tipoDer + "'.",
                ctx.SUMA().getSymbol()
        ));
        return TIPO_ERROR;
    }

    /**
     * Visita una expresión de resta. (Lógica idéntica a la suma)
     * ANTLR Rule: expresion: expresion RESTA expresion # ExpResta
     */
    @Override
    public String visitExpResta(LanguageParser.ExpRestaContext ctx) {
        String tipoIzq = visit(ctx.expresion(0));
        String tipoDer = visit(ctx.expresion(1));

        if (tipoIzq.equals(TIPO_ERROR) || tipoDer.equals(TIPO_ERROR)) {
            return TIPO_ERROR;
        }

        if (tipoIzq.equals(TIPO_INT) && tipoDer.equals(TIPO_INT)) {
            return TIPO_INT;
        }

        errores.add(new ErrorSemantico(
                "El operador '-' solo se puede aplicar a operandos de tipo 'int', pero se encontraron '" + tipoIzq + "' y '" + tipoDer + "'.",
                ctx.RESTA().getSymbol()
        ));
        return TIPO_ERROR;
    }

    /**
     * Visita un identificador en una expresión.
     * ANTLR Rule: expresion: ... | ID # ExpId
     */
    @Override
    public String visitExpId(LanguageParser.ExpIdContext ctx) {
        String nombreVar = ctx.ID().getText();
        Simbolo simbolo = tablaSimbolos.buscar(nombreVar);
        if (simbolo == null) {
            errores.add(new ErrorSemantico("La variable '" + nombreVar + "' no ha sido declarada.", ctx.ID().getSymbol()));
            return TIPO_ERROR;
        }
        return simbolo.getTipo(); // Devuelve el tipo guardado en la tabla de símbolos.
    }

    /**
     * Visita un número literal.
     * ANTLR Rule: expresion: ... | NUM # ExpNum
     */
    @Override
    public String visitExpNum(LanguageParser.ExpNumContext ctx) {
        return TIPO_INT;
    }

    /**
     * Visita un literal booleano.
     * ANTLR Rule: expresion: ... | (TRUE | FALSE) # ExpBoolean
     */
    @Override
    public String visitExpBoolean(LanguageParser.ExpBooleanContext ctx) {
        return TIPO_BOOLEAN;
    }

    // --- Métodos de visita que no requieren lógica semántica compleja ---

    @Override
    public String visitBloque(LanguageParser.BloqueContext ctx) {
        tablaSimbolos.abrirAmbito();
        visitChildren(ctx);
        tablaSimbolos.cerrarAmbito();
        return null; // Un bloque no tiene tipo.
    }

    @Override
    public String visitPrint(LanguageParser.PrintContext ctx) {
        visitChildren(ctx);
        return null; // Una sentencia no tiene tipo.
    }
}