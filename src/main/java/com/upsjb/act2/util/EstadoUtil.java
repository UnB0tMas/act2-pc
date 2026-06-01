package com.upsjb.act2.util;

public final class EstadoUtil {

    public static final String ACTIVO = "1";
    public static final String INACTIVO = "0";

    private EstadoUtil() {
    }

    public static String normalizar(String estado) {
        if (estado == null || estado.isBlank()) {
            return ACTIVO;
        }

        String value = estado.trim().toUpperCase();

        return switch (value) {
            case "1", "A", "ACTIVO", "TRUE", "S", "SI", "SÍ" -> ACTIVO;
            case "0", "I", "INACTIVO", "FALSE", "N", "NO" -> INACTIVO;
            default -> value.substring(0, 1);
        };
    }

    public static String normalizarFiltro(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }

        return normalizar(estado);
    }

    public static String invertir(String estadoActual) {
        return esActivo(estadoActual) ? INACTIVO : ACTIVO;
    }

    public static boolean esActivo(String estado) {
        if (estado == null || estado.isBlank()) {
            return false;
        }

        String value = estado.trim().toUpperCase();
        return "1".equals(value) || "A".equals(value) || "ACTIVO".equals(value);
    }

    public static boolean esInactivo(String estado) {
        if (estado == null || estado.isBlank()) {
            return false;
        }

        String value = estado.trim().toUpperCase();
        return "0".equals(value) || "I".equals(value) || "INACTIVO".equals(value);
    }

    public static String etiqueta(String estado) {
        if (estado == null || estado.isBlank()) {
            return "Sin estado";
        }

        String value = estado.trim().toUpperCase();

        return switch (value) {
            case "1", "A", "ACTIVO", "TRUE" -> "Activo";
            case "0", "I", "INACTIVO", "FALSE" -> "Inactivo";
            case "N" -> "Anulado";
            case "P" -> "Pendiente";
            default -> estado.trim();
        };
    }
}