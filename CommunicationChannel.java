/**
 * (C) Copyright 2020
 */

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class CommunicationChannel {
    private BlockingQueue<Message> hqChannel;
    private BlockingQueue<Message> seChannel;
    private ReentrantLock lock;
    private Message parent;

    /**
     * Creeaza un obiect {@code CommunicationChannel}.
     */
    public CommunicationChannel() {
        hqChannel = new LinkedBlockingQueue<>();
        seChannel = new LinkedBlockingQueue<>();
        lock = new ReentrantLock();
    }

    /**
     * Pune un mesaj pe canalul exploratorilor (unde exploratorii scriu și de unde centrele
     * citesc).
     *
     * @param message
     *            mesajul de pus pe canal
     */
    public void putMessageSpaceExplorerChannel(final Message message) {
        seChannel.add(message);
    }

    /**
     * Citeste un mesaj de pe canalul exploratorilor (unde exploratorii scriu și de unde centrele
     * citesc).
     *
     * Apelul este blocant.
     *
     * @return un mesaj de pe canalul exploratorilor
     */
    public Message getMessageSpaceExplorerChannel() {
        try {
            return seChannel.take();
        } catch (InterruptedException e) {
            System.exit(1);
            return null;
        }
    }

    /**
     *  Pune un mesaj pe canalul centrelor (unde centrele scriu si de unde exploratorii citesc).
     *
     * @param message
     *            mesajul de pus pe canal
     */
    public void putMessageHeadQuarterChannel(final Message message) {
        if (message.getData().equals(HeadQuarter.END)
                || message.getData().equals(HeadQuarter.EXIT)) {
            // Mesajele END si EXIT se trimit intotdeauna imediat.
            hqChannel.add(message);
        } else {
            lock.lock();

            if (lock.getHoldCount() == 2) {
                // Daca am deja un mesaj memorat, se grupeaza cele doua mesaje pentru o
                // eficienta sporita si se trimit.
                // Se permite trimiterea unei noi secvente de mesaje.
                message.setParentSolarSystem(parent.getCurrentSolarSystem());
                hqChannel.add(message);

                lock.unlock();
                lock.unlock();
            } else {
                // Daca este primul mesaj din calup pus pe canal de HeadQuarter, se memoreaza
                // pentru a fi trimis impreuna cu urmatorul.
                parent = message;
            }
        }
    }

    /**
     * Citeste un mesaj de pe canalul centrelor (unde centrele scriu si de unde exploratorii
     * citesc).
     *
     * Apelul este blocant.
     *
     * @return un mesaj de pe canalul centrelor
     */
    public Message getMessageHeadQuarterChannel() {
        try {
            return hqChannel.take();
        } catch (InterruptedException e) {
            System.exit(1);
            return null;
        }
    }
}
