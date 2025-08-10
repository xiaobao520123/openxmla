FROM ubuntu:22.04

RUN apt-get update && \
    apt-get install -y mysql-server && \
    apt-get install -y openjdk-8-jdk wget && \
    rm -rf /var/lib/apt/lists/*

RUN service mysql start && \
    mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'openxmla';"

COPY scripts/init_insight.sql /tmp/init_insight.sql

RUN service mysql start && \
    mysql -uroot -popenxmla < /tmp/init_insight.sql

RUN mkdir /app
WORKDIR /app

RUN mkdir /app/lib
RUN wget https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar -O /app/lib/mysql-connector-j-8.4.0.jar
RUN wget https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.50.3.0/sqlite-jdbc-3.50.3.0.jar -O /app/lib/sqlite-jdbc-3.50.3.0.jar

COPY build /app
RUN rm -rf /app/semantic-service.jar
COPY semantic-mdx/semantic-deploy/target/semantic-service.jar /app/semantic-service.jar

ENV INSIGHT_HOME=/app
COPY scripts/startup.sh /app/startup.sh
RUN chmod +x /app/startup.sh
COPY scripts/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

EXPOSE 6068

ENTRYPOINT ["/app/entrypoint.sh"]