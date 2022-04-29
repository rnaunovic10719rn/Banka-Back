# Backend aplikacije za banku

## Preduslovi

Da bi se backend uspešno pokrenuo potrebno je zadovoljiti sledeće preduslove.

### Docker Compose

Ukoliko se koristi Docker Compose, potrebno je imati:

* Docker 18.09 ili noviji
* Docker Compose 3+
* Uključen BuildKit na Dockeru

BuildKit je uključen po default-u na Windows operativnom sistemu. Na Linux-u zavisi od instalacije, ali često nije
uključen, pa ga je potrebno ručno uključiti. Ukoliko ste na Linux-u, pogledajte sledeći dokument za [objašnjenje kako
da uključite BuildKit](./docs/buildkit-linux.md).

### Manuelno

Backend je takođe moguće pokrenuti manuelno, što može biti olakšavajuće u development/debugging fazi. Za to je potrebno
imate:

* Java 17
* PostgreSQL 14 sa dve baze podataka
  * Po defaultu servisi su konfigurisani da rade sa dve različite instance PostgreSQL-a. To se može promeniti
    modifikacijom `application.properties` fajlova u `user_service` i `berza` servisima
* InfluxDB 2.1.1
* Artemis/ActiveMQ

Moguće je podići sve potrebne servise sa Docker Compose-om, bez da se podižu mikroservisi aplikacije. Za više
informacija o tome pogledajte sledeću sekciju.

## Pokretanje aplikacije

### Tokeni i kredencijali

Da bi se ceo stack uspešno pokrenuo, potrebno je ubaciti tokene i kredencijale na određena mesta. Potrebni su vam
sledeći tokeni:

* AlphaVantage API key: može se generisati na [sledećem sajtu][alphavantage-token]
  * Potrebno ga je ubaciti u `berza/src/main/java/rs/edu/raf/banka/berza/configuration/SpringConfig.java` umesto
    `INSERT_API_KEY`
  * Potrebno ga je ubaciti u `InfluxScrapper/InfluxScrapper/Models/Constants.cs` umesto `alpha-key`
* Nasdaq API key: može se generisati registracijom na [Nasdaq][nasdaq-reg]
  * Potrebno ga je ubaciti u `InfluxScrapper/InfluxScrapper/Models/Constants.cs` umesto `nasdaq-key`
* Email nalog: obratiti se vašem team leadu za kredencijale
  * Kredencijale je potrebno ubaciti u `docker-compose.yml` ili u 
    `mail-service/src/main/resources/application.properties` ako se servis pokreće ručno 

**BITNO: Pazite da ne commitujute API ključeve i kredencijale na Git-u!**

[alphavantage-token]: https://www.alphavantage.co/support/#api-key
[nasdaq-reg]: https://data.nasdaq.com/sign-up

### Docker Compose

Docker Compose se može koristiti na sledeća dva načina:

* Da se pokrene ceo stack (tj. ceo backend i svi potrebni servisi)
* Da se pokrenu samo potrebni servisi (baze podataka, InfluxDB), a da se backend pokrene ručno

Ovo je omogućeno korišćenjem Docker Compose profila, a postoje sledeći profili:

* `all`: pokreće sve
* `core`: pokreće PostgreSQL, InfluxDB, Artemis ActiveMQ, InfluxDBScrapper servis
* `influxdb`: pokreće InfluxDB

Da bi ste pokrenuli ceo stack, koristite sledeću komandu:

```shell
docker compose --profile=all up --build
```

Da bi ste pokrenuli samo neophodne servise (`core` profil), koristite sledeću komandu:

```shell
docker compose --profile=core up --build
```

**Pre pokretanja aplikacije je bitno da ubacite tokene i kredencijale kao što je opisano iznad!**

Ako koristite `all` profil, Docker Compose će pokrenuti sve za vas, tj. aplikacija je spremna za korišćenje.

Ako koristite `core` profil, biće pokrenuti samo gore-pomenuti servisi. Ostale servise (user-service, mail-service i
berza) je potrebno pokrenuti ručno i to možete učiniti preko IntelliJ-a kao što bi pokrenuli bilo koju Java aplikaciju.

**Napomena:** Od sad više nije potrebno da radite Maven package (`mvn package`) pre pokretanje aplikacije, Docker
će to uraditi za vas.

### Manuelno

Moguće je manuelno pokrenuti sve, bez upotrebe Docker Compose-a, ali to se **NE PREPORUČUJE**!
