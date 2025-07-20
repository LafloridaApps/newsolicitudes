package com.newsolicitudes.newsolicitudes.utlils;

public class PersonaUtils {

    private PersonaUtils() {

    }

    public static Boolean validateRut(Integer rut, String vRut) {
        // Asegurarse de que el RUT no sea nulo, no vacío y que el VRUT no sea nulo ni
        // vacío
        if (rut == null || vRut.isEmpty()) {
            return false;
        }


        // Calcular el dígito verificador basado en el RUT
        char dvCalculado = calculateVerifyDigit(rut.toString());

        // Comparar el dígito verificador calculado con el proporcionado (ignorar
        // mayúsculas/minúsculas)
        return String.valueOf(dvCalculado).equalsIgnoreCase(vRut);
    }

    private static char calculateVerifyDigit(String rutNumeros) {
        int suma = 0;
        int multiplicador = 2;

        // Iterar sobre el RUT desde el último dígito hacia el primero
        for (int i = rutNumeros.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(rutNumeros.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }

        int resto = suma % 11;
        int digito = 11 - resto;

        // Retornar el dígito verificador correspondiente
        return switch (digito) {
            case 11 -> '0';
            case 10 -> 'K';
            default -> (char) (digito + '0');
        };
    }

}
