package rs.edu.raf.banka.racun.exceptions;

public class InvalidCompanyException extends RuntimeException {

    public static final String MESSAGE_DOES_NOT_EXIST = "Provided company does not exist";

    public InvalidCompanyException(String message) {
        super(message);
    }
}
