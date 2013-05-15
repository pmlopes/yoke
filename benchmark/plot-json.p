# output as png image
set terminal png

# save file to "out.png"
set output "json.png"

# graph title
set title "ab -n 8000 -c 100 -k"

# nicer aspect ratio for image size
set size 1,0.7

# y-axis grid
set grid y

# x-axis label
set xlabel "request"

# y-axis label
set ylabel "response time (ms)"

# plot data from "yoke.dat" using column 9 with smooth sbezier lines
# and title of "yoke" for the given data
plot "yoke.dat"      using 9 smooth sbezier with lines title "yoke json", \
     "expressjs.dat" using 9 smooth sbezier with lines title "expressjs json"
