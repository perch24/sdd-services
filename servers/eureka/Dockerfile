FROM adoptopenjdk/openjdk11:latest
RUN mkdir /opt/app && \
    apt-get update && apt-get install -y \
        iputils-ping
COPY build/libs/eureka-boot.jar /opt/app
COPY build/docker/ /opt/app/
EXPOSE 8760-8770

WORKDIR /opt/app
RUN chmod +x *.sh
ENV BOOT_JAR=eureka-boot.jar
CMD ["./entry-point.sh"]

