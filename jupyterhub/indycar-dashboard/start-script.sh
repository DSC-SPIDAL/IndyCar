#!/bin/sh

# replace the ip part with the system variable
sed -i "s/149.165.150.51/$SOCKET_SERVER_IP/g" /dashboard/src/index.js
sed -i "s/31623/$SOCKET_SERVER_PORT/g" /dashboard/src/index.js
cd /dashboard
sass --watch src:src &
npm install -g sass --watch .
npm start
