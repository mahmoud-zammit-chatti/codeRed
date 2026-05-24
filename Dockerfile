FROM ubuntu:latest
LABEL authors="niri"

ENTRYPOINT ["top", "-b"]