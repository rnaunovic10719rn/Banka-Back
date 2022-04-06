# Korisnicki servis za banku

Podaci su premesteni na PostgreSQL bazu koju cemo pokretati na Dockeru

## Docker:
U slucaju da neko nema Docker Desktop moze da ga nadje ovde `https://docs.docker.com/get-docker/`

U slucaju da neko nema neku vrstu menadzera za baze, predlazem `https://dbeaver.io/download/`

### Komande:
- `docker pull postgres` komanda za ucitavanje potrebnog image
- `docker run --name postgres-db -e POSTGRES_PASSWORD=docker -p 5432:5432 -d postgres` komanda za pokretanje containera
- `docker stop postgres-db` komanda za gasenje container-a
