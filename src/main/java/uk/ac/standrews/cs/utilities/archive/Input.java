/*
 * Copyright 2021 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module utilities.
 *
 * utilities is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * utilities is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with utilities. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.utilities.archive;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Utility that provides input readers.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@SuppressWarnings("unused")
public final class Input {

    private static final BufferedReader SYSTEM_READER = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Returns the next line from the console.
     *
     * @param prompt the message to be prompted to the user
     * @return the next line from the console
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings("WeakerAccess")
    public static String readLine(final String prompt) throws IOException {

        System.out.print(prompt);
        return SYSTEM_READER.readLine();
    }

    /**
     * Reads an integer from the command line.
     *
     * @param prompt the message to be prompted
     * @return the integer
     * @throws IOException           if an I/O error occurs
     * @throws NumberFormatException if user input cannot be parsed as integer
     * @see Integer#parseInt(String)
     * @see #readLine(String)
     */
    public static int readInt(final String prompt) throws NumberFormatException, IOException {

        return Integer.parseInt(readLine(prompt));
    }

    /**
     * Gets a masked string, from the console if {@code System.console() != null}, otherwise using a Swing dialog.
     *
     * @param prompt the user prompt
     * @return the string entered
     * @see #readPassword(String)
     */
    public static String readMaskedLine(final String prompt) {

        final char[] password = readPassword(prompt);
        return password == null ? null : new String(password);
    }

    /**
     * Prompts the given message and reads a password or passphrase.
     * If {@code System.console() != null}, the string is read via command-line with echoing disabled; otherwise using a GUI with masked input.
     *
     * @param prompt the message to be prompted to the user
     * @return A character array containing the password or passphrase, not including any line-termination characters, or {@code null} if an end of stream has been reached.
     * @see Console#readPassword()
     */
    @SuppressWarnings("WeakerAccess")
    public static char[] readPassword(final String prompt) {

        final Console console = System.console();
        return console != null ? console.readPassword(prompt) : readPasswordViaGUI(prompt);
    }

    /**
     * Converts a chars to bytes.
     *
     * @param chars the chars to convert
     * @return the chars as bytes
     */
    public static byte[] toBytes(final char[] chars) {

        final byte[] bytes;
        if (chars == null) {
            bytes = null;
        } else {
            bytes = new byte[chars.length];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) chars[i];
            }
        }
        return bytes;
    }

    private static char[] readPasswordViaGUI(final String prompt) {

        final JPanel panel = new JPanel(new BorderLayout());
        final JLabel prompt_label = new JLabel(prompt);
        final JPasswordField password_field = new JPasswordField(10);
        panel.add(prompt_label, BorderLayout.NORTH);
        panel.add(password_field, BorderLayout.CENTER);
        final JOptionPane option_pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE);
        final JDialog dialog = option_pane.createDialog("Password");
        try {
            //Put the focus on the textfield.
            password_field.addAncestorListener(new AncestorListener() {

                @Override
                public void ancestorAdded(final AncestorEvent e) {

                    final JComponent component = e.getComponent();
                    component.requestFocusInWindow();
                }

                @Override
                public void ancestorMoved(final AncestorEvent e) {

                    //ignore;
                }

                @Override
                public void ancestorRemoved(final AncestorEvent e) {

                    //ignore;
                }
            });

            dialog.setVisible(true);
            return !option_pane.getValue().equals(JOptionPane.OK_OPTION) ? null : password_field.getPassword();
        } finally {
            dialog.dispose();
        }
    }
}
