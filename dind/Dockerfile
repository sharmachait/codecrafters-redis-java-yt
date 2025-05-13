FROM alpine:latest

ENV TARGETARCH="linux-musl-x64"

RUN apk update && apk upgrade && apk add --no-cache \
    docker \
    bash \
    curl \
    git \
    icu-libs \
    jq \
    && rm -rf /var/cache/apk/*

EXPOSE 2375

WORKDIR /azp/

COPY ./start.sh ./azp/start.sh
RUN chmod +x ./azp/start.sh

ENV AGENT_ALLOW_RUNASROOT="true"

ENTRYPOINT ["sh", "-c", "dockerd & ./azp/start.sh"]