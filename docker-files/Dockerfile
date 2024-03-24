FROM mysql
ENV MYSQL_ROOT_PASSWORD mysql
ENV MYSQL_DATABASE mydb
COPY ./mydb.sql /docker-entrypoint-initdb.d/
EXPOSE 3306
ENTRYPOINT ["mysqld", "--user=root"]