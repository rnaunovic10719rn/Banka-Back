FROM docker.io/maven:3.8.5-openjdk-17@sha256:e299c7dd06091386e49ce6658b30d61bbf62b4287608e156c3468b52c0c78305 AS builder

ARG VERSION=0.0.1-SNAPSHOT

ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME

ADD . $HOME
RUN --mount=type=cache,target=/root/.m2 mvn -Drevision=$VERSION -Dmaven.test.skip -f $HOME/pom.xml clean package

FROM docker.io/openjdk:17-alpine@sha256:a996cdcc040704ec6badaf5fecf1e144c096e00231a29188596c784bcf858d05
LABEL org.opencontainers.image.source https://github.com/RAF-SI-2021/Banka-Back

ARG VERSION=0.0.1-SNAPSHOT
ARG SERVICE_NAME="mailservice"

COPY --from=builder /usr/app/target/$SERVICE_NAME-$VERSION.jar /app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
