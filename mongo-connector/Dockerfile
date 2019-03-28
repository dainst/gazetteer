FROM python:3-alpine

RUN apk add curl
RUN pip install 'elastic2-doc-manager[elastic5]'
RUN pip install 'mongo-connector[elastic5]'

ENTRYPOINT ["/docker-entrypoint.sh"]