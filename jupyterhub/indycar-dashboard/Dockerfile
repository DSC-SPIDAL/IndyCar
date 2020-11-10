FROM node:12

RUN git clone https://github.com/DSC-SPIDAL/IndyCar.git

RUN mv IndyCar/dashboard /  && \
    rm -rf IndyCar

WORKDIR /dashboard

RUN npm install -g sass --watch .

COPY start-script.sh /

CMD /start-script.sh

