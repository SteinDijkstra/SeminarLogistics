library(readxl)
traveltime_matrix <- read_excel("C:/Users/mwd/Dropbox/0 - Erasmus Universiteit/0 - Jaar 3/4- Seminar Logistics/traveltime_matrix_triangleeq.xlsx", col_names = FALSE)
travelMatrix <- as.matrix(traveltime_matrix)
