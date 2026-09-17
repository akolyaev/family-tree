package com.akolyaev.family_tree.util;

import java.time.LocalDate;

public final class MaskingUtil {

    private MaskingUtil() {
        // utility class
    }

    /**
     * Masks a last name: first character + asterisks for the rest.
     * "Иванов" -> "И*****"
     * "Smith" -> "S*****"
     * "A" -> "A"
     */
    public static String maskLastName(String lastName) {
        if (lastName == null || lastName.isEmpty()) {
            return lastName;
        }
        return lastName.charAt(0) + "*".repeat(lastName.length() - 1);
    }

    /**
     * Extracts only the year from a LocalDate.
     * "1990-01-01" -> "1990"
     * null -> null
     */
    public static String maskDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.valueOf(date.getYear());
    }
}
