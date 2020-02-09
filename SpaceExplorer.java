/**
 * (C) Copyright 2020
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class SpaceExplorer extends Thread {
    private Integer hashCount;
    private static Set<Integer> discovered;
    private CommunicationChannel channel;

    /**
     * Creeaza un obiect {@code SpaceExplorer}.
     *
     * @param hashCount
     *            numarul de decodari necesare
     * @param initialDiscovered
     *            set cu ID-urile sistemelor deja vizitate
     * @param channel
     *            canalul de comunicatie exploratori - centre
     */
    SpaceExplorer(final Integer hashCount, final Set<Integer> initialDiscovered,
                  final CommunicationChannel channel) {
        this.hashCount = hashCount;

        if (discovered == null) {
            discovered = new HashSet<>(initialDiscovered);
        }

        this.channel = channel;
    }

    /**
     * Metoda run a threadurilor Space Explorer.
     *
     * Un explorer citeste mesaje primite de la HQ, cat timp se poate.
     */
    @Override
    public void run() {
        while (true) {
            Message message = channel.getMessageHeadQuarterChannel();

            if (message.getData().equals(HeadQuarter.EXIT)) {
                return;
            }

            if (message.getData().equals(HeadQuarter.END)) {
                continue;
            }

            if (!discovered.contains(message.getCurrentSolarSystem())) {
                discovered.add(message.getCurrentSolarSystem());

                // Se inlocuieste frecventa codificata cu decodificarea ei si se trimite inapoi.
                String frequency = encryptMultipleTimes(message.getData(), hashCount);
                message.setData(frequency);

                channel.putMessageSpaceExplorerChannel(message);
            }
        }
    }

    /**
     * Aplica functia de hash de mai multe ori unui sir (decodifica o frecventa).
     *
     * @param input
     *            frecventa codificata
     * @param count
     *            numarul de decodificari necesare
     * @return frecventa decodificata
     */
    private String encryptMultipleTimes(final String input, final Integer count) {
        String hashed = input;

        for (int i = 0; i < count; ++i) {
            hashed = encryptThisString(hashed);
        }

        return hashed;
    }

    /**
     * Functie utilitar.
     * Aplica o data functia de hash pe sir.
     *
     * @param input
     *            sirul de decodificat
     * @return sirul decriptat o data
     */
    private static String encryptThisString(final String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // convert to string
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
