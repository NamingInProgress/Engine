package com.vke.core.ui;

public class MalformedArgumentsException extends RuntimeException {
    private static final String FMT = """
            Formal mathematical predicate:
             P(array) ≡\s
               ( (array[0] ∈ SignalTracker) ∧ (∀k ∈ ℕ₀ : (2k + 1 < n) ⇒ array[2k + 1] ∈ String) )
               ⊕
               ( ∀k ∈ ℕ₀ : (2k < n) ⇒ array[2k] ∈ String )
            """;

    public MalformedArgumentsException() {
        super(String.format("Arguments are not properly formatted! Correct format:%n%s", FMT));
    }
}
