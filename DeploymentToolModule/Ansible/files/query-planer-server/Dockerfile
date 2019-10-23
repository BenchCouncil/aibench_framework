FROM tiangolo/uwsgi-nginx-flask:python3.7

ENV LISTEN_PORT 8080
EXPOSE 8080

COPY ./pip.conf /etc/pip.conf
COPY ./Cython.txt Cython.txt
COPY ./requirement.txt requirement.txt
RUN pip install -r Cython.txt
RUN pip install -r requirement.txt

COPY ./custom.conf /etc/nginx/conf.d/custom.conf
COPY ./app /app
