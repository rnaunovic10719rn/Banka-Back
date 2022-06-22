package rs.edu.raf.banka.racun.enums;

public enum HartijaOdVrednostiType {

    AKCIJA, FOREX, FUTURES_UGOVOR;

    public KapitalType toKapitalType()
    {
        switch (this)
        {
            case AKCIJA:
                return KapitalType.AKCIJA;
            case FOREX:
                return KapitalType.FOREX;
            case FUTURES_UGOVOR:
                return KapitalType.FUTURE_UGOVOR;
        }
        throw new ArrayIndexOutOfBoundsException();
    }
}
