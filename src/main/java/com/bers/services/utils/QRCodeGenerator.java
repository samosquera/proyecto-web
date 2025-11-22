package com.bers.services.utils;

import java.security.SecureRandom;
import java.util.Random;

//Utilidad para generar códigos QR únicos para tickets.

//Genera strings alfanuméricos aleatorios de longitud configurable.

public class QRCodeGenerator {


    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final Random RANDOM = new SecureRandom();

    private static final int DEFAULT_LENGTH = 16;


    public static String generate() {

        return generate(DEFAULT_LENGTH);

    }

    public static String generate(int length) {

        if (length < 8 || length > 32) {

            throw new IllegalArgumentException("La longitud debe estar entre 8 y 32 caracteres");

        }

        StringBuilder code = new StringBuilder(length);

        for (int i = 0; i < length; i++) {

            int index = RANDOM.nextInt(CHARACTERS.length());

            code.append(CHARACTERS.charAt(index));

        }

        return code.toString();

    }


    public static String generateWithPrefix(String prefix, int randomLength) {

        if (prefix == null || prefix.isBlank()) {

            throw new IllegalArgumentException("El prefijo no puede estar vacío");

        }

        String randomPart = generate(randomLength);

        return prefix + "-" + randomPart;

    }

    public static String generateForTicket(Long tripId, String seatNumber) {

        String randomCode = generate(8);

        return String.format("TKT-%d-%s-%s", tripId, seatNumber, randomCode);

    }

}
