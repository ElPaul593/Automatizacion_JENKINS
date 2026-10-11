package edu.udla;

/** Operaciones simples para demostrar compilación y CI en Jenkins. */
public final class Calculadora {

    private Calculadora() {
        // Clase de utilidades: no se instancia.
    }

    public static int sumar(int a, int b) {
        return a + b;
    }

    public static int restar(int a, int b) {
        return a - b;
    }

    public static int multiplicar(int a, int b) {
        return a * b;
    }
}
