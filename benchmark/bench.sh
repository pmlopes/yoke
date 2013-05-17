#!/bin/sh
set -e

# clean up
rm *.dat || true
rm *.png || true

# run ab 3 times to warm up the server
ab -n 8000 -c 100 -k -g yoke.dat http://localhost:8080/json
# wait 5 seconds...
echo "Waiting 5 seconds before measurement..."
sleep 5
ab -n 8000 -c 100 -k -g yoke.dat http://localhost:8080/json

echo "Waiting 5 seconds before running other implementation..."
sleep 5

# run ab twice to warm up the server
ab -n 8000 -c 100 -k -g expressjs.dat http://localhost:8000/json
echo "Waiting 5 seconds before measurement..."
sleep 5
ab -n 8000 -c 100 -k -g expressjs.dat http://localhost:8000/json

# plot
gnuplot plot-json.p

echo "Waiting 5 seconds before running 2nd test"
sleep 5

# run ab once to warm up the server
ab -n 8000 -c 100 -k -g yoke.dat http://localhost:8080/
# wait 5 seconds...
echo "Waiting 5 seconds before measurement..."
sleep 5
ab -n 8000 -c 100 -k -g yoke.dat http://localhost:8080/

echo "Waiting 5 seconds before running other implementation..."
sleep 5

# run ab twice to warm up the server
ab -n 8000 -c 100 -k -g expressjs.dat http://localhost:8000/
echo "Waiting 5 seconds before measurement..."
sleep 5
ab -n 8000 -c 100 -k -g expressjs.dat http://localhost:8000/

# plot
gnuplot plot-text.p

echo "Waiting 5 seconds before running 3rd test"
sleep 5

# run ab once to warm up the server
ab -n 8000 -c 100 -k -g yoke.dat http://localhost:8080/middleware
# wait 5 seconds...
echo "Waiting 5 seconds before measurement..."
sleep 5
ab -n 8000 -c 100 -k -g yoke.dat http://localhost:8080/middleware

echo "Waiting 5 seconds before running other implementation..."
sleep 5

# run ab twice to warm up the server
ab -n 8000 -c 100 -k -g expressjs.dat http://localhost:8000/middleware
echo "Waiting 5 seconds before measurement..."
sleep 5
ab -n 8000 -c 100 -k -g expressjs.dat http://localhost:8000/middleware

# plot
gnuplot plot-middleware.p
