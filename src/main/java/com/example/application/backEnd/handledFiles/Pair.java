package com.example.application.backEnd.handledFiles;

/**
 * Αυτή η κλάση διαχειρίζεται τα ζευγάρια. Χρησιμοποιείται στην κλάση HandledJsonFile
 * στη μέθοδο createImagePath
 * @param <L> αριστερό μέλος
 * @param <R> δεξιό μέλος
 */

public class Pair<L,R> {

    private R right;
    private L left;

    /**
     * Κατασκευαστές
     */
    public Pair(){}

    public Pair( L left ,R right){
        this.right = right;
        this.left = left;
    }

    /**
     * @return Επιστρέφει το δεξιό μέλος
     */
    public R getRight(){
        return right;
    }
    /**
     * Επιστρέφει το αριστερό μέλος
     */
    public L getLeft(){
        return left;
    }

    /**
     * Μέθοδος μετάλλαξης του δεξιού μέλους
     * @param right τιμή του δεξιού μέλους
     */

    public void setRight(R right) {
        this.right = right;
    }

    /**
     * Μέθοδος μετάλλαξης του αριστερού μέλους
     * @param left τιμή του αριστερού μέλους
     */

    public void setLeft(L left) {
        this.left = left;
    }
}
