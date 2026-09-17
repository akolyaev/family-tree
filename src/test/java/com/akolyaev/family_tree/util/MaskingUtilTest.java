package com.akolyaev.family_tree.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MaskingUtilTest {

    @Test
    void maskLastName_withCyrillicName() {
        assertEquals("И*****", MaskingUtil.maskLastName("Иванов"));
    }

    @Test
    void maskLastName_withEnglishName() {
        assertEquals("S****", MaskingUtil.maskLastName("Smith"));
    }

    @Test
    void maskLastName_withSingleChar() {
        assertEquals("A", MaskingUtil.maskLastName("A"));
    }

    @Test
    void maskLastName_withTwoChars() {
        assertEquals("A*", MaskingUtil.maskLastName("Ab"));
    }

    @Test
    void maskLastName_withNull() {
        assertNull(MaskingUtil.maskLastName(null));
    }

    @Test
    void maskLastName_withEmpty() {
        assertEquals("", MaskingUtil.maskLastName(""));
    }

    @Test
    void maskDate_withValidDate() {
        assertEquals("1990", MaskingUtil.maskDate(LocalDate.of(1990, 1, 1)));
    }

    @Test
    void maskDate_withDifferentDate() {
        assertEquals("1985", MaskingUtil.maskDate(LocalDate.of(1985, 5, 15)));
    }

    @Test
    void maskDate_withNull() {
        assertNull(MaskingUtil.maskDate(null));
    }
}
