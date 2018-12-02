FROM adoptopenjdk/openjdk11:latest
RUN mkdir /opt/app && \
    apt-get update && apt-get install -y \
        netcat \
        iputils-ping

COPY build/libs/config-boot.jar /opt/app
COPY build/docker/ /opt/app/

WORKDIR /opt/app
RUN chmod +x *.sh

ENV BOOT_JAR=config-boot.jar
CMD ["./entry-point.sh"]
