package com.example.models;

/**
 * Tells two kinds of "temporary access" apart inside the my_library table.
 *
 * RENT - the reader paid per day for this one book (see CheckoutService).
 *        There is no library package involved.
 * LEND - the reader borrowed this book using a library package they
 *        already bought (see LibraryCheckoutServiceImpl).
 *
 * Both give the same thing in the end: the right to read a book until
 * end_date. This enum only exists so the code (and the UI) can label the
 * two differently and so a library package's book limit only ever counts
 * LEND rows, never a separate paid rental.
 */
public enum LibraryAccessType {
    RENT,
    LEND
}
