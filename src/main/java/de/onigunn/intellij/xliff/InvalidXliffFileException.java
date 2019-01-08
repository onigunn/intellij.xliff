package de.onigunn.intellij.xliff;

public class InvalidXliffFileException extends Exception {
    public InvalidXliffFileException(String missingTag) {
        super("Invalid XLIFF file! Missing " + missingTag + " tag.");
    }
}
