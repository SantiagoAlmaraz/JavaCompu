package org.example;
/**
 * Visitor para la Pasada de ejecución.
 *
 * Esta clase recorre el árbol sintáctico asumiendo que es semánticamente
 * correcto. Su única responsabilidad es interpretar las instrucciones, calcular
 * los valores de las expresiones y ejecutar el programa.
 *
 * El tipo de retorno genérico es <Integer> porque las expresiones en nuestro
 * lenguaje evalúan a un valor numérico (0 para false, 1 para true). Las
 * sentencias pueden devolver un valor dummy (como 0 o null).
 */
public class EjecutorVisitor extends LanguageBaseVisitor<Integer> {

    // El ejecutor necesita su propia tabla de símbolos para almacenar los valores de las variables en tiempo de ejecución.
    private final TablaSimbolos tablaSimbolos = new TablaSimbolos();

    // --- Métodos de visita para las Sentencias ---

    /**
     * Visita la regla 'declaracion'.
     * ANTLR Rule: declaracion: INT ID IGUAL expresion;
     *
     * Acción: Calcula el valor de la expresión de la derecha, crea un nuevo
     * símbolo con ese valor y lo inserta en la tabla de símbolos.
     */
    @Override
    public Integer visitDeclaracion(LanguageParser.DeclaracionContext ctx) {
        // 1. Calcular el valor de la expresión de la derecha.
        Integer valor = visit(ctx.expresion());

        String nombreVar = ctx.ID().getText();
        String tipo = ctx.INT().getText();

        // 2. Crear el Símbolo con su valor inicial y guardarlo.
        // Usamos el nuevo constructor de Simbolo.
        Simbolo simbolo = new Simbolo(nombreVar, tipo, ctx.ID().getSymbol(), valor);
        tablaSimbolos.insertar(simbolo); // No lanzará excepción porque la Pasada 1 ya lo validó.

        return 0; // Las sentencias no devuelven un valor relevante.
    }

    /**
     * Visita la regla 'asignacion'.
     * ANTLR Rule: asignacion: ID IGUAL expresion;
     *
     * Acción: Busca la variable en la tabla de símbolos y actualiza su valor.
     */
    @Override
    public Integer visitAsignacion(LanguageParser.AsignacionContext ctx) {
        String nombreVar = ctx.ID().getText();
        Integer nuevoValor = visit(ctx.expresion());

        // Buscamos el símbolo. Tenemos la garantía de que existe gracias a la Pasada 1.
        Simbolo simbolo = tablaSimbolos.buscar(nombreVar);
        simbolo.setValor(nuevoValor);

        return 0;
    }

    /**
     * Visita la regla 'impresion'.
     * ANTLR Rule: impresion: PRINT PAREN_ABIERTO expresion PAREN_CERRADO;
     *
     * Acción: Evalúa la expresión interna e imprime el resultado en la consola.
     */
    @Override
    public Integer visitPrint(LanguageParser.PrintContext ctx) {
        Integer valor = visit(ctx.expresion());
        System.out.println(valor);
        return 0;
    }

    /**
     * Visita la regla 'sentenciaSi'.
     * ANTLR Rule: sentenciaSi: IF PAREN_ABIERTO expresion PAREN_CERRADO bloque;
     *
     * Acción: Evalúa la condición. Si el resultado es '1' (true), visita el bloque.
     */
    @Override
    public Integer visitIf(LanguageParser.IfContext ctx) {
        Integer condicion = visit(ctx.expresion());
        // En nuestro lenguaje, '1' representa 'true'.
        if (condicion == 1) {
            visit(ctx.bloque());
        }
        return 0;
    }

    // --- Métodos de visita para las Expresiones (donde se calculan valores) ---

    /**
     * Visita la regla de suma.
     * ANTLR Rule: expresion: expresion SUMA expresion # ExpSuma
     *
     * Acción: Evalúa recursivamente los operandos y devuelve su suma.
     */
    @Override
    public Integer visitExpSuma(LanguageParser.ExpSumaContext ctx) {
        Integer izq = visit(ctx.expresion(0));
        Integer der = visit(ctx.expresion(1));
        return izq + der;
    }

    /**
     * Visita la regla de resta.
     * ANTLR Rule: expresion: expresion RESTA expresion # ExpResta
     */
    @Override
    public Integer visitExpResta(LanguageParser.ExpRestaContext ctx) {
        Integer izq = visit(ctx.expresion(0));
        Integer der = visit(ctx.expresion(1));
        return izq - der;
    }

    /**
     * Visita un identificador en una expresión.
     * ANTLR Rule: expresion: ... | ID # ExpId
     *
     * Acción: Busca la variable en la tabla de símbolos y devuelve su valor actual.
     */
    @Override
    public Integer visitExpId(LanguageParser.ExpIdContext ctx) {
        String nombreVar = ctx.ID().getText();
        Simbolo simbolo = tablaSimbolos.buscar(nombreVar);
        return simbolo.getValor();
    }

    /**
     * Visita un número literal.
     * ANTLR Rule: expresion: ... | NUM # ExpNum
     */
    @Override
    public Integer visitExpNum(LanguageParser.ExpNumContext ctx) {
        return Integer.parseInt(ctx.NUM().getText());
    }

    /**
     * Visita un literal booleano.
     * ANTLR Rule: expresion: ... | (TRUE | FALSE) # ExpBoolean
     */
    @Override
    public Integer visitExpBoolean(LanguageParser.ExpBooleanContext ctx) {
        // Representamos true como 1 y false como 0.
        return ctx.getText().equals("true") ? 1 : 0;
    }

    // --- Gestión de Ámbitos ---

    @Override
    public Integer visitBloque(LanguageParser.BloqueContext ctx) {
        tablaSimbolos.abrirAmbito();
        visitChildren(ctx);
        tablaSimbolos.cerrarAmbito();
        return 0;
    }
}
