package rs.edu.raf.banka.mailservice.exceptions;

public class BadMessage extends RuntimeException {

    public BadMessage() {
        super("Bad message.");
    }
}
