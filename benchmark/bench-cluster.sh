#!/bin/sh
set -e

# clean up
rm *.dat || true
rm *.png || true

# run ab 3 times to warm up the server
ab -n 8000 -c 100 -k -g yoke-cluster.dat http://localhost:8080/
ab -n 8000 -c 100 -k -g yoke-cluster.dat http://localhost:8080/
ab -n 8000 -c 100 -k -g yoke-cluster.dat http://localhost:8080/
# real measurement...
sync
sleep 1
ab -n 8000 -c 100 -k -g yoke-cluster.dat http://localhost:8080/

echo "Waiting 5 seconds before running other implementation..."
sleep 5

# run ab twice to warm up the server
ab -n 8000 -c 100 -k -g expressjs-cluster.dat http://localhost:3000/
ab -n 8000 -c 100 -k -g expressjs-cluster.dat http://localhost:3000/
ab -n 8000 -c 100 -k -g expressjs-cluster.dat http://localhost:3000/
# real measurement...
sync
sleep 1
ab -n 8000 -c 100 -k -g expressjs-cluster.dat http://localhost:3000/

# plot
gnuplot plot-cluster.p
