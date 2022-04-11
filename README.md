# Korisnicki servis za banku

## Docker:
U slucaju da neko nema Docker moze da ga nadje ovde `https://docs.docker.com/get-docker/`

Nije potrebno imati pokrenutu bazu lokalno , s obzirom da je isti port ukoliko imate postgre na lokalnoj masini , iskjucite pre pokretanja dockera.

## Buildovati JAR korisnckog servisa (default u target/) pre pokretanja dockera.

- `mvn clean package spring-boot:repackage`

Nakon uspesnog builda dovoljno je pokrenuti u terminalu 'docker-compose up'


### Komande:
- `docker-compose up --build -d` komanda za build i pokretanje servisa i baze (bez -d ukoliko zelite da pratite console log)


