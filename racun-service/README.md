##Racun servis

Sve rute zahtevaju token u headeru!


Provera stanja
GET api/racun/stanje/{racun}/{valuta}

Pregled transakcija
GET api/racun/transakcije/{username}

Uplata/Isplata
POST api/racun/transakcija
```
{
       "brojRacuna":"e428a579-5841-4a44-b8a3-3703ce903704",
       "opis":"",
       "valuta":"RSD",
       "uplata":"1000"
}
 ```