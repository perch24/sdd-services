FROM adoptopenjdk/openjdk11:latest
RUN mkdir /opt/app && \
    apt-get update && apt-get install -y \
        netcat
COPY build/libs/gateway-boot.jar /opt/app
COPY build/docker/ /opt/app/

WORKDIR /opt/app
RUN chmod +x entry-point.sh
ENV BOOT_JAR=gateway-boot.jar
CMD ["./entry-point.sh"]
