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

### Docker Compose

Docker Compose se može koristiti na sledeća dva načina:

* Da se pokrene ceo stack (tj. ceo backend i svi potrebni servisi)
* Da se pokrenu samo potrebni servisi (baze podataka, InfluxDB, Eureka i Zuul), a da se backend pokrene ručno

Ovo je omogućeno korišćenjem Docker Compose profila, a postoje sledeći profili:

* `all`: pokreće sve
* `core`: pokreće PostgreSQL, InfluxDB, Artemis ActiveMQ, Zuul, Eureku, InfluxDBScrapper servis
* `influxdb`: pokreće InfluxDB

Da bi ste pokrenuli ceo stack, koristite sledeću komandu:

```shell
docker compose --profile=all up --build
```

Da bi ste pokrenuli samo neophodne servise (`core` profil), koristite sledeću komandu:

```shell
docker compose --profile=core up --build
```

**Napomena:** U ovom slučaju morate sve Java servise (osim Zuul-a i Eureke) pokrenuti ručno.

**Napomena 2:** Od sad više nije potrebno da radite Maven package (`mvn package`) pre pokretanje aplikacije, Docker
će to uraditi za vas.

### Manuelno

Moguće je manuelno pokrenuti sve, bez upotrebe Docker Compose-a, ali to se **NE PREPORUČUJE**!
