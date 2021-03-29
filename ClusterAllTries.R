# dendogram
d <- as.dist(travelMatrix)
mds.coor <- cmdscale(d)
plot(mds.coor[,1], mds.coor[,2], type="n", xlab="", ylab="")
text(jitter(mds.coor[,1]), jitter(mds.coor[,2]), rownames(mds.coor), cex=0.8)
abline(h=0,v=0,col="gray75")
plot(hclust(dist(1-travelMatrix), method="complete"))

#hierarchical clustering
rect.hclust(hclust, k = 20, # k is used to specify the number of clusters 
            border = "blue")

#kmeans clustering
fit <- kmeans(travelMatrix, 20)

# Model Based Clustering
library(mclust)
fit <- Mclust(travelMatrix)
plot(fit) # plot results
summary(fit) # display the best model

# Ward Hierarchical Clustering with Bootstrapped p values
library(pvclust)
fit <- pvclust(travelMatrix, method.hclust="ward",
               method.dist="euclidean")
plot(fit) # dendogram with p values
# add rectangles around groups highly supported by the data
pvrect(fit, alpha=.95)