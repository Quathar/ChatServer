package com.quathar.chatserver;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>Prompt</h1>
 * <br>
 * This class contains the codes to change the prompt color
 * (in System.out.print, now that there is a javafx interface we don't use this class).
 *
 * @since 2022-11-21
 * @version 2.0
 * @author Q
 */
@AllArgsConstructor
@Getter
public enum Prompt {

    ANSI_RESET ("\u001B[0m"),
    ANSI_BLACK ("\u001B[30m"),
    ANSI_RED   ("\u001B[31m"),
    ANSI_GREEN ("\u001B[32m"),
    ANSI_YELLOW("\u001B[33m"),
    ANSI_BLUE  ("\u001B[34m"),
    ANSI_PURPLE("\u001B[35m"),
    ANSI_CYAN  ("\u001B[36m"),
    ANSI_WHITE ("\u001B[37m");

    // <<-FIELD->>
    private final String code;

}