FROM registry.cn-shenzhen.aliyuncs.com/yimutea-base/java:1.8.0_312
ARG project=yimutea
ARG app=yimu-server
ARG profile=prod

ENV PATH=.:$PATH

WORKDIR /opt/${project}/${app}

COPY ./VERSION                               /opt/${project}/${app}/VERSION
COPY ./${app}/target/${app}.jar              /opt/${project}/${app}/${app}.jar
COPY ./${app}/target/classes/application.yml /opt/${project}/${app}/application.yml
COPY ./provisioning/docker-entrypoint.sh     /opt/${project}/${app}/docker-entrypoint.sh
RUN chmod +x /opt/${project}/${app}/docker-entrypoint.sh
RUN mkdir -p /opt/tmp && chmod 777 /opt/tmp
EXPOSE 8080
VOLUME /opt/${project}/${app}/logs

ENTRYPOINT ["docker-entrypoint.sh"]
CMD [""]
