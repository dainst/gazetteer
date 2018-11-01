FROM python:3-alpine

RUN apk add curl
RUN pip install 'mongo-connector[elastic]'

ENTRYPOINT ["/docker-entrypoint.sh"]