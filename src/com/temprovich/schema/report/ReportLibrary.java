package com.temprovich.schema.report;

public final class ReportLibrary {
    
    //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
    //              Virtual Machine Initialization                \\
    //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

    public static final String MALFORMED_RUNTIME_ARGS = "Invalid use of runtime arguments.\n"
                                                        + "\t- Usage: schema <script>";

    public static final String NULL_FILE_NAME = "Source files name cannot be null.";
    
    public static final String INVALID_FILE_EXTENSION = "Invalid file extension on file '{0}'. Files must have the extension '{1}' or '{2}'.";

    public static final String NON_EXISTENT_FILE = "File '{0}' does not exist";

    //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
    //                    UNCATEGORIZED ERRORS                    \\
    //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

    public static final String DUPLICATE_MODULE = "File '{0}' has already defined module '{1}'.";
}
