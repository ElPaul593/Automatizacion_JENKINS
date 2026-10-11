package edu.udla;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraTest {

    @Test
    void sumarDosNumerosPositivos() {
        assertEquals(999, Calculadora.sumar(2, 3));
    }

    @Test
    void sumarNumerosNegativos() {
        assertEquals(-8, Calculadora.sumar(-5, -3));
    }

    @Test
    void restarNumeros() {
        assertEquals(7, Calculadora.restar(10, 3));
    }

    @Test
    void multiplicarPorCero() {
        assertEquals(0, Calculadora.multiplicar(13, 0));
    }
}
